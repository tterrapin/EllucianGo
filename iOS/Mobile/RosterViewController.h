//
//  RosterViewController.h
//  Mobile
//
//  Created by Jason Hocker on 10/2/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Module+Attributes.h"
#import "CourseRoster.h"
#import "EllucianSectionedUITableViewController.h"

@interface RosterViewController : EllucianSectionedUITableViewController<NSFetchedResultsControllerDelegate>

@property (strong, nonatomic) NSString *termId;
@property (strong, nonatomic) NSString *sectionId;
@property (strong, nonatomic) NSString *courseNameAndSectionNumber;
@property (strong, nonatomic) Module *module;
@property (strong, nonatomic) NSFetchedResultsController *fetchedResultsController;
- (IBAction)dismiss:(id)sender;

@end
