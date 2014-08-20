//
//  ScheduleViewController.m
//  Mobile
//
//  Created by Jason Hocker on 9/25/12.
//  Copyright (c) 2012-2014 Ellucian. All rights reserved.
//

#import "ScheduleViewController.h"
#import "CurrentUser.h"
#import "AuthenticatedRequest.h"
#import "CourseSection.h"
#import "CourseDetailTabBarController.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"

@interface ScheduleViewController ()

@property (strong, nonatomic) NSArray *terms;
@property (strong, nonatomic) NSDateFormatter *dateFormatter;
@property (nonatomic, strong) CoursesPageSelectionViewController *termPicker;
@property (nonatomic, strong) UIPopoverController *termPickerPopover;
@property (nonatomic, strong) NSMutableSet *pages;


@end

@implementation ScheduleViewController

-(void) viewDidLoad
{
    [super viewDidLoad];
    
    self.pages = [NSMutableSet new];

    self.navigationController.navigationBar.translucent = NO;

    self.navigationItem.title = self.module.name;
    
    if([CurrentUser sharedInstance].isLoggedIn) {
        [self fetchSchedule:self];
    }
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(fetchSchedule:) name:kLoginExecutorSuccess object:nil];
    
}

-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self loadSchedule];
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];

    [self sendView:@"Schedule (full schedule)" forModuleNamed:self.module.name];
}

-(void) loadSchedule
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"CourseTerm" inManagedObjectContext:self.module.managedObjectContext];
    [fetchRequest setEntity:entity];
    
    NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"startDate" ascending:NO];
    NSArray *sortDescriptors = [NSArray arrayWithObjects:sortDescriptor, nil];
    [fetchRequest setSortDescriptors:sortDescriptors];
    
    NSError *error = nil;
    self.terms = [self.module.managedObjectContext executeFetchRequest:fetchRequest error:&error];
    
    if([self.terms count] > 0) {
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
            [subview registerClass:[UITableViewHeaderFooterView class] forHeaderFooterViewReuseIdentifier:@"Header"];
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
    CourseTerm *term = [self.terms objectAtIndex:tableView.tag];
    return [term.sections count];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
    
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    CourseTerm *term = [self.terms objectAtIndex:tableView.tag];

    static NSString *CellIdentifier = @"Schedule Cell";
    ScheduleCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        
        NSArray* views = [[NSBundle mainBundle] loadNibNamed:@"ScheduleTableViewCell" owner:nil options:nil];
        
        for (UIView *view in views) {
            if([view isKindOfClass:[UITableViewCell class]])
            {
                cell = (ScheduleCell*)view;
            }
        }
    }
    CourseSection *course = [term.sections objectAtIndex:[indexPath row]];
    cell.courseNameLabel.text = [NSString stringWithFormat:NSLocalizedStringWithDefaultValue(@"course name-section number", @"Localizable", [NSBundle mainBundle], @"%@-%@", @"course name-section number"), course.courseName, course.courseSectionNumber];
    cell.sectionTitleLabel.text = course.sectionTitle;
    return cell;
 
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionList_Select withLabel:@"Click Course" withValue:nil forModuleNamed:self.module.name];
    CourseTerm *term = [self.terms objectAtIndex:tableView.tag];
    CourseSection *course = [term.sections objectAtIndex:[indexPath row]];
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    [self performSegueWithIdentifier:@"Show Course Detail" sender:course];
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:@"Show Course Detail"])
    {
        CourseSection *course = (CourseSection *)sender;
        CourseDetailTabBarController *tabBarController = (CourseDetailTabBarController *)[segue destinationViewController];
        tabBarController.isInstructor = [course.isInstructor boolValue];
        tabBarController.module = self.module;
        tabBarController.termId = course.term.termId;
        tabBarController.sectionId = course.sectionId;
        for (UIViewController *v in tabBarController.viewControllers)
        {
            UIViewController *vc = v;
            
            if ([v isKindOfClass:[UINavigationController class]]) {
                UINavigationController *navVC = (UINavigationController *)v;
                vc = [navVC.viewControllers objectAtIndex:0];
            }
            if([vc respondsToSelector:@selector(setModule:)]) {
                [vc setValue:self.module forKey:@"module"];
            }
            if([vc respondsToSelector:@selector(setSectionId:)]) {
                [vc setValue:course.sectionId forKey:@"sectionId"];
            }
            if([vc respondsToSelector:@selector(setTermId:)]) {
                [vc setValue:course.term.termId forKey:@"termId"];
            }
            if([vc respondsToSelector:@selector(setCourseNameAndSectionNumber:)]) {
                NSString *courseNameAndSectionNumber = [NSString stringWithFormat:NSLocalizedStringWithDefaultValue(@"course name-section number", @"Localizable", [NSBundle mainBundle], @"%@-%@", @"course name-section number"), course.courseName, course.courseSectionNumber];
                [vc setValue:courseNameAndSectionNumber forKey:@"courseNameAndSectionNumber"];
            }

        }
    } else if ([[segue identifier] isEqualToString:@"Choose Courses Term"])
    {
        UINavigationController *navController = [segue destinationViewController];
        CoursesPageSelectionViewController *detailController = [[navController viewControllers] objectAtIndex:0];
        detailController.terms = self.terms;
        detailController.coursesChangePageDelegate = self;
        detailController.module = self.module;
    }

}

