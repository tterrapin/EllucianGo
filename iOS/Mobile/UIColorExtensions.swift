//
//  UIColorExtensions.swift
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
            return UIColor(rgba: color)
        } else {
            return UIColor(rgba: kSchoolCustomizationPrimaryColor)
        }
    }
    
    class func headerTextColor() -> UIColor {
        let defaults = AppGroupUtilities.userDefaults()
        let color = defaults?.objectForKey("headerTextColor") as? String
        if let color = color {
            return UIColor(rgba: color)
        } else {
            return UIColor(rgba: kSchoolCustomizationHeaderColor)
        }
    }
    
    class func accentColor() -> UIColor {
        let defaults = AppGroupUtilities.userDefaults()
        let color = defaults?.objectForKey("accentColor") as? String
        if let color = color {
            return UIColor(rgba: color)
        } else {
            return UIColor(rgba: kSchoolCustomizationAccentColor)
        }
    }
    
    class func subheaderTextColor() -> UIColor {
        let defaults = AppGroupUtilities.userDefaults()
        let color = defaults?.objectForKey("subheaderTextColor") as? String
        if let color = color {
            return UIColor(rgba: color)
        } else {
            return UIColor(rgba: kSchoolCustomizationSubheaderColor)
        }
    }
    
    class func defaultPrimaryColor() -> UIColor {
        return UIColor(rgba: kSchoolCustomizationPrimaryColor)
    }
    
    class func defaultHeaderColor() -> UIColor {
        return UIColor(rgba: kSchoolCustomizationHeaderColor)
    }
    
    convenience init(rgba: String) {
        var rgba = rgba
        var red:   CGFloat = 0.0
        var green: CGFloat = 0.0
        var blue:  CGFloat = 0.0
        var alpha: CGFloat = 1.0
        
        if !rgba.hasPrefix("#") {
            rgba = "#\(rgba)"
        }
        if rgba.hasPrefix("#") {
            let index   = rgba.startIndex.advancedBy(1)
            let hex     = rgba.substringFromIndex(index)
            let scanner = NSScanner(string: hex)
            var hexValue: CUnsignedLongLong = 0
            if scanner.scanHexLongLong(&hexValue) {
                switch (hex.characters.count) {
                case 3:
                    red   = CGFloat((hexValue & 0xF00) >> 8)       / 15.0
                    green = CGFloat((hexValue & 0x0F0) >> 4)       / 15.0
                    blue  = CGFloat(hexValue & 0x00F)              / 15.0
                case 4:
                    red   = CGFloat((hexValue & 0xF000) >> 12)     / 15.0
                    green = CGFloat((hexValue & 0x0F00) >> 8)      / 15.0
                    blue  = CGFloat((hexValue & 0x00F0) >> 4)      / 15.0
                    alpha = CGFloat(hexValue & 0x000F)             / 15.0
                case 6:
                    red   = CGFloat((hexValue & 0xFF0000) >> 16)   / 255.0
                    green = CGFloat((hexValue & 0x00FF00) >> 8)    / 255.0
                    blue  = CGFloat(hexValue & 0x0000FF)           / 255.0
                case 8:
                    red   = CGFloat((hexValue & 0xFF000000) >> 24) / 255.0
                    green = CGFloat((hexValue & 0x00FF0000) >> 16) / 255.0
                    blue  = CGFloat((hexValue & 0x0000FF00) >> 8)  / 255.0
                    alpha = CGFloat(hexValue & 0x000000FF)         / 255.0
                default:
                    print("Invalid RGB string, number of characters after '#' should be either 3, 4, 6 or 8")
                }
            } else {
                print("Scan hex error")
            }
        } else {
            print("Invalid RGB string, missing '#' as prefix")
        }
        self.init(red:red, green:green, blue:blue, alpha:alpha)
    }
    
    
}