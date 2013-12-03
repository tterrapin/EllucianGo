//
//  CourseGradesViewController.h
//  Mobile
//
//  Created by Jason Hocker on 10/17/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Module+Attributes.h"
#import "CourseSection.h"
#import "Grade.h"
#import "GradesCell.h"
#import "GradeTerm.h"
#import "GradeCourse.h"
#import "EllucianUITableViewController.h"

@interface CourseGradesViewController : EllucianUITableViewController<NSFetchedResultsControllerDelegate>

@property (strong, nonatomic) NSString *termId;
@property (strong, nonatomic) NSString *sectionId;
@property (strong, nonatomic) NSString *courseNameAndSectionNumber;
@property (strong, nonatomic) Module *module;
@property (strong, nonatomic) NSFetchedResultsController *fetchedResultsController;
- (IBAction)dismiss:(id)sender;

@end
