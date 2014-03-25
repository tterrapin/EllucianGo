//
//  NotificationsViewController.m
//  Mobile
//
//  Created by Jason Hocker on 9/27/12.
//  Copyright (c) 2012-2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import "NotificationsViewController.h"
#import "CurrentUser.h"
#import "EmptyTableViewCell.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "AppDelegate.h"

@interface NotificationsViewController ()

@property (nonatomic, strong) UIView *nomatchesView;
@property (nonatomic, assign) NSUInteger rowCount;
@end

static NSString* requestedNotificationId;
static Notification* requestedNotification;

@implementation NotificationsViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.navigationController.navigationBar.translucent = NO;
    
    NSError *error;
	if (![[self fetchedResultsController] performFetch:&error]) {
		NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
	}
    
    self.title = self.module.name;

    if([CurrentUser sharedInstance].isLoggedIn) {
        if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
        {
            [self selectFirst];
        }
        [self fetchNotifications];
    }
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(fetchNotifications:) name:kLoginExecutorSuccess object:nil];
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Notifications Detail" forModuleNamed:self.module.name];
    
    if (requestedNotificationId) {
        // see if the requested notification id is already loaded and can be shown
        [self showDetailForRequestedNotification];
    }
}

- (NSFetchedResultsController *)fetchedResultsController {
    
    if (_fetchedResultsController != nil) {
        return _fetchedResultsController;
    }
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription
                                   entityForName:@"Notification" inManagedObjectContext:self.module.managedObjectContext];
    [fetchRequest setEntity:entity];
    
    NSSortDescriptor *sortSticky = [[NSSortDescriptor alloc]
                              initWithKey:@"sticky" ascending:NO];
    NSSortDescriptor *sortDate = [[NSSortDescriptor alloc]
                              initWithKey:@"noticeDate" ascending:NO];
    NSSortDescriptor *sortTitle = [[NSSortDescriptor alloc]
                              initWithKey:@"title" ascending:YES];
    [fetchRequest setSortDescriptors:[NSArray arrayWithObjects:sortSticky, sortDate, sortTitle, nil]];
    
    [fetchRequest setFetchBatchSize:20];
    
    NSFetchedResultsController *theFetchedResultsController =
    [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest
                                        managedObjectContext:self.module.managedObjectContext sectionNameKeyPath:nil
                                                   cacheName:nil];
    self.fetchedResultsController = theFetchedResultsController;
    _fetchedResultsController.delegate = self;
    
    return _fetchedResultsController;
    
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    id  sectionInfo = [[_fetchedResultsController sections] objectAtIndex:section];
    NSInteger numberOfObjects = [sectionInfo numberOfObjects];
    
    if(numberOfObjects == 0 ){
        [self showNoDataView:NSLocalizedString(@"No Notifications to display", @"message when there are no notifications to display")];
    } else {
        [self hideNoDataView];
    }
    return numberOfObjects;
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
    {
        Notification *selectedNotification = [self.fetchedResultsController objectAtIndexPath:indexPath];
        if (_detailSelectionDelegate) {
            [_detailSelectionDelegate selectedDetail:selectedNotification withModule:self.module];
        }
    } else if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
        [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionList_Select withLabel:@"Select Notification" withValue:nil forModuleNamed:self.module.name];
        [self performSegueWithIdentifier:@"Show Notification Detail" sender:self];
    }
}

+ (void)requestNotificationDetailById:(NSString*) notificationId
{
    requestedNotificationId = notificationId;
}

- (void)showDetailForRequestedNotification
{
    if (requestedNotificationId) {
        NSArray* notifications = [self.fetchedResultsController fetchedObjects];
        Notification* notification = nil;
        for (Notification* testNotification in notifications) {
            NSString* testNotificationId = testNotification.notificationId;
            if ([requestedNotificationId isEqualToString:testNotificationId]) {
                notification = testNotification;
                break;
            }
        }
        
        if (notification) {
            // fullfilled the request, blank it out
            requestedNotificationId = nil;
            if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
            {
                if (_detailSelectionDelegate) {
                    [_detailSelectionDelegate selectedDetail:notification withModule:self.module];
                }
            } else if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
                requestedNotification = notification;
                [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionList_Select withLabel:@"Select Notification" withValue:nil forModuleNamed:self.module.name];
                [self performSegueWithIdentifier:@"Show Notification Detail" sender:self];
            }
        }
    }
}

- (void)configureCell:(UITableViewCell *)cell atIndexPath:(NSIndexPath *)indexPath {
    Notification *notification = [_fetchedResultsController objectAtIndexPath:indexPath];
    UILabel *textLabel = (UILabel *)[cell viewWithTag:1];
    textLabel.text = notification.title;
    
    if([notification.read boolValue]) {
        textLabel.font = [UIFont systemFontOfSize:20.0f];
    } else {
        textLabel.font = [UIFont boldSystemFontOfSize:20.0f];
    }
    UIImageView *stickyImageView = (UIImageView *)[cell viewWithTag:2];
    if([notification.sticky boolValue]) {
        stickyImageView.backgroundColor = [UIColor colorWithRed:241/255.0f green:90/255.0f blue:36/255.0f alpha:1.0f];
    } else {
        stickyImageView.backgroundColor = [UIColor clearColor];
    }
}

