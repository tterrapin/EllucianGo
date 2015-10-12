     //
//  SwiftDailyViewController.swift
//  Mobile
//
//  Created by Alan McEwan on 1/9/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation
import UIKit



class ILPViewController : UIViewController, UIActionSheetDelegate, UIGestureRecognizerDelegate, UISplitViewControllerDelegate
{

    var requestedAssignmentId: String? = nil

    @IBOutlet weak var scrollView: UIScrollView!
    
    @IBOutlet weak var assignmentsCardTitle: UILabel!
    @IBOutlet weak var showAllAssignmentsView: UIView!
    @IBOutlet weak var assignmentCardView: UIView!
    @IBOutlet var assignmentTableView: UITableView!
    
    @IBOutlet weak var eventsCardTitle: UILabel!
    @IBOutlet weak var showAllEventsView: UIView!
    @IBOutlet weak var eventCardView: UIView!
    @IBOutlet var eventTableView: UITableView!
    
    @IBOutlet weak var announcementsCardTitle: UILabel!
    @IBOutlet weak var showAllAnnouncementsView: UIView!
    @IBOutlet weak var announcementCardView: UIView!
    @IBOutlet var announcementTableView: UITableView!
    
    @IBOutlet weak var assignmentTableHeightConstraint: NSLayoutConstraint!
    @IBOutlet weak var assignmentTableWidthConstraint: NSLayoutConstraint!
    @IBOutlet weak var eventTableHeightConstraint: NSLayoutConstraint!
    @IBOutlet weak var eventTableWidthConstraint: NSLayoutConstraint!
    @IBOutlet weak var announcementTableHeightConstraint: NSLayoutConstraint!
    @IBOutlet weak var announcementTableWidthConstraint: NSLayoutConstraint!
    
    var cardInsetConstant:CGFloat = 0
    //Landscape Constraints
    var landscapeConstraintArray = [NSLayoutConstraint]()
    //Portrait Constraints
    var portraitConstraintArray = [NSLayoutConstraint]()
    
    var assignmentsFetchedResultController: NSFetchedResultsController?
    var assignmentsTableViewDelegate: AssignmentTableViewDelegate?
    
    var eventsFetchedResultsController: NSFetchedResultsController?
    var eventsTableViewDelegate: EventTableViewDelegate?
    
    var announcementsFetchedResultsController : NSFetchedResultsController?
    var announcementsTableViewDelegate: AnnouncementTableViewDelegate?
    
    var module: Module!
    
    var myDatetimeFormatter: NSDateFormatter?
    var darkGray:UIColor = UIColor(red: 152.0/255.0, green: 152.0/255.0, blue: 152.0/255.0, alpha: 1.0)
    
    override func viewWillAppear(animated: Bool) {
        resizeAfterOrientationChange()
        //reset the delegates on the fetched results controller
        assignmentsFetchedResultController!.delegate = assignmentsTableViewDelegate
        eventsFetchedResultsController!.delegate = eventsTableViewDelegate
        announcementsFetchedResultsController!.delegate = announcementsTableViewDelegate
    }
    
    override func viewDidLoad() {

        super.viewDidLoad()
        showAllAnnouncementsView.userInteractionEnabled = true
        
        navigationController?.navigationBar.translucent = false;
        self.title = self.module!.name;

        assignmentsFetchedResultController = getAssignmentsFetchedResultsController()
        assignmentsTableViewDelegate = AssignmentTableViewDelegate(tableView: assignmentTableView, resultsController: assignmentsFetchedResultController!, heightConstraint: assignmentTableHeightConstraint, widthConstraint: assignmentTableWidthConstraint, parentModule:self.module, viewController:self)
        
        do {
            try assignmentsFetchedResultController!.performFetch()
            
            if requestedAssignmentId != nil {
                self.showDetailForRequestedAssignment()
            }
        } catch let assignmentError as NSError {
            NSLog("Unresolved error fetching assignments: fetch error: \(assignmentError.localizedDescription)")
        }
        
        announcementsFetchedResultsController = getAnnouncementsFetchedResultsController()
        announcementsTableViewDelegate = AnnouncementTableViewDelegate(tableView: announcementTableView, controller: announcementsFetchedResultsController!, heightConstraint:announcementTableHeightConstraint, widthConstraint: announcementTableWidthConstraint, parentModule:module)
        do {
            try announcementsFetchedResultsController!.performFetch()
        } catch let announcementError as NSError {
            NSLog("Unresolved error fetching announcements: fetch error: \(announcementError.localizedDescription)")
        }
        
        eventsFetchedResultsController = getEventsFetchedResultsController()
        eventsTableViewDelegate = EventTableViewDelegate(tableView: eventTableView, controller: eventsFetchedResultsController!, heightConstraint: eventTableHeightConstraint, widthConstraint: eventTableWidthConstraint, parentModule:module)
        do {
            try eventsFetchedResultsController!.performFetch()
        } catch let eventError as NSError {
            NSLog("Unresolved error fetching events: fetch error: \(eventError.localizedDescription)")
        }

        
        let currentUser = CurrentUser.sharedInstance()
        
        if currentUser.isLoggedIn {
            fetchAnnouncements(self)
            fetchEvents(self)
            fetchAssignments(self)
        }
        
        let screenWidth = AppearanceChanger.currentScreenBoundsDependOnOrientation().width
        if UIDevice.currentDevice().userInterfaceIdiom == .Pad
        {
            cardInsetConstant = 25.0
            initIPadPortraitConstraints()
            initIPadLandscapeConstraints()

            switch UIDevice.currentDevice().orientation{
            case .Portrait:
                scrollView.removeConstraints(landscapeConstraintArray)
                scrollView.addConstraints(portraitConstraintArray)
                assignmentTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
                announcementTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
                eventTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
            case .PortraitUpsideDown:
                scrollView.removeConstraints(landscapeConstraintArray)
                scrollView.addConstraints(portraitConstraintArray)
                assignmentTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
                announcementTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
                eventTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
            case .LandscapeLeft:
                scrollView.removeConstraints(portraitConstraintArray)
                scrollView.addConstraints(landscapeConstraintArray)
                assignmentTableWidthConstraint.constant = (screenWidth - 100)/3
                announcementTableWidthConstraint.constant = (screenWidth - 100)/3
                eventTableWidthConstraint.constant = (screenWidth - 100)/3
            case .LandscapeRight:
                scrollView.removeConstraints(portraitConstraintArray)
                scrollView.addConstraints(landscapeConstraintArray)
                assignmentTableWidthConstraint.constant = (screenWidth - 100)/3
                announcementTableWidthConstraint.constant = (screenWidth - 100)/3
                eventTableWidthConstraint.constant = (screenWidth - 100)/3
            default:
                scrollView.removeConstraints(landscapeConstraintArray)
                scrollView.addConstraints(portraitConstraintArray)
                assignmentTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
                announcementTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
                eventTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
            }
        } else if UIDevice.currentDevice().userInterfaceIdiom == .Phone
        {
            cardInsetConstant = 15.0
            assignmentTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
            announcementTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
            eventTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
        }
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector:"fetchAssignments:", name:kLoginExecutorSuccess, object:nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector:"fetchEvents:", name: kLoginExecutorSuccess, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector:"fetchAnnouncements:", name: kLoginExecutorSuccess, object: nil)
        
        
        var totalRowHeight:CGFloat = 0.0
        
