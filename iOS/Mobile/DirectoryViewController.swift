    //
    //  DirectoryViewController.swift
    //  Mobile
    //
    //  Created by Jason Hocker on 12/2/15.
    //  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
    //
    
    import Foundation
    
    class DirectoryViewController : UITableViewController, UISearchBarDelegate, DirectoryFilterDelegate {
        
        let searchDelayInterval : NSTimeInterval = 0.5
        
        @IBOutlet var searchBar: UISearchBar!
        var module : Module?
        var hideStudents : Bool?
        var hideFaculty : Bool?
        
        var forcedFilteredGroup : String?
        var searchDelayer : NSTimer?
        var legacySearch = false
        var connection : NSURLConnection?
        var hiddenGroups = [String]()
        
        @IBOutlet var filterButton: UIBarButtonItem!
        var tableData = [[DirectoryEntry]]()
        var entries = [DirectoryEntry]()
        var data : NSMutableData?
        var groups = [DirectoryDefinitionProtocol]()
        var inSearch = false
        var sortByLastName = true
        
        override func viewDidLoad() {
            super.viewDidLoad()
            
            if entries.count > 0 {
                //coming from another source, such as course roster
                inSearch = true
                self.tableData = self.partitionObjects(self.entries, collationStringSelector: Selector("lastNameSort"))
            }
            
            buildGroups()
            
            tableView.rowHeight = UITableViewAutomaticDimension
            tableView.estimatedRowHeight = 53
            
            self.title = self.module?.name
            
            if UIScreen.mainScreen().traitCollection.userInterfaceIdiom == .Pad {
                self.splitViewController?.preferredDisplayMode = .AllVisible;
            } else {
                self.splitViewController?.preferredDisplayMode = .Automatic;
            }
            
            if groups.count <= 1 {
                filterButton.enabled = false;
                self.navigationController?.navigationBar.topItem!.rightBarButtonItem = nil;
            }
            
            if let searchBar = searchBar {
                searchBar.becomeFirstResponder()
            }
            
            
            NSNotificationCenter.defaultCenter().addObserver(self, selector: "signInHappened", name: kLoginExecutorSuccess, object: nil)
        }
        
        override func viewDidAppear(animated: Bool) {
            super.viewDidAppear(animated)
            sendView("Directory page", forModuleNamed: self.module?.name)
            
        }
        
        func doDelayedSearch(timer: NSTimer) {
            searchDelayer = nil
            doSearch()
        }
        
        func doSearch() {
            if !self.legacySearch {
                doModernSearch()
            } else {
                doLegacySearch()
            }
        }
        
        func doLegacySearch() {
            if let searchString = self.searchBar.text {
                if searchString.characters.count > 0 {
                    var url: String? = nil
                    if let module = module where self.module?.type == "directory" {
                        
                        let groupsInUse = self.groups.filter() {
                            !self.hiddenGroups.contains($0.internalName!)
                        }
                        let filteredGroups = groupsInUse.map() {
                            $0.internalName!
                        }
                        
                        if filteredGroups.count == 0 {
                            clear()
                            return
                        } else if filteredGroups.count == 2 {
                            url = module.propertyForKey("allSearch")
                        } else if filteredGroups.count == 1 && filteredGroups.contains("student") {
                            url = module.propertyForKey("studentSearch")
                        } else if filteredGroups.count == 1 && filteredGroups.contains("faculty") {
                            url = module.propertyForKey("facultySearch")
                        }
                    }
                    
                    let encodedSearchString = searchString.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())
                    url = "\(url!)?searchString=\(encodedSearchString!)"
                    
                    let request = NSMutableURLRequest(URL: NSURL(string: url!)!, cachePolicy: .UseProtocolCachePolicy, timeoutInterval: 30.0)
                    let authenticationMode = AppGroupUtilities.userDefaults()?.objectForKey("login-authenticationType") as? String
                    if authenticationMode == nil || authenticationMode == "native" {
                        request.addAuthenticationHeader()
                    }
                    
                    if let connection = self.connection {
                        connection.cancel()
                    }
                    self.connection = NSURLConnection(request: request, delegate: self)
                    UIApplication.sharedApplication().networkActivityIndicatorVisible = true
                    
                }
            } else {
                clear()
            }
        }
        
        func doModernSearch() {
            if let searchString = self.searchBar.text {
                if searchString.characters.count > 0 {
                    var url = module?.propertyForKey("baseSearch")
                    
                    var groupsInUse = self.groups
                    if self.hiddenGroups.count > 0 {
                        groupsInUse = self.groups.filter() {
                            !self.hiddenGroups.contains($0.internalName!)
                        }
                    }
                    
                    let filteredGroups = groupsInUse.map() {
                        return $0.internalName as String!
                        } as [String]
                    
                    if filteredGroups.count > 0 {
                        
                        let encodedSearchString = searchString.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())
                        let encodedDirectories = filteredGroups.joinWithSeparator(",").stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())
                        url = "\(url!)?searchString=\(encodedSearchString!)&directories=\(encodedDirectories!)"
                        let request = NSMutableURLRequest(URL: NSURL(string: url!)!, cachePolicy: .UseProtocolCachePolicy, timeoutInterval: 30.0)
                        if CurrentUser.sharedInstance().isLoggedIn {
                            request.addAuthenticationHeader()
                        }
                        if let connection = self.connection {
                            connection.cancel()
                        }
                        self.connection = NSURLConnection(request: request, delegate: self)
                        UIApplication.sharedApplication().networkActivityIndicatorVisible = true
                    } else {
                        clear()
                    }
                }
            } else {
                clear()
            }
        }
        
        func clear() {
            if !self.splitViewController!.collapsed {
                self.performSegueWithIdentifier("Empty Entry", sender: nil)
            }
            self.tableData = self.partitionObjects([DirectoryEntry](), collationStringSelector: Selector("lastNameSort"))
            self.tableView.reloadData()
            inSearch = false
        }
        
        // MARK: UISearchBarDelegate
        
        func searchBar(searchBar: UISearchBar, textDidChange searchText: String) {
            if let searchDelayer = searchDelayer {
                searchDelayer.invalidate()
            }
            if searchText.characters.count > 0 {
                searchDelayer = NSTimer.scheduledTimerWithTimeInterval(searchDelayInterval, target: self, selector: Selector("doDelayedSearch:"), userInfo: searchText, repeats: false)
                inSearch = true
            } else {
                clear()
            }
        }
        
        func searchBarSearchButtonClicked(searchBar: UISearchBar) {
            searchBar.resignFirstResponder()
            searchDelayer = nil
            doSearch()
        }
        
        //MARK: segue
        override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
            if segue.identifier == "Show Filter" {
                self.sendEventWithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionButton_Press, withLabel: "Select directory type", withValue: nil, forModuleNamed: self.module?.name)
                let navigationController = segue.destinationViewController as! UINavigationController
                let detailController = navigationController.viewControllers[0] as! DirectoryFilterViewController
                detailController.module = self.module
                detailController.hiddenGroups = self.hiddenGroups
                detailController.groups = self.groups
                detailController.delegate = self
            } else if segue.identifier == "Show Directory Profile" {
                let detailController : DirectoryEntryViewController
                if segue.destinationViewController is DirectoryEntryViewController {
                    detailController = segue.destinationViewController as! DirectoryEntryViewController
                } else {
                    let navigationController = segue.destinationViewController as! UINavigationController
                    detailController = navigationController.viewControllers[0] as! DirectoryEntryViewController
                }
                detailController.entry = sender as? DirectoryEntry;
                detailController.module = self.module;
            }
        }
        
        //MARK: other
        func partitionObjects(array: [DirectoryEntry], collationStringSelector selector: Selector) -> [[DirectoryEntry]] {
            let collation: UILocalizedIndexedCollation = UILocalizedIndexedCollation.currentCollation()
            let sectionCount = collation.sectionTitles.count
            //section count is take from sectionTitles and not sectionIndexTitles
            var unsortedSections = [[DirectoryEntry]](count: sectionCount, repeatedValue:[DirectoryEntry]())
            
            //put each object into a section
            for object in array {
                let index = collation.sectionForObject(object, collationStringSelector: selector)
                unsortedSections[index].append(object)
            }
            var sections = [[DirectoryEntry]](count: sectionCount, repeatedValue:[DirectoryEntry]())
            //sort each section
            for i in 0 ..< unsortedSections.count {
                let sorted = collation.sortedArrayFromArray(unsortedSections[i], collationStringSelector: selector) as! [DirectoryEntry]
                sections[i] = sorted
            }
            
            return sections;
        }
        
        func parseResponse(responseData: NSData) {
            entries = DirectoryEntry.parseResponse(responseData)
            buildTableData(entries)
            
        }
        
        func connection(theConnection: NSURLConnection, didReceiveData incrementalData: NSData) {
            if self.data == nil {
                self.data = NSMutableData(capacity: 2048)
            }
            self.data?.appendData(incrementalData)
            
        }
        
        func connectionDidFinishLoading(theConnection: NSURLConnection) {
            if let data = self.data {
                self.parseResponse(data)
            }
            UIApplication.sharedApplication().networkActivityIndicatorVisible = false
            self.data = nil
            self.connection = nil
        }
        
        func buildTableData(entries: [DirectoryEntry]) {
            
            var error: Unmanaged<CFError>?
            let addressBook: ABAddressBook? = ABAddressBookCreateWithOptions(nil, &error)?.takeRetainedValue()
            if let addressBook = addressBook {
                // request permission to use it
                ABAddressBookRequestAccessWithCompletion(addressBook) {
                    granted, error in
                    
                    if !granted {
                        self.sortByLastName = true
                        self.tableData = self.partitionObjects(self.entries, collationStringSelector: Selector("lastNameSort"))
                        dispatch_async(dispatch_get_main_queue(),{
                            self.tableView.reloadData()
                            
                        })
                    } else {
                        
                        let sort = Int(ABPersonGetSortOrdering())
                        if sort == Int(kABPersonSortByFirstName) {
                            self.sortByLastName = false
                            self.tableData = self.partitionObjects(self.entries, collationStringSelector: Selector("firstNameSort"))
                        }
                        else {
                            self.sortByLastName = true
                            self.tableData = self.partitionObjects(self.entries, collationStringSelector: Selector("lastNameSort"))
                        }
                        dispatch_async(dispatch_get_main_queue(),{
                            self.tableView.reloadData()
                        })
                    }
                }
            } else {
                self.sortByLastName = true
                self.tableData = self.partitionObjects(self.entries, collationStringSelector: Selector("lastNameSort"))
                self.tableView.reloadData()
            }
        }
        
        override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
            if section == 0 {
                var count = 0
                if inSearch && entries.count == 0 || searchBar?.text?.characters.count == 0 {
                    count += 1
                }
                if !inSearch {
                    count += 1
                }
                if !CurrentUser.sharedInstance().isLoggedIn {
                    count += 1
                }
                return count
            }
            if entries.count > 0 {
                return tableData[section-1].count
            }
            return 0
        }
        
        override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
            return UILocalizedIndexedCollation.currentCollation().sectionTitles.count + 1
        }
        
        var badImages = [String]()
        
        override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
            if indexPath.section == 0 {
                switch (indexPath.row, CurrentUser.sharedInstance().isLoggedIn, inSearch, entries.count == 0) {
                case (0, true, false, _), (0, false, false, _):
                    return tableView.dequeueReusableCellWithIdentifier("Directory Initial Message Cell", forIndexPath: indexPath) as UITableViewCell
                case (0, false, true, _), (1, false, false, _):
                    return tableView.dequeueReusableCellWithIdentifier("Directory Sign In Message Cell", forIndexPath: indexPath) as UITableViewCell
                case (0, true, true, true), (1, false, true, true):
                    return tableView.dequeueReusableCellWithIdentifier("Directory Empty Message Cell", forIndexPath: indexPath) as UITableViewCell
                default:
                    return UITableViewCell()
                }
            } else {
                let entry = self.tableData[indexPath.section-1][indexPath.row]
                let cell : UITableViewCell
                if let logo = entry.imageUrl where logo != "" {
                    if badImages.contains(logo) {
                        cell  = tableView.dequeueReusableCellWithIdentifier("Directory Name Cell", forIndexPath: indexPath) as UITableViewCell
                    } else {
                        cell  = tableView.dequeueReusableCellWithIdentifier("Directory Name Image Cell", forIndexPath: indexPath) as UITableViewCell
                        let imageView = cell.viewWithTag(3) as! UIImageView
                        imageView.loadImagefromURL(logo, successHandler: {
                            imageView.hidden = false
                            imageView.convertToCircleImage() }, failureHandler:  {
                            dispatch_async(dispatch_get_main_queue()) {
                                () -> Void in
                                imageView.hidden = true
                                self.badImages.append(logo)
                                self.tableView.reloadRowsAtIndexPaths([indexPath], withRowAnimation: .None)
                            }
                            
                            
                            }
                        )
                    }
                } else {
                    cell  = tableView.dequeueReusableCellWithIdentifier("Directory Name Cell", forIndexPath: indexPath) as UITableViewCell
                }
                
                let nameLabel = cell.viewWithTag(1) as! UILabel
                let typeLabel = cell.viewWithTag(2) as! UILabel
                nameLabel.text = entry.nameOrderedByFirstName(!self.sortByLastName)
                typeLabel.text = formatType(entry)
                return cell
            }
        }
        
        func formatType(entry: DirectoryEntry) -> String? {
            if let type = entry.type {
                if !legacySearch {
                    return type
                } else {
                    switch type {
                    case "student": return NSLocalizedString("Students", comment: "student search scope in directory")
                    case "faculty": return NSLocalizedString("Faculty/Staff", comment:"facilty/staff search scope in directory")
                    default: return type
                        
                    }
                }
                
            }
            return nil
        }
        
        override func tableView(tableView: UITableView, willSelectRowAtIndexPath indexPath: NSIndexPath) -> NSIndexPath? {
            return indexPath.section == 0 ? nil : indexPath
        }
        
        override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
            let entry = self.tableData[indexPath.section-1][indexPath.row]
            self.performSegueWithIdentifier("Show Directory Profile", sender: entry)
        }
        
        func updateFilter(filter: [String]) {
            if !self.splitViewController!.collapsed {
                self.performSegueWithIdentifier("Empty Entry", sender: nil)
            }
            self.hiddenGroups = filter
            if groups.count == filter.count {
                clear()
            } else {
                doSearch()
            }
        }
        
        func signInHappened() {
            dispatch_async(dispatch_get_main_queue(),{
                self.tableView.reloadData()
            })
        }
        
        func buildGroups () {
            
            
            let moduleKey = "\(module!.internalKey!)-hiddenGroups"
            if let hiddenGroups = AppGroupUtilities.userDefaults()?.arrayForKey(moduleKey) as? [String] {
                self.hiddenGroups = hiddenGroups
            } else {
                self.hiddenGroups = [String]()
            }
            
            do {
                let fetchRequest = NSFetchRequest(entityName: "DirectoryDefinition")
                fetchRequest.sortDescriptors = [ NSSortDescriptor(key: "displayName", ascending: true)]
                
                if let groups = try self.module?.managedObjectContext?.executeFetchRequest(fetchRequest) as? [DirectoryDefinition] where groups.count > 0 && module?.propertyForKey("directories") != nil {
                    let supportedDirectories = module?.propertyForKey("directories").componentsSeparatedByString(",")
                    let moduleGroups = groups.filter() {
                        supportedDirectories!.contains($0.key!)
                    }
                    self.groups = moduleGroups
                } else {
                    legacySearch = true
                    if self.module?.propertyForKey("student") == "true" && self.module?.propertyForKey("faculty") == "true" {
                        self.groups = [ legacyStudentDefinition(), legacyFacultyDefinition() ]
                    } else if self.module?.propertyForKey("student") == "true" {
                        self.groups = [ legacyStudentDefinition() ]
                    } else if self.module?.propertyForKey("faculty") == "true" {
                        self.groups = [ legacyFacultyDefinition() ]
                    } else {
                        self.groups = [ legacyStudentDefinition(), legacyFacultyDefinition() ]
                    }
                    self.groups = self.groups.sort( { $0.displayName < $1.displayName})
                }
            } catch {
            }
        }
        
        func legacyStudentDefinition() -> DirectoryDefinitionProtocol {
            let dir = DirectoryUnmanagedDefinition()
            dir.internalName = "student"
            dir.displayName = NSLocalizedString("Students", comment: "student search scope in directory")
            dir.authenticatedOnly = true
            return dir
        }
        
        func legacyFacultyDefinition() -> DirectoryDefinitionProtocol {
            let dir = DirectoryUnmanagedDefinition()
            dir.internalName = "faculty"
            dir.displayName = NSLocalizedString("Faculty/Staff", comment:"facilty/staff search scope in directory")
            dir.authenticatedOnly = true
            return dir
        }
    }