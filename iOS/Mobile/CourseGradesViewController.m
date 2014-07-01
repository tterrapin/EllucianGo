//
//  CourseGradesViewController.m
//  Mobile
//
//  Created by Jason Hocker on 10/17/12.
//  Copyright (c) 2012-2014 Ellucian. All rights reserved.
//

#import "CourseGradesViewController.h"
#import "CurrentUser.h"
#import "AuthenticatedRequest.h"
#import "EmptyTableViewCell.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"

@interface CourseGradesViewController ()
@property (strong, nonatomic) NSDateFormatter *dateFormatter;
@property (strong, nonatomic) NSDateFormatter *datetimeFormatter;
@property (strong, nonatomic) NSDateFormatter *datetimeOutputFormatter;
@end

@implementation CourseGradesViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.navigationController.navigationBar.translucent = NO;
    
    NSError *error;
	if (![[self fetchedResultsController] performFetch:&error]) {
		// Update to handle the error appropriately.
		NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
	}
    
    self.navigationItem.title = self.courseNameAndSectionNumber;
    if([CurrentUser sharedInstance].isLoggedIn) {
        [self fetchGrades:self];
    }
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(fetchGrades:) name:kLoginExecutorSuccess object:nil];
    
    
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Course grades" forModuleNamed:self.module.name];
}

- (NSFetchedResultsController *)fetchedResultsController {
    
    if (_fetchedResultsController != nil) {
        return _fetchedResultsController;
    }
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription
                                   entityForName:@"Grade" inManagedObjectContext:self.module.managedObjectContext];
    [fetchRequest setEntity:entity];
    fetchRequest.predicate = [NSPredicate predicateWithFormat:@"course.sectionId == %@ and course.term.termId == %@", self.sectionId, self.termId];
    NSArray *sortDescriptors = [NSArray arrayWithObject:[[NSSortDescriptor alloc]
                                                         initWithKey:@"lastUpdated" ascending:NO]];
    [fetchRequest setSortDescriptors:sortDescriptors];
    
    NSFetchedResultsController *theFetchedResultsController =
    [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest
                                        managedObjectContext:self.module.managedObjectContext sectionNameKeyPath:nil
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
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    id  sectionInfo = [[_fetchedResultsController sections] objectAtIndex:section];
    NSInteger numberOfObjects = [sectionInfo numberOfObjects];
    
    if(numberOfObjects == 0 ){
        [self showNoDataView:NSLocalizedString(@"No Grades Recorded", @"no grades recorded message")];
    } else {
        [self hideNoDataView];
    }
    return numberOfObjects;
}


#pragma mark - Table view delegate

- (UITableViewCell *)configureAtIndexPath:(NSIndexPath *)indexPath {
   
    Grade *grade = [_fetchedResultsController objectAtIndexPath:indexPath];
                
    GradesCell *cell = [self.tableView dequeueReusableCellWithIdentifier:@"Grades Cell"];
    if (cell == nil) {
                    
        NSArray* views = [[NSBundle mainBundle] loadNibNamed:@"GradesTableViewCell" owner:nil options:nil];
        
        for (UIView *view in views) {
            if([view isKindOfClass:[UITableViewCell class]])
            {
                cell = (GradesCell*)view;
            }
        }
    }
                
    cell.gradeTypeLabel.text = grade.name;
    cell.gradeValueLabel.text = grade.value;
    NSString *formattedDate = [self.datetimeOutputFormatter stringFromDate:grade.lastUpdated];
    if(formattedDate) {
        cell.gradeLastUpdated.text = [NSString stringWithFormat:NSLocalizedString(@"Last Updated: %@", @"label for when data was last updated"), formattedDate];
    }
    
    return cell;
  
}

- (UITableViewCell *)tableView:(UITableView *)tableView
         cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    return [self configureAtIndexPath:indexPath];

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
            [self configureAtIndexPath:indexPath];
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
}

#pragma mark - fetch notifications