        for section in assignmentsFetchedResultController!.sections! {
            if section.name  == NSLocalizedString("DUE TODAY", comment:"due today assignment indicator for ilp module") {
                totalRowHeight +=  CGFloat(section.numberOfObjects) * 40.0
            } else if section.name == NSLocalizedString("OVERDUE", comment:"overdue assignment indicator for ilp module") {
                totalRowHeight +=  CGFloat(section.numberOfObjects) * 50.0
            }
        }
        assignmentTableHeightConstraint.constant = totalRowHeight + (CGFloat(assignmentsFetchedResultController!.sections!.count) * 30.0) + 50.0
        
        eventTableHeightConstraint.constant = (CGFloat(eventsFetchedResultsController!.fetchedObjects!.count) * 50.0) + 50.0
        announcementTableHeightConstraint.constant = (CGFloat(announcementsFetchedResultsController!.fetchedObjects!.count) * 40.0) + 50.0
        
        assignmentTableView.registerClass(NSClassFromString("UITableViewHeaderFooterView"), forHeaderFooterViewReuseIdentifier: "Header")
        eventTableView.registerClass(NSClassFromString("UITableViewHeaderFooterView"), forHeaderFooterViewReuseIdentifier: "Header")
        announcementTableView.registerClass(NSClassFromString("UITableViewHeaderFooterView"), forHeaderFooterViewReuseIdentifier: "Header")
        
        assignmentsCardTitle.text = NSLocalizedString("Assignments", comment:"ILP View: Assignments Card Title")
        announcementsCardTitle.text = NSLocalizedString("Announcements", comment:"ILP View: Announcements Card Title")
        eventsCardTitle.text = NSLocalizedString("Events", comment:"ILP View: Events Card Title")
        
        styleCardHeaders()
        
