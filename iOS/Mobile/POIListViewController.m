//
//  POIListViewController.m
//  Mobile
//
//  Created by Jason Hocker on 9/7/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "POIListViewController.h"
#import "MapPOI.h"
#import "POIDetailViewController.h"
#import "MapPOIType.h"
#import "MapCampus.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"

@interface POIListViewController ()

@end

@implementation POIListViewController


- (void)viewDidLoad
{
    [super viewDidLoad];
    self.navigationController.navigationBar.translucent = NO;
    
    NSError *error;
    if (![[self fetchedResultsController] performFetch:&error]) {
		NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
	}
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    [self sendView:@"Building List" forModuleNamed:self.module.name];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    NSFetchedResultsController *fetchController = [self fetchedResultsControllerForTableView:tableView];
    return [[fetchController sections] count];
}

-(NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    
    NSFetchedResultsController *fetchController = [self fetchedResultsControllerForTableView:tableView];
    return [[[fetchController sections] objectAtIndex:section] name];
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

- (UITableViewCell *)tableView:(UITableView *)tableView
         cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    static NSString *CellIdentifier = @"POI Cell";
    
    UITableViewCell *cell =
    [self.tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    
    MapPOI *poi = [[self fetchedResultsControllerForTableView:tableView] objectAtIndexPath:indexPath];
    UILabel *textLabel = (UILabel *)[cell viewWithTag:1];
    UILabel *detailTextLabel = (UILabel *)[cell viewWithTag:2];
    
    textLabel.text = poi.name;
    detailTextLabel.text = poi.campus.name;
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [self performSegueWithIdentifier:@"Show POI" sender:tableView];
}

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
    NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"MapPOI"];
      
    if(searchString.length)
    {
        request.predicate = [NSPredicate predicateWithFormat:@"moduleInternalKey = %@ AND (name CONTAINS[cd] %@ OR ANY types.name CONTAINS[cd] %@ OR campus.name CONTAINS[cd] %@)", self.module.internalKey, searchString, searchString, searchString];
    } else {
        request.predicate = [NSPredicate predicateWithFormat:@"moduleInternalKey = %@", self.module.internalKey];
    }
//    request.sortDescriptors = [NSArray arrayWithObjects:[NSSortDescriptor sortDescriptorWithKey:@"type" ascending:YES ],[NSSortDescriptor sortDescriptorWithKey:@"name" ascending:YES ],nil ];
    request.sortDescriptors = [NSArray arrayWithObjects:[NSSortDescriptor sortDescriptorWithKey:@"name" ascending:YES ],nil ];


    NSFetchedResultsController *aFetchedResultsController = [[NSFetchedResultsController alloc] initWithFetchRequest:request
                                                                                                managedObjectContext:self.module.managedObjectContext
                                                                                                  sectionNameKeyPath:nil
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


-(void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:@"Show POI"])
    {
        UITableView *tableView = sender;
        NSIndexPath *indexPath = [tableView indexPathForSelectedRow];
        MapPOI *poi = [[self fetchedResultsControllerForTableView:tableView] objectAtIndexPath:indexPath];
        
        POIDetailViewController *vc = (POIDetailViewController *)[segue destinationViewController];
        vc.imageUrl = poi.imageUrl;
        vc.name = poi.name;
        NSMutableArray *types = [[NSMutableArray alloc] init];
        for(MapPOIType *type in poi.types) {
            [types addObject:type.name];
        }
        vc.types = [types copy];
        vc.location = [[CLLocation alloc] initWithLatitude:[poi.latitude doubleValue] longitude:[poi.longitude doubleValue]];
        vc.address = poi.address;
        vc.description = poi.description_;
        vc.additionalServices = poi.additionalServices;
        vc.campusName = poi.campus.name;
        vc.module = self.module;
    }
}

- (BOOL)searchDisplayController:(UISearchDisplayController *)controller shouldReloadTableForSearchString:(NSString *)searchString
{
    self.searchFetchedResultsController = nil;
    return YES;
}

- (void)searchBarTextDidBeginEditing:(UISearchBar *)searchBar
{
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionSearch withLabel:@"Search" withValue:nil forModuleNamed:nil];
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
