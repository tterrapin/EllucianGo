//
//  GradesFetchOperation.swift
//  Mobile
//
//  Created by Jason Hocker on 9/10/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class GradesFetchOperation : NSOperation {
    
    private var module : Module?
    private var view : UIViewController?
    
    let dateFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
        }()
    let datetimeFormatter : NSDateFormatter = {
        var formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ"
        return formatter
        }()
    
    init (module: Module?, view: UIViewController?) {
        self.module = module
        self.view = view
    }
    
    override func main() {
        
        if let userid = CurrentUser.sharedInstance().userid {
            let urlBase = self.module?.propertyForKey("grades")
            let urlString = "\(urlBase!)/\(userid.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet())!)"
            
            let privateContext = NSManagedObjectContext(concurrencyType: .PrivateQueueConcurrencyType)
            privateContext.parentContext = self.module?.managedObjectContext
            privateContext.undoManager = nil
            
            privateContext.performBlock { () -> Void in
                
                do {
                    let authenticatedRequest = AuthenticatedRequest()
                    let responseData = authenticatedRequest.requestURL(NSURL(string: urlString), fromView: self.view)
                    let json = JSON(data: responseData)
                    
                    var previousTerms = [GradeTerm]()
                    var existingTerms = [GradeTerm]()

                    
                    let termRequest = NSFetchRequest(entityName: "GradeTerm")
                    let oldObjects = try privateContext
                        .executeFetchRequest(termRequest) as! [GradeTerm]
                    for oldObject in oldObjects {
                        previousTerms.append(oldObject)
                    }
                    
                    for termJson in json["terms"].array! {
                        
                        let termId = termJson["id"].string;
                        var gradeTerm : GradeTerm
                        
                        let filteredArray = previousTerms.filter({
                            let gradeTerm = $0 as GradeTerm
                            return gradeTerm.termId == termId;
                        })
                        if filteredArray.count > 0 {
                            gradeTerm = filteredArray[0]
                            existingTerms.append(gradeTerm)
                        } else {
                            gradeTerm = NSEntityDescription.insertNewObjectForEntityForName("GradeTerm", inManagedObjectContext: privateContext) as! GradeTerm
                            gradeTerm.termId = termJson["id"].string
                            gradeTerm.name = termJson["name"].string
                            gradeTerm.startDate = self.dateFormatter.dateFromString(termJson["startDate"].string!)
                            gradeTerm.endDate = self.dateFormatter.dateFromString(termJson["endDate"].string!)
                        }
                        
                        var previousCourses = [GradeCourse]()
                        var existingCourses = [GradeCourse]()
                        
                        for oldObject in gradeTerm.courses {
                            previousCourses.append(oldObject as! GradeCourse)
                        }
                        
                        for courseJson in termJson["sections"].array! {
                            
                            let sectionId = courseJson["sectionId"].string;
                            var gradeCourse : GradeCourse
                            
                            let filteredArray = previousCourses.filter({
                                let gradeCourse = $0 as GradeCourse
                                return gradeCourse.sectionId == sectionId;
                            })
                            if filteredArray.count > 0 {
                                gradeCourse = filteredArray[0]
                                existingCourses.append(gradeCourse)
                            } else {
                                gradeCourse = NSEntityDescription.insertNewObjectForEntityForName("GradeCourse", inManagedObjectContext: privateContext) as! GradeCourse
                                gradeCourse.sectionId = courseJson["sectionId"].string
                                gradeCourse.courseName = courseJson["courseName"].string
                                if let sectionTitle = courseJson["sectionTitle"].string where sectionTitle != "" {
                                    gradeCourse.sectionTitle = sectionTitle
                                }
                                if let courseSectionNumber = courseJson["courseSectionNumber"].string where courseSectionNumber != "" {
                                    gradeCourse.courseSectionNumber = courseSectionNumber
                                }
                                //rdar://10114310
                                gradeTerm.addCoursesObject(gradeCourse)
                                gradeCourse.term = gradeTerm
                            }
                            
                            var previousGrades = [Grade]()
                            var existingGrades = [Grade]()
                            for oldObject in gradeCourse.grades {
                                previousGrades.append(oldObject as! Grade)
                            }
                            
                            for gradeJson in courseJson["grades"].array! {
                                let name = gradeJson["name"].string;
                                let value = gradeJson["value"].string
                                var grade : Grade
                                
                                let filteredArray = previousGrades.filter({
                                    let grade = $0 as Grade
                                    return grade.name == name && grade.value == value;
                                })
                                if filteredArray.count > 0 {
                                    grade = filteredArray[0]
                                    existingGrades.append(grade)
                                } else {
                                    grade = NSEntityDescription.insertNewObjectForEntityForName("Grade", inManagedObjectContext: privateContext) as! Grade
                                    grade.name = gradeJson["name"].string
                                    grade.value = gradeJson["value"].string
                                    if let updated = gradeJson["updated"].string where updated != "" {
                                        grade.lastUpdated = self.datetimeFormatter.dateFromString(updated)
                                    }
                                    gradeCourse.addGradesObject(grade)
                                    grade.course = gradeCourse
                                }
                            }
                            for oldObject in previousGrades {
                                if !previousGrades.contains(oldObject) {
                                    privateContext.deleteObject(oldObject)
                                }
                            }
                        }
                        
                        for oldObject in previousCourses {
                            if !existingCourses.contains(oldObject) {
                                privateContext.deleteObject(oldObject)
                            }
                        }
                    }
                    
                    for oldObject in previousTerms {
                        if !existingTerms.contains(oldObject) {
                            privateContext.deleteObject(oldObject)
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
                } catch let error {
                    print (error)
                }
                
            }
        }
    }
}
