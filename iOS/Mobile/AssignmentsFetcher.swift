//
//  AssignmentsFetcher.swift
//  Mobile
//
//  Created by Jason Hocker on 5/19/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import UIKit

class AssignmentsFetcher: NSObject {
    
    class func fetch(context: NSManagedObjectContext, url: String) {
        
        let importContext = NSManagedObjectContext(concurrencyType: .PrivateQueueConcurrencyType)
        
        importContext.parentContext = context
        
        if let userid = CurrentUser.sharedInstance().userid {
            
            let urlString = NSString( format:"%@/%@/assignments", url, userid )
            let url: NSURL? = NSURL(string: urlString as String)
            
            let authenticatedRequest = AuthenticatedRequest()
            let responseData:NSData? = authenticatedRequest.requestURL(url, fromView: nil)
            
            if let response = responseData
            {
                NSNotificationCenter.defaultCenter().removeObserver(self, name:kLoginExecutorSuccess, object:nil)
                
                let json = JSON(data: response)
                
                let request = NSFetchRequest(entityName:"CourseAssignment")
                var oldObjects: [AnyObject]?
                do {
                    oldObjects = try importContext.executeFetchRequest(request)
                } catch {
                }
                
                for oldObject in oldObjects! {
                    importContext.deleteObject(oldObject as! NSManagedObject)
                }
                
                let assignmentList: Array<JSON> = json["assignments"].arrayValue
                
                for  jsonDictionary in assignmentList {
                    let entry:CourseAssignment = NSEntityDescription.insertNewObjectForEntityForName("CourseAssignment", inManagedObjectContext: importContext) as! CourseAssignment
                    entry.sectionId = jsonDictionary["sectionId"].stringValue
                    entry.courseName = jsonDictionary["courseName"].stringValue
                    entry.courseSectionNumber = jsonDictionary["courseSectionNumber"].stringValue
                    entry.name = jsonDictionary["name"].stringValue
                    entry.assignmentDescription = jsonDictionary["description"].stringValue
                    
                    let datetimeFormatter = NSDateFormatter()
                    datetimeFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
                    datetimeFormatter.timeZone = NSTimeZone(name:"UTC")
                    
                    entry.dueDate = datetimeFormatter.dateFromString(jsonDictionary["dueDate"].stringValue)
                    entry.url = jsonDictionary["url"].stringValue
                }
                
                do {
                    try importContext.save()
                } catch let saveError as NSError {
                    NSLog("save error: \(saveError.localizedDescription)")
                } catch {
                    
                }
            }

            do {
                try importContext.parentContext!.save()
            } catch let parentError as NSError {
                NSLog("Could not save to store after update to course assignments: \(parentError.localizedDescription)")
            } catch {
            }
        }
        
    }
}
