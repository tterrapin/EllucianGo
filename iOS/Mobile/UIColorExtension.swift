//
//  UIColorExtension.swift
//  Mobile
//
//  Created by Jason Hocker on 4/27/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

let kSchoolCustomizationPrimaryColor = "#331640"
let kSchoolCustomizationHeaderColor = "#FFFFFF"
let kSchoolCustomizationAccentColor = "#D9C696"
let kSchoolCustomizationSubheaderColor = "#736357"

extension UIColor {

    class func hasCustomizationColors() -> Bool {
        let defaults = AppGroupUtilities.userDefaults()
        let color = defaults?.objectForKey("primaryColor") as? String
        return color != nil
    }
        
    class func primaryColor() -> UIColor {
        let defaults = AppGroupUtilities.userDefaults()
        let color = defaults?.objectForKey("primaryColor") as? String
        if let color = color {
            return UIColor(hex: color)
        } else {
            return UIColor(hex: kSchoolCustomizationPrimaryColor)
        }
    }
    
    class func headerTextColor() -> UIColor {
        let defaults = AppGroupUtilities.userDefaults()
        let color = defaults?.objectForKey("headerTextColor") as? String
        if let color = color {
            return UIColor(hex: color)
        } else {
            return UIColor(hex: kSchoolCustomizationHeaderColor)
        }
    }
    
    class func accentColor() -> UIColor {
        let defaults = AppGroupUtilities.userDefaults()
        let color = defaults?.objectForKey("accentColor") as? String
        if let color = color {
            return UIColor(hex: color)
        } else {
            return UIColor(hex: kSchoolCustomizationAccentColor)
        }
    }
    
    class func subheaderTextColor() -> UIColor {
        let defaults = AppGroupUtilities.userDefaults()
        let color = defaults?.objectForKey("subheaderTextColor") as? String
        if let color = color {
            return UIColor(hex: color)
        } else {
            return UIColor(hex: kSchoolCustomizationSubheaderColor)
        }
    }
    
    class func defaultPrimaryColor() -> UIColor {
        return UIColor(hex: kSchoolCustomizationPrimaryColor)
    }
    
    class func defaultHeaderColor() -> UIColor {
        return UIColor(hex: kSchoolCustomizationHeaderColor)
    }
    
    convenience init(hex: String) {
        let characterSet = NSCharacterSet.whitespaceAndNewlineCharacterSet().mutableCopy() as! NSMutableCharacterSet
        characterSet.formUnionWithCharacterSet(NSCharacterSet(charactersInString: "#"))
        var cString = hex.stringByTrimmingCharactersInSet(characterSet).uppercaseString
        if (count(cString) != 6) {
            self.init(white: 1.0, alpha: 1.0)
        } else {
            var rgbValue: UInt32 = 0
            NSScanner(string: cString).scanHexInt(&rgbValue)
            
            self.init(red: CGFloat((rgbValue & 0xFF0000) >> 16) / 255.0,
                green: CGFloat((rgbValue & 0x00FF00) >> 8) / 255.0,
                blue: CGFloat(rgbValue & 0x0000FF) / 255.0,
                alpha: CGFloat(1.0))
        }
    }
}