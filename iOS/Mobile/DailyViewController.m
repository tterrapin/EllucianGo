//
//  AssignmentsController.m
//  Mobile
//
//  Created by Alan McEwan on 11/21/14.
//  Copyright (c) 2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import "DailyViewController.h"
#import "CurrentUser.h"
#import "AuthenticatedRequest.h"
#import "EmptyTableViewCell.h"
#import "CourseAssignment.h"
#import "CourseAnnouncement.h"
#import "CourseEvent.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "CourseAssignmentDetailViewController.h"
#import "CourseEventsDetailViewController.h"
#import "CourseAnnouncementDetailViewController.h"


@interface DailyViewController ()

@property (strong, nonatomic) NSDateFormatter *datetimeFormatter;
@property (strong, nonatomic) NSDateFormatter *datetimeOutputFormatter;

@end

@implementation DailyViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.assignmentTableView.delegate = self;
    self.eventTableView.delegate = self;
    self.announcementTableView.delegate = self;
    
    self.navigationController.navigationBar.translucent = NO;
    
    NSError *error;
    if (![[self assignmentsFetchedResultsController] performFetch:&error]) {
        // Update to handle the error appropriately.
        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
    }
    
    if (![[self eventsFetchedResultsController] performFetch:&error]) {
        // Update to handle the error appropriately.
        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
    }
    
    if (![[self announcementsFetchedResultsController] performFetch:&error]) {
        // Update to handle the error appropriately.
        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
    }
    
    self.navigationItem.title = self.courseNameAndSectionNumber;
    self.title = self.module.name;
    
    if([CurrentUser sharedInstance].isLoggedIn) {
        [self fetchAssignments:self];
        //[self fetchEvents:self];
        //[self fetchAnnouncements:self];
    }

    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(fetchAssignments:)    name:kLoginExecutorSuccess object:nil];
    //[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(fetchEvents:)         name:kLoginExecutorSuccess object:nil];
    //[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(fetchAnnouncements:)  name:kLoginExecutorSuccess object:nil];
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    [self sendView:@"Daily view" forModuleNamed:self.module.name];
}

- (NSFetchedResultsController *)assignmentsFetchedResultsController {
    
    if (_assignmentsFetchedResultsController != nil) {
        return _assignmentsFetchedResultsController;
    }
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription
                                   entityForName:@"CourseAssignment" inManagedObjectContext:self.module.managedObjectContext];
    [fetchRequest setEntity:entity];
    //fetchRequest.predicate = [NSPredicate predicateWithFormat:@"dueDate == %@", self.dueDate];
    NSArray *sortDescriptors = [NSArray arrayWithObject:[[NSSortDescriptor alloc]
                                                         initWithKey:@"dueDate" ascending:NO]];
    [fetchRequest setSortDescriptors:sortDescriptors];
    
    NSFetchedResultsController *theFetchedResultsController =
    [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest
                                        managedObjectContext:self.module.managedObjectContext
                                        sectionNameKeyPath:@"sectionHeaderName"
                                        cacheName:nil];
    self.assignmentsFetchedResultsController = theFetchedResultsController;
    _assignmentsFetchedResultsController.delegate = self;
    
    return _assignmentsFetchedResultsController;
    
}

- (NSFetchedResultsController *)eventsFetchedResultsController {
    
    if (_eventsFetchedResultsController != nil) {
        return _eventsFetchedResultsController;
    }
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"CourseEvent" inManagedObjectContext:self.module.managedObjectContext];
    [fetchRequest setEntity:entity];
    //fetchRequest.predicate = [NSPredicate predicateWithFormat:@"sectionId == %@", self.sectionId];
    NSArray *sortDescriptors = [NSArray arrayWithObject:[[NSSortDescriptor alloc]
                                                         initWithKey:@"startDate" ascending:NO]];
    [fetchRequest setSortDescriptors:sortDescriptors];
    
    NSFetchedResultsController *theFetchedResultsController =
    [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest
                                        managedObjectContext:self.module.managedObjectContext sectionNameKeyPath:nil
                                                   cacheName:nil];
    self.eventsFetchedResultsController = theFetchedResultsController;
    _eventsFetchedResultsController.delegate = self;
    
    return _eventsFetchedResultsController;
    
}

