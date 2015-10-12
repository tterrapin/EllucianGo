//
//  GradesTermFilterViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 9/10/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class GradesTermFilterViewController : UITableViewController {
    
    var module : Module?
    var delegate : GradesTermSelectorDelegate?
    var terms : [GradeTerm]?
    
    override func viewDidLoad() {
        super.viewDidLoad()

        tableView.estimatedRowHeight = 44
        tableView.rowHeight = UITableViewAutomaticDimension
        
        let managedObjectContext = CoreDataManager.shared.managedObjectContext
        let request = NSFetchRequest()
        request.entity = NSEntityDescription.entityForName("GradeTerm", inManagedObjectContext: managedObjectContext)
        request.sortDescriptors = [NSSortDescriptor(key: "startDate", ascending: false)]
        terms = try? managedObjectContext.executeFetchRequest(request) as! [GradeTerm]
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        sendView("Grades Term Filter", forModuleNamed: self.module?.name)
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
        if let _ = self.delegate {
            self.delegate!.term = terms![indexPath.row]
            self.dismissViewControllerAnimated(true, completion: nil)

        }
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("Term Cell", forIndexPath: indexPath) as UITableViewCell
        
        let term = terms![indexPath.row]
        
        let titleLabel = cell.viewWithTag(1) as! UILabel
        
        titleLabel.text = term.name
        return cell
    }
}
