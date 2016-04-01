//
//  ModuleExtensions.swift
//  Mobile
//
//  Created by Jason Hocker on 7/7/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

extension Module {
    
    func requiresAuthentication() -> Bool {
        var requiresAuthenticatedUser = false
        if let type = self.type {
            if type == "web" {
                if let property = self.propertyForKey("secure") where property == "true" {
                    requiresAuthenticatedUser = true
                }
            } else if type == "directory" {
                //for legacy supprt of mobile server < 4.5
                if ConfigurationManager.doesMobileServerSupportVersion("4.5") {
                    //if no directories defined
                    if let _ = propertyForKey("directories") {
                        requiresAuthenticatedUser = checkPlist()
                    } else {
                        requiresAuthenticatedUser = true
                    }
                } else {
                    requiresAuthenticatedUser = true
                }
            } else if type == "custom" {
                let customModuleType = self.propertyForKey("custom-type")
                
                if let customizationsPath = NSBundle.mainBundle().pathForResource("Customizations", ofType: "plist"), let customizationsDictionary = NSDictionary(contentsOfFile: customizationsPath) as? Dictionary<String, AnyObject> {
                    
                    let customModuleDefinitions = customizationsDictionary["Custom Modules"] as! Dictionary<String, AnyObject>
                    let moduleDefinition = customModuleDefinitions[customModuleType] as! Dictionary<String, AnyObject>
                    if let needsAuth = moduleDefinition["Needs Authentication"] {
                        requiresAuthenticatedUser = needsAuth.boolValue
                    }
                }
            } else {
                requiresAuthenticatedUser = checkPlist()
            }
        }
        if let roles = self.roles {
            let moduleRoles = Array(roles)
            let filteredRoles = moduleRoles.filter {
                let role = $0 as! ModuleRole
                return role.role != "Everyone"
            }
            
            if filteredRoles.count > 0 {
                requiresAuthenticatedUser = true
            }
        }
        return requiresAuthenticatedUser
    }
    
    func checkPlist() -> Bool {
        var requiresAuthenticatedUser = false
        if let ellucianPath = NSBundle.mainBundle().pathForResource("EllucianModules", ofType: "plist"), let ellucianDictionary = NSDictionary(contentsOfFile: ellucianPath) as? Dictionary<String, AnyObject> {
            
            let moduleDefinitions = ellucianDictionary
            let moduleDefinition = moduleDefinitions[self.type] as! Dictionary<String, AnyObject>
            if let needsAuth = moduleDefinition["Needs Authentication"] {
                requiresAuthenticatedUser = needsAuth.boolValue
            }
        }
        return requiresAuthenticatedUser
    }
}
