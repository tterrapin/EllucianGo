//
//  EventsDetailViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 8/10/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation
import WebKit

class EventsDetailViewController: UIViewController, UIWebViewDelegate, EKEventEditViewDelegate {
    
    let eventStore = EKEventStore()
    var event : Event?
    var module : Module?
    
    @IBOutlet var webView: UIWebView!
    @IBOutlet var titleLabel: UILabel!
    @IBOutlet var dateLabel: UILabel!
    @IBOutlet var locationLabel: UILabel!
    
    let dateTimeFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateStyle = .MediumStyle
        formatter.timeStyle = .ShortStyle
        return formatter
        }()
    let dateFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateStyle = .MediumStyle
        formatter.timeStyle = .NoStyle
        return formatter
        }()
    let timeFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateStyle = .NoStyle
        formatter.timeStyle = .ShortStyle
        return formatter
        }()
    
    override func loadView() {
        super.loadView()

        self.webView.delegate = self
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationController!.topViewController!.navigationItem.leftBarButtonItem = splitViewController!.displayModeButtonItem()
        self.navigationController!.topViewController!.navigationItem.leftItemsSupplementBackButton = true
        
        if let event = event {
            titleLabel.text = event.summary
            locationLabel.text = event.location

            if event.allDay.boolValue == true {
                let dateString = dateFormatter.stringFromDate(event.startDate!)
                let localizedAllDay = NSLocalizedString("All Day", comment: "label for all day event")
                dateLabel.text = "\(dateString) \(localizedAllDay)"
            } else {
                if let startDate = event.startDate, endDate = event.endDate {
                    if isSameDate(event.startDate!, end: event.endDate) {
                        let formattedStart = self.dateTimeFormatter.stringFromDate(startDate)
                        let formattedEnd = self.timeFormatter.stringFromDate(endDate)
                        dateLabel.text = String(format: NSLocalizedString("%@ - %@", comment: "event start - end"), formattedStart, formattedEnd)

                    } else {
                        let formattedStart = self.dateTimeFormatter.stringFromDate(startDate)
                        let formattedEnd = self.dateTimeFormatter.stringFromDate(endDate)
                        dateLabel.text = String(format: NSLocalizedString("%@ - %@", comment: "event start - end"), formattedStart, formattedEnd)
                    }
                } else {
                    dateLabel.text = self.dateTimeFormatter.stringFromDate(event.startDate)
                }
            }
        }
        
        loadWebView()
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        sendView("News Detail", forModuleNamed: module?.name)
    }
    
    func loadWebView() {
        var htmlStringWithFont : String
        let text = event!.description_
        let pointSize = UIFont.preferredFontForTextStyle(UIFontTextStyleBody).pointSize
        
        if AppearanceChanger.isIOS8AndRTL() {
            htmlStringWithFont = "<meta name=\"viewport\" content=\"initial-scale=1.0\" /><div style=\"font-family: -apple-system; color:black; font-size: \(pointSize); direction:rtl;\">\(text)</div>"
        } else {
            htmlStringWithFont = "<meta name=\"viewport\" content=\"initial-scale=1.0\" /><div style=\"font-family: -apple-system; color:black; font-size: \(pointSize);\">\(text)</div>"
        }
        // Replace '\n' characters with <br /> for content that isn't html based to begin with...
        // One issue is if html text also has \n characters in it. In that case we'll be changing the spacing of the content.
        htmlStringWithFont = htmlStringWithFont.stringByReplacingOccurrencesOfString("\n", withString: "<br/>")
        self.webView?.loadHTMLString(htmlStringWithFont, baseURL: nil)
    }
    
    func webView(webView: WKWebView, decidePolicyForNavigationAction navigationAction: WKNavigationAction, decisionHandler: ((WKNavigationActionPolicy) -> Void)) {
        
        if navigationAction.navigationType == .LinkActivated{
            UIApplication.sharedApplication().openURL(navigationAction.request.URL!)
            decisionHandler(.Cancel)
        } else{
            decisionHandler(.Allow)
        }
    }

    @IBAction func addToCalendar(sender: AnyObject) {
        let ekevent = EKEvent(eventStore: self.eventStore)
        ekevent.title = event!.summary
        ekevent.location = event!.location
        ekevent.startDate = event!.startDate
        ekevent.endDate = event!.endDate
        ekevent.notes = event!.description_
        ekevent.allDay = event!.allDay.boolValue
        
        let eventController = EKEventEditViewController()
        eventController.eventStore = self.eventStore
        eventController.event = ekevent
        eventController.editViewDelegate = self
        
        self.sendEventWithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionInvoke_Native, withLabel: "Add to Calendar", withValue: nil, forModuleNamed: self.module!.name)

        let status = EKEventStore.authorizationStatusForEntityType(.Event)
        switch status {
        case .Authorized:
            dispatch_async(dispatch_get_main_queue(), { () -> Void in
                self.presentViewController(eventController, animated: true, completion: nil)
            })
            
        case .NotDetermined:
            self.eventStore.requestAccessToEntityType(EKEntityType.Event, completion: { (granted, error) -> Void in
                if granted {
                    dispatch_async(dispatch_get_main_queue(), { () -> Void in
                        self.presentViewController(eventController, animated: true, completion: nil)
                    })
                }
            })
        case .Denied, .Restricted:
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
    
    func eventEditViewController(controller: EKEventEditViewController,
        didCompleteWithAction action: EKEventEditViewAction) {
            self.dismissViewControllerAnimated(true, completion: nil)
    }
    
    @IBAction func share(sender: UIBarButtonItem) {
        
        let itemsToShare : [AnyObject]
        let activities : [UIActivity]?
        

        itemsToShare = [self.event!.summary]
        activities = nil

        
        let activityVC = UIActivityViewController(activityItems: itemsToShare, applicationActivities: activities)
        activityVC.popoverPresentationController?.barButtonItem = sender
        activityVC.completionWithItemsHandler = {
            (activityType, success, returnedItems, error) in
            let label = "Tap Share Icon - \(activityType)"
            self.sendEventWithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionInvoke_Native, withLabel: label, withValue: nil, forModuleNamed: self.module!.name)
        }
        
        self.presentViewController(activityVC, animated: true, completion: nil)
        
    }
    
    
    func webView(webView: UIWebView, shouldStartLoadWithRequest request: NSURLRequest, navigationType: UIWebViewNavigationType) -> Bool {
        if navigationType == UIWebViewNavigationType.LinkClicked {
            UIApplication.sharedApplication().openURL(request.URL!)
            return false;
        }
        return true;
    }
    
    func isSameDate(start: NSDate, end:NSDate) -> Bool {
        let calendar = NSCalendar.currentCalendar()
        let componentsForStartDate = calendar.components([.Year, .Month, .Day], fromDate: start)
        //end date is not inclusive so remove a second
        let includiveEnd = end.dateByAddingTimeInterval(-1);
        let componentsForEndDate = calendar.components([.Year, .Month, .Day], fromDate: includiveEnd)
        
       
        if componentsForStartDate.year == componentsForEndDate.year && componentsForStartDate.month == componentsForEndDate.month && componentsForStartDate.day == componentsForEndDate.day {
            return true
        } else {
            return false
        }
    }
}
