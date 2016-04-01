//
//  NotificationsDetailViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 8/13/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation
import WebKit

class NotificationsDetailViewController : UIViewController, WKNavigationDelegate, UINavigationControllerDelegate {
    
    var notification : Notification?
    var module : Module?
    
    @IBOutlet var titleLabel: UILabel!
    @IBOutlet var dateLabel: UILabel!
    @IBOutlet var webContainerView: UIView!
    
    @IBOutlet var actionButton: UIButton!
    
    @IBOutlet var toolbar: UIToolbar!
    @IBOutlet var trashButton: UIBarButtonItem!
    @IBOutlet var trashFlexSpace: UIBarButtonItem!
    
    var webView: WKWebView?
    
    let dateFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateStyle = .MediumStyle
        formatter.timeStyle = .ShortStyle
        return formatter
        }()
    
    override func loadView() {
        super.loadView()
        self.webView = WKWebView()
        self.webView?.navigationDelegate = self
        self.webView?.translatesAutoresizingMaskIntoConstraints = false
        
        self.webContainerView.addSubview(self.webView!)
        
        let viewsDictionary = ["webView": webView, "webViewContainer": webContainerView]
        
        webContainerView.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("V:|-[webView]-|",
            options: NSLayoutFormatOptions.AlignAllBaseline,
            metrics: nil,
            views: viewsDictionary))
        
        // Create and add the horizontal constraints
        webContainerView.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("|-[webView]-|",
            options: NSLayoutFormatOptions.AlignAllBaseline,
            metrics: nil,
            views: viewsDictionary))
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationController!.topViewController!.navigationItem.leftBarButtonItem = splitViewController!.displayModeButtonItem()
        self.navigationController!.topViewController!.navigationItem.leftItemsSupplementBackButton = true
        
        if let label = notification!.linkLabel {
            self.actionButton.setTitle(label, forState: .Normal)
        } else {
            self.actionButton.removeFromSuperview()
        }

        if notification!.sticky.boolValue {
            let items = toolbar.items!.filter({ $0.tag == 0 })
            toolbar.setItems(items, animated: false)
        }
        
        titleLabel.text = notification!.title
        dateLabel.text = dateFormatter.stringFromDate(notification!.noticeDate)

        loadWebView()
        dispatch_async(dispatch_get_global_queue(QOS_CLASS_BACKGROUND, 0), {
            self.markNotificationRead()
        })
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        sendView("Notifications Detail", forModuleNamed: module?.name)
    }
    
    func loadWebView() {
        var htmlStringWithFont : String

        if let description = notification?.notificationDescription {
            let text = description.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet())
            
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
    }
    
    func webView(webView: WKWebView, decidePolicyForNavigationAction navigationAction: WKNavigationAction, decisionHandler: ((WKNavigationActionPolicy) -> Void)) {
        
        if navigationAction.navigationType == .LinkActivated{
            UIApplication.sharedApplication().openURL(navigationAction.request.URL!)
            decisionHandler(.Cancel)
        } else{
            decisionHandler(.Allow)
        }
    }
    
    //MARK: segue
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "Show Notification Link" {
            self.sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionFollow_web, withLabel: "Open notification in web frame", withValue: nil, forModuleNamed: self.module!.name)
            
            let detailController = segue.destinationViewController as! WebViewController
            detailController.loadRequest = NSURLRequest(URL: NSURL(string: self.notification!.hyperlink)!)
            detailController.title = self.notification!.linkLabel
            detailController.analyticsLabel = self.module?.name
        }
    }
    
    //MARK: UINavigationControllerDelegate
    func navigationController(navigationController: UINavigationController, willShowViewController viewController: UIViewController, animated: Bool) {
        viewController.viewWillAppear(animated)
    }
    
    func navigationController(navigationController: UINavigationController, didShowViewController viewController: UIViewController, animated: Bool) {
        viewController.viewDidAppear(animated)
    }
    
    //MARK: notifications API
    func markNotificationRead() {
        let urlBase = self.module!.propertyForKey("mobilenotifications")!
        let userid =  CurrentUser.sharedInstance().userid.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())
        let urlString = "\(urlBase)/\(userid!)/\(notification!.notificationId!)"
        
        let urlRequest = NSMutableURLRequest(URL: NSURL(string: urlString)!)
        urlRequest.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let authenticationMode = AppGroupUtilities.userDefaults()?.objectForKey("login-authenticationType") as? String
        if authenticationMode == nil || authenticationMode == "native" {
            urlRequest.addAuthenticationHeader()
        }
        
        urlRequest.HTTPMethod = "POST"
        
        let postDictionary = ["uuid": self.notification!.notificationId, "statuses" : "READ"]
        do {
            let jsonData = try NSJSONSerialization.dataWithJSONObject(postDictionary, options: NSJSONWritingOptions.PrettyPrinted)
            urlRequest.HTTPBody = jsonData
            
            let queue = NSOperationQueue()
            NSURLConnection.sendAsynchronousRequest(urlRequest,queue:queue,completionHandler:{response,data,error in /* code goes here */ })
            self.notification!.read = true
            try self.notification?.managedObjectContext?.save()
        } catch {
        }
        
    }
    
    //MARK: button actions
    

    @IBAction func deleteNotification(sender: UIBarButtonItem) {
        
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .ActionSheet)
        let cancelAction: UIAlertAction = UIAlertAction(title: NSLocalizedString("Cancel", comment:"Cancel"), style: .Cancel) { action -> Void in
        }
        alertController.addAction(cancelAction)
        //Create and add first option action
        let deleteAction: UIAlertAction = UIAlertAction(title: NSLocalizedString("Delete", comment:"Delete button"), style: .Destructive) { action -> Void in
            NotificationsFetcher.deleteNotification(self.notification, module:self.module!)

            if self.splitViewController!.collapsed {
                self.performSegueWithIdentifier("Show Empty", sender: nil)
                self.navigationController?.navigationController?.popToRootViewControllerAnimated(true)
            } else {
                self.performSegueWithIdentifier("Show Empty", sender: nil)
            }
        }
        alertController.addAction(deleteAction)
        alertController.popoverPresentationController?.barButtonItem = sender;
        
        //Present the AlertController
        self.presentViewController(alertController, animated: true, completion: nil)
    }
    
    @IBAction func share(sender: UIBarButtonItem) {
        
        var text = self.notification!.notificationDescription
        if let link = self.notification?.hyperlink {
            text = "\(text) \(link)"
        }
        
        let itemProvider = NotificationUIActivityItemProvider(subject: notification!.title, text: text)
        let activityVC = UIActivityViewController(activityItems: [itemProvider], applicationActivities: nil)
        activityVC.popoverPresentationController?.barButtonItem = sender
        activityVC.completionWithItemsHandler = {
            (activityType, success, returnedItems, error) in
            let label = "Tap Share Icon - \(activityType)"
            self.sendEventWithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionInvoke_Native, withLabel: label, withValue: nil, forModuleNamed: self.module!.name)
        }
        
        self.presentViewController(activityVC, animated: true, completion: nil)

    }
}