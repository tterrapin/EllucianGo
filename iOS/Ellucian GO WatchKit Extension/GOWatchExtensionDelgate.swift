//
//  GOWatchExtensionDelgate.swift
//  Mobile
//
//  Created by Bret Hansen on 9/16/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation
import WatchKit

class GOWatchExtensionDelegate: NSObject, WKExtensionDelegate {
    // need a strong reference in Watch to the WatchConnectivityManager
    
    var watchConnectivityManager: WatchConnectivityManager? = nil

    func applicationDidBecomeActive() {
        NSLog("applicationDidBecomeActive")
        watchConnectivityManager = WatchConnectivityManager.instance.ensureWatchConnectivityInitialized().refreshUser()
    }
}