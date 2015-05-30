//
//  AppGroupUtilities
//  Mobile
//
//  Created by Jason Hocker on 1/26/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

@objc
class AppGroupUtilities {

    @objc class func userDefaults() -> NSUserDefaults? {
        if NSBundle.mainBundle().bundleIdentifier!.hasPrefix("com.ellucian.elluciangoenterprise") {
            return NSUserDefaults()
        }
        return NSUserDefaults(suiteName: lookupAppGroup()!)
    }
    
    class func lookupAppGroup() -> String? {
        var plistDictionary: NSDictionary?
        if let path = NSBundle.mainBundle().pathForResource("Customizations", ofType: "plist") {
            plistDictionary = NSDictionary(contentsOfFile: path)
        }
        if plistDictionary != nil && plistDictionary!["App Group"] != nil {
            let appGroup = plistDictionary?["App Group"] as! String
            if count(appGroup) > 0 {
                return appGroup
            }
        }
        if NSBundle.mainBundle().bundleIdentifier!.hasPrefix("com.ellucian.elluciangoenterprise") {
            return "group.com.ellucian.elluciangoenterprise"
        }
        if NSBundle.mainBundle().bundleIdentifier!.hasPrefix("com.ellucian.elluciango") {
            return "group.com.ellucian.elluciango"
        }

        return nil;
    }
    
    @objc class func applicationDocumentsDirectory() -> NSURL? {
        return NSFileManager.defaultManager().containerURLForSecurityApplicationGroupIdentifier(lookupAppGroup()!)
    }
}