//
//  ConfigurationManager.swift
//  Mobile
//
//  Shared code to manage fetching configuration and sending updated configuration between iPhone and watch
//
//  Created by Bret Hansen on 8/24/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation
import UIKit

class ConfigurationManager: NSObject {
    
    private let refreshInterval: Double = 1200  //seconds for 20 minutes = 1200 seconds
    
    private var configurationData: NSData?
    
    lazy var urlSession: NSURLSession = {
        let config = NSURLSessionConfiguration.ephemeralSessionConfiguration()
        config.timeoutIntervalForRequest = 10
        return NSURLSession(configuration: config)
    }()
    
    // Returns the singleton instance of ConfigurationManager
    class var instance: ConfigurationManager {
        get {
            struct Singleton {
                static var instance : ConfigurationManager? = nil
                static var token : dispatch_once_t = 0
            }
            dispatch_once(&Singleton.token) { Singleton.instance = ConfigurationManager() }
            
            return Singleton.instance!
        }
    }
    
    // make init private for singleton
    override private init() {
    }
    
    func isConfigurationLoaded() -> Bool {
        return mostRecentConfigurationData() != nil
    }
    
    func shouldConfigurationBeRefreshed() -> Bool {
        var result = false
        
        // check if it is time to refresh
        if let defaults = AppGroupUtilities.userDefaults() {
            if let refreshDate = defaults.objectForKey("configuration refresh date") {
                let intervals = (NSDate().timeIntervalSinceDate(refreshDate as! NSDate)) / refreshInterval
                result = intervals >= 1
            } else {
                return true
            }
            
            
            guard let lastUpdated = defaults.stringForKey("lastUpdated-configuration") else {
                return result
            }
            
            
            if let baseConfigurationUrl = getConfigurationUrl() where result {
                let configurationUrl = baseConfigurationUrl + "?onlyLastUpdated=true"
                if let theUrl = NSURL(string: configurationUrl) {
                    do {
                        let configurationData = try NSData(contentsOfURL: theUrl, options: NSDataReadingOptions())
                        
                        
                        let jsonResults = try NSJSONSerialization.JSONObjectWithData(configurationData, options: [])
                        let lastUpdatedFromServer = jsonResults["lastUpdated"] as! String
                        result = lastUpdatedFromServer != lastUpdated
                        defaults.setObject(lastUpdatedFromServer, forKey: "lastUpdated-configuration")
                        
                    } catch {
                        print(error)
                    }
                }
                
            }
            
        }
        return result
    }
    
    func mostRecentConfigurationData() -> NSData? {
        var configurationData: NSData? = self.configurationData
        
        if configurationData == nil {
            if let defaults = AppGroupUtilities.userDefaults() {
                if let defaultConfigurationData = defaults.objectForKey("configuration data") as! NSData? {
                    configurationData = defaultConfigurationData
                }
            }
        }
        
        return configurationData
    }
    
    func getConfigurationUrl(configurationUrl pConfigurationUrl: String? = nil) -> String? {
        var configurationUrl = pConfigurationUrl
        if configurationUrl == nil {
            // use the defaults configurationUrl
            if let defaults = AppGroupUtilities.userDefaults() {
                configurationUrl = defaults.stringForKey("configurationUrl")
            }
        }
        
        return configurationUrl
    }
    
    // true return value means either configuration is loaded and doesn't need to be refreshed or configuration was refreshed
    // false return value indicates configuration has not been loaded and can't. In Watch case this may mean the user needs to select a configuration
    func refreshConfigurationIfNeeded(configurationUrl configurationUrl: String? = nil, completionHandler: ((result: AnyObject) -> Void)?) {
        var respondWithFailureToCompletionHandler = true
        
        if mostRecentConfigurationData() == nil || shouldConfigurationBeRefreshed() {
            // time to refresh
            NSLog("ConfigurationManager refreshConfigurationIfNeeded needs to be refreshed")
            loadConfiguration(configurationUrl: configurationUrl, completionHandler: completionHandler)
            respondWithFailureToCompletionHandler = false
        }
        
        if shouldMobileServerConfigurationBeRefreshed() {
            // time to refresh
            loadMobileServerConfiguration(nil)
        }
        
        if respondWithFailureToCompletionHandler {
            if let completionHandler = completionHandler {
                completionHandler(result: false)
            }
        }
    }
    
