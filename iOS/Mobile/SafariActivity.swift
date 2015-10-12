//
//  SafariActivity.swift
//  Mobile
//
//  Created by Jason Hocker on 8/5/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class SafariActivity : UIActivity {
    
    var targetURL : NSURL?
    
    override func activityType() -> String? {
        return "SafariActivity"
    }
    
    override func activityTitle() -> String? {
        return NSLocalizedString("Open in Safari", comment: "label to open link in Safari")
    }
    
    override func activityImage() -> UIImage? {
        return UIImage(named:"icon_website")
    }
    
    override class func activityCategory() -> UIActivityCategory {
        return .Action
    }
    
    override func canPerformWithActivityItems(activityItems: [AnyObject]) -> Bool {
        for item in activityItems {
            if let _ = item as? NSURL {
                return true
            }
            if let urlString = item as? String {
                if let url = NSURL(string: urlString) {
                    if UIApplication.sharedApplication().canOpenURL(url) {
                        return true
                    }
                }
            }
        }
        return false
    }
    
    override func prepareWithActivityItems(activityItems: [AnyObject]) {
        for item in activityItems {
            if let url = item as? NSURL {
                targetURL = url
            } else if let urlString = item as? String, url = NSURL(string:urlString) {
                targetURL = url
            }
        }
    }
    
    override func performActivity() {
        if let targetURL = targetURL {
            let completed = UIApplication.sharedApplication().openURL(targetURL)
            activityDidFinish(completed)
        } else {
            activityDidFinish(false)
        }
    }
}
