//
//  CourseGradesViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 10/29/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class CourseGradesViewController : UITableViewController, NSFetchedResultsControllerDelegate {
    var termId : String?
    var sectionId : String?
    var courseNameAndSectionNumber : String?
    var module : Module?
    var _fetchedResultsController : NSFetchedResultsController?
    
    let dateFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
    }()
    let datetimeFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        formatter.timeZone = NSTimeZone(name:"UTC")
        return formatter
    }()
    let datetimeOutputFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateStyle = .ShortStyle
        formatter.timeStyle = .ShortStyle
        return formatter
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationController!.navigationBar.translucent = false
        try! self.fetchedResultsController.performFetch()
        self.navigationItem.title = self.courseNameAndSectionNumber
        if CurrentUser.sharedInstance().isLoggedIn {
            self.fetchGrades(self)
        }
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "fetchGrades:", name: kLoginExecutorSuccess, object: nil)
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        self.sendView("Course grades", forModuleNamed: self.module!.name)
    }
    
    var fetchedResultsController: NSFetchedResultsController {
        // return if already initialized
        if self._fetchedResultsController != nil {
            return self._fetchedResultsController!
        }
        let managedObjectContext = self.module!.managedObjectContext!
        
        let request = NSFetchRequest()
        request.entity = NSEntityDescription.entityForName("Grade", inManagedObjectContext: managedObjectContext)
        request.predicate = NSPredicate(format: "course.sectionId == %@ and course.term.termId == %@", self.sectionId!, self.termId!)
        request.sortDescriptors = [NSSortDescriptor(key: "lastUpdated", ascending: false)]
        
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
    
    @IBAction func dismiss(sender: AnyObject) {
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if let sections = fetchedResultsController.sections {
            let currentSection = sections[section] as NSFetchedResultsSectionInfo
            let count = currentSection.numberOfObjects
            if count > 0 {
                return count
            } else {
                return 1
            }
        }
        
        return 1
    }
    
    
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        if fetchedResultsController.fetchedObjects?.count > 0 {
            
            let grade = fetchedResultsController.objectAtIndexPath(indexPath)
            let cell = tableView.dequeueReusableCellWithIdentifier("Grade Cell", forIndexPath: indexPath) as UITableViewCell
            
            
            let descriptionLabel = cell.viewWithTag(1) as! UILabel
            let lastUpdatedLabel = cell.viewWithTag(2) as! UILabel
            let gradeLabel = cell.viewWithTag(3) as! UILabel
            
            descriptionLabel.text = grade.name
            
            if let date = grade.lastUpdated where date != nil {
                let formattedDate = self.datetimeOutputFormatter.stringFromDate(date)
                lastUpdatedLabel.text = String(format: NSLocalizedString("Last Updated %@", comment: "Last Updated date"), formattedDate)
            } else {
                lastUpdatedLabel.text = NSLocalizedString("Last Updated Unknown", comment: "Last Updated date unknown")
            }
            
            gradeLabel.text = grade.value
            return cell
        } else {
            return tableView.dequeueReusableCellWithIdentifier("No Grades Cell", forIndexPath: indexPath) as UITableViewCell
        }
    }

    
    func fetchGrades(sender: AnyObject) {
        
        
        if let userid = CurrentUser.sharedInstance().userid {
            let urlBase = self.module?.propertyForKey("grades")
            let escapedUserId = userid.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())
            let escapedTermId = self.termId?.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())
            let escapedSectionId = self.sectionId?.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())
            let urlString = "\(urlBase!)/\(escapedUserId!)?term=\(escapedTermId!)&section=\(escapedSectionId!)"
            
            if self.fetchedResultsController.fetchedObjects!.count <= 0 {
                let hud = MBProgressHUD.showHUDAddedTo(self.view, animated: true)
                hud.labelText = NSLocalizedString("Loading", comment: "loading message while waiting for data to load")
            }
            
            let privateContext = NSManagedObjectContext(concurrencyType: .PrivateQueueConcurrencyType)
            privateContext.parentContext = self.module!.managedObjectContext
            privateContext.undoManager = nil
            
            privateContext.performBlock { () -> Void in
                
                do {
                    UIApplication.sharedApplication().networkActivityIndicatorVisible = true
                    
                    defer {
                        dispatch_async(dispatch_get_main_queue()) {
                            
                            UIApplication.sharedApplication().networkActivityIndicatorVisible = false
                            MBProgressHUD.hideHUDForView(self.view, animated: true)
                        }
                    }
                    
                    let authenticatedRequest = AuthenticatedRequest()
                    let responseData = authenticatedRequest.requestURL(NSURL(string: urlString)!, fromView: self)
                    let json = JSON(data: responseData)
                    UIApplication.sharedApplication().networkActivityIndicatorVisible = false
                    
                    let request = NSFetchRequest(entityName: "GradeTerm")
                    request.predicate = NSPredicate(format: "termId == %@", self.termId!)
                    let terms = try privateContext
                        .executeFetchRequest(request) as! [GradeTerm]
                    
                    if let jsonTerms = json["terms"].array {
                        for jsonTerm in jsonTerms {
                            
                            
                            
                            let filtered = terms.filter {
                                let x = $0 as GradeTerm
                                return x.termId == self.termId
                            }
                            let gradeTerm : GradeTerm
                            if filtered.count > 0 {
                                gradeTerm = filtered.first!
                            } else {
                                gradeTerm = NSEntityDescription.insertNewObjectForEntityForName("GradeTerm", inManagedObjectContext: privateContext) as! GradeTerm
                                gradeTerm.termId = jsonTerm["id"].string
                                gradeTerm.name = jsonTerm["name"].string
                                gradeTerm.startDate = self.dateFormatter.dateFromString(jsonTerm["startDate"].string!)
                                gradeTerm.endDate = self.dateFormatter.dateFromString(jsonTerm["endDate"].string!)
                                
                                
                            }
                            if let jsonSections = jsonTerm["sections"].array {
                                for jsonSection in jsonSections {
                                    let courses = gradeTerm.courses
                                    let filtered = courses!.filter {
                                        let x = $0 as! GradeCourse
                                        return x.sectionId == self.sectionId
                                    }
                                    let gradeCourse : GradeCourse
                                    if filtered.count > 0 {
                                        gradeCourse = filtered.first as! GradeCourse
                                    } else {
                                        gradeCourse = NSEntityDescription.insertNewObjectForEntityForName("GradeCourse", inManagedObjectContext: privateContext) as! GradeCourse
                                        gradeCourse.sectionId = jsonSection["sectionId"].string
                                        gradeCourse.courseName = jsonSection["courseName"].string
                                        gradeCourse.sectionTitle = jsonSection["sectionTitle"].string
                                        gradeCourse.courseSectionNumber = jsonSection["courseSectionNumber"].string
                                        gradeCourse.term = gradeTerm
                                        gradeTerm.addCoursesObject(gradeCourse)
                                    }
                                    
                                    for oldObject in gradeCourse.grades {
                                        privateContext.deleteObject(oldObject as! Grade)
                                    }
                                    
                                    if let jsonGrades = jsonSection["grades"].array {
                                        for jsonGrade in jsonGrades {
                                            let grade = NSEntityDescription.insertNewObjectForEntityForName("Grade", inManagedObjectContext: privateContext) as! Grade
                                            grade.name = jsonGrade["name"].string
                                            grade.value = jsonGrade["value"].string
                                            if let lastUpdated = jsonGrade["updated"].string {
                                                grade.lastUpdated = self.datetimeFormatter.dateFromString(lastUpdated)
                                            }
                                            gradeCourse.addGradesObject(grade)
                                            grade.course = gradeCourse
                                            
                                        }
                                    }
                                    
                                }
                            }
                        }
                    }
                    try privateContext.save()
                    
                    privateContext.parentContext?.performBlock({
                        do {
                            try privateContext.parentContext?.save()
                        } catch let error {
                            print (error)
                        }
                    })
                    
                    dispatch_async(dispatch_get_main_queue(), {
                        self._fetchedResultsController = nil;
                        try! self.fetchedResultsController.performFetch()
                        self.tableView.reloadData()
                    })
                    
                } catch let error {
                    print (error)
                }
                
            }
        }
        
    }
    
    
}