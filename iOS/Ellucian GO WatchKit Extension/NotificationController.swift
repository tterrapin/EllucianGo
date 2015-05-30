//
//  NotificationController.swift
//  Ellucian GO WatchKit Extension
//
//  Created by Jason Hocker on 4/24/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import WatchKit
import Foundation


class NotificationController: WKUserNotificationInterfaceController {

    @IBOutlet var configurationLabel: WKInterfaceLabel!
    @IBOutlet var notificationAlertLabel: WKInterfaceLabel!
    
    override init() {
        // Initialize variables here.
        super.init()
        
    }

    override func willActivate() {
        // This method is called when watch view controller is about to be visible to user
        super.willActivate()
    }

    override func didDeactivate() {
        // This method is called when watch view controller is no longer visible
        super.didDeactivate()
    }


    override func didReceiveLocalNotification(localNotification: UILocalNotification, withCompletion completionHandler: ((WKUserNotificationInterfaceType) -> Void)) {
        // This method is called when a local notification needs to be presented.
        // Implement it if you use a dynamic notification interface.
        // Populate your dynamic notification interface as quickly as possible.
        //
        // After populating your dynamic notification interface call the completion block.
        // Configure interface objects here.
        if let configurationName = AppGroupUtilities.userDefaults()?.stringForKey("configurationName") {
            self.configurationLabel.setText(configurationName)
        } else {
            self.configurationLabel.setHidden(true)
        }
        self.notificationAlertLabel!.setText(localNotification.alertBody);        
        completionHandler(.Custom)
    }
    

    override func didReceiveRemoteNotification(remoteNotification: [NSObject : AnyObject], withCompletion completionHandler: ((WKUserNotificationInterfaceType) -> Void)) {
        // This method is called when a remote notification needs to be presented.
        // Implement it if you use a dynamic notification interface.
        // Populate your dynamic notification interface as quickly as possible.
        //
        // After populating your dynamic notification interface call the completion block.

        // Configure interface objects here.
        if let configurationName = AppGroupUtilities.userDefaults()?.stringForKey("configurationName") {
            self.configurationLabel.setText(configurationName)
        }
        self.notificationAlertLabel!.setText(remoteNotification.description);
        if let remoteaps:NSDictionary = remoteNotification["aps"] as? NSDictionary {
            if let remoteAlert:NSDictionary = remoteaps["alert"] as? NSDictionary {
                if let remotebody = remoteAlert["body"] as? String {
                    self.notificationAlertLabel!.setText(remotebody);
                }
            } else if let remotebody:String = remoteaps["alert"] as? String {
                self.notificationAlertLabel!.setText(remotebody);
            }
        }

        completionHandler(.Custom)
    }

}
