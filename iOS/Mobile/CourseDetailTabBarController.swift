//
//  CourseDetailTabBarController.swift
//  Mobile
//
//  Created by Jason Hocker on 10/23/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

let kCourseDetailInformationLoaded = "CourseDetailInformationLoaded"

class CourseDetailTabBarController : UITabBarController {
    
    var module : Module?
    var isInstructor : Bool?
    var termId : String?
    var sectionId : String?
    
    let dateFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        formatter.timeZone = NSTimeZone(name:"UTC")
        return formatter
    }()
    let timeFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateFormat = "HH:mm'Z'"
        formatter.timeZone = NSTimeZone(name:"UTC")
        return formatter
    }()
    let tzTimeFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateFormat = "HH:mm'Z'"
        return formatter
    }()
    var originalViewControllers : [UIViewController]?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        let navBarController = self.moreNavigationController
        navBarController.navigationBar.translucent = false
        navBarController.navigationBar.barTintColor = UIColor.primaryColor()
        self.tabBar.translucent = false
        self.originalViewControllers = self.viewControllers
        self.renderTabs()
        if CurrentUser.sharedInstance().isLoggedIn {
            self.fetchCourseDetail(self)
        }
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "fetchCourseDetail:", name: kLoginExecutorSuccess, object: nil)
        
    }
    
    func renderTabs() {
        let request: NSFetchRequest = NSFetchRequest(entityName:"CourseDetail")
        request.predicate = NSPredicate(format: "termId == %@ && sectionId == %@", self.termId!, self.sectionId!)
        var tempArray = [UIViewController]()
        tempArray.append(self.originalViewControllers![0])
        tempArray.append(self.originalViewControllers![1])
        var rosterVisible = self.module!.propertyForKey("visible")
        //for backwards compatible to the way data was stored before 3.0.
        if rosterVisible == nil {
            rosterVisible = self.module!.propertyForKey("rosterVisible")
        }
        if !((rosterVisible == "none") || rosterVisible == "faculty" && !self.isInstructor! ) {
            tempArray.append(self.originalViewControllers![2])
        }
        
        if let _ = self.module!.propertyForKey("ilp") {
            tempArray.append(self.originalViewControllers![3])
            tempArray.append(self.originalViewControllers![4])
            tempArray.append(self.originalViewControllers![5])
        }
        self.viewControllers = tempArray
        self.customizableViewControllers = nil
        let navBarController: UINavigationController = self.moreNavigationController
        navBarController.navigationBar.translucent = false
        navBarController.navigationBar.barTintColor = UIColor.primaryColor()
    }
    
    
    
    
    
    func fetchCourseDetail(sender: AnyObject) {
        
        if let userid = CurrentUser.sharedInstance().userid {
            let urlBase = self.module?.propertyForKey("overview")
            let escapedUserId = userid.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())
            let escapedTermId = self.termId?.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())
            let escapedSectionId = self.sectionId?.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())
            let urlString = "\(urlBase!)/\(escapedUserId!)?term=\(escapedTermId!)&section=\(escapedSectionId!)"
            
            let privateContext = NSManagedObjectContext(concurrencyType: .PrivateQueueConcurrencyType)
            privateContext.parentContext = self.module?.managedObjectContext
            privateContext.undoManager = nil
            
            let hud = MBProgressHUD.showHUDAddedTo(self.view, animated: true)
            hud.labelText = NSLocalizedString("Loading", comment: "loading message while waiting for data to load")
            
            privateContext.performBlock { () -> Void in
                
                do {
                    UIApplication.sharedApplication().networkActivityIndicatorVisible = true
                    
                    defer {
                        dispatch_async(dispatch_get_main_queue()) {
                            
                            UIApplication.sharedApplication().networkActivityIndicatorVisible = false
                            
                            MBProgressHUD.hideHUDForView(self.view, animated: true)
                        }
                    }
                    
                    
                    let authenticatedRequest = AuthenticatedRequest()
                    let responseData = authenticatedRequest.requestURL(NSURL(string: urlString)!, fromView: self)
                    let json = JSON(data: responseData)
                    UIApplication.sharedApplication().networkActivityIndicatorVisible = false
                    
                    let request = NSFetchRequest(entityName: "CourseDetail")
                    request.predicate = NSPredicate(format: "termId == %@ && sectionId == %@", self.termId!, self.sectionId!)
                    let oldObjects = try privateContext
                        .executeFetchRequest(request) as! [CourseDetail]
                    for oldObject in oldObjects {
                        privateContext.deleteObject(oldObject)
                    }
                    
                    if let jsonTerms = json["terms"].array {
                        for jsonTerm in jsonTerms {
                            if let jsonSections = jsonTerm["sections"].array {
                                for jsonSection in jsonSections {
                                    let courseDetail = NSEntityDescription.insertNewObjectForEntityForName("CourseDetail", inManagedObjectContext: privateContext) as! CourseDetail
                                    courseDetail.termId = self.termId
                                    courseDetail.sectionId = jsonSection["sectionId"].string
                                    if let sectionTitle = jsonSection["sectionTitle"].string {
                                        courseDetail.sectionTitle = sectionTitle
                                    }
                                    if let courseName = jsonSection["courseName"].string {
                                        courseDetail.courseName = courseName
                                    }
                                    if let courseDescription = jsonSection["courseDescription"].string {
                                        courseDetail.courseDescription = courseDescription
                                    }
                                    if let courseSectionNumber = jsonSection["courseSectionNumber"].string {
                                        courseDetail.courseSectionNumber = courseSectionNumber
                                    }
                                    if let firstMeetingDate = jsonSection["firstMeetingDate"].string {
                                        let date = self.dateFormatter.dateFromString(firstMeetingDate)
                                        courseDetail.firstMeetingDate = date
                                    }
                                    if let lastMeetingDate = jsonSection["lastMeetingDate"].string {
                                        let date = self.dateFormatter.dateFromString(lastMeetingDate)
                                        courseDetail.lastMeetingDate = date
                                    }
                                    
                                    if let jsonInstructors = jsonSection["instructors"].array {
                                        for jsonInstructor in jsonInstructors {
                                            let instructor = NSEntityDescription.insertNewObjectForEntityForName("CourseDetailInstructor", inManagedObjectContext: privateContext) as! CourseDetailInstructor
                                            if let firstName = jsonInstructor["firstName"].string {
                                                instructor.firstName = firstName
                                            }
                                            if let lastName = jsonInstructor["lastName"].string {
                                                instructor.lastName = lastName
                                            }
                                            if let middleInitial = jsonInstructor["middleInitial"].string {
                                                instructor.middleInitial = middleInitial
                                            }
                                            instructor.instructorId = jsonInstructor["instructorId"].string
                                            instructor.primary = jsonInstructor["primary"].bool
                                            instructor.formattedName = jsonInstructor["formattedName"].string
                                            instructor.course = courseDetail
                                            courseDetail.addInstructorsObject(instructor)

                                        }
                                        
                                    }
                                    if let learningProvider = jsonSection["learningProvider"].string {
                                        courseDetail.learningProvider = learningProvider
                                    }
                                    if let learningProviderSiteId = jsonSection["learningProviderSiteId"].string {
                                        courseDetail.learningProviderSiteId = learningProviderSiteId
                                    }
                                    if let primarySectionId = jsonSection["primarySectionId"].string {
                                        courseDetail.primarySectionId = primarySectionId
                                    }
                                    
                                    if let jsonMeetingPatterns = jsonSection["meetingPatterns"].array {
                                        for jsonMeetingPattern in jsonMeetingPatterns {
                                            let mp = NSEntityDescription.insertNewObjectForEntityForName("CourseMeetingPattern", inManagedObjectContext: privateContext) as! CourseMeetingPattern
                                            if let instructionalMethodCode = jsonMeetingPattern["instructionalMethodCode"].string {
                                                mp.instructionalMethodCode = instructionalMethodCode
                                            }
                                            mp.startDate = self.dateFormatter.dateFromString(jsonMeetingPattern["startDate"].string!)
                                            mp.endDate = self.dateFormatter.dateFromString(jsonMeetingPattern["endDate"].string!)
                                            if let startTime = jsonMeetingPattern["startTime"].string {
                                                let time = self.timeFormatter.dateFromString(startTime)
                                                mp.startTime = time
                                            }
                                            if let endTime = jsonMeetingPattern["endTime"].string {
                                                let time = self.timeFormatter.dateFromString(endTime)
                                                mp.endTime = time
                                            }
                                            if let sisStartTimeWTz = jsonMeetingPattern["sisStartTimeWTz"].string {
                                                var components = sisStartTimeWTz.characters.split { $0 == " " }.map(String.init)
                                                self.tzTimeFormatter.timeZone = NSTimeZone(name: components[1])
                                                let time = self.tzTimeFormatter.dateFromString(components[0])
                                                mp.startTime = time
                                            }
                                            if let sisEndTimeWTz = jsonMeetingPattern["sisEndTimeWTz"].string {
                                                var components = sisEndTimeWTz.characters.split { $0 == " " }.map(String.init)
                                                self.tzTimeFormatter.timeZone = NSTimeZone(name: components[1])
                                                let time = self.tzTimeFormatter.dateFromString(components[0])
                                                mp.endTime = time
                                            }
                                        
                         
                                            let days = jsonMeetingPattern["daysOfWeek"].array!.map{
                                                String($0.intValue)}
                                            mp.daysOfWeek = days.joinWithSeparator(",")
                                            
                                            if let room = jsonMeetingPattern["room"].string {
                                                mp.room = room
                                            }
                                            if let building = jsonMeetingPattern["building"].string {
                                                mp.building = building
                                            }
                                            if let buildingId = jsonMeetingPattern["buildingId"].string {
                                                mp.buildingId = buildingId
                                            }
                                            if let campusId = jsonMeetingPattern["campusId"].string {
                                                mp.campusId = campusId
                                            }
                                            mp.course = courseDetail
                                            courseDetail.addMeetingPatternsObject(mp)
                                        }
                                    }

                                }
                            }
                            
                            
                        }
                    }
                    try privateContext.save()
                    
                    privateContext.parentContext?.performBlock({
                        do {
                            try privateContext.parentContext?.save()
                        } catch let error {
                            print (error)
                        }
                    })
                    
                    dispatch_async(dispatch_get_main_queue(), {
                        self.renderTabs()
                        NSNotificationCenter.defaultCenter().postNotificationName(kCourseDetailInformationLoaded, object: nil)
                    })
                    
                    
                } catch let error {
                    print (error)
                }
                
            }
        }
        
    }
    
    
    
}