- (UITableViewCell *)tableView:(UITableView *)tableView
         cellForRowAtIndexPath:(NSIndexPath *)indexPath {

    UITableViewCell *cell =[tableView dequeueReusableCellWithIdentifier:@"Notification Cell"];
    [self configureCell:cell atIndexPath:indexPath];
    
    return cell;

}

#pragma mark fetched results controller delegate methods

- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller {
    id sectionInfo = [[_fetchedResultsController sections] objectAtIndex:0];
    _rowCount = [sectionInfo numberOfObjects];

    [self.tableView beginUpdates];
}


- (void)controller:(NSFetchedResultsController *)controller didChangeObject:(id)anObject atIndexPath:(NSIndexPath *)indexPath forChangeType:(NSFetchedResultsChangeType)type newIndexPath:(NSIndexPath *)newIndexPath {
    
    UITableView *tableView = self.tableView;
    
    switch(type) {
            
        case NSFetchedResultsChangeInsert:
            [tableView insertRowsAtIndexPaths:[NSArray arrayWithObject:newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeUpdate:
            [self configureCell:[tableView cellForRowAtIndexPath:indexPath] atIndexPath:indexPath];
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
    
    switch(type) {
            
        case NSFetchedResultsChangeInsert:
            [self.tableView insertSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            [self.tableView deleteSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}


- (void)controllerDidChangeContent:(NSFetchedResultsController *)controller {
    [self.tableView endUpdates];
    if(_rowCount == 0) {
        [self selectFirst];
    }
    id sectionInfo = [[_fetchedResultsController sections] objectAtIndex:0];
    _rowCount = [sectionInfo numberOfObjects];
    
    // data loaded attempt to show requested notification
    if (requestedNotificationId) {
        [self showDetailForRequestedNotification];
    }
}

#pragma mark - fetch notifications
- (void) fetchNotifications:(id) sender
{
    [self fetchNotifications];
}
     
- (void) fetchNotifications {
    NSString *userid = [[CurrentUser sharedInstance] userid];
    if(userid) {
        NSString *urlString = [NSString stringWithFormat:@"%@/%@", [self.module propertyForKey:@"notifications"], [userid stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding]];
        [NotificationsFetcher fetchNotificationsFromURL:urlString withManagedObjectContext:self.module.managedObjectContext showLocalNotification:NO];
    }
    
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    
    if ([[segue identifier] isEqualToString:@"Show Notification Detail"])
    {
        Notification* notification = nil;

        if (requestedNotification) {
            notification = requestedNotification;
            
            // clear requested notification
            requestedNotificationId = nil;
            requestedNotification = nil;
        } else {
            NSIndexPath *indexPath = [self.tableView indexPathForSelectedRow];
            notification = [[self fetchedResultsController] objectAtIndexPath:indexPath];
        }

        NotificationDetailViewController *detailController = [segue destinationViewController];
        detailController.notification = notification;
        detailController.module = self.module;
    }
}

- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    Notification *notification = [[self fetchedResultsController] objectAtIndexPath:indexPath];
    return ![notification.sticky boolValue];
}

- (void)tableView:(UITableView*)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath*)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete)
    {
        Notification* notification = [self.fetchedResultsController objectAtIndexPath:indexPath];
        [NotificationsFetcher deleteNotification:notification module:self.module];
    }
}

-(void)tableView:(UITableView *)tableView willBeginEditingRowAtIndexPath:(NSIndexPath *)indexPath
{
    UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone
                                                                                target:self
                                                                                action:@selector(dismissRow:)];
    self.navigationItem.rightBarButtonItem = doneButton;
}

- (void)tableView:(UITableView *)tableView didEndEditingRowAtIndexPath:(NSIndexPath *)indexPath
{
    self.navigationItem.rightBarButtonItem = nil;
}

-(void) dismissRow:(id) sender
{
    UITableViewCell *cell = [self.tableView cellForRowAtIndexPath:[self.tableView indexPathForSelectedRow]];
    [cell setEditing:NO animated:YES];
    [self.tableView reloadData];
}

-(void) selectFirst
{
    if ([self.fetchedResultsController.fetchedObjects count] > 0 ) {
        
        NSIndexPath *indexPath = [NSIndexPath indexPathForRow:0 inSection:0];
        Notification *selectedEvent = [self.fetchedResultsController objectAtIndexPath:indexPath];
        
        if (_detailSelectionDelegate && selectedEvent) {
            [_detailSelectionDelegate selectedDetail:selectedEvent withModule:self.module];
        }
    }
}

@end
