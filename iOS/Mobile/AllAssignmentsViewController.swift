//
//  AllAssignmentsViewController.swift
//  Mobile
//
//  Created by Alan McEwan on 2/3/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation


class AllAssignmentsViewController : UIViewController, UIActionSheetDelegate, UITableViewDataSource, UITableViewDelegate, NSFetchedResultsControllerDelegate, UISplitViewControllerDelegate, UIAlertViewDelegate, EKEventEditViewDelegate
{
    @IBOutlet weak var dateSelector: UISegmentedControl!
    @IBOutlet weak var myTabBarItem: UITabBarItem!
    @IBOutlet var allAssignmentsTableView: UITableView!
    
    var detailSelectionDelegate: DetailSelectionDelegate!
    var allAssignmentController: NSFetchedResultsController!
    var myDatetimeOutputFormatter: NSDateFormatter?
    var myOverDueDatetimeOutputFormatter: NSDateFormatter?
    var myTabBarController: UITabBarController?
    var module: Module!
    var detailViewController: CourseAssignmentDetailViewController!
    var selectedAssignment:CourseAssignment?
    var selectedIndex:NSInteger = 0
    var overdueRed:UIColor?
    
    var showHeaders: Bool = true
    
    var myManagedObjectContext: NSManagedObjectContext!
    
    
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
        
        allAssignmentController = getAssignmentsFetchedResultsController(true)
        do {
            try allAssignmentController.performFetch()
        } catch let assignmentError as NSError {
            NSLog("fetch error: \(assignmentError.localizedDescription)")
        }
        allAssignmentsTableView.registerClass(NSClassFromString("UITableViewHeaderFooterView"), forHeaderFooterViewReuseIdentifier: "Header")
        
        self.view.backgroundColor = UIColor.primaryColor()
        let tabBarItem0 = myTabBarController?.tabBar.items?[0]
        if let tabBarItem0 = tabBarItem0 {
            tabBarItem0.selectedImage = UIImage(named: "ilp-assignments-selected")
        }
        