    func loadConfiguration(configurationUrl pConfigurationUrl: String? = nil, completionHandler: ((result: AnyObject) -> Void)?) {
        var handleCompletionHandler = true
        
        if let configurationUrl = getConfigurationUrl(configurationUrl: pConfigurationUrl) {
            // download the configuration
            if let theUrl = NSURL(string: configurationUrl) {
                handleCompletionHandler = false
                let task = urlSession.downloadTaskWithURL(theUrl) {
                    (location, response, error) in
                    
                    var loadSuccess = false
                    
                    #if os(iOS)
                        UIApplication.sharedApplication().networkActivityIndicatorVisible = false
                    #endif
                    
                    if (error == nil) {
                        if let configurationData = NSData(contentsOfURL: location!) {
                            loadSuccess = self.processConfigurationData(configurationData)
                            
                            if loadSuccess {
                                NSNotificationCenter.defaultCenter().postNotificationName("Configuration Load Succeeded", object: nil)
                            }
                        } else {
                            NSLog("Cannot load configuration, unable to access data from: \(location) \(error)")
                            NSNotificationCenter.defaultCenter().postNotificationName("Configuration Load Failed", object: nil)
                        }
                    } else {
                        NSLog("Cannot load configuration, unable to access data from: \(location) \(error)")
                        NSNotificationCenter.defaultCenter().postNotificationName("Configuration Load Failed", object: nil)
                    }
                    
                    if let completionHandler = completionHandler {
                        completionHandler(result: loadSuccess)
                    }
                }
                task.resume()
                #if os(iOS)
                    UIApplication.sharedApplication().networkActivityIndicatorVisible = true
                #endif
            } else {
                // failed to create NSURL
                // notify the failure
                NSLog("Cannot load configuration, unable to create NSURL from: \"\(configurationUrl)\"")
                NSNotificationCenter.defaultCenter().postNotificationName("Configuration Load Failed", object: nil)
            }
        } else {
            
            #if os(watchOS)
                handleCompletionHandler = false
                
                // don't know the configuration url - watch needs to ask phone for configuration
                WatchConnectivityManager.instance.sendActionMessage("fetch configuration", replyHandler: {
                    (reply: [String: AnyObject]) -> Void in
                    var result = false
                    var handleCompletionHandler = true
                    
                    if let configurationData = reply["configurationData"] as! NSData? {
                        self.processConfigurationData(configurationData, notifyOtherSide: false)
                        result = true
                    } else if let configurationUrl = reply["configurationUrl"] as! String? {
                        handleCompletionHandler = false
                        self.loadConfiguration(configurationUrl: configurationUrl) {
                            (loadResult) -> Void in
                            
                            if let completionHandler = completionHandler {
                                completionHandler(result: loadResult)
                            }
                        }
                    } else {
                        NSLog("configurationData is missing from the data: \(reply)")
                    }
                    
                    
                    if handleCompletionHandler {
                        if let completionHandler = completionHandler {
                            completionHandler(result: result)
                        }
                    }
                    },
                    errorHandler: {
                        (error: NSError) -> Void in
                        
                        if let completionHandler = completionHandler {
                            completionHandler(result: error)
                        }
                        NSLog("Cannot load configuration fetched from Phone")
                })
            #endif
            
            
            // No Configuration URL notify the failure
            NSLog("Cannot load configuration, no configurationUrl")
            NSNotificationCenter.defaultCenter().postNotificationName("Configuration Load Failed", object: nil)
        }
        
        if handleCompletionHandler {
            if let uCompletionHandler = completionHandler {
                uCompletionHandler(result: false)
            }
        }
    }
    
    func processConfigurationData(configurationData: NSData, notifyOtherSide: Bool = true) -> Bool {
        do {
            let json = try NSJSONSerialization.JSONObjectWithData(configurationData, options: NSJSONReadingOptions.AllowFragments) as! NSDictionary
            self.processConfigurationData(json)
            
            // cache the configuration data
            if let defaults = AppGroupUtilities.userDefaults() {
                defaults.setObject(configurationData, forKey: "configuration data")
                defaults.setObject(NSDate(), forKey: "configuration refresh date")
            }
            
            // clear watch cached data
            #if os(watchOS)
                DefaultsCache.clearAllCaches()
            #endif
            
            // let other side know
            if #available(iOS 9.0, *) {
                if notifyOtherSide {
                    WatchConnectivityManager.instance.notifyOtherSide("configurationLoaded", data: configurationData)
                }
            }
            
            NSNotificationCenter.defaultCenter().postNotificationName("Configuration Load Succeeded", object: nil)
            return true
        } catch {
            NSLog("Cannot load configuration, data doesn't parse")
            NSNotificationCenter.defaultCenter().postNotificationName("Configuration Load Failed", object: nil)
            return false
        }
    }
    
    private func setDefaultObject(defaults: NSUserDefaults, key: String, value: AnyObject?) {
        if value != nil && !(value is NSNull) {
            defaults.setObject(value, forKey: key)
        }
    }
    
    func processConfigurationData(json: NSDictionary) {
        if let versions = json["versions"] as! NSDictionary? {
            if let iosVersions = versions["ios"] as! [AnyObject]? {
                if VersionChecker.checkVersion(iosVersions) {
                    let defaults = AppGroupUtilities.userDefaults()!
                    
                    if let lastUpdated = json["lastUpdated"] as! String? {
                        defaults.setObject(lastUpdated, forKey: "lastUpdated-configuration")
                    }
                    
                    if let about = json["about"] as! NSDictionary? {
                        defaults.setObject(about["contact"], forKey: "about-contact")
                        if let email = about["email"] as! NSDictionary? {
                            defaults.setObject(email["address"], forKey: "about-email-address")
                            defaults.setObject(email["display"], forKey: "about-email-display")
                        }
                        let aboutIcon = about["icon"] as! String?
                        defaults.setObject(aboutIcon, forKey: "about-icon")
                        if aboutIcon != nil {
                            ImageCache.sharedCache().getImage(aboutIcon!)
                        }
                        let aboutLogoUrlPhone = about["logoUrlPhone"] as! String?
                        defaults.setObject(aboutLogoUrlPhone, forKey: "about-logoUrlPhone")
                        if aboutLogoUrlPhone != nil {
                            ImageCache.sharedCache().getImage(aboutLogoUrlPhone!)
                        }
                        if let phone = about["phone"] as! NSDictionary? {
                            defaults.setObject(phone["display"], forKey: "about-phone-display")
                            defaults.setObject(phone["number"], forKey: "about-phone-number")
                        }
                        if let privacy = about["privacy"] as! NSDictionary? {
                            defaults.setObject(privacy["display"], forKey: "about-privacy-display")
                            defaults.setObject(privacy["url"], forKey: "about-privacy-url")
                        }
                        if let version = about["version"] as! NSDictionary? {
                            defaults.setObject(version["url"], forKey: "about-version-url")
                        }
                        if let website = about["website"] as! NSDictionary? {
                            defaults.setObject(website["display"], forKey: "about-website-display")
                            defaults.setObject(website["url"], forKey: "about-website-url")
                        }
                    }
                    
                    if let layout = json["layout"] as! NSDictionary? {
                        defaults.setObject(layout["primaryColor"], forKey: "primaryColor")
                        defaults.setObject(layout["headerTextColor"], forKey: "headerTextColor")
                        defaults.setObject(layout["accentColor"], forKey: "accentColor")
                        defaults.setObject(layout["subheaderTextColor"], forKey: "subheaderTextColor")
                        
                        let homeUrlPhone = layout["homeUrlPhone"] as! String?
                        defaults.setObject(homeUrlPhone, forKey: "home-background")
                        
                        #if os(iOS)
                            let homeUrlTablet = layout["homeUrlTablet"] as! String?
                            defaults.setObject(homeUrlTablet, forKey: "home-tablet-background")
                            
                        
                            if let homeUrlTablet = homeUrlTablet where UIDevice.currentDevice().userInterfaceIdiom == .Pad && homeUrlTablet.characters.count > 0 {
                                ImageCache.sharedCache().getImage(homeUrlTablet)
                            } else if let homeUrlPhone = homeUrlPhone where homeUrlPhone.characters.count > 0 {
                                ImageCache.sharedCache().getImage(homeUrlPhone)
                            }
                        #endif
                    }
                    
                    if let security = json["security"] as! NSDictionary? {
                        defaults.setObject(security["url"], forKey: "login-url")
                        if let web = security["web"] {
                            defaults.setObject("browser", forKey:"login-authenticationType")
                            defaults.setObject(web["loginUrl"], forKey: "login-web-url")
                        } else if let cas = security["cas"] {
                            defaults.setObject(cas["loginType"], forKey: "login-authenticationType")
                            defaults.setObject(cas["loginUrl"], forKey: "login-web-url")
                        } else {
                            defaults.setObject("native", forKey: "login-authenticationType")
                        }
                    }
                    
                    if let mobileServerConfig = json["mobileServerConfig"] as! NSDictionary? {
                        defaults.setObject(mobileServerConfig["url"], forKey: "mobileServerConfig-url")
                    }
                    
                    if let map = json["map"] as! NSDictionary? {
                        defaults.setObject(map["buildings"], forKey: "urls-map-buildings")
                        defaults.setObject(map["campuses"], forKey: "urls-map-campuses")
                    }
                    
                    if let directory = json["directory"] as! NSDictionary? {
                        defaults.setObject(directory["allSearch"], forKey: "urls-directory-allSearch")
                        defaults.setObject(directory["facultySearch"], forKey: "urls-directory-facultySearch")
                        defaults.setObject(directory["studentSearch"], forKey: "urls-directory-studentSearch")
                        if directory["baseSearch"] != nil {
                            defaults.setObject(directory["baseSearch"], forKey: "urls-directory-baseSearch")
                        }
                    }
                    
                    if let notification = json["notification"] as! NSDictionary? {
                        if let urls = notification["urls"] as! NSDictionary? {
                            defaults.setObject(urls["registration"], forKey: "notification-registration-url")
                            defaults.setObject(urls["delivered"], forKey: "notification-delivered-url")
                        }
                    }
                    
                    // remove notifications enabled flag for this configuration until it is determined that notifications are enabled
                    defaults.removeObjectForKey("notification-enabled")
                    
                    //Google Analytics
                    if let analytics = json["analytics"] as! NSDictionary? {
                        setDefaultObject(defaults, key: "gaTracker1", value: analytics["ellucian"])
                        setDefaultObject(defaults, key: "gaTracker2", value: analytics["client"])
                    }
                    
                    if let home = json["home"] as! NSDictionary? {
                        setDefaultObject(defaults, key: "home-overlay-color", value: home["overlay"])
                        //                        setDefaultObject(defaults, key: "icons", value: analytics["icons"])
                    }

                    dispatch_async(dispatch_get_main_queue()) {
                        let context = CoreDataManager.shared.managedObjectContext
                        
                        var currentKeys = [AnyObject]()
                        if let modules = json["mapp"] as! NSDictionary? {
                            for (key, jsonModule) in modules as! [String: AnyObject] {
                                let moduleDictionary = jsonModule as! NSDictionary
                                if self.validModuleDefinition(moduleDictionary) {
                                
                                    let _ = Module(fromDictionary: moduleDictionary as [NSObject : AnyObject], inManagedObjectContext: context, withKey: key)
                                    currentKeys.append(key)
                                }
                            }
                        }
                        
                        let keysNoLongerUsedReqeust = NSFetchRequest(entityName: "Module")
                        let keysNoLongerUsedPredicate = NSPredicate(format: "NOT (internalKey IN %@)", currentKeys)
                        keysNoLongerUsedReqeust.predicate = keysNoLongerUsedPredicate
                        do {
                            let modules = try context.executeFetchRequest(keysNoLongerUsedReqeust)
                            for module in modules as! [NSManagedObject] {
                                context.deleteObject(module)
                            }
                            do {
                                try context.save()
                            } catch {
                                NSLog("Unable to save Managed Object context after deleting no longer used modules")
                            }
                            
                        } catch {
                            NSLog("Unable to query for no longer used keys")
                        }
                    }
                    
                    defaults.setObject(NSDate(), forKey: "menu updated date")
                }
            }
        }
    }
    
    //way to test if configuration is complete and ready.  If not, drop the module
    func validModuleDefinition(dictionary: NSDictionary) -> Bool {
        if dictionary["type"] as? String == "web" {
            return dictionary["urls"] != nil
        }
        return true
    }
    //MARK: Mobile server
    
    
    func shouldMobileServerConfigurationBeRefreshed() -> Bool {
        var result = false
        
        // check if it is time to refresh
        if let defaults = AppGroupUtilities.userDefaults() {
            guard let baseConfigurationUrl = defaults.stringForKey("mobileServerConfig-url") else {
                return false
            }
            
            if let refreshDate = defaults.objectForKey("mobile server configuration refresh date") {
                let intervals = (NSDate().timeIntervalSinceDate(refreshDate as! NSDate)) / refreshInterval
                result = intervals >= 1
            } else {
                return true
            }
            
            guard let lastUpdated = defaults.stringForKey("lastUpdated-mobileServerConfiguration") else {
                return result
            }
            
            
            let configurationUrl = baseConfigurationUrl + "?onlyLastUpdated=true"
            if let theUrl = NSURL(string: configurationUrl) where result {
                do {
                    let configurationData = try NSData(contentsOfURL: theUrl, options: NSDataReadingOptions())
                    
                    
                    let jsonResults = try NSJSONSerialization.JSONObjectWithData(configurationData, options: [])
                    let lastUpdatedFromServer = jsonResults["lastUpdated"] as! String
                    result = lastUpdatedFromServer != lastUpdated
                    defaults.setObject(lastUpdatedFromServer, forKey: "lastUpdated-mobileServerConfiguration")
                    
                } catch {
                    print(error)
                }
            }
            
        }
        return result
    }
    
    func loadMobileServerConfiguration(completionHandler: ((result: AnyObject) -> Void)?) {
        
        
        if let defaults = AppGroupUtilities.userDefaults() {
            guard let configurationUrl = defaults.stringForKey("mobileServerConfig-url") else {
                if let completionHandler = completionHandler {
                    completionHandler(result: false)
                }
                return
            }
            
            // download the configuration
            if let theUrl = NSURL(string: configurationUrl) {
                
                let task = urlSession.downloadTaskWithURL(theUrl) {
                    (location, response, error) in
                    
                    #if os(iOS)
                        UIApplication.sharedApplication().networkActivityIndicatorVisible = false
                    #endif
                    
                    var loadSuccess = false
                    if let httpResponse = response as? NSHTTPURLResponse {
                        if httpResponse.statusCode != 200 {
                            if let completionHandler = completionHandler {
                                completionHandler(result: loadSuccess)
                            }
                            
                            return
                        }
                    }

                    if (error == nil) {
                        if let configurationData = NSData(contentsOfURL: location!) {
                            loadSuccess = self.processMobileServerConfiguration(configurationData)
                            
                            
                            NSNotificationCenter.defaultCenter().postNotificationName("Mobile Server Configuration Load Succeeded", object: nil)
                            
                        } else {
                            NSLog("Cannot load mobile server configuration, unable to access data from: \(location)")
                            NSNotificationCenter.defaultCenter().postNotificationName("Mobile Server Configuration Load Failed", object: nil)
                        }
                    } else {
                        NSLog("Cannot load mobile server configuration, unable to access data from: \(location)")
                        NSNotificationCenter.defaultCenter().postNotificationName("Mobile Server Configuration Load Failed", object: nil)
                    }
                    
                    if let completionHandler = completionHandler {
                        completionHandler(result: loadSuccess)
                    }
                }
                task.resume()
                #if os(iOS)
                    UIApplication.sharedApplication().networkActivityIndicatorVisible = true
                #endif
            } else {
                // failed to create NSURL
                // notify the failure
                NSLog("Cannot load mobile server configuration, unable to create NSURL from: \"\(configurationUrl)\"")
            }
        } else {
            // No Configuration URL notify the failure
            NSLog("Cannot load configuration, no mobile server configurationUrl")
            NSNotificationCenter.defaultCenter().postNotificationName("Mobile Server Configuration Load Failed", object: nil)
        }
    }
    
    func processMobileServerConfiguration(responseData: NSData) -> Bool {
        
        let json = JSON(data: responseData)
        
        let defaults = AppGroupUtilities.userDefaults()!
        
        if let lastUpdated = json["lastUpdated"].string {
            defaults.setObject(lastUpdated, forKey: "lastUpdated-mobileServerConfiguration")
        }
        
        if let codebaseVersion = json["codebaseVersion"].string {
            defaults.setObject(codebaseVersion, forKey: "mobileServerCodebaseVersion")
        }
        
        dispatch_async(dispatch_get_main_queue()) {
            let context = CoreDataManager.shared.managedObjectContext
            
            let privateContext = NSManagedObjectContext(concurrencyType: .PrivateQueueConcurrencyType)
            privateContext.parentContext = context
            privateContext.undoManager = nil
            
            privateContext.performBlock { () -> Void in
                
                do {
                    let request = NSFetchRequest(entityName: "DirectoryDefinition")
                    let oldObjects = try privateContext
                        .executeFetchRequest(request)
                    
                    if let managedObjects = oldObjects as? [NSManagedObject] {
                        for object in managedObjects {
                            privateContext.deleteObject(object)
                        }
                    }
                    for (key,subJson):(String, JSON) in json["directories"].dictionary! {
                        let directory = NSEntityDescription.insertNewObjectForEntityForName("DirectoryDefinition", inManagedObjectContext: privateContext) as! DirectoryDefinition
                        
                        if(subJson["internalName"] != nil) { //todo
                            if let authenticated = subJson["authenticatedOnly"].string {
                                directory.authenticatedOnly = (authenticated == "true")
                            } else {
                                directory.authenticatedOnly = false
                            }
                            directory.internalName = subJson["internalName"].string
                            directory.displayName = subJson["displayName"].string
                            directory.key = key
                        }
                        
                    }
                    
                    
                    try privateContext.save()
                    
                    privateContext.parentContext?.performBlock({
                        do {
                            try privateContext.parentContext?.save()
                        } catch let error {
                            print (error)
                        }
                    })
                    
                    dispatch_async(dispatch_get_main_queue()) {
                        if let defaults = AppGroupUtilities.userDefaults() {
                            //                                            defaults.setObject(configurationData, forKey: "mobile server configuration data")
                            defaults.setObject(NSDate(), forKey: "mobile server configuration refresh date")
                        }
                        
                    }
                    
                } catch let error {
                    print (error)
                }
            }
            
        }
        return true
    }
    
    class func doesMobileServerSupportVersion(version: String) -> Bool {
        
        let defaults = AppGroupUtilities.userDefaults()!
        if let mobileServerCodebaseVersion = defaults.stringForKey("mobileServerCodebaseVersion") {
            if version == mobileServerCodebaseVersion {
                return true
            }
            let askedVersion = version.componentsSeparatedByString(".")
                .map {
                    Int.init($0) ?? 0
            }
            let serverVersion = mobileServerCodebaseVersion.componentsSeparatedByString(".")
                .map {
                    Int.init($0) ?? 0
            }
            return askedVersion.lexicographicalCompare(serverVersion)
        }
        return false //unknown
    }
    
}