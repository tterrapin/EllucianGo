//
//  GradesViewController.m
//  Mobile
//
//  Created by Jason Hocker on 8/2/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "GradesViewController.h"
#import "GradesCell.h"
#import "GradeCourse.h"
#import "GradesCourseNameCell.h"
#import "Grade.h"
#import "NSData+AuthenticatedRequest.h"
#import "CurrentUser.h"
#import "UIColor+SchoolCustomization.h"
#import "EmptyTableViewCell.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "AppearanceChanger.h"

@interface GradesViewController ()

@property (strong, nonatomic) NSArray *terms;
@property (strong, nonatomic) NSDateFormatter *dateFormatter;
@property (strong, nonatomic) NSDateFormatter *datetimeFormatter;
@property (strong, nonatomic) NSDateFormatter *datetimeOutputFormatter;
@property (nonatomic, strong) GradesPageSelectionViewController *termPicker;
@property (nonatomic, strong) UIPopoverController *termPickerPopover;
@property (nonatomic, strong) NSMutableSet *pages;

@end

@implementation GradesViewController

-(void)viewDidLoad
{
    [super viewDidLoad];
    
    self.pages = [NSMutableSet new];
    self.navigationController.navigationBar.translucent = NO;
    
}

-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self loadGrades];
    [self fetchGrades];
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Grades list" forModuleNamed:self.module.name];
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

-(void) loadGrades
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"GradeTerm" inManagedObjectContext:self.module.managedObjectContext];
    [fetchRequest setEntity:entity];
    
    NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"startDate" ascending:NO];
    NSArray *sortDescriptors = [NSArray arrayWithObjects:sortDescriptor, nil];// sortDescriptorCourses, nil];
    [fetchRequest setSortDescriptors:sortDescriptors];
    
    NSError *error = nil;
    self.terms = [self.module.managedObjectContext executeFetchRequest:fetchRequest error:&error];
    
    if([self.terms count]> 0) {
        
        self.pageControl.numberOfPages = [self.terms count];
        
        pageControlBeingUsed = NO;
        
        for(UITableView *v in self.pages) {
            [v removeFromSuperview];
        }
        [self.pages removeAllObjects];
        
        for (int i = 0; i < [self.terms count]; i++) {
            CGRect frame;
            frame.origin.x = self.scrollView.frame.size.width * i;
            frame.origin.y = 0;
            frame.size = self.scrollView.frame.size;
            
            UITableView *subview = [[UITableView alloc] initWithFrame:frame];
            subview.dataSource = self;
            subview.delegate = self;
            subview.tag = i;
            [self.scrollView addSubview:subview];
            [self.pages addObject:subview];
        }
        
        self.scrollView.contentSize = CGSizeMake(self.scrollView.frame.size.width * [self.terms count], self.scrollView.frame.size.height);
        [self.pageControl addTarget:self action:@selector(changePage:) forControlEvents:UIControlEventValueChanged];
        [self changePage:self.pageControl];
    }
}

- (void)scrollViewDidScroll:(UIScrollView *)sender {
	if (!pageControlBeingUsed) {
		// Switch the indicator when more than 50% of the previous/next page is visible
		CGFloat pageWidth = self.scrollView.frame.size.width;
		int page = floor((self.scrollView.contentOffset.x - pageWidth / 2) / pageWidth) + 1;
        if(page != self.pageControl.currentPage) {
            [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionSlide_Action withLabel:@"Swipe Terms" withValue:nil forModuleNamed:self.module.name];
        }
		self.pageControl.currentPage = page;
	}
}

- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView {
	pageControlBeingUsed = NO;
}

- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView {
	pageControlBeingUsed = NO;
}

