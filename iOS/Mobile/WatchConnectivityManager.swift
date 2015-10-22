//
//  WatchStateManager.swift
//  Mobile
//
//  Created by Bret Hansen on 9/3/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation
import WatchConnectivity
import WatchKit

class WatchConnectivityManager: NSObject {
    private var wcSession: AnyObject?
    private(set) var lastConfigData: NSData?
    private var user: [String: AnyObject]?
    var refreshUserAfterReachable = false
    private var refreshingUser = false
    
    private var wcSessionDelegate: WCSessionDelegate? = nil
    private var wkRootController: AnyObject? = nil
    
    private var phoneAppName: String?
    
    private var actionQueue = [ActionMessage]()
    private var pinging = false
    private var pingid = 0
    private var retryDelaySeconds: Double = 1
    private var actionTimeoutSeconds: Double = 7
    
    // Returns the singleton instance of WatchManager
    class var instance: WatchConnectivityManager {
        get {
            struct Singleton {
                static var instance : WatchConnectivityManager? = nil
                static var token : dispatch_once_t = 0
            }
            dispatch_once(&Singleton.token) { Singleton.instance = WatchConnectivityManager() }
            
            return Singleton.instance!
        }
    }
    
    // make init private for singleton
    override private init() {
    }
    
    func getPhoneAppName() -> String {
        if phoneAppName == nil {
            let plistPath = NSBundle.mainBundle().pathForResource("Customizations", ofType: "plist")
            let plistDictioanry = NSDictionary(contentsOfFile: plistPath!)!
            
            if let name = plistDictioanry["iOS Application Name"] as! String? {
                self.phoneAppName = name
            } else {
                self.phoneAppName = "Ellucian GO"
            }
        }
        
        return self.phoneAppName!
    }
    
    func saveRootController(controller: AnyObject) {
        self.wkRootController = controller
        NSLog("WatchConnectivityManager saved root controller")
    }

    func ensureWatchConnectivityInitialized() -> WatchConnectivityManager {
        if #available(iOS 9.0, *) {
            if wcSession as? WCSession == nil {
                if WCSession.isSupported() {
                    let session = WCSession.defaultSession()
                    wcSession = session
                    wcSessionDelegate = WatchConnectivityManagerWCSessionDelegate(watchConnectivityManager: self)
                    session.delegate = wcSessionDelegate
                    session.activateSession()
                    NSLog("WatchConnectivityManager - WSSession.activateSession()")
                }
            }
        }
        
