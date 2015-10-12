//
//  FeedViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 7/29/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class FeedViewController : UITableViewController, UISearchResultsUpdating , NSFetchedResultsControllerDelegate, FeedFilterDelegate {
    
    
    @IBOutlet var filterButton: UIBarButtonItem!
    
    let searchController = UISearchController(searchResultsController: nil)
    var module : Module?
    
    let datetimeOutputFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
        
        }()
    let dateFormatterSectionHeader : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateStyle = .ShortStyle
        formatter.timeStyle = .NoStyle
        formatter.doesRelativeDateFormatting = true
        return formatter
        }()
    var feedModule : FeedModule?
    var hiddenCategories = NSMutableSet()
    var searchString : String?
    var thumbnailCache = [String: UIImage]()
    
    override func viewDidLoad() {
        super.viewDidLoad()

        self.title = self.module?.name
        tableView.estimatedRowHeight = 100
        tableView.rowHeight = UITableViewAutomaticDimension
        buildSearchBar()
        
        if UIScreen.mainScreen().traitCollection.userInterfaceIdiom == .Pad {
            self.splitViewController?.preferredDisplayMode = .AllVisible;
        } else {
            self.splitViewController?.preferredDisplayMode = .Automatic;
        }
        
        fetchFeeds()
        reloadData()
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        sendView("News List", forModuleNamed: self.module?.name)
    }
    
    // MARK: UISearchResultsUpdating delegate
    func updateSearchResultsForSearchController(searchController: UISearchController) {
        self.sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction:kAnalyticsActionSearch, withLabel:"Search", withValue:nil, forModuleNamed:self.module!.name);
        _fetchedResultsController = nil
        self.tableView.reloadData()
    }
    
    // MARK: search
    func buildSearchBar() {
        self.searchController.searchResultsUpdater = self
        self.searchController.hidesNavigationBarDuringPresentation = false
        self.searchController.dimsBackgroundDuringPresentation = false
        self.searchController.definesPresentationContext = true
        self.searchController.searchBar.sizeToFit()
        self.tableView.tableHeaderView = searchController.searchBar
    }
    
    // MARK: data retrieval
    func fetchFeeds() {
        let privateContext = NSManagedObjectContext(concurrencyType: .PrivateQueueConcurrencyType)
        privateContext.parentContext = self.module?.managedObjectContext
        privateContext.undoManager = nil
        
        let urlString = self.module?.propertyForKey("feed")
        
        if self.fetchedResultsController.fetchedObjects!.count <= 0 {
            let hud = MBProgressHUD.showHUDAddedTo(self.view, animated: true)
            hud.labelText = NSLocalizedString("Loading", comment: "loading message while waiting for data to load")
        }
        
        privateContext.performBlock { () -> Void in
            
            UIApplication.sharedApplication().networkActivityIndicatorVisible = true
            
            defer {
                dispatch_async(dispatch_get_main_queue()) {
                    
                    UIApplication.sharedApplication().networkActivityIndicatorVisible = false
                    
                    MBProgressHUD.hideHUDForView(self.view, animated: true)
                }
            }
            
            do {
                let url = NSURL(string: urlString!)
                let responseData = try NSData(contentsOfURL: url!, options:NSDataReadingOptions())
                let json = JSON(data: responseData)
                
                var previousFeeds = [Feed]()
                var existingFeeds = [Feed]()
                var newKeys = [String]()
                
                let request = NSFetchRequest(entityName: "Feed")
                request.predicate = NSPredicate(format:"module.name = %@", self.module!.name)
                let oldObjects = try privateContext
                    .executeFetchRequest(request)
                for oldObject in oldObjects {
                    let feed = oldObject as! Feed
                    if feed.entryId.characters.count > 0 {
                        previousFeeds.append(feed)
                    } else {
                        privateContext.deleteObject(feed)
                    }
                }
                
                let moduleRequest = NSFetchRequest(entityName: "FeedModule")
                moduleRequest.predicate = NSPredicate(format: "name = %@", self.module!.name)
                let feedModules = try privateContext.executeFetchRequest(moduleRequest)
                let feedModule : FeedModule
                if feedModules.count > 0 {
                    feedModule = feedModules.last as! FeedModule
                } else {
                    feedModule = NSEntityDescription.insertNewObjectForEntityForName("FeedModule", inManagedObjectContext: privateContext) as! FeedModule
                    feedModule.name = self.module?.name
                    
                    
                }
                
                let categoryRequest = NSFetchRequest(entityName: "FeedCategory")
                categoryRequest.predicate = NSPredicate(format: "moduleName = %@", self.module!.name)
                let categoryArray = try privateContext.executeFetchRequest(categoryRequest)
                var categoryMap = [String: FeedCategory]()
                for feedCategory in categoryArray as! [FeedCategory]  {
                    categoryMap[feedCategory.name] = feedCategory
                }
                
                let dateFormatter = NSDateFormatter()
                dateFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
                dateFormatter.timeZone = NSTimeZone(forSecondsFromGMT: 0)
                
                for entry in json["entries"].array! {
                    let uid = entry["entryId"].string;
                    
                    let filteredArray = previousFeeds.filter({
                        let feed = $0 as Feed
                        return feed.entryId == uid;
                    })
                    if filteredArray.count > 0 {
                        existingFeeds.append(filteredArray[0])
                    } else {
                        let feed = NSEntityDescription.insertNewObjectForEntityForName("Feed", inManagedObjectContext: privateContext) as! Feed
                        feed.module = feedModule
                        feedModule.addFeedsObject(feed)
                        
                        newKeys.append(uid!)
                        
                        feed.entryId = entry["entryId"].string
                        let postDate = entry["postDate"].string!
                        feed.postDateTime = dateFormatter.dateFromString(postDate)
                        feed.dateLabel = self.datetimeOutputFormatter.stringFromDate(feed.postDateTime)
                        if let links = entry["link"].array where links.count > 0 {
                            feed.link = links[0].string
                        }
                        if let title = entry["title"].string where title != "" {
                            feed.title = title.stringByConvertingHTMLToPlainText()
                        }
                        if let content = entry["content"].string where content != "" {
                            feed.content = content
                        }
                        if let logo = entry["logo"].string where logo != "" {
                            feed.logo = logo
                        }
                        
                        let categoryLabel = entry["feedName"].string
                        var category = categoryMap[categoryLabel!]
                        if category == nil {
                            category = NSEntityDescription.insertNewObjectForEntityForName("FeedCategory", inManagedObjectContext: privateContext) as? FeedCategory
                            category!.name = categoryLabel
                            category!.moduleName = self.module?.name
                            categoryMap[categoryLabel!] = category
                        }
                        feed.addCategoryObject(category)
                        category!.addFeedObject(feed)
                        
                    }
                }
                
                try privateContext.save()
                for oldObject in previousFeeds {
                    if !existingFeeds.contains(oldObject) {
                        privateContext.deleteObject(oldObject)
                    }
                }
                try privateContext.save()
                
                privateContext.parentContext?.performBlock({
                    do {
                        try privateContext.save()
                    } catch let error {
                        print (error)
                    }
                })
                
                
                
                dispatch_async(dispatch_get_main_queue()) {
                    self.readFeedModule()
                    self.filterButton.enabled = true
                    
                }
                
            } catch let error {
                print (error)
            }
        }
    }
    
    //MARK: segue
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "Show Feed Filter" {
            self.sendEventWithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionList_Select, withLabel: "Select filter", withValue: nil, forModuleNamed: self.module?.name)
            let navigationController = segue.destinationViewController as! UINavigationController
            let detailController = navigationController.viewControllers[0] as! FeedFilterViewController
            detailController.feedModule = self.feedModule
            detailController.hiddenCategories = self.hiddenCategories
            detailController.module = self.module
            detailController.delegate = self
        } else if segue.identifier == "Show Detail" || segue.identifier == "Show Detail with Image" {
            let detailController = (segue.destinationViewController as! UINavigationController).topViewController as! FeedDetailViewController
            let feed = fetchedResultsController.objectAtIndexPath(self.tableView.indexPathForSelectedRow!) as! Feed
            detailController.feed = feed
            detailController.module = self.module
        }
    }
    
    //MARK read
    func readFeedModule() {
        let request = NSFetchRequest(entityName: "FeedModule")
        request.predicate = NSPredicate(format: "name = %@", self.module!.name)
        request.fetchLimit = 1
        do {
            let results = try self.module?.managedObjectContext?.executeFetchRequest(request) as! [FeedModule]
            if results.count > 0 {
                self.feedModule = results[0]
                let hiddenCategories = self.feedModule?.hiddenCategories
                if let hiddenCategories = hiddenCategories {
                    let array = hiddenCategories.componentsSeparatedByString(",")
                    self.hiddenCategories = NSMutableSet(array: array)
                    
                } else {
                    self.hiddenCategories = NSMutableSet()
                }
            } else {
                self.hiddenCategories = NSMutableSet()
                
            }
        } catch {}
    }
    
    //MARK: FeedFilterDelegate
    func reloadData() {
        self.readFeedModule()
        _fetchedResultsController = nil
        do {
            try self.fetchedResultsController.performFetch()
        } catch { }
        self.tableView.reloadData()
    }
    
    // MARK: fetch
    var fetchedResultsController: NSFetchedResultsController {
        // return if already initialized
        if self._fetchedResultsController != nil {
            return self._fetchedResultsController!
        }
        let managedObjectContext = CoreDataManager.shared.managedObjectContext
        
        let request = NSFetchRequest()
        request.entity = NSEntityDescription.entityForName("Feed", inManagedObjectContext: managedObjectContext)
        
        
        var subPredicates = [NSPredicate]()
        
        subPredicates.append( NSPredicate(format: "module.name = %@", self.module!.name) )
        
        if let searchString = self.searchController.searchBar.text where searchString.characters.count > 0 {
            subPredicates.append( NSPredicate(format: "((title CONTAINS[cd] %@) OR (content CONTAINS[cd] %@))", searchString, searchString) )
        }
        
        if self.hiddenCategories.count > 0 {
            subPredicates.append( NSPredicate(format: "NONE category.name IN %@",  self.hiddenCategories) )
        }
        
        request.predicate = NSCompoundPredicate(andPredicateWithSubpredicates: subPredicates)
        
        request.sortDescriptors = [NSSortDescriptor(key: "dateLabel", ascending: false),NSSortDescriptor(key: "postDateTime", ascending: false),NSSortDescriptor(key: "title", ascending: true)]
        
        let aFetchedResultsController = NSFetchedResultsController(fetchRequest: request, managedObjectContext: managedObjectContext, sectionNameKeyPath: "dateLabel", cacheName: nil)
        aFetchedResultsController.delegate = self
        self._fetchedResultsController = aFetchedResultsController
        
        do {
            try self._fetchedResultsController!.performFetch()
            
        } catch let error {
            print("fetch error: \(error)")
        }
        
        return self._fetchedResultsController!
    }
    var _fetchedResultsController: NSFetchedResultsController?
    
    //MARK :UITable
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return (fetchedResultsController.sections?.count)!
    }
    
    override func tableView(tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {

        let view = UIView(frame: CGRectMake(0, 0, CGRectGetWidth(tableView.frame), 30))
        let label = UILabel(frame: CGRectMake(8,0,CGRectGetWidth(tableView.frame), 30))
        label.translatesAutoresizingMaskIntoConstraints = false
        
        if let sections = fetchedResultsController.sections {
            let currentSection = sections[section] as NSFetchedResultsSectionInfo
            let header = currentSection.name
            let date = datetimeOutputFormatter.dateFromString(header)
            label.text = dateFormatterSectionHeader.stringFromDate(date!)
        }
        
        label.textColor = UIColor(red: 0.2, green: 0.2, blue: 0.2, alpha: 1.0)
        view.backgroundColor = UIColor(rgba: "#e6e6e6")
        label.font = UIFont.preferredFontForTextStyle(UIFontTextStyleHeadline)
        
        view.addSubview(label)
        
        let viewsDictionary = ["label": label, "view": view]
        
        // Create and add the vertical constraints
        view.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("V:|-1-[label]-1-|",
            options: NSLayoutFormatOptions.AlignAllBaseline,
            metrics: nil,
            views: viewsDictionary))
        
        // Create and add the horizontal constraints
        view.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("|-[label]",
            options: NSLayoutFormatOptions.AlignAllBaseline,
            metrics: nil,
            views: viewsDictionary))
        return view;
        
    }
    
    override func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 30
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if let sections = fetchedResultsController.sections {
            let currentSection = sections[section] as NSFetchedResultsSectionInfo
            return currentSection.numberOfObjects
        }
        
        return 0
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell : UITableViewCell
        let feed = fetchedResultsController.objectAtIndexPath(indexPath) as! Feed
        
        
        if let logo = feed.logo where logo != "" {
            cell  = tableView.dequeueReusableCellWithIdentifier("Feed Image Cell", forIndexPath: indexPath) as UITableViewCell
            let imageView = cell.viewWithTag(5) as! UIImageView
            
            if let image = thumbnailCache[logo] {
                imageView.image = image
                // imageView.convertToCircleImage()
                
            } else {
                imageView.image = nil
                let priority = DISPATCH_QUEUE_PRIORITY_DEFAULT
                dispatch_async(dispatch_get_global_queue(priority, 0)) {
                   
                    let imageData = NSData(contentsOfURL: NSURL(string: logo)!)
                    
                    if let imageData = imageData {
                        if let image = UIImage(data: imageData) {
                            
                            self.thumbnailCache[logo] = image
                            dispatch_async(dispatch_get_main_queue()) {
                                imageView.image = image
                                // imageView.convertToCircleImage()
                            }
                        }
                    }
                }
            }


        } else {
            cell  = tableView.dequeueReusableCellWithIdentifier("Feed Cell", forIndexPath: indexPath) as UITableViewCell
            
        }
        
        
        let titleLabel = cell.viewWithTag(1) as! UILabel
        let dateLabel = cell.viewWithTag(2) as! UILabel
        let categoryLabel = cell.viewWithTag(3) as! UILabel
        let contentLabel = cell.viewWithTag(4) as! UILabel
        
        titleLabel.preferredMaxLayoutWidth = CGRectGetWidth(titleLabel.frame)
        dateLabel.preferredMaxLayoutWidth = CGRectGetWidth(dateLabel.frame)
        categoryLabel.preferredMaxLayoutWidth = CGRectGetWidth(categoryLabel.frame)
        contentLabel.preferredMaxLayoutWidth = CGRectGetWidth(contentLabel.frame)
        
        titleLabel.text = feed.title
        dateLabel.text = feed.postDateTime.timeAgo
        let categoriesArray = feed.category.map{ m -> String in
            let category = m as! FeedCategory
            return category.name
        }
        let categories = categoriesArray.joinWithSeparator(", ")
        
        categoryLabel.text = categories
        contentLabel.text = feed.content.stringByConvertingHTMLToPlainText()
        
        cell.layoutIfNeeded()
        
        return cell
    }
    
    func controllerWillChangeContent(controller: NSFetchedResultsController) {
        self.tableView.beginUpdates()
    }
    
    func controller(controller: NSFetchedResultsController, didChangeObject anObject: AnyObject, atIndexPath indexPath: NSIndexPath?, forChangeType type: NSFetchedResultsChangeType, newIndexPath: NSIndexPath?) {
        
        switch type{
        case NSFetchedResultsChangeType.Insert:
            self.tableView.insertRowsAtIndexPaths([newIndexPath!], withRowAnimation: UITableViewRowAnimation.Top)
            break
        case NSFetchedResultsChangeType.Delete:
            self.tableView.deleteRowsAtIndexPaths([indexPath!], withRowAnimation: UITableViewRowAnimation.Left)
            break
        case NSFetchedResultsChangeType.Update:
            self.tableView.cellForRowAtIndexPath(indexPath!)?.setNeedsLayout()
            break
        default:
            return
        }
    }
    
    func controller(controller: NSFetchedResultsController, didChangeSection sectionInfo: NSFetchedResultsSectionInfo, atIndex sectionIndex: Int, forChangeType type: NSFetchedResultsChangeType) {
        let indexSet = NSIndexSet(index: sectionIndex)
        switch type {
        case NSFetchedResultsChangeType.Insert:
            self.tableView.insertSections(indexSet, withRowAnimation: UITableViewRowAnimation.Fade)
        case NSFetchedResultsChangeType.Delete:
            self.tableView.deleteSections(indexSet, withRowAnimation: UITableViewRowAnimation.Fade)
        case NSFetchedResultsChangeType.Update:
            break
        case NSFetchedResultsChangeType.Move:
            break
        }
    }
    
    func controllerDidChangeContent(controller: NSFetchedResultsController) {
        self.tableView.endUpdates()
    }
}