- (IBAction)changePage:(UIPageControl*)control
{
	// Update the scroll view to the appropriate page
	CGRect frame;
	frame.origin.x = self.scrollView.frame.size.width * self.pageControl.currentPage;
	frame.origin.y = 0;
	frame.size = self.scrollView.frame.size;
	[self.scrollView scrollRectToVisible:frame animated:YES];
    
	// Keep track of when scrolls happen in response to the page control
	// value changing. If we don't do this, a noticeable "flashing" occurs
	// as the the scroll delegate will temporarily switch back the page
	// number.
	pageControlBeingUsed = YES;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    switch(section) {
        case 0: return 1;
        default: {
            GradeTerm *term = [self.terms objectAtIndex:tableView.tag];
            GradeCourse *course = [term.courses objectAtIndex:section - 1];
            int count = [course.grades count];
            if(count == 0) return 1;
            else return count;
        }
    }
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    GradeTerm *term = [self.terms objectAtIndex:tableView.tag];
    return [term.courses count] + 1;
    
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    switch (section) {
        case 0: return 0;
        default: return 44;
    }
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    if(section == 0) {
        return nil;
    } else {
    
        GradeTerm *term = [self.terms objectAtIndex:tableView.tag];
        GradeCourse *course = [term.courses objectAtIndex:section - 1];
        
        UIView* h = [UIView new];
        h.backgroundColor = [UIColor accentColor];
        h.opaque = YES;
        
        UILabel* courseNameLabel = [UILabel new];
        if(course.courseSectionNumber) {
            courseNameLabel.text = [NSString stringWithFormat:@"%@-%@", course.courseName, course.courseSectionNumber];
        } else {
            courseNameLabel.text = course.courseName;
        }
        courseNameLabel.tag = 1;
        courseNameLabel.backgroundColor = [UIColor clearColor];
        courseNameLabel.textColor = [UIColor subheaderTextColor];
        courseNameLabel.font = [UIFont boldSystemFontOfSize:18];
        [courseNameLabel setMinimumScaleFactor:.5f];
        courseNameLabel.translatesAutoresizingMaskIntoConstraints = NO;
        [h addSubview:courseNameLabel];
        
        UILabel* sectionTitleLabel = [UILabel new];
        sectionTitleLabel.tag = 2;
        sectionTitleLabel.text = course.sectionTitle;
        sectionTitleLabel.backgroundColor = [UIColor clearColor];
        sectionTitleLabel.textColor = [UIColor subheaderTextColor];
        sectionTitleLabel.font = [UIFont systemFontOfSize:14];
        [sectionTitleLabel setMinimumScaleFactor:.5f];
        sectionTitleLabel.translatesAutoresizingMaskIntoConstraints = NO;
        [h addSubview:sectionTitleLabel];
        
        [h addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"H:|-20-[courseNameLabel]-(>=10)-|"
                                                 options:0 metrics:nil
                                                   views:@{@"courseNameLabel":courseNameLabel}]];
        [h addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"H:|-20-[sectionTitleLabel]-(>=10)-|"
                                                 options:0 metrics:nil
                                                   views:@{@"sectionTitleLabel":sectionTitleLabel}]];
        [h addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"V:|-2-[courseNameLabel][sectionTitleLabel]-2-|"
                                                 options:0 metrics:nil
                                                   views:@{@"courseNameLabel":courseNameLabel, @"sectionTitleLabel":sectionTitleLabel}]];
        return h;
    }
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    GradeTerm *term = [self.terms objectAtIndex:tableView.tag];

    
    switch ([indexPath section]) {
        case 0: {
            GradesCourseNameCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Grades Course Name Cell"];
            if (cell == nil) {
                
                NSArray* views = [[NSBundle mainBundle] loadNibNamed:@"GradesCourseNameCell" owner:nil options:nil];
                
                for (UIView *view in views) {
                    if([view isKindOfClass:[UITableViewCell class]])
                    {
                        cell = (GradesCourseNameCell*)view;
                    }
                }
            }
            
            cell.courseName.text = term.name;
            cell.courseName.textColor = [UIColor subheaderTextColor];
            cell.customBackgroundView.backgroundColor = [UIColor accentColor];
            if([AppearanceChanger isRTL]) {
                cell.courseName.textAlignment = NSTextAlignmentRight;
            }
            return cell;
        }
        default: {
            GradeCourse *course = [term.courses objectAtIndex:[indexPath section] - 1];

            if([course.grades count] == 0) {
                
                EmptyTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Grades Empty Cell"];
                if (cell == nil) {
                    
                    NSArray* views = [[NSBundle mainBundle] loadNibNamed:@"EmptyTableViewCell" owner:nil options:nil];
                    
                    for (UIView *view in views) {
                        if([view isKindOfClass:[EmptyTableViewCell class]])
                        {
                            cell = (EmptyTableViewCell*)view;
                        }
                    }
                }
                cell.label.text = NSLocalizedString(@"No Grades Recorded", @"no grades recorded message");
                return cell;
            } else {

                GradesCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Grades Cell"];
                if (cell == nil) {
                    
                    NSArray* views = [[NSBundle mainBundle] loadNibNamed:@"GradesTableViewCell" owner:nil options:nil];
                    
                    for (UIView *view in views) {
                        if([view isKindOfClass:[UITableViewCell class]])
                        {
                            cell = (GradesCell*)view;
                        }
                    }
                }
                
                           
                Grade *grade = [course.grades objectAtIndex:[indexPath row]];
                cell.gradeTypeLabel.text = grade.name;
                cell.gradeValueLabel.text = grade.value;
                NSString *formattedDate = [self.datetimeOutputFormatter stringFromDate:grade.lastUpdated];
                if(formattedDate) {
                    cell.gradeLastUpdated.text = [NSString stringWithFormat:NSLocalizedString(@"Last Updated: %@", @"label for when data was last updated"), formattedDate];
                } else {
                    cell.gradeLastUpdated.text = NSLocalizedString(@"Last Updated: Unavailable", @"label for when data for last updated time is unavailable");
                }
                return cell;
            }
            
        }
    }
}

#pragma mark - fetch grades

