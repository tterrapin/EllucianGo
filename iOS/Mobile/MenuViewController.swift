//
//  MenuViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 6/19/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import UIKit

class MenuViewController: UIViewController, UITableViewDataSource, UITableViewDelegate, SectionHeaderViewDelegate {

    var useSwitchSchool = true
    var managedObjectContext : NSManagedObjectContext?
    @IBOutlet var tableView: UITableView!
    var menuSectionInfo: [MenuSectionInfo]?
    var loaded = false

    override func viewDidLoad() {
        super.viewDidLoad()
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "reload", name: kSignInNotification, object: nil)

        tableView .registerClass(MenuTableViewHeaderFooterView.self, forHeaderFooterViewReuseIdentifier: "Header")
        tableView .registerClass(MenuTableViewHeaderFooterView.self, forHeaderFooterViewReuseIdentifier: "CollapseableHeader")
        
        readCustomizationsPropertyList()
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "registerObservers", name: "Mobile Server Configuration Load Succeeded", object: nil)
        
    }
    
    deinit {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        reload()
        
        dispatch_async(dispatch_get_global_queue(0, 0), {() -> Void in

            //Ellucian Mobile 4.0 -> 4.1 upgrade... if configurationUrl was known but doesn't have it cached in new structure, go refresh
            let configurationManager = ConfigurationManager.instance
            let defaults = AppGroupUtilities.userDefaults()
            let configurationUrl = defaults?.stringForKey("configurationUrl")
            
            if configurationManager.isConfigurationLoaded() || configurationUrl != nil {
                // trigger refresh if needed
                configurationManager.refreshConfigurationIfNeeded() {
                    (result) -> Void in
                    
                    if configurationManager.isConfigurationLoaded() {
                        dispatch_async(dispatch_get_main_queue()) {
                            AppearanceChanger.applyAppearanceChanges(self.view)
                            self.reload()
                        }
                    } else {
                        if self.loaded {
                            NSNotificationCenter.defaultCenter().postNotificationName(kConfigurationFetcherError, object: nil)
                        }
                    }
                }
            } else {
                NSOperationQueue.mainQueue().addOperation(OpenModuleConfigurationSelectionOperation())
            }
        })
    }
    
    // MARK: - Observers
    
    func registerObservers() {
        self.loaded = true
        NSNotificationCenter.defaultCenter().removeObserver(self)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "outdated:", name: "VersionCheckerOutdatedNotification", object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "updateAvailable:", name: "VersionCheckerUpdateAvailableNotification", object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "applicationDidBecomeActive:", name: UIApplicationDidBecomeActiveNotification, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "respondToSignOut:", name: "SignOutNotification", object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "notificationsUpdated:", name: "NotificationsUpdated", object: nil)
    }
    
    func outdated(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue()) {
            let alert = UIAlertController(title: NSLocalizedString("Outdated", comment: "Outdated alert title"), message: NSLocalizedString("The application must be upgraded to the latest version.", comment: "Force update alert message"), preferredStyle: UIAlertControllerStyle.Alert)
            alert.addAction(UIAlertAction(title: NSLocalizedString("Upgrade", comment: "Upgrade software button label"), style: .Default, handler: { action in
                 self.openITunes()
            }))
            self.presentViewController(alert, animated: true, completion: nil)
        }
    }
    
    func updateAvailable(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue()) {
            let alert = UIAlertController(title: NSLocalizedString("Outdated", comment: "Outdated alert title"), message: NSLocalizedString("A new version is available.", comment: "Outdated alert message"), preferredStyle: UIAlertControllerStyle.Alert)
            alert.addAction(UIAlertAction(title: NSLocalizedString("Upgrade", comment: "Upgrade software button label"), style: .Default, handler: { action in
                self.openITunes()
            }))
            alert.addAction(UIAlertAction(title: NSLocalizedString("OK", comment: "OK"), style: .Cancel, handler: nil))

            self.presentViewController(alert, animated: true, completion: nil)
        }
    }
    
    func applicationDidBecomeActive(notification: NSNotification) {
        reload()
        NotificationsFetcher.fetchNotifications(CoreDataManager.shared.managedObjectContext)
    }
    
    func respondToSignOut(notification: NSNotification) {
        if let rows = tableView.indexPathsForVisibleRows {
            self.tableView.reloadRowsAtIndexPaths(rows, withRowAnimation: .None)
        }
    }
    
    func notificationsUpdated(notifcation: NSNotification) {
        reload()
    }

    func reload() {
        let buildMenuOperation = OpenModuleFindModulesOperation()
        buildMenuOperation.completionBlock = {
            let modules = buildMenuOperation.modules
            self.drawMenu(modules)
            dispatch_async(dispatch_get_main_queue(),{
                self.tableView.reloadData()
                
            })
        }
        NSOperationQueue.mainQueue().addOperation(buildMenuOperation)
    }
    
    // MARK: - iTunes
    
    func openITunes () {
        let delegate : AppDelegate = UIApplication.sharedApplication().delegate as! AppDelegate
        delegate.reset()
        
        let iTunesLink = "http://appstore.com/elluciango"
        if let url = NSURL(string: iTunesLink) {
            UIApplication.sharedApplication().openURL(url)
        }
    }
    
    // MARK: protocol UITableViewDataSource
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if isActionSection(section) {
            return useSwitchSchool ? 4 : 3
        }
        let menuSectionInfo = self.menuSectionInfo![section]
        if menuSectionInfo.collapsed { return 0 }
        return menuSectionInfo.modules.count
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let section = indexPath.section
        if isActionSection(section) {
            return cellForActionsRow(indexPath)
        } else {
            return cellForModulesRow(indexPath)
        }
    }
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int
    {
        guard let _ = self.menuSectionInfo else { return 0 }
        return self.menuSectionInfo!.count
    }

    // MARK: UITableViewDelegate
    
    // Section header & footer information. Views are preferred over title should you decide to provide both
    
    func tableView(tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let menuSectionInfo = self.menuSectionInfo![section]
        
        let sectionHeaderView : MenuTableViewHeaderFooterView
        if menuSectionInfo.collapseable {
            sectionHeaderView =
                 tableView.dequeueReusableHeaderFooterViewWithIdentifier("CollapseableHeader") as! MenuTableViewHeaderFooterView
            
            var collapsedHeaders : [String]? = AppGroupUtilities.userDefaults()?.stringArrayForKey("menu-collapsed")
            if collapsedHeaders == nil {
                collapsedHeaders = []
            }
            let collapsed = (collapsedHeaders?.contains(menuSectionInfo.headerTitle!))!
            sectionHeaderView.collapsibleButton!.selected = collapsed;

        } else {
            sectionHeaderView = tableView.dequeueReusableHeaderFooterViewWithIdentifier("Header") as!MenuTableViewHeaderFooterView
        }

        if let headerLabel = sectionHeaderView.headerLabel {
            headerLabel.text = menuSectionInfo.headerTitle
        }
        sectionHeaderView.section = section
        sectionHeaderView.delegate = self

        return sectionHeaderView;
    }
    
    // Called after the user changes the selection.
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        switch (indexPath.section, indexPath.row, isActionSection(indexPath.section), self.useSwitchSchool) {
        case (_, 0, true, _) :
            NSOperationQueue.mainQueue().addOperation(OpenModuleHomeOperation())
        case (_, 1, true, _) :
            NSOperationQueue.mainQueue().addOperation(OpenModuleAboutOperation())
        case (_, 2, true, true):
            NSOperationQueue.mainQueue().addOperation(OpenModuleConfigurationSelectionOperation())
        case (_, 2, true, false), (_, 3, true, _):
            //sign out
            if CurrentUser.sharedInstance().isLoggedIn {
                sendEventWithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionMenu_selection, withLabel: "Menu-Click Sign Out", withValue: nil, forModuleNamed: nil)
                NSOperationQueue.mainQueue().addOperation(LoginSignOutOperation())
                let cell = tableView.cellForRowAtIndexPath(indexPath)
                if let cell = cell, nameLabel = cell.viewWithTag(101) as? UILabel {
                    nameLabel.text = NSLocalizedString("Sign In", comment: "label to sign in")
                }
                tableView.deselectRowAtIndexPath(indexPath, animated: true)
                reload()
            } else {
                sendEventWithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionMenu_selection, withLabel: "Menu-Click Sign In", withValue: nil, forModuleNamed: nil)
                let operation = LoginSignInOperation(controller: self)
                if let slidingViewController = self.view.window?.rootViewController as? ECSlidingViewController {
                    if slidingViewController.topViewController is UINavigationController && slidingViewController.topViewController.childViewControllers[0] is HomeViewController {
                        operation.successCompletionHandler = {
                            NSOperationQueue.mainQueue().addOperation(OpenModuleHomeOperation())
                        }
                    }
                }
                NSOperationQueue.mainQueue().addOperation(operation)
            }
        case (_, _, _, _) :
            //anything else
            let menuSectionInfo = self.menuSectionInfo![indexPath.section]
            let modules = menuSectionInfo.modules
            let module = modules[indexPath.row]
            NSOperationQueue.mainQueue().addOperation(OpenModuleOperation(module: module))
        }
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
    }

    // MARK: utility
    func isActionSection(sectionIndex: Int) -> Bool {
        guard let _ = self.menuSectionInfo else { return true }
        return menuSectionInfo!.count == 1 + sectionIndex
    }
    
    func cellForActionsRow(indexPath: NSIndexPath) -> UITableViewCell {
        
        let cell: UITableViewCell;
        
        if AppearanceChanger.isIOS8AndRTL() {
            cell = tableView.dequeueReusableCellWithIdentifier("Menu RTL Cell", forIndexPath: indexPath) as UITableViewCell
        } else {
            cell = tableView.dequeueReusableCellWithIdentifier("Menu Cell", forIndexPath: indexPath) as UITableViewCell
        }
        
        if let nameLabel = cell.viewWithTag(101) as? UILabel, imageView = cell.viewWithTag(102) as? UIImageView {
            switch (indexPath.row, self.useSwitchSchool) {
            case (0, _):
                nameLabel.text = NSLocalizedString("Home", comment: "Home menu item")
                imageView.image = UIImage(named: "icon-home")
            case (1, _):
                nameLabel.text = NSLocalizedString("About", comment: "About menu item")
                let iconUrl = AppGroupUtilities.userDefaults()?.stringForKey("about-icon")
                if iconUrl != nil {
                    imageView.image = ImageCache.sharedCache().getCachedImage(iconUrl)
                } else {
                    imageView.image = UIImage(named: "icon-about")
                }
            case (2, true):
                nameLabel.text = NSLocalizedString("Switch School", comment: "Switch school menu item")
                imageView.image = UIImage(named: "icon-switch-schools")
            case (2, false), (3, _):
                if CurrentUser.sharedInstance().isLoggedIn {
                    nameLabel.text = NSLocalizedString("Sign Out", comment: "Sign Out menu item");
                } else {
                    nameLabel.text = NSLocalizedString("Sign In", comment: "Sign In menu item");
                }
                imageView.image = UIImage(named: "icon-sign-in")
            default:
                ()
            }
        }
        if let countLabel = cell.viewWithTag(103) as? UILabel, lockImageView = cell.viewWithTag(104) as? UIImageView  {
            countLabel.text = nil
            countLabel.hidden = true
            lockImageView.hidden = true
        }
        return cell
        
    }
    
    func cellForModulesRow(indexPath: NSIndexPath) -> UITableViewCell {
        let cell: UITableViewCell;

        if AppearanceChanger.isIOS8AndRTL() {
            cell = tableView.dequeueReusableCellWithIdentifier("Menu RTL Cell", forIndexPath: indexPath) as UITableViewCell
        } else {
            cell = tableView.dequeueReusableCellWithIdentifier("Menu Cell", forIndexPath: indexPath) as UITableViewCell
        }
        
        let menuSectionInfo = self.menuSectionInfo![indexPath.section]
        let module = menuSectionInfo.modules[indexPath.row]

        if let nameLabel = cell.viewWithTag(101) as? UILabel {
            nameLabel.text = module.name
        }
        if let imageView = cell.viewWithTag(102) as? UIImageView {
            if let iconUrl = module.iconUrl {
                imageView.image = ImageCache.sharedCache().getCachedImage(iconUrl)
            } else {
                imageView.image = nil
            }
        }
        if let countLabel = cell.viewWithTag(103) as? UILabel, lockImageView = cell.viewWithTag(104) as? UIImageView  {
            
            countLabel.text = nil
            countLabel.hidden = true
            lockImageView.hidden = true
            
            if CurrentUser.sharedInstance().isLoggedIn {
                
                if module.type == "notifications" {
                    do{
                        let managedObjectContext = CoreDataManager.shared.managedObjectContext
                        let request = NSFetchRequest(entityName: "Notification")
                        request.predicate = NSPredicate(format: "read == %@", false)
                        request.includesSubentities = false
                        let notifications = try managedObjectContext.executeFetchRequest(request)
                        let count = notifications.count
                        countLabel.text = "\(count)"
                        drawLabel(countLabel)
                        countLabel.hidden = (count == 0)
                    } catch {
                    }
                }
                
                lockImageView.hidden = true
            } else {
                if module.requiresAuthentication() {
                    lockImageView.hidden = false
                }
            }
            
        }
        return cell
    }
    
    func drawLabel(label: UILabel) {
        let layer = label.layer
        layer.cornerRadius = label.bounds.size.height / 2
        label.textColor = UIColor.blackColor()
        label.font = UIFont.systemFontOfSize(14)
        label.textAlignment = NSTextAlignment.Center
        label.backgroundColor = UIColor(red: 102/255, green: 102/255, blue: 102/255, alpha: 1)
    }
    
    // MARK: SectionHeaderViewDelegate
    
    func sectionHeaderView(sectionHeaderView: MenuTableViewHeaderFooterView, sectionOpened section: Int) {
        let menuSectionInfo = self.menuSectionInfo![section]
        menuSectionInfo.collapsed = false
        
        let defaults = AppGroupUtilities.userDefaults()!
        var collapsedHeaders : [String]? = defaults.stringArrayForKey("menu-collapsed")
        if collapsedHeaders == nil {
            collapsedHeaders = []
        }
        collapsedHeaders = collapsedHeaders!.filter({ $0 != menuSectionInfo.headerTitle})
        defaults.setObject(collapsedHeaders, forKey: "menu-collapsed")
        
        let modules = menuSectionInfo.modules
        let countOfRowsToInsert = modules.count
        var indexPathsToInsert = [NSIndexPath]()
        for index in 0 ..< countOfRowsToInsert {
            indexPathsToInsert.append( NSIndexPath(forRow: index, inSection: section) )
        }
        
        sectionHeaderView.collapsibleButton?.accessibilityLabel = NSLocalizedString("Toggle menu section", comment:"Accessibility label for toggle menu section button")
        
        tableView.beginUpdates()
        tableView.insertRowsAtIndexPaths(indexPathsToInsert, withRowAnimation: .None)
        tableView.endUpdates()
        
        UIAccessibilityPostNotification(UIAccessibilityLayoutChangedNotification, sectionHeaderView);
        
    }
    
    func sectionHeaderView(sectionHeaderView: MenuTableViewHeaderFooterView, sectionClosed section: Int) {
        let menuSectionInfo = self.menuSectionInfo![section]
        menuSectionInfo.collapsed = true
        
        let defaults = AppGroupUtilities.userDefaults()!
        var collapsedHeaders : [String]? = defaults.stringArrayForKey("menu-collapsed")
        if collapsedHeaders == nil {
            collapsedHeaders = []
        }
        collapsedHeaders!.append(menuSectionInfo.headerTitle!)
        defaults.setObject(collapsedHeaders, forKey: "menu-collapsed")
        
        let modules = menuSectionInfo.modules
        let countOfRowsToInsert = modules.count
        var indexPathsToInsert = [NSIndexPath]()
        for index in 0 ..< countOfRowsToInsert {
            indexPathsToInsert.append( NSIndexPath(forRow: index, inSection: section) )
        }
        
        sectionHeaderView.collapsibleButton?.accessibilityLabel = NSLocalizedString("Toggle menu section", comment:"Accessibility label for toggle menu section button")
        
        tableView.beginUpdates()
        tableView.deleteRowsAtIndexPaths(indexPathsToInsert, withRowAnimation: .None)
        tableView.endUpdates()
        
        UIAccessibilityPostNotification(UIAccessibilityLayoutChangedNotification, sectionHeaderView);

    }
    
    // MARK: draw
    
    func drawMenu(modules: [Module]) {
        
        var collapsedHeaders : [String]? = AppGroupUtilities.userDefaults()?.stringArrayForKey("menu-collapsed")
        if collapsedHeaders == nil {
            collapsedHeaders = []
        }
        
        var infoArray = [MenuSectionInfo]()
        
        var tempInfo = MenuSectionInfo()
        
        if modules.count > 0 {
            for module in modules {
                if module.type == "header" {
                    tempInfo = MenuSectionInfo()
                    tempInfo.collapsed = (collapsedHeaders?.contains(module.name))!
                    tempInfo.headerTitle = module.name
                    tempInfo.collapseable = true
                    tempInfo.modules = [Module]()
                    infoArray.append(tempInfo)
                } else {
                    if infoArray.count == 0 {
                        let localizedApplications = NSLocalizedString("Applications", comment:"Applications menu heading")
                        tempInfo = MenuSectionInfo()
                        tempInfo.collapsed = (collapsedHeaders?.contains(localizedApplications))!
                        tempInfo.headerTitle = localizedApplications
                        tempInfo.collapseable = true
                        tempInfo.modules = [Module]()
                        infoArray.append(tempInfo)
                        tempInfo.modules.append(module)
                    } else {
                        tempInfo.modules.append(module)
                    }
                }
            }
        } else {
            let localizedApplications = NSLocalizedString("Applications", comment:"Applications menu heading")
            tempInfo = MenuSectionInfo()
            tempInfo.collapsed = (collapsedHeaders?.contains(localizedApplications))!
            tempInfo.headerTitle = localizedApplications
            tempInfo.collapseable = true
            tempInfo.modules = [Module]()
            infoArray.append(tempInfo)
            
        }
        
        
        let localizedActions = NSLocalizedString("Actions", comment:"Actions menu heading")
        tempInfo = MenuSectionInfo()
        tempInfo.collapsed = false
        tempInfo.headerTitle = localizedActions
        tempInfo.collapseable = false
        tempInfo.modules = [Module]()
        infoArray.append(tempInfo)
        
        self.menuSectionInfo = infoArray
    }
    
    private func readCustomizationsPropertyList() {
        if let customizationsPath = NSBundle.mainBundle().pathForResource("Customizations", ofType: "plist") , let customizationsDictionary = NSDictionary(contentsOfFile: customizationsPath) as? Dictionary<String, AnyObject> {
            if let useSwitchSchool = customizationsDictionary["Allow Switch School"] {
                self.useSwitchSchool = useSwitchSchool as! Bool
            }
        }
    }
    
    func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell, forRowAtIndexPath indexPath: NSIndexPath) {
        cell.backgroundColor = UIColor(white: 0.163037, alpha: 1.0)
    }
}


class MenuSectionInfo {
    
    var collapsed : Bool = false
    var collapseable : Bool = false
    var modules = [Module]()
    var headerTitle : String?
}



