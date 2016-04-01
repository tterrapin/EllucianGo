//
//  DirectoryFilterViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 12/3/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

protocol DirectoryFilterDelegate : class {
    func updateFilter(hiddenGroups: [String])
}


class DirectoryFilterViewController : UITableViewController {
    var hiddenGroups = [String]()
    weak var delegate : DirectoryFilterDelegate?
    var module : Module?
    var groups = [DirectoryDefinitionProtocol]()
    


    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
    }
    
    //MARK table view data source
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.groups.count
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("Directory Filter Cell", forIndexPath: indexPath) as UITableViewCell

        let definition = groups[indexPath.row]
        
        let textLabel = cell.viewWithTag(1) as! UILabel
        textLabel.text = definition.displayName
        if hiddenGroups.contains(definition.internalName!) {
            cell.accessoryType = UITableViewCellAccessoryType.None
        } else {
            cell.accessoryType = .Checkmark
        }
        let lockImageView = cell.viewWithTag(2) as! UIImageView
        
        if CurrentUser.sharedInstance().isLoggedIn {
            lockImageView.hidden = true
        } else {
            lockImageView.hidden = !definition.authenticatedOnly
        }
        
        return cell
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {

        let definition = groups[indexPath.row]
        let cell = tableView.cellForRowAtIndexPath(indexPath)
        
        if cell?.accessoryType == .Checkmark {
            cell?.accessoryType = .None
            self.hiddenGroups.append(definition.internalName!)
        } else {
            cell?.accessoryType = .Checkmark
            if let index = self.hiddenGroups.indexOf(definition.internalName!) {
                self.hiddenGroups.removeAtIndex(index)
            }
        }
        
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        
        self.updateCategories()
    }
    
    func updateCategories() {
        let moduleKey = "\(module!.internalKey!)-hiddenGroups"
        
        let savedGroups = AppGroupUtilities.userDefaults()?.arrayForKey(moduleKey)

        if savedGroups == nil || savedGroups as! [String] != hiddenGroups {
            sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionList_Select, withLabel: "Filter changed", withValue: nil, forModuleNamed: self.module?.name)

        }
        AppGroupUtilities.userDefaults()?.setObject(hiddenGroups, forKey: moduleKey)

        self.delegate?.updateFilter(hiddenGroups)
    }
    
    @IBAction func dismiss(sender: AnyObject) {
        updateCategories()
        dismissViewControllerAnimated(true, completion: nil)
    }
}
