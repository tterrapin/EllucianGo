//
//  AppDelegate.swift
//  AppDelegateMaker
//
//  Created by Jason Hocker on 2/11/16.
//  Copyright © 2016 Ellucian. All rights reserved.
//

import UIKit
import CoreData

//@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    var window: UIWindow?
    
    
    func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject: AnyObject]?) -> Bool {
        // Override point for customization after application launch.
        
        WatchConnectivityManager.instance.ensureWatchConnectivityInitialized()
        
        NSSetUncaughtExceptionHandler { exception in
            print("uncaught exception: \(exception.description)")
            print(exception.callStackSymbols)
        }
        
        //Google Analytics
        let gai = GAI.sharedInstance()
        gai.trackUncaughtExceptions = true
        gai.dispatchInterval = 20
        gai.logger.logLevel = GAILogLevel.Error
        
        //Notifications registration
        let settings = UIUserNotificationSettings(forTypes: [.Alert, .Badge, .Sound], categories: nil)
        application.registerUserNotificationSettings(settings)
        
        
        if !NSUserDefaults.standardUserDefaults().boolForKey("didMigrateToAppGroups") {
            var oldDefaults: [String : AnyObject] = NSUserDefaults.standardUserDefaults().dictionaryRepresentation()
            
            for key: String in oldDefaults.keys {
                AppGroupUtilities.userDefaults()?.setObject(oldDefaults[key], forKey: key)
            }
            if let appDomain = NSBundle.mainBundle().bundleIdentifier {
                NSUserDefaults.standardUserDefaults().removePersistentDomainForName(appDomain)
                NSUserDefaults.standardUserDefaults().setBool(true, forKey: "didMigrateToAppGroups")
            }
        }
        
        let slidingViewController = self.window?.rootViewController as? ECSlidingViewController
        if let slidingViewController = slidingViewController {
            self.slidingViewController = slidingViewController
            slidingViewController.anchorRightRevealAmount = 276
            slidingViewController.anchorLeftRevealAmount = 276
            slidingViewController.topViewAnchoredGesture = [ECSlidingViewControllerAnchoredGesture.Tapping , ECSlidingViewControllerAnchoredGesture.Panning]
            let storyboard = UIStoryboard(name: "HomeStoryboard", bundle: nil)
            let menu: UIViewController = storyboard.instantiateViewControllerWithIdentifier("Menu")
            
            if #available(iOS 9, *) {
                let direction: UIUserInterfaceLayoutDirection = UIView.userInterfaceLayoutDirectionForSemanticContentAttribute((self.window?.rootViewController!.view.semanticContentAttribute)!)
                if direction == .RightToLeft {
                    slidingViewController.underRightViewController = menu
                } else {
                    slidingViewController.underLeftViewController = menu
                }
                
            } else {
                slidingViewController.underLeftViewController = menu
            }
        }
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "applicationDidTimeout:", name: "AppTimeOut", object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "applicationDidTouch:", name: "AppTouch", object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "returnHome:", name: kSignOutNotification, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "returnHome:", name: kSignInReturnToHomeNotification, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "forceConfigurationSelection:", name: kConfigurationFetcherError, object: nil)
        if let currentUser = CurrentUser.sharedInstance() {
            if currentUser.isLoggedIn && !currentUser.remember && logoutOnStartup != false {
                currentUser.logout(false)
            }
        }
        // for when swapping the application in, honor the current logged in/out state
        logoutOnStartup = false
        
        if let prefs = AppGroupUtilities.userDefaults() {
            if let _ = prefs.stringForKey("configurationUrl") {
                
                AppearanceChanger.applyAppearanceChanges(self.window)
                NSOperationQueue.mainQueue().addOperation(OpenModuleHomeOperation())
                
            } else {
                if self.useDefaultConfiguration {
                    prefs.setObject(self.defaultConfigUrl, forKey: "configurationUrl")
                    self.loadDefaultConfiguration(self.defaultConfigUrl!)
                } else {
                    let storyboard: UIStoryboard = UIStoryboard(name: "ConfigurationSelectionStoryboard", bundle: nil)
                    let navcontroller = storyboard.instantiateViewControllerWithIdentifier("ConfigurationSelector") as! UINavigationController
                    let vc = navcontroller.childViewControllers[0] as! ConfigurationSelectionViewController
                    vc.modalPresentationStyle = .FullScreen
                    self.window?.rootViewController = navcontroller
                    AppearanceChanger.applyAppearanceChanges(self.window)
                    
                }
            }
        }
        
        UIApplication.sharedApplication().applicationIconBadgeNumber = 0
        
        if #available(iOS 9.0, *) {
            if let shortcutItem = launchOptions?[UIApplicationLaunchOptionsShortcutItemKey] as? UIApplicationShortcutItem {
                handleShortcut(shortcutItem)
                return false
            }
        }
        return true
    }
    
    func applicationWillResignActive(application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
    }
    
    func applicationDidEnterBackground(application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
        self.saveContext()
    }
    
    func applicationWillEnterForeground(application: UIApplication) {
        // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
    }
    
    func applicationDidBecomeActive(application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
        
        if let prefs = AppGroupUtilities.userDefaults() {
            let configurationUrl = prefs.stringForKey("configurationUrl")
            if let defaultConfigUrl = self.defaultConfigUrl where configurationUrl == nil && useDefaultConfiguration {
                self.loadDefaultConfiguration(defaultConfigUrl)
            }
            
            if AppDelegate.openURL != true {
                NSNotificationCenter.defaultCenter().postNotificationName("RefreshConfigurationListIfPresent", object: nil)
            }
            AppDelegate.openURL = false
            
            let authenticationMode = prefs.stringForKey("login-authenticationType")
            if authenticationMode == nil || (authenticationMode == "native") {
                if let currentUser = CurrentUser.sharedInstance() {
                    if currentUser.isLoggedIn && !currentUser.remember {
                        if let timestampLastActivity = timestampLastActivity {
                            let compareDate = timestampLastActivity.dateByAddingTimeInterval(ApplicationConstants.applicationTimeoutInMinutes * 60)
                            let currentDate = NSDate()
                            if (compareDate.compare(currentDate) == .OrderedAscending) || (logoutOnStartup != false) {
                                self.sendEventWithCategory(kAnalyticsCategoryAuthentication, withAction: kAnalyticsActionTimeout, withLabel: "Password Timeout")
                                currentUser.logout(false)
                            }
                        }
                    }
                }
            }
        }
    }
    
    func applicationWillTerminate(application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
        // Saves changes in the application's managed object context before the application terminates.
        if let currentUser = CurrentUser.sharedInstance() {
            if currentUser.isLoggedIn && !currentUser.remember {
                currentUser.logout(false)
            }
        }
        self.saveContext()
    }
    
    func application(application: UIApplication, openURL url: NSURL, sourceApplication: String?, annotation: AnyObject) -> Bool {
        AppDelegate.openURL = true
        
        let urlComponents = NSURLComponents(string: url.absoluteString)
        let type = urlComponents?.host
        let queryItems = urlComponents?.queryItems
        
        let pathComponents = url.pathComponents
        if (type == "mobilecloud") {
            if let pathComponents = pathComponents where pathComponents.count >= 2 {
                
                let scheme = pathComponents[1]
                let host = pathComponents[2]
                
                let newPathComponents = pathComponents[3 ..< pathComponents.count].flatMap { $0 }
                let newPath = newPathComponents.joinWithSeparator("/")
                
                let newUrl = NSURL(scheme: scheme, host: host, path: "/\(newPath)")
                let defaults = AppGroupUtilities.userDefaults()
                defaults?.setObject(newUrl?.absoluteString, forKey: "mobilecloud-url")
                let storyboard: UIStoryboard = UIStoryboard(name: "ConfigurationSelectionStoryboard", bundle: nil)
                let navcontroller = storyboard.instantiateViewControllerWithIdentifier("ConfigurationSelector") as! UINavigationController
                let vc = navcontroller.childViewControllers[0] as! ConfigurationSelectionViewController
                vc.modalPresentationStyle = .FullScreen
                self.window?.rootViewController = navcontroller
            }
        }
        else if (type == "configuration") {
            if let pathComponents = pathComponents where pathComponents.count >= 2 {
                let scheme = pathComponents[1]
                let host = pathComponents[2]
                
                let newPathComponents = pathComponents[3 ..< pathComponents.count].flatMap { $0 }
                let newPath = newPathComponents.joinWithSeparator("/")
                
                CurrentUser.sharedInstance().logoutWithoutUpdatingUI()
                self.window?.rootViewController?.dismissViewControllerAnimated(false, completion: nil)
                let passcode = queryItems?.filter({$0.name == "passcode"}).first?.value
                dispatch_async(dispatch_get_main_queue(), {() -> Void in
                    let storyboard: UIStoryboard = UIStoryboard(name: "HomeStoryboard", bundle: nil)
                    let vc: UIViewController = storyboard.instantiateViewControllerWithIdentifier("Loading")
                    self.window?.rootViewController = vc
                    dispatch_async(dispatch_get_global_queue(0, 0), {() -> Void in
                        self.loadConfigurationInBackground(scheme, host: host, newPath: newPath, passcode: passcode)
                    })
                })
            }
            
        }
        else if (type == "module-type") {
            if let pathComponents = pathComponents where pathComponents.count >= 1 {
                if (pathComponents[1] == "ilp") {
                    self.sendEventWithCategory(kAnalyticsCategoryWidget, withAction: kAnalyticsActionList_Select, withLabel: "Assignments")
                    self.window?.rootViewController?.dismissViewControllerAnimated(false, completion: nil)
                    let operation: OpenModuleOperation = OpenModuleOperation(type: "ilp")
                    if let urlToAssignment = queryItems?.filter({$0.name == "url"}).first
                    {
                        //sign in will not pass a url to open
                        operation.properties = ["requestedAssignmentId": urlToAssignment]
                    }
                    NSOperationQueue.mainQueue().addOperation(operation)
                }
            }
        }
        
        return true
    }
    
    // MARK: - 3d touch
    @available(iOS 9.0, *)
    func application(application: UIApplication, performActionForShortcutItem shortcutItem: UIApplicationShortcutItem, completionHandler: (Bool) -> Void) {
        completionHandler(handleShortcut(shortcutItem))
    }
    
    @available(iOS 9.0, *)
    private func handleShortcut(shortcutItem: UIApplicationShortcutItem) -> Bool {
        let shortcutType = shortcutItem.type
        
        let operation: OpenModuleOperation = OpenModuleOperation(id: shortcutType)
        
        NSOperationQueue.mainQueue().addOperation(operation)
        return true
    }
    
    // MARK: - Core Data stack
    
    lazy var applicationDocumentsDirectory: NSURL = {
        // The directory the application uses to store the Core Data store file.
        return AppGroupUtilities.applicationDocumentsDirectory()!
    }()
    
    lazy var managedObjectContext: NSManagedObjectContext = {
        // Returns the managed object context for the application (which is already bound to the persistent store coordinator for the application.) This property is optional since there are legitimate error conditions that could cause the creation of the context to fail.
        return CoreDataManager.shared.managedObjectContext;
    }()
    
    // MARK: - Core Data Saving support
    
    func saveContext () {
        CoreDataManager.shared.save()
    }
    
    // MARK: Noficiations
    func application(application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: NSData) {
        print("Application deviceToken: \(deviceToken)")
        NotificationManager.registerDeviceToken(deviceToken)
    }
    
    func application(application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: NSError) {
        print("Failed to get token, error: \(error)")
    }
    
    func application(application: UIApplication, didReceiveRemoteNotification userInfo: [NSObject : AnyObject]) {
        
        let uuid = userInfo["uuid"] as? String
        print("didReceiveRemoteNotification - for uuid: \(uuid)")
        if application.applicationState == .Active {
            NSLog("application active - show notification message alert")
            // log activity to Google Analytics
            self.sendEventWithCategory(kAnalyticsCategoryPushNotification, withAction: kAnalyticsActionReceivedMessage, withLabel: "whileActive")
            
            if let aps = userInfo["aps"] as? NSDictionary {
                //                if let alert = aps["alert"] as? NSDictionary {
                //                    if let message = alert["message"] as? NSString {
                //                        //Do stuff
                //                    }
                //                } else
                if let alertMessage = aps["alert"] as? String {
                    let alert: UIAlertController = UIAlertController(title: NSLocalizedString("New Notification", comment: "new notification has arrived"), message: alertMessage, preferredStyle: .Alert)
                    let view: UIAlertAction = UIAlertAction(title: NSLocalizedString("View", comment: "view label"), style: .Default, handler: {(action: UIAlertAction) -> Void in
                        let operation: OpenModuleOperation = OpenModuleOperation(type: "notifications")
                        if let uuid = uuid {
                            operation.properties = ["uuid": uuid]
                        }
                        NSOperationQueue.mainQueue().addOperation(operation)
                    })
                    let cancel: UIAlertAction = UIAlertAction(title: NSLocalizedString("Close", comment: "Close"), style: .Cancel, handler: {(action: UIAlertAction) -> Void in
                        alert.dismissViewControllerAnimated(true, completion: { _ in })
                    })
                    alert.addAction(cancel)
                    alert.addAction(view)
                    self.window?.makeKeyAndVisible()
                    self.window?.rootViewController?.presentViewController(alert, animated: true, completion: nil)
                }
            }
        } else {
            // navigate to notifications
            NSLog("application not active - open from notifications")
            // log activity to Google Analytics
            self.sendEventWithCategory(kAnalyticsCategoryPushNotification, withAction: kAnalyticsActionReceivedMessage, withLabel: "whileInActive")
            self.window?.rootViewController?.dismissViewControllerAnimated(false, completion: nil)
            let operation: OpenModuleOperation = OpenModuleOperation(type: "notifications")
            if let uuid = uuid {
                operation.properties = ["uuid": uuid]
            }
            NSOperationQueue.mainQueue().addOperation(operation)
        }
    }
    
    // MARK: - Ellucian Mobile
    var slidingViewController : ECSlidingViewController?
    var timestampLastActivity : NSDate?
    var logoutOnStartup = true
    static var openURL = false
    
    var useDefaultConfiguration: Bool {
        
        if let plistPath = NSBundle.mainBundle().pathForResource("Customizations", ofType: "plist"), let customizationsDictionary = NSDictionary(contentsOfFile: plistPath) as? Dictionary<String, AnyObject> {
            
            let useDefaultConfiguration = customizationsDictionary["Use Default Configuration"] as! Bool
            return useDefaultConfiguration
        } else {
            return false
        }
    }
    
    var defaultConfigUrl : String? {
        
        if let plistPath = NSBundle.mainBundle().pathForResource("Customizations", ofType: "plist"), let customizationsDictionary = NSDictionary(contentsOfFile: plistPath) as? Dictionary<String, AnyObject> {
            
            if let url = customizationsDictionary["Default Configuration URL"] {
                return url as? String
            }
            
        }
        return nil
        
    }
    
    func reset() {
        print("reset")
        let sharedCache: NSURLCache = NSURLCache(memoryCapacity: 0, diskCapacity: 0, diskPath: nil)
        NSURLCache.setSharedURLCache(sharedCache)
        let appDomain = NSBundle.mainBundle().bundleIdentifier
        //must persist this property when switching configurations
        if let appGroupDefaults = AppGroupUtilities.userDefaults() {
            let cloudUrl = appGroupDefaults.stringForKey("mobilecloud-url")
            let appGroupDefaultsDictionary = appGroupDefaults.dictionaryRepresentation()
            
            NSUserDefaults.standardUserDefaults().removePersistentDomainForName(appDomain!)
            for key: String in appGroupDefaultsDictionary.keys {
                appGroupDefaults.removeObjectForKey(key)
            }
            
            if let cloudUrl = cloudUrl {
                appGroupDefaults.setObject(cloudUrl, forKey: "mobilecloud-url")
            }
            
        }

        CurrentUser.sharedInstance().logoutWithoutUpdatingUI()
        
        CoreDataManager.shared.reset()
        
        
        ImageCache.sharedCache().reset()
        UIApplication.sharedApplication().cancelAllLocalNotifications()
        UIApplication.sharedApplication().applicationIconBadgeNumber = 0
        
        if #available(iOS 9, *) {
            UIApplication.sharedApplication().shortcutItems = nil
        }
    }
    
    func loadDefaultConfiguration(defaultConfigUrl: String) {
        if let hudView = self.window?.rootViewController?.view {
            let hud = MBProgressHUD.showHUDAddedTo(hudView, animated: true)
            hud.labelText = NSLocalizedString("Loading", comment: "loading message while waiting for data to load")
            
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW,Int64(0.01 * Double(NSEC_PER_SEC))), dispatch_get_main_queue(), {() -> Void in
                let manager = ConfigurationManager.instance
                manager.loadConfiguration(configurationUrl: defaultConfigUrl, completionHandler: {(result: AnyObject) -> Void in
                    dispatch_async(dispatch_get_main_queue(), {() -> Void in
                        MBProgressHUD.hideHUDForView(hudView, animated: true)
                        if result is Bool && (result as! Bool) {
                            AppearanceChanger.applyAppearanceChanges(self.window)
                            NSOperationQueue.mainQueue().addOperation(OpenModuleHomeOperation())
                        }
                    })
                })
            })
        }
        
    }
    
    func sendEventWithCategory(category: String, withAction action: String, withLabel label: String) {
        if let defaults = AppGroupUtilities.userDefaults() {
            let trackingId1 = defaults.stringForKey( "gaTracker1" )
            let trackingId2 = defaults.stringForKey( "gaTracker2" )
            let configurationName = defaults.stringForKey( "configurationName" )
            let builder: GAIDictionaryBuilder = GAIDictionaryBuilder.createEventWithCategory(category, action: action, label: label, value: nil)
            builder.set(configurationName, forKey: GAIFields.customMetricForIndex(1))
            let buildDictionary = builder.build() as [NSObject : AnyObject]
            if let trackingId1 = trackingId1 {
                let tracker1: GAITracker = GAI.sharedInstance().trackerWithTrackingId(trackingId1)
                tracker1.send(buildDictionary)
            }
            if let trackingId2 = trackingId2 {
                let tracker2: GAITracker = GAI.sharedInstance().trackerWithTrackingId(trackingId2)
                tracker2.send(buildDictionary)
            }
        }
    }
    
    func loadConfigurationInBackground(scheme: String, host: String, newPath: String, passcode: String?) {
        self.reset()
        var newUrl = NSURL(scheme: scheme, host: host, path: "/\(newPath)")!
        if let passcode = passcode {
            newUrl = NSURL(scheme: scheme, host: host, path: "/\(newPath)?passcode=\(passcode)")!
        }
        if let prefs = AppGroupUtilities.userDefaults() {
            prefs.setObject(newUrl.absoluteString, forKey: "configurationUrl")
        }
        let manager = ConfigurationManager.instance
        manager.loadConfiguration(configurationUrl: newUrl.absoluteString, completionHandler:  {(result: AnyObject) -> Void in
            
            ConfigurationManager.instance.loadMobileServerConfiguration() {
                (result2) in
                
                dispatch_async(dispatch_get_main_queue(), {() -> Void in
                    
                    var resultBool = false
                    if result is NSNumber {
                        let num = result as! NSNumber
                        resultBool = num.boolValue
                    }
                    
                    if resultBool || self.useDefaultConfiguration {
                        AppearanceChanger.applyAppearanceChanges(self.window)
                        //the case may be that the user was on the modal "configuration selection" screen.  dismiss in case that's the case.
                        self.window?.rootViewController?.dismissViewControllerAnimated(false, completion: nil)
                        let storyboard: UIStoryboard = UIStoryboard(name: "HomeStoryboard", bundle: nil)
                        let slidingVC = storyboard.instantiateViewControllerWithIdentifier("SlidingViewController") as! ECSlidingViewController
                        slidingVC.anchorRightRevealAmount = 276
                        slidingVC.anchorLeftRevealAmount = 276
                        slidingVC.topViewAnchoredGesture = [ECSlidingViewControllerAnchoredGesture.Tapping , ECSlidingViewControllerAnchoredGesture.Panning]
                        let menu: UIViewController = storyboard.instantiateViewControllerWithIdentifier("Menu")
                        if #available(iOS 9.0, *) {
                            let direction: UIUserInterfaceLayoutDirection = UIView.userInterfaceLayoutDirectionForSemanticContentAttribute(slidingVC.view.semanticContentAttribute)
                            if direction == .RightToLeft {
                                slidingVC.underRightViewController = menu
                            }
                            else {
                                slidingVC.underLeftViewController = menu
                            }
                        }
                        else {
                            slidingVC.underLeftViewController = menu
                        }
                        self.window?.rootViewController = slidingVC
                        self.slidingViewController = slidingVC
                        NSOperationQueue.mainQueue().addOperation(OpenModuleHomeOperation())
                    }
                    else {
                        if let prefs = AppGroupUtilities.userDefaults() {
                            prefs.removeObjectForKey("configurationUrl")
                            prefs.setObject(newUrl.absoluteString, forKey: "configurationUrl")
                        }
                        
                        let storyboard: UIStoryboard = UIStoryboard(name: "ConfigurationSelectionStoryboard", bundle: nil)
                        let navcontroller = storyboard.instantiateViewControllerWithIdentifier("ConfigurationSelector") as! UINavigationController
                        let vc = navcontroller.childViewControllers[0] as! ConfigurationSelectionViewController
                        vc.modalPresentationStyle = .FullScreen
                        dispatch_async(dispatch_get_main_queue(), {() -> Void in
                            self.window?.rootViewController = navcontroller
                            ConfigurationFetcher.showErrorAlertView()
                        })
                    }
                    
                })
            }
        })
    }
    
    // MARK: responds to notification center
    func applicationDidTimeout(notif: NSNotification) {
        NSLog("time exceeded!!")
        if let currentUser = CurrentUser.sharedInstance() {
            if currentUser.isLoggedIn && !currentUser.remember {
                if let authenticationMode = AppGroupUtilities.userDefaults()!.stringForKey("login-authenticationType") {
                    if authenticationMode == "native" {
                        currentUser.logout(false)
                    }
                } else {
                    currentUser.logout(false)
                    
                }
            }
        }
    }
    
    func applicationDidTouch(notif: NSNotification) {
        timestampLastActivity = NSDate()
    }
    
    func returnHome(sender: AnyObject) {
        AppearanceChanger.applyAppearanceChanges(self.window)
        NSOperationQueue.mainQueue().addOperation(OpenModuleHomeOperation())
    }
    
    func forceConfigurationSelection(sender: AnyObject) {
        dispatch_async(dispatch_get_main_queue(), {() -> Void in
            ConfigurationFetcher.showErrorAlertView()
            AppearanceChanger.applyAppearanceChanges(self.window)
            NSOperationQueue.mainQueue().addOperation(OpenModuleConfigurationSelectionOperation())
        })
    }
}

