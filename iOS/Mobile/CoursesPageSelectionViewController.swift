//
//  CoursesPageSelectionViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 10/15/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

protocol ScheduleTermSelectedDelegate : class {
    var selectedTerm : Int { get set }
    func loadSchedule()
}


class CoursesPageSelectionViewController : UITableViewController {
    var terms : [CourseTerm]?
    var coursesChangePageDelegate : ScheduleTermSelectedDelegate?
    var module : Module?
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.estimatedRowHeight = 44
        tableView.rowHeight = UITableViewAutomaticDimension
        
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        sendView("Term List", forModuleNamed: self.module?.name)
    }
    
    //MARK :UITable
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if let terms = terms {
            return terms.count
        } else {
            return 0
        }
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        if let delegate = self.coursesChangePageDelegate {
            delegate.selectedTerm = indexPath.row
            delegate.loadSchedule()
            self.dismissViewControllerAnimated(true, completion: nil)
            
        }
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("Courses Term Selection Cell", forIndexPath: indexPath) as UITableViewCell
        
        let term = terms![indexPath.row]
        
        let titleLabel = cell.viewWithTag(1) as? UILabel
        
        titleLabel!.text = term.name
        return cell
    }
    
    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return UITableViewAutomaticDimension
    }
}
