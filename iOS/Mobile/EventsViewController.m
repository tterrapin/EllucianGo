//
//  EventsViewController.m
//  Mobile
//
//  Created by Jason Hocker on 7/31/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "EventsViewController.h"
#import "EventDetailViewController.h"
#import "EventsFilterViewController.h"
#import "EventModule.h"
#import "Event.h"
#import "EventCategory.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"

@interface EventsViewController ()

@property (nonatomic,strong) NSMutableSet *hiddenCategories;
@property (nonatomic, strong) NSDateFormatter *dateFormatter;
@property (nonatomic, strong) NSDateFormatter *dateFormatterSectionHeader;
@property (nonatomic, strong) NSDateFormatter *dateFormatterCoreData;
@property (nonatomic, strong) NSDateFormatter *timeFormatter;
@property (nonatomic, strong) EventModule *eventModule;
@property (nonatomic, strong) EventsFilterViewController *filterPicker;
@property (nonatomic, strong) UIPopoverController *filterPickerPopover;

@end

@implementation EventsViewController


- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:@"Show Events Filter"])
    {
        [self sendEventWithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionList_Select withLabel:@"Select filter" withValue:nil forModuleNamed:self.module.name];
        UINavigationController *navController = [segue destinationViewController];
        EventsFilterViewController *detailController = [[navController viewControllers] objectAtIndex:0];
        detailController.eventModule = self.eventModule;
        detailController.hiddenCategories = self.hiddenCategories;
        detailController.module = self.module;
    } else if ([[segue identifier] isEqualToString:@"Show Event Detail"])
    {
        UITableView *tableView = sender;
        NSIndexPath *indexPath = [tableView indexPathForSelectedRow];
        Event *event = [[self fetchedResultsControllerForTableView:tableView] objectAtIndexPath:indexPath];
        EventDetailViewController *detailController = [segue destinationViewController];
        detailController.eventTitle = event.summary;
        detailController.startDate = event.startDate;
        detailController.endDate = event.endDate;
        detailController.location = event.location;
        detailController.eventDescription = event.description_;
        detailController.allDay = [event.allDay boolValue];
        detailController.module = self.module;
    }
}

-(void) viewDidLoad
{
    [super viewDidLoad];
    
    self.navigationController.navigationBar.translucent = NO;
    self.searchBar.translucent = NO;
    
    self.title = self.module.name;
    [self fetchEvents];
    
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];

    [self sendView:@"Events List" forModuleNamed:self.module.name];
    
    [self readEventModule];
    _fetchedResultsController = nil;
    NSError *error;
    [self.fetchedResultsController performFetch:&error];
    [self.tableView reloadData];

}

#pragma mark - Table View

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    NSFetchedResultsController *fetchController = [self fetchedResultsControllerForTableView:tableView];
    return [[fetchController sections] count];
}

-(NSDateFormatter *)dateFormatterSectionHeader
{

    if(_dateFormatterSectionHeader == nil) {
        _dateFormatterSectionHeader = [[NSDateFormatter alloc] init];
        [_dateFormatterSectionHeader setDateStyle:NSDateFormatterShortStyle];
        [_dateFormatterSectionHeader setTimeStyle:NSDateFormatterNoStyle];
        [_dateFormatterSectionHeader setDoesRelativeDateFormatting:YES];
    }
    return _dateFormatterSectionHeader;
}

-(NSDateFormatter *)dateFormatterCoreData
{
    if(_dateFormatterCoreData == nil) {
        _dateFormatterCoreData = [[NSDateFormatter alloc] init];
        [_dateFormatterCoreData setDateFormat: @"yyyy-MM-dd"];
    }
    return _dateFormatterCoreData;
}

