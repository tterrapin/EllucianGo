//
//  RosterViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 10/30/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class RosterViewController : UITableViewController, NSFetchedResultsControllerDelegate {
    var termId : String?
    var sectionId : String?
    var courseNameAndSectionNumber : String?
    var module : Module?
    var _fetchedResultsController : NSFetchedResultsController?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationController?.navigationBar.translucent = false
        self.tableView.sectionIndexBackgroundColor = UIColor.clearColor()

        try! self.fetchedResultsController.performFetch()
        self.navigationItem.title = self.courseNameAndSectionNumber
        if CurrentUser.sharedInstance().isLoggedIn {
            self.fetchRoster(self)
        }
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "fetchRoster:", name: kLoginExecutorSuccess, object: nil)
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        self.sendView("Course roster list", forModuleNamed: self.module!.name)
    }
    
    var fetchedResultsController: NSFetchedResultsController {
        // return if already initialized
        if self._fetchedResultsController != nil {
            return self._fetchedResultsController!
        }
        let managedObjectContext = self.module!.managedObjectContext!
        
        let request = NSFetchRequest()
        request.entity = NSEntityDescription.entityForName("CourseRoster", inManagedObjectContext: managedObjectContext)
        request.predicate = NSPredicate(format: "termId == %@ && sectionId == %@", self.termId!, self.sectionId!)
        request.sortDescriptors = [NSSortDescriptor(key: "lastName", ascending: true), NSSortDescriptor(key: "firstName", ascending: true), NSSortDescriptor(key: "middleName", ascending: true)]
        
        let aFetchedResultsController = NSFetchedResultsController(fetchRequest: request, managedObjectContext: managedObjectContext, sectionNameKeyPath: "sectionKey", cacheName: nil)
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
        return fetchedResultsController.sections!.count
    }
    
    override func sectionIndexTitlesForTableView(tableView: UITableView) -> [String]? {
        return UILocalizedIndexedCollation.currentCollation().sectionIndexTitles
    }
    

    override func tableView(tableView: UITableView, sectionForSectionIndexTitle title: String, atIndex index: Int) -> Int {
        let localizedIndex: Int = UILocalizedIndexedCollation.currentCollation().sectionForSectionIndexTitleAtIndex(index)
        var localizedIndexTitles = UILocalizedIndexedCollation.currentCollation().sectionIndexTitles
        for currentLocalizedIndex in localizedIndex.stride(to: 0, by: -1) {
            for frcIndex in 0 ..< fetchedResultsController.sections!.count {
                let sectionInfo: NSFetchedResultsSectionInfo = fetchedResultsController.sections![frcIndex]
                let indexTitle: String = sectionInfo.indexTitle!
                if indexTitle == localizedIndexTitles[currentLocalizedIndex] {
                    return frcIndex
                }
            }
        }
        return 0

    }

    override func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        return fetchedResultsController.sections![section].name
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if let sections = fetchedResultsController.sections {
            let currentSection = sections[section] as NSFetchedResultsSectionInfo
            let count = currentSection.numberOfObjects
            return count
        }
        
        return 0
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        if let roster = fetchedResultsController.objectAtIndexPath(indexPath) as? CourseRoster {
            let defaults = AppGroupUtilities.userDefaults()
            var urlString : String?
            
            if ConfigurationManager.doesMobileServerSupportVersion("4.5") {
                urlString = defaults?.stringForKey("urls-directory-baseSearch")
            } else {
                urlString = defaults?.stringForKey("urls-directory-studentSearch")
            }
            
            let name : String
            if roster.firstName != nil  && roster.lastName != nil  {
                name = roster.firstName + " " + roster.lastName
            } else if roster.firstName != nil  {
                name = roster.firstName
            } else if roster.lastName != nil {
                name = roster.lastName
            } else {
                name = roster.name
            }
            
            let encodedSearchString = name.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())
            let encodedIdString = roster.studentId.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())
            urlString = "\(urlString!)?searchString=\(encodedSearchString!)&targetId=\(encodedIdString!)"
            let authenticatedRequest = AuthenticatedRequest()
            let responseData: NSData = authenticatedRequest.requestURL(NSURL(string: urlString!)!, fromView: self)
            let entries = DirectoryEntry.parseResponse(responseData)
            
            if entries.count == 0 {
                let alertController = UIAlertController(title: NSLocalizedString("Roster", comment: "title for roster no match"), message: NSLocalizedString("Person was not found", comment: "Person was not found"), preferredStyle: .Alert)
                let OKAction = UIAlertAction(title: "OK", style: .Default, handler: nil)
                alertController.addAction(OKAction)
                self.presentViewController(alertController, animated: true, completion: nil)
            } else if entries.count == 1 {
                self.performSegueWithIdentifier("Show Roster Person", sender: entries[0])
            } else {
                self.performSegueWithIdentifier("Show Roster List", sender: entries)
            }
        }
    }
    
 
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let roster = fetchedResultsController.objectAtIndexPath(indexPath)
        let cell = tableView.dequeueReusableCellWithIdentifier("Course Roster Cell", forIndexPath: indexPath) as UITableViewCell

        let label = cell.viewWithTag(1) as! UILabel
        label.text = roster.name
        
        if let _ = AppGroupUtilities.userDefaults()?.stringForKey("urls-directory-studentSearch") {
            cell.userInteractionEnabled = true
            cell.accessoryType = .DisclosureIndicator
            cell.selectionStyle = .Blue
        } else {
            cell.userInteractionEnabled = false
            cell.accessoryType = .None
            cell.selectionStyle = .None
        }
        
        return cell

    }
    
    func controllerWillChangeContent(controller: NSFetchedResultsController) {
        self.tableView.beginUpdates()
    }
    
    func controller(controller: NSFetchedResultsController, didChangeObject anObject: AnyObject, atIndexPath indexPath: NSIndexPath?, forChangeType type: NSFetchedResultsChangeType, newIndexPath: NSIndexPath?) {
        
        switch type{
        case NSFetchedResultsChangeType.Insert:
            self.tableView.insertRowsAtIndexPaths([newIndexPath!], withRowAnimation: .None)
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
    
    func controller(controller: NSFetchedResultsController, didChangeSection sectionInfo: NSFetchedResultsSectionInfo, atIndex sectionIndex: Int, forChangeType type: NSFetchedResultsChangeType) {
        let indexSet = NSIndexSet(index: sectionIndex)
        switch type {
        case NSFetchedResultsChangeType.Insert:
            self.tableView.insertSections(indexSet, withRowAnimation: UITableViewRowAnimation.Fade)
        case NSFetchedResultsChangeType.Delete:
            self.tableView.deleteSections(indexSet, withRowAnimation: UITableViewRowAnimation.Fade)
        case NSFetchedResultsChangeType.Update:
            break
        case NSFetchedResultsChangeType.Move:
            break
        }
    }
    
    func controllerDidChangeContent(controller: NSFetchedResultsController) {
        self.tableView.endUpdates()
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "Show Roster List" {
            let detailController = segue.destinationViewController as! DirectoryViewController
            detailController.entries = sender as! [DirectoryEntry];
            detailController.module = self.module;
        } else if segue.identifier == "Show Roster Person" {
            let detailController = segue.destinationViewController as! DirectoryEntryViewController
            detailController.entry = sender as? DirectoryEntry;
            detailController.module = self.module;
        }
    }

    
    func fetchRoster(sender: AnyObject) {
        
        
        if let userid = CurrentUser.sharedInstance().userid {
            let urlBase = self.module?.propertyForKey("roster")
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
                    
                    let request = NSFetchRequest(entityName: "CourseRoster")
                    request.predicate = NSPredicate(format: "termId == %@ && sectionId == %@", self.termId!, self.sectionId!)
                    let oldObjects = try! privateContext.executeFetchRequest(request) as! [CourseRoster]
                    
                    for oldObject in oldObjects {
                        privateContext.deleteObject(oldObject)
                    }

                    for jsonDictionary in json["activeStudents"].array! {
                        
                        let entry = NSEntityDescription.insertNewObjectForEntityForName("CourseRoster", inManagedObjectContext: privateContext) as! CourseRoster
                        entry.termId = self.termId;
                        entry.sectionId = self.sectionId;
                        entry.studentId = jsonDictionary["id"].string
                        entry.name = jsonDictionary["name"].string
                        entry.firstName = jsonDictionary["firstName"].string
                        if let middleName = jsonDictionary["middleName"].string {
                            entry.middleName = middleName
                        }
                        entry.lastName = jsonDictionary["lastName"].string
                        if let photo = jsonDictionary["photo"].string {
                            entry.photo = photo
                        }
                        entry.sectionKey = "\(entry.lastName.characters.first!)"
                    }
                    
                    try privateContext.save()
                    
                    privateContext.parentContext?.performBlock({
                        do {
                            try privateContext.parentContext?.save()
                        } catch let error {
                            print (error)
                        }
                    })
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

        label.text = fetchedResultsController.sections![section].name
        
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
}
