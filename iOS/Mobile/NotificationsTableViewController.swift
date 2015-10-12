//
//  NotificationsTableViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 8/13/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class NotificationsTableViewController : UITableViewController, NSFetchedResultsControllerDelegate {

    var module : Module?
    var indexPathToReselect : NSIndexPath?

    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.title = self.module?.name
        tableView.estimatedRowHeight = 44
        tableView.rowHeight = UITableViewAutomaticDimension

        if UIScreen.mainScreen().traitCollection.userInterfaceIdiom == .Pad {
            self.splitViewController?.preferredDisplayMode = .AllVisible;
        } else {
            self.splitViewController?.preferredDisplayMode = .Automatic;
        }
        
        fetchNotifications()
        reloadData()
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        sendView("Notifications List", forModuleNamed: self.module?.name)
    }
    
    // MARK: data retrieval
    func fetchNotifications() {

        let urlBase = self.module!.propertyForKey("notifications")!
        let userid =  CurrentUser.sharedInstance().userid.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())
        let urlString = "\(urlBase)/\(userid!)"
        NotificationsFetcher.fetchNotificationsFromURL(urlString, withManagedObjectContext: CoreDataManager.shared.managedObjectContext, showLocalNotification: false, fromView: self)
        
    }
    
    //MARK: segue
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "Show Detail" {
            self.sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction:kAnalyticsActionList_Select, withLabel:"Select Notification", withValue:nil, forModuleNamed:self.module!.name);
            let detailController = (segue.destinationViewController as! UINavigationController).topViewController as! NotificationsDetailViewController
            let notification = fetchedResultsController.objectAtIndexPath(self.tableView.indexPathForSelectedRow!) as! Notification
            detailController.notification = notification
            detailController.module = self.module
        }
    }
    
    //MARK: FeedFilterDelegate
    func reloadData() {
        _fetchedResultsController = nil
        do {
            try self.fetchedResultsController.performFetch()
        } catch { }
        self.tableView.reloadData()
        
        showNotificationSelectedIfSet()
    }
    
    private func showNotificationSelectedIfSet() {
        let splitView = self.splitViewController as! NotificationsSplitViewController
        if let selectedId = splitView.uuid {
            let notifications = _fetchedResultsController?.fetchedObjects as! [Notification]
            for (index, notification) in notifications.enumerate() {
                if notification.notificationId == selectedId {
                    splitView.uuid = nil
                    let indexPath = NSIndexPath(forRow: index, inSection: 0)
                    tableView.selectRowAtIndexPath(indexPath, animated: true, scrollPosition: UITableViewScrollPosition.Bottom)
                    self.performSegueWithIdentifier("Show Detail", sender: nil)
                    tableView.reloadRowsAtIndexPaths( [ indexPath ], withRowAnimation: UITableViewRowAnimation.None )
                }
            }
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
        request.entity = NSEntityDescription.entityForName("Notification", inManagedObjectContext: managedObjectContext)
        
        request.sortDescriptors = [NSSortDescriptor(key: "sticky", ascending: false),NSSortDescriptor(key: "noticeDate", ascending: false),NSSortDescriptor(key: "title", ascending: true)]
        
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
        let cell = tableView.dequeueReusableCellWithIdentifier("Notification Cell", forIndexPath: indexPath) as UITableViewCell
        let notification = fetchedResultsController.objectAtIndexPath(indexPath) as! Notification

        let textLabel = cell.viewWithTag(1) as! UILabel
        let barImageView = cell.viewWithTag(2) as! UIImageView
        
        textLabel.text = notification.title

        var stickyColor = UIColor.clearColor()
        if notification.read.boolValue {
            textLabel.font = UIFont.preferredFontForTextStyle(UIFontTextStyleBody)
        } else {
            textLabel.font = UIFont.preferredFontForTextStyle(UIFontTextStyleHeadline)
        }
        if notification.sticky.boolValue {
            stickyColor = UIColor(red: 241/255.0, green: 90/255.0, blue: 36/255.0, alpha: 1.0)
        }
        
        let rect = CGRectMake(0, 0, 1, 1)
        // Create a 1 by 1 pixel context
        UIGraphicsBeginImageContextWithOptions(rect.size, false, 0)
        stickyColor.setFill()
        UIRectFill(rect)
        let image = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext();
        barImageView.image = image;

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
            tableView.reloadRowsAtIndexPaths( [ indexPath! ], withRowAnimation: UITableViewRowAnimation.None )
            indexPathToReselect = indexPath
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
        if let indexPath = indexPathToReselect {
            tableView.selectRowAtIndexPath(indexPath, animated: true, scrollPosition: UITableViewScrollPosition.Bottom)
            indexPathToReselect = nil
        }
        
        showNotificationSelectedIfSet()
    }
    
    //MARK: edit
    override func tableView(tableView: UITableView, canEditRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        let notification = fetchedResultsController.objectAtIndexPath(indexPath) as! Notification
        return !notification.sticky.boolValue
    }
    
    override func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {

        if editingStyle == .Delete {
            let notification = fetchedResultsController.objectAtIndexPath(indexPath) as! Notification
            NotificationsFetcher.deleteNotification(notification, module: self.module!)
        }
    }

}
