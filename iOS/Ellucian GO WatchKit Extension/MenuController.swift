
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
    @IBOutlet var retrievingDataLabel: WKInterfaceLabel!
    @IBOutlet var spinner: WKInterfaceImage!
    
    private let supportedModuleTypes = [ "ilp", "maps" ]
    private var watchAppTitle: String?
    
    private var fetchingConfigurationFlag = false
    
    func getTitle() -> String? {
        if watchAppTitle == nil {
            let plistPath = NSBundle.mainBundle().pathForResource("Customizations", ofType: "plist")
            let plistDictioanry = NSDictionary(contentsOfFile: plistPath!)!
            
            if let title = plistDictioanry["Watch Menu Title"] as! String? {
                self.watchAppTitle = title
            } else {
                self.watchAppTitle = "Ellucian GO"
            }
        }
        
        return self.watchAppTitle
    }
    
    override func awakeWithContext(context: AnyObject?) {
        super.awakeWithContext(context)
        
        // tell WatchConnectivityManager the menu controller instance
        WatchConnectivityManager.instance.saveRootController(self)
    }
    
    override func willActivate() {
        super.willActivate()

        NSLog("MenuController willActivate called")

        let prefs = AppGroupUtilities.userDefaults()!
        let _ = prefs.stringForKey("configurationUrl")
        let _ = prefs.stringForKey("watchkit-last-configurationUrl")
        
        let _ = prefs.objectForKey("menu updated date") as! NSDate?
        let _ = prefs.objectForKey("watchkit-last-updatedConfiguration") as! NSDate?
        
        self.initMenu()
        
        if let title = getTitle() {
            self.setTitle(title)
        }
    }
    
    override func table(table: WKInterfaceTable, didSelectRowAtIndex rowIndex: Int) {
        if(rowIndex == self.modules.count + 0) {
            pushControllerWithName("about",  context: nil)
        } else {
            let selectedModule = self.modules[rowIndex]
            
            if let user = WatchConnectivityManager.instance.currentUser() {
                
                var match = false
                let roles = selectedModule["roles"] as! [String]
                for roleName in roles {
                    if let userRoles = user["roles"] as! [String]? {
                        if (userRoles.contains(roleName)) {
                            match = true
                        }
                    }
                    
                    if roleName == "Everyone" {
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
        NSLog("MenuController initMenu called")
        
        if !fetchingConfigurationFlag {
            fetchingConfigurationFlag = true
            dispatch_async(dispatch_get_main_queue()) {
                self.chooseConfigurationLabel.setHidden(true)

                let configurationManager = ConfigurationManager.instance
                if configurationManager.isConfigurationLoaded() {
                    if configurationManager.shouldConfigurationBeRefreshed() {
                        // load in the background but show what we have
                        configurationManager.refreshConfigurationIfNeeded() {
                            (result) in
                            
                            self.fetchingConfigurationFlag = false
                            WatchConnectivityManager.instance.refreshUser()
                            if (result is Bool && result as! Bool) || configurationManager.isConfigurationLoaded() {
                                dispatch_async(dispatch_get_main_queue()) {
                                    self.initMenuAfterConfigurationLoaded()
                                }
                            } else if result is Bool && !(result as! Bool) {
                                self.chooseConfigurationLabel.setHidden(false)
                            }
                        }
                    } else {
                        // just show it
                        self.fetchingConfigurationFlag = false
                        self.initMenuAfterConfigurationLoaded()
                        WatchConnectivityManager.instance.refreshUser()
                    }
                } else {
                    // attempt to load configuration
                    self.retrievingDataLabel.setHidden(false)
                    self.spinner.startAnimating()
                    self.spinner.setHidden(false)
                    configurationManager.loadConfiguration() {
                        (result) in
                        
                        self.fetchingConfigurationFlag = false
                        WatchConnectivityManager.instance.refreshUser()
                        dispatch_async(dispatch_get_main_queue()) {
                            if (result is Bool && result as! Bool) || configurationManager.isConfigurationLoaded() {
                                self.initMenuAfterConfigurationLoaded()
                            } else if result is Bool && !(result as! Bool) {
                                self.retrievingDataLabel.setHidden(true)
                                self.spinner.stopAnimating()
                                self.spinner.setHidden(true)
                                self.chooseConfigurationLabel.setHidden(false)
                            } else {
                                self.retrievingDataLabel.setHidden(true)
                                self.spinner.stopAnimating()
                                self.spinner.setHidden(true)
                                
                                // communicaton error
                            }
                        }
                    }
                }
            }
        }
    }
    
    private func initMenuAfterConfigurationLoaded() {
        self.retrievingDataLabel.setHidden(true)
        self.spinner.stopAnimating()
        self.spinner.setHidden(true)

        // configuration is loaded - build menu
        let context = CoreDataManager.shared.managedObjectContext
        
        let modulesRequest = NSFetchRequest(entityName: "Module")
        modulesRequest.sortDescriptors = [NSSortDescriptor(key: "index" , ascending: true)]
        
        var rolePredicates = [NSPredicate]()
        rolePredicates.append(NSPredicate(format: "roles.@count == 0"))
        rolePredicates.append(NSPredicate(format: "ANY roles.role like %@", "Everyone"))
        
        if let user = WatchConnectivityManager.instance.currentUser() {
            if let roles = user["roles"] as! [String]? {
                for role in roles {
                    rolePredicates.append(NSPredicate(format: "ANY roles.role like %@", role))
                }
            }
        }
        
        let joinOnRolesPredicate = NSCompoundPredicate(orPredicateWithSubpredicates: rolePredicates)
        
        var typePredicates = [NSPredicate]()
        for supportedType in supportedModuleTypes {
            typePredicates.append(NSPredicate(format: "type == %@", supportedType))
        }
        let joinOnTypesPredicate = NSCompoundPredicate(orPredicateWithSubpredicates: typePredicates)

        var andPredicates = [NSPredicate]()
        andPredicates.append(joinOnRolesPredicate)
        andPredicates.append(joinOnTypesPredicate)
        
        let compoundPredicate = NSCompoundPredicate(andPredicateWithSubpredicates: andPredicates)
        
        do {
            let allModules = try context.executeFetchRequest(modulesRequest)
            
            // filter after Core Data fetch to avoid bug when there are >= about 10 predicates
            let modules = allModules.filter{ compoundPredicate.evaluateWithObject($0) }
            
            var modulesAsDictionaries = [Dictionary<String, AnyObject>]()
            
            for module in modules as! [Module] {
                if (module.type) != nil && supportedModuleTypes.contains(module.type) {
                    var properties = Dictionary<String, String>()
                    for property in module.properties as! Set<ModuleProperty>! {
                        properties[property.name] = property.value
                    }
                    
                    var roles = [String]()
                    for role in module.roles as! Set<ModuleRole>! {
                        roles.append(role.role)
                    }
                    
                    var moduleAsDictionary = [String : AnyObject]()
                    if ((module.iconUrl) != nil) { moduleAsDictionary["iconUrl"] = module.iconUrl }
                    if ((module.index) != nil) { moduleAsDictionary["index"] = module.index }
                    if ((module.internalKey) != nil) { moduleAsDictionary["internalKey"] = module.internalKey }
                    if ((module.name) != nil) { moduleAsDictionary["name"] = module.name }
                    if ((module.hideBeforeLogin) != nil) { moduleAsDictionary["hideBeforeLogin"] = module.hideBeforeLogin }
                    if ((module.type) != nil) { moduleAsDictionary["type"] = module.type }
                    moduleAsDictionary["properties"] = properties
                    moduleAsDictionary["roles"] = roles
                    
                    modulesAsDictionaries.append(moduleAsDictionary)
                }
            }
            self.setUpTable(modulesAsDictionaries)
            
        } catch {
            NSLog("Unable to query modules for menu")
        }
    }
    
    func setUpTable(modules: [Dictionary<String, AnyObject>]) {
        self.chooseConfigurationLabel.setHidden(true)
        
        let modulesCount = modules.count
        if(self.modules?.count != modulesCount) {
            self.clearTableRows()
            
            self.createTableFromModules(modules)
            
            self.menuTable.insertRowsAtIndexes(NSIndexSet(indexesInRange: NSMakeRange(modulesCount, 1)), withRowType: "MenuTableRowController")
            let row = self.menuTable.rowControllerAtIndex(modules.count) as! MenuTableRowController
            row.nameLabel.setText(NSLocalizedString("About", comment: "About menu item"))
            let defaults = AppGroupUtilities .userDefaults()
            if let aboutIcon : String = defaults?.stringForKey("about-icon") where aboutIcon.characters.count > 0 {
                
                dispatch_async(dispatch_get_main_queue(), {
                    let image = ImageCache.sharedCache().getImage(aboutIcon)
                    row.image.setImage(image)
                })
            } else {
                row.image.setImageNamed("icon-about")
            }
            
        } else {
            self.updateTableFromModules(modules)
        }
        self.modules = modules
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
                        let image = ImageCache.sharedCache().getImage(iconUrl)
                        rowInterfaceController.image.setImage(image)
                    })
                }
            }
            i += 1
        }
        let row = self.menuTable.rowControllerAtIndex(modules.count) as! MenuTableRowController
        row.nameLabel.setText(NSLocalizedString("About", comment: "About menu item"))
        let defaults = AppGroupUtilities .userDefaults()
        if let aboutIcon : String = defaults?.stringForKey("about-icon") where aboutIcon.characters.count > 0 {
            
            dispatch_async(dispatch_get_main_queue(), {
                let image = ImageCache.sharedCache().getImage(aboutIcon)
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
                        let image = ImageCache.sharedCache().getImage(iconUrl)
                        rowInterfaceController.image.setImage(image)
                    })
                }
            }
            i += 1
        }
    }
    
    // MARK - customized functions
    
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
