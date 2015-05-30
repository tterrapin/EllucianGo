//
//  RosterViewController.m
//  Mobile
//
//  Created by Jason Hocker on 10/2/12.
//  Copyright (c) 2012-2014 Ellucian. All rights reserved.
//

#import "RosterViewController.h"
#import "CurrentUser.h"
#import "AuthenticatedRequest.h"
#import "DirectoryViewController.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "Ellucian_GO-Swift.h"

@interface RosterViewController ()

@property (nonatomic, assign) UILocalizedIndexedCollation *collation;
@property (nonatomic, assign) NSMutableArray *collatedSections;

@end

@implementation RosterViewController


- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.navigationController.navigationBar.translucent = NO;
    
    NSError *error;
	if (![[self fetchedResultsController] performFetch:&error]) {
		NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
	}
    
    self.navigationItem.title = self.courseNameAndSectionNumber;
    if([CurrentUser sharedInstance].isLoggedIn) {
        [self fetchRoster:self];
    }
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(fetchRoster:) name:kLoginExecutorSuccess object:nil];
    
    
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Course roster list" forModuleNamed:self.module.name];
}

- (NSFetchedResultsController *)fetchedResultsController {
    
    if (_fetchedResultsController != nil) {
        return _fetchedResultsController;
    }
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription
                                   entityForName:@"CourseRoster" inManagedObjectContext:self.module.managedObjectContext];
    [fetchRequest setEntity:entity];
    fetchRequest.predicate = [NSPredicate predicateWithFormat:@"termId == %@ && sectionId == %@", self.termId, self.sectionId];
    
    
    [fetchRequest setSortDescriptors:[NSArray arrayWithObjects:[[NSSortDescriptor alloc]
                                                                initWithKey:@"lastName" ascending:YES],[[NSSortDescriptor alloc] initWithKey:@"firstName" ascending:YES], [[NSSortDescriptor alloc] initWithKey:@"middleName" ascending:YES],
                                      nil]];
    
    NSFetchedResultsController *theFetchedResultsController =
    [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest
                                        managedObjectContext:self.module.managedObjectContext sectionNameKeyPath:@"sectionKey"
                                                   cacheName:nil];
    self.fetchedResultsController = theFetchedResultsController;
    _fetchedResultsController.delegate = self;
    
    return _fetchedResultsController;
    
}

- (IBAction)dismiss:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - Table view data source
 - (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [[_fetchedResultsController sections] count];
}


-(NSString *)tableView:(UITableView *)tableView stringForTitleForHeaderInSection:(NSInteger)section
{
    return [[[_fetchedResultsController sections] objectAtIndex:section] name];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSInteger numberOfRows = 0;

    NSArray *sections = _fetchedResultsController.sections;
    if(sections.count > 0)
    {
        id <NSFetchedResultsSectionInfo> sectionInfo = [sections objectAtIndex:section];
        numberOfRows = [sectionInfo numberOfObjects];
    }
    return numberOfRows;
}

- (NSArray *)sectionIndexTitlesForTableView:(UITableView *)tableView
{
    return [[UILocalizedIndexedCollation currentCollation] sectionIndexTitles];
}

- (NSInteger)tableView:(UITableView *)tableView sectionForSectionIndexTitle:(NSString *)title atIndex:(NSInteger)index
{
    NSInteger localizedIndex = [[UILocalizedIndexedCollation currentCollation] sectionForSectionIndexTitleAtIndex:index];
    NSArray *localizedIndexTitles = [[UILocalizedIndexedCollation currentCollation] sectionIndexTitles];
    for(NSInteger currentLocalizedIndex = localizedIndex; currentLocalizedIndex > 0; currentLocalizedIndex--) {
        for(int frcIndex = 0; frcIndex < [[_fetchedResultsController sections] count]; frcIndex++) {
            id<NSFetchedResultsSectionInfo> sectionInfo = [[_fetchedResultsController sections] objectAtIndex:frcIndex];
            NSString *indexTitle = sectionInfo.indexTitle;
            if([indexTitle isEqualToString:[localizedIndexTitles objectAtIndex:currentLocalizedIndex]]) {
                return frcIndex;
            }
        }
    }
    return 0;
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    CourseRoster *roster = [_fetchedResultsController objectAtIndexPath:indexPath];
    [self performSegueWithIdentifier:@"Show Roster Person" sender:roster];
}

