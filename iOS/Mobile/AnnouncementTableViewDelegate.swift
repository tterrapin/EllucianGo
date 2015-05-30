//
//  AnnouncementTableViewDelegate.swift
//  Mobile
//
//  Created by Alan McEwan on 1/20/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation


class AnnouncementTableViewDelegate: NSObject, UITableViewDataSource, UITableViewDelegate, NSFetchedResultsControllerDelegate
{
    var announcementTableView: UITableView
    var announcementController: NSFetchedResultsController?
    var myDatetimeOutputFormatter: NSDateFormatter?
    var announcementTableHeightConstraint: NSLayoutConstraint
    var announcementTableWidthConstraint: NSLayoutConstraint
    var module:Module!
    var myNoDataView:UIView?
    
    init(tableView: UITableView, controller: NSFetchedResultsController, heightConstraint: NSLayoutConstraint, widthConstraint: NSLayoutConstraint, parentModule:Module) {
        
        announcementTableView = tableView
        announcementController = controller
        announcementTableHeightConstraint = heightConstraint
        announcementTableWidthConstraint = widthConstraint
        module = parentModule
        super.init()
        
        announcementTableView.delegate = self
        announcementTableView.dataSource = self
        
        announcementController!.delegate = self
    }
    
    /* called first
    begins update to `UITableView`
    ensures all updates are animated simultaneously */
    func controllerWillChangeContent(controller: NSFetchedResultsController) {
        announcementTableView.beginUpdates()
    }
    
    /* called:
    - when a new model is created
    - when an existing model is updated
    - when an existing model is deleted */
    func controller(controller: NSFetchedResultsController,
        didChangeObject object: AnyObject,
        atIndexPath indexPath: NSIndexPath?,
        forChangeType type: NSFetchedResultsChangeType,
        newIndexPath: NSIndexPath?) {
            
            switch type {
            case .Insert:
                self.announcementTableView.insertRowsAtIndexPaths([newIndexPath as NSIndexPath!], withRowAnimation: .Fade)
            case .Update:
                let cell = self.announcementTableView.cellForRowAtIndexPath(indexPath as NSIndexPath!)
                configureCell(cell!, atIndexPath: indexPath as NSIndexPath!)
                announcementTableView.reloadRowsAtIndexPaths([indexPath as NSIndexPath!], withRowAnimation: .Fade)
            case .Move:
                announcementTableView.deleteRowsAtIndexPaths([indexPath as NSIndexPath!], withRowAnimation: .Fade)
                announcementTableView.insertRowsAtIndexPaths([newIndexPath as NSIndexPath!], withRowAnimation: .Fade)
            case .Delete:
                announcementTableView.deleteRowsAtIndexPaths([indexPath as NSIndexPath!], withRowAnimation: .Fade)
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
            announcementTableView.insertSections(NSIndexSet(index: sectionIndex),
                withRowAnimation: UITableViewRowAnimation.Fade)
    
        case .Delete:
            announcementTableView.deleteSections(NSIndexSet(index: sectionIndex),
                withRowAnimation: UITableViewRowAnimation.Fade)
            
        default:
            break
        }
    }

    /* called last
    tells `UITableView` updates are complete */
    func controllerDidChangeContent(controller: NSFetchedResultsController) {
        announcementTableView.endUpdates()
        announcementTableHeightConstraint.constant = (CGFloat(announcementController!.fetchedObjects!.count) * 40.0) + 50.0
    }
    
