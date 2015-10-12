//
//  GradesTermTableViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 8/12/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

protocol GradesTermSelectorDelegate {
    var term : GradeTerm? { get set }
}

class GradesTermTableViewController : UITableViewController, GradesTermSelectorDelegate, NSFetchedResultsControllerDelegate {
    
    var module : Module?
    @IBOutlet var termsButton: UIBarButtonItem! //iPhone Only
    var term : GradeTerm?
    let dateFormatterLastUpdatedHeader : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateStyle = .ShortStyle
        formatter.timeStyle = .ShortStyle
        return formatter
        }()
    var _fetchedResultsController: NSFetchedResultsController?
    
    override func viewDidLoad() {
        termsButton.enabled = false
        if let splitViewController = splitViewController {
            //iPad
            self.navigationController!.topViewController!.navigationItem.leftBarButtonItem = splitViewController.displayModeButtonItem()
            self.navigationController!.topViewController!.navigationItem.leftItemsSupplementBackButton = true
            
            var rightButtonItems = self.navigationItem.rightBarButtonItems
            rightButtonItems?.removeFirst()
            self.navigationItem.rightBarButtonItems = rightButtonItems
        } else {
            //iPhone
            sendView("Grades List", forModuleNamed: self.module?.name)
            if let sections = fetchedResultsController.sections {
                let currentSection = sections[0] as NSFetchedResultsSectionInfo
                let count = currentSection.numberOfObjects
                if count > 0 {
                    self.selectFirst()
                }
            }

            fetchGrades()
        }
        
        tableView.estimatedRowHeight = 44
        tableView.rowHeight = UITableViewAutomaticDimension
    }
    
    override func viewWillAppear(animated: Bool) {
        if let _ = term {
            self.tableView.reloadData()
        }
    }
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        if term == nil {
            return 0
        }
        if let courses = term!.courses {
            return courses.count + 1
        } else {
            return 1
        }
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch section {
        case 0:
            if term!.courses?.count > 0 {
                return 1
            } else {
                return 2
            }
        default:
            let course = term!.courses[section - 1] as! GradeCourse
            let count = course.grades.count
            if count == 0 {
                return 2
            }
            else {
                return count + 1
            }
        }
    }
    
    override func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell, forRowAtIndexPath indexPath: NSIndexPath) {
        tableView.separatorInset = UIEdgeInsetsZero
        tableView.layoutMargins = UIEdgeInsetsZero
        cell.layoutMargins = UIEdgeInsetsZero
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        switch (indexPath.section, indexPath.row) {
        case (0, 0):
            let cell = tableView.dequeueReusableCellWithIdentifier("Term Name Cell", forIndexPath: indexPath) as UITableViewCell
            
            let termLabel = cell.viewWithTag(1) as! UILabel
            
            termLabel.text = term!.name
            return cell
        case (_, 0):
            let cell = tableView.dequeueReusableCellWithIdentifier("Course Name Cell", forIndexPath: indexPath) as UITableViewCell
            
            let course = term!.courses[indexPath.section - 1] as! GradeCourse
            let courseLabel = cell.viewWithTag(1) as! UILabel
            let titleLabel = cell.viewWithTag(2) as! UILabel
            courseLabel.text = String(format: NSLocalizedString("%@-%@", comment: "course name - course section number"), course.courseName, course.courseSectionNumber)
            titleLabel.text = course.sectionTitle
            return cell
        
        default :
            if let courses = term!.courses where courses.count > 0 {
                let course = term!.courses[indexPath.section - 1] as! GradeCourse
                if course.grades.count > 0 {
                    let cell = tableView.dequeueReusableCellWithIdentifier("Grade Cell", forIndexPath: indexPath) as UITableViewCell
                    let grade = course.grades[indexPath.row - 1] as! Grade
                    
                    let descriptionLabel = cell.viewWithTag(1) as! UILabel
                    let lastUpdatedLabel = cell.viewWithTag(2) as! UILabel
                    let gradeLabel = cell.viewWithTag(3) as! UILabel
                    
                    descriptionLabel.text = grade.name
                    
                    if let date = grade.lastUpdated {
                        let formattedDate = self.dateFormatterLastUpdatedHeader.stringFromDate(date)
                        lastUpdatedLabel.text = String(format: NSLocalizedString("Last Updated %@", comment: "Last Updated date"), formattedDate)
                    } else {
                        lastUpdatedLabel.text = NSLocalizedString("Last Updated Unknown", comment: "Last Updated date unknown")
                    }
                    
                    gradeLabel.text = grade.value
                    return cell
                } else {
                    return tableView.dequeueReusableCellWithIdentifier("No Grades Cell", forIndexPath: indexPath) as UITableViewCell
                }

            } else {
                 return tableView.dequeueReusableCellWithIdentifier("No Grades Cell", forIndexPath: indexPath) as UITableViewCell
            }
            
        }
    
    }
    
    func fetchGrades() {
        let operation = GradesFetchOperation(module: module, view: self)
        NSOperationQueue.mainQueue().addOperation(operation)
        
        if self.fetchedResultsController.fetchedObjects!.count <= 0 {
            let hud = MBProgressHUD.showHUDAddedTo(self.view, animated: true)
            hud.labelText = NSLocalizedString("Loading", comment: "loading message while waiting for data to load")
        }
        
    }
    
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "Show Filter" {
            let detailController = (segue.destinationViewController as! UINavigationController).topViewController as! GradesTermFilterViewController
            detailController.delegate = self
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
    
    func controllerDidChangeContent(controller: NSFetchedResultsController) {
        dispatch_async(dispatch_get_main_queue()) {
            self.selectFirst()
            MBProgressHUD.hideHUDForView(self.view, animated: true)
        }
    }
    
    func selectFirst() {
        let managedObjectContext = CoreDataManager.shared.managedObjectContext
        let request = NSFetchRequest()
        request.entity = NSEntityDescription.entityForName("GradeTerm", inManagedObjectContext: managedObjectContext)
        request.sortDescriptors = [NSSortDescriptor(key: "startDate", ascending: false)]
        let terms = try? managedObjectContext.executeFetchRequest(request) as! [GradeTerm]
        if let terms = terms where terms.count > 0 {
            termsButton.enabled = true

            self.term = terms[0]
            self.tableView.reloadData()
        }
    }

}