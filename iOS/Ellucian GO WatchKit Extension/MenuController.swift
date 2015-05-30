
//
//  MenuController.swift
//  Ellucian GO WatchKit Extension
//
//  Created by Jason Hocker on 4/24/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import WatchKit
import Foundation
import CoreData


class MenuController: WKInterfaceController {
    
    @IBOutlet var menuTable: WKInterfaceTable!
    var modules : [Dictionary<String, AnyObject>]!
    
    @IBOutlet var chooseConfigurationLabel: WKInterfaceLabel!
    
    override func awakeWithContext(context: AnyObject?) {
        super.awakeWithContext(context)
    }
    
    override func willActivate() {
        super.willActivate()
        
        let prefs = AppGroupUtilities.userDefaults()!
        let configurationUrl = prefs.stringForKey("configurationUrl")
        let lastConfigurationUrl = prefs.stringForKey("watchkit-last-configurationUrl")
        
        let updateDate = prefs.objectForKey("menu updated date") as! NSDate?
        let watchUpdateDate = prefs.objectForKey("watchkit-last-updatedConfiguration") as! NSDate?
        
        self.initMenu()
    }
    
    override func table(table: WKInterfaceTable, didSelectRowAtIndex rowIndex: Int) {
        if(rowIndex == self.modules.count + 0) {
            pushControllerWithName("about",  context: nil)
        } else {
            let selectedModule = self.modules[rowIndex]
            
            if let userauth = UserInfo.userauth() {
                
                var match = false
                let roles = selectedModule["roles"] as! [String]
                for roleName in roles {
                    if let userRoles = UserInfo.roles() {
                        if (userRoles.contains(roleName)) {
                            match = true
                        }
                    } else if roleName == "Everyone" {
                        match = true
                    }
                    
                }
                if(roles.count == 0) {
                    match = true
                }
                
                if !match {
                    self.presentControllerWithName("You do not have permission", context: nil)
                }
            }
            pushController(selectedModule)
        }
    }
    
    func initMenu() {
        
        let prefs = AppGroupUtilities.userDefaults()!
        if let configurationUrl = prefs.stringForKey("configurationUrl") {
            prefs.setValue(configurationUrl, forKey: "watchkit-last-configurationUrl")
            
            let updateDate = prefs.objectForKey("menu updated date") as! NSDate?
            if var updateDate = updateDate {
                
                var dictionary = ["action": "fetch configuration"]
                WKInterfaceController.openParentApplication(dictionary as [NSObject : AnyObject], reply: { (replyInfo, error) -> Void in
                    var dictionary = replyInfo as NSDictionary
                    let definedModules = dictionary["modules"] as! [Dictionary<String, AnyObject>]
                    let prefs2 = AppGroupUtilities.userDefaults()!
                    var updateDate2 = prefs2.objectForKey("menu updated date") as! NSDate?

                    prefs2.setValue(updateDate2, forKey: "watchkit-last-updatedConfiguration")
                    
                    let filteredModules = self.filterModules(definedModules)
                    self.setUpTable(filteredModules)
                })
            }
        } else {
            self.chooseConfigurationLabel.setHidden(false)
        }
        
        
    }
    
    func setUpTable(modules: [Dictionary<String, AnyObject>]) {
        let prefs = AppGroupUtilities.userDefaults()!
        if let configurationUrl = prefs.stringForKey("configurationUrl") {
            self.chooseConfigurationLabel.setHidden(true)
            
            let modulesCount = modules.count
            if(self.modules?.count != modulesCount) {
                self.clearTableRows()
                
                self.createTableFromModules(modules)
                
                self.menuTable.insertRowsAtIndexes(NSIndexSet(indexesInRange: NSMakeRange(modulesCount, 1)), withRowType: "MenuTableRowController")
                let row = self.menuTable.rowControllerAtIndex(modules.count) as! MenuTableRowController
                row.nameLabel.setText(NSLocalizedString("About", comment: "About menu item"))
                let defaults = AppGroupUtilities .userDefaults()
                if let aboutIcon : String = defaults?.stringForKey("about-icon") where count(aboutIcon) > 0 {
                    
                    dispatch_async(dispatch_get_main_queue(), {
                        var image = ImageCache.sharedCache().getImage(aboutIcon)
                        row.image.setImage(image)
                    })
                } else {
                    row.image.setImageNamed("icon-about")
                }
                
            } else {
                self.updateTableFromModules(modules)
            }
            self.modules = modules
        } else {
            self.chooseConfigurationLabel.setHidden(false)
        }
    }
    