        return WatchConnectivityManager.instance
    }
    
    @available(iOS 9.0, *)
    func session() -> WCSession? {
        return wcSession as! WCSession?
    }
    
    func currentUser() -> [String: AnyObject]? {
        var user: [String: AnyObject]? = self.user
        
        if user == nil {
            if let defaults = AppGroupUtilities.userDefaults() {
                if let defaultuser = defaults.objectForKey("current user") as! [String: AnyObject]? {
                    user = defaultuser
                }
            }
        }
        
        return user
    }
    
    func isUserLoggedIn() -> Bool {
        let currentUser = self.currentUser()
        let isLoggedIn = (currentUser != nil) && (currentUser!["userid"] != nil)
        
        return isLoggedIn
    }
    
    func userLoggedIn(user: NSDictionary, notifyOtherSide: Bool = true) {
        // save the user
        self.user = user as? [String: AnyObject]
        if let defaults = AppGroupUtilities.userDefaults() {
            defaults.setObject(self.user, forKey: "current user")
        }
        
        if notifyOtherSide {
            // need to let the other device know
            self.notifyOtherSide("userLoggedIn", data: user as AnyObject)
        }
        
        // clear user data caches
        #if os(watchOS)
            DefaultsCache.clearLogoutCaches()
        #endif

    }
    
    func userLoggedOut(notifyOtherSide: Bool = true) {
        user = nil
        if let defaults = AppGroupUtilities.userDefaults() {
            defaults.removeObjectForKey("current user")
        }
        if notifyOtherSide {
            self.notifyOtherSide("userLoggedOut")
        }
        
        // clear user data caches
        #if os(watchOS)
            DefaultsCache.clearLogoutCaches()
        #endif
    }
    
    func refreshUser() -> WatchConnectivityManager {
        ensureWatchConnectivityInitialized()
        
        if #available(iOS 9.0, *) {
            if let wcSession = wcSession as! WCSession? {
                if !refreshingUser {
                    refreshingUser = true
                    if wcSession.reachable {
                        // ask for a refresh of the user data
                        NSLog("refreshUser sending action \"fetch user\" to phone")
                        sendActionMessage("fetch user", replyHandler: {
                            (data) -> Void in
                            
                            self.refreshingUser = false
                            let newUser = data["user"] as! [String: AnyObject]?
                            if (newUser != nil) {
                                let currentUser = self.currentUser()
                                let currentUserId = currentUser != nil ? currentUser!["userid"] as! String? : nil
                                var newUserId = newUser!["userid"] as! String?
                                
                                if newUserId != nil && newUserId == "" {
                                    // when no user is logged in the userid is ""
                                    newUserId = nil
                                }
                                
                                let logout = currentUserId != nil && (newUserId == nil || currentUserId! != newUserId!)
                                let login = newUserId != nil && (currentUserId == nil || currentUserId! != newUserId!)
                                
                                if logout {
                                    self.userLoggedOut()
                                    NSLog("refreshUser logged out \(currentUserId!)")
                                }
                                
                                if login {
                                    self.userLoggedIn(newUser!)
                                    NSLog("refreshUser logged in \(newUserId!)")
                                }
                            } else {
                                if self.isUserLoggedIn() {
                                    self.userLoggedOut(false)
                                }
                                NSLog("refreshUser user is logged out")
                            }
                            }, errorHandler: {
                                (error) -> Void in
                                
                                self.refreshingUser = false
                                NSLog("refreshUser failed: \(error)")
                        })
                    } else {
                        refreshUserAfterReachable = true
                        NSLog("refreshUser WC not yet reachable")
                    }
                }
            }
        }
        
        return WatchConnectivityManager.instance
    }
    
    func notifyOtherSide(action: String, data: AnyObject? = nil) {
        if #available(iOS 9.0, *) {
            if let wcSession = wcSession as! WCSession? {
                var userInfo: [String: AnyObject] = [
                    "action": action
                ]
                
                if data != nil {
                    userInfo["data"] = data
                }
                
                wcSession.transferUserInfo(userInfo)
            }
        }
    }

    private class ActionMessage {
        let action: String
        let data: [String: AnyObject]?
        let replyHandler: (([String: AnyObject]) -> Void)?
        let errorHandler: ((NSError) -> Void)?
        var retryCount: Int = 0
        
        init(action: String, data: [String: AnyObject]? = nil, replyHandler: (([String: AnyObject]) -> Void)?, errorHandler: ((NSError) -> Void)?) {
            self.action = action
            self.data = data
            self.replyHandler = replyHandler
            self.errorHandler = errorHandler
        }
    }
    
    func sendActionMessage(action: String, data: [String: AnyObject]? = nil, replyHandler: (([String: AnyObject]) -> Void)?, errorHandler: ((NSError) -> Void)?) {
        actionQueue.append(ActionMessage(action: action, data: data, replyHandler: replyHandler, errorHandler: errorHandler))

        pingPhone(successHandler: {
                () -> Void in
                self.sendNextActionMessage()
            }, errorHandler: {
                self.sendCommunicationErrorToAllActionMessages()
        })
    }
    
    func sendCommunicationErrorToAllActionMessages() {
        for actionMessage in actionQueue {
            if let errorHandler = actionMessage.errorHandler {
                errorHandler(NSError(domain: "WatchConnectivityManager", code: 1, userInfo: nil))
            }
        }
    }
    
    private func pingPhone(retryCount: Int = 0, successHandler: () -> Void, errorHandler: () -> Void) {
        ensureWatchConnectivityInitialized()
        
        if #available(iOS 9.0, *) {
            if !pinging || retryCount > 0 {
                pinging = true
                pingid++
                if let session = wcSession as! WCSession? {
                    let message: [String: AnyObject] = [ "action": "ping"]
                    
                    NSLog("pingPhone sending ping")
                    NSLog("pingPhone session reachable: \(session.reachable)")
                    session.sendMessage(message, replyHandler: {
                        (data) -> Void in
                        
                        if self.pinging {
                            self.pinging = false
                            NSLog("pingPhone got a response")
                            successHandler()
                        } else {
                            NSLog("pingPhone got a response after timeout")
                        }
                    }, errorHandler: {
                        (error) -> Void in
                        
                        if self.pinging {
                            self.pinging = false

                            var errorHandled = false
                            NSLog("pingPhone error code: \(error.code) domain: \(error.domain)")
                            
                            var showCommunicationErrorFlag = false
                            if error.code == 7014 {
                                if retryCount < 3 {
                                    // failed to send, retry after a short delay
                                    NSLog("pingPhone retrying ping retry count: \(retryCount+1))")
                                    errorHandled = true
                                    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, Int64(self.retryDelaySeconds * Double(NSEC_PER_SEC))), dispatch_get_main_queue()) {
                                        self.pingPhone(retryCount+1, successHandler: successHandler, errorHandler: errorHandler)
                                    }
                                } else {
                                    showCommunicationErrorFlag = true
                                }
                            } else {
                                showCommunicationErrorFlag = true
                            }
                            
                            #if os(watchOS)
                                if showCommunicationErrorFlag {
                                    self.showCommunicationError()
                                }
                            #endif
                            
                            if !errorHandled {
                                errorHandler()
                            }
                        } else {
                            NSLog("pingPhone got the error after timeout")
                        }

                    })
                    
                    // start a block after x seconds - so we fail sooner if response is slow
                    let pingidForTimeout = self.pingid
                    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, Int64(actionTimeoutSeconds * Double(NSEC_PER_SEC))), dispatch_get_main_queue()) {
                        if pingidForTimeout == self.pingid && self.pinging {
                            self.pinging = false
                            NSLog("pingPhone ping timed out")
                            self.showCommunicationError()
                            errorHandler()
                        }
                    }
                }
            }
        }
    }
    
    private func showCommunicationError() {
        #if os(watchOS)
            if #available(iOS 8.2, *) {
                if let wkRootController = self.wkRootController as! WKInterfaceController? {
                    let action = WKAlertAction(title: NSLocalizedString("OK", comment: "OK button on alert view"), style: WKAlertActionStyle.Default, handler: {})
                    let message = String.localizedStringWithFormat(NSLocalizedString("Please ensure the phone is nearby and launch %@ on the phone",
                        comment: "Watch communication to phone error message"), self.getPhoneAppName())
                    wkRootController.presentAlertControllerWithTitle(NSLocalizedString("Communication Error", comment: "Watch communication to phone error title"), message: message, preferredStyle: WKAlertControllerStyle.Alert, actions: [action])
                }
            }
       #endif
    }
    
    private func sendNextActionMessage() {
        if actionQueue.count > 0 {
            let actionMessage = actionQueue.removeFirst()
            sendActionMessage(actionMessage.action, data: actionMessage.data, retryCount: actionMessage.retryCount, replyHandler: {
                    (response) -> Void in
                
                    if let actionReplyHandler = actionMessage.replyHandler {
                        actionReplyHandler(response)
                    }
                    self.sendNextActionMessage()
                }, errorHandler: {
                    (error) -> Void in
                    
                    if let actionErrorHandler = actionMessage.errorHandler {
                        actionErrorHandler(error)
                    }
                    self.sendNextActionMessage()
                })
        }
    }
    
    private func sendActionMessage(action: String, data: [String: AnyObject]? = nil, retryCount: Int, replyHandler: (([String: AnyObject]) -> Void)?, errorHandler: ((NSError) -> Void)?) {
        ensureWatchConnectivityInitialized()
        
        if #available(iOS 9.0, *) {
            if let session = wcSession as! WCSession? {
                var message: [String: AnyObject] = [ "action": action]
                if let data = data {
                    message.merge(data)
                }
            
                NSLog("sendActionMessage sending action: \(action)")
                NSLog("sendActionMessage session reachable: \(session.reachable)")
                session.sendMessage(message, replyHandler: {
                    (data) -> Void in

                    NSLog("sendActionMessage action: \(action) received data")

                    if let replyHandler = replyHandler {
                        replyHandler(data)
                    }
                }, errorHandler: {
                    (error) -> Void in

                    let code = error.code
                    let domain = error.domain
                    var errorHandled = false
                    NSLog("sendActionMessage error code: \(code) domain: \(domain)")
                    
                    var showCommunicationError = false
                    if error.code == 7014 {
                        if retryCount < 3 {
                            // failed to send, retry after a short delay
                            NSLog("failed to send -> retry: \(retryCount+1))")
                            errorHandled = true
                            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, Int64(1 * Double(NSEC_PER_SEC))), dispatch_get_main_queue()) {
                                self.sendActionMessage(action, data: data, retryCount: retryCount+1, replyHandler: replyHandler, errorHandler: errorHandler)
                            }
                        } else {
                            showCommunicationError = true
                        }
                    } else {
                        showCommunicationError = true
                    }
                    
                    #if os(watchOS)
                        if showCommunicationError {
                            if let wkRootController = self.wkRootController as! WKInterfaceController? {
                                let action = WKAlertAction(title: NSLocalizedString("OK", comment: "OK button on alert view"), style: WKAlertActionStyle.Default, handler: {})
                                let message = NSLocalizedString("Please ensure the phone is nearby and launch",
                                                                comment: "Watch communication to phone error message") + self.getPhoneAppName()
                                wkRootController.presentAlertControllerWithTitle(NSLocalizedString("Communication Error", comment: "Watch communication to phone error title"), message: message, preferredStyle: WKAlertControllerStyle.Alert, actions: [action])
                            }
                        }
                    #endif
                    
                    if !errorHandled {
                        if let errorHandler = errorHandler {
                            errorHandler(error)
                        }
                    }
                })
            }
        } else {
            if let errorHandler = errorHandler {
                errorHandler(NSError(domain: "CanNotSendToVersionLessThan9", code: 1, userInfo: nil))
            }
        }
    }
    
    @available(iOS 9.0, *)
    class WatchConnectivityManagerWCSessionDelegate: NSObject, WCSessionDelegate {
        let watchConnectivityManager: WatchConnectivityManager
        
        init(watchConnectivityManager: WatchConnectivityManager) {
                self.watchConnectivityManager = watchConnectivityManager
        }
        
        func sessionReachabilityDidChange(session: WCSession) {
            NSLog("WCSession reachability did change: \(session.reachable)")
            if watchConnectivityManager.refreshUserAfterReachable && session.reachable {
                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, Int64(1 * Double(NSEC_PER_SEC))), dispatch_get_main_queue()) {
                    self.watchConnectivityManager.refreshUser()
                }
            }
            
            watchConnectivityManager.refreshUserAfterReachable = false
        }
        
        // responses for watch from requests to phone
        func session(session: WCSession, didReceiveUserInfo userInfo: [String : AnyObject]) {
            var popToRoot = false
            if let action = userInfo["action"] as! String? {
                switch(action) {
                case "configurationLoaded":
                    if let configurationData = userInfo["data"] as! NSData? {
                        ConfigurationManager.instance.processConfigurationData(configurationData, notifyOtherSide: false)
                        popToRoot = true
                        NSLog("WatchConnectivityManager received configuration loaded")
                    }
                    break
                case "userLoggedIn":
                    if let userData = userInfo["data"] as! NSDictionary? {
                        self.watchConnectivityManager.userLoggedIn(userData, notifyOtherSide: false)
                        popToRoot = true
                        NSLog("WatchConnectivityManager received user logged in")
                    }
                    break
                case "userLoggedOut":
                    self.watchConnectivityManager.userLoggedOut(false)
                    popToRoot = true
                    NSLog("WatchConnectivityManager received user logged out")
                    break
                default:
                    NSLog("Received unknown infoType: \(action)")
                }
            }
            
            if popToRoot && watchConnectivityManager.wkRootController != nil {
                dispatch_async(dispatch_get_main_queue()) {
                    if let wkRootController = self.watchConnectivityManager.wkRootController as! WKInterfaceController? {
                        wkRootController.popToRootController()
                        NSLog("WatchConnectivityManager pop to root controller")
                    }
                    
                }
            }
        }
    
        // requests from watch to phone
        func session(session: WCSession, didReceiveMessage message: [String: AnyObject], replyHandler: ([String: AnyObject]) -> Void) {
            var response = [String: AnyObject]()
            
            var processClosure = {
                let action = message["action"] as! String
                NSLog("WatchConnectivityManager didReceiveMessage action: \(action)")
                switch (action) {
                case "ping":
                    break
                case "fetch user":
                    #if os(iOS)
                        response["user"] = CurrentUser.sharedInstance().userAsPropertyListDictionary()
                    #endif
                    break
                case "fetch configuration":
                    #if os(iOS)
                        if let configurationData = ConfigurationManager.instance.mostRecentConfigurationData() {
                            response["configurationData"] = configurationData
                        } else {
                            NSLog("WatchConnectivityManager didReceiveMessage configuration data wasn't cached - refresh it now and send to watch")
                            
                            // just send the configuration url, let the watch fetch configuration
                            if let configurationUrl = ConfigurationManager.instance.getConfigurationUrl() {
                                response["configurationUrl"] = configurationUrl
                            }
                        }
                    #endif
                    break
                case "fetch maps":
                    #if os(iOS)
                        let internalKey = message["internalKey"] as! String
                        let url = message["url"] as! String
                        
                        let operation = MapsFetchOperation(internalKey: internalKey, url: url)
                        NSOperationQueue.mainQueue().addOperation(operation)
                        
                        operation.waitUntilFinished()
                        
                        response["campuses"] = operation.campuses
                    #endif
                    break;
                case "fetch assignments":
                    #if os(iOS)
                        let internalKey = message["internalKey"] as! String
                        let url = message["url"] as! String
                        
                        let operation = ILPAssignmentsFetchOperation(internalKey: internalKey, url: url)
                        NSOperationQueue.mainQueue().addOperation(operation)
                        
                        operation.waitUntilFinished()
                        
                        response["assignments"] = operation.assignments
                    #endif
                    break;
                default:
                    break
                }
                
                // send reply with any associated data
                replyHandler(response)
            }
            
            #if os(iOS)
                // allow these to run as a background task
                let taskIdentifier = UIApplication.sharedApplication().beginBackgroundTaskWithExpirationHandler(){ () -> Void in }
                
                dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0)) {
                    NSLog("Running Process in background")
                    processClosure()
                    UIApplication.sharedApplication().endBackgroundTask(taskIdentifier)
                }
                
                #else
                processClosure()
            #endif
        }
    }
}