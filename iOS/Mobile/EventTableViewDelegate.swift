//
//  EventTableViewDelegate.swift
//  Mobile
//
//  Created by Alan McEwan on 1/20/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation


class EventTableViewDelegate: NSObject, UITableViewDataSource, UITableViewDelegate, NSFetchedResultsControllerDelegate
{
    var eventTableView: UITableView
    var eventController: NSFetchedResultsController?
    var eventTableHeightConstraint: NSLayoutConstraint
    var eventTableWidthConstraint: NSLayoutConstraint
    var myDatetimeOutputFormatter: NSDateFormatter?
    var module:Module!
    var myNoDataView:UIView?
    
    init(tableView: UITableView, controller: NSFetchedResultsController, heightConstraint: NSLayoutConstraint, widthConstraint: NSLayoutConstraint, parentModule:Module) {
        
        eventTableView = tableView
        eventController = controller
        eventTableHeightConstraint = heightConstraint
        eventTableWidthConstraint = widthConstraint
        module = parentModule
        
        super.init()
        
        eventTableView.delegate = self
        eventTableView.dataSource = self
        
        eventController!.delegate = self
    }
    
    /* called first
    begins update to `UITableView`
    ensures all updates are animated simultaneously */
    func controllerWillChangeContent(controller: NSFetchedResultsController) {
        eventTableView.beginUpdates()
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
                eventTableView.insertRowsAtIndexPaths([newIndexPath as NSIndexPath!], withRowAnimation: .Fade)
            case .Update:
                let cell = self.eventTableView.cellForRowAtIndexPath(indexPath as NSIndexPath!)
                configureCell(cell!, atIndexPath: indexPath as NSIndexPath!)
                eventTableView.reloadRowsAtIndexPaths([indexPath as NSIndexPath!], withRowAnimation: .Fade)
            case .Move:
                eventTableView.deleteRowsAtIndexPaths([indexPath as NSIndexPath!], withRowAnimation: .Fade)
                eventTableView.insertRowsAtIndexPaths([newIndexPath as NSIndexPath!], withRowAnimation: .Fade)
            case .Delete:
                eventTableView.deleteRowsAtIndexPaths([indexPath as NSIndexPath!], withRowAnimation: .Fade)
            default:
                return
            }
    }
    
    func controller(controller: NSFetchedResultsController,
        didChangeSection sectionInfo: NSFetchedResultsSectionInfo,
        atIndex sectionIndex: Int,
        forChangeType type: NSFetchedResultsChangeType)
    {
        switch(type) {
            
        case .Insert:
            eventTableView.insertSections(NSIndexSet(index: sectionIndex),
                withRowAnimation: UITableViewRowAnimation.Fade)
            
        case .Delete:
            eventTableView.deleteSections(NSIndexSet(index: sectionIndex),
                withRowAnimation: UITableViewRowAnimation.Fade)
            
        default:
            break
        }
    }

    /* called last
    tells `UITableView` updates are complete */
    func controllerDidChangeContent(controller: NSFetchedResultsController) {
        eventTableView.endUpdates()
        eventTableHeightConstraint.constant = (CGFloat(eventController!.fetchedObjects!.count) * 50.0)  + 50.0
    }
    
    /* helper method to configure a `UITableViewCell`
    ask `NSFetchedResultsController` for the model */
    func configureCell(cell: UITableViewCell, atIndexPath indexPath: NSIndexPath) {
        let event = eventController!.objectAtIndexPath(indexPath) as! CourseEvent
        
        if event.title != nil {
            let nameLabel = cell.viewWithTag(100) as! UILabel
            nameLabel.text = event.title
        }
        
        if event.courseName != nil && event.courseSectionNumber != nil {
            let courseSectionLabel = cell.viewWithTag(102) as! UILabel
            courseSectionLabel.text = event.courseName + "-" + event.courseSectionNumber
        }
        
        let startDateLabel = cell.viewWithTag(101) as! UILabel
        
        if event.startDate != nil {
            if let eventStartDate = event.startDate {
                startDateLabel.text = self.datetimeOutputFormatter()!.stringFromDate(eventStartDate)
            } else {
                startDateLabel.text = ""
            }
        }
    }

    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        var cell = tableView.dequeueReusableCellWithIdentifier("Daily Event Cell", forIndexPath: indexPath) as! UITableViewCell
        configureCell(cell, atIndexPath:indexPath)
        return cell
    }
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        
        let numberOfSections = eventController!.sections?.count
        return numberOfSections!
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        let numberOfRowsInSection = eventController!.sections?[section].numberOfObjects
        
        if numberOfRowsInSection == 0 {
            showNoDataView()
        } else {
            hideNoDataView()
        }

        return numberOfRowsInSection!
    }
        
    func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        
        if let sections = eventController!.sections as? [NSFetchedResultsSectionInfo] {
            return sections[section].name
        } else {
            return nil
        }
    }
    
    func tableView(tableView: UITableView,
        heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return 50.0
    }
    
    func noDataView() ->UIView? {
        if myNoDataView == nil {
            myNoDataView = UIView(frame: CGRect(x:0,y:0,width:eventTableWidthConstraint.constant, height:40.0))
            
            let constrainedView = UIView()
            constrainedView.setTranslatesAutoresizingMaskIntoConstraints(false)
            myNoDataView?.addSubview(constrainedView)
            
            myNoDataView?.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("H:|[constrainedView]|", options: NSLayoutFormatOptions(0), metrics: nil, views: ["constrainedView":constrainedView]))
            myNoDataView?.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("V:|[constrainedView]|", options: NSLayoutFormatOptions(0), metrics: nil, views: ["constrainedView":constrainedView]))
            
            var noMatchesLabel = UILabel(frame:CGRect(x:0,y:0,width:eventTableWidthConstraint.constant, height:40.0))
            noMatchesLabel.font = UIFont.systemFontOfSize(14)
            noMatchesLabel.numberOfLines = 1;
            noMatchesLabel.lineBreakMode = NSLineBreakMode.ByTruncatingTail
            noMatchesLabel.textAlignment = NSTextAlignment.Left
            noMatchesLabel.text = NSLocalizedString("No events scheduled for today", comment:"no events scheduled for today message")
            
            
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
            
            eventTableView.insertSubview(myNoDataView!, belowSubview:eventTableView)
            eventTableHeightConstraint.constant =  90.0
        }
        return myNoDataView
    }
    
    func showNoDataView() {
        if ( myNoDataView == nil ) {
            myNoDataView = noDataView()
        }
        myNoDataView?.hidden = false
    }
    
    func hideNoDataView() {
        self.myNoDataView?.hidden = true
    }
    
    func datetimeOutputFormatter() ->NSDateFormatter? {
        
        if (myDatetimeOutputFormatter == nil) {
            myDatetimeOutputFormatter = NSDateFormatter()
            myDatetimeOutputFormatter!.timeStyle = .ShortStyle
        }
        return myDatetimeOutputFormatter
    }

}