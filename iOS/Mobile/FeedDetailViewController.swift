//
//  FeedDetailViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 7/31/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation
import WebKit

class FeedDetailViewController: UIViewController, UIWebViewDelegate {
    
    var feed : Feed?
    var module : Module?
    
    @IBOutlet var titleLabel: UILabel!
    @IBOutlet var dateLabel: UILabel!
    @IBOutlet var imageView: UIImageView!
    
 
    @IBOutlet var webView: UIWebView!
    
    let dateFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateStyle = .MediumStyle
        formatter.timeStyle = .ShortStyle
        return formatter
        }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationController!.topViewController!.navigationItem.leftBarButtonItem = splitViewController!.displayModeButtonItem()
        self.navigationController!.topViewController!.navigationItem.leftItemsSupplementBackButton = true
        
        titleLabel.text = feed!.title
        dateLabel.text = dateFormatter.stringFromDate(feed!.postDateTime)

        if let logo = feed!.logo where logo != "" {
//            imageView.convertToCircleImage()
            imageView.loadImagefromURL(logo)
        }

        webView.delegate = self;
        loadWebView()
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        sendView("News Detail", forModuleNamed: module?.name)
    }
    
    func loadWebView() {
        var htmlStringWithFont : String
        let text : String
        let link = feed!.link.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet())
        if link.characters.count > 0 {
            text = "\(feed!.content)<br><br>\(link)"
        } else {
            text = feed!.content
        }
        
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
    
    func webView(webView: UIWebView, shouldStartLoadWithRequest request: NSURLRequest, navigationType: UIWebViewNavigationType) -> Bool {
        if navigationType == UIWebViewNavigationType.LinkClicked {
            UIApplication.sharedApplication().openURL(request.URL!)
            return false;
        }
        return true;
    }
    
    @IBAction func share(sender: UIBarButtonItem) {
    
        let itemsToShare : [AnyObject]
        let activities : [UIActivity]?
        
        if let link = self.feed!.link, let url = NSURL(string: self.feed!.link) where link != "" {
            itemsToShare = [url]
            activities = [SafariActivity()]
        } else {
            itemsToShare = [self.feed!.title]
            activities = nil
        }
        
        let activityVC = UIActivityViewController(activityItems: itemsToShare, applicationActivities: activities)
        activityVC.popoverPresentationController?.barButtonItem = sender
        activityVC.completionWithItemsHandler = {
            (activityType, success, returnedItems, error) in
            let label = "Tap Share Icon - \(activityType)"
            self.sendEventWithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionInvoke_Native, withLabel: label, withValue: nil, forModuleNamed: self.module!.name)
        }

        self.presentViewController(activityVC, animated: true, completion: nil)

    }
}