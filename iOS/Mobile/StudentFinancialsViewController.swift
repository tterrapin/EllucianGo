//
//  StudentFinancialsViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 3/23/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class StudentFinancialsViewController: UIViewController, UITableViewDataSource, UITableViewDelegate {
    
    var module: Module!
    
    @IBOutlet var bottomViewHeightConstraint: NSLayoutConstraint!
    @IBOutlet var linkButton: UIButton!
    @IBOutlet var balanceLabel: UILabel!
    @IBOutlet var tableView: UITableView!
    
    var transactions: [StudentFinancialsTransaction] = []
    
    let parsingDateFormatter: NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        formatter.timeZone = NSTimeZone(name:"UTC")
        return formatter
        
        }()
    let displayDateFormatter: NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateStyle = .MediumStyle
        return formatter
        
        }()
    
    let currencyFormatter: NSNumberFormatter = {
        var formatter = NSNumberFormatter()
        formatter.numberStyle = .CurrencyStyle
        return formatter
        }()
    
    override func viewDidLoad() {
        self.title = self.module.name
        let linkLabel = self.module.propertyForKey("externalLinkLabel")
        let linkUrl = self.module.propertyForKey("externalLinkUrl")
        if linkLabel == nil || linkUrl == nil {
            linkButton.hidden = true
            bottomViewHeightConstraint.constant = 0;
        } else {
            linkButton.setTitle(linkLabel, forState: .Normal)
            linkButton.setTitle(linkLabel, forState: .Selected)
            linkButton.addBorderAndColor()
        }
        
        tableView.estimatedRowHeight = 100
        tableView.rowHeight = UITableViewAutomaticDimension
        self.fetchData()
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        self.sendView("View Account Balance", forModuleNamed:self.module.name)
    }
    
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return transactions.count > 0 ? transactions.count : 1
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        if transactions.count > 0 {
            let cell = tableView.dequeueReusableCellWithIdentifier("Recent Payment Cell", forIndexPath: indexPath) as UITableViewCell
            
            let transaction = transactions[indexPath.row]
            let descriptionLabel = cell.viewWithTag(1) as! UILabel
            descriptionLabel.text = transaction.description
            let dateLabel = cell.viewWithTag(2) as! UILabel
            dateLabel.text = displayDateFormatter.stringFromDate(transaction.entryDate)
            let amountLabel = cell.viewWithTag(3) as! UILabel
            amountLabel.text = currencyFormatter.stringFromNumber(transaction.amount)
            
            
            return cell
        } else {
            return tableView.dequeueReusableCellWithIdentifier("No Transactions Cell", forIndexPath: indexPath) as UITableViewCell
        }
    }
    
    func tableView(tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        
        let view = UIView(frame: CGRectMake(0, 0, CGRectGetWidth(tableView.frame), 30))
        let label = UILabel(frame: CGRectMake(8,0,CGRectGetWidth(tableView.frame), 30))
        label.translatesAutoresizingMaskIntoConstraints = false
        
        label.text =  NSLocalizedString("RECENT PAYMENTS", comment: "Table section header RECENT PAYMENTS")
        
        label.textColor = UIColor(red: 0.2, green: 0.2, blue: 0.2, alpha: 1.0)
        view.backgroundColor = UIColor(rgba: "#e6e6e6")
        label.font = UIFont.preferredFontForTextStyle(UIFontTextStyleHeadline)
        
        view.addSubview(label)
        
        let viewsDictionary = ["label": label, "view": view]
        
        // Create and add the vertical constraints
        view.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("V:|-1-[label]-1-|",
            options: NSLayoutFormatOptions.AlignAllBaseline,
            metrics: nil,
            views: viewsDictionary))
        
        // Create and add the horizontal constraints
        view.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("|-20-[label]",
            options: NSLayoutFormatOptions.AlignAllBaseline,
            metrics: nil,
            views: viewsDictionary))
        return view;
        
    }
    
    func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 30
    }

    func fetchData() {
        let loadingNotification = MBProgressHUD.showHUDAddedTo(self.view, animated: true)
        loadingNotification.labelText = NSLocalizedString("Loading", comment: "loading message while fetching recent transactions");
        
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT,0)) {
            
            self.fetchTransactions()
            self.fetchBalance()
            
            dispatch_async(dispatch_get_main_queue()) {
                self.tableView.reloadData()
                MBProgressHUD.hideAllHUDsForView(self.view, animated: true)
            }
        }
    }
    
    func fetchTransactions() {
        
        let urlBase = self.module.propertyForKey("financials")!
        let userid =  CurrentUser.sharedInstance().userid.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())
        let urlString = "\(urlBase)/\(userid!)/transactions"
        let url: NSURL? = NSURL(string: urlString as String)
        
        UIApplication.sharedApplication().networkActivityIndicatorVisible = true
        
        let authenticatedRequest = AuthenticatedRequest()
        let responseData:NSData? = authenticatedRequest.requestURL(url, fromView: self)
        
        UIApplication.sharedApplication().networkActivityIndicatorVisible = false
        if let response = responseData {
            
            let json = JSON(data: response)
            let termsList: Array<JSON> = json["terms"].arrayValue
            currencyFormatter.currencyCode = json["currencyCode"].stringValue

            for termDictioanry in termsList {
                for transactionDictioanry in termDictioanry["transactions"].arrayValue {
                    let amount = transactionDictioanry["amount"].floatValue
                    let description = transactionDictioanry["description"].stringValue
                    let date = self.parsingDateFormatter.dateFromString(transactionDictioanry["entryDate"].stringValue)
                    let type = transactionDictioanry["type"].stringValue
                    let transaction = StudentFinancialsTransaction(amount: amount, description: description, entryDate: date!, type: type)
                    self.transactions.append(transaction)
                }
            }
            self.transactions.sortInPlace {
                item1, item2 in
                let date1 = item1.entryDate as NSDate
                let date2 = item2.entryDate as NSDate
                return date1.compare(date2) == NSComparisonResult.OrderedDescending
            }
        }
    }
    
    func fetchBalance() {
        
        let urlString = NSString( format:"%@/%@/balances", self.module.propertyForKey("financials"), CurrentUser.sharedInstance().userid )
        let url: NSURL? = NSURL(string: urlString as String)

        UIApplication.sharedApplication().networkActivityIndicatorVisible = true
        
        let authenticatedRequest = AuthenticatedRequest()
        let responseData:NSData? = authenticatedRequest.requestURL(url, fromView: self)
        
        UIApplication.sharedApplication().networkActivityIndicatorVisible = false
        if let response = responseData {
            
            let json = JSON(data: response)
            let termsList: Array<JSON> = json["terms"].arrayValue
            
            for termDictionary in termsList {
                let balance = termDictionary["balance"].floatValue
                balanceLabel.text = currencyFormatter.stringFromNumber(balance);
            }
        }
    }
    @IBAction func gotoLink(sender: UIButton) {
        self.sendEventWithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionButton_Press, withLabel: "Open financial service", withValue: nil, forModuleNamed: self.module.name)
        let external = self.module.propertyForKey("external")
        if external != nil && external == "true" {
            let url = NSURL(string: self.module.propertyForKey("externalLinkUrl"))
            UIApplication.sharedApplication().openURL(url!)
        } else {
            self.performSegueWithIdentifier("Take action", sender: nil)
        }
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject!) {
        if segue.identifier == "Take action"{
            let vc = segue.destinationViewController as! WebViewController
            vc.loadRequest = NSURLRequest(URL: NSURL(string: self.module.propertyForKey("externalLinkUrl"))!)
            vc.title = self.module.propertyForKey("externalLinkLabel")
            vc.analyticsLabel = self.module.propertyForKey("externalLinkLabel")
        }
    }
}