//
//  SwiftDailyViewController.swift
//  Mobile
//
//  Created by Alan McEwan on 1/9/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation
import UIKit


var requestedAssignmentId: String? = nil

class ILPViewController : UIViewController, UIActionSheetDelegate, UIGestureRecognizerDelegate, UISplitViewControllerDelegate
{
    
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
    var landscapeConstraintArray:NSMutableArray = []
    //Portrait Constraints
    var portraitConstraintArray:NSMutableArray = []
    
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
        
        var assignmentError:NSError?
        var announcementError:NSError?
        var eventError:NSError?
        
        super.viewDidLoad()
        showAllAnnouncementsView.userInteractionEnabled = true
        
        navigationController?.navigationBar.translucent = false;
        self.title = self.module!.name;

        assignmentsFetchedResultController = getAssignmentsFetchedResultsController()
        var assignmentTableWidth = assignmentCardView.frame.width
        assignmentsTableViewDelegate = AssignmentTableViewDelegate(tableView: assignmentTableView, resultsController: assignmentsFetchedResultController!, heightConstraint: assignmentTableHeightConstraint, widthConstraint: assignmentTableWidthConstraint, parentModule:self.module, viewController:self)
        if (!assignmentsFetchedResultController!.performFetch(&assignmentError)) {
            NSLog("Unersolved error fetching assignments: fetch error: \(assignmentError!.localizedDescription)")
        }
        
        announcementsFetchedResultsController = getAnnouncementsFetchedResultsController()
        announcementsTableViewDelegate = AnnouncementTableViewDelegate(tableView: announcementTableView, controller: announcementsFetchedResultsController!, heightConstraint:announcementTableHeightConstraint, widthConstraint: announcementTableWidthConstraint, parentModule:module)
        if (!announcementsFetchedResultsController!.performFetch(&announcementError)) {
            NSLog("Unresolved error fetching announcements: fetch error: \(announcementError!.localizedDescription)")
        }
        
        eventsFetchedResultsController = getEventsFetchedResultsController()
        eventsTableViewDelegate = EventTableViewDelegate(tableView: eventTableView, controller: eventsFetchedResultsController!, heightConstraint: eventTableHeightConstraint, widthConstraint: eventTableWidthConstraint, parentModule:module)
        if (!eventsFetchedResultsController!.performFetch(&eventError)) {
            NSLog("Unresolved error fetching events: fetch error: \(eventError!.localizedDescription)")
        }
        
        let currentUser = CurrentUser.sharedInstance()
        
        if currentUser.isLoggedIn {
            fetchAnnouncements(self)
            fetchEvents(self)
            fetchAssignments(self)
        }
        
