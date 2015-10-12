//
//  GradesTableViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 8/12/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class GradesTableViewController : UITableViewController , NSFetchedResultsControllerDelegate {

    var module : Module?
    let dateFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
        }()
    let datetimeFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ"
        return formatter
        }()
    var initiallyEmpty = false

    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.title = self.module?.name
        tableView.estimatedRowHeight = 44
        tableView.rowHeight = UITableViewAutomaticDimension
        
        fetchGrades()
        if let sections = fetchedResultsController.sections {
            let currentSection = sections[0] as NSFetchedResultsSectionInfo
            let count = currentSection.numberOfObjects
            if count > 0 {
                self.tableView.selectRowAtIndexPath(NSIndexPath(forRow: 0, inSection: 0), animated: true, scrollPosition: UITableViewScrollPosition.None)
                self.performSegueWithIdentifier("Show Term", sender: nil)
            } else {
                initiallyEmpty = true
            }
        }
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        sendView("Grades List", forModuleNamed: self.module?.name)
    }
    
    // MARK: data retrieval
    func fetchGrades() {
        let operation = GradesFetchOperation(module: module, view: self)
        NSOperationQueue.mainQueue().addOperation(operation)
        
        if self.fetchedResultsController.fetchedObjects!.count <= 0 {
            let hud = MBProgressHUD.showHUDAddedTo(self.view, animated: true)
            hud.labelText = NSLocalizedString("Loading", comment: "loading message while waiting for data to load")
        }
    }
    
    //MARK: segue
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "Show Term" {
            
            let detailController = (segue.destinationViewController as! UINavigationController).topViewController as! GradesTermTableViewController
            let term = fetchedResultsController.objectAtIndexPath(self.tableView.indexPathForSelectedRow!) as! GradeTerm
            detailController.term = term
            detailController.module = self.module
        }
    }
    
    // MARK: fetch
    var fetchedResultsController: NSFetchedResultsController {
        // return if already initialized
        if self._fetchedResultsController != nil {
            return self._fetchedResultsController!
        }
        let managedObjectContext = CoreDataManager.shared.managedObjectContext
        
        let request = NSFetchRequest()
        request.entity = NSEntityDescription.entityForName("GradeTerm", inManagedObjectContext: managedObjectContext)
        request.sortDescriptors = [NSSortDescriptor(key: "startDate", ascending: false)]
        
        let aFetchedResultsController = NSFetchedResultsController(fetchRequest: request, managedObjectContext: managedObjectContext, sectionNameKeyPath: nil, cacheName: nil)
        aFetchedResultsController.delegate = self
        self._fetchedResultsController = aFetchedResultsController
        
        do {
            try self._fetchedResultsController!.performFetch()
            
        } catch let error {
            print("fetch error: \(error)")
        }
        
        return self._fetchedResultsController!
    }
    var _fetchedResultsController: NSFetchedResultsController?
    
    //MARK :UITable
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if let sections = fetchedResultsController.sections {
            let currentSection = sections[section] as NSFetchedResultsSectionInfo
            return currentSection.numberOfObjects
        }
        
        return 0
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("Term Cell", forIndexPath: indexPath) as UITableViewCell
        let term = fetchedResultsController.objectAtIndexPath(indexPath) as! GradeTerm
        
        let titleLabel = cell.viewWithTag(1) as! UILabel
        
        titleLabel.text = term.name
        return cell
    }
    
    func controllerWillChangeContent(controller: NSFetchedResultsController) {
        self.tableView.beginUpdates()
    }
    
    func controller(controller: NSFetchedResultsController, didChangeObject anObject: AnyObject, atIndexPath indexPath: NSIndexPath?, forChangeType type: NSFetchedResultsChangeType, newIndexPath: NSIndexPath?) {
        
        switch type{
        case NSFetchedResultsChangeType.Insert:
            self.tableView.insertRowsAtIndexPaths([newIndexPath!], withRowAnimation: UITableViewRowAnimation.Top)
            break
        case NSFetchedResultsChangeType.Delete:
            self.tableView.deleteRowsAtIndexPaths([indexPath!], withRowAnimation: UITableViewRowAnimation.Left)
            break
        case NSFetchedResultsChangeType.Update:
            self.tableView.cellForRowAtIndexPath(indexPath!)?.setNeedsLayout()
            break
        default:
            return
        }
    }

    
    func controllerDidChangeContent(controller: NSFetchedResultsController) {
        self.tableView.endUpdates()
        
        dispatch_async(dispatch_get_main_queue()) {
            self._fetchedResultsController = nil
            self.tableView.reloadData()
            if self.initiallyEmpty {
                let currentSection = self.fetchedResultsController.sections![0] as NSFetchedResultsSectionInfo
                let count = currentSection.numberOfObjects
                if count > 0 {
                    self.tableView.selectRowAtIndexPath(NSIndexPath(forRow: 0, inSection: 0), animated: false, scrollPosition: UITableViewScrollPosition.None)
                    self.performSegueWithIdentifier("Show Term", sender: nil)
                }
            }
            MBProgressHUD.hideHUDForView(self.view, animated: true)
        }
    }
}