        self.sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction:kAnalyticsActionSearch, withLabel:"ILP Today Summary", withValue:nil, forModuleNamed:"ILP");
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        self.sendView("ILP view", forModuleNamed:self.module.name)
    }
    
    override func viewWillDisappear(animated: Bool) {
        
        //remove the delegates on the fetched results controller so that other controllers accessing the objects don't execute the respective delegates
        assignmentsFetchedResultController!.delegate = nil
        eventsFetchedResultsController!.delegate = nil
        announcementsFetchedResultsController!.delegate = nil
    }
    
    func assignmentFetchRequest() -> NSFetchRequest {
        
        let todayDateRange = createTodayDateRange() as NSArray?
        let endOfToday = todayDateRange![1] as! NSDate
        
        ////today only
        ////let predicate = NSPredicate(format: "(dueDate >= %@) AND (dueDate <= %@)", argumentArray: todayDateRange)
        
        //today and earlier
        let predicate = NSPredicate(format: "(dueDate <= %@)", endOfToday)
        
        ////debug toggle to simulate no data scenario
        ////let predicate = NSPredicate(format: "(dueDate < %@) AND (dueDate > %@)", argumentArray: todayDateRange)
        
        let fetchRequest = NSFetchRequest(entityName:"CourseAssignment")
        let sortDescriptor = NSSortDescriptor(key:"dueDate", ascending:true)
        fetchRequest.sortDescriptors = [sortDescriptor]
        fetchRequest.predicate = predicate
        return fetchRequest
    }
    
    func getAssignmentsFetchedResultsController() -> NSFetchedResultsController {

        let fetchRequest = assignmentFetchRequest()
        let entity:NSEntityDescription? = NSEntityDescription.entityForName("CourseAssignment", inManagedObjectContext:self.module.managedObjectContext!)
        fetchRequest.entity = entity;
        let theFetchedResultsController:NSFetchedResultsController? = NSFetchedResultsController(fetchRequest: fetchRequest, managedObjectContext:self.module.managedObjectContext!, sectionNameKeyPath:"overDueWarningSectionHeader", cacheName:"ilp")
    
        return theFetchedResultsController!
        
    }
    
    
    func fetchAssignments(sender:AnyObject) {
        
        let importContext = NSManagedObjectContext(concurrencyType: .PrivateQueueConcurrencyType)
        
        importContext.parentContext = self.module.managedObjectContext
        
        let urlString = NSString( format:"%@/%@/assignments", self.module.propertyForKey("ilp"), CurrentUser.sharedInstance().userid )
        let url: NSURL? = NSURL(string: urlString as String)
        
        importContext.performBlock( {
            
            UIApplication.sharedApplication().networkActivityIndicatorVisible = true
            
            let authenticatedRequest = AuthenticatedRequest()
            let responseData:NSData? = authenticatedRequest.requestURL(url, fromView: self)
            
            UIApplication.sharedApplication().networkActivityIndicatorVisible = false
            if let response = responseData
            {
                NSNotificationCenter.defaultCenter().removeObserver(self, name:kLoginExecutorSuccess, object:nil)
                
                let json = JSON(data: response)
                
                let request = NSFetchRequest(entityName:"CourseAssignment")
                var oldObjects: [AnyObject]?
                do {
                    oldObjects = try importContext.executeFetchRequest(request)
                } catch {
                }
                
                for oldObject in oldObjects! {
                    importContext.deleteObject(oldObject as! NSManagedObject)
                }

                let assignmentList: Array<JSON> = json["assignments"].arrayValue
                
                for  jsonDictionary in assignmentList {
                    let entry:CourseAssignment = NSEntityDescription.insertNewObjectForEntityForName("CourseAssignment", inManagedObjectContext: importContext) as! CourseAssignment
                    entry.sectionId = jsonDictionary["sectionId"].stringValue
                    entry.courseName = jsonDictionary["courseName"].stringValue
                    entry.courseSectionNumber = jsonDictionary["courseSectionNumber"].stringValue
                    entry.name = jsonDictionary["name"].stringValue
                    entry.assignmentDescription = jsonDictionary["description"].stringValue
                    entry.dueDate = self.datetimeFormatter()?.dateFromString(jsonDictionary["dueDate"].stringValue)
                    entry.url = jsonDictionary["url"].stringValue
                }
                
                do {
                    try importContext.save()
                } catch let saveError as NSError {
                    NSLog("save error: \(saveError.localizedDescription)")
                } catch {
                }
            }
            importContext.parentContext?.performBlock({
                do {
                    try importContext.parentContext!.save()
                } catch let parentError as NSError {
                    NSLog("Could not save to store after update to course assignments: \(parentError.localizedDescription)")
                } catch {
                    
                }
            })
        })
    }

    func announcementFetchRequest() -> NSFetchRequest {
        
        let todayDateRange = createTodayDateRange()
        
        //debug toggle to simulate no data scenario
        //let predicate = NSPredicate(format: "(date < %@) AND (date > %@)", argumentArray: todayDateRange)
        let predicate = NSPredicate(format: "(date >= %@) AND (date <= %@)", argumentArray: todayDateRange)
        
        let fetchRequest = NSFetchRequest(entityName:"CourseAnnouncement")
        let sortDescriptor = NSSortDescriptor(key:"date", ascending:false)
        fetchRequest.sortDescriptors = [sortDescriptor]
        fetchRequest.predicate = predicate
        return fetchRequest
    }
    
    
    func getAnnouncementsFetchedResultsController() -> NSFetchedResultsController {
        let fetchRequest = announcementFetchRequest()
        let entity:NSEntityDescription? = NSEntityDescription.entityForName("CourseAnnouncement", inManagedObjectContext:self.module.managedObjectContext!)
        fetchRequest.entity = entity;
        
        let theFetchedResultsController:NSFetchedResultsController? = NSFetchedResultsController(fetchRequest: fetchRequest, managedObjectContext:module.managedObjectContext!, sectionNameKeyPath:nil, cacheName:"ilp")
        return theFetchedResultsController!
        
    }
    
    func fetchAnnouncements(sender:AnyObject) {
        let importContext = NSManagedObjectContext(concurrencyType: .PrivateQueueConcurrencyType)
        
        importContext.parentContext = self.module.managedObjectContext
        
        let urlString = NSString( format:"%@/%@/announcements", self.module.propertyForKey("ilp"), CurrentUser.sharedInstance().userid )
        let url: NSURL? = NSURL(string: urlString as String)

        importContext.performBlock( {
        
            UIApplication.sharedApplication().networkActivityIndicatorVisible = true
            
            let authenticatedRequest = AuthenticatedRequest()
            let responseData:NSData? = authenticatedRequest.requestURL(url, fromView: self)
            
            UIApplication.sharedApplication().networkActivityIndicatorVisible = false
            
            if let response = responseData {
                
                NSNotificationCenter.defaultCenter().removeObserver(self, name:kLoginExecutorSuccess, object:nil)
                
                let json = JSON(data: response)
                
                let request = NSFetchRequest(entityName:"CourseAnnouncement")
                var oldObjects: [AnyObject]?
                do {
                    oldObjects = try importContext.executeFetchRequest(request)
                } catch {
                }
                
                for oldObject in oldObjects! {
                    importContext.deleteObject(oldObject as! NSManagedObject)
                }
                
                let announcementList: Array<JSON> = json["items"].arrayValue
                
                for  jsonDictionary in announcementList {
                    let entry:CourseAnnouncement = NSEntityDescription.insertNewObjectForEntityForName("CourseAnnouncement", inManagedObjectContext: importContext) as! CourseAnnouncement
                    
                    entry.sectionId = jsonDictionary["sectionId"].stringValue
                    entry.courseName = jsonDictionary["courseName"].stringValue
                    entry.courseSectionNumber = jsonDictionary["courseSectionNumber"].stringValue
                    entry.title = jsonDictionary["title"].stringValue
                    entry.content = jsonDictionary["content"].stringValue
                    if let entryDate = self.datetimeFormatter()?.dateFromString(jsonDictionary["date"].stringValue) {
                        entry.date = entryDate
                    } else {
                        
                        let date = NSDate()
                        let cal = NSCalendar(calendarIdentifier: NSCalendarIdentifierGregorian)
                        let components = cal!.components([.Day, .Month, .Year], fromDate: date)
                        let todayAtMidnight = cal!.dateFromComponents(components)

                        entry.date = todayAtMidnight
                    }
                    
                    entry.website = jsonDictionary["website"].stringValue
                }
                

                do {
                    try importContext.save()
                } catch let error as NSError {
                    NSLog("save error: \(error.localizedDescription)")
                } catch {
                }
            }
            importContext.parentContext?.performBlock({
                do {
                    try importContext.parentContext!.save()
                } catch let parentError as NSError {
                    NSLog("Could not save to store after update to course announcements: \(parentError.localizedDescription)")
                } catch {
                }
            })
        })
    }
    
    func createTodayDateRange() -> [NSDate] {
        let cal = NSCalendar.currentCalendar()
        let timezone = NSTimeZone.systemTimeZone()
        cal.timeZone = timezone
        
        let beginComps = cal.components([.Year, .Month, .Day, .Hour, .Minute, .Second], fromDate: NSDate())
        beginComps.hour = 0
        beginComps.minute = 0
        beginComps.second = 0
        
        let endComps = cal.components([.Year, .Month, .Day, .Hour, .Minute, .Second], fromDate: NSDate())
        endComps.hour = 23
        endComps.minute = 59
        endComps.second = 59
        
        let beginOfToday = cal.dateFromComponents(beginComps)!
        let endOfToday = cal.dateFromComponents(endComps)!
        
        return [beginOfToday, endOfToday, beginOfToday, endOfToday, beginOfToday, endOfToday]
    }
    
    func eventFetchRequest() -> NSFetchRequest {
        
        let todayDateRange = createTodayDateRange()
        
        let predicate = NSPredicate(format: "((startDate >= %@) AND (startDate <= %@)) OR ((endDate >= %@) AND (endDate <= %@)) OR ((startDate <= %@) AND (endDate >= %@))", argumentArray: todayDateRange)
        //debug toggle to simulate no data scenario
        //let predicate = NSPredicate(format: "(startDate < %@) AND (startDate > %@)", argumentArray: todayDateRange)
        
        let fetchRequest = NSFetchRequest(entityName:"CourseEvent")
        let sortDescriptor = NSSortDescriptor(key:"startDate", ascending:true)
        fetchRequest.sortDescriptors = [sortDescriptor]
        fetchRequest.predicate = predicate
        return fetchRequest
    }
    
    func getEventsFetchedResultsController() -> NSFetchedResultsController {
        
        let fetchRequest = eventFetchRequest()
        let entity:NSEntityDescription? = NSEntityDescription.entityForName("CourseEvent", inManagedObjectContext:self.module.managedObjectContext!)
        fetchRequest.entity = entity;
        let theFetchedResultsController:NSFetchedResultsController? = NSFetchedResultsController(fetchRequest: fetchRequest, managedObjectContext:module.managedObjectContext!, sectionNameKeyPath:nil, cacheName:"ilp")
        return theFetchedResultsController!
    }
    
    
    func fetchEvents(sender:AnyObject) {
        let importContext = NSManagedObjectContext(concurrencyType: .PrivateQueueConcurrencyType)
        
        importContext.parentContext = self.module.managedObjectContext
        
        let urlString = NSString( format:"%@/%@/events", self.module.propertyForKey("ilp"), CurrentUser.sharedInstance().userid )
        let url: NSURL? = NSURL(string: urlString as String)
        
        importContext.performBlock( {

            UIApplication.sharedApplication().networkActivityIndicatorVisible = true
            
            let authenticatedRequest = AuthenticatedRequest()
            let responseData:NSData? = authenticatedRequest.requestURL(url, fromView: self)
            
            UIApplication.sharedApplication().networkActivityIndicatorVisible = false
            if let response = responseData {
                
                NSNotificationCenter.defaultCenter().removeObserver(self, name:kLoginExecutorSuccess, object:nil)
                
                let json = JSON(data: response)
                
                let request = NSFetchRequest(entityName:"CourseEvent")
                var oldObjects: [AnyObject]?
                do {
                    oldObjects = try importContext.executeFetchRequest(request)
                } catch {
                }
                
                for oldObject in oldObjects! {
                    importContext.deleteObject(oldObject as! NSManagedObject)
                }
                
                let assignmentList: Array<JSON> = json["events"].arrayValue
                
                for  jsonDictionary in assignmentList {
                    let entry:CourseEvent = NSEntityDescription.insertNewObjectForEntityForName("CourseEvent", inManagedObjectContext: importContext) as! CourseEvent
                    
                    entry.sectionId = jsonDictionary["sectionId"].stringValue
                    entry.courseName = jsonDictionary["courseName"].stringValue
                    entry.courseSectionNumber = jsonDictionary["courseSectionNumber"].stringValue
                    entry.title = jsonDictionary["title"].stringValue
                    entry.eventDescription = jsonDictionary["description"].stringValue
                    entry.startDate = self.datetimeFormatter()?.dateFromString(jsonDictionary["startDate"].stringValue)
                    entry.endDate = self.datetimeFormatter()?.dateFromString(jsonDictionary["endDate"].stringValue)
                    entry.location = jsonDictionary["location"].stringValue
                }
                
                do {
                    try importContext.save()
                } catch let error as NSError {
                    NSLog("save error: \(error.localizedDescription)")
                } catch {
                    
                }
            }
            importContext.parentContext?.performBlock({
                do {
                    try importContext.parentContext!.save()
                } catch let error as NSError {
                    NSLog("Could not save to store after update to course events: \(error.localizedDescription)")
                } catch {
                    
                }
            })
        })
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?)
    {

        if (segue.identifier == "Show ILP Assignment Detail") {

            if(requestedAssignmentId != nil) {
                let indexPath = indexPathForAssignmentWithUrl(requestedAssignmentId)
                
                if let indexPath = indexPath {
                    assignmentTableView.selectRowAtIndexPath(indexPath, animated: true, scrollPosition:UITableViewScrollPosition.Top)
                }
            }
            let indexPath: NSIndexPath! = assignmentTableView.indexPathForSelectedRow
            let assignment = assignmentsFetchedResultController!.objectAtIndexPath(indexPath) as! CourseAssignment
            let detailController = segue.destinationViewController as! CourseAssignmentDetailViewController
            detailController.courseName = assignment.courseName
            detailController.courseSectionNumber = assignment.courseSectionNumber
            detailController.itemTitle = assignment.name
            detailController.itemContent = assignment.assignmentDescription
            if let url = assignment.url {
                    detailController.itemLink = url.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet())
            }
            if let assignmentDate = assignment.dueDate {
                detailController.itemPostDateTime = assignmentDate
            }
            else {
                detailController.itemPostDateTime = nil
            }

            assignmentTableView.deselectRowAtIndexPath(indexPath, animated:true)
            requestedAssignmentId = nil
        
        }
        else if (segue.identifier == "Show ILP Announcement Detail") {
            let indexPath: NSIndexPath! = announcementTableView.indexPathForSelectedRow
            let announcement = announcementsFetchedResultsController!.objectAtIndexPath(indexPath) as! CourseAnnouncement
            let detailController = segue.destinationViewController as! CourseAnnouncementDetailViewController
            detailController.courseName = announcement.courseName
            detailController.courseSectionNumber = announcement.courseSectionNumber
            detailController.itemTitle = announcement.title
            detailController.itemContent = announcement.content
            if let url = announcement.website {
                detailController.itemLink = url.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet())
            }
            if let announcementDate = announcement.date {
                detailController.itemPostDateTime = announcementDate
            }
            else {
                detailController.itemPostDateTime = nil
            }
            
            detailController.module = module;
            announcementTableView.deselectRowAtIndexPath(indexPath, animated:true)
            self.sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction:kAnalyticsActionSearch, withLabel:"Show ILP Announcement Detail", withValue:nil, forModuleNamed:"ILP");
        }
        else if (segue.identifier == "Show ILP Event Detail") {
            let indexPath: NSIndexPath! = eventTableView.indexPathForSelectedRow
            let event = eventsFetchedResultsController!.objectAtIndexPath(indexPath) as! CourseEvent
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
            
            detailController.module = module;
            eventTableView.deselectRowAtIndexPath(indexPath, animated: true)
        }
        else if (segue.identifier == "Show All ILP Assignments") {
            let tabBarController = segue.destinationViewController as! UITabBarController
            
            self.initManagedObjectContextInDetailViewControllers(tabBarController)
            tabBarController.selectedIndex = 0
            tabBarController.tabBar.translucent = false
        }
        else if (segue.identifier == "Show All ILP Events") {
            let tabBarController = segue.destinationViewController as! UITabBarController
            self.initManagedObjectContextInDetailViewControllers(tabBarController)
            tabBarController.selectedIndex = 1
            tabBarController.tabBar.translucent = false;
        }
        else if (segue.identifier == "Show All ILP Announcements") {
            let tabBarController = segue.destinationViewController as! UITabBarController
            self.initManagedObjectContextInDetailViewControllers(tabBarController)
            tabBarController.selectedIndex = 2
            tabBarController.tabBar.translucent = false;
        }
        else if (segue.identifier == "Show ILP Assignment Split View For Assignment")
        {
            if(requestedAssignmentId != nil) {
                let indexPath = indexPathForAssignmentWithUrl(requestedAssignmentId)
                
                if let indexPath = indexPath {
                    assignmentTableView.selectRowAtIndexPath(indexPath, animated: true, scrollPosition:UITableViewScrollPosition.Top)
                }
            }
            let indexPath: NSIndexPath! = assignmentTableView.indexPathForSelectedRow
            let assignment = assignmentsFetchedResultController!.objectAtIndexPath(indexPath) as! CourseAssignment
            let tabBarController = segue.destinationViewController as! UITabBarController
            self.initManagedObjectContextInSplitViewControllers(tabBarController, selectedItem:assignment)
            tabBarController.selectedIndex = 0
            tabBarController.tabBar.translucent = false;
            assignmentTableView.deselectRowAtIndexPath(indexPath, animated:true)
            requestedAssignmentId = nil
        }
        else if (segue.identifier == "Show ILP Event Split View For Event" )
        {
            let indexPath: NSIndexPath! = eventTableView.indexPathForSelectedRow
            let event = eventsFetchedResultsController!.objectAtIndexPath(indexPath) as! CourseEvent
            let tabBarController = segue.destinationViewController as! UITabBarController
            self.initManagedObjectContextInSplitViewControllers(tabBarController, selectedItem:event)
            tabBarController.selectedIndex = 1
            tabBarController.tabBar.translucent = false;
            eventTableView.deselectRowAtIndexPath(indexPath, animated: true)
        }
        else if (segue.identifier == "Show ILP Announcement Split View For Announcement")
        {
            let indexPath: NSIndexPath! = announcementTableView.indexPathForSelectedRow
            let announcement = announcementsFetchedResultsController!.objectAtIndexPath(indexPath) as! CourseAnnouncement
            let tabBarController = segue.destinationViewController as! UITabBarController
            self.initManagedObjectContextInSplitViewControllers(tabBarController, selectedItem:announcement)
            tabBarController.selectedIndex = 2
            tabBarController.tabBar.translucent = false;
            announcementTableView.deselectRowAtIndexPath(indexPath, animated:true)
        }
        else if (segue.identifier == "Show All ILP Assignments Split View") {
            let tabBarController = segue.destinationViewController as! UITabBarController
            self.initManagedObjectContextInSplitViewControllers(tabBarController, selectedItem: nil)
            tabBarController.selectedIndex = 0
            tabBarController.tabBar.translucent = false;
        }
        else if (segue.identifier == "Show All ILP Events Split View") {
            let tabBarController = segue.destinationViewController as! UITabBarController
            self.initManagedObjectContextInSplitViewControllers(tabBarController, selectedItem: nil)
            tabBarController.selectedIndex = 1
            tabBarController.tabBar.translucent = false;
        }
        else if (segue.identifier == "Show All ILP Announcements Split View") {
            let tabBarController = segue.destinationViewController as! UITabBarController
            self.initManagedObjectContextInSplitViewControllers(tabBarController, selectedItem: nil)
            tabBarController.selectedIndex = 2
            tabBarController.tabBar.translucent = false;
        }
        
    }
    
    func initManagedObjectContextInSplitViewControllers(tabBarController:UITabBarController, selectedItem:NSObject?)
    {
        for tabbedViewController in tabBarController.viewControllers! {
            
            let splitViewController = tabbedViewController as! UISplitViewController
            
            let navController = splitViewController.viewControllers[0] as! UINavigationController
            let rootViewController = navController.viewControllers[0] as UIViewController
            let detailViewController = splitViewController.viewControllers[1] as UIViewController
            
            if rootViewController is AllAssignmentsViewController ||
                rootViewController is AllEventsViewController ||
                rootViewController is AllAnnouncementsViewController {
                rootViewController.setValue(self.module.managedObjectContext, forKey: "myManagedObjectContext")
                rootViewController.setValue(tabBarController, forKey: "myTabBarController")
                rootViewController.setValue(self.module, forKey: "module")
                rootViewController.setValue(detailViewController, forKey: "detailViewController")
            }
            
            if selectedItem is CourseAssignment && rootViewController is AllAssignmentsViewController {
                let viewController = rootViewController as! AllAssignmentsViewController
                viewController.selectedAssignment = selectedItem as? CourseAssignment
            }
            if selectedItem is CourseEvent && rootViewController is AllEventsViewController {
                let viewController = rootViewController as! AllEventsViewController
                viewController.selectedEvent = selectedItem as? CourseEvent
            }
            if selectedItem is CourseAnnouncement && rootViewController is AllAnnouncementsViewController {
                let viewController = rootViewController as! AllAnnouncementsViewController
                    viewController.selectedAnnouncement = selectedItem as? CourseAnnouncement
            }
            
            splitViewController.preferredDisplayMode = UISplitViewControllerDisplayMode.AllVisible
        }
    }
    
    //iPad Split View Delegate Method for iOS < 8.0
    func splitViewController(svc: UISplitViewController,
        shouldHideViewController vc: UIViewController,
        inOrientation orientation: UIInterfaceOrientation) -> Bool
    {
        return false;
    }
    
    func initManagedObjectContextInDetailViewControllers(tabBarController:UITabBarController)
    {
        for tabbedViewController in tabBarController.viewControllers! {
            if tabbedViewController is AllAssignmentsViewController ||
                tabbedViewController is AllEventsViewController ||
                tabbedViewController is AllAnnouncementsViewController
            {
                tabbedViewController.setValue(self.module.managedObjectContext, forKey: "myManagedObjectContext")
                tabbedViewController.setValue(tabBarController, forKey: "myTabBarController")
            }
        }
    }
    
    override func didRotateFromInterfaceOrientation(fromInterfaceOrientation: UIInterfaceOrientation) {
        resizeAfterOrientationChange()
    }
    
    func styleCardHeaders() {
        
        let allAssignmentButtonBounds = showAllAssignmentsView.layer.bounds
        let allAssignmentButtonRect:CGRect = CGRect(x:allAssignmentButtonBounds.origin.x, y:allAssignmentButtonBounds.origin.y, width:assignmentTableWidthConstraint.constant, height:allAssignmentButtonBounds.height)
        
        let allEventsButtonBounds = showAllEventsView.layer.bounds
        let allEventsButtonRect:CGRect = CGRect(x:allEventsButtonBounds.origin.x, y:allEventsButtonBounds.origin.y, width:eventTableWidthConstraint.constant, height:allEventsButtonBounds.height)
        
        let allAnnouncementsButtonBounds = showAllAnnouncementsView.layer.bounds
        let allAnnouncementsButtonRect:CGRect = CGRect(x:allAnnouncementsButtonBounds.origin.x, y:allAnnouncementsButtonBounds.origin.y, width:announcementTableWidthConstraint.constant, height:allAnnouncementsButtonBounds.height)
        
        // Create the path (with only the top-left corner rounded
        let assignmentPath = UIBezierPath(roundedRect: allAssignmentButtonRect, byRoundingCorners:[.TopLeft, .TopRight], cornerRadii: CGSize(width: 3.0, height: 3.0))
        let assignmentMask = CAShapeLayer()
        assignmentMask.path = assignmentPath.CGPath
        showAllAssignmentsView.layer.mask = assignmentMask
        
        // Create the path (with only the top-left corner rounded
        let eventPath = UIBezierPath(roundedRect: allEventsButtonRect, byRoundingCorners:[.TopLeft, .TopRight], cornerRadii: CGSize(width: 3.0, height: 3.0))
        let eventMask = CAShapeLayer()
        eventMask.path = eventPath.CGPath
        showAllEventsView.layer.mask = eventMask
        
        // Create the path (with only the top-left corner rounded
        let announcementPath = UIBezierPath(roundedRect: allAnnouncementsButtonRect, byRoundingCorners:[.TopLeft, .TopRight], cornerRadii: CGSize(width: 3.0, height: 3.0))
        let announcementMask = CAShapeLayer()
        announcementMask.path = announcementPath.CGPath
        showAllAnnouncementsView.layer.mask = announcementMask
        
        showAllAssignmentsView.backgroundColor = darkGray
        showAllAnnouncementsView.backgroundColor = darkGray
        showAllEventsView.backgroundColor = darkGray
        
        assignmentCardView.layer.cornerRadius = 5.0
        eventCardView.layer.cornerRadius = 5.0
        announcementCardView.layer.cornerRadius = 5.0
    }
    
    func initIPadPortraitConstraints() {
        
        if ( portraitConstraintArray.count == 0) {
            
            portraitConstraintArray.append(NSLayoutConstraint(item:assignmentCardView, attribute:NSLayoutAttribute.Leading, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Leading, multiplier:1.0, constant:cardInsetConstant))
            
            portraitConstraintArray.append(NSLayoutConstraint(item:assignmentCardView, attribute:NSLayoutAttribute.Trailing, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Trailing, multiplier:1.0, constant:cardInsetConstant))
            
            portraitConstraintArray.append(NSLayoutConstraint(item:assignmentCardView, attribute:NSLayoutAttribute.Top, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Top, multiplier:1.0, constant:cardInsetConstant))
            
            portraitConstraintArray.appendContentsOf(NSLayoutConstraint.constraintsWithVisualFormat("V:[assignmentCardView]-25-[eventCardView]", options: NSLayoutFormatOptions(rawValue: 0), metrics: nil, views: ["assignmentCardView":assignmentCardView, "eventCardView":eventCardView]))
            
            portraitConstraintArray.append(NSLayoutConstraint(item:eventCardView, attribute:NSLayoutAttribute.Leading, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Leading, multiplier:1.0, constant:cardInsetConstant))
            
            portraitConstraintArray.append(NSLayoutConstraint(item:eventCardView, attribute:NSLayoutAttribute.Trailing, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Trailing, multiplier:1.0, constant:cardInsetConstant))
            
            portraitConstraintArray.appendContentsOf(NSLayoutConstraint.constraintsWithVisualFormat("V:[eventCardView]-25-[announcementCardView]", options: NSLayoutFormatOptions(rawValue: 0), metrics: nil, views: ["eventCardView":eventCardView, "announcementCardView":announcementCardView]))
            
            portraitConstraintArray.append(NSLayoutConstraint(item:announcementCardView, attribute:NSLayoutAttribute.Leading, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Leading, multiplier:1.0, constant:cardInsetConstant))
            
            portraitConstraintArray.append(NSLayoutConstraint(item:announcementCardView, attribute:NSLayoutAttribute.Trailing, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Trailing, multiplier:1.0, constant:cardInsetConstant))
            
            portraitConstraintArray.appendContentsOf(NSLayoutConstraint.constraintsWithVisualFormat("V:[announcementCardView]-25-|", options: NSLayoutFormatOptions(rawValue: 0), metrics: nil, views: ["announcementCardView":announcementCardView]))
        }
    }
    
    func initIPadLandscapeConstraints() {
        
        if landscapeConstraintArray.count == 0 {
            landscapeConstraintArray.append(NSLayoutConstraint(item:assignmentCardView, attribute:NSLayoutAttribute.Leading, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Leading, multiplier:1.0, constant:cardInsetConstant))
            
            landscapeConstraintArray.append(NSLayoutConstraint(item:assignmentCardView, attribute:NSLayoutAttribute.Top, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Top, multiplier:1.0, constant:cardInsetConstant))
            
            landscapeConstraintArray.appendContentsOf(NSLayoutConstraint.constraintsWithVisualFormat("V:[assignmentCardView]->=25-|", options: NSLayoutFormatOptions(rawValue: 0), metrics: nil, views: ["assignmentCardView":assignmentCardView]))

            landscapeConstraintArray.appendContentsOf(NSLayoutConstraint.constraintsWithVisualFormat("H:[assignmentCardView]-25-[eventCardView]", options: NSLayoutFormatOptions(rawValue: 0), metrics: nil, views: ["assignmentCardView":assignmentCardView, "eventCardView":eventCardView]))

            landscapeConstraintArray.append(NSLayoutConstraint(item:eventCardView, attribute:NSLayoutAttribute.Top, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Top, multiplier:1.0, constant:cardInsetConstant))
            
            landscapeConstraintArray.appendContentsOf(NSLayoutConstraint.constraintsWithVisualFormat("V:[eventCardView]->=25-|", options: NSLayoutFormatOptions(rawValue: 0), metrics: nil, views: ["eventCardView":eventCardView]))
            
            landscapeConstraintArray.append(NSLayoutConstraint(item:announcementCardView, attribute:NSLayoutAttribute.Top, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Top, multiplier:1.0, constant:cardInsetConstant))
            
            landscapeConstraintArray.appendContentsOf(NSLayoutConstraint.constraintsWithVisualFormat("V:[announcementCardView]->=25-|", options: NSLayoutFormatOptions(rawValue: 0), metrics: nil, views: ["announcementCardView":announcementCardView]))
            
            landscapeConstraintArray.append(NSLayoutConstraint(item:announcementCardView, attribute:NSLayoutAttribute.Trailing, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Trailing, multiplier:1.0, constant:cardInsetConstant))
            
            landscapeConstraintArray.appendContentsOf(NSLayoutConstraint.constraintsWithVisualFormat("H:[eventCardView]-25-[announcementCardView]", options: NSLayoutFormatOptions(rawValue: 0), metrics: nil, views: ["eventCardView":eventCardView, "announcementCardView":announcementCardView]))
        }
    }
    
    func datetimeFormatter() -> NSDateFormatter?{
        
        if (self.myDatetimeFormatter != nil)  {
            return self.myDatetimeFormatter
        } else {
            self.myDatetimeFormatter = NSDateFormatter()
            self.myDatetimeFormatter!.dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
            self.myDatetimeFormatter!.timeZone = NSTimeZone(name:"UTC")
            return self.myDatetimeFormatter
        }
    }
    
    func showDetailForRequestedAssignment() {
        if let requestedAssignmentId = requestedAssignmentId {
            if let _ = indexPathForAssignmentWithUrl(requestedAssignmentId) {
                if ( UIDevice.currentDevice().userInterfaceIdiom == .Pad ) {
                    self.performSegueWithIdentifier("Show ILP Assignment Split View For Assignment", sender: self)
                } else {
                    self.performSegueWithIdentifier("Show ILP Assignment Detail", sender: self)
                }
            }
        }
    }
    
    func resizeAfterOrientationChange() {
        let screenWidth = AppearanceChanger.currentScreenBoundsDependOnOrientation().width
        
        if UIDevice.currentDevice().userInterfaceIdiom == .Pad {
            
            initIPadPortraitConstraints()
            initIPadLandscapeConstraints()
            
            switch UIDevice.currentDevice().orientation{
            case .Portrait:
                scrollView.removeConstraints(landscapeConstraintArray)
                scrollView.addConstraints(portraitConstraintArray)
                assignmentTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
                announcementTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
                eventTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
            case .PortraitUpsideDown:
                scrollView.removeConstraints(landscapeConstraintArray)
                scrollView.addConstraints(portraitConstraintArray)
                assignmentTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
                announcementTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
                eventTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
            case .LandscapeLeft:
                scrollView.removeConstraints(portraitConstraintArray)
                scrollView.addConstraints(landscapeConstraintArray)
                assignmentTableWidthConstraint.constant = (screenWidth - 100)/3
                announcementTableWidthConstraint.constant = (screenWidth - 100)/3
                eventTableWidthConstraint.constant = (screenWidth - 100)/3
            case .LandscapeRight:
                scrollView.removeConstraints(portraitConstraintArray)
                scrollView.addConstraints(landscapeConstraintArray)
                assignmentTableWidthConstraint.constant = (screenWidth - 100)/3
                announcementTableWidthConstraint.constant = (screenWidth - 100)/3
                eventTableWidthConstraint.constant = (screenWidth - 100)/3
            default:
                scrollView.removeConstraints(landscapeConstraintArray)
                scrollView.addConstraints(portraitConstraintArray)
                assignmentTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
                announcementTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
                eventTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
            }
        } else if UIDevice.currentDevice().userInterfaceIdiom == .Phone {
            assignmentTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
            eventTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
            announcementTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
        }
        styleCardHeaders()
    }
    
    
    func indexPathForAssignmentWithUrl(requestedAssignmentId: String?) -> NSIndexPath? {
        if let requestedAssignmentId = requestedAssignmentId {
            if let assignmentsFetchedResultController = assignmentsFetchedResultController {
                for iter in assignmentsFetchedResultController.fetchedObjects!
                {
                    let temp: CourseAssignment = iter as! CourseAssignment
                    if temp.url != nil {
                        if temp.url == requestedAssignmentId {
                            return assignmentsFetchedResultController.indexPathForObject(temp)
                        }
                    }
                }
            }
        }
        return nil
    }
}
