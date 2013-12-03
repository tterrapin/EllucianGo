//
//  CourseAnnouncementsViewController.h
//  Mobile
//
//  Created by jkh on 6/4/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import "EllucianUITableViewController.h"
#import "EllucianUITableViewController.h"
#import "Module+Attributes.h"

@interface CourseAnnouncementsViewController : EllucianUITableViewController<NSFetchedResultsControllerDelegate>

@property (strong, nonatomic) NSString *termId;
@property (strong, nonatomic) NSString *sectionId;
@property (strong, nonatomic) NSString *courseNameAndSectionNumber;
@property (strong, nonatomic) Module *module;
@property (strong, nonatomic) NSFetchedResultsController *fetchedResultsController;
- (IBAction)dismiss:(id)sender;

@end