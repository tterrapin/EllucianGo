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
    @IBOutlet var retrievingDataLabel: WKInterfaceLabel!
    @IBOutlet var signInLabel: WKInterfaceLabel!
    @IBOutlet var noAssignments: WKInterfaceLabel!
    @IBOutlet var spinner: WKInterfaceImage!
    
    var assignments : [Dictionary<String, AnyObject>]!
    var internalKey : String?
    var urlString : String?
    var cache: DefaultsCache?
    
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
        
        cache = DefaultsCache(key: "ilp assignments \((self.internalKey)!)")
        
        var data: [String: AnyObject] = [:]
        
        if urlString != nil {
            data["url"] = self.urlString
        }
        if internalKey != nil {
            data["internalKey"] = self.internalKey
        }
        
        if WatchConnectivityManager.instance.isUserLoggedIn() {
            if let assignments = cache?.fetch() as! [[String: AnyObject]]? {
                self.assignments = assignments
                self.populateTable()
                
                self.retrievingDataLabel.setHidden(true)
                self.spinner.stopAnimating()
                self.spinner.setHidden(true)
            } else {
                // show the spinner because we don't have data yet
                retrievingDataLabel.setHidden(false)
                self.spinner.startAnimating()
                self.spinner.setHidden(false)
            }
            
            WatchConnectivityManager.instance.sendActionMessage("fetch assignments", data: data, replyHandler: {
                    (data) -> Void in
                
                    self.retrievingDataLabel.setHidden(true)
                    self.spinner.stopAnimating()
                    self.spinner.setHidden(true)

                    self.assignments = data["assignments"] as! [[String:AnyObject]]
                    self.cache?.store(self.assignments)
                    dispatch_async(dispatch_get_main_queue(), {
                        self.populateTable()
                    })
                }, errorHandler: {
                    (error) -> Void in
                    
                    dispatch_async(dispatch_get_main_queue(), {
                        // show error message
                        self.retrievingDataLabel.setHidden(true)
                        self.spinner.stopAnimating()
                        self.spinner.setHidden(true)
                    })
            })
        } else {
            self.assignments = [[String: AnyObject]]()
            self.populateTable()
        }
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
        
        return [beginOfToday, endOfToday]
    }    
    
    func populateTable() {
        assignmentsTable.setNumberOfRows(assignments.count, withRowType: "ILPAssignmentsTableRowController")

        var displayedAssignments = false
        for (index, assignment) in self.assignments.enumerate() {
            displayedAssignments = true
            let row = assignmentsTable.rowControllerAtIndex(index) as! ILPAssignmentsTableRowController
            row.titleLabel.setText(assignment["name"] as! String!)

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
        
        if WatchConnectivityManager.instance.isUserLoggedIn() {
            self.signInLabel.setHidden(true)
            noAssignments.setHidden(displayedAssignments)
        } else {
            self.signInLabel.setHidden(false)
            noAssignments.setHidden(true)
        }
    


    }
    
    override func contextForSegueWithIdentifier(segueIdentifier: String, inTable table: WKInterfaceTable, rowIndex: Int) -> AnyObject? {
        if (segueIdentifier == "ilp-assignment-detail") {
            return self.assignments![rowIndex]
        }
        return nil
    }
    
}
