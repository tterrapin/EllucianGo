//
//  FeedFilterViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 7/31/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

protocol FeedFilterDelegate : class {
    func reloadData()
}

class FeedFilterViewController : UITableViewController {
    
    var startingHiddenCategories : NSMutableSet?
    var categories : NSArray?
    
    var feedModule : FeedModule?
    var hiddenCategories : NSMutableSet?
    var module : Module?
    weak var delegate : FeedFilterDelegate?
    
    override func viewDidLoad() {
        self.startingHiddenCategories = self.hiddenCategories
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        self.initializeCategories()
    }
    
    //MARK table view data source
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.categories!.count
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("Feed Filter Cell", forIndexPath: indexPath) as UITableViewCell
        
        let category = self.categories?.objectAtIndex(indexPath.row) as! String
        
        let textLabel = cell.viewWithTag(1) as! UILabel
        textLabel.text = category
        if (self.hiddenCategories!.containsObject(category) ) {
            cell.accessoryType = UITableViewCellAccessoryType.None
        } else {
            cell.accessoryType = .Checkmark
        }
        
        return cell
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        let cell = tableView.cellForRowAtIndexPath(indexPath)
        let textLabel = cell?.viewWithTag(1) as! UILabel
        
        if cell?.accessoryType == .Checkmark {
            cell?.accessoryType = .None
            self.hiddenCategories?.addObject(textLabel.text!)
        } else {
            cell?.accessoryType = .Checkmark
            //self.hiddenCategories = self.hiddenCategories!.filter() { $0 as? String != textLabel.text }
            self.hiddenCategories?.removeObject(textLabel.text!)
        }
        
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        
        self.updateCategories()
        self.delegate?.reloadData()
    }
    
    func updateCategories() {
        if hiddenCategories!.count > 0 {
            self.feedModule?.hiddenCategories = (hiddenCategories!.allObjects as! [String]).joinWithSeparator(",")
        } else {
            self.feedModule?.hiddenCategories = nil
        }
        
        if self.startingHiddenCategories!.isEqual(self.hiddenCategories) {
            sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionList_Select, withLabel: "Filter changed", withValue: nil, forModuleNamed: self.module?.name)
        }
        do {
            try feedModule?.managedObjectContext?.save()
        } catch {
            
        }
    }
    
    @IBAction func dismiss(sender: AnyObject) {
        updateCategories()
        dismissViewControllerAnimated(true, completion: nil)
    }
    func initializeCategories() {

        do {
            let request = NSFetchRequest(entityName: "FeedCategory")
            request.predicate = NSPredicate(format: "moduleName = %@", self.module!.name)
            let results = try self.module?.managedObjectContext?.executeFetchRequest(request) as! [FeedCategory]
            let categories = results.map {
                    return $0.name
            }
            self.categories = categories.sort { $0.localizedCaseInsensitiveCompare($1) == NSComparisonResult.OrderedDescending }
        } catch {
            
        }
    }
}