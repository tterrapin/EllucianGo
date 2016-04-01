//
//  EventsViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 8/10/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class EventsViewController : UITableViewController, UISearchResultsUpdating , NSFetchedResultsControllerDelegate, EventsFilterDelegate {

    @IBOutlet var filterButton: UIBarButtonItem!
    
    let searchController = UISearchController(searchResultsController: nil)
    var module : Module?
    var eventModule : EventModule?
    var hiddenCategories = NSMutableSet()
    var searchString : String?
    
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
    let timeFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateStyle = .NoStyle
        formatter.timeStyle = .ShortStyle
        return formatter
    }()
    let dateFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ"
        return formatter
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.title = self.module?.name
        tableView.estimatedRowHeight = 68
        tableView.rowHeight = UITableViewAutomaticDimension
        buildSearchBar()
        
        if UIScreen.mainScreen().traitCollection.userInterfaceIdiom == .Pad {
            self.splitViewController?.preferredDisplayMode = .AllVisible;
        } else {
            self.splitViewController?.preferredDisplayMode = .Automatic;
        }
        
        fetchEvents()
        reloadData()
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        sendView("Events List", forModuleNamed: self.module?.name)
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
    func fetchEvents() {
        let privateContext = NSManagedObjectContext(concurrencyType: .PrivateQueueConcurrencyType)
        privateContext.parentContext = self.module?.managedObjectContext
        privateContext.undoManager = nil
        
        let urlString = self.module?.propertyForKey("events")
        
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
                
                var previousEvents = [Event]()
                var existingEvents = [Event]()
                var newKeys = [String]()
                
                let request = NSFetchRequest(entityName: "Event")
                request.predicate = NSPredicate(format:"module.name = %@", self.module!.name)
                let oldObjects = try privateContext
                    .executeFetchRequest(request) as! [Event]
                for oldObject in oldObjects {
                    previousEvents.append(oldObject)
                }
                
                let moduleRequest = NSFetchRequest(entityName: "EventModule")
                moduleRequest.predicate = NSPredicate(format: "name = %@", self.module!.name)
                let eventModules = try privateContext.executeFetchRequest(moduleRequest)
                let eventModule : EventModule
                if eventModules.count > 0 {
                    eventModule = eventModules.last as! EventModule
                } else {
                    eventModule = NSEntityDescription.insertNewObjectForEntityForName("EventModule", inManagedObjectContext: privateContext) as! EventModule
                    eventModule.name = self.module?.name
                    
                    
                }
                
                let categoryRequest = NSFetchRequest(entityName: "EventCategory")
                categoryRequest.predicate = NSPredicate(format: "moduleName = %@", self.module!.name)
                let categoryArray = try privateContext.executeFetchRequest(categoryRequest)
                var categoryMap = [String: EventCategory]()
                for eventCategory in categoryArray as! [EventCategory]  {
                    categoryMap[eventCategory.name] = eventCategory
                }
                
                var orderedKeys = [String]()
                for (key, _) in json {
                    orderedKeys.append(key)
                }
                orderedKeys.sortInPlace(){ $0 < $1 }
                
                for key in orderedKeys {
                    let eventsForDate = json[key]
                    for jsonEvent in eventsForDate.array! {
                        let uid = jsonEvent["uid"].string
                        
                        let filteredArray = previousEvents.filter({
                            let event = $0 as Event
                            return event.uid == uid;
                        })
                        if filteredArray.count > 0 {
                            existingEvents.append(filteredArray[0])
                        } else {
                            let event = NSEntityDescription.insertNewObjectForEntityForName("Event", inManagedObjectContext: privateContext) as! Event
                            event.module = eventModule
                            eventModule.addEventsObject(event)
                            event.uid = uid
                            newKeys.append(uid!)
                            if let contact = jsonEvent["title"].string where contact != "" {
                                event.contact = contact
                            }
                            let start = self.dateFormatter.dateFromString(jsonEvent["start"].string!)
                            let end = self.dateFormatter.dateFromString(jsonEvent["end"].string!)
                            
                            event.dateLabel = self.datetimeOutputFormatter.stringFromDate(start!)
                            
                           if let description = jsonEvent["description"].string  {
                                event.description_ = description
                            }
                            event.endDate = end
                            if let location = jsonEvent["location"].string where location != "" {
                                event.location = location
                            }
                            event.startDate = start
                            if let summary = jsonEvent["summary"].string where summary != "" {
                                event.summary = summary
                            }
                            event.allDay = NSNumber(bool: jsonEvent["allDay"].bool!)
                            let categories = jsonEvent["categories"].arrayValue.map {
                                return $0["name"].string
                            }

                            for categoryLabel in categories {
                                var category = categoryMap[categoryLabel!]
                                if category == nil {
                                    category = NSEntityDescription.insertNewObjectForEntityForName("EventCategory", inManagedObjectContext: privateContext) as? EventCategory
                                    category!.name = categoryLabel
                                    category!.moduleName = self.module?.name
                                    categoryMap[categoryLabel!] = category
                                }
                                event.addCategoryObject(category)
                                category!.addEventObject(event)
                            }
                        }
                    }
                }
            
                try privateContext.save()
                for oldObject in previousEvents {
                    if !existingEvents.contains(oldObject) {
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
                    self.readEventModule()
                    self.filterButton.enabled = true
                    
                }
                
            } catch let error {
                print (error)
            }
        }
    }

    
    //MARK: segue
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "Show Event Filter" {
            self.sendEventWithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionList_Select, withLabel: "Select filter", withValue: nil, forModuleNamed: self.module?.name)
            let navigationController = segue.destinationViewController as! UINavigationController
            let detailController = navigationController.viewControllers[0] as! EventsFilterViewController
            detailController.eventModule = self.eventModule
            detailController.hiddenCategories = self.hiddenCategories
            detailController.module = self.module
            detailController.delegate = self
        } else if segue.identifier == "Show Detail" {
            let detailController = (segue.destinationViewController as! UINavigationController).topViewController as! EventsDetailViewController
            let event = fetchedResultsController.objectAtIndexPath(self.tableView.indexPathForSelectedRow!) as! Event
            detailController.event = event
            detailController.module = self.module
        }
    }
    
    //MARK reaad
    func readEventModule() {
        let request = NSFetchRequest(entityName: "EventModule")
        request.predicate = NSPredicate(format: "name = %@", self.module!.name)
        request.fetchLimit = 1
        do {
            let results = try self.module?.managedObjectContext?.executeFetchRequest(request) as! [EventModule]
            if results.count > 0 {
                self.eventModule = results[0]
                let hiddenCategories = self.eventModule?.hiddenCategories
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
    
    //MARK: EventFilterDelegate
    func reloadData() {
        self.readEventModule()
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
        request.entity = NSEntityDescription.entityForName("Event", inManagedObjectContext: managedObjectContext)
        
        
        var subPredicates = [NSPredicate]()
        
        subPredicates.append( NSPredicate(format: "module.name = %@", self.module!.name) )
        
        if let searchString = self.searchController.searchBar.text where searchString.characters.count > 0 {
            subPredicates.append( NSPredicate(format: "((summary CONTAINS[cd] %@) OR (description_ CONTAINS[cd] %@) OR (location CONTAINS[cd] %@))", searchString, searchString, searchString) )
        }
        
        if self.hiddenCategories.count > 0 {
            subPredicates.append( NSPredicate(format: "NONE category.name IN %@",  self.hiddenCategories) )
        }
        
        request.predicate = NSCompoundPredicate(andPredicateWithSubpredicates: subPredicates)
        
        request.sortDescriptors = [NSSortDescriptor(key: "dateLabel", ascending: true),NSSortDescriptor(key: "startDate", ascending: true),NSSortDescriptor(key: "summary", ascending: true)]
        
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
        
        let event = fetchedResultsController.objectAtIndexPath(indexPath) as! Event
    
        let cell : UITableViewCell  = tableView.dequeueReusableCellWithIdentifier("Event Cell", forIndexPath: indexPath) as UITableViewCell

        let summaryLabel = cell.viewWithTag(1) as! UILabel
        let dateLabel = cell.viewWithTag(2) as! UILabel
        let categoryLabel = cell.viewWithTag(3) as! UILabel
        let locationLabel = cell.viewWithTag(4) as! UILabel
        let descriptionLabel = cell.viewWithTag(5) as! UILabel

        summaryLabel.text = event.summary
        
        if event.allDay.boolValue == true {
            dateLabel.text = NSLocalizedString("All Day", comment: "label for all day event")
        } else {
            if let startDate = event.startDate, endDate = event.endDate {
                let formattedStart = self.timeFormatter.stringFromDate(startDate)
                let formattedEnd = self.timeFormatter.stringFromDate(endDate)
                dateLabel.text = String(format: NSLocalizedString("%@ - %@", comment: "event start - end"), formattedStart, formattedEnd)
            } else {
                dateLabel.text = self.timeFormatter.stringFromDate(event.startDate)
            }
        }
        let categoriesArray = event.category.map{ m -> String in
            let category = m as! EventCategory
            return category.name
        }
        let categories = categoriesArray.joinWithSeparator(", ")
        
        categoryLabel.text = categories
        locationLabel.text = event.location
        if let description = event.description_ {
            descriptionLabel.text = description.stringByConvertingHTMLToPlainText()
        } else {
            descriptionLabel.text = ""
        }
        
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