    /* helper method to configure a `UITableViewCell`
    ask `NSFetchedResultsController` for the model */
    func configureCell(cell: UITableViewCell, atIndexPath indexPath: NSIndexPath) {
        let announcement = announcementController!.objectAtIndexPath(indexPath) as! CourseAnnouncement
        
        if announcement.title != nil {
            let nameLabel = cell.viewWithTag(100) as! UILabel
            nameLabel.text = announcement.title
        }
        
        if announcement.courseName != nil && announcement.courseSectionNumber != nil {
            let sectionNameLabel = cell.viewWithTag(102) as! UILabel
            sectionNameLabel.text = announcement.courseName + "-" + announcement.courseSectionNumber
        }
    }

    
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        var cell = tableView.dequeueReusableCellWithIdentifier("Daily Announcement Cell", forIndexPath: indexPath) as! UITableViewCell
        configureCell(cell, atIndexPath:indexPath)
        return cell
    }
    
    func tableView(tableView: UITableView,
        heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat
    {
        return 40.0
    }
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        
        let numberOfSections = announcementController!.sections?.count
        return numberOfSections!
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        let numberOfRowsInSection = announcementController!.sections?[section].numberOfObjects
        
        if numberOfRowsInSection == 0 {
            showNoDataView()
        } else {
            hideNoDataView()
        }
        
        return numberOfRowsInSection!
    }
    
    func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        
        if let sections = announcementController!.sections as? [NSFetchedResultsSectionInfo] {
            return sections[section].name
        } else {
            return nil
        }
    }
    
    func datetimeOutputFormatter() ->NSDateFormatter? {
        
        if (myDatetimeOutputFormatter == nil) {
            myDatetimeOutputFormatter = NSDateFormatter()
            myDatetimeOutputFormatter!.timeStyle = .ShortStyle
        }
        
        return myDatetimeOutputFormatter
    }
    
    
    func noDataView() ->UIView? {
        if myNoDataView == nil {
            myNoDataView = UIView(frame: CGRect(x:0,y:0,width:announcementTableWidthConstraint.constant, height:40.0))
            
            let constrainedView = UIView()
            constrainedView.setTranslatesAutoresizingMaskIntoConstraints(false)
            myNoDataView?.addSubview(constrainedView)
            
            myNoDataView?.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("H:|[constrainedView]|", options: NSLayoutFormatOptions(0), metrics: nil, views: ["constrainedView":constrainedView]))
            myNoDataView?.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("V:|[constrainedView]|", options: NSLayoutFormatOptions(0), metrics: nil, views: ["constrainedView":constrainedView]))
            
            
            var noMatchesLabel = UILabel(frame:CGRect(x:0,y:0,width:announcementTableWidthConstraint.constant, height:40.0))
            noMatchesLabel.font = UIFont.systemFontOfSize(14)
            noMatchesLabel.numberOfLines = 1;
            noMatchesLabel.lineBreakMode = NSLineBreakMode.ByTruncatingTail
            noMatchesLabel.textAlignment = NSTextAlignment.Left
            noMatchesLabel.text = NSLocalizedString("No announcements for today", comment:"no announcements for today message")
            
            constrainedView.backgroundColor = UIColor.whiteColor()
            myNoDataView?.hidden = true
            noMatchesLabel.setTranslatesAutoresizingMaskIntoConstraints(false)
            constrainedView.addSubview(noMatchesLabel)
            constrainedView.addConstraint(NSLayoutConstraint(item:noMatchesLabel,
                attribute:NSLayoutAttribute.CenterY,
                relatedBy:NSLayoutRelation.Equal,
                toItem:constrainedView,
                attribute:NSLayoutAttribute.CenterY,
                multiplier:1.0,
                constant:0.0))
            constrainedView.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("|-10-[label]-10-|", options: NSLayoutFormatOptions.AlignAllCenterY, metrics: nil, views: ["label" : noMatchesLabel]))
            
            announcementTableView.insertSubview(myNoDataView!, belowSubview:announcementTableView)
            announcementTableHeightConstraint.constant =  90.0
        }
        return myNoDataView
    }
    
    func showNoDataView()
    {
        if ( myNoDataView == nil ) {
            myNoDataView = noDataView()
        }
        self.myNoDataView?.hidden = false
        
    }
    
    func hideNoDataView()
    {
        self.myNoDataView?.hidden = true
    }

}