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
        }
        
        if CurrentUser.sharedInstance().isLoggedIn {
            self.fetchData()
        }
        
        NSNotificationCenter.defaultCenter().addObserver(self,
            selector: "fetchData",
            name: kLoginExecutorSuccess,
            object: nil)
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
            let cell = tableView.dequeueReusableCellWithIdentifier("Recent Payment Cell", forIndexPath: indexPath) as! UITableViewCell
            
            let transaction = transactions[indexPath.row]
            var descriptionLabel = cell.viewWithTag(1) as! UILabel
            descriptionLabel.text = transaction.description
            var dateLabel = cell.viewWithTag(2) as! UILabel
            dateLabel.text = displayDateFormatter.stringFromDate(transaction.entryDate)
            var amountLabel = cell.viewWithTag(3) as! UILabel
            amountLabel.text = currencyFormatter.stringFromNumber(transaction.amount)
            
            
            return cell
        } else {
            return tableView.dequeueReusableCellWithIdentifier("No Transactions Cell", forIndexPath: indexPath) as! UITableViewCell
        }
    }
    
    func tableView(tableView: UITableView, willDisplayHeaderView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.contentView.backgroundColor = UIColor.accentColor()
        header.textLabel.textColor = UIColor.subheaderTextColor()
    }
    
    func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        return NSLocalizedString("RECENT PAYMENTS", comment: "Table section header RECENT PAYMENTS")
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
        
        var urlString = NSString( format:"%@/%@/transactions", self.module.propertyForKey("financials")!, CurrentUser.sharedInstance().userid )
        var url: NSURL? = NSURL(string: urlString as String)
        
        var error:NSError?
        UIApplication.sharedApplication().networkActivityIndicatorVisible = true
        
        var authenticatedRequest = AuthenticatedRequest()
        var responseData:NSData? = authenticatedRequest.requestURL(url, fromView: self)
        
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
            self.transactions.sort {
                item1, item2 in
                let date1 = item1.entryDate as NSDate
                let date2 = item2.entryDate as NSDate
                return date1.compare(date2) == NSComparisonResult.OrderedDescending
            }
        }
    }
    
    func fetchBalance() {
        
        var urlString = NSString( format:"%@/%@/balances", self.module.propertyForKey("financials"), CurrentUser.sharedInstance().userid )
        var url: NSURL? = NSURL(string: urlString as String)
        
        var error:NSError?
        UIApplication.sharedApplication().networkActivityIndicatorVisible = true
        
        var authenticatedRequest = AuthenticatedRequest()
        var responseData:NSData? = authenticatedRequest.requestURL(url, fromView: self)
        
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