-(void)changeToPageNumber:(NSInteger)page {
    self.pageControl.currentPage = page;
    [self changePage:self.pageControl];
    
}

- (NSDateFormatter *)dateFormatter {
    if (_dateFormatter == nil) {
        _dateFormatter = [[NSDateFormatter alloc] init];
        [_dateFormatter setDateFormat:@"yyyy-MM-dd"];
    }
    return _dateFormatter;
}

- (void) fetchSchedule:(id)sender {
    
    NSManagedObjectContext *importContext = [[NSManagedObjectContext alloc] initWithConcurrencyType:NSPrivateQueueConcurrencyType];
    importContext.parentContext = self.module.managedObjectContext;
    NSString *userid = [[CurrentUser sharedInstance] userid];
    if(userid) {
        NSString *urlString = [NSString stringWithFormat:@"%@/%@", [self.module propertyForKey:@"full"], [userid stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding]];
                
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

                NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"CourseTerm"];
                
                NSArray * oldObjects = [importContext executeFetchRequest:request error:&error];
                for (CourseTerm* oldObject in oldObjects) {
                    [importContext deleteObject:oldObject];
                }

                if([json objectForKey:@"terms"] != [NSNull null]) {
                    
                    for(NSDictionary *termJson in [json objectForKey:@"terms"]) {
                        CourseTerm *term = [NSEntityDescription insertNewObjectForEntityForName:@"CourseTerm" inManagedObjectContext:importContext];
                        term.termId = [termJson objectForKey:@"id"];
                        term.name = [termJson objectForKey:@"name"];
                        term.startDate = [self.dateFormatter dateFromString:[termJson objectForKey:@"startDate"]];
                        term.endDate = [self.dateFormatter dateFromString:[termJson objectForKey:@"endDate"]];
                        
                        for(NSDictionary *sectionJson in [termJson objectForKey:@"sections"]) {
                        
                            CourseSection *course = [NSEntityDescription insertNewObjectForEntityForName:@"CourseSection" inManagedObjectContext:importContext];
                            course.sectionId = [sectionJson objectForKey:@"sectionId"];
                            course.sectionTitle = [sectionJson objectForKey:@"sectionTitle"];
                            course.isInstructor = [NSNumber numberWithBool:[[sectionJson objectForKey:@"isInstructor"] boolValue]];
                            course.courseName = [sectionJson objectForKey:@"courseName"];
                            course.courseSectionNumber = [sectionJson objectForKey:@"courseSectionNumber"];
                            [term addSectionsObject:course];
                            course.term = term;
                        }
                    }
                }
                
                if (![importContext save:&error]) {
                    NSLog(@"Could not save to main context after update to schedule: %@", [error userInfo]);
                }
                                
                [importContext.parentContext performBlock:^{
                    NSError *parentError = nil;
                    if(![importContext.parentContext save:&parentError]) {
                        NSLog(@"Could not save to store after update to grades: %@", [error userInfo]);
                    }
                    [self loadSchedule];
                }];
            }
        }];
    }
    
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    UITableViewHeaderFooterView* h = [tableView dequeueReusableHeaderFooterViewWithIdentifier:@"Header"];
    
    if (![h.backgroundColor isEqual: [UIColor accentColor]]) {
        
        h.contentView.backgroundColor = [UIColor accentColor];
        
        UILabel* headerLabel = [UILabel new];
        headerLabel.tag = 1;
        headerLabel.backgroundColor = [UIColor clearColor];
        headerLabel.textColor = [UIColor subheaderTextColor];
        headerLabel.font = [UIFont boldSystemFontOfSize:16];
        [headerLabel setMinimumScaleFactor:.5f];
        
        [h.contentView addSubview:headerLabel];
        
        headerLabel.translatesAutoresizingMaskIntoConstraints = NO;
        [h.contentView addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"H:|-10-[headerLabel]"
                                                 options:0 metrics:nil
                                                   views:@{@"headerLabel":headerLabel}]];
        [h.contentView addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"V:|[headerLabel]|"
                                                 options:0 metrics:nil
                                                   views:@{@"headerLabel":headerLabel}]];
        
        
    }
    UILabel* headerLabel = (UILabel*)[h.contentView viewWithTag:1];
    CourseTerm *term = [self.terms objectAtIndex:tableView.tag];
    headerLabel.text = term.name;
    
    return h;
}

-(IBAction)chooseTermButtonTapped:(id)sender
{
    if (_termPicker == nil) {
        //Create the CoursesPageSelectionViewController.
        _termPicker = (CoursesPageSelectionViewController *)[self.storyboard instantiateViewControllerWithIdentifier:@"CoursesTermsView"];
    }
    
    _termPicker.terms = self.terms;
    _termPicker.coursesChangePageDelegate = self;
    _termPicker.module = self.module;
    _termPicker.delegate = self;
    
    //[_termPicker initializeTerms];
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
    
    [self loadSchedule];
}
@end

