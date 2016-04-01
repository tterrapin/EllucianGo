//
//  ConfigurationSelectionViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 8/5/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class ConfigurationSelectionViewController : UITableViewController, UISearchResultsUpdating, NSFetchedResultsControllerDelegate {
    
    let itunesLink = "https://itunes.apple.com/us/app/ellucian-go/id607185179?mt=8"
    let searchController = UISearchController(searchResultsController: nil)
    var allItems = [Configuration]()
    var filteredItems = [Configuration]()
    
    var fetchInProgress = false
    
    lazy var liveConfigurationsUrl : String = {
        
        let plistPath = NSBundle.mainBundle().pathForResource("Customizations", ofType: "plist")
        let plistDictioanry = NSDictionary(contentsOfFile: plistPath!)!
        
        if let url = plistDictioanry["Live Configurations URL"] {
            return url as! String
        } else {
            return "https://mobile.elluciancloud.com/mobilecloud/api/liveConfigurations"
        }
    }()
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        buildSearchBar()
        self.navigationItem.leftBarButtonItem?.tintColor = UIColor.defaultHeaderColor()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector: Selector("outdated:"), name: kVersionCheckerOutdatedNotification, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: Selector("updateAvailable:"), name: kVersionCheckerUpdateAvailableNotification, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: Selector("fetchConfigurations"), name: "RefreshConfigurationListIfPresent", object: nil)
        
        self.fetchConfigurations()
    }
    
    func buildSearchBar() {
        self.searchController.searchResultsUpdater = self
        self.searchController.hidesNavigationBarDuringPresentation = false
        self.searchController.dimsBackgroundDuringPresentation = false
        self.searchController.searchBar.placeholder = NSLocalizedString("Search Schools", comment: "Placeholder text in search bar for switch schools")
        self.searchController.definesPresentationContext = true
        self.searchController.searchBar.sizeToFit()
        self.navigationItem.titleView = self.searchController.searchBar;
        tableView.sectionIndexBackgroundColor = UIColor.clearColor()
    }
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        if self.searchController.active {
            return 1
        } else {
            return UILocalizedIndexedCollation.currentCollation().sectionTitles.count
        }
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if (self.searchController.active) {
            return filteredItems.count
        } else {
            if let rows = rowsForSection(section) {
                return rows.count
            } else {
                return 0
            }
        }
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("ConfigurationCell", forIndexPath: indexPath) as UITableViewCell
        
        let configuration : Configuration
        if (self.searchController.active) {
            configuration = self.filteredItems[indexPath.row]
        } else {
            let rows = rowsForSection(indexPath.section)!
            configuration = rows[indexPath.row]
        }
        
        let nameLabel = cell.viewWithTag(1) as! UILabel
        nameLabel.text = configuration.configurationName
        return cell
    }
    
    override func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        if searchController.active {
            return nil
        } else {
            let sectionRows = rowsForSection(section)
            if let sectionRows = sectionRows where sectionRows.count > 0 {
                return self.sectionIndexTitlesForTableView(tableView)![section]
                
            } else {
                return nil
            }
        }
    }
    
    func rowsForSection(section: Int) -> [Configuration]? {
        let index = self.sectionIndexTitlesForTableView(self.tableView)![section]
        
        return allItems.filter {
            let name = $0.configurationName as String
            
            let range = name.rangeOfString(index, options: [NSStringCompareOptions.CaseInsensitiveSearch , NSStringCompareOptions.AnchoredSearch] )
            if let range  = range {
                if !range.isEmpty {
                    return true
                }
            }
            return false
        }
    }
    
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        let configuration : Configuration
        if (self.searchController.active) {
            configuration = self.filteredItems[indexPath.row]
        } else {
            let rows = rowsForSection(indexPath.section)!
            configuration = rows[indexPath.row]
        }
        schoolChosen(configuration)
    }
    
    func schoolChosen(configuration: Configuration) {
        let defaults = AppGroupUtilities.userDefaults()
        if let trackingId1 = defaults?.stringForKey("gaTracker1") {
            let tracker1 = GAI.sharedInstance().trackerWithTrackingId(trackingId1)
            tracker1.send(GAIDictionaryBuilder.createEventWithCategory(kAnalyticsCategoryUI_Action, action:kAnalyticsActionList_Select, label: "Choose Institution", value: nil).build() as [NSObject : AnyObject])
            
        }
        
        let hud = MBProgressHUD.showHUDAddedTo(self.view.window, animated: true)
        hud.labelText = NSLocalizedString("Loading", comment: "loading message while waiting for data to load")
        
        
        let delayTime = dispatch_time(DISPATCH_TIME_NOW,
            Int64(0.01 * Double(NSEC_PER_SEC)))
        dispatch_after(delayTime, dispatch_get_main_queue()) {
            let configurationUrl = configuration.configurationUrl
            let name = configuration.configurationName
            
            let delegate : AppDelegate = UIApplication.sharedApplication().delegate as! AppDelegate
            delegate.reset()
            
            defaults?.setObject(configurationUrl, forKey: "configurationUrl")
            defaults?.setObject(name, forKey: "configurationName")
            
            NSNotificationCenter.defaultCenter().removeObserver(self, name: kVersionCheckerUpdateAvailableNotification, object: nil)
            ConfigurationManager.instance.loadConfiguration(configurationUrl: configurationUrl) {
                (result) in
                
                ConfigurationManager.instance.loadMobileServerConfiguration() {
                    (result2) in
                    
                    dispatch_async(dispatch_get_main_queue()) {
                        MBProgressHUD.hideHUDForView(self.view.window, animated: true)
                        if (result is Bool && result as! Bool) {
                            NSNotificationCenter.defaultCenter().removeObserver(self)
                            
                            AppearanceChanger.applyAppearanceChanges(self.view)
                            
                            let storyboard = UIStoryboard(name: "HomeStoryboard", bundle: nil)
                            let slidingVC = storyboard.instantiateViewControllerWithIdentifier("SlidingViewController") as! ECSlidingViewController
                            slidingVC.anchorRightRevealAmount = 276
                            slidingVC.anchorLeftRevealAmount = 276
                            slidingVC.topViewAnchoredGesture = [ECSlidingViewControllerAnchoredGesture.Tapping, ECSlidingViewControllerAnchoredGesture.Panning]
                            let menu = storyboard.instantiateViewControllerWithIdentifier("Menu")
                            
                            if #available(iOS 9, *) {
                                let direction = UIView.userInterfaceLayoutDirectionForSemanticContentAttribute(slidingVC.view.semanticContentAttribute)
                                if direction == .RightToLeft {
                                    slidingVC.underRightViewController = menu
                                } else {
                                    slidingVC.underLeftViewController = menu
                                }
                            } else {
                                slidingVC.underLeftViewController = menu
                            }
                            
                            self.view.window?.rootViewController = slidingVC
                            delegate.slidingViewController = slidingVC
                            
                            NSOperationQueue.mainQueue().addOperation(OpenModuleHomeOperation())
                        } else {
                            self.tableView.deselectRowAtIndexPath(self.tableView.indexPathForSelectedRow!, animated: true)
                            self.fetchConfigurations()
                            dispatch_async(dispatch_get_main_queue()) {
                                ConfigurationFetcher.showErrorAlertView()
                            }
                        }
                    }
                }
            }
        }
    }
    
    func fetchConfigurations() {
        if fetchInProgress {
            return
        }
        UIApplication.sharedApplication().networkActivityIndicatorVisible = true
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), {
            self.fetchInProgress = true
            let defaults = AppGroupUtilities.userDefaults()
            var urlString = defaults?.stringForKey("mobilecloud-url")
            if urlString == nil || urlString?.characters.count == 0 {
                urlString = self.liveConfigurationsUrl
                defaults?.setObject(urlString, forKey: "mobilecloud-url")
                
            }
            let url = NSURL(string:urlString!)
            NSURLSession.sharedSession().dataTaskWithURL(url!,
                completionHandler: {
                    (data, response, error) -> Void in
                    UIApplication.sharedApplication().networkActivityIndicatorVisible = false
                    
                    if let _ = error {
                        self.showErrorAlert()
                    } else {
                        let json = JSON(data: data!)
                        let supportedVersions = json["versions"]["ios"].arrayValue.map { $0.string!}
                        if VersionChecker.checkVersion(supportedVersions) {
                            if let analytics = json["analytics"]["ellucian"].string {
                                defaults?.setObject(analytics, forKey: "gaTracker1")
                            }
                            
                            var configurations = [Configuration]()
                            let jsonInstitutions = json["institutions"].array
                            if let jsonInstitutions = jsonInstitutions {
                                for jsonInstitution in jsonInstitutions {
                                    let jsonConfigurations = jsonInstitution["configurations"].array
                                    for jsonConfiguration in jsonConfigurations! {
                                        let institutionId = jsonInstitution["id"].int!
                                        let institutionName = jsonInstitution["name"].string!
                                        let configurationId = jsonConfiguration["id"].int!
                                        let configurationName = jsonConfiguration["name"].string!
                                        let configurationUrl = jsonConfiguration["configurationUrl"].string!
                                        let keywords =  jsonConfiguration["keywords"].arrayValue.map { $0.string!}
                                        let configuration = Configuration(configurationId : configurationId,
                                            configurationUrl : configurationUrl,
                                            institutionId : institutionId,
                                            institutionName : institutionName,
                                            configurationName : configurationName,
                                            keywords : keywords)
                                        configurations.append(configuration)
                                    }
                                    
                                }
                            }
                            self.allItems = configurations.sort { $0.configurationName.localizedCaseInsensitiveCompare($1.configurationName) == NSComparisonResult.OrderedAscending }
                        }
                    }
                    dispatch_async(dispatch_get_main_queue(), {
                        self.fetchInProgress = false
                        self.tableView.reloadData()
                    })
                    
                }
                ).resume()
            
        })
    }
    
    func updateSearchResultsForSearchController(searchController: UISearchController) {
        if let searchBarText = self.searchController.searchBar.text where searchBarText.characters.count > 0 {
            
            //            let predicate = NSPredicate(format: "(configurationName CONTAINS[cd] %@) OR (institutionName CONTAINS[cd] %@) OR (ANY keywords CONTAINS[cd] %@)", searchBarText, searchBarText, searchBarText)
            
            
            self.filteredItems = self.allItems.filter( {
                
                if #available(iOS 9, *) {
                    return $0.configurationName.localizedStandardContainsString(searchBarText) ||
                        $0.institutionName.localizedStandardContainsString(searchBarText) ||
                        $0.keywords.filter{ $0.localizedStandardContainsString(searchBarText)
                            }.count > 0
                    
                } else {
                    return ($0.configurationName as NSString).localizedCaseInsensitiveContainsString(searchBarText) ||
                        ($0.institutionName as NSString).localizedCaseInsensitiveContainsString(searchBarText) ||
                        $0.keywords.filter{ ($0 as NSString).localizedCaseInsensitiveContainsString(searchBarText)
                            }.count > 0
                    
                }
                
                
                }
            )
        } else {
            self.filteredItems = self.allItems
        }
        tableView.reloadData()
    }
    
    func updateAvailable(sender: AnyObject) {
        dispatch_async(dispatch_get_main_queue(), {
            let alertController = UIAlertController(title: NSLocalizedString("Outdated", comment: "Outdated alert title"), message: NSLocalizedString("A new version is available.", comment: "Outdated alert message"), preferredStyle: .Alert)
            let okAction = UIAlertAction(title: NSLocalizedString("OK", comment: "OK"), style: .Cancel, handler: nil)
            alertController.addAction(okAction)
            let upgradeAction = UIAlertAction(title: NSLocalizedString("Upgrade", comment: "Upgrade software button label"), style: .Default) { (action) in
                self.openITunes()
            }
            alertController.addAction(upgradeAction)
            
            self.presentViewController(alertController, animated: true, completion: nil)
        })
    }
    
    func outdated(sender: AnyObject) {
        dispatch_async(dispatch_get_main_queue(), {
            let alertController = UIAlertController(title: NSLocalizedString("Outdated", comment: "Outdated alert title"), message: NSLocalizedString("The application must be upgraded to the latest version.", comment: "Force update alert message"), preferredStyle: .Alert)
            let upgradeAction = UIAlertAction(title: NSLocalizedString("Upgrade", comment: "Upgrade software button label"), style: .Cancel) { (action) in
                self.openITunes()
            }
            alertController.addAction(upgradeAction)
            
            self.presentViewController(alertController, animated: true, completion: nil)
        })
    }
    
    func openITunes() {
        UIApplication.sharedApplication().openURL(NSURL(string:itunesLink)!)
    }
    
    func showErrorAlert() {
        if self.allItems.count == 0 {
            dispatch_async(dispatch_get_main_queue(), {
                let alertController = UIAlertController(title: nil, message: NSLocalizedString("There are no institutions to display at this time.", comment: "configurations cannot be downloaded"), preferredStyle: .Alert)
                let okAction = UIAlertAction(title: NSLocalizedString("OK", comment: "OK"), style: .Cancel, handler: nil)
                alertController.addAction(okAction)
                
                self.presentViewController(alertController, animated: true, completion: nil)
            })
        }
    }
    
    override func sectionIndexTitlesForTableView(tableView: UITableView) -> [String]? {
        if searchController.active {
            return nil
        }
        return UILocalizedIndexedCollation.currentCollation().sectionIndexTitles
    }
    
    override func tableView(tableView: UITableView, sectionForSectionIndexTitle title: String, atIndex index: Int) -> Int {
        return UILocalizedIndexedCollation.currentCollation().sectionForSectionIndexTitleAtIndex(index)
        
    }
    
}