- (void) fetchGrades {
    
    NSManagedObjectContext *importContext = [[NSManagedObjectContext alloc] initWithConcurrencyType:NSPrivateQueueConcurrencyType];
    importContext.parentContext = self.module.managedObjectContext;
    NSString *userid = [CurrentUser userid];
    if(userid) {
        NSString *urlString = [NSString stringWithFormat:@"%@/%@", [self.module propertyForKey:@"grades"], [[CurrentUser userid]  stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding]];
        
        [importContext performBlock: ^{
            
            //download data
            NSError *error;
            NSURLResponse *response;
            [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
            NSData *responseData = [NSData dataWithContentsOfURLUsingCurrentUser:[NSURL URLWithString:urlString] returningResponse:&response error:&error];
            
            [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
            if(responseData) {
                NSDictionary* json = [NSJSONSerialization
                                      JSONObjectWithData:responseData
                                      options:kNilOptions
                                      error:&error];
            
                NSMutableDictionary *previousTerms = [[NSMutableDictionary alloc] init];
                NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"GradeTerm"];
                
                NSArray * oldObjects = [importContext executeFetchRequest:request error:&error];
                for (GradeTerm* oldObject in oldObjects) {
                    [importContext deleteObject:oldObject];
                }
                
                //create/update objects

                for(NSDictionary *termJson in [json objectForKey:@"terms"]) {
                    GradeTerm *gradeTerm = [NSEntityDescription insertNewObjectForEntityForName:@"GradeTerm"    inManagedObjectContext:importContext];
                    gradeTerm.termId = [termJson objectForKey:@"id"];
                    gradeTerm.name = [termJson objectForKey:@"name"];
                    gradeTerm.startDate = [self.dateFormatter dateFromString:[termJson objectForKey:@"startDate"]];
                    gradeTerm.endDate = [self.dateFormatter dateFromString:[termJson objectForKey:@"endDate"]];
                
                    for(NSDictionary *courseJson in [termJson objectForKey:@"sections"]) {

                        GradeCourse *gradeCourse = [NSEntityDescription insertNewObjectForEntityForName:@"GradeCourse" inManagedObjectContext:importContext];
                        gradeCourse.sectionId = [courseJson objectForKey:@"sectionId"];
                        gradeCourse.courseName = [courseJson objectForKey:@"courseName"];
                        if([courseJson objectForKey:@"sectionTitle"] != [NSNull null]) {
                            gradeCourse.sectionTitle = [courseJson objectForKey:@"sectionTitle"];
                        }
                        if([courseJson objectForKey:@"courseSectionNumber"] != [NSNull null]) {
                            gradeCourse.courseSectionNumber = [courseJson objectForKey:@"courseSectionNumber"];
                        }
                        //rdar://10114310
                        [gradeTerm addCoursesObject:gradeCourse];
                        gradeCourse.term = gradeTerm;
                        
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
                
                //find and delete old ones
                for (NSManagedObject * oldObject in [previousTerms allValues]) {
                    [importContext deleteObject:oldObject];
                }
                
                //save to main context
                if (![importContext save:&error]) {
                    NSLog(@"Could not save to main context after update to grades: %@", [error userInfo]);
                }
                
                //persist to store and update fetched result controllers
                [importContext.parentContext performBlock:^{
                    NSError *parentError = nil;
                    if(![importContext.parentContext save:&parentError]) {
                        NSLog(@"Could not save to store after update to grades: %@", [error userInfo]);
                    }
                    [self loadGrades ];
                }];
            }
        }];
    }
 
}

-(void)changeToPageNumber:(NSInteger)page {
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionList_Select withLabel:@"Select Term" withValue:nil forModuleNamed:self.module.name];
    self.pageControl.currentPage = page;
    [self changePage:self.pageControl];

}


- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:@"Choose Grades Term"])
    {
        UINavigationController *navController = [segue destinationViewController];
        GradesPageSelectionViewController *detailController = [[navController viewControllers] objectAtIndex:0];
        detailController.terms = self.terms;
        detailController.gradesChangePageDelegate = self;
        detailController.module = self.module;
    }
}

-(IBAction)chooseTermButtonTapped:(id)sender
{
    if (_termPicker == nil) {
        //Create the CoursesPageSelectionViewController.
        _termPicker = (GradesPageSelectionViewController *)[self.storyboard instantiateViewControllerWithIdentifier:@"GradeTermsView"];
    }
    
    _termPicker.terms = self.terms;
    _termPicker.gradesChangePageDelegate = self;
    _termPicker.module = self.module;
    _termPicker.delegate = self;
    
    [_termPicker sizeForPopover];
    
    if (!_termPickerPopover) {
        //The filter picker popover is not showing. Show it.
        _termPickerPopover = [[UIPopoverController alloc] initWithContentViewController:_termPicker];
        [_termPickerPopover presentPopoverFromBarButtonItem:(UIBarButtonItem *)sender
                                   permittedArrowDirections:UIPopoverArrowDirectionUp animated:YES];
        _termPickerPopover.delegate = _termPicker;
        _termPickerPopover.passthroughViews = nil;
        
    } else {
        [self dismissPopover];
    }
}

- (void)resetPopover
{
    _termPickerPopover = nil;
}

- (void)dismissPopover
{
    [_termPickerPopover dismissPopoverAnimated:YES];
    [_termPicker dismiss:self.module.name];
    _termPickerPopover = nil;
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    if (_termPickerPopover)
    {
        [self dismissPopover];
    }
    [self loadGrades];
}


@end

