//
//  TodayViewController.swift
//  AssignmentsToday
//
//  Created by Jason Hocker on 1/21/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import UIKit
import NotificationCenter

class TodayViewController: UITableViewController, NCWidgetProviding, UITableViewDataSource, UITableViewDelegate {
    
    var items : [NSDictionary]?
    var noILP : Bool = false
    var disconnected : Bool = false
    
    @IBOutlet var disconnectedButtonView: UIView!
    @IBOutlet var disconnectedFooter: UIView!
    @IBOutlet var noILPFooter: UIView!
    @IBOutlet var noItemsFooter: UIView!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        let defaults = AppGroupUtilities.userDefaults()
        items = defaults?.objectForKey("today-widget-assignments") as! [NSDictionary]?
        self.reloadTable()
        
        let url = defaults?.objectForKey("ilp-url") as! NSString?
        self.disconnected = UserInfo.userauth() == nil
        
        addButtonToDisconnectedFooter()
        fetch()

    }
    
    func widgetPerformUpdateWithCompletionHandler(completionHandler: ((NCUpdateResult) -> Void)!) {
        completionHandler(NCUpdateResult.NewData)
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if let items = self.items {
            return items.count
        }
        return 0
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var itemNumber = indexPath.row
        let cell : UITableViewCell = tableView.dequeueReusableCellWithIdentifier("Assignment Today Cell", forIndexPath: indexPath) as! UITableViewCell
        
        if let items = self.items {
            let item = items[itemNumber]
            (cell.contentView.viewWithTag(1) as! UILabel).text = item["name"] as! String?
            let courseName = item["courseName"] as! String!
            let courseSectionNumber = item["courseSectionNumber"] as! String!
            (cell.contentView.viewWithTag(2) as! UILabel).text = "\(courseName)-\(courseSectionNumber)"
        }
        return cell
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        var itemNumber = indexPath.row
        if let items = self.items {
            let item = items[itemNumber]
            let itemUrl = item["url"] as! String
            let scheme = getScheme()
            let escapedString : String! = itemUrl.stringByAddingPercentEncodingWithAllowedCharacters(.URLHostAllowedCharacterSet())
            let url = NSURL(string: "\(scheme)://module-type/ilp?url=\(escapedString)")
            self.extensionContext!.openURL(url!, completionHandler: nil)
        }
    }
    
    override func viewWillTransitionToSize(size: CGSize, withTransitionCoordinator coordinator: UIViewControllerTransitionCoordinator)
    {
        super.viewWillTransitionToSize(size, withTransitionCoordinator: coordinator)
        
        if let safeCoordinator = coordinator as UIViewControllerTransitionCoordinator?
        {
            println("coordinator != nil")
            safeCoordinator.animateAlongsideTransition({ context in
                self.tableView.frame = CGRectMake(0, 0, size.width, size.height)
                }, completion: nil)

        }
        else
        {
            println("coordinator == nil")
        }
    }
    
    func reload() {
        self.tableView.reloadData()
        self.preferredContentSize = self.tableView.contentSize

    }
    
    func filterItems(items: [NSDictionary]) -> [NSDictionary]? {
        println("Start filtering")
        let sourceToDateFormatter = NSDateFormatter()
        sourceToDateFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        sourceToDateFormatter.timeZone = NSTimeZone(forSecondsFromGMT: 0)
        
        var comparisonFormatter = NSDateFormatter()
        comparisonFormatter.dateFormat = "yyyy-MM-dd"
        comparisonFormatter.timeZone = NSTimeZone.localTimeZone()
        var dateNow = NSDate()
        
        let dateNowString = comparisonFormatter.stringFromDate(dateNow)
        
        let filteredArray = items.filter() { (item : NSDictionary) -> Bool in
            
            if let date = sourceToDateFormatter.dateFromString(item["dueDate"] as! String) {
                let dateString = comparisonFormatter.stringFromDate(date)
                return dateNowString == dateString
            } else {
                return false
            }
        }
        println("End filtering")
        return filteredArray
    }
    
    //The default it passes has a 47 point left margin and a 39 point bottom margin.