        if UIDevice.currentDevice().userInterfaceIdiom == .Pad {
            if selectedAssignment == nil {
                if allAssignmentController.fetchedObjects!.count > 0 {
                    let indexPath = NSIndexPath(forRow: 0, inSection: 0)
                    allAssignmentsTableView.selectRowAtIndexPath(indexPath, animated: true, scrollPosition:UITableViewScrollPosition.Top)
                    tableView(self.allAssignmentsTableView, didSelectRowAtIndexPath: indexPath);
                }
            }
        }
        self.sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction:kAnalyticsActionSearch, withLabel:"ILP Assignments List", withValue:nil, forModuleNamed:"ILP");
        overdueRed = UIColor(red: 193.0/255.0, green: 39.0/255.0, blue: 45.0/255.0, alpha: 1.0)
    }
    
    @IBAction func indexChanged(sender: UISegmentedControl) {
        
        switch dateSelector.selectedSegmentIndex
        {
        case 0:
            allAssignmentController = getAssignmentsFetchedResultsController(true)
            do {
                try allAssignmentController.performFetch()
            } catch let assignmentError as NSError {
                NSLog("Unresolved error: fetch error: \(assignmentError.localizedDescription)")
            }
        case 1:
            allAssignmentController = getAssignmentsFetchedResultsController(false)
            do {
                try allAssignmentController.performFetch()
            } catch let assignmentError as NSError {
                NSLog("Unresolved error: fetch error: \(assignmentError.localizedDescription)")
            }
        default:
            break
        }
        
        allAssignmentsTableView.reloadData()
        
        if UIDevice.currentDevice().userInterfaceIdiom == .Pad {
            if allAssignmentController.fetchedObjects!.count > 0 {
                let indexPath = NSIndexPath(forRow: 0, inSection: 0)
                allAssignmentsTableView.selectRowAtIndexPath(indexPath, animated: true, scrollPosition:UITableViewScrollPosition.Top)
                tableView(self.allAssignmentsTableView, didSelectRowAtIndexPath: indexPath);
            }
        }
        
    }
    
    /* called first
    begins update to `UITableView`
    ensures all updates are animated simultaneously */
    func controllerWillChangeContent(controller: NSFetchedResultsController) {
        allAssignmentsTableView.beginUpdates()
    }
    
    /* helper method to configure a `UITableViewCell`
    ask `NSFetchedResultsController` for the model */
    func configureCell(cell: UITableViewCell, atIndexPath indexPath: NSIndexPath) {
        
        var isOverdue = false
        var sectionName:String = ""
        
        if let sections = allAssignmentController!.sections {
            let name = sections[indexPath.section].name
            sectionName =  name
        }
        
        if sectionName == NSLocalizedString("OVERDUE", comment:"overdue assignment indicator for ilp module") {
            isOverdue = true
        }
        
        let assignment = allAssignmentController.objectAtIndexPath(indexPath) as! CourseAssignment
        let nameLabel = cell.viewWithTag(100) as! UILabel
        nameLabel.text = assignment.name
        
        let sectionNameLabel = cell.viewWithTag(101) as! UILabel
        sectionNameLabel.text = assignment.courseName + "-" + assignment.courseSectionNumber
        
        let dueDateLabel = cell.viewWithTag(102) as! UILabel
        
        if let assignmentDate = assignment.dueDate {
            if isOverdue {
                dueDateLabel.text = self.overDueDatetimeOutputFormatter()!.stringFromDate(assignmentDate)
            } else {
                dueDateLabel.text = self.datetimeOutputFormatter()!.stringFromDate(assignmentDate)
            }
        } else {
            dueDateLabel.text = ""
        }
        
        if isOverdue {
            nameLabel.textColor = overdueRed
        } else {
            nameLabel.textColor = UIColor.blackColor()
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
                allAssignmentsTableView.insertRowsAtIndexPaths([newIndexPath as NSIndexPath!], withRowAnimation: .Fade)
            case .Update:
                let cell = self.allAssignmentsTableView.cellForRowAtIndexPath(indexPath as NSIndexPath!)
                configureCell(cell!, atIndexPath: indexPath as NSIndexPath!)
                allAssignmentsTableView.reloadRowsAtIndexPaths([indexPath as NSIndexPath!], withRowAnimation: .Fade)
            case .Delete:
                allAssignmentsTableView.deleteRowsAtIndexPaths([indexPath as NSIndexPath!], withRowAnimation: .Fade)
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
            allAssignmentsTableView.insertSections(NSIndexSet(index: sectionIndex),
                withRowAnimation: UITableViewRowAnimation.Fade)
            
        case .Delete:
            allAssignmentsTableView.deleteSections(NSIndexSet(index: sectionIndex),
                withRowAnimation: UITableViewRowAnimation.Fade)
            
        default:
            break
        }
    }
    
    /* called last
    tells `UITableView` updates are complete */
    func controllerDidChangeContent(controller: NSFetchedResultsController) {
        allAssignmentsTableView.endUpdates()
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        let cell = tableView.dequeueReusableCellWithIdentifier("Daily Assignment Cell", forIndexPath: indexPath) as UITableViewCell
        configureCell(cell, atIndexPath:indexPath)
        return cell
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        
        if UIDevice.currentDevice().userInterfaceIdiom == .Pad {
            let assignment = allAssignmentController.objectAtIndexPath(indexPath) as! CourseAssignment
            selectedAssignment = assignment
            
            detailViewController.courseName = assignment.courseName
            detailViewController.courseSectionNumber = assignment.courseSectionNumber
            detailViewController.itemTitle = assignment.name
            detailViewController.itemContent = assignment.assignmentDescription
            detailViewController.itemLink = assignment.url.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet())
            if let assignmentDate = assignment.dueDate {
                detailViewController.itemPostDateTime = assignmentDate
            }
            else {
                detailViewController.itemPostDateTime = nil
            }
            self.detailSelectionDelegate = detailViewController
            self.detailSelectionDelegate.selectedDetail(assignment, withIndex: indexPath, withModule: self.module!, withController: self)
            
        } else if UIDevice.currentDevice().userInterfaceIdiom == .Phone {
            self.performSegueWithIdentifier("Show ILP Assignment Detail", sender:tableView)
        }
        
    }
    func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return 60.0
    }
    
    func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat{
        
        let count = allAssignmentController.sections?.count
        
        if count == 0 || !showHeaders {
            return 0.0
        } else {
            //return 18.0
            return 30.0
        }
    }
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        
        let numberOfSections = allAssignmentController.sections?.count
        return numberOfSections!
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        let numberOfRowsInSection = allAssignmentController.sections?[section].numberOfObjects
        return numberOfRowsInSection!
    }
    
    func tableView(tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        
        let sections = allAssignmentController.sections
        let dateLabel:String? = sections?[section].name
        var header:UITableViewCell
        
        if dateLabel == NSLocalizedString("OVERDUE", comment:"overdue assignment indicator for ilp module") {
            header = tableView.dequeueReusableCellWithIdentifier("OverdueSectionHeader")!
        } else {
            header = tableView.dequeueReusableCellWithIdentifier("SectionHeader")!
        }
        
        header.contentView.backgroundColor = UIColor.accentColor()
        let labelView = header.viewWithTag(101) as! UILabel
        labelView.text = dateLabel
        labelView.sizeToFit()
        return header
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?)
    {
        if segue.identifier == "Show ILP Assignment Detail" {
            let indexPath: NSIndexPath! = allAssignmentsTableView.indexPathForSelectedRow
            let assignment = allAssignmentController.objectAtIndexPath(indexPath) as! CourseAssignment
            let detailController = segue.destinationViewController as! CourseAssignmentDetailViewController
            detailController.courseSectionNumber = assignment.courseSectionNumber
            detailController.courseName = assignment.courseName
            detailController.itemTitle = assignment.name
            
            detailController.itemContent = assignment.assignmentDescription
            
            if let url = assignment.url {
                detailController.itemLink = url.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet())
            } else {
                detailController.itemLink = nil
            }
            
            if let assignmentDate = assignment.dueDate {
                detailController.itemPostDateTime = assignmentDate
            }
            else {
                detailController.itemPostDateTime = nil
            }
            allAssignmentsTableView.deselectRowAtIndexPath(indexPath, animated: true)
        } else if segue.identifier == "Edit Reminder"{
            let detailController = segue.destinationViewController.childViewControllers[0] as! EditReminderViewController
            
            detailController.reminderTitle = reminderAssignment!.name
            
            if let date = reminderAssignment!.dueDate {
                detailController.reminderDate = date
                let formattedDate = NSDateFormatter.localizedStringFromDate(date, dateStyle: .ShortStyle, timeStyle: .ShortStyle)
                let localizedDue = NSString.localizedStringWithFormat(NSLocalizedString("Due: %@", comment: "due date label with date"), formattedDate)
                detailController.reminderNotes = "\(reminderAssignment!.courseName)-\(reminderAssignment!.courseSectionNumber)\n\(localizedDue)\n\(reminderAssignment!.assignmentDescription)"
            } else {
                detailController.reminderNotes = "\(reminderAssignment!.courseName)-\(reminderAssignment!.courseSectionNumber)\n\(reminderAssignment!.assignmentDescription)"
            }
            
            
        }
    }
    
    func assignmentFetchRequest(showOnlyItemsWithDates:Bool) -> NSFetchRequest {
        
        let fetchRequest = NSFetchRequest(entityName:"CourseAssignment")
        var fetchPredicate:NSPredicate!
        
        if (showOnlyItemsWithDates) {
            fetchPredicate = NSPredicate(format: "dueDate != nil")
        } else {
            fetchPredicate = NSPredicate(format: "dueDate == nil")
        }
        
        let sortDescriptor = NSSortDescriptor(key:"dueDate", ascending:true)
        fetchRequest.sortDescriptors = [sortDescriptor]
        fetchRequest.predicate = fetchPredicate
        return fetchRequest
    }
    
    func getAssignmentsFetchedResultsController(showOnlyItemsWithDates: Bool) -> NSFetchedResultsController {
        
        showHeaders = showOnlyItemsWithDates
        
        let importContext = NSManagedObjectContext(concurrencyType: .MainQueueConcurrencyType)
        
        importContext.parentContext = self.myManagedObjectContext
        
        let fetchRequest = assignmentFetchRequest(showOnlyItemsWithDates)
        let entity:NSEntityDescription? = NSEntityDescription.entityForName("CourseAssignment", inManagedObjectContext:importContext)
        fetchRequest.entity = entity;
        
        var theFetchedResultsController:NSFetchedResultsController!
        
        if showOnlyItemsWithDates {
            theFetchedResultsController = NSFetchedResultsController(fetchRequest: fetchRequest, managedObjectContext:importContext, sectionNameKeyPath:"displayDateSectionHeader", cacheName:nil)
        } else {
            theFetchedResultsController = NSFetchedResultsController(fetchRequest: fetchRequest, managedObjectContext:importContext, sectionNameKeyPath:nil, cacheName:nil)
        }
        
        return theFetchedResultsController
        
    }
    
    func datetimeOutputFormatter() ->NSDateFormatter? {
        
        if (myDatetimeOutputFormatter == nil) {
            myDatetimeOutputFormatter = NSDateFormatter()
            myDatetimeOutputFormatter!.dateStyle = .NoStyle
            myDatetimeOutputFormatter!.timeStyle = .ShortStyle
        }
        return myDatetimeOutputFormatter
    }
    
    func overDueDatetimeOutputFormatter() ->NSDateFormatter? {
        
        if (myOverDueDatetimeOutputFormatter == nil) {
            myOverDueDatetimeOutputFormatter = NSDateFormatter()
            myOverDueDatetimeOutputFormatter!.dateStyle = .ShortStyle
            myOverDueDatetimeOutputFormatter!.timeStyle = .ShortStyle
        }
        return myOverDueDatetimeOutputFormatter
    }
    
    //    func setSelectedAssignment(item:CourseAssignment?)
    //    {
    //        selectedAssignment = item
    //    }
    
    func findSelectedItem() {
        
        if let myTargetItem = selectedAssignment {
            var indexPath = NSIndexPath(forRow: 0, inSection: 0)
            
            for iter in allAssignmentController.fetchedObjects!
            {
                let temp: CourseAssignment = iter as! CourseAssignment
                if myTargetItem.url != nil && temp.url == myTargetItem.url {
                    indexPath = allAssignmentController.indexPathForObject(temp)!
                }
            }
            allAssignmentsTableView.selectRowAtIndexPath(indexPath, animated: true, scrollPosition:UITableViewScrollPosition.Top)
            tableView(self.allAssignmentsTableView, didSelectRowAtIndexPath: indexPath);
        }
    }
    
    var reminderAssignment : CourseAssignment? = nil
    
    @IBAction func addReminderTapped(sender: AnyObject) {
        let buttonPosition = sender.convertPoint(CGPointZero, toView: self.allAssignmentsTableView);
        let indexPath = self.allAssignmentsTableView.indexPathForRowAtPoint(buttonPosition);
        if let indexPath = indexPath {
            reminderAssignment = allAssignmentController.objectAtIndexPath(indexPath) as? CourseAssignment
            
            let reminderType = NSUserDefaults.standardUserDefaults().stringForKey("settings-assignments-reminder")
            if reminderType == "Calendar" {
                addToCalendar()
            } else if reminderType == "Reminders" {
                addToReminders()
            } else {
                let alert = UIAlertView(title: NSLocalizedString("Reminder Type", comment:"Reminder setting title"), message: NSLocalizedString("What application would you list to use for reminders?", comment:"Reminder setting message"), delegate:self, cancelButtonTitle:nil,  otherButtonTitles:  NSLocalizedString("Calendar", comment:"Calendar app name"), NSLocalizedString("Reminders", comment:"Reminders app name"))
                alert.tag = 1
                alert.show()
                
                
            }
        }
    }
    
    func alertView(alertView: UIAlertView, didDismissWithButtonIndex buttonIndex: Int) {
        if alertView.tag == 1 {
            if buttonIndex == 1 {
                NSUserDefaults.standardUserDefaults().setObject("Reminders", forKey: "settings-assignments-reminder")
                addToReminders()
            } else  if buttonIndex == 0 {
                NSUserDefaults.standardUserDefaults().setObject("Calendar", forKey: "settings-assignments-reminder")
                addToCalendar()
            }
        }
    }
    
    func addToReminders() {
        if let _ = reminderAssignment {
            let eventStore : EKEventStore = EKEventStore()
            eventStore.requestAccessToEntityType(.Reminder) {
                granted, error in
                if (granted) && (error == nil) {
                    self.performSegueWithIdentifier("Edit Reminder", sender: self)
                } else {
                    self.showPermissionNotGrantedAlert()
                }
            }
        }
    }
    
    func addToCalendar() {
        if let assignment = reminderAssignment {
            let eventStore : EKEventStore = EKEventStore()
            eventStore.requestAccessToEntityType(.Event, completion: {
                granted, error in
                if (granted) && (error == nil) {
                    
                    let event:EKEvent = EKEvent(eventStore: eventStore)
                    event.title = assignment.name
                    if let dueDate = assignment.dueDate {
                        event.startDate = dueDate
                        event.endDate = dueDate
                    }
                    event.notes = assignment.assignmentDescription
                    event.calendar = eventStore.defaultCalendarForNewEvents
                    event.location = assignment.courseName + "-" + assignment.courseSectionNumber
                    
                    let evc = EKEventEditViewController()
                    evc.event = event
                    evc.eventStore = eventStore
                    evc.editViewDelegate = self
                    self.reminderAssignment = nil
                    self.presentViewController(evc, animated: true, completion: nil)
                } else {
                    self.showPermissionNotGrantedAlert()
                }
            })
        }
    }
    
    func eventEditViewController(controller: EKEventEditViewController, didCompleteWithAction action: EKEventEditViewAction) {
        self.dismissViewControllerAnimated(true, completion:nil)
    }
    
    private func showPermissionNotGrantedAlert() {
        
        let alertController = UIAlertController(title: NSLocalizedString("Permission not granted", comment: "Permission not granted title"), message: NSLocalizedString("You must give permission in Settings to allow access", comment: "Permission not granted message"), preferredStyle: .Alert)
        
        
        let settingsAction = UIAlertAction(title: NSLocalizedString("Settings", comment: "Settings application name"), style: .Default) { value in
            let settingsUrl = NSURL(string: UIApplicationOpenSettingsURLString)
            if let url = settingsUrl {
                UIApplication.sharedApplication().openURL(url)
            }
        }
        let cancelAction = UIAlertAction(title: NSLocalizedString("Cancel", comment: "Cancel"), style: .Default, handler: nil)
        alertController.addAction(settingsAction)
        alertController.addAction(cancelAction)
        dispatch_async(dispatch_get_main_queue()) {
            () -> Void in
            self.presentViewController(alertController, animated: true, completion: nil)
            
        }
    }
}