- (NSFetchedResultsController *)announcementsFetchedResultsController {
    
    if (_announcementsFetchedResultsController != nil) {
        return _announcementsFetchedResultsController;
    }
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"CourseAnnouncement" inManagedObjectContext:self.module.managedObjectContext];
    [fetchRequest setEntity:entity];
    //fetchRequest.predicate = [NSPredicate predicateWithFormat:@"sectionId == %@", self.sectionId];
    NSArray *sortDescriptors = [NSArray arrayWithObject:[[NSSortDescriptor alloc]
                                                         initWithKey:@"date" ascending:NO]];
    [fetchRequest setSortDescriptors:sortDescriptors];
    
    NSFetchedResultsController *theFetchedResultsController =
    [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest
                                        managedObjectContext:self.module.managedObjectContext sectionNameKeyPath:nil
                                                   cacheName:nil];
    self.announcementsFetchedResultsController = theFetchedResultsController;
    _announcementsFetchedResultsController.delegate = self;
    
    return _announcementsFetchedResultsController;
    
}

- (IBAction)dismiss:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    NSInteger count = 0;
    
    if ( tableView.tag == 300 ) {
        count = [[self.assignmentsFetchedResultsController sections] count];
    } else if ( tableView.tag == 301 ) {
        count = [[self.eventsFetchedResultsController sections] count];
    } else if ( tableView.tag == 302 ) {
        count = [[self.announcementsFetchedResultsController sections] count];
    }
    
    return count;
}


