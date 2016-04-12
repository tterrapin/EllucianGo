//
//  CourseDetailViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 10/26/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class CourseDetailViewController: UITableViewController {
    
    var termId : String?
    var sectionId : String?
    var courseNameAndSectionNumber : String?
    var module : Module?
    var courseDetail : CourseDetail?
    
    let dateFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        formatter.timeZone = NSTimeZone(name:"UTC")
        return formatter
    }()
    let timeFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateFormat = "HH:mm'Z'"
        formatter.timeZone = NSTimeZone(name:"UTC")
        return formatter
    }()
    let displayDateFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateStyle = .ShortStyle
        formatter.timeStyle = .NoStyle
        formatter.timeZone = NSTimeZone(name:"UTC")
        return formatter
    }()
    let displayTimeFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateStyle = .NoStyle
        formatter.timeStyle = .ShortStyle
        return formatter
    }()
    var instructors : [CourseDetailInstructor]?
    var meetingPatterns : [CourseMeetingPattern]?
    var buildingsUrl : String?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationController?.navigationBar.translucent = false
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "setupData", name: "CourseDetailInformationLoaded", object: nil)
        self.navigationItem.title = self.courseNameAndSectionNumber!
        
        tableView.estimatedRowHeight = 60
        tableView.rowHeight = UITableViewAutomaticDimension
        tableView.tableFooterView = UIView()
        
    }
    
    override func tableView(tableView: UITableView, estimatedHeightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        switch(indexPath.section) {
        case 0:
            return 60
        case 1:
            return 60
        default:
            return 120;
        }
        
    }
    
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 3
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if let _ = self.courseDetail {
            switch(section) {
            case 0:
                if let count = self.meetingPatterns?.count where count > 0 {
                    return count
                }
                return 1
            case 1:
                if let count = self.instructors?.count where count > 0 {
                    return count
                }
                return 1
            case 2:
                return courseDetail?.courseDescription != nil ? 1 : 0;
            default:
                return 0;
            }
        }
        return 0;
    }
    
    override func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if let _ = self.courseDetail {
            switch(section) {
            case 0:
                return 52
            case 1:
                return 30
            case 2:
                return 0
            default:
                return 1
            }
        }
        return 0
        
    }
    
    override func tableView(tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        
        if let _ = self.courseDetail {
            switch(section) {
            case 0:
                return headerForTitle()
            case 1:
                return headerForFaculty()
            case 2:
                return nil;
            default:
                return nil;
            }
        }
        return nil;
    }
    
    func headerForTitle() -> UIView? {
        let view = UIView(frame: CGRectMake(0, 0, CGRectGetWidth(tableView.frame), 30))
        let label = UILabel(frame: CGRectMake(8,0,CGRectGetWidth(tableView.frame), 30))
        label.translatesAutoresizingMaskIntoConstraints = false
        let label2 = UILabel(frame: CGRectMake(8,30,CGRectGetWidth(tableView.frame), 30))
        label2.translatesAutoresizingMaskIntoConstraints = false
        
        
        label.textColor = UIColor(red: 0.2, green: 0.2, blue: 0.2, alpha: 1.0)
        label2.textColor = UIColor(red: 0.2, green: 0.2, blue: 0.2, alpha: 1.0)
        view.backgroundColor = UIColor(rgba: "#e6e6e6")
        label.font = UIFont.preferredFontForTextStyle(UIFontTextStyleHeadline)
        label2.font = UIFont.preferredFontForTextStyle(UIFontTextStyleSubheadline)
        
        view.addSubview(label)
        view.addSubview(label2)
        
        let viewsDictionary = ["label": label, "label2": label2, "view": view]
        
        // Create and add the vertical constraints
        view.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("V:|-4-[label]-4-[label2]-4-|",
            options: NSLayoutFormatOptions.AlignAllLeading,
            metrics: nil,
            views: viewsDictionary))
        
        // Create and add the horizontal constraints
        view.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("|-[label]",
            options: NSLayoutFormatOptions.AlignAllBaseline,
            metrics: nil,
            views: viewsDictionary))
        view.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("|-[label2]",
            options: NSLayoutFormatOptions.AlignAllBaseline,
            metrics: nil,
            views: viewsDictionary))
        
        label.text = self.courseDetail!.sectionTitle;
        if let courseDetail = courseDetail, firstMeetingDate = courseDetail.firstMeetingDate, lastMeetingDate = courseDetail.lastMeetingDate {
            let dates = String(format: NSLocalizedString("course first meeting - last meeting", tableName: "Localizable", bundle: NSBundle.mainBundle(), value: "%@ - %@", comment: "course first meeting - last meeting"), self.displayDateFormatter.stringFromDate(firstMeetingDate), self.displayDateFormatter.stringFromDate(lastMeetingDate))
            label2.text = dates
        }
        return view;
    }
    
    func headerForFaculty() -> UIView? {
        let view = UIView(frame: CGRectMake(0, 0, CGRectGetWidth(tableView.frame), 30))
        let label = UILabel(frame: CGRectMake(8,0,CGRectGetWidth(tableView.frame), 30))
        label.translatesAutoresizingMaskIntoConstraints = false
        
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
        
        label.text = NSLocalizedString("Faculty", comment:"Faculty label")
        return view;
        
    }
    
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        self.sendView("Course Overview", forModuleNamed: self.module!.name)
    }
    
    @IBAction func dismiss(sender: AnyObject) {
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
    func setupData() {
        let defaults = AppGroupUtilities.userDefaults()
        self.buildingsUrl = defaults?.stringForKey("urls-map-buildings")
        let request: NSFetchRequest = NSFetchRequest(entityName: "CourseDetail")
        request.predicate = NSPredicate(format: "termId == %@ && sectionId == %@", self.termId!, self.sectionId!)
        
        self.courseDetail = try! self.module?.managedObjectContext?.executeFetchRequest(request).last as? CourseDetail
        if let courseDetail = self.courseDetail {
            self.instructors = courseDetail.instructors.array as? [CourseDetailInstructor]
            self.meetingPatterns = courseDetail.meetingPatterns.array as? [CourseMeetingPattern]
        }
        self.tableView.reloadData()
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell : UITableViewCell
        
        switch indexPath.section {
        case 0:
            if self.meetingPatterns?.count > 0 {
                let meetingPattern = self.meetingPatterns![indexPath.row]
                cell = tableView.dequeueReusableCellWithIdentifier("Course Detail Meeting Pattern Cell", forIndexPath: indexPath) as UITableViewCell
                cell.userInteractionEnabled = true
                let label = cell.viewWithTag(1) as! UILabel
                let label2 = cell.viewWithTag(2) as! UILabel
                
                var daysOfClass = meetingPattern.daysOfWeek.componentsSeparatedByString(",") as [String]
                var shortStandaloneWeekdaySymbols = self.dateFormatter.shortStandaloneWeekdaySymbols
                var localizedDays = [String]()
                for i in 0 ..< daysOfClass.count {
                    let value = Int(daysOfClass[i])! - 1
                    localizedDays.append(shortStandaloneWeekdaySymbols[value])
                }
                
                //time
                let days: String = String(format: NSLocalizedString("days:", tableName: "Localizable", bundle: NSBundle.mainBundle(), value:"%@: ", comment: "days:"), localizedDays.joinWithSeparator(", "))
                var line1: String
                if meetingPattern.instructionalMethodCode != nil {
                    line1 = String(format: NSLocalizedString("days start - end method", tableName: "Localizable", bundle: NSBundle.mainBundle(), value: "%@ %@ - %@ %@", comment: "days start - end method"), days, self.displayTimeFormatter.stringFromDate(meetingPattern.startTime), self.displayTimeFormatter.stringFromDate(meetingPattern.endTime), meetingPattern.instructionalMethodCode)
                }
                else {
                    line1 = String(format: NSLocalizedString("days start - end", tableName: "Localizable", bundle: NSBundle.mainBundle(), value:"%@ %@ - %@", comment:"days start - end"), days, self.displayTimeFormatter.stringFromDate(meetingPattern.startTime), self.displayTimeFormatter.stringFromDate(meetingPattern.endTime))
                }
                
                let attributedLine1: NSMutableAttributedString = NSMutableAttributedString(string: line1)
                attributedLine1.addAttribute(NSFontAttributeName, value: UIFont.preferredFontForTextStyle(UIFontTextStyleHeadline), range: NSMakeRange(0, days.characters.count))
                label.translatesAutoresizingMaskIntoConstraints = false
                label.attributedText = attributedLine1
                
                //location
                var location: String = ""
                if meetingPattern.building != nil && meetingPattern.room != nil {
                    location = String(format: NSLocalizedString("%@, Room %@", comment: "label - building name, room number"), meetingPattern.building, meetingPattern.room)
                }
                else {
                    if let _ = meetingPattern.building {
                        location = meetingPattern.building
                    }
                    else {
                        if let _ = meetingPattern.room {
                            location = String(format: NSLocalizedString("Room %@", comment: "label - room number"), meetingPattern.room)
                        }
                    }
                }
                if let _ = self.buildingsUrl, _ = meetingPattern.buildingId {
                    
                    let underlineAttributes = [NSUnderlineStyleAttributeName: NSUnderlineStyle.StyleSingle.rawValue,
                        NSForegroundColorAttributeName: UIColor.primaryColor()]
                    let underlineAttributedString = NSAttributedString(string: location, attributes: underlineAttributes)
                    label2.attributedText = underlineAttributedString
                    
                } else {
                    label2.text = location
                }
                
                let imageView = cell.viewWithTag(3)
                imageView!.tintColor = UIColor.primaryColor()
            } else {
                cell = tableView.dequeueReusableCellWithIdentifier("Course Detail No Meeting Pattern Cell", forIndexPath: indexPath) as UITableViewCell
                cell.userInteractionEnabled = false
            }
            
        case 1:
            if self.instructors!.count > 0 {
                let instructor = self.instructors![indexPath.row]
                cell = tableView.dequeueReusableCellWithIdentifier("Course Detail Instructor Cell", forIndexPath: indexPath) as UITableViewCell
                let label = cell.viewWithTag(1) as! UILabel
                label.text = instructor.formattedName
                cell.userInteractionEnabled = true
            } else {
                cell = tableView.dequeueReusableCellWithIdentifier("Course Detail No Instructor Cell", forIndexPath: indexPath) as UITableViewCell
                cell.userInteractionEnabled = false
            }
            
        case 2:
            cell = tableView.dequeueReusableCellWithIdentifier("Course Detail Description Cell", forIndexPath: indexPath) as UITableViewCell
            let label = cell.viewWithTag(1) as! UILabel
            label.text = courseDetail?.courseDescription
            cell.userInteractionEnabled = false
        default:
            cell = UITableViewCell()
        }
        
        cell.layoutIfNeeded()
        
        return cell
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        switch indexPath.section {
        case 0:
            let mp: CourseMeetingPattern = self.meetingPatterns![indexPath.row]
            self.performSegueWithIdentifier("Show Course Location", sender: mp.buildingId)
        case 1:
            
            let instructor = self.instructors![indexPath.row]
            let defaults = AppGroupUtilities.userDefaults()
            var urlString : String?
            
            if ConfigurationManager.doesMobileServerSupportVersion("4.5") {
                urlString = defaults?.stringForKey("urls-directory-baseSearch")
            } else {
                urlString = defaults?.stringForKey("urls-directory-facultySearch")
            }
            
            let name : String
            if instructor.firstName != nil  && instructor.lastName != nil  {
                name = instructor.firstName + " " + instructor.lastName
            } else if instructor.firstName != nil  {
                name = instructor.firstName
            } else if instructor.lastName != nil {
                name = instructor.lastName
            } else {
                name = instructor.formattedName
            }
            
            let encodedSearchString = name.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())
            let encodedIdString = instructor.instructorId.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())
            urlString = "\(urlString!)?searchString=\(encodedSearchString!)&targetId=\(encodedIdString!)"
            let authenticatedRequest = AuthenticatedRequest()
            let responseData: NSData = authenticatedRequest.requestURL(NSURL(string: urlString!)!, fromView: self)
            let entries = DirectoryEntry.parseResponse(responseData)
            
            if entries.count == 0 {
                let alertController = UIAlertController(title: NSLocalizedString("Faculty", comment: "title for faculty no match"), message: NSLocalizedString("Person was not found", comment: "Person was not found"), preferredStyle: .Alert)
                let OKAction = UIAlertAction(title: "OK", style: .Default, handler: nil)
                alertController.addAction(OKAction)
                self.presentViewController(alertController, animated: true, completion: nil)
            } else if entries.count == 1 {
                self.performSegueWithIdentifier("Show Faculty Person", sender: entries[0])
            } else {
                self.performSegueWithIdentifier("Show Faculty List", sender: entries)
            }
            
            
        case 2:
            ()
        default:
            ()
        }
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        
        if segue.identifier == "Show Course Location" {
            self.sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionList_Select, withLabel: "Map Detail", withValue: nil, forModuleNamed: self.module!.name)
            let vc = segue.destinationViewController as! POIDetailViewController
            vc.buildingId = sender as? String
            vc.module = self.module
        } else if segue.identifier == "Show Faculty List" {
            let detailController = segue.destinationViewController as! DirectoryViewController
            detailController.entries = sender as! [DirectoryEntry];
            detailController.module = self.module;
        } else if segue.identifier == "Show Faculty Person" {
            let detailController = segue.destinationViewController as! DirectoryEntryViewController
            detailController.entry = sender as? DirectoryEntry;
            detailController.module = self.module;
        }
    }
}
