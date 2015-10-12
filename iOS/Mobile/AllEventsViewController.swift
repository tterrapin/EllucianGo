//
//  AllEventsViewController.swift
//  Mobile
//
//  Created by Alan McEwan on 2/3/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation


class AllEventsViewController : UIViewController, UIActionSheetDelegate, UITableViewDataSource, UITableViewDelegate, NSFetchedResultsControllerDelegate, UISplitViewControllerDelegate
{
    
    @IBOutlet var allEventsTableView: UITableView!
    @IBOutlet weak var myTabBarItem: UITabBarItem!
    
    var detailSelectionDelegate: DetailSelectionDelegate!
    var allEventController: NSFetchedResultsController!
    var myDatetimeOutputFormatter: NSDateFormatter?
    var myManagedObjectContext: NSManagedObjectContext!
    var myTabBarController: UITabBarController!
    var showHeaders: Bool = true
    var module: Module!
    var detailViewController: CourseEventsDetailViewController!
    var selectedEvent:CourseEvent?
    
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        self.extendedLayoutIncludesOpaqueBars = true
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        if UIDevice.currentDevice().userInterfaceIdiom == .Pad {
            findSelectedItem()
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        allEventController = getEventsFetchedResultsController(true)
        do {
            try allEventController.performFetch()
        } catch let eventError as NSError {
             NSLog("Unresolved error: fetch error: \(eventError.localizedDescription)")
        }
    
    allEventsTableView.registerClass(NSClassFromString("UITableViewHeaderFooterView"), forHeaderFooterViewReuseIdentifier: "Header")
        self.view.backgroundColor = UIColor.primaryColor()
        
        let tabBarItem1 = myTabBarController?.tabBar.items?[1]
        if let tabBarItem1 = tabBarItem1 {
            tabBarItem1.selectedImage = UIImage(named: "ilp-events-selected")
        }
        
        if UIDevice.currentDevice().userInterfaceIdiom == .Pad {
            if selectedEvent == nil {
                if allEventController.fetchedObjects!.count > 0 {
                    let indexPath = NSIndexPath(forRow: 0, inSection: 0)
                    allEventsTableView.selectRowAtIndexPath(indexPath, animated: true, scrollPosition:UITableViewScrollPosition.Top)
                    tableView(allEventsTableView, didSelectRowAtIndexPath: indexPath);
                }
            }
        }
        
        self.sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction:kAnalyticsActionSearch, withLabel:"ILP Events List", withValue:nil, forModuleNamed:"ILP");
    }
    
    /* called first
    begins update to `UITableView`
    ensures all updates are animated simultaneously */
    func controllerWillChangeContent(controller: NSFetchedResultsController) {
        allEventsTableView.beginUpdates()
    }
    
    /* helper method to configure a `UITableViewCell`
    ask `NSFetchedResultsController` for the model */
    func configureCell(cell: UITableViewCell, atIndexPath indexPath: NSIndexPath) {
        let event = allEventController.objectAtIndexPath(indexPath) as! CourseEvent
        
        let nameLabel = cell.viewWithTag(100) as! UILabel
        nameLabel.text = event.title
        
        let courseNameLabel = cell.viewWithTag(101) as! UILabel
        courseNameLabel.text = event.courseName + "-" + event.courseSectionNumber
        
        let startDateLabel = cell.viewWithTag(102) as! UILabel
        
        if let date = event.startDate {
            startDateLabel.text = self.datetimeOutputFormatter()!.stringFromDate(date)
        } else {
            startDateLabel.text = ""
        }
    }
    
    /* called:
    - when a new model is created
    - when an existing model is updated
    - when an existing model is deleted */
    func controller(controller: NSFetchedResultsController,
        didChangeObject object: AnyObject,
        atIndexPath indexPath: NSIndexPath?,
        forChangeType type: NSFetchedResultsChangeType,
        newIndexPath: NSIndexPath?)  {
            
            switch type {
            case .Insert:
                allEventsTableView.insertRowsAtIndexPaths([newIndexPath as NSIndexPath!], withRowAnimation: .Fade)
            case .Update:
                let cell = self.allEventsTableView.cellForRowAtIndexPath(indexPath as NSIndexPath!)
                configureCell(cell!, atIndexPath: indexPath as NSIndexPath!)
                allEventsTableView.reloadRowsAtIndexPaths([indexPath as NSIndexPath!], withRowAnimation: .Fade)
            case .Delete:
                allEventsTableView.deleteRowsAtIndexPaths([indexPath as NSIndexPath!], withRowAnimation: .Fade)
            default:
                break
            }
    }
    
    func controller(controller: NSFetchedResultsController,
        didChangeSection sectionInfo: NSFetchedResultsSectionInfo,
        atIndex sectionIndex: Int,
        forChangeType type: NSFetchedResultsChangeType)
    {
        switch(type) {
            
        case .Insert:
            allEventsTableView.insertSections(NSIndexSet(index: sectionIndex),
                withRowAnimation: UITableViewRowAnimation.Fade)
        case .Delete:
            allEventsTableView.deleteSections(NSIndexSet(index: sectionIndex),
                withRowAnimation: UITableViewRowAnimation.Fade)
            
        default:
            break
        }
    }
    
    /* called last
    tells `UITableView` updates are complete */
    func controllerDidChangeContent(controller: NSFetchedResultsController) {
        allEventsTableView.endUpdates()
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        
        let event = allEventController.objectAtIndexPath(indexPath) as! CourseEvent
        selectedEvent = event
        
        if UIDevice.currentDevice().userInterfaceIdiom == .Pad {
            
            detailViewController.courseName = event.courseName
            detailViewController.courseSectionNumber = event.courseSectionNumber
            detailViewController.eventTitle = event.title
            detailViewController.eventDescription = event.eventDescription
            detailViewController.location = event.location
            if let eventStartDate = event.startDate {
                detailViewController.startDate = eventStartDate
            }
            else {
                detailViewController.startDate = nil
            }
            if let eventEndDate = event.endDate {
                detailViewController.endDate = eventEndDate
            }
            else {
                detailViewController.endDate = nil
            }
            self.detailSelectionDelegate = detailViewController
            self.detailSelectionDelegate.selectedDetail(event, withIndex: indexPath, withModule: self.module!, withController: self)
            
        } else if UIDevice.currentDevice().userInterfaceIdiom == .Phone {
            self.performSegueWithIdentifier("Show ILP Event Detail", sender:tableView)
        }
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        let cell = tableView.dequeueReusableCellWithIdentifier("Daily Event Cell", forIndexPath: indexPath) as UITableViewCell
        configureCell(cell, atIndexPath:indexPath)
        return cell

    }
    
    func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat
    {
        return 60.0
    }
    
    func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat
    {
        let count = allEventController.sections?.count
        
        if count == 0 || !showHeaders {
            return 0.0
        } else {
            return 18.0
        }
    }
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        let numberOfSections = allEventController.sections?.count
        return numberOfSections!
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        let numberOfRowsInSection = allEventController.sections?[section].numberOfObjects
        return numberOfRowsInSection!
    }
    
    func tableView(tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        
        if self.showHeaders {
            let h = allEventsTableView.dequeueReusableHeaderFooterViewWithIdentifier("Header")
            if let h = h {
                for subView in h.contentView.subviews
                {
                    if (subView.tag == 1 || subView.tag == 2)
                    {
                        subView.removeFromSuperview()
                    }
                }
                
                let sections = allEventController.sections
                
                let dateLabel:String? = sections?[section].name
                
                if h.backgroundColor != UIColor.accentColor() {
                    h.contentView.backgroundColor = UIColor.accentColor()
                    let headerLabel = UILabel()
                    headerLabel.tag = 1
                    headerLabel.text = dateLabel
                    headerLabel.backgroundColor = UIColor.clearColor()
                    headerLabel.textColor = UIColor.subheaderTextColor()
                    headerLabel.font = UIFont.boldSystemFontOfSize(16)
                    headerLabel.minimumScaleFactor = 0.5
                    h.contentView.addSubview(headerLabel)
                    headerLabel.translatesAutoresizingMaskIntoConstraints = false
                    h.contentView.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("H:|-10-[headerLabel]", options: NSLayoutFormatOptions(rawValue: 0), metrics: nil, views: ["headerLabel":headerLabel]))
                    h.contentView.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("V:|[headerLabel]|", options: NSLayoutFormatOptions(rawValue: 0), metrics:nil, views: ["headerLabel":headerLabel]))
                    
                }
            }
            return h
            
        } else {
            return nil
        }
    }

    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?)
    {
        let indexPath: NSIndexPath! = allEventsTableView.indexPathForSelectedRow
        let event = allEventController.objectAtIndexPath(indexPath) as! CourseEvent
        let detailController = segue.destinationViewController as! CourseEventsDetailViewController
        detailController.eventTitle = event.title
        detailController.eventDescription = event.eventDescription
        detailController.location = event.location
        detailController.courseName = event.courseName
        detailController.courseSectionNumber = event.courseSectionNumber
        if let eventStartDate = event.startDate {
            detailController.startDate = eventStartDate
        }
        else {
            detailController.startDate = nil
        }
        if let eventEndDate = event.endDate {
            detailController.endDate = eventEndDate
        }
        else {
            detailController.endDate = nil
        }
        
        allEventsTableView.deselectRowAtIndexPath(indexPath, animated: true)
    }
    
    func eventFetchRequest() -> NSFetchRequest {
        
        let cal = NSCalendar.currentCalendar()
        let timezone = NSTimeZone.systemTimeZone()
        cal.timeZone = timezone
        let beginComps = cal.components([.Year, .Month, .Day, .Hour, .Minute, .Second], fromDate: NSDate())
        beginComps.hour = 0
        beginComps.minute = 0
        beginComps.second = 0
        let beginOfToday = cal.dateFromComponents(beginComps)!
        
        let fetchRequest = NSFetchRequest(entityName:"CourseEvent")
        
        let predicate = NSPredicate(format: "(endDate >= %@)", beginOfToday)
        
        let sortDescriptor = NSSortDescriptor(key:"startDate", ascending:true)
        fetchRequest.sortDescriptors = [sortDescriptor]
        fetchRequest.predicate = predicate
        return fetchRequest
    }
    
    func getEventsFetchedResultsController(showOnlyItemsWithDates:Bool) -> NSFetchedResultsController {
        
        showHeaders = showOnlyItemsWithDates
        let importContext = NSManagedObjectContext(concurrencyType: .MainQueueConcurrencyType)
        
        importContext.parentContext = self.myManagedObjectContext
        
        let fetchRequest = eventFetchRequest()
        let entity:NSEntityDescription? = NSEntityDescription.entityForName("CourseEvent", inManagedObjectContext:importContext)
        fetchRequest.entity = entity;
        
        var theFetchedResultsController:NSFetchedResultsController!
        
        if showOnlyItemsWithDates {
            theFetchedResultsController = NSFetchedResultsController(fetchRequest: fetchRequest, managedObjectContext:importContext, sectionNameKeyPath:"displayDateSectionHeader", cacheName:nil)
        } else {
            theFetchedResultsController = NSFetchedResultsController(fetchRequest: fetchRequest, managedObjectContext:importContext, sectionNameKeyPath:nil, cacheName:nil)
        }

        return theFetchedResultsController!
    }

    
    func datetimeOutputFormatter() ->NSDateFormatter? {
        
        if (myDatetimeOutputFormatter == nil) {
            myDatetimeOutputFormatter = NSDateFormatter()
            myDatetimeOutputFormatter!.timeStyle = .ShortStyle
            myDatetimeOutputFormatter!.dateStyle = .ShortStyle
        }
        return myDatetimeOutputFormatter
    }
    
    func splitViewController(svc: UISplitViewController,
        shouldHideViewController vc: UIViewController,
        inOrientation orientation: UIInterfaceOrientation) -> Bool {
            return false;
    }
    
    func setSelectedItem(item:CourseEvent?)
    {
        selectedEvent = item
    }
    
    func findSelectedItem() {
        
        if selectedEvent != nil {
            var indexPath = NSIndexPath(forRow: 0, inSection: 0)
            let myTargetItem = selectedEvent!
            if ( myTargetItem.managedObjectContext != nil)
            {
                for iter in allEventController.fetchedObjects!
                {
                    let tempEvent: CourseEvent = iter as! CourseEvent
                    if tempEvent.title == myTargetItem.title && tempEvent.startDate == myTargetItem.startDate && tempEvent.endDate==myTargetItem.endDate && tempEvent.eventDescription == myTargetItem.eventDescription {
                        indexPath = allEventController.indexPathForObject(tempEvent)!
                    }
                }
                allEventsTableView.selectRowAtIndexPath(indexPath, animated: true, scrollPosition:UITableViewScrollPosition.Top)
                tableView(self.allEventsTableView, didSelectRowAtIndexPath: indexPath);
            }
        }
    }
}