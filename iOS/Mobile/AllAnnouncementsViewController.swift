//
//  AllAnnouncementsViewController.swift
//  Mobile
//
//  Created by Alan McEwan on 2/3/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class AllAnnouncementsViewController : UIViewController, UIActionSheetDelegate, UITableViewDataSource, UITableViewDelegate, NSFetchedResultsControllerDelegate, UISplitViewControllerDelegate
{
    @IBOutlet var allAnnouncementsTableView: UITableView!
    @IBOutlet weak var myTabBarItem: UITabBarItem!
    
    var allAnnouncementController: NSFetchedResultsController!
    var myDatetimeOutputFormatter: NSDateFormatter?
    var myManagedObjectContext: NSManagedObjectContext!
    var myTabBarController: UITabBarController!
    var showHeaders:Bool = true
    var detailSelectionDelegate: DetailSelectionDelegate!
    var module: Module!
    var selectedAnnouncement:CourseAnnouncement?
    var detailViewController: CourseAnnouncementDetailViewController!
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        self.extendedLayoutIncludesOpaqueBars = true
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        var announcementError:NSError?
        
        allAnnouncementController = getAnnouncementsFetchedResultsController()
        
        if (!allAnnouncementController.performFetch(&announcementError)) {
            NSLog("Unersolved error: fetch error: \(announcementError!.localizedDescription)")
        }
        
        allAnnouncementsTableView.registerClass(NSClassFromString("UITableViewHeaderFooterView"), forHeaderFooterViewReuseIdentifier: "Header")
        self.view.backgroundColor = UIColor.primaryColor()
        
        var tabBarItem2 = myTabBarController?.tabBar.items?[2] as! UITabBarItem
        tabBarItem2.selectedImage = UIImage(named:"ilp-announcements-selected")
        
        if UIDevice.currentDevice().userInterfaceIdiom == .Pad {
            if selectedAnnouncement == nil {
                if allAnnouncementController.fetchedObjects!.count > 0 {
                    var indexPath = NSIndexPath(forRow: 0, inSection: 0)
                    allAnnouncementsTableView.selectRowAtIndexPath(indexPath, animated: true, scrollPosition:UITableViewScrollPosition.Top)
                    tableView(allAnnouncementsTableView, didSelectRowAtIndexPath: indexPath);
                }
            }
        }
        
        self.sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction:kAnalyticsActionSearch, withLabel:"ILP Announcements List", withValue:nil, forModuleNamed:"ILP");
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        if UIDevice.currentDevice().userInterfaceIdiom == .Pad {
            findSelectedItem()
        }
    }
    
    /* called first
    begins update to `UITableView`
    ensures all updates are animated simultaneously */
    func controllerWillChangeContent(controller: NSFetchedResultsController) {
        allAnnouncementsTableView.beginUpdates()
    }
    
    /* helper method to configure a `UITableViewCell`
    ask `NSFetchedResultsController` for the model */
    func configureCell(cell: UITableViewCell, atIndexPath indexPath: NSIndexPath) {
        let announcement = allAnnouncementController.objectAtIndexPath(indexPath) as! CourseAnnouncement
        
        let nameLabel = cell.viewWithTag(100) as! UILabel
        nameLabel.text = announcement.title
        let sectionNameLabel = cell.viewWithTag(102) as! UILabel
        sectionNameLabel.text = announcement.courseName + "-" + announcement.courseSectionNumber
        
        let dateLabel = cell.viewWithTag(101) as! UILabel
        
        if let date = announcement.date {
            dateLabel.text = self.datetimeOutputFormatter()!.stringFromDate(date)
        } else {
            dateLabel.text = ""
        }
    }
    
    /* called:
    - when a new model is created
    - when an existing model is updated
    - when an existing model is deleted */
    func controller(controller: NSFetchedResultsController,
        didChangeObject object: AnyObject,
        atIndexPath indexPath: NSIndexPath?,
        forChangeType type: NSFetchedResultsChangeType,
        newIndexPath: NSIndexPath?)  {
            
        switch type {
            case .Insert:
                allAnnouncementsTableView.insertRowsAtIndexPaths([newIndexPath as NSIndexPath!], withRowAnimation: .Fade)
            case .Update:
                let cell = self.allAnnouncementsTableView.cellForRowAtIndexPath(indexPath as NSIndexPath!)
                configureCell(cell!, atIndexPath: indexPath as NSIndexPath!)
                allAnnouncementsTableView.reloadRowsAtIndexPaths([indexPath as NSIndexPath!], withRowAnimation: .Fade)
            case .Delete:
                allAnnouncementsTableView.deleteRowsAtIndexPaths([indexPath as NSIndexPath!], withRowAnimation: .Fade)
            default:
                break
        }
    }
    
    func controller(controller: NSFetchedResultsController,
        didChangeSection sectionInfo: NSFetchedResultsSectionInfo,
        atIndex sectionIndex: Int,
        forChangeType type: NSFetchedResultsChangeType)
    {
        switch(type) {
            
            case .Insert:
                allAnnouncementsTableView.insertSections(NSIndexSet(index: sectionIndex), withRowAnimation: UITableViewRowAnimation.Fade)
            case .Delete:
                allAnnouncementsTableView.deleteSections(NSIndexSet(index: sectionIndex), withRowAnimation: UITableViewRowAnimation.Fade)
            default:
                break
        }
    }
    
    /* called last
    tells `UITableView` updates are complete */
    func controllerDidChangeContent(controller: NSFetchedResultsController) {
        allAnnouncementsTableView.endUpdates()
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        
        let announcement = allAnnouncementController.objectAtIndexPath(indexPath) as! CourseAnnouncement
        selectedAnnouncement = announcement
        
        if UIDevice.currentDevice().userInterfaceIdiom == .Pad {
            
            detailViewController.courseName = announcement.courseName
            detailViewController.courseSectionNumber = announcement.courseSectionNumber
            detailViewController.itemTitle = announcement.title
            detailViewController.itemContent = announcement.content
            detailViewController.itemLink = announcement.website
            if let announcementDate = announcement.date {
                detailViewController.itemPostDateTime = announcementDate
            }
            else {
                detailViewController.itemPostDateTime = nil
            }
            self.detailSelectionDelegate = detailViewController
            self.detailSelectionDelegate.selectedDetail(announcement, withIndex: indexPath, withModule: self.module!, withController: self)
            
        } else if UIDevice.currentDevice().userInterfaceIdiom == .Phone {
            self.performSegueWithIdentifier("Show ILP Announcement Detail", sender:tableView)
        }
    }
    
    func tableView(tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        
        if self.showHeaders {
            let h = allAnnouncementsTableView.dequeueReusableHeaderFooterViewWithIdentifier("Header") as! UITableViewHeaderFooterView
            
            for subView in h.contentView.subviews
            {
                if (subView.tag == 1 || subView.tag == 2)
                {
                    subView.removeFromSuperview()
                }
            }
            
            let sections = allAnnouncementController.sections as? [NSFetchedResultsSectionInfo]
            
            var dateLabel:String? = sections?[section].name
            
            if h.backgroundColor != UIColor.accentColor() {
                h.contentView.backgroundColor = UIColor.accentColor()
                let headerLabel = UILabel()
                headerLabel.tag = 1
                headerLabel.text = dateLabel
                headerLabel.backgroundColor = UIColor.clearColor()
                headerLabel.textColor = UIColor.subheaderTextColor()
                headerLabel.font = UIFont.boldSystemFontOfSize(16)
                headerLabel.minimumScaleFactor = 0.5
                h.contentView.addSubview(headerLabel)
                headerLabel.setTranslatesAutoresizingMaskIntoConstraints(false)
                h.contentView.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("H:|-10-[headerLabel]", options: NSLayoutFormatOptions(0), metrics: nil, views: ["headerLabel":headerLabel]))
                h.contentView.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("V:|[headerLabel]|", options: NSLayoutFormatOptions(0), metrics:nil, views: ["headerLabel":headerLabel]))
            }
            
            return h
            
        } else {
            return nil
        }
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        var cell = tableView.dequeueReusableCellWithIdentifier("Daily Announcement Cell", forIndexPath: indexPath) as! UITableViewCell
        let announcement = allAnnouncementController.objectAtIndexPath(indexPath) as! CourseAnnouncement

        configureCell(cell, atIndexPath: indexPath)

        return cell
    }
    
    func tableView(tableView: UITableView,
        heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat
    {
        return 60.0
    }
    
    func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat{
        let count = allAnnouncementController.sections?.count
        if count == 0 || !showHeaders {
            return 0.0
        } else {
            return 18.0
        }
    }
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        let numberOfSections = allAnnouncementController.sections?.count
        return numberOfSections!
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        let numberOfRowsInSection = allAnnouncementController.sections?[section].numberOfObjects
        return numberOfRowsInSection!
    }

    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?)
    {
        let indexPath: NSIndexPath! = allAnnouncementsTableView.indexPathForSelectedRow()
        let announcement = allAnnouncementController.objectAtIndexPath(indexPath) as! CourseAnnouncement
        let detailController = segue.destinationViewController as! CourseAnnouncementDetailViewController
        detailController.courseName = announcement.courseName
        detailController.courseSectionNumber = announcement.courseSectionNumber
        detailController.itemTitle = announcement.title
        detailController.itemContent = announcement.content
        detailController.itemLink = announcement.website.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet())
        if let announcementDate = announcement.date {
            detailController.itemPostDateTime = announcementDate
        }
        else {
            detailController.itemPostDateTime = nil
        }

        allAnnouncementsTableView.deselectRowAtIndexPath(indexPath, animated:true)
    }
    
    
    func announcementFetchRequest() -> NSFetchRequest {
        
        let fetchRequest = NSFetchRequest(entityName:"CourseAnnouncement")
        let sortDescriptor = NSSortDescriptor(key:"date", ascending:false)
        fetchRequest.sortDescriptors = [sortDescriptor]

        return fetchRequest
    }
    
    
    func getAnnouncementsFetchedResultsController() -> NSFetchedResultsController {
        
        var importContext = NSManagedObjectContext(concurrencyType: .MainQueueConcurrencyType)
        
        importContext.parentContext = self.myManagedObjectContext
        
        let fetchRequest = announcementFetchRequest()
        let entity:NSEntityDescription? = NSEntityDescription.entityForName("CourseAnnouncement", inManagedObjectContext:importContext)
        fetchRequest.entity = entity;
        
        var theFetchedResultsController:NSFetchedResultsController?
        
        
        theFetchedResultsController = NSFetchedResultsController(fetchRequest: fetchRequest, managedObjectContext:importContext, sectionNameKeyPath:"displayDateSectionHeader", cacheName:nil)
        
        return theFetchedResultsController!
    }

    func datetimeOutputFormatter() ->NSDateFormatter? {
        
        if (myDatetimeOutputFormatter == nil) {
            myDatetimeOutputFormatter = NSDateFormatter()
            myDatetimeOutputFormatter!.timeStyle = .ShortStyle
        }
        return myDatetimeOutputFormatter
    }
    
    func splitViewController(svc: UISplitViewController,
        shouldHideViewController vc: UIViewController,
        inOrientation orientation: UIInterfaceOrientation) -> Bool {
            return false;
    }
    
    func setSelectedItem(item:CourseAnnouncement?)
    {
        selectedAnnouncement = item
    }
    
    func findSelectedItem() {
        if selectedAnnouncement != nil {
            var indexPath = NSIndexPath(forRow: 0, inSection: 0)
            let myTargetItem = selectedAnnouncement!
            
            for iter in allAnnouncementController.fetchedObjects!
            {
                let temp: CourseAnnouncement = iter as! CourseAnnouncement
                if myTargetItem.website != nil && temp.website == myTargetItem.website {
                    indexPath = allAnnouncementController.indexPathForObject(temp)
                }
            }
            allAnnouncementsTableView.selectRowAtIndexPath(indexPath, animated: true, scrollPosition:UITableViewScrollPosition.Top)
            tableView(self.allAnnouncementsTableView, didSelectRowAtIndexPath: indexPath);
        }
    }

}