-(NSString *)tableView:(UITableView *)tableView stringForTitleForHeaderInSection:(NSInteger)section
{
    
    NSFetchedResultsController *fetchController = [self fetchedResultsControllerForTableView:tableView];
    NSString *header = [[[fetchController sections] objectAtIndex:section] name];
    NSDate *date = [self.dateFormatterCoreData dateFromString:header];
    return [self.dateFormatterSectionHeader stringFromDate:date];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSInteger numberOfRows = 0;
    NSFetchedResultsController *fetchController = [self fetchedResultsControllerForTableView:tableView];
    NSArray *sections = fetchController.sections;
    if(sections.count > 0)
    {
        id <NSFetchedResultsSectionInfo> sectionInfo = [sections objectAtIndex:section];
        numberOfRows = [sectionInfo numberOfObjects];
    }
    return numberOfRows;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:@"Event Cell"];
    Event *event = [[self fetchedResultsControllerForTableView:tableView] objectAtIndexPath:indexPath];
    
    UILabel *titleLabel = (UILabel *)[cell viewWithTag:101];
    titleLabel.text = event.summary;
    UILabel *contentLabel = (UILabel *)[cell viewWithTag:102];
    
    if([event.allDay boolValue] == YES) {
        if(event.location) {
            contentLabel.text = [NSString stringWithFormat:@"%@, %@", NSLocalizedString(@"All Day", @"label for all day event"), event.location];
        } else {
            contentLabel.text = [NSString stringWithFormat:@"%@", NSLocalizedString(@"All Day", @"label for all day event")];
        }
    } else if(event.location && event.endDate) {
        contentLabel.text = [NSString stringWithFormat:@"%@ - %@, %@", [self.timeFormatter stringFromDate:event.startDate], [self.timeFormatter stringFromDate:event.endDate], event.location];
    } else if(event.location) {
        contentLabel.text = [NSString stringWithFormat:@"%@, %@", [self.timeFormatter stringFromDate:event.startDate], event.location];
    } else if(event.endDate) {
        contentLabel.text = [NSString stringWithFormat:@"%@ - %@", [self.timeFormatter stringFromDate:event.startDate], [self.timeFormatter stringFromDate:event.endDate]];
    } else {
        contentLabel.text = [NSString stringWithFormat:@"%@", [self.timeFormatter stringFromDate:event.startDate]];
    }
    UILabel *categoryLabel = (UILabel *)[cell viewWithTag:103];
    NSMutableArray *categoryValues = [[NSMutableArray alloc] init];
    for(EventCategory* value in event.category) {
        [categoryValues addObject:value.name];
    }
    categoryLabel.text = [NSString stringWithFormat:@"%@: %@", NSLocalizedString(@"Category", "label for the categories"), [categoryValues componentsJoinedByString:@", "]];

        
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [self performSegueWithIdentifier:@"Show Event Detail" sender:tableView];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath;
{
    return 66;
}



#pragma mark - Fetched results controller

- (NSFetchedResultsController *)fetchedResultsController
{
    if (_fetchedResultsController != nil)
    {
        return _fetchedResultsController;
    }
    _fetchedResultsController = [self newFetchedResultsControllerWithSearch:nil];
    return _fetchedResultsController;
}



#pragma mark Fetch results controller management
- (NSFetchedResultsController *)fetchedResultsControllerForTableView:(UITableView *)tableView
{
    return tableView == self.tableView ? self.fetchedResultsController : self.searchFetchedResultsController;
}

- (NSFetchedResultsController *)newFetchedResultsControllerWithSearch:(NSString *)searchString
{
    NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"Event"];
    NSMutableArray *subPredicates = [[NSMutableArray alloc] init];
    NSPredicate *filterPredicate = [NSPredicate predicateWithFormat:@"(module.name = %@)", self.module.name];
    [subPredicates addObject:filterPredicate];
    
    if(searchString.length)
    {
        NSPredicate *searchPredicate = [NSPredicate predicateWithFormat:@"((summary CONTAINS[cd] %@) OR (description_ CONTAINS[cd] %@) OR (location CONTAINS[cd] %@))", searchString, searchString, searchString];
        [subPredicates addObject:searchPredicate];
    }
    
    if([self.hiddenCategories count] > 0) {
        NSPredicate *categoryPredicate = [NSPredicate predicateWithFormat:@"NONE category.name IN %@", self.hiddenCategories];
        [subPredicates addObject:categoryPredicate];
    }
    
     request.predicate = [NSCompoundPredicate andPredicateWithSubpredicates:subPredicates];
    
    request.sortDescriptors = [NSArray arrayWithObjects:[NSSortDescriptor sortDescriptorWithKey:@"dateLabel" ascending:YES ],[NSSortDescriptor sortDescriptorWithKey:@"startDate" ascending:YES ],[NSSortDescriptor sortDescriptorWithKey:@"summary" ascending:YES ],nil ];
    
    NSFetchedResultsController *aFetchedResultsController = [[NSFetchedResultsController alloc] initWithFetchRequest:request
                                                                                                managedObjectContext:self.module.managedObjectContext
                                                                                                  sectionNameKeyPath:@"dateLabel"
                                                                                                           cacheName:nil];
    aFetchedResultsController.delegate = self;
    
    
    NSError *error = nil;
    if (![aFetchedResultsController performFetch:&error])
    {
        NSLog(@"Error performing institution fetch with search string %@: %@, %@", searchString, error, [error userInfo]);
    }
    
    return aFetchedResultsController;
}

