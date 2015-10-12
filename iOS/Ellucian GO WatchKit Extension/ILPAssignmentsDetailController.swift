//
//  ILPAssignmentsDetailController.swift
//  Mobile
//
//  Created by Jason Hocker on 4/28/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation
import WatchKit
import CoreData
import EventKit

class ILPAssignmentsDetailController: WKInterfaceController {
    
    
    var assignment : Dictionary<String, AnyObject>?
    
    let datetimeOutputFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.timeStyle = .ShortStyle
        formatter.dateStyle = .ShortStyle
        return formatter
        
        }()
    
    @IBOutlet var titleLabel: WKInterfaceLabel!
    @IBOutlet var timeLabel: WKInterfaceLabel!
    @IBOutlet var courseNameLabel: WKInterfaceLabel!
    @IBOutlet var descriptionLabel: WKInterfaceLabel!

    override func awakeWithContext(context: AnyObject?) {
        self.assignment = context as? Dictionary<String, AnyObject>
        
        if let assignment = self.assignment {
            self.titleLabel.setText(assignment["name"] as! String!)
            
            if assignment["courseName"] != nil && assignment["courseSectionNumber"] != nil {
                if let courseNameString = assignment["courseName"] as! String!, courseSectionNumberString = assignment["courseSectionNumber"] as! String! {
                    self.courseNameLabel.setText("\(courseNameString)-\(courseSectionNumberString)")
                }
            } else if assignment["courseName"] != nil {
                self.courseNameLabel.setText(assignment["courseName"] as! String!)
            }
            
            if assignment["dueDate"] != nil {
                if let assignmentDate = assignment["dueDate"] as! NSDate! {
                    let time = (self.datetimeOutputFormatter.stringFromDate(assignmentDate))
                    self.timeLabel.setText(time)
                }
            } else {
                self.timeLabel.setHidden(true)
            }

            self.descriptionLabel.setText(assignment["assignmentDescription"] as! String!)
        }
    }
    
    @IBAction func addReminder() {
        let eventStore : EKEventStore = EKEventStore()
        
        eventStore.requestAccessToEntityType(.Reminder) { (granted, error) -> Void in
            //
            if granted {
                let reminder:EKReminder = EKReminder(eventStore: eventStore)
                reminder.title = self.assignment!["name"] as! String!
                reminder.calendar = eventStore.defaultCalendarForNewReminders()
                
                let date = self.assignment!["dueDate"] as! NSDate!
                let calendar = NSCalendar.currentCalendar()
                let dueDateComponents = calendar.components([.Year, .Month, .Day, .Hour, .Minute, .Second], fromDate: date)
                reminder.dueDateComponents = dueDateComponents
                let alarm:EKAlarm = EKAlarm(absoluteDate: date)
                reminder.alarms = [alarm]
                let formattedDate = NSDateFormatter.localizedStringFromDate(date, dateStyle: .ShortStyle, timeStyle: .ShortStyle)
                let localizedDue = NSString.localizedStringWithFormat(NSLocalizedString("Due: %@", comment: "due date label with date"), formattedDate)
                
                var reminderCourseName : String?
                if self.assignment!["courseName"] != nil && self.assignment!["courseSectionNumber"] != nil {
                    if let courseNameString = self.assignment!["courseName"] as! String!, courseSectionNumberString = self.assignment!["courseSectionNumber"] as! String! {
                        reminderCourseName = ("\(courseNameString)-\(courseSectionNumberString)")
                    }
                } else if self.assignment!["courseName"] != nil {
                    reminderCourseName = (self.assignment!["courseName"] as! String!)
                }
                
                let description = self.assignment!["assignmentDescription"] as! String!
                reminder.notes = "\(reminderCourseName)\n\(localizedDue)\n\(description) as! String!)"
//                do {
//                    try eventStore.saveReminder(reminder, commit: true)
//                } catch {
//                }
            } else {
                //todo add modal message to tell them to go iphone to change permissions
            }

            //
        }
        
        eventStore.requestAccessToEntityType(.Reminder) { (granted, error) -> Void in
            if granted {
                let reminder:EKReminder = EKReminder(eventStore: eventStore)
                reminder.title = self.assignment!["name"] as! String!
                reminder.calendar = eventStore.defaultCalendarForNewReminders()
                
                let date = self.assignment!["dueDate"] as! NSDate!
                let calendar = NSCalendar.currentCalendar()
                let dueDateComponents = calendar.components([.Year, .Month, .Day, .Hour, .Minute, .Second], fromDate: date)
                reminder.dueDateComponents = dueDateComponents
                let alarm:EKAlarm = EKAlarm(absoluteDate: date)
                reminder.alarms = [alarm]
                let formattedDate = NSDateFormatter.localizedStringFromDate(date, dateStyle: .ShortStyle, timeStyle: .ShortStyle)
                let localizedDue = NSString.localizedStringWithFormat(NSLocalizedString("Due: %@", comment: "due date label with date"), formattedDate)
                
                var reminderCourseName : String?
                if self.assignment!["courseName"] != nil && self.assignment!["courseSectionNumber"] != nil {
                    if let courseNameString = self.assignment!["courseName"] as! String!, courseSectionNumberString = self.assignment!["courseSectionNumber"] as! String! {
                        reminderCourseName = ("\(courseNameString)-\(courseSectionNumberString)")
                    }
                } else if self.assignment!["courseName"] != nil {
                    reminderCourseName = (self.assignment!["courseName"] as! String!)
                }
                
                let description = self.assignment!["assignmentDescription"] as! String!
                reminder.notes = "\(reminderCourseName)\n\(localizedDue)\n\(description) as! String!)"
//                do {
//                    try eventStore.saveReminder(reminder, commit: true)
//                } catch {
//                    //
//                }

            }
        }
    }
    
    @IBAction func addToCalendar() {
        
        let eventStore : EKEventStore = EKEventStore()
        eventStore.requestAccessToEntityType(.Event, completion: {
            granted, error in
            if (granted) && (error == nil) {
                
                let event:EKEvent = EKEvent(eventStore: eventStore)
                event.title = self.assignment!["name"] as! String!
                if let dueDate = self.assignment!["dueDate"] as! NSDate! {
                    event.startDate = dueDate
                    event.endDate = dueDate
                }
                event.notes = self.assignment!["assignmentDescription"] as! String!
                event.calendar = eventStore.defaultCalendarForNewEvents
                
                var location : String?
                if self.assignment!["courseName"] != nil && self.assignment!["courseSectionNumber"] != nil {
                    if let courseNameString = self.assignment!["courseName"] as! String!, courseSectionNumberString = self.assignment!["courseSectionNumber"] as! String! {
                        location = ("\(courseNameString)-\(courseSectionNumberString)")
                    }
                } else if self.assignment!["courseName"] != nil {
                    location = (self.assignment!["courseName"] as! String!)
                }
                
                event.location = location
                
//                do {
//                    try eventStore.saveEvent(event, span: .ThisEvent)
//                } catch {
//                    fatalError()
//                }
            } else {
                //todo add modal message to tell them to go iphone to change permissions
            }
        })
        
    }

}
