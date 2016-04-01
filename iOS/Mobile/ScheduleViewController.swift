//
//  ScheduleViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 10/14/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class ScheduleViewController : UITableViewController, ScheduleTermSelectedDelegate {
    
    var module : Module?
    var terms : [CourseTerm]?
    var selectedTerm = 0
    let dateFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
    }()
    
    @IBOutlet var termsButton: UIBarButtonItem!
    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationController?.navigationBar.translucent = false
        self.navigationItem.title = self.module!.name
        fetchSchedule()
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        loadSchedule()
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        self.sendView("Schedule (full schedule)", forModuleNamed: self.module!.name)
    }
    
    func loadSchedule() {
        
        let managedObjectContext = CoreDataManager.shared.managedObjectContext
        
        let fetchRequest = NSFetchRequest()
        fetchRequest.entity = NSEntityDescription.entityForName("CourseTerm", inManagedObjectContext: managedObjectContext)
        fetchRequest.sortDescriptors = [NSSortDescriptor(key: "startDate", ascending: false)]
        self.terms = try? managedObjectContext.executeFetchRequest(fetchRequest) as! [CourseTerm]
        self.reload()
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if let terms = self.terms where terms.count > section {
            return terms[selectedTerm].sections.count
        }
        return 0
        
    }
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        if let terms = self.terms where terms.count > 0 {
            return 1
        }
        return 0
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let section = self.terms![selectedTerm].sections[indexPath.row]
        
        let cell = tableView.dequeueReusableCellWithIdentifier("Schedule Cell", forIndexPath: indexPath) as UITableViewCell
        
        let courseNameLabel = cell.viewWithTag(1) as! UILabel
        let sectionTitleLabel = cell.viewWithTag(2) as! UILabel
        
        courseNameLabel.text = String(format: NSLocalizedString("course name-section number", tableName: "Localizable", bundle: NSBundle.mainBundle(), value: "%@-%@", comment: "course name-section number"), section.courseName, section.courseSectionNumber)
        sectionTitleLabel.text = section.sectionTitle
        return cell
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        self.sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionList_Select, withLabel: "Click Course", withValue: nil, forModuleNamed: self.module!.name)
        let term = self.terms![selectedTerm]
        let section = term.sections[indexPath.row]
        
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        self.performSegueWithIdentifier("Show Course Detail", sender: section)
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?)
    {
        if segue.identifier == "Show Course Detail" {
            if let course = sender as? CourseSection {
                let tabBarController = segue.destinationViewController as! CourseDetailTabBarController
                tabBarController.isInstructor = course.isInstructor as Bool
                tabBarController.module = self.module!
                tabBarController.termId = course.term.termId
                tabBarController.sectionId = course.sectionId
                for v in tabBarController.viewControllers! {
                    var vc: UIViewController = v
                    if let navVC = v as? UINavigationController {
                        
                        vc = navVC.viewControllers[0]
                    }
                    if vc.respondsToSelector(Selector("setModule:")) {
                        vc.setValue(self.module, forKey: "module")
                    }
                    if vc.respondsToSelector(Selector("setSectionId:")) {
                        vc.setValue(course.sectionId, forKey: "sectionId")
                    }
                    if vc.respondsToSelector(Selector("setTermId:")) {
                        vc.setValue(course.term.termId, forKey: "termId")
                    }
                    if vc.respondsToSelector(Selector("setCourseName:")) {
                        vc.setValue(course.courseName, forKey: "courseName")
                    }
                    if vc.respondsToSelector(Selector("setCourseNameAndSectionNumber:")) {
                        let courseName = course.courseName
                        let courseSectionNumber = course.courseSectionNumber
                        let courseNameAndSectionNumber = "\(courseName)-\(courseSectionNumber)"
                        vc.setValue(courseNameAndSectionNumber, forKey: "courseNameAndSectionNumber")
                    }
                    
                }
            }
        }  else if segue.identifier == "Choose Courses Term" {
            let navController = segue.destinationViewController as! UINavigationController
            let detailController = navController.viewControllers[0] as! CoursesPageSelectionViewController
            detailController.terms = self.terms
            detailController.coursesChangePageDelegate = self
            detailController.module = self.module
            
        }
    }
    
    func fetchSchedule() {
        if let userid = CurrentUser.sharedInstance().userid {
            let urlBase = self.module?.propertyForKey("full")
            let urlString = "\(urlBase!)/\(userid.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())!)"
            
            let privateContext = NSManagedObjectContext(concurrencyType: .PrivateQueueConcurrencyType)
            privateContext.parentContext = self.module?.managedObjectContext
            privateContext.undoManager = nil
            
            privateContext.performBlock { () -> Void in
                
                do {
                    UIApplication.sharedApplication().networkActivityIndicatorVisible = true
                    let authenticatedRequest = AuthenticatedRequest()
                    if let url = NSURL(string: urlString) {
                        let responseData = authenticatedRequest.requestURL(url, fromView: self)
                        if let responseData = responseData {
                            let json = JSON(data: responseData)
                            UIApplication.sharedApplication().networkActivityIndicatorVisible = false
                            
                            let termRequest = NSFetchRequest(entityName: "CourseTerm")
                            let oldObjects = try privateContext
                                .executeFetchRequest(termRequest) as! [CourseTerm]
                            for oldObject in oldObjects {
                                privateContext.deleteObject(oldObject)
                            }
                            
                            if let jsonTerms = json["terms"].array {
                                
                                for termJson in jsonTerms {
                                    let term = NSEntityDescription.insertNewObjectForEntityForName("CourseTerm", inManagedObjectContext: privateContext) as! CourseTerm
                                    term.termId = termJson["id"].string
                                    term.name = termJson["name"].string
                                    term.startDate = self.dateFormatter.dateFromString(termJson["startDate"].string!)
                                    term.endDate = self.dateFormatter.dateFromString(termJson["endDate"].string!)
                                    
                                    for courseJson in termJson["sections"].array! {
                                        
                                        let course = NSEntityDescription.insertNewObjectForEntityForName("CourseSection", inManagedObjectContext: privateContext) as! CourseSection
                                        course.sectionId = courseJson["sectionId"].string
                                        course.sectionTitle = courseJson["sectionTitle"].string
                                        course.isInstructor = courseJson["isInstructor"].boolValue
                                        course.courseName = courseJson["courseName"].string
                                        course.courseSectionNumber = courseJson["courseSectionNumber"].string
                                        term.addSectionsObject(course)
                                        course.term = term
                                        
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
                                self.loadSchedule()
                            })
                        }}
                    
                } catch let error {
                    print (error)
                }
                
            }
        }
        
    }
    
    
    override func tableView(tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        
        let view = UIView(frame: CGRectMake(0, 0, CGRectGetWidth(tableView.frame), 30))
        let label = UILabel(frame: CGRectMake(8,0,CGRectGetWidth(tableView.frame), 30))
        label.translatesAutoresizingMaskIntoConstraints = false
        
        let term = self.terms![selectedTerm]
        label.text = term.name
        
        
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
        view.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("|-[label]",
            options: NSLayoutFormatOptions.AlignAllBaseline,
            metrics: nil,
            views: viewsDictionary))
        return view;
        
    }
    
    override func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 30
    }
    
    
    func reload() {
        self.tableView.reloadData()
        if let terms = terms where terms.count > 0 {
            self.termsButton.enabled = true
        }
    }
    
}