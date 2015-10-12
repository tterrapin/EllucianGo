//
//  EditReminderViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 4/6/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

public class EditReminderViewController: UITableViewController, UIAlertViewDelegate, UIActionSheetDelegate {
    
    var reminderTitle : String?
    var reminderDate : NSDate?
    var reminderNotes : String?
    
    @IBOutlet var titleLabel: UILabel!
    
    @IBOutlet var reminderSwitch: UISwitch!
    @IBOutlet var dateLabel: UILabel!
    @IBOutlet var datePicker: UIDatePicker!
    @IBOutlet var notesTextView: UITextView!
    @IBOutlet var reminderListLabel: UILabel!
    
    private var datePickerHidden = false
    let eventStore : EKEventStore = {
        return EKEventStore()
    }()
    var selectedCalendar : EKCalendar?

    
    public override func viewDidLoad() {
        titleLabel.text = reminderTitle
        notesTextView.text = reminderNotes
        if let date = reminderDate {
            datePicker.date = date
        }
        eventStore.requestAccessToEntityType(.Reminder) {
            granted, error in
            if (granted) && (error == nil) {
                self.selectedCalendar = self.eventStore.defaultCalendarForNewReminders()
                self.didChangeCalendar()
            } else {
                self.showPermissionNotGrantedAlert()
                self.dismissViewControllerAnimated(true, completion: {});
            }
        };
        didChangeDate()
        toggleDatePicker()
    }
    
    @IBAction func didChangeDate() {
        dateLabel.text = NSDateFormatter.localizedStringFromDate(datePicker.date, dateStyle: .ShortStyle, timeStyle: .ShortStyle)
    }
    
    func didChangeCalendar() {
        if let selectedCalendar = selectedCalendar {
            reminderListLabel.text = selectedCalendar.title
        }
    }
    
    @IBAction func add(sender: AnyObject) {
        eventStore.requestAccessToEntityType(.Reminder) {
            granted, error in
            if granted {
                let reminder:EKReminder = EKReminder(eventStore: self.eventStore)
                reminder.title = self.titleLabel.text!
                reminder.calendar = self.selectedCalendar!
                
                if self.reminderSwitch.on {
                    
                    let calendar = NSCalendar.currentCalendar()
                    let dueDateComponents = calendar.components([.Era, .Year, .Month, .Day, .Hour, .Minute, .Second] , fromDate: self.datePicker.date)
                    reminder.dueDateComponents = dueDateComponents
                    let alarm:EKAlarm = EKAlarm(absoluteDate: self.datePicker.date)
                    reminder.alarms = [alarm]
                }
                reminder.notes = self.notesTextView.text

                do {
                    try self.eventStore.saveReminder(reminder, commit: true)
                } catch  {
                }
                self.dismissViewControllerAnimated(true, completion: {});
            } else {
                self.showPermissionNotGrantedAlert()
            }
        }
    }
    
    @IBAction func cancel(sender: AnyObject) {
        self.dismissViewControllerAnimated(true, completion: {});
    }
    
    public override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        switch (indexPath.section, indexPath.row) {
        case (1, 1):
            tableView.deselectRowAtIndexPath(indexPath, animated: false)
            toggleDatePicker()
        case (2, 0):
            tableView.deselectRowAtIndexPath(indexPath, animated: false)
            showCalendarList()
        default:
            ()
        }
    }
    
    public override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        if datePickerHidden && indexPath.section == 1 && indexPath.row == 2 {
            return 0
        } else {
            return super.tableView(tableView, heightForRowAtIndexPath: indexPath)
        }
    }
    
    private func toggleDatePicker() {
        datePickerHidden = !datePickerHidden
        
        // Force table to update its contents
        tableView.beginUpdates()
        tableView.endUpdates()
    }
    
    public override func tableView(tableView: UITableView, shouldHighlightRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        switch (indexPath.section, indexPath.row) {
        case (1, 1), (2, 0):
            return true
        default:
            return false
        }

    }
    
    private func showCalendarList() {
        
        
        let alertController = UIAlertController(title: NSLocalizedString("Reminder List", comment: "title of reminder list alert picker"), message: NSLocalizedString("Select the name of the reminder list to use.", comment: "Title of the action sheet to select a reminder list to save the reminder"), preferredStyle: .ActionSheet)
        
        let calendars = eventStore.calendarsForEntityType(.Reminder)
        for calendar in calendars {
            let action = UIAlertAction(title: calendar.title, style: .Default) { value in
                self.setCalendarWithName(value.title!)
            }
            alertController.addAction(action)
        }
        let cancelAction = UIAlertAction(title: NSLocalizedString("Cancel", comment: "Cancel"), style: .Cancel, handler: nil)
        alertController.addAction(cancelAction)
        presentViewController(alertController, animated: true, completion: nil)
        
        
    }
    
    public func actionSheet(actionSheet: UIActionSheet, clickedButtonAtIndex buttonIndex: Int) {
        let calendarName = actionSheet.buttonTitleAtIndex(buttonIndex)
        
        setCalendarWithName(calendarName!)
    }
    
    private func setCalendarWithName(calendarName: String) {
        let calendars = eventStore.calendarsForEntityType(.Reminder)
        let filteredCalendars = calendars.filter({ (calendar: AnyObject) -> Bool in
            return calendar.title == calendarName
        })
        if(filteredCalendars.count > 0) {
            selectedCalendar = filteredCalendars[0]
            didChangeCalendar()
        }
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