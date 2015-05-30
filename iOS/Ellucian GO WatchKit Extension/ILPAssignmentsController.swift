//
//  ILPAssignmentsController.swift
//  Mobile
//
//  Created by Jason Hocker on 4/28/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation
import WatchKit
import CoreData

class ILPAssignmentsController: WKInterfaceController {
    
    @IBOutlet var assignmentsTable: WKInterfaceTable!
    
    @IBOutlet var signInLabel: WKInterfaceLabel!
    var assignments : [Dictionary<String, AnyObject>]!
    var internalKey : String?
    var urlString : String?
    
    let datetimeOutputFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.timeStyle = .ShortStyle
        formatter.dateStyle = .ShortStyle
        return formatter
        
        }()
    
    override func awakeWithContext(context: AnyObject?) {
        let dictionary = context! as! Dictionary<String, AnyObject>
        self.internalKey = dictionary["internalKey"] as? String
        self.setTitle(dictionary["title"] as? String)
        self.urlString = dictionary["ilp"] as? String
        
        let infoDictionary = ["action": "fetch assignments", "url" : self.urlString!, "internalKey" : self.internalKey!]
        
        WKInterfaceController.openParentApplication(infoDictionary, reply: { (replyInfo, error) -> Void in
            if let dictionary = replyInfo {
                let loggedInStatus = dictionary["loggedInStatus"] as! Bool
                self.signInLabel.setHidden(loggedInStatus)
                self.assignments = dictionary["assignments"] as! [[String:AnyObject]]
                dispatch_async(dispatch_get_main_queue(), {
                    self.populateTable()
                })
            }
        })
    }
    
    func createTodayDateRange() -> [NSDate] {
        let cal = NSCalendar.currentCalendar()
        let timezone = NSTimeZone.systemTimeZone()
        cal.timeZone = timezone
        
        var beginComps = cal.components(.CalendarUnitYear | .CalendarUnitMonth | .CalendarUnitDay | .CalendarUnitHour | .CalendarUnitMinute | .CalendarUnitSecond, fromDate: NSDate())
        beginComps.hour = 0
        beginComps.minute = 0
        beginComps.second = 0
        
        var endComps = cal.components(.CalendarUnitYear | .CalendarUnitMonth | .CalendarUnitDay | .CalendarUnitHour | .CalendarUnitMinute | .CalendarUnitSecond, fromDate: NSDate())
        endComps.hour = 23
        endComps.minute = 59
        endComps.second = 59
        
        let beginOfToday = cal.dateFromComponents(beginComps)!
        let endOfToday = cal.dateFromComponents(endComps)!
        
        return [beginOfToday, endOfToday]
    }    
    
    func populateTable() {
        assignmentsTable.setNumberOfRows(assignments.count, withRowType: "ILPAssignmentsTableRowController")

        for (index, assignment) in enumerate(self.assignments) {
            let row = assignmentsTable.rowControllerAtIndex(index) as! ILPAssignmentsTableRowController
            row.titleLabel.setText(assignment["name"] as! String!)
        
            var courseName : String?
            if assignment["courseName"] != nil && assignment["courseSectionNumber"] != nil {
                if let courseNameString = assignment["courseName"] as! String!, courseSectionNumberString = assignment["courseSectionNumber"] as! String! {
                    row.courseLabel.setText("\(courseNameString)-\(courseSectionNumberString)")
                }
            } else if assignment["courseName"] != nil {
                row.courseLabel.setText(assignment["courseName"] as! String!)
            } else {
                row.courseLabel.setHidden(true)
            }

            if assignment["dueDate"] != nil {
                if let assignmentDate = assignment["dueDate"] as! NSDate! {
                    let time = (self.datetimeOutputFormatter.stringFromDate(assignmentDate))
                    row.timeLabel.setText(time)
                }
            } else {
                row.timeLabel.setHidden(true)
            }
        }

    }
    
    override func contextForSegueWithIdentifier(segueIdentifier: String, inTable table: WKInterfaceTable, rowIndex: Int) -> AnyObject? {
        if (segueIdentifier == "ilp-assignment-detail") {
            return self.assignments![rowIndex]
        }
        return nil
    }
    
}