    // MARK: setUpTable helper methods
    
    private func clearTableRows() {
        self.menuTable.removeRowsAtIndexes(NSIndexSet(indexesInRange: NSMakeRange(0, self.menuTable.numberOfRows)))
        
    }
    
    private func updateTableFromModules(modules:[Dictionary<String, AnyObject>]) {
        var i = 0
        for module in modules {
            if let rowInterfaceController = self.menuTable.rowControllerAtIndex(i) as? MenuTableRowController {
                
                rowInterfaceController.nameLabel.setText(module["name"] as? String)
                
                if let iconUrl = module["iconUrl"] as? String {
                    
                    dispatch_async(dispatch_get_main_queue(), {
                        var image = ImageCache.sharedCache().getImage(iconUrl)
                        rowInterfaceController.image.setImage(image)
                    })
                }
            }
            i++
        }
        let row = self.menuTable.rowControllerAtIndex(modules.count) as! MenuTableRowController
        row.nameLabel.setText(NSLocalizedString("About", comment: "About menu item"))
        let defaults = AppGroupUtilities .userDefaults()
        if let aboutIcon : String = defaults?.stringForKey("about-icon") where count(aboutIcon) > 0 {
            
            dispatch_async(dispatch_get_main_queue(), {
                var image = ImageCache.sharedCache().getImage(aboutIcon)
                row.image.setImage(image)
            })
        } else {
            row.image.setImageNamed("icon-about")
        }
    }
    
    private func createTableFromModules(modules:[Dictionary<String, AnyObject>]) {
        self.menuTable.insertRowsAtIndexes(NSIndexSet(indexesInRange: NSMakeRange(0, modules.count)), withRowType: "MenuTableRowController")
        
        var i = 0
        for module in modules {
            
            if let rowInterfaceController = self.menuTable.rowControllerAtIndex(i) as? MenuTableRowController{
                
                rowInterfaceController.nameLabel.setText(module["name"] as? String)
                
                if let iconUrl = module["iconUrl"] as? String  {
                    
                    dispatch_async(dispatch_get_main_queue(), {
                        var image = ImageCache.sharedCache().getImage(iconUrl)
                        rowInterfaceController.image.setImage(image)
                    })
                }
            }
            i++
        }
    }
    
    // MARK - customized functions
    
    func filterModules(definedModules: [Dictionary<String, AnyObject>]) -> [Dictionary<String, AnyObject>] {
        let modules = definedModules.filter{ (module) in
            switch module["type"] as! String {
            case "maps", "ilp":
                return true
            default:
                return false
            }
        }
        return modules
    }
    
    func pushController(selectedModule: Dictionary<String, AnyObject>) {
        switch selectedModule["type"] as! String  {
        case "maps":
            let properties = selectedModule["properties"] as! Dictionary<String, String>
            let url = properties["campuses"] as String!
            self.pushControllerWithName("maps",  context: ["internalKey": selectedModule["internalKey"] as! String, "title": selectedModule["name"] as! String, "campuses": url])
        case "ilp":
            let properties = selectedModule["properties"] as! Dictionary<String, String>
            let url = properties["ilp"] as String!
            self.pushControllerWithName("ilp",  context: ["internalKey": selectedModule["internalKey"] as! String, "title": selectedModule["name"] as! String, "ilp": url])
        default:
            pushControllerWithName(selectedModule["type"] as! String,  context: nil)
        }
    }
}