- (void)configureCell:(UITableViewCell *)cell atIndexPath:(NSIndexPath *)indexPath {
    CourseRoster *roster = [_fetchedResultsController objectAtIndexPath:indexPath];
    UILabel *nameLabel = (UILabel *)[cell viewWithTag:1];
    nameLabel.text = roster.name;
    NSString *directoryUrl = [[AppGroupUtilities userDefaults] objectForKey:@"urls-directory-studentSearch"];
    
    if(directoryUrl) {
        cell.userInteractionEnabled = YES;
        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
        cell.selectionStyle = UITableViewCellSelectionStyleBlue;
    } else {
        cell.userInteractionEnabled = NO;
        cell.accessoryType = UITableViewCellAccessoryNone;
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
    }
    
}

- (UITableViewCell *)tableView:(UITableView *)tableView
         cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    static NSString *CellIdentifier = @"Course Roster Cell";
    
    UITableViewCell *cell =
    [tableView dequeueReusableCellWithIdentifier:CellIdentifier];

    [self configureCell:cell atIndexPath:indexPath];
    
    return cell;
}

- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller {
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
        case NSFetchedResultsChangeMove:
            break;
        case NSFetchedResultsChangeUpdate:
            break;
    }
}

- (void)controllerDidChangeContent:(NSFetchedResultsController *)controller {
    [self.tableView endUpdates];
}

#pragma mark - fetch notifications
- (void) fetchRoster:(id)sender {
    
    NSManagedObjectContext *importContext = [[NSManagedObjectContext alloc] initWithConcurrencyType:NSPrivateQueueConcurrencyType];
    importContext.parentContext = self.module.managedObjectContext;
    NSString *urlString = [NSString stringWithFormat:@"%@/%@?term=%@&section=%@", [self.module propertyForKey:@"roster"], [[[CurrentUser sharedInstance] userid] stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding], [self.termId stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding] ,[self.sectionId stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding]];
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
            
            NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"CourseRoster"];
            request.predicate = [NSPredicate predicateWithFormat:@"termId == %@ && sectionId == %@", self.termId, self.sectionId];
            
            NSArray * oldObjects = [importContext executeFetchRequest:request error:&error];
            for (CourseRoster* oldObject in oldObjects) {
                [importContext deleteObject:oldObject];
            }
            
            for(NSDictionary *jsonDictionary in [json valueForKey:@"activeStudents"]) {
                CourseRoster *entry = [NSEntityDescription insertNewObjectForEntityForName:@"CourseRoster" inManagedObjectContext:importContext];

                entry.termId = self.termId;
                entry.sectionId = self.sectionId;
                entry.studentId = [jsonDictionary objectForKey:@"id"];
                entry.name = [jsonDictionary objectForKey:@"name"];
                entry.firstName = [jsonDictionary objectForKey:@"firstName"];
                if([jsonDictionary objectForKey:@"middleName"] != [NSNull null]) {
                    entry.middleName = [jsonDictionary objectForKey:@"middleName"];
                }
                entry.lastName = [jsonDictionary objectForKey:@"lastName"];
                if([jsonDictionary objectForKey:@"photo"] != [NSNull null]) {
                    entry.photo = [jsonDictionary objectForKey:@"photo"];
                }
                entry.sectionKey = [entry.lastName substringToIndex:1];
            }
        }
        
        if (![importContext save:&error]) {
            NSLog(@"Could not save to main context after update to course roster: %@", [error userInfo]);
        }

        [importContext.parentContext performBlock:^{
            NSError *parentError = nil;
            if(![importContext.parentContext save:&parentError]) {
                NSLog(@"Could not save to store after update to course roster: %@", [error userInfo]);
            }
        }];
    }
     ];
    
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:@"Show Roster Person"])
    {
        CourseRoster *roster = (CourseRoster *)sender;
        DirectoryViewController *detailController = [segue destinationViewController];
        detailController.initialQueryString = [NSString  stringWithFormat:@"%@ %@", roster.firstName, roster.lastName];
        detailController.initialScope = DirectoryViewTypeStudent;
        detailController.hideFaculty = YES;
        detailController.module = self.module;
    }
}


@end