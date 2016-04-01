//
//  MobileUIApplication.swift
//  Mobile
//
//  Created by Jason Hocker on 2/11/16.
//  Copyright © 2016 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

struct ApplicationConstants {
    static let applicationTimeoutInMinutes = 30.0
    static let applicationDidTimeoutNotification = "AppTimeOut"
    static let applicationDidTouchNotification = "AppTouch"
}

class MobileUIApplication : UIApplication {
    
    var timer : NSTimer?
    
    //here we are listening for any touch. If the screen receives touch, the timer is reset
    override func sendEvent(event: UIEvent) {
        super.sendEvent(event)
        if let _ = timer {
            self.resetIdleTimer()
        }
        if let allTouches = event.allTouches() {
            if allTouches.count > 0 {
                let phase = allTouches.first?.phase
                if phase == .Began {
                    self.resetIdleTimer()
                    NSNotificationCenter.defaultCenter().postNotificationName(ApplicationConstants.applicationDidTouchNotification, object: nil)
                }
            }
        }
    }
    //as labeled...reset the timer
    
    func resetIdleTimer() {
        if let timer = timer {
            timer.invalidate()
        }
        //convert the wait period into seconds rather than minutes
        let timeout = ApplicationConstants.applicationTimeoutInMinutes * 60
        timer = NSTimer.scheduledTimerWithTimeInterval(timeout, target: self, selector: "idleTimerExceeded", userInfo: nil, repeats: false)
    }
    //if the timer reaches the limit as defined in kApplicationTimeoutInMinutes, post this notification
    
    func idleTimerExceeded() {
        let authenticationMode = AppGroupUtilities.userDefaults()?.stringForKey("login-authenticationType")
        if authenticationMode == nil || authenticationMode == "native" {
            self.sendEventWithCategory(kAnalyticsCategoryAuthentication, withAction: kAnalyticsActionTimeout, withLabel: "Password Timeout")
            NSNotificationCenter.defaultCenter().postNotificationName(ApplicationConstants.applicationDidTimeoutNotification, object: nil)
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
}