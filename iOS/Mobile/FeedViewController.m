//
//  FeedViewController.m
//  Mobile
//
//  Created by Jason Hocker on 8/17/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "FeedViewController.h"
#import "FeedCategory.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "AppearanceChanger.h"
#import "MBProgressHUD.h"

@interface FeedViewController ()
@property (nonatomic,strong) NSMutableSet *hiddenCategories;
@property (nonatomic, strong) NSDateFormatter *dateFormatter;
@property (nonatomic, strong) NSDateFormatter *dateFormatterSectionHeader;
@property (nonatomic, strong) NSDateFormatter *dateFormatterCoreData;
@property (nonatomic, strong) NSDateFormatter *timeFormatter;
@property (nonatomic, strong) FeedModule *feedModule;
@property (nonatomic, strong) NSMutableDictionary *thumbImageCache;
@property (nonatomic, strong) FeedFilterViewController *filterPicker;
@property (nonatomic, strong) UIPopoverController *filterPickerPopover;

@end

@implementation FeedViewController

- (void) awakeFromNib{
    [super awakeFromNib];
    self.splitViewController.delegate = self;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:@"ShowFeedFilter"])
    {
        [self sendEventWithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionList_Select withLabel:@"Select filter" withValue:nil forModuleNamed:self.module.name];
        UINavigationController *navController = [segue destinationViewController];
        FeedFilterViewController *detailController = [[navController viewControllers] objectAtIndex:0];
        detailController.feedModule = self.feedModule;
        detailController.hiddenCategories = self.hiddenCategories;
        detailController.module = self.module;
        detailController.delegate = self;
    }
    else if ([[segue identifier] isEqualToString:@"Show Feed Detail"] || [[segue identifier] isEqualToString:@"Show Feed Image Detail"])
    {
        UITableView *tableView = sender;
        NSIndexPath *indexPath = [tableView indexPathForSelectedRow];
        Feed *feed = [[self fetchedResultsControllerForTableView:tableView] objectAtIndexPath:indexPath];
        FeedDetailViewController *detailController = [segue destinationViewController];
        detailController.itemTitle = feed.title;
        detailController.itemContent = feed.content;
        detailController.itemLink = [feed.link stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
        detailController.itemPostDateTime = feed.postDateTime;
        detailController.itemCategory = feed.category;
        detailController.itemImageUrl = feed.logo;
        detailController.module = self.module;
    }
}

-(void) viewDidLoad
{
    [super viewDidLoad];
    
    self.navigationController.navigationBar.translucent = NO;
    self.searchBar.translucent = NO;
    
    self.title = self.module.name;
    [self fetchFeeds];
    [self reloadData];
    
    if ([self.fetchedResultsController.fetchedObjects count] > 0 ) {
    
        NSIndexPath * head = [NSIndexPath indexPathForRow:0 inSection:0];
        Feed *selectedFeed = [self.fetchedResultsController objectAtIndexPath:head];
    
        if (_detailSelectionDelegate && selectedFeed) {
            [_detailSelectionDelegate selectedDetail:selectedFeed withIndex:head withModule:self.module withController:self];
        }
    }
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    [self sendView:@"News List" forModuleNamed:self.module.name];
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
    Feed *feed = [[self fetchedResultsControllerForTableView:tableView] objectAtIndexPath:indexPath];
    if(feed.logo) {
        UITableViewCell *cell = [self.tableView
                                 dequeueReusableCellWithIdentifier:@"Feed Image Cell"];
        UILabel *titleLabel = (UILabel *)[cell viewWithTag:100];
        titleLabel.text = [feed valueForKey:@"title"];
        UILabel *contentLabel = (UILabel *)[cell viewWithTag:101];
        contentLabel.text = [[feed valueForKey:@"content"] stringByConvertingHTMLToPlainText];
        contentLabel.textAlignment = [AppearanceChanger isRTL] ? NSTextAlignmentRight : NSTextAlignmentLeft;
        UIImageView * imageView = (UIImageView*)[cell viewWithTag:102];
        
        //check the cache for the image
        UIImage* logoImage = [self getImageFromCache:feed.logo];
        
        //if the image is not there download in the background
        if (!logoImage)
        {
        
            dispatch_async(dispatch_get_global_queue(0,0), ^{
        
                NSData *imageData = [[NSData alloc] initWithContentsOfURL:[NSURL URLWithString:feed.logo]];
        
                UIImage *myimage = [[UIImage alloc] initWithData:imageData];
        
                dispatch_async(dispatch_get_main_queue(), ^{
                    //store the image in the cache
                    NSString *logo = feed.logo;
                    if(logo && myimage) {
                        [self storeImageInCache:myimage forKey:logo];
                        [imageView setImage:myimage];
                    }
                });
            });
        } else {
            [imageView setImage:logoImage];
        }
        
        UILabel *feedNameLabel = (UILabel *)[cell viewWithTag:103];
        NSMutableArray *categoryValues = [[NSMutableArray alloc] init];
        for(FeedCategory* value in feed.category) {
            [categoryValues addObject:value.name];
        }
        feedNameLabel.text = [NSString stringWithFormat:NSLocalizedString(@"Category: %@", "label for the categories"), [categoryValues componentsJoinedByString:@", "]];
        return cell;
        
    } else {
        UITableViewCell *cell = [self.tableView
                                 dequeueReusableCellWithIdentifier:@"Feed Cell"];
        UILabel *titleLabel = (UILabel *)[cell viewWithTag:100];
        titleLabel.text = [feed valueForKey:@"title"];
        UILabel *contentLabel = (UILabel *)[cell viewWithTag:101];
        contentLabel.text = [[feed valueForKey:@"content"] stringByConvertingHTMLToPlainText ];
        contentLabel.textAlignment = [AppearanceChanger isRTL] ? NSTextAlignmentRight : NSTextAlignmentLeft;
        UILabel *feedNameLabel = (UILabel *)[cell viewWithTag:102];

        NSMutableArray *categoryValues = [[NSMutableArray alloc] init];
        for(FeedCategory* value in feed.category) {
            [categoryValues addObject:value.name];
        }
        feedNameLabel.text = [NSString stringWithFormat:NSLocalizedString(@"Category: %@", "label for the categories"), [categoryValues componentsJoinedByString:@", "]];
        
        return cell;
    }
    
}

