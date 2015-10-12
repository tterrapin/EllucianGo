//
//  DefaultsCache.swift
//  Mobile
//
//  Created by Bret Hansen on 9/21/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class DefaultsCache {
    
    private static let cacheKeysKey = "cache keys"
    private static let cacheLogoutKeysKey = "cache logout keys"
    private let key: String
    private let clearOnLogout: Bool
    private let defaults: NSUserDefaults
    
    init(key: String, clearOnLogout: Bool = true) {
        self.key = key
        self.clearOnLogout = clearOnLogout
        defaults = AppGroupUtilities.userDefaults()!
    }
    
    func store(data: AnyObject) {
        // key track of this key so it can be cleared later
        storeKey(key, keyCacheKey: DefaultsCache.cacheKeysKey)
        
        if clearOnLogout {
            storeKey(key, keyCacheKey: DefaultsCache.cacheLogoutKeysKey)
        }
        
        NSLog("Stored cache data for key: \(key)")
        defaults.setObject(data, forKey: key)
    }
    
    func fetch() -> AnyObject? {
        return defaults.objectForKey(key)
    }

    class func clearLogoutCaches() {
        let defaults = AppGroupUtilities.userDefaults()!
        
        if let cacheOfKeys = defaults.objectForKey(cacheLogoutKeysKey) as! [String]? {
            for key in cacheOfKeys {
                defaults.removeObjectForKey(key)
                NSLog("Cleared cache data for key: \(key)")
            }
        }
        
        // for grins remove the cache of keys too
        defaults.removeObjectForKey(cacheLogoutKeysKey)
    }
    
    class func clearAllCaches() {
        let defaults = AppGroupUtilities.userDefaults()!

        if let cacheOfKeys = defaults.objectForKey(cacheKeysKey) as! [String]? {
            for key in cacheOfKeys {
                defaults.removeObjectForKey(key)
                NSLog("Cleared cache data for key: \(key)")
            }
        }
        
        // for grins remove the cache of keys too
        defaults.removeObjectForKey(cacheKeysKey)
        defaults.removeObjectForKey(cacheLogoutKeysKey)
    }

    private func storeKey(key: String, keyCacheKey: String) {
        var cacheKeys = defaults.objectForKey(keyCacheKey) as! [String]?
        if cacheKeys == nil {
            cacheKeys = [String]()
        }
        
        if !cacheKeys!.contains(key) {
            cacheKeys?.append(key)
            defaults.setObject(cacheKeys, forKey: keyCacheKey)
        }
    }
}