- (NSDateFormatter *)datetimeFormatter {
    if (_datetimeFormatter == nil) {
        _datetimeFormatter = [[NSDateFormatter alloc] init];
        [_datetimeFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss'Z'"];
        [_datetimeFormatter setTimeZone:[NSTimeZone timeZoneWithName:@"UTC"]];
    }
    return _datetimeFormatter;
}

- (NSDateFormatter *)datetimeOutputFormatter {
    if (_datetimeOutputFormatter == nil) {
        _datetimeOutputFormatter = [[NSDateFormatter alloc] init];
        [_datetimeOutputFormatter setDateStyle:NSDateFormatterShortStyle];
        [_datetimeOutputFormatter setTimeStyle:NSDateFormatterShortStyle];
    }
    return _datetimeOutputFormatter;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    
    id <NSFetchedResultsSectionInfo> theSection = nil;
    
    if ( tableView.tag == 300 ) {
        theSection = [[self.assignmentsFetchedResultsController sections] objectAtIndex:section];
    } else if ( tableView.tag == 301 ) {
        theSection = [[self.eventsFetchedResultsController sections] objectAtIndex:section];
    } else if ( tableView.tag == 302 ) {
        theSection = [[self.announcementsFetchedResultsController sections] objectAtIndex:section];
    }
    
    return [theSection name];

}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath;
{
    return 66;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSInteger numberOfObjects = 0;
    id sectionInfo = nil;
    
    if (tableView.tag == 300)
    {
        sectionInfo = [[_assignmentsFetchedResultsController sections] objectAtIndex:section];
        numberOfObjects = [sectionInfo numberOfObjects];
    } else if (tableView.tag == 301)
    {
        sectionInfo = [[_eventsFetchedResultsController sections] objectAtIndex:section];
        numberOfObjects = [sectionInfo numberOfObjects];
    } else if (tableView.tag == 302)
    {
        sectionInfo = [[_announcementsFetchedResultsController sections] objectAtIndex:section];
        numberOfObjects = [sectionInfo numberOfObjects];
    }

    return numberOfObjects;
}


#pragma mark - Table view delegate

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = nil;
    if (tableView.tag == 300) {
        cell = [self.assignmentTableView dequeueReusableCellWithIdentifier:@"Daily Assignment Cell"];
        CourseAssignment *assignment = [_assignmentsFetchedResultsController objectAtIndexPath:indexPath];
        
        UILabel *nameLabel = (UILabel *)[cell viewWithTag:100];
        nameLabel.text = assignment.name;
        UILabel *dueDateLabel = (UILabel *)[cell viewWithTag:101];
        if(assignment.dueDate) {
            dueDateLabel.text = [NSString stringWithFormat:NSLocalizedString(@"Due: %@", @"due date label with date"), [self.datetimeOutputFormatter stringFromDate:assignment.dueDate]];
    }   else {
            dueDateLabel.text = NSLocalizedString(@"Due: None assigned", "no due date for assignment");
        }
    }
    else if (tableView.tag == 301) {
        cell = [self.eventTableView dequeueReusableCellWithIdentifier:@"Daily Event Cell"];
        CourseEvent *event = [_eventsFetchedResultsController objectAtIndexPath:indexPath];
        
        UILabel *nameLabel = (UILabel *)[cell viewWithTag:100];
        nameLabel.text = event.title;
        UILabel *startDateLabel = (UILabel *)[cell viewWithTag:101];
        if(event.startDate) {
            startDateLabel.text = [NSString stringWithFormat:NSLocalizedString(@"Start: %@", @"start date label with date"), [self.datetimeOutputFormatter stringFromDate:event.startDate]];
        }   else {
            startDateLabel.text = NSLocalizedString(@"Start: None assigned", "no start date for event");
        }
    }
    else if (tableView.tag == 302) {
        cell = [self.assignmentTableView dequeueReusableCellWithIdentifier:@"Daily Announcement Cell"];
        CourseAnnouncement *announcement = [_announcementsFetchedResultsController objectAtIndexPath:indexPath];
        
        UILabel *nameLabel = (UILabel *)[cell viewWithTag:100];
        nameLabel.text = announcement.title;
        UILabel *dateLabel = (UILabel *)[cell viewWithTag:101];
        if(announcement.date) {
            dateLabel.text = [NSString stringWithFormat:NSLocalizedString(@"Due: %@", @"due date label with date"), [self.datetimeOutputFormatter stringFromDate:announcement.date]];
        }   else {
            dateLabel.text = NSLocalizedString(@"Due: None assigned", "no due date for announcement");
        }
    }

    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if ( tableView.tag == 300 ) {
        [self performSegueWithIdentifier:@"Show Assignment Detail" sender:self.assignmentTableView];
    } else if ( tableView.tag == 301 ) {
        [self performSegueWithIdentifier:@"Show Event Detail" sender:self.eventTableView];
    } else if ( tableView.tag == 302 ) {
        [self performSegueWithIdentifier:@"Show Announcement Detail" sender:self.announcementTableView];
    }
}

- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller {
    [self.assignmentTableView beginUpdates];
    [self.eventTableView beginUpdates];
    [self.announcementTableView beginUpdates];
}

