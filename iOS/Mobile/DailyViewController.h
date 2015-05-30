//
//  AssignmentsController.h
//  Mobile
//
//  Created by Alan McEwan on 11/21/14.
//  Copyright (c) 2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import "EllucianUITableViewController.h"

#import "Module+Attributes.h"

@interface DailyViewController : UIViewController<UIActionSheetDelegate, UITableViewDataSource, UITableViewDelegate, NSFetchedResultsControllerDelegate>

@property (strong, nonatomic) NSString *termId;
@property (strong, nonatomic) NSString *sectionId;
@property (strong, nonatomic) NSString *courseNameAndSectionNumber;
@property (strong, nonatomic) NSString *dueDate;

@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (weak, nonatomic) IBOutlet UITableView *assignmentTableView;
@property (weak, nonatomic) IBOutlet UITableView *eventTableView;
@property (weak, nonatomic) IBOutlet UITableView *announcementTableView;

@property (strong, nonatomic) Module *module;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *dueDateSelectionButton;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *noDueDateSelectionButton;


@property (strong, nonatomic) NSFetchedResultsController *assignmentsFetchedResultsController;
@property (strong, nonatomic) NSFetchedResultsController *eventsFetchedResultsController;
@property (strong, nonatomic) NSFetchedResultsController *announcementsFetchedResultsController;
- (IBAction)dismiss:(id)sender;

@end