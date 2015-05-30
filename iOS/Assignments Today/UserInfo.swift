//
//  UserInfo.swift
//  Mobile
//
//  Created by Jason Hocker on 1/26/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

public struct UserInfo {
    
    public static func userauth() -> String? {
        var defaults = AppGroupUtilities.userDefaults()
        if let defaults = defaults, stored = defaults.objectForKey("login-userauth") as! NSData? {
                let decryptedData = RNDecryptor.decryptData(stored, withPassword: "key", error: nil)
                let nsstring = NSString(data: decryptedData, encoding: NSUTF8StringEncoding)
                if let userauth = nsstring as? String {
                    return userauth
                }
        }
        return nil
    }
    
    public static func userid() -> String? {
        var defaults = AppGroupUtilities.userDefaults()
        if let defaults = defaults, stored = defaults.objectForKey("login-userid") as! NSData? {
            let decryptedData = RNDecryptor.decryptData(stored, withPassword: "key", error: nil)
            let nsstring = NSString(data: decryptedData, encoding: NSUTF8StringEncoding)
            if let userid = nsstring as? String {
                return userid
            }
        }
        return nil
    }
    
    public static func roles() -> Set<String>? {
        var defaults = AppGroupUtilities.userDefaults()
        if let defaults = defaults , data = defaults.objectForKey("login-roles") as? Set<String> {
            return data
        }
        return nil
    }
    
    public static func password() -> String? {
        if let userauth = UserInfo.userauth() {
            var error : NSError?
            let shaUserauth = UserInfo.sha1(userauth)
            let identifier = NSBundle.mainBundle().objectForInfoDictionaryKey("CFBundleIdentifier") as! String!
            let index = identifier.rangeOfString(".", options: .BackwardsSearch)?.startIndex
            let service = identifier.substringToIndex(index!)
            let p = KeychainWrapper.getPasswordForUsername(shaUserauth, andServiceName: service, error: &error)
            return p
        }
        
        return nil
    }
    
    static func sha1(input: String?) -> String? {
        if let input = input {
            let data = input.dataUsingEncoding(NSUTF8StringEncoding)!
            var digest = [UInt8](count:Int(CC_SHA1_DIGEST_LENGTH), repeatedValue: 0)
            CC_SHA1(data.bytes, CC_LONG(data.length), &digest)
            let output = NSMutableString(capacity: Int(CC_SHA1_DIGEST_LENGTH))
            for byte in digest {
                output.appendFormat("%02x", byte)
            }
            return output as String
        }
        return nil
        
    }

}
