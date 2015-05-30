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
        
        var importContext = NSManagedObjectContext(concurrencyType: .PrivateQueueConcurrencyType)
        
        importContext.parentContext = context
        
        if let userid = CurrentUser.sharedInstance().userid {
            
            var urlString = NSString( format:"%@/%@/assignments", url, userid )
            var url: NSURL? = NSURL(string: urlString as String)
            
            
            
            var error:NSError?
            
            var authenticatedRequest = AuthenticatedRequest()
            var responseData:NSData? = authenticatedRequest.requestURL(url, fromView: nil)
            
            if let response = responseData
            {
                NSNotificationCenter.defaultCenter().removeObserver(self, name:kLoginExecutorSuccess, object:nil)
                
                let json = JSON(data: response)
                
                var request = NSFetchRequest(entityName:"CourseAssignment")
                var oldObjects = importContext.executeFetchRequest(request, error:&error)
                
                for oldObject in oldObjects! {
                    importContext.deleteObject(oldObject as! NSManagedObject)
                }
                
                let assignmentList: Array<JSON> = json["assignments"].arrayValue
                
                for  jsonDictionary in assignmentList {
                    var entry:CourseAssignment = NSEntityDescription.insertNewObjectForEntityForName("CourseAssignment", inManagedObjectContext: importContext) as! CourseAssignment
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
                
                var saveError: NSError?
                importContext.save(&saveError)
                if !importContext.save(&saveError) {
                    NSLog("save error: \(saveError!.localizedDescription)")
                }
            }
            
            var parentError: NSError?
            if !importContext.parentContext!.save(&parentError)
            {
                NSLog("Could not save to store after update to course assignments: \(parentError!.localizedDescription)")
            }
        }
        
    }
}