- (void) fetchGrades:(id) sender
{
    NSManagedObjectContext *importContext = [[NSManagedObjectContext alloc] initWithConcurrencyType:NSPrivateQueueConcurrencyType];
    importContext.parentContext = self.module.managedObjectContext;
    
    NSString *urlString = [NSString stringWithFormat:@"%@/%@?term=%@&section=%@", [self.module propertyForKey:@"grades"], [[[CurrentUser sharedInstance] userid] stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding], [self.termId stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding] ,[self.sectionId stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding]];
    [importContext performBlock: ^{
        
        //download data
        NSError *error;
        [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
        AuthenticatedRequest *authenticatedRequet = [AuthenticatedRequest new];
        NSData *responseData = [authenticatedRequet requestURL:[NSURL URLWithString:urlString] fromView:self];
        
        [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
        if(responseData) {
            NSDictionary* json = [NSJSONSerialization
                             JSONObjectWithData:responseData
                             options:kNilOptions
                             error:&error];
            
            NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"GradeTerm"];
            request.predicate = [NSPredicate predicateWithFormat:@"termId == %@", self.termId];
            NSArray *terms = [importContext executeFetchRequest:request error:&error];
            
            //create/update objects
            for(NSDictionary *termJson in [json objectForKey:@"terms"]) {
                GradeTerm *gradeTerm = nil;
                if([self.termId isEqualToString:[termJson objectForKey:@"id"]]) {
                     gradeTerm = [[terms filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"termId == %@", self.termId]] lastObject];
                    if(!gradeTerm) {
                        gradeTerm = [NSEntityDescription insertNewObjectForEntityForName:@"GradeTerm"    inManagedObjectContext:importContext];
                        gradeTerm.termId = [termJson objectForKey:@"id"];
                        gradeTerm.name = [termJson objectForKey:@"name"];
                        gradeTerm.startDate = [self.dateFormatter dateFromString:[termJson objectForKey:@"startDate"]];
                        gradeTerm.endDate = [self.dateFormatter dateFromString:[termJson objectForKey:@"endDate"]];
                    }
                }
                
                for(NSDictionary *courseJson in [termJson objectForKey:@"sections"]) {
                    NSArray* filteredTerms = [terms filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"termId = %@", self.termId]];
                    NSArray *courses = [[[filteredTerms lastObject] courses] array];
                    NSArray *filteredCourses = [courses filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"sectionId = %@", self.sectionId]];
                    GradeCourse *gradeCourse = [filteredCourses lastObject];
                    if(!gradeCourse) {
                    
                        gradeCourse = [NSEntityDescription insertNewObjectForEntityForName:@"GradeCourse" inManagedObjectContext:importContext];
                        gradeCourse.sectionId = [courseJson objectForKey:@"sectionId"];
                        gradeCourse.courseName = [courseJson objectForKey:@"courseName"];
                        gradeCourse.sectionTitle = [courseJson objectForKey:@"sectionTitle"];
                        gradeCourse.courseSectionNumber = [courseJson objectForKey:@"courseSectionNumber"];
                        gradeCourse.term = gradeTerm;
                        [gradeTerm addCoursesObject:gradeCourse];
                    }
                    
                    for (Grade* oldObject in gradeCourse.grades) {
                        [importContext deleteObject:oldObject];
                    }
                    
                    NSArray *gradesJson = [courseJson objectForKey:@"grades"];
                    for(NSDictionary *gradeJson in gradesJson) {
                        
                        Grade *grade = [NSEntityDescription insertNewObjectForEntityForName:@"Grade" inManagedObjectContext:importContext];
                        grade.name = [gradeJson objectForKey:@"name"];
                        grade.value = [gradeJson objectForKey:@"value"];
                        if([gradeJson objectForKey:@"updated"] != [NSNull null]) {
                            grade.lastUpdated = [self.datetimeFormatter dateFromString:[gradeJson objectForKey:@"updated"]];
                        }
                        [gradeCourse addGradesObject:grade];
                        grade.course = gradeCourse;
                    }
                }
            }
            
            if (![importContext save:&error]) {
                NSLog(@"Could not save to main context after update to grades: %@", [error userInfo]);
            }
            
            //persist to store and update fetched result controllers
            [importContext.parentContext performBlock:^{
                NSError *parentError = nil;
                if(![importContext.parentContext save:&parentError]) {
                    NSLog(@"Could not save to store after update to grades: %@", [error userInfo]);
                }
            }];
        }
    }];
    
}

- (NSDateFormatter *)dateFormatter {
    if (_dateFormatter == nil) {
        _dateFormatter = [[NSDateFormatter alloc] init];
        [_dateFormatter setDateFormat:@"yyyy-MM-dd"];
    }
    return _dateFormatter;
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
@end
