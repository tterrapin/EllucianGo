//
//  CoursesCalendarViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 10/14/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class CoursesCalendarViewController : UIViewController, UIPickerViewDelegate {
    
    var module : Module?
    
    let timeFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateStyle = .NoStyle
        formatter.timeStyle = .ShortStyle
        return formatter
        }()
    
    let dateFormatterISO8601 : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        formatter.timeZone = NSTimeZone(name:"UTC")
        return formatter
        }()
    let dateFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
        }()
    
    var date : NSDate?
    var cachedData = [CalendarViewEvent]()
    var fetchedDates = [String]()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationController!.navigationBar.translucent = false
        let dayView = self.view as? CalendarViewDayView
        dayView!.autoScrollToFirstEvent = true
        self.navigationItem.title = self.module!.name
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        self.sendView("Schedule (daily view)", forModuleNamed: self.module?.name)
        let dayView = self.view as? CalendarViewDayView
        dayView!.reloadData()
    }
    
    func reloadScheduleNotification(notification: NSNotification) {
        let dayView = self.view as? CalendarViewDayView
        dayView!.reloadData()
    }
    
    func dayView(dayView: CalendarViewDayView!, eventsForDate date: NSDate!) -> [AnyObject]!
    {
        self.date = date
        if let userid = CurrentUser.sharedInstance().userid {
            let startDate: NSDate = date.dateByAddingTimeInterval(-86400.0)
            let endDate: NSDate = date.dateByAddingTimeInterval((86400.0 * 7.0))
            let startFormattedString: String = dateFormatter.stringFromDate(startDate)
            let endFormattedString: String = dateFormatter.stringFromDate(endDate)
            let thisFormattedDate: String = dateFormatter.stringFromDate(date)
            
            if !self.fetchedDates.contains(thisFormattedDate) {
                dispatch_async(dispatch_get_main_queue(), {
                    let hud = MBProgressHUD.showHUDAddedTo(self.view, animated: true)
                    hud.labelText = NSLocalizedString("Loading", comment: "loading message while waiting for data to load")
                })

                let urlPrefix = self.module!.propertyForKey("daily")
                let encodedUserId = userid.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())
                let encodedStart = startFormattedString.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())
                let encodedEnd = endFormattedString.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())
                let urlString: String = "\(urlPrefix!)/\(encodedUserId!)?start=\(encodedStart!)&end=\(encodedEnd!)"
                UIApplication.sharedApplication().networkActivityIndicatorVisible = true
                
                let authenticatedRequest = AuthenticatedRequest()
                let responseData: NSData = authenticatedRequest.requestURL(NSURL(string: urlString), fromView: self)
                
                
                let json = JSON(data: responseData)
                let courseDays = json["coursesDays"].arrayValue
                for dateJson in courseDays {
                    if let date = dateFormatter.dateFromString(dateJson["date"].stringValue) {
                        let dateString = dateFormatter.stringFromDate(date)
                        if dateString.isEqual(startFormattedString) || dateString.isEqual(endFormattedString) {
                            //At the edges of the response, do not include as fully fetched date
                        }
                        else {
                            self.fetchedDates.append(dateString)
                        }
                        
                        for meetingJson in dateJson["coursesMeetings"].arrayValue {
                            let event = CalendarViewEvent()
                            event.allDay = false
                            let sectionTitle: String = meetingJson["sectionTitle"].stringValue
                            let courseName: String = meetingJson["courseName"].stringValue
                            let courseSectionNumber: String = meetingJson["courseSectionNumber"].stringValue
                            let localizedFormat = NSLocalizedString("course calendar course name-course section - title", tableName: "Localizable", bundle: NSBundle.mainBundle(), value: "%@-%@ - %@", comment: "course calendar course name-course section - title") as String
                            event.line1 = String(format:localizedFormat, courseName, courseSectionNumber, sectionTitle)
                            let startDate = dateFormatterISO8601.dateFromString(meetingJson["start"].stringValue)
                            let endDate = dateFormatterISO8601.dateFromString(meetingJson["end"].stringValue)
                            
                            let startLabel: String = timeFormatter.stringFromDate(startDate!)
                            let endLabel: String = timeFormatter.stringFromDate(endDate!)
                            if meetingJson["building"].string != nil && meetingJson["room"] != nil {
                                event.line3 = String(format:NSLocalizedString("course event start - end date", tableName: "Localizable", bundle: NSBundle.mainBundle(), value: "%@ - %@", comment: "course event start - end date"), startLabel, endLabel)
                                event.line2 = String(format:NSLocalizedString("%@, Room %@", comment: "label - building name, room number"), meetingJson["building"].stringValue, meetingJson["room"].stringValue)
                            } else if meetingJson["building"] != nil{
                                event.line3 = String(format:NSLocalizedString("course event start - end date", tableName: "Localizable", bundle: NSBundle.mainBundle(), value: "%@ - %@", comment: "course event start - end date"), startLabel, endLabel)
                                event.line2 = meetingJson["building"].stringValue
                            } else if meetingJson["room"] != nil {
                                event.line3 = String(format:NSLocalizedString("course event start - end date", tableName: "Localizable", bundle: NSBundle.mainBundle(), value: "%@ - %@", comment: "course event start - end date"), startLabel, endLabel)
                                event.line2 = String(format:NSLocalizedString("Room %@", comment: "label - room number"), meetingJson["room"].stringValue)
                            } else {event.line2 = String(format:NSLocalizedString("course event start - end date", tableName: "Localizable", bundle: NSBundle.mainBundle(), value: "%@ - %@", comment: "course event start - end date"), startLabel, endLabel)
                            }
                            
                            event.start = startDate
                            event.end = endDate
                            
                            event.userInfo = [ "courseName":meetingJson["courseName"].stringValue, "sectionId":meetingJson["sectionId"].stringValue, "termId":meetingJson["termId"].stringValue, "isInstructor":meetingJson["isInstructor"].boolValue, "courseSectionNumber":meetingJson["courseSectionNumber"].stringValue]
                            if !self.cachedData.contains(event) {
                                self.cachedData.append(event)
                            }
                            
                        }
                        
                    }
                    
                }
                dispatch_async(dispatch_get_main_queue(), {
                    MBProgressHUD.hideHUDForView(self.view, animated: true)
                })                
            }
            UIApplication.sharedApplication().networkActivityIndicatorVisible = false
        }
        return self.cachedData
    }
    
    func dayView(dayView: CalendarViewDayView!, eventTapped event: CalendarViewEvent!) {
        self.sendEventToTracker1WithCategory(kAnalyticsCategoryCourses, withAction: kAnalyticsActionButton_Press, withLabel: "Click Course", withValue: nil, forModuleNamed: self.module?.name)
        self.performSegueWithIdentifier("Show Course Detail", sender: event)
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?)
    {
        if segue.identifier == "Show Course Detail" {
            let viewEvent = sender as! CalendarViewEvent
            var userInfo: [NSObject : AnyObject] = viewEvent.userInfo
            let tabBarController = segue.destinationViewController as! CourseDetailTabBarController
            tabBarController.isInstructor = userInfo["isInstructor"] as? Bool
            tabBarController.module = self.module
            tabBarController.termId = userInfo["termId"] as? String
            tabBarController.sectionId = userInfo["sectionId"] as? String
            for v in tabBarController.viewControllers! {
                var vc: UIViewController = v
                if let navVC = v as? UINavigationController {

                    vc = navVC.viewControllers[0]
                }
                if vc.respondsToSelector(Selector("setModule:")) {
                    vc.setValue(self.module, forKey: "module")
                }
                if vc.respondsToSelector(Selector("setSectionId:")) {
                    vc.setValue(userInfo["sectionId"], forKey: "sectionId")
                }
                if vc.respondsToSelector(Selector("setTermId:")) {
                    vc.setValue(userInfo["termId"], forKey: "termId")
                }
                if vc.respondsToSelector(Selector("setCourseName:")) {
                    vc.setValue(userInfo["courseName"], forKey: "courseName")
                }
                if vc.respondsToSelector(Selector("setCourseNameAndSectionNumber:")) {
                    let courseName = userInfo["courseName"]
                    let courseSectionNumber = userInfo["courseSectionNumber"]
                    let courseNameAndSectionNumber = "\(courseName!)-\(courseSectionNumber!)"
                    vc.setValue(courseNameAndSectionNumber, forKey: "courseNameAndSectionNumber")
                }


            }
        }
    }
    
    func reloadDay(sender: AnyObject) {
        let dayView = self.view as! CalendarViewDayView
        dayView.reloadData()
    }
    
}