        var screenWidth = AppearanceChanger.currentScreenBoundsDependOnOrientation().width
        if UIDevice.currentDevice().userInterfaceIdiom == .Pad
        {
            cardInsetConstant = 25.0
            initIPadPortraitConstraints()
            initIPadLandscapeConstraints()

            switch UIDevice.currentDevice().orientation{
            case .Portrait:
                scrollView.removeConstraints(landscapeConstraintArray as [AnyObject])
                scrollView.addConstraints(portraitConstraintArray as [AnyObject])
                assignmentTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
                announcementTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
                eventTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
            case .PortraitUpsideDown:
                scrollView.removeConstraints(landscapeConstraintArray as [AnyObject])
                scrollView.addConstraints(portraitConstraintArray as [AnyObject])
                assignmentTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
                announcementTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
                eventTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
            case .LandscapeLeft:
                scrollView.removeConstraints(portraitConstraintArray as [AnyObject])
                scrollView.addConstraints(landscapeConstraintArray as [AnyObject])
                assignmentTableWidthConstraint.constant = (screenWidth - 100)/3
                announcementTableWidthConstraint.constant = (screenWidth - 100)/3
                eventTableWidthConstraint.constant = (screenWidth - 100)/3
            case .LandscapeRight:
                scrollView.removeConstraints(portraitConstraintArray as [AnyObject])
                scrollView.addConstraints(landscapeConstraintArray as [AnyObject])
                assignmentTableWidthConstraint.constant = (screenWidth - 100)/3
                announcementTableWidthConstraint.constant = (screenWidth - 100)/3
                eventTableWidthConstraint.constant = (screenWidth - 100)/3
            default:
                scrollView.removeConstraints(landscapeConstraintArray as [AnyObject])
                scrollView.addConstraints(portraitConstraintArray as [AnyObject])
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
        eventsCardTitle.text = NSLocalizedString("Today's Events", comment:"ILP View: Events Card Title")
        
        styleCardHeaders()
        
        self.sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction:kAnalyticsActionSearch, withLabel:"ILP Today Summary", withValue:nil, forModuleNamed:"ILP");
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        self.sendView("ILP view", forModuleNamed:self.module.name)
        
        if requestedAssignmentId != nil {
            self.showDetailForRequestedAssignment()
        }
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
        
        var importContext = NSManagedObjectContext(concurrencyType: .PrivateQueueConcurrencyType)
        
        importContext.parentContext = self.module.managedObjectContext
        
        var urlString = NSString( format:"%@/%@/assignments", self.module.propertyForKey("ilp"), CurrentUser.sharedInstance().userid )
        var url: NSURL? = NSURL(string: urlString as String)
        
        importContext.performBlock( {
            
            var error:NSError?
            UIApplication.sharedApplication().networkActivityIndicatorVisible = true
            
            var authenticatedRequest = AuthenticatedRequest()
            var responseData:NSData? = authenticatedRequest.requestURL(url, fromView: self)
            
            UIApplication.sharedApplication().networkActivityIndicatorVisible = false
            if let response = responseData
            {
                NSNotificationCenter.defaultCenter().removeObserver(self, name:kLoginExecutorSuccess, object:nil)
                
                let json = JSON(data: response)
                
                var request = NSFetchRequest(entityName:"CourseAssignment")
                var oldObjects = importContext.executeFetchRequest(request, error:&error)
                
                for oldObject in oldObjects! {
                    importContext.deleteObject(oldObject as! NSManagedObject)
                }
                
                let assignmentList: Array<JSON> = json["assignments"].arrayValue
                
                for  jsonDictionary in assignmentList {
                    var entry:CourseAssignment = NSEntityDescription.insertNewObjectForEntityForName("CourseAssignment", inManagedObjectContext: importContext) as! CourseAssignment
                    entry.sectionId = jsonDictionary["sectionId"].stringValue
                    entry.courseName = jsonDictionary["courseName"].stringValue
                    entry.courseSectionNumber = jsonDictionary["courseSectionNumber"].stringValue
                    entry.name = jsonDictionary["name"].stringValue
                    entry.assignmentDescription = jsonDictionary["description"].stringValue
                    entry.dueDate = self.datetimeFormatter()?.dateFromString(jsonDictionary["dueDate"].stringValue)
                    entry.url = jsonDictionary["url"].stringValue
                }
                
                var saveError: NSError?
                importContext.save(&saveError)
                if !importContext.save(&saveError) {
                    NSLog("save error: \(saveError!.localizedDescription)")
                }
            }
            importContext.parentContext?.performBlock({
                var parentError: NSError?
                if !importContext.parentContext!.save(&parentError)
                {
                    NSLog("Could not save to store after update to course assignments: \(parentError!.localizedDescription)")
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
        var importContext = NSManagedObjectContext(concurrencyType: .PrivateQueueConcurrencyType)
        
        importContext.parentContext = self.module.managedObjectContext
        
        var urlString = NSString( format:"%@/%@/announcements", self.module.propertyForKey("ilp"), CurrentUser.sharedInstance().userid )
        var url: NSURL? = NSURL(string: urlString as String)

        importContext.performBlock( {
            
            var error:NSError?
            UIApplication.sharedApplication().networkActivityIndicatorVisible = true
            
            var authenticatedRequest = AuthenticatedRequest()
            var responseData:NSData? = authenticatedRequest.requestURL(url, fromView: self)
            
            UIApplication.sharedApplication().networkActivityIndicatorVisible = false
            
            if let response = responseData {
                
                NSNotificationCenter.defaultCenter().removeObserver(self, name:kLoginExecutorSuccess, object:nil)
                
                let json = JSON(data: response)
                
                var request = NSFetchRequest(entityName:"CourseAnnouncement")
                var oldObjects = importContext.executeFetchRequest(request, error:&error)
                
                for oldObject in oldObjects! {
                    importContext.deleteObject(oldObject as! NSManagedObject)
                }
                
                let announcementList: Array<JSON> = json["items"].arrayValue
                
                for  jsonDictionary in announcementList {
                    var entry:CourseAnnouncement = NSEntityDescription.insertNewObjectForEntityForName("CourseAnnouncement", inManagedObjectContext: importContext) as! CourseAnnouncement
                    
                    entry.sectionId = jsonDictionary["sectionId"].stringValue
                    entry.courseName = jsonDictionary["courseName"].stringValue
                    entry.courseSectionNumber = jsonDictionary["courseSectionNumber"].stringValue
                    entry.title = jsonDictionary["title"].stringValue
                    entry.content = jsonDictionary["content"].stringValue
                    if let entryDate = self.datetimeFormatter()?.dateFromString(jsonDictionary["date"].stringValue) {
                        entry.date = entryDate
                    } else {
                        
                        let date = NSDate()
                        let cal = NSCalendar(calendarIdentifier: NSGregorianCalendar)
                        let components = cal!.components(.CalendarUnitDay | .CalendarUnitMonth | .CalendarUnitYear, fromDate: date)
                        let todayAtMidnight = cal!.dateFromComponents(components)

                        entry.date = todayAtMidnight
                    }
                    
                    entry.website = jsonDictionary["website"].stringValue
                }
                
                var saveError: NSError?
                importContext.save(&saveError)
                if !importContext.save(&saveError) {
                    NSLog("save error: \(saveError!.localizedDescription)")
                }
            }
            importContext.parentContext?.performBlock({
                var parentError: NSError?
                if !importContext.parentContext!.save(&parentError)
                {
                    NSLog("Could not save to store after update to course announcements: \(parentError!.localizedDescription)")
                }
            })
        })
    }
    
    func createTodayDateRange() -> [NSDate] {
        let cal = NSCalendar.currentCalendar()
        let timezone = NSTimeZone.systemTimeZone()
        cal.timeZone = timezone
        
        var beginComps = cal.components(NSCalendarUnit.YearCalendarUnit | .MonthCalendarUnit | .DayCalendarUnit | .HourCalendarUnit | .MinuteCalendarUnit | .SecondCalendarUnit, fromDate: NSDate())
        beginComps.hour = 0
        beginComps.minute = 0
        beginComps.second = 0
        
        var endComps = cal.components(NSCalendarUnit.YearCalendarUnit | .MonthCalendarUnit | .DayCalendarUnit | .HourCalendarUnit | .MinuteCalendarUnit | .SecondCalendarUnit, fromDate: NSDate())
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
        var importContext = NSManagedObjectContext(concurrencyType: .PrivateQueueConcurrencyType)
        
        importContext.parentContext = self.module.managedObjectContext
        
        var urlString = NSString( format:"%@/%@/events", self.module.propertyForKey("ilp"), CurrentUser.sharedInstance().userid )
        var url: NSURL? = NSURL(string: urlString as String)
        
        importContext.performBlock( {
            
            var error:NSError?
            UIApplication.sharedApplication().networkActivityIndicatorVisible = true
            
            var authenticatedRequest = AuthenticatedRequest()
            var responseData:NSData? = authenticatedRequest.requestURL(url, fromView: self)
            
            UIApplication.sharedApplication().networkActivityIndicatorVisible = false
            if let response = responseData {
                
                NSNotificationCenter.defaultCenter().removeObserver(self, name:kLoginExecutorSuccess, object:nil)
                
                let json = JSON(data: response)
                
                var request = NSFetchRequest(entityName:"CourseEvent")
                var oldObjects = importContext.executeFetchRequest(request, error:&error)
                
                for oldObject in oldObjects! {
                    importContext.deleteObject(oldObject as! NSManagedObject)
                }
                
                let assignmentList: Array<JSON> = json["events"].arrayValue
                
                for  jsonDictionary in assignmentList {
                    var entry:CourseEvent = NSEntityDescription.insertNewObjectForEntityForName("CourseEvent", inManagedObjectContext: importContext) as! CourseEvent
                    
                    entry.sectionId = jsonDictionary["sectionId"].stringValue
                    entry.courseName = jsonDictionary["courseName"].stringValue
                    entry.courseSectionNumber = jsonDictionary["courseSectionNumber"].stringValue
                    entry.title = jsonDictionary["title"].stringValue
                    entry.eventDescription = jsonDictionary["description"].stringValue
                    entry.startDate = self.datetimeFormatter()?.dateFromString(jsonDictionary["startDate"].stringValue)
                    entry.endDate = self.datetimeFormatter()?.dateFromString(jsonDictionary["endDate"].stringValue)
                    entry.location = jsonDictionary["location"].stringValue
                }
                
                var saveError: NSError?
                importContext.save(&saveError)
                if !importContext.save(&saveError) {
                    NSLog("save error: \(saveError!.localizedDescription)")
                }
            }
            importContext.parentContext?.performBlock({
                var parentError: NSError?
                if !importContext.parentContext!.save(&parentError)
                {
                    NSLog("Could not save to store after update to course events: \(parentError!.localizedDescription)")
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
            let indexPath: NSIndexPath! = assignmentTableView.indexPathForSelectedRow()
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
            let indexPath: NSIndexPath! = announcementTableView.indexPathForSelectedRow()
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
            let indexPath: NSIndexPath! = eventTableView.indexPathForSelectedRow()
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
            let indexPath: NSIndexPath! = assignmentTableView.indexPathForSelectedRow()
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
            let indexPath: NSIndexPath! = eventTableView.indexPathForSelectedRow()
            let event = eventsFetchedResultsController!.objectAtIndexPath(indexPath) as! CourseEvent
            let tabBarController = segue.destinationViewController as! UITabBarController
            self.initManagedObjectContextInSplitViewControllers(tabBarController, selectedItem:event)
            tabBarController.selectedIndex = 1
            tabBarController.tabBar.translucent = false;
            eventTableView.deselectRowAtIndexPath(indexPath, animated: true)
        }
        else if (segue.identifier == "Show ILP Announcement Split View For Announcement")
        {
            let indexPath: NSIndexPath! = announcementTableView.indexPathForSelectedRow()
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
            
            var navController = splitViewController.viewControllers[0] as! UINavigationController
            var rootViewController = navController.viewControllers[0] as! UIViewController
            var detailViewController = splitViewController.viewControllers[1] as! UIViewController
            
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
    
            switch UIDevice.currentDevice().systemVersion.compare("8.0.0", options: NSStringCompareOptions.NumericSearch) {
            case .OrderedSame, .OrderedDescending:
                //iOS >= 8.0
                splitViewController.preferredDisplayMode = UISplitViewControllerDisplayMode.AllVisible
            case .OrderedAscending:
                //iOS < 8.0
                if splitViewController.delegate == nil {
                    splitViewController.delegate = self
                }
            }
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
        
        var allAssignmentButtonBounds = showAllAssignmentsView.layer.bounds
        var allAssignmentButtonRect:CGRect = CGRect(x:allAssignmentButtonBounds.origin.x, y:allAssignmentButtonBounds.origin.y, width:assignmentTableWidthConstraint.constant, height:allAssignmentButtonBounds.height)
        
        var allEventsButtonBounds = showAllEventsView.layer.bounds
        var allEventsButtonRect:CGRect = CGRect(x:allEventsButtonBounds.origin.x, y:allEventsButtonBounds.origin.y, width:eventTableWidthConstraint.constant, height:allEventsButtonBounds.height)
        
        var allAnnouncementsButtonBounds = showAllAnnouncementsView.layer.bounds
        var allAnnouncementsButtonRect:CGRect = CGRect(x:allAnnouncementsButtonBounds.origin.x, y:allAnnouncementsButtonBounds.origin.y, width:announcementTableWidthConstraint.constant, height:allAnnouncementsButtonBounds.height)
        
        // Create the path (with only the top-left corner rounded
        let assignmentPath = UIBezierPath(roundedRect: allAssignmentButtonRect, byRoundingCorners:.TopLeft | .TopRight, cornerRadii: CGSize(width: 3.0, height: 3.0))
        let assignmentMask = CAShapeLayer()
        assignmentMask.path = assignmentPath.CGPath
        showAllAssignmentsView.layer.mask = assignmentMask
        
        // Create the path (with only the top-left corner rounded
        let eventPath = UIBezierPath(roundedRect: allEventsButtonRect, byRoundingCorners:.TopLeft | .TopRight, cornerRadii: CGSize(width: 3.0, height: 3.0))
        let eventMask = CAShapeLayer()
        eventMask.path = eventPath.CGPath
        showAllEventsView.layer.mask = eventMask
        
        // Create the path (with only the top-left corner rounded
        let announcementPath = UIBezierPath(roundedRect: allAnnouncementsButtonRect, byRoundingCorners:.TopLeft | .TopRight, cornerRadii: CGSize(width: 3.0, height: 3.0))
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
            
            portraitConstraintArray.addObject(NSLayoutConstraint(item:assignmentCardView, attribute:NSLayoutAttribute.Leading, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Leading, multiplier:1.0, constant:cardInsetConstant))
            
            portraitConstraintArray.addObject(NSLayoutConstraint(item:assignmentCardView, attribute:NSLayoutAttribute.Trailing, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Trailing, multiplier:1.0, constant:cardInsetConstant))
            
            portraitConstraintArray.addObject(NSLayoutConstraint(item:assignmentCardView, attribute:NSLayoutAttribute.Top, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Top, multiplier:1.0, constant:cardInsetConstant))
            
            portraitConstraintArray.addObjectsFromArray(NSLayoutConstraint.constraintsWithVisualFormat("V:[assignmentCardView]-25-[eventCardView]", options: NSLayoutFormatOptions(0), metrics: nil, views: ["assignmentCardView":assignmentCardView, "eventCardView":eventCardView]))
            
            portraitConstraintArray.addObject(NSLayoutConstraint(item:eventCardView, attribute:NSLayoutAttribute.Leading, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Leading, multiplier:1.0, constant:cardInsetConstant))
            
            portraitConstraintArray.addObject(NSLayoutConstraint(item:eventCardView, attribute:NSLayoutAttribute.Trailing, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Trailing, multiplier:1.0, constant:cardInsetConstant))
            
            portraitConstraintArray.addObjectsFromArray(NSLayoutConstraint.constraintsWithVisualFormat("V:[eventCardView]-25-[announcementCardView]", options: NSLayoutFormatOptions(0), metrics: nil, views: ["eventCardView":eventCardView, "announcementCardView":announcementCardView]))
            
            portraitConstraintArray.addObject(NSLayoutConstraint(item:announcementCardView, attribute:NSLayoutAttribute.Leading, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Leading, multiplier:1.0, constant:cardInsetConstant))
            
            portraitConstraintArray.addObject(NSLayoutConstraint(item:announcementCardView, attribute:NSLayoutAttribute.Trailing, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Trailing, multiplier:1.0, constant:cardInsetConstant))
            
            portraitConstraintArray.addObjectsFromArray(NSLayoutConstraint.constraintsWithVisualFormat("V:[announcementCardView]-25-|", options: NSLayoutFormatOptions(0), metrics: nil, views: ["announcementCardView":announcementCardView]))
        }
    }
    
    func initIPadLandscapeConstraints() {
        
        if landscapeConstraintArray.count == 0 {
            landscapeConstraintArray.addObject(NSLayoutConstraint(item:assignmentCardView, attribute:NSLayoutAttribute.Leading, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Leading, multiplier:1.0, constant:cardInsetConstant))
            
            landscapeConstraintArray.addObject(NSLayoutConstraint(item:assignmentCardView, attribute:NSLayoutAttribute.Top, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Top, multiplier:1.0, constant:cardInsetConstant))
            
            landscapeConstraintArray.addObjectsFromArray(NSLayoutConstraint.constraintsWithVisualFormat("V:[assignmentCardView]->=25-|", options: NSLayoutFormatOptions(0), metrics: nil, views: ["assignmentCardView":assignmentCardView]))

            landscapeConstraintArray.addObjectsFromArray(NSLayoutConstraint.constraintsWithVisualFormat("H:[assignmentCardView]-25-[eventCardView]", options: NSLayoutFormatOptions(0), metrics: nil, views: ["assignmentCardView":assignmentCardView, "eventCardView":eventCardView]))

            landscapeConstraintArray.addObject(NSLayoutConstraint(item:eventCardView, attribute:NSLayoutAttribute.Top, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Top, multiplier:1.0, constant:cardInsetConstant))
            
            landscapeConstraintArray.addObjectsFromArray(NSLayoutConstraint.constraintsWithVisualFormat("V:[eventCardView]->=25-|", options: NSLayoutFormatOptions(0), metrics: nil, views: ["eventCardView":eventCardView]))
            
            landscapeConstraintArray.addObject(NSLayoutConstraint(item:announcementCardView, attribute:NSLayoutAttribute.Top, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Top, multiplier:1.0, constant:cardInsetConstant))
            
            landscapeConstraintArray.addObjectsFromArray(NSLayoutConstraint.constraintsWithVisualFormat("V:[announcementCardView]->=25-|", options: NSLayoutFormatOptions(0), metrics: nil, views: ["announcementCardView":announcementCardView]))
            
            landscapeConstraintArray.addObject(NSLayoutConstraint(item:announcementCardView, attribute:NSLayoutAttribute.Trailing, relatedBy:NSLayoutRelation.Equal, toItem:scrollView, attribute:NSLayoutAttribute.Trailing, multiplier:1.0, constant:cardInsetConstant))
            
            landscapeConstraintArray.addObjectsFromArray(NSLayoutConstraint.constraintsWithVisualFormat("H:[eventCardView]-25-[announcementCardView]", options: NSLayoutFormatOptions(0), metrics: nil, views: ["eventCardView":eventCardView, "announcementCardView":announcementCardView]))
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
    
    class func requestAssignmentDetailById(assignmentUrl: String) {
        requestedAssignmentId = assignmentUrl
    }
    
    func showDetailForRequestedAssignment() {
        if(requestedAssignmentId != nil) {
            let indexPath = indexPathForAssignmentWithUrl(requestedAssignmentId)
            
            if let indexPath = indexPath {
                if ( UIDevice.currentDevice().userInterfaceIdiom == .Pad ) {
                    self.performSegueWithIdentifier("Show ILP Assignment Split View For Assignment", sender: self)
                } else {
                    self.performSegueWithIdentifier("Show ILP Assignment Detail", sender: self)
                }
            }
        }
    }
    
    func resizeAfterOrientationChange() {
        var screenWidth = AppearanceChanger.currentScreenBoundsDependOnOrientation().width
        
        if UIDevice.currentDevice().userInterfaceIdiom == .Pad {
            
            initIPadPortraitConstraints()
            initIPadLandscapeConstraints()
            
            switch UIDevice.currentDevice().orientation{
            case .Portrait:
                scrollView.removeConstraints(landscapeConstraintArray as [AnyObject])
                scrollView.addConstraints(portraitConstraintArray as [AnyObject])
                assignmentTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
                announcementTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
                eventTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
            case .PortraitUpsideDown:
                scrollView.removeConstraints(landscapeConstraintArray as [AnyObject])
                scrollView.addConstraints(portraitConstraintArray as [AnyObject])
                assignmentTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
                announcementTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
                eventTableWidthConstraint.constant = screenWidth - (cardInsetConstant * 2.0)
            case .LandscapeLeft:
                scrollView.removeConstraints(portraitConstraintArray as [AnyObject])
                scrollView.addConstraints(landscapeConstraintArray as [AnyObject])
                assignmentTableWidthConstraint.constant = (screenWidth - 100)/3
                announcementTableWidthConstraint.constant = (screenWidth - 100)/3
                eventTableWidthConstraint.constant = (screenWidth - 100)/3
            case .LandscapeRight:
                scrollView.removeConstraints(portraitConstraintArray as [AnyObject])
                scrollView.addConstraints(landscapeConstraintArray as [AnyObject])
                assignmentTableWidthConstraint.constant = (screenWidth - 100)/3
                announcementTableWidthConstraint.constant = (screenWidth - 100)/3
                eventTableWidthConstraint.constant = (screenWidth - 100)/3
            default:
                scrollView.removeConstraints(landscapeConstraintArray as [AnyObject])
                scrollView.addConstraints(portraitConstraintArray as [AnyObject])
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