- (void) storeImageInCache:(UIImage *)image forKey:(NSString*)key
{
    if (self.thumbImageCache == nil)
    {
        self.thumbImageCache = [[NSMutableDictionary alloc] init ];
    }
    
    [self.thumbImageCache setObject:image forKey:key];
}

- (UIImage *)getImageFromCache:(NSString *)imageURL
{
    if (self.thumbImageCache == nil)
    {
        self.thumbImageCache = [[NSMutableDictionary alloc] init ];
    }
    
    return [self.thumbImageCache objectForKey:imageURL];

}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath;
{
    return 86;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        Feed *selectedFeed = [[self fetchedResultsControllerForTableView:tableView] objectAtIndexPath:indexPath];
        if (_detailSelectionDelegate) {
            [_detailSelectionDelegate selectedDetail:selectedFeed withIndex:indexPath withModule:self.module withController:self];
        }
    } else if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
        [self performSegueWithIdentifier:@"Show Feed Detail" sender:tableView];
    }
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

- (NSFetchedResultsController *)fetchedResultsControllerForTableView:(UITableView *)tableView
{
    return tableView == self.tableView ? self.fetchedResultsController : self.searchFetchedResultsController;
}


- (NSFetchedResultsController *)newFetchedResultsControllerWithSearch:(NSString *)searchString
{
    NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"Feed"];
    
    NSMutableArray *subPredicates = [[NSMutableArray alloc] init];
    NSPredicate *filterPredicate = [NSPredicate predicateWithFormat:@"(module.name = %@)", self.module.name];
    [subPredicates addObject:filterPredicate];
    
    if(searchString.length)
    {
        NSPredicate *searchPredicate = [NSPredicate predicateWithFormat:@"((title CONTAINS[cd] %@) OR (content CONTAINS[cd] %@))", searchString, searchString];
        [subPredicates addObject:searchPredicate];
    }
    
    if([self.hiddenCategories count] > 0) {
        NSPredicate *categoryPredicate = [NSPredicate predicateWithFormat:@"NONE category.name IN %@", self.hiddenCategories];
        [subPredicates addObject:categoryPredicate];
    }

    request.predicate = [NSCompoundPredicate andPredicateWithSubpredicates:subPredicates];

    request.sortDescriptors = [NSArray arrayWithObjects:[NSSortDescriptor sortDescriptorWithKey:@"dateLabel" ascending:NO ],[NSSortDescriptor sortDescriptorWithKey:@"postDateTime" ascending:NO ],[NSSortDescriptor sortDescriptorWithKey:@"title" ascending:YES ],nil ];
    
    NSFetchedResultsController *aFetchedResultsController = [[NSFetchedResultsController alloc] initWithFetchRequest:request
                                                                                                managedObjectContext:self.module.managedObjectContext
                                                                                                  sectionNameKeyPath:@"dateLabel"
                                                                                                           cacheName:nil];
    aFetchedResultsController.delegate = self;
    
    
    NSError *error = nil;
    if (![aFetchedResultsController performFetch:&error])
    {
        NSLog(@"Error performing institution fetch with search string %@: %@", error, [error userInfo]);
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
    
    if ([controller.fetchedObjects count] > 0 ) {
        
        NSIndexPath * head = [NSIndexPath indexPathForRow:0 inSection:0];
        Feed *selectedFeed = [controller objectAtIndexPath:head];
        
        if (_detailSelectionDelegate && selectedFeed) {
            [_detailSelectionDelegate selectedDetail:selectedFeed withIndex:head withModule:self.module withController:self];
        }
    } else if (_detailSelectionDelegate) {
        [_detailSelectionDelegate selectedDetail:nil withIndex:nil withModule:self.module withController:self];
    }
}
#pragma mark - fetch configuration