//    func widgetMarginInsetsForProposedMarginInsets(defaultMarginInsets: UIEdgeInsets) -> UIEdgeInsets {
//        return UIEdgeInsets(top: defaultMarginInsets.top, left: defaultMarginInsets.left, bottom: defaultMarginInsets.bottom, right: defaultMarginInsets.right)
////        return UIEdgeInsetsZero
//    }
    
    override func tableView(tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        
        if(self.noILP) {
            return self.noILPFooter
        }
        else if(self.disconnected) {
            return self.disconnectedFooter
        }
        else if let items = self.items {
            if(items.count == 0) {
                return self.noItemsFooter
            }
        }
        return nil
    }
    
    override func tableView(tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        var width = CGFloat(0)
        if(self.noILP) {
            width = self.noILPFooter.frame.height
        }
        else if(self.disconnected) {
            width = self.disconnectedFooter.frame.height
        }
        else if let items = self.items {
            if(items.count == 0) {
                width = self.noItemsFooter.frame.height
            }
        }
        return width
    }

    
    func imageWithCutOutString(string: NSString, size:CGSize, backgroundColor: UIColor, font: UIFont) -> UIImage? {
        if(size.width == 0) {
            return nil
        }
        let textAttributes = [NSFontAttributeName : font]
        
        let textSize = string.sizeWithAttributes(textAttributes)
        
        UIGraphicsBeginImageContextWithOptions(size, false, UIScreen.mainScreen().scale)
        let ctx = UIGraphicsGetCurrentContext()
        
        CGContextSetFillColorWithColor(ctx, backgroundColor.CGColor);
        
        let path = UIBezierPath(rect: CGRectMake(0.0, 0.0, size.width, size.height))
        CGContextAddPath(ctx, path.CGPath);
        CGContextFillPath(ctx);
        
        CGContextSetBlendMode(ctx, kCGBlendModeDestinationOut);
        let center = CGPointMake((size.width - textSize.width) / 2.0, (size.height - textSize.height) / 2.0);
        string.drawAtPoint(center, withAttributes: textAttributes)
        
        let image = UIGraphicsGetImageFromCurrentImageContext();
        
        UIGraphicsEndImageContext();
        
        return image
    }
    
    func signIn(sender: UIButton!) {
        let scheme = getScheme()
        let url = NSURL(string: "\(scheme)://module-type/ilp")
        self.extensionContext!.openURL(url!, completionHandler: nil)
    }
    
    private func getScheme() -> String {
        var scheme = "ellucianmobile"
        if let path = NSBundle.mainBundle().pathForResource("Customizations", ofType: "plist") {
            let plistDictionary = NSDictionary(contentsOfFile: path)
            if let plistDictionary = plistDictionary {
                if plistDictionary["URL Scheme"] != nil &&  plistDictionary["URL Scheme"]?.length > 0 {
                    scheme = plistDictionary["URL Scheme"] as! NSString as String
                }
            }
        }
        return scheme
    }
    
    private func addButtonToDisconnectedFooter() {
        let button = UIButton.buttonWithType(.System) as! UIButton
        button.frame = CGRectMake(0, 0, disconnectedButtonView.frame.width, disconnectedButtonView.frame.height)
        button.contentHorizontalAlignment = .Center
        button.backgroundColor = UIColor.clearColor()
        button.layer.cornerRadius = 4.0
        button.clipsToBounds = true
        
        let imageCutout = self.imageWithCutOutString(NSLocalizedString("Sign In", comment: "Sign In"), size: button.frame.size, backgroundColor: UIColor.whiteColor(), font: UIFont.boldSystemFontOfSize(14))
        button.setImage(imageCutout, forState: UIControlState.Normal)
        button.setImage(imageCutout, forState: UIControlState.Highlighted)
        button.addTarget(self, action: "signIn:", forControlEvents: .TouchUpInside)
        
        var visualEffectView = UIVisualEffectView(effect: UIVibrancyEffect.notificationCenterVibrancyEffect())
        visualEffectView.frame = CGRectMake(0, 0, disconnectedButtonView.frame.size.width, disconnectedButtonView.frame.height)
        visualEffectView.contentView.addSubview(button)
        
        disconnectedButtonView.addSubview(visualEffectView)
    }
    
    func fetch() {
        let defaults = AppGroupUtilities.userDefaults()
        let url = defaults?.objectForKey("ilp-url") as! NSString?
        println("Assignments Today widgetPerformUpdateWithCompletionHandler")
        
        items = defaults?.objectForKey("today-widget-assignments") as! [NSDictionary]?
        
        var storage : NSHTTPCookieStorage = NSHTTPCookieStorage.sharedHTTPCookieStorage()
        for cookie in storage.cookies as! [NSHTTPCookie]{
            storage.deleteCookie(cookie)
        }

        var cookiesArray : NSArray? = defaults?.objectForKey("cookieArray") as! NSArray?
        if let cookiesArray = cookiesArray {
            for cookieItem in cookiesArray  {
                let cookieDictionary = cookieItem as! [NSObject : AnyObject]
                let cookie = NSHTTPCookie(properties: cookieDictionary)
                NSHTTPCookieStorage.sharedHTTPCookieStorage().setCookie(cookie!)
            }
        }

        if let ilpUrl = url {
            println("Assignments Today url: \(ilpUrl)")
            if let username = UserInfo.userauth() {
                println("Assignments Today has username")
                let config = NSURLSessionConfiguration.defaultSessionConfiguration()
                let password = UserInfo.password()
                if let password = password {
                    let userPasswordString = "\(username):\(password)"
                    let userPasswordData = userPasswordString.dataUsingEncoding(NSUTF8StringEncoding)
                    let base64EncodedCredential = userPasswordData!.base64EncodedStringWithOptions(nil)
                    let authString = "Basic \(base64EncodedCredential)"
                    config.HTTPAdditionalHeaders = ["Authorization" : authString]
                }
                let session = NSURLSession(configuration: config)
                
                if let studentId = UserInfo.userid() {
                    println("Assignments Today has studentId")
                    let fullUrl = NSURL(string: "\(ilpUrl)/\(studentId)/assignments")!
                    
                    
                    let task = session.dataTaskWithURL(fullUrl) {
                        (let data, let response, let error) in
                        
                        if let httpRes = response as? NSHTTPURLResponse {
                            println("Assignments Today response code: \(httpRes.statusCode)")
                            if httpRes.statusCode == 200 {
                                let json = JSON(data: data)
                                
                                
                                if(json == nil) {
                                    println("Assignments Today disconnected")
                                    self.disconnected = true
                                    self.reloadTable()
                                } else {
                                    var items = [NSDictionary]()
                                    let assignmentList: Array<JSON> = json["assignments"].arrayValue
                                    
                                    for assignmentJson in json["assignments"].arrayValue {
                                        items.append(["sectionId" : assignmentJson["sectionId"].stringValue,
                                            "courseName": assignmentJson["courseName"].stringValue, "courseSectionNumber": assignmentJson["courseSectionNumber"].stringValue, "name": assignmentJson["name"].stringValue, "assignmentDescription": assignmentJson["description"].stringValue, "dueDate": assignmentJson["dueDate"].stringValue, "url": assignmentJson["url"].stringValue])
                                    }

                                    self.items = self.filterItems(items)
                                    
                                    self.reloadTable()
                                    defaults?.setObject(self.items, forKey: "today-widget-assignments")
                                    println("Assignments Today count: \(self.items!.count)")
                                }
                            } else {
                                self.disconnected = true
                                self.reloadTable()
                            }
                        } else {
                            println("error \(error)")
                        }
                    }
                    task.resume()
                }
            } else {
                println("Assignments Today disconnected")
                self.disconnected = true
                reloadTable()
            }
        } else {
            println("Assignments Today no ILP")
            self.noILP = true
            reloadTable()
        }
    }
    
    func reloadTable() {
        println("Start reload")
        dispatch_async(dispatch_get_main_queue()) {
            self.reload()
            println("End reload")
        }
    }
}