- (NSFetchedResultsController *)searchFetchedResultsController
{
    if (_searchFetchedResultsController != nil)
    {
        return _searchFetchedResultsController;
    }
    _searchFetchedResultsController = [self newFetchedResultsControllerWithSearch:self.searchDisplayController.searchBar.text];
    return _searchFetchedResultsController;
}

- (UITableView *) tableViewForFetchedResultsController:(NSFetchedResultsController *)controller
{
    UITableView *tableView = controller == self.fetchedResultsController ? self.tableView : self.searchDisplayController.searchResultsTableView;
    return tableView;
    
}

- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller {
    UITableView *tableView = [self tableViewForFetchedResultsController:controller];
    [tableView beginUpdates];
}

- (void)controller:(NSFetchedResultsController *)controller didChangeSection:(id <NSFetchedResultsSectionInfo>)sectionInfo atIndex:(NSUInteger)sectionIndex forChangeType:(NSFetchedResultsChangeType)type {
    UITableView *tableView = controller == self.fetchedResultsController ? self.tableView : self.searchDisplayController.searchResultsTableView;
    
    switch(type) {
        case NSFetchedResultsChangeInsert:
            if (!(sectionIndex == 0 && [self.tableView numberOfSections] == 1))
                [tableView insertSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
        case NSFetchedResultsChangeDelete:
            if (!(sectionIndex == 0 && [self.tableView numberOfSections] == 1))
                [tableView deleteSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
        case NSFetchedResultsChangeMove:
            break;
        case NSFetchedResultsChangeUpdate:
            break;
        default:
            break;
    }
}

- (void)controller:(NSFetchedResultsController *)controller didChangeObject:(id)anObject atIndexPath:(NSIndexPath *)indexPath forChangeType:(NSFetchedResultsChangeType)type newIndexPath:(NSIndexPath *)newIndexPath {
    UITableView *tableView = [self tableViewForFetchedResultsController:controller];
    
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
            [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
            [tableView insertRowsAtIndexPaths:[NSArray arrayWithObject:newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}

- (void)controllerDidChangeContent:(NSFetchedResultsController *)controller {
    UITableView *tableView = [self tableViewForFetchedResultsController:controller];
    [tableView endUpdates];
}

#pragma mark - fetch configuration

- (void) fetchEvents {
    
    NSManagedObjectContext *importContext = [[NSManagedObjectContext alloc] initWithConcurrencyType:NSPrivateQueueConcurrencyType];
    importContext.parentContext = self.module.managedObjectContext;
    [importContext setUndoManager:nil];
    NSString *urlString = [self.module propertyForKey:@"events"];
    NSLog(@"events urlString: %@", urlString);

    [importContext performBlock: ^{
        
        
        //download data
        NSError *error;
        [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
        NSData *responseData = [NSData dataWithContentsOfURL: [NSURL URLWithString: urlString]];
        
        if(responseData)
        {


            NSDictionary* json = [NSJSONSerialization
                                  JSONObjectWithData:responseData
                                  options:kNilOptions
                                  error:&error];
            
            NSMutableDictionary *previousEvents = [[NSMutableDictionary alloc] init];
            NSMutableSet *newKeys = [[NSMutableSet alloc] init];
            
            NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"Event"];
            
            NSPredicate *filterPredicate = [NSPredicate predicateWithFormat:@"module.name = %@", self.module.name];
            [request setPredicate:filterPredicate];
            NSArray * oldObjects = [importContext executeFetchRequest:request error:&error];
            for (Event* oldObject in oldObjects) {
                [previousEvents setObject:oldObject forKey:oldObject.uid];
            }
            
            NSArray *orderedKeys = [[json allKeys] sortedArrayUsingSelector:@selector(localizedCaseInsensitiveCompare:)];
            
            NSFetchRequest *moduleRequest = [[NSFetchRequest alloc] init];
            NSEntityDescription *entity = [NSEntityDescription entityForName:@"EventModule" inManagedObjectContext:importContext];
            NSPredicate *predicate =[NSPredicate predicateWithFormat:@"name = %@",self.module.name];
            [moduleRequest setEntity:entity];
            [moduleRequest setPredicate:predicate];
            
            EventModule *eventModule = [[importContext executeFetchRequest:moduleRequest error:&error] lastObject];
            if(!eventModule) {
                eventModule = [NSEntityDescription insertNewObjectForEntityForName:@"EventModule" inManagedObjectContext:importContext];
                eventModule.name = self.module.name;
            }
            
            NSFetchRequest *categoryRequest = [[NSFetchRequest alloc] init];
            NSEntityDescription *categoryEntity = [NSEntityDescription entityForName:@"EventCategory" inManagedObjectContext:importContext];
            [categoryRequest setEntity:categoryEntity];
            NSPredicate *categoryPredicate =[NSPredicate predicateWithFormat:@"moduleName = %@",self.module.name];
            [categoryRequest setPredicate:categoryPredicate];
            
            NSArray *categoryArray = [importContext executeFetchRequest:categoryRequest error:&error];
            NSMutableDictionary *categoryMap = [[NSMutableDictionary alloc] init];
            for(EventCategory *eventCategory in categoryArray) {
                [categoryMap setObject:eventCategory forKey:eventCategory.name];
            }

            //create/update objects
            int j=0;
            for( id key in orderedKeys) {
                NSArray *eventsForDate = [json objectForKey:key];
                for(int i = 0; i < [eventsForDate count]; i++) {
                    NSDictionary *jsonEvent = [eventsForDate objectAtIndex:i];
                    
                    //if exists
                    NSString *uid = [jsonEvent objectForKey:@"uid"];
                    Event *event = [previousEvents objectForKey:uid];
                    
                    if(event) {
                        [previousEvents removeObjectForKey:uid];
                    } else {
                        j++;
                        event = [NSEntityDescription insertNewObjectForEntityForName:@"Event" inManagedObjectContext:importContext];
                        event.module = eventModule;
                        [eventModule addEventsObject:event];
                        
                        [newKeys addObject:uid];
                        if([jsonEvent objectForKey:@"summary"] != [NSNull null]) {
                            event.summary = [jsonEvent objectForKey:@"summary"];
                        }
                        event.uid = uid;
                        
                        if([jsonEvent objectForKey:@"description"] != [NSNull null]) {
                            event.description_ = [jsonEvent objectForKey:@"description"];
                        }
                        if([jsonEvent objectForKey:@"location"] != [NSNull null]) {
                            event.location = [jsonEvent objectForKey:@"location"];
                        }
                        if([jsonEvent objectForKey:@"contact"] != [NSNull null]) {
                            event.contact = [jsonEvent objectForKey:@"contact"];
                        }
                        event.startDate = [self.dateFormatter dateFromString:[jsonEvent objectForKey:@"start"]];
                        event.dateLabel = [self.dateFormatterCoreData stringFromDate:event.startDate];
                        event.endDate = [self.dateFormatter dateFromString:[jsonEvent objectForKey:@"end"]];
                        event.allDay = [NSNumber numberWithBool:[[jsonEvent objectForKey:@"allDay"] boolValue]];
                        NSArray *categoriesArray = [jsonEvent objectForKey:@"categories"];

                        for (NSDictionary *categoryDictionary in categoriesArray) {
                            NSString *categoryLabel = [categoryDictionary objectForKey:@"name"];
                            EventCategory* category = [categoryMap objectForKey:categoryLabel];
                            if(!category) {
                                category = [NSEntityDescription insertNewObjectForEntityForName:@"EventCategory" inManagedObjectContext:importContext];
                                category.name = categoryLabel;
                                category.moduleName = self.module.name;
                                [categoryMap setObject:category forKey:category.name];
                            }
                            [event addCategoryObject:category];
                            [category addEventObject:event];
                        }
                        
                        if (j % 10 == 0) {
                            if (![importContext save:&error]) {
                                NSLog(@"Could not save to main context after update to events: %@", [error userInfo]);
                            }
                            [importContext.parentContext performBlock:^{
                                NSError *parentError = nil;
                                if(![importContext.parentContext save:&parentError]) {
                                    NSLog(@"Could not save to store after update to events: %@", [error userInfo]);
                                }
                            }];
                        }
                    }
                }
            }

            if (![importContext save:&error]) {
                NSLog(@"Could not save to main context after update to events: %@", [error userInfo]);
            }
            
            //find and delete old ones
            for (NSManagedObject * oldObject in [previousEvents allValues]) {
                 [importContext deleteObject:oldObject];
             }
            
            //save to main context
            if (![importContext save:&error]) {
                NSLog(@"Could not save to main context after update to events: %@", [error userInfo]);
            }
            
            //persist to store and update fetched result controllers
            [importContext.parentContext performBlock:^{
                NSError *parentError = nil;
                if(![importContext.parentContext save:&parentError]) {
                    NSLog(@"Could not save to store after update to events: %@", [error userInfo]);
                }
            }];
        }
        [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
        dispatch_async(dispatch_get_main_queue(), ^{
            [self readEventModule];
            [self.filterButton setEnabled:YES];
        });
    }];
}

#pragma mark - Search Display Delegaten
- (BOOL)searchDisplayController:(UISearchDisplayController *)controller shouldReloadTableForSearchString:(NSString *)searchString
{
    self.searchFetchedResultsController = nil;
    return YES;
}

-(NSDateFormatter *)dateFormatter
{
    if(_dateFormatter == nil) {
        _dateFormatter = [[NSDateFormatter alloc] init];
        [_dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ssZ"];
    }
    return _dateFormatter;
}

-(NSDateFormatter *)timeFormatter
{
    if(_timeFormatter == nil) {
        
        _timeFormatter = [[NSDateFormatter alloc] init];
        [_timeFormatter setDateStyle:NSDateFormatterNoStyle];
        [_timeFormatter setTimeStyle:NSDateFormatterShortStyle];
    }
    return _timeFormatter;
}

-(void) readEventModule {
    NSError *error;
    NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"EventModule"];
    request.predicate = [NSPredicate predicateWithFormat:@"name = %@", self.module.name];
    [request setFetchLimit:1];
    NSArray *fetchResults = [self.module.managedObjectContext executeFetchRequest:request error:&error];
    
    if([fetchResults count] > 0) {
        self.eventModule = [fetchResults objectAtIndex:0];
        self.hiddenCategories = [[NSMutableSet alloc] initWithArray:[[self.eventModule valueForKey:@"hiddenCategories"] componentsSeparatedByString:@","]];
    } else {
        self.hiddenCategories = [[NSMutableSet alloc] init];
    }

}

- (void)searchBarTextDidBeginEditing:(UISearchBar *)searchBar
{
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionSearch withLabel:@"Search" withValue:nil forModuleNamed:nil];
}

- (IBAction)chooseFilterButtonTapped:(id)sender
{
    if (_filterPicker == nil) {
        //Create the FeedFilterViewController.
        _filterPicker = (EventsFilterViewController *)[self.storyboard instantiateViewControllerWithIdentifier:@"EventsFilterView"];
    }
    
    [self sendEventWithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionList_Select withLabel:@"Select filter" withValue:nil forModuleNamed:self.module.name];
    
    _filterPicker.delegate = self;
    _filterPicker.eventModule = self.eventModule;
    _filterPicker.hiddenCategories = self.hiddenCategories;
    _filterPicker.module = self.module;
    
    [_filterPicker initializeCategories];
    [_filterPicker sizeForPopover];
    
    
    
    if (!_filterPickerPopover) {
        //The filter picker popover is not showing. Show it.
        _filterPickerPopover = [[UIPopoverController alloc] initWithContentViewController:_filterPicker];
        [_filterPickerPopover presentPopoverFromBarButtonItem:(UIBarButtonItem *)sender
                                     permittedArrowDirections:UIPopoverArrowDirectionUp animated:YES];
        _filterPickerPopover.delegate = _filterPicker;
        _filterPickerPopover.passthroughViews = nil;
        
    } else {
        //The filter picker popover is showing. Hide it.
        [_filterPickerPopover dismissPopoverAnimated:YES];
        [_filterPicker dismiss:self.module.name];
        _filterPickerPopover = nil;
    }

}


- (void)reloadData
{
    NSError *error;
    
    [self sendView:@"Events List" forModuleNamed:self.module.name];
    [self readEventModule];
    _fetchedResultsController = nil;
    [self.fetchedResultsController performFetch:&error];
    [self.tableView reloadData];
}

- (void)resetPopover
{
    _filterPickerPopover = nil;
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    if (_filterPickerPopover)
    {
        [_filterPickerPopover dismissPopoverAnimated:YES];
        [_filterPicker dismiss:self.module.name];
        _filterPickerPopover = nil;
    }
}

//workaround for iOS 7 double tap on search bar, search disappears
-(void)searchDisplayControllerDidEndSearch:(UISearchDisplayController *)controller
{
    self.searchFetchedResultsController = nil;
    if (floor(NSFoundationVersionNumber) > NSFoundationVersionNumber_iOS_6_1) {
        [self.tableView insertSubview:self.searchDisplayController.searchBar aboveSubview:self.tableView];
    }
    return;
}

@end