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
    
    private let refreshInterval: Double = 60 * 60 * 24
    
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
                            NSLog("Cannot load configuration, unable to access data from: \(location)")
                            NSNotificationCenter.defaultCenter().postNotificationName("Configuration Load Failed", object: nil)
                        }
                    } else {
                        NSLog("Cannot load configuration, unable to access data from: \(location)")
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

    func processConfigurationData(json: NSDictionary, managedObjectContext: NSManagedObjectContext? = nil) {
        if let versions = json["versions"] as! NSDictionary? {
            if let iosVersions = versions["ios"] as! [AnyObject]? {
                if VersionChecker.checkVersion(iosVersions) {
                    let defaults = AppGroupUtilities.userDefaults()!
                    
                    //set the new config for about
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
                        defaults.setObject(about["contact"], forKey: "about-contact")
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
                            defaults.setObject(privacy["number"], forKey: "about-privacy-number")
                        }
                        if let version = about["version"] as! NSDictionary? {
                            defaults.setObject(version["url"], forKey: "about-version-url")
                        }
                        if let website = about["website"] as! NSDictionary? {
                            defaults.setObject(website["display"], forKey: "about-website-display")
                            defaults.setObject(website["url"], forKey: "about-privwebsiteacy-url")
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
                        
                        if UIDevice.currentDevice().userInterfaceIdiom == .Pad {
                            if homeUrlTablet != nil {
                                ImageCache.sharedCache().getImage(homeUrlTablet!)
                            }
                        } else {
                            if homeUrlPhone != nil {
                                ImageCache.sharedCache().getImage(homeUrlPhone!)
                            }
                        }
                        #endif

                        let schoolLogoPhone = layout["schoolLogoPhone"] as! String?
                        defaults.setObject(schoolLogoPhone, forKey: "home-logo-stripe")
                        if schoolLogoPhone != nil {
                            ImageCache.sharedCache().getImage(schoolLogoPhone!)
                        }
                        
                        let defaultMenuIcon = (layout["defaultMenuIcon"] as! NSString).boolValue
                        if !defaultMenuIcon {
                            let menuIconUrl = layout["menuIconUrl"] as! String?
                            defaults.setObject(menuIconUrl, forKey: "menu-icon")
                            if menuIconUrl != nil {
                                ImageCache.sharedCache().getImage(menuIconUrl!)
                            }
                        }
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
                    
                    if let map = json["map"] as! NSDictionary? {
                        defaults.setObject(map["buildings"], forKey: "urls-map-buildings")
                        defaults.setObject(map["campuses"], forKey: "urls-map-campuses")
                    }
                    
                    if let directory = json["directory"] as! NSDictionary? {
                        defaults.setObject(directory["allSearch"], forKey: "urls-directory-allSearch")
                        defaults.setObject(directory["facultySearch"], forKey: "urls-directory-facultySearch")
                        defaults.setObject(directory["studentSearch"], forKey: "urls-directory-studentSearch")
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

                    dispatch_async(dispatch_get_main_queue()) {
                        let context = CoreDataManager.shared.managedObjectContext
                        
                        var currentKeys = [AnyObject]()
                        if let modules = json["mapp"] as! NSDictionary? {
                            for (key, jsonModule) in modules as! [String: AnyObject] {
                                let moduleDictionary = jsonModule as! NSDictionary
                                let _ = Module(fromDictionary: moduleDictionary as [NSObject : AnyObject], inManagedObjectContext: context, withKey: key)
                                currentKeys.append(key)
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
}