- (void)controller:(NSFetchedResultsController *)controller didChangeObject:(id)anObject atIndexPath:(NSIndexPath *)indexPath forChangeType:(NSFetchedResultsChangeType)type newIndexPath:(NSIndexPath *)newIndexPath {

    UITableView *tableView = nil;
    
    if ( controller == _assignmentsFetchedResultsController ){
        tableView = self.assignmentTableView;
    } else if ( controller == _announcementsFetchedResultsController ) {
        tableView = self.announcementTableView;
    } else if ( controller == _eventsFetchedResultsController ) {
        tableView = self.eventTableView;
    }
    
    switch(type) {
            
        case NSFetchedResultsChangeInsert:
            [tableView insertRowsAtIndexPaths:[NSArray arrayWithObject:newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeUpdate:
            [tableView reloadRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeMove:
            [tableView deleteRowsAtIndexPaths:[NSArray
                                               arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
            [tableView insertRowsAtIndexPaths:[NSArray
                                               arrayWithObject:newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}


- (void)controller:(NSFetchedResultsController *)controller didChangeSection:(id )sectionInfo atIndex:(NSUInteger)sectionIndex forChangeType:(NSFetchedResultsChangeType)type {
    
    
    UITableView *tableView = nil;
    
    if ( controller == _assignmentsFetchedResultsController ){
        tableView = self.assignmentTableView;
    } else if ( controller == _announcementsFetchedResultsController ) {
        tableView = self.announcementTableView;
    } else if ( controller == _eventsFetchedResultsController ) {
        tableView = self.eventTableView;
    }
    
    switch(type) {
            
        case NSFetchedResultsChangeInsert:
            [tableView insertSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
        case NSFetchedResultsChangeDelete:
            [tableView deleteSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
        case NSFetchedResultsChangeMove:
            break;
        case NSFetchedResultsChangeUpdate:
            break;
            
    }
}

- (void)controllerDidChangeContent:(NSFetchedResultsController *)controller {
    
    if ( controller == _assignmentsFetchedResultsController ) {
        [self.assignmentTableView endUpdates];
    } else if ( controller == _announcementsFetchedResultsController) {
        [self.announcementTableView endUpdates];
    } else if ( controller == _eventsFetchedResultsController) {
        [self.eventTableView endUpdates];
    }
}

#pragma mark - fetch announcements
- (void) fetchAnnouncements:(id)sender {
    
    NSManagedObjectContext *importContext = [[NSManagedObjectContext alloc] initWithConcurrencyType:NSPrivateQueueConcurrencyType];
    importContext.parentContext = self.module.managedObjectContext;
    
    NSString *urlString = [NSString stringWithFormat:@"%@/%@/announcements", [self.module propertyForKey:@"assignments"], [[[CurrentUser sharedInstance] userid]  stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding]];
    
    NSLog(@"announcements controller urlString:%@", urlString);
    
    [importContext performBlock: ^{
        
        NSError *error;
        [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
        AuthenticatedRequest *authenticatedRequet = [AuthenticatedRequest new];
        NSData *responseData = [authenticatedRequet requestURL:[NSURL URLWithString:urlString] fromView:self];
        
        
        [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
        if(responseData) {
            NSArray* json = [NSJSONSerialization
                             JSONObjectWithData:responseData
                             options:kNilOptions
                             error:&error];
            
            NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"CourseAnnouncement"];
            //request.predicate = [NSPredicate predicateWithFormat:@"termId == %@ && sectionId == %@", self.termId, self.sectionId];
            request.predicate = [NSPredicate predicateWithFormat:@"sectionId == %@", self.sectionId];
            
            NSArray * oldObjects = [importContext executeFetchRequest:request error:&error];
            for (CourseAnnouncement* oldObject in oldObjects) {
                [importContext deleteObject:oldObject];
            }
            
            for(NSDictionary *jsonDictionary in [json valueForKey:@"items"]) {
                CourseAnnouncement *entry = [NSEntityDescription insertNewObjectForEntityForName:@"CourseAnnouncement" inManagedObjectContext:importContext];
                
                entry.sectionId = self.sectionId;
                entry.title = [jsonDictionary objectForKey:@"title"];
                if([jsonDictionary objectForKey:@"content"] != [NSNull null]) {
                    entry.content = [jsonDictionary objectForKey:@"content"];
                }
                if([jsonDictionary objectForKey:@"date"] != [NSNull null]) {
                    entry.date = [self.datetimeFormatter dateFromString:[jsonDictionary objectForKey:@"date"]];
                }
                entry.website = [jsonDictionary objectForKey:@"website"];
            }
        }
        
        if (![importContext save:&error]) {
            NSLog(@"Could not save to main context after update to course events: %@", [error userInfo]);
        }
        
        [importContext.parentContext performBlock:^{
            NSError *parentError = nil;
            if(![importContext.parentContext save:&parentError]) {
                NSLog(@"Could not save to store after update to course events: %@", [error userInfo]);
            }
        }];
    }
     ];
    
}
- (IBAction)chooseScheduledOrUnscheduled:(id)sender {
    
    [self sendEventWithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionInvoke_Native withLabel:@"Change daily view" withValue:nil forModuleNamed:self.module.name];
    UISegmentedControl *segmentedControl = (UISegmentedControl *)sender;
    switch([segmentedControl selectedSegmentIndex]) {
        case 0: {
            //self.mapView.mapType = MKMapTypeStandard;
            NSLog(@"Show all scheduled");
            break;
        }
        case 1: {
            //self.mapView.mapType = MKMapTypeSatellite;
            NSLog(@"Show unscheduled");
            break;
        }
    }

}

#pragma mark - fetch assignements
- (void) fetchAssignments:(id)sender {
    
    NSManagedObjectContext *importContext = [[NSManagedObjectContext alloc] initWithConcurrencyType:NSPrivateQueueConcurrencyType];
    importContext.parentContext = self.module.managedObjectContext;
    
    NSString *urlString = [NSString stringWithFormat:@"%@/%@/assignments", [self.module propertyForKey:@"assignments"], [[[CurrentUser sharedInstance] userid]  stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding]];
    
    NSLog(@"assignments controller urlString:%@", urlString);
    
    [importContext performBlock: ^{
        
        NSError *error;
        [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
        AuthenticatedRequest *authenticatedRequet = [AuthenticatedRequest new];

        NSData *responseData = [authenticatedRequet requestURL:[NSURL URLWithString:urlString] fromView:self];
        
        [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
        if(responseData) {
            NSArray* json = [NSJSONSerialization
                             JSONObjectWithData:responseData
                             options:kNilOptions
                             error:&error];
            
            NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"CourseAssignment"];
            //request.predicate = [NSPredicate predicateWithFormat:@"termId == %@ && sectionId == %@", self.termId, self.sectionId];
            //request.predicate = [NSPredicate predicateWithFormat:@"sectionId == %@", self.sectionId];
            
            NSArray * oldObjects = [importContext executeFetchRequest:request error:&error];
            for (CourseAssignment* oldObject in oldObjects) {
                [importContext deleteObject:oldObject];
            }
            
            for(NSDictionary *jsonDictionary in [json valueForKey:@"assignments"]) {
                CourseAssignment *entry = [NSEntityDescription insertNewObjectForEntityForName:@"CourseAssignment" inManagedObjectContext:importContext];
                
                entry.sectionId = self.sectionId;
                entry.name = [jsonDictionary objectForKey:@"name"];
                if([jsonDictionary objectForKey:@"description"] != [NSNull null]) {
                    entry.assignmentDescription = [jsonDictionary objectForKey:@"description"];
                }
                if([jsonDictionary objectForKey:@"dueDate"] != [NSNull null]) {
                    entry.dueDate = [self.datetimeFormatter dateFromString:[jsonDictionary objectForKey:@"dueDate"]];
                }
                entry.url = [jsonDictionary objectForKey:@"url"];
            }
        }
        
        if (![importContext save:&error]) {
            NSLog(@"Could not save to main context after update to course assignments: %@", [error userInfo]);
        }
        
        [importContext.parentContext performBlock:^{
            NSError *parentError = nil;
            if(![importContext.parentContext save:&parentError]) {
                NSLog(@"Could not save to store after update to course assignments: %@", [error userInfo]);
            }
        }];
    }
     ];
}

#pragma mark - fetch events
- (void) fetchEvents:(id)sender {
    
    NSManagedObjectContext *importContext = [[NSManagedObjectContext alloc] initWithConcurrencyType:NSPrivateQueueConcurrencyType];
    importContext.parentContext = self.module.managedObjectContext;
    
    NSString *urlString = [NSString stringWithFormat:@"%@/%@/events", [self.module propertyForKey:@"assignments"], [[[CurrentUser sharedInstance] userid]  stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding]];
    
    NSLog(@"events controller urlString:%@", urlString);
    
    [importContext performBlock: ^{
        
        NSError *error;
        [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
        AuthenticatedRequest *authenticatedRequet = [AuthenticatedRequest new];
        NSData *responseData = [authenticatedRequet requestURL:[NSURL URLWithString:urlString] fromView:self];
        
        
        [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
        if(responseData) {
            NSArray* json = [NSJSONSerialization
                             JSONObjectWithData:responseData
                             options:kNilOptions
                             error:&error];
            
            NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"CourseEvent"];
            //request.predicate = [NSPredicate predicateWithFormat:@"termId == %@ && sectionId == %@", self.termId, self.sectionId];
            request.predicate = [NSPredicate predicateWithFormat:@"sectionId == %@", self.sectionId];
            
            NSArray * oldObjects = [importContext executeFetchRequest:request error:&error];
            for (CourseEvent* oldObject in oldObjects) {
                [importContext deleteObject:oldObject];
            }
            
            for(NSDictionary *jsonDictionary in [json valueForKey:@"events"]) {
                CourseEvent *entry = [NSEntityDescription insertNewObjectForEntityForName:@"CourseEvent" inManagedObjectContext:importContext];
                
                entry.sectionId = self.sectionId;
                entry.title = [jsonDictionary objectForKey:@"title"];
                if([jsonDictionary objectForKey:@"description"] != [NSNull null]) {
                    entry.eventDescription = [jsonDictionary objectForKey:@"description"];
                }
                entry.startDate = [self.datetimeFormatter dateFromString:[jsonDictionary objectForKey:@"startDate"]];
                entry.endDate = [self.datetimeFormatter dateFromString:[jsonDictionary objectForKey:@"endDate"]];
                if([jsonDictionary objectForKey:@"location"] != [NSNull null]) {
                    entry.location = [jsonDictionary objectForKey:@"location"];
                }
            }
        }
        
        if (![importContext save:&error]) {
            NSLog(@"Could not save to main context after update to course events: %@", [error userInfo]);
        }
        
        [importContext.parentContext performBlock:^{
            NSError *parentError = nil;
            if(![importContext.parentContext save:&parentError]) {
                NSLog(@"Could not save to store after update to course events: %@", [error userInfo]);
            }
        }];
    }
     ];
    
}


- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:@"Show Assignment Detail"]){
        UITableView *tableView = sender;
        NSIndexPath *indexPath = [tableView indexPathForSelectedRow];
        CourseAssignment *assignment = [self.assignmentsFetchedResultsController objectAtIndexPath:indexPath];
        
        CourseAssignmentDetailViewController *detailController = [segue destinationViewController];
        detailController.itemTitle = assignment.name;
        detailController.itemContent = assignment.assignmentDescription;
        detailController.itemLink = [assignment.url stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
        detailController.itemPostDateTime = assignment.dueDate;
        detailController.module = self.module;

    } else if ([[segue identifier] isEqualToString:@"Show Announcment Detail"]){
        UITableView *tableView = sender;
        NSIndexPath *indexPath = [tableView indexPathForSelectedRow];
        CourseAnnouncement *announcement = [self.announcementsFetchedResultsController objectAtIndexPath:indexPath];
        
        CourseAnnouncementDetailViewController *detailController = [segue destinationViewController];
        detailController.itemTitle = announcement.title;
        detailController.itemContent = announcement.content;
        detailController.itemPostDateTime = announcement.date;
        detailController.module = self.module;
        
    } else if ([[segue identifier] isEqualToString:@"Show Event Detail"]){
        UITableView *tableView = sender;
        NSIndexPath *indexPath = [tableView indexPathForSelectedRow];
        CourseEvent *event = [self.eventsFetchedResultsController objectAtIndexPath:indexPath];
        
        CourseEventsDetailViewController *detailController = [segue destinationViewController];
        detailController.eventTitle = event.title;
        detailController.eventDescription = event.eventDescription;
        detailController.startDate = event.startDate;
        detailController.endDate = event.endDate;
        detailController.module = self.module;
    }
}

@end