- (void) fetchFeeds
{
    
    NSManagedObjectContext *importContext = [[NSManagedObjectContext alloc] initWithConcurrencyType:NSPrivateQueueConcurrencyType];
    importContext.parentContext = self.module.managedObjectContext;
    [importContext setUndoManager:nil];
    NSString *urlString = [self.module propertyForKey:@"feed"];

    // If we don't have anything in the DB for events let's show the loading HUD.
    if ( [self.fetchedResultsController.fetchedObjects count] <= 0 )
    {
        MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo: self.view animated:YES];
        hud.labelText = NSLocalizedString(@"Loading", @"loading message while waiting for data to load");
    }
        
    [importContext performBlock: ^{

        //download data
        NSError *error;
        [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
     
        NSData *responseData = [NSData dataWithContentsOfURL:[NSURL URLWithString: urlString]];
        
        if(responseData)
        {
     
            NSDictionary* jsonResponse = [NSJSONSerialization
                                  JSONObjectWithData:responseData
                                  options:kNilOptions
                                  error:&error];
            
            NSMutableDictionary *previousFeeds = [[NSMutableDictionary alloc] init];
            NSMutableSet *newKeys = [[NSMutableSet alloc] init];
            
            NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"Feed"];
            
            NSPredicate *filterPredicate = [NSPredicate predicateWithFormat:@"module.name = %@", self.module.name];
            [request setPredicate:filterPredicate];
            NSArray * oldObjects = [importContext executeFetchRequest:request error:&error];
            for (Feed* oldObject in oldObjects) {
                //if it exists, add to the array so it will be deleted if not found in latest response.  If there is no entry id, go ahead and delete it and treat it as new if its in the new response.
                if([oldObject.entryId length] > 0) {
                    [previousFeeds setObject:oldObject forKey:oldObject.entryId];
                } else {
                    [importContext deleteObject:oldObject];
                }
            }
            
            NSFetchRequest *moduleRequest = [[NSFetchRequest alloc] init];
            NSEntityDescription *entity = [NSEntityDescription entityForName:@"FeedModule" inManagedObjectContext:importContext];
            NSPredicate *predicate =[NSPredicate predicateWithFormat:@"name = %@",self.module.name];
            [moduleRequest setEntity:entity];
            [moduleRequest setPredicate:predicate];
            
            FeedModule *feedModule = [[importContext executeFetchRequest:moduleRequest error:&error] lastObject];
            if(!feedModule) {
                feedModule = [NSEntityDescription insertNewObjectForEntityForName:@"FeedModule" inManagedObjectContext:importContext];
                feedModule.name = self.module.name;
            }
            
            NSFetchRequest *categoryRequest = [[NSFetchRequest alloc] init];
            NSEntityDescription *categoryEntity = [NSEntityDescription entityForName:@"FeedCategory" inManagedObjectContext:importContext];
            [categoryRequest setEntity:categoryEntity];
            NSPredicate *categoryPredicate =[NSPredicate predicateWithFormat:@"moduleName = %@",self.module.name];
            [categoryRequest setPredicate:categoryPredicate];
            
            NSArray *categoryArray = [importContext executeFetchRequest:categoryRequest error:&error];
            NSMutableDictionary *categoryMap = [[NSMutableDictionary alloc] init];
            for(FeedCategory *feedCategory in categoryArray) {
                [categoryMap setObject:feedCategory forKey:feedCategory.name];
            }

            //create/update objects
            int j=0;
            for(NSDictionary *json in [jsonResponse objectForKey:@"entries"]) {
                NSString *uid = [json objectForKey:@"entryId"];
                Feed *feed = [previousFeeds objectForKey:uid];

                if(feed) {
                    [previousFeeds removeObjectForKey:uid];
                } else {
                    j++;
                    feed = [NSEntityDescription insertNewObjectForEntityForName:@"Feed" inManagedObjectContext:importContext];
                    feed.module = feedModule;
                    [feedModule addFeedsObject:feed];
                        
                    [newKeys addObject:uid];
                    
                    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
                    [dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss'Z'"];
                    dateFormatter.timeZone = [NSTimeZone timeZoneForSecondsFromGMT:0];
                    
                    feed.entryId = [json objectForKey:@"entryId"];
                    feed.postDateTime = [dateFormatter dateFromString:[json objectForKey:@"postDate"]];
                    feed.dateLabel = [self.dateFormatterCoreData stringFromDate:feed.postDateTime];
                    if([[json objectForKey:@"link"] count] > 0) {
                        feed.link = [[json objectForKey:@"link"] objectAtIndex:0];
                    }
                    if([json objectForKey:@"title"] != [NSNull null]) {
                        feed.title = [[json objectForKey:@"title"] stringByConvertingHTMLToPlainText];
                    }
                    if([json objectForKey:@"content"] != [NSNull null]) {
                        feed.content = [json objectForKey:@"content"];
                    }
                    
                    if([json objectForKey:@"logo"] != [NSNull null]) {
                        feed.logo = [json objectForKey:@"logo"];
                    }

                    
                    
                    NSString *categoryLabel = [json objectForKey:@"feedName"];
                    FeedCategory* category = [categoryMap objectForKey:categoryLabel];
                    if(!category) {
                        category = [NSEntityDescription insertNewObjectForEntityForName:@"FeedCategory" inManagedObjectContext:importContext];
                        category.name = categoryLabel;
                        category.moduleName = self.module.name;
                        [categoryMap setObject:category forKey:category.name];
                    }
                    [feed addCategoryObject:category];
                    [category addFeedObject:feed];
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
        
 
        if (![importContext save:&error]) {
            NSLog(@"Could not save to main context after update to events: %@", [error userInfo]);
        }
            
        //find and delete old ones
        for (NSManagedObject * oldObject in [previousFeeds allValues]) {
            [importContext deleteObject:oldObject];
        }
     
        //save to main context
        if (![importContext save:&error]) {
            NSLog(@"Could not save to main context after update to events: %@", [error userInfo]);
        }
     
        //persist to store and update fetched result controllers
        [importContext.parentContext performBlock:^{
            NSError *parentError = nil;
            if(![importContext.parentContext save:&parentError])        {
                NSLog(@"Could not save to store after update to events: %@", [error userInfo]);
                }
            }];
        }
        [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
        dispatch_async(dispatch_get_main_queue(), ^{
            [self readFeedModule];
            [self.filterButton setEnabled:YES];
            [MBProgressHUD hideHUDForView:self.view animated:YES];
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

-(void) readFeedModule {
    NSError *error;
    NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"FeedModule"];
    request.predicate = [NSPredicate predicateWithFormat:@"name = %@", self.module.name];
    [request setFetchLimit:1];
    NSArray *fetchResults = [self.module.managedObjectContext executeFetchRequest:request error:&error];
    
    if([fetchResults count] > 0) {
        self.feedModule = [fetchResults objectAtIndex:0];
        self.hiddenCategories = [[NSMutableSet alloc] initWithArray:[[self.feedModule valueForKey:@"hiddenCategories"] componentsSeparatedByString:@","]];
    } else {
        self.hiddenCategories = [[NSMutableSet alloc] init];
    }
    
}

- (void)searchBarTextDidBeginEditing:(UISearchBar *)searchBar
{
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionSearch withLabel:@"Search" withValue:nil forModuleNamed:nil];
}

-(IBAction)chooseFilterButtonTapped:(id)sender
{
    if (_filterPicker == nil) {
        //Create the FeedFilterViewController.
        _filterPicker = (FeedFilterViewController *)[self.storyboard instantiateViewControllerWithIdentifier:@"FeedFilterView"];
    }
    
    [self sendEventWithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionList_Select withLabel:@"Select filter" withValue:nil forModuleNamed:self.module.name];
    
    _filterPicker.feedModule = self.feedModule;
    _filterPicker.hiddenCategories = self.hiddenCategories;
    _filterPicker.module = self.module;
    _filterPicker.delegate = self;
    
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
    [self readFeedModule];
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

-(BOOL)splitViewController:(UISplitViewController *)svc shouldHideViewController:(UIViewController *)vc inOrientation:(UIInterfaceOrientation)orientation
{
    return NO;
}

@end
