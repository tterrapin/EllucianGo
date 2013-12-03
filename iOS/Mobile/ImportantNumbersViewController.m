//
//  ImportantNumbersViewController
//  Mobile
//
//  Created by Jason Hocker on 9/28/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "ImportantNumbersViewController.h"
#import "ImportantNumbersDetailViewController.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"

@interface ImportantNumbersViewController ()

@end

@implementation ImportantNumbersViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.navigationController.navigationBar.translucent = NO;
    self.searchBar.translucent = NO;
    
    NSError *error;
	if (![[self fetchedResultsController] performFetch:&error]) {
		// Update to handle the error appropriately.
		NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
	}
    
    self.title = self.module.name;
    [self fetchImportantNumbers];
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Important Number List" forModuleNamed:self.module.name];
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

    NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"ImportantNumbersDirectoryEntry"];
    NSMutableArray *subPredicates = [[NSMutableArray alloc] init];
    NSPredicate *filterPredicate = [NSPredicate predicateWithFormat:@"(moduleName = %@)", self.module.internalKey];
    [subPredicates addObject:filterPredicate];
    
    if(searchString.length)
    {
        NSPredicate *searchPredicate = [NSPredicate predicateWithFormat:@"(name CONTAINS[cd] %@)", searchString];
        [subPredicates addObject:searchPredicate];
    }
    
    request.predicate = [NSCompoundPredicate andPredicateWithSubpredicates:subPredicates];
    
    request.sortDescriptors = [NSArray arrayWithObjects:[NSSortDescriptor sortDescriptorWithKey:@"category" ascending:YES ],[NSSortDescriptor sortDescriptorWithKey:@"name" ascending:YES ],nil ];
    
    NSFetchedResultsController *aFetchedResultsController = [[NSFetchedResultsController alloc] initWithFetchRequest:request
                                                                                                managedObjectContext:self.module.managedObjectContext
                                                                                                  sectionNameKeyPath:@"category" cacheName:nil];
                                                                                                               
    
    aFetchedResultsController.delegate = self;
    
    
    NSError *error = nil;
    if (![aFetchedResultsController performFetch:&error])
    {
        NSLog(@"Error performing institution fetch with search string %@: %@", error, [error userInfo]);
    }
    
    return aFetchedResultsController;

}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    NSFetchedResultsController *fetchController = [self fetchedResultsControllerForTableView:tableView];
    return [[fetchController sections] count];
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


#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionList_Select withLabel:@"Select Important Number" withValue:nil forModuleNamed:self.module.name];
    [self performSegueWithIdentifier:@"Show Important Numbers Detail" sender:tableView];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    static NSString *CellIdentifier = @"Yellow Pages Directory Item Cell";
    
    UITableViewCell *cell =
    [self.tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    
    ImportantNumbersDirectoryEntry *entry = [[self fetchedResultsControllerForTableView:tableView] objectAtIndexPath:indexPath];
    UILabel *textLabel = (UILabel*)[cell viewWithTag:1];
    textLabel.text = entry.name;
    
    return cell;
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

#pragma mark - fetch ImportantNumbers

- (void) fetchImportantNumbers {
    
    NSManagedObjectContext *importContext = [[NSManagedObjectContext alloc] initWithConcurrencyType:NSPrivateQueueConcurrencyType];
    importContext.parentContext = self.module.managedObjectContext;
    NSString *urlString = [self.module propertyForKey:@"numbers"];
    [importContext performBlock: ^{
        
        //download data
        NSError *error;
        [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
        NSData *responseData = [NSData dataWithContentsOfURL: [NSURL URLWithString: urlString]];

        [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
        if(responseData) {
            NSDictionary* json = [NSJSONSerialization
                                  JSONObjectWithData:responseData
                                  options:kNilOptions
                                  error:&error];
            
            NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"ImportantNumbersDirectoryEntry"];
            request.predicate = [NSPredicate predicateWithFormat:@"moduleName = %@", self.module.internalKey];
            
            NSArray * oldObjects = [importContext executeFetchRequest:request error:&error];
            for (ImportantNumbersDirectoryEntry* oldObject in oldObjects) {
                [importContext deleteObject:oldObject];
            }
            
            //create/update objects
            for(NSDictionary *itemsDictionary in [json objectForKey:@"numbers"]) {
                ImportantNumbersDirectoryEntry *entity = [NSEntityDescription insertNewObjectForEntityForName:@"ImportantNumbersDirectoryEntry" inManagedObjectContext:importContext];
                entity.moduleName = self.module.internalKey;
                entity.name = [itemsDictionary objectForKey:@"name"];
                entity.category = [itemsDictionary objectForKey:@"category"];
                if([itemsDictionary objectForKey:@"phone"] != [NSNull null])
                    entity.phone = [itemsDictionary objectForKey:@"phone"];
                if([itemsDictionary objectForKey:@"extension"] != [NSNull null])    
                    entity.phoneExtension = [itemsDictionary objectForKey:@"extension"];
                if([itemsDictionary objectForKey:@"email"] != [NSNull null])
                    entity.email = [itemsDictionary objectForKey:@"email"];
                if([itemsDictionary objectForKey:@"buildingId"] != [NSNull null])
                    entity.buildingId = [itemsDictionary objectForKey:@"buildingId"];
                if([itemsDictionary objectForKey:@"latitude"] != [NSNull null])
                    entity.latitude = [itemsDictionary valueForKey:@"latitude"];
                if([itemsDictionary objectForKey:@"longitude"] != [NSNull null])
                    entity.longitude = [itemsDictionary valueForKey:@"longitude"];
                if([itemsDictionary objectForKey:@"campusId"] != [NSNull null])
                    entity.campusId = [itemsDictionary objectForKey:@"campusId"];
                if([itemsDictionary objectForKey:@"address"] != [NSNull null])
                    entity.address = [[itemsDictionary objectForKey:@"address"] stringByReplacingOccurrencesOfString:@"\\n" withString:@"\n"];
                
                
            }
        }
        
        if (![importContext save:&error]) {
            NSLog(@"Could not save to main context after update to ImportantNumbers: %@", [error userInfo]);
        }
        
        //save to main context
        if (![importContext save:&error]) {
            NSLog(@"Could not save to main context after update to ImportantNumbers: %@", [error userInfo]);
        }
        
        //persist to store and update fetched result controllers
        [importContext.parentContext performBlock:^{
            NSError *parentError = nil;
            if(![importContext.parentContext save:&parentError]) {
                NSLog(@"Could not save to store after update to ImportantNumbers: %@", [error userInfo]);
               
            }
            [self.tableView reloadData];
        }];
    }
     ];
    
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    
    if ([[segue identifier] isEqualToString:@"Show Important Numbers Detail"])
    {
        UITableView *tableView = sender;
        NSIndexPath *indexPath = [tableView indexPathForSelectedRow];
        ImportantNumbersDirectoryEntry *directory = [[self fetchedResultsControllerForTableView:tableView] objectAtIndexPath:indexPath];

        ImportantNumbersDetailViewController *vc = (ImportantNumbersDetailViewController *)[segue destinationViewController];
        vc.name = directory.name;
        vc.types = [NSArray arrayWithObject:directory.category];
        if([directory.latitude doubleValue] != 0 && [directory.longitude doubleValue] != 0) {
            vc.location = [[CLLocation alloc] initWithLatitude:[directory.latitude doubleValue] longitude:[directory.longitude doubleValue]];
        }
        vc.buildingId = directory.buildingId;
        vc.campusId = directory.campusId;
        vc.email = directory.email;
        vc.phone = directory.phone;
        vc.phoneExtension = directory.phoneExtension;
        vc.address = directory.address;
        vc.module = self.module;
    }
}

-(NSString *)tableView:(UITableView *)tableView stringForTitleForHeaderInSection:(NSInteger)section
{
    NSFetchedResultsController *fetchController = [self fetchedResultsControllerForTableView:tableView];
    return [[[fetchController sections] objectAtIndex:section] name];
}


#pragma mark - Search Display Delegaten
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
