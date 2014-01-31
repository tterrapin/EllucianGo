//
//  RegistrationCartViewController.m
//  Mobile
//
//  Created by jkh on 11/18/13.
//  Copyright (c) 2013 - 2014 Ellucian. All rights reserved.
//

#import "RegistrationCartViewController.h"
#import "MBProgressHUD.h"
#import "RegistrationPlannedSection.h"
#import "Module+Attributes.h"
#import "CurrentUser.h"
#import "NSData+AuthenticatedRequest.h"
#import "RegistrationPlannedSectionInstructor.h"
#import "RegistrationPlannedSectionMeetingPattern.h"
#import "RegistrationTerm.h"
#import <AddressBook/AddressBook.h>
#import "MBProgressHUD.h"
#import "RegistrationResultsViewController.h"
#import "NSMutableURLRequest+BasicAuthentication.h"
#import "AppDelegate.h"
#import "RegistrationTabBarController.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"

@interface RegistrationCartViewController ()
@property (strong, nonatomic) UIBarButtonItem *registerButton;
@end

@implementation RegistrationCartViewController

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Registration Cart list" forModuleNamed:self.module.name];
    [self updateStatusBar];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(reloadData:) name:kRegistrationPlanDataReloaded object:nil];

    self.navigationItem.title = [self.module name];

    UIBarButtonItem *flexibleItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:self action:nil];
    self.registerButton = [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"Register", "Register button") style:UIBarButtonItemStyleBordered target:self action:@selector(startRegistration:)];
    if (SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"7.0")) {
        UIImage *registerButtonImage = [UIImage imageNamed:@"Registration Button"];
        [self.navigationController.toolbar setBackgroundImage:registerButtonImage forToolbarPosition:UIToolbarPositionBottom barMetrics:UIBarMetricsDefault];
    }
    
    self.toolbarItems = [NSArray arrayWithObjects:flexibleItem, self.registerButton, flexibleItem, nil];

}

#pragma mark - variables from tab
-(RegistrationTabBarController *) registrationTabController
{
    return  (RegistrationTabBarController *)[self tabBarController];
}

-(NSString *) planId
{
    return self.registrationTabController.planId;
}

-(BOOL) registrationAllowed
{
    return self.registrationTabController.registrationAllowed;
}

#pragma mark - table

- (NSIndexPath *)tableView:(UITableView *)tableView willSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    // rows in section 0 should not be selectable
    if ( indexPath.section == 0 ) return nil;
    return indexPath;
}


- (UITableViewCell *)tableView:(UITableView *)tableView configureCell:(NSIndexPath *)indexPath
{
    if(indexPath.section == 0) {
        UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Registration Ineligible Cell"];
        UILabel *label = (UILabel *)[cell viewWithTag:20];
        label.text = self.registrationTabController.ineligibleMessage;
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
        [cell.contentView setNeedsLayout];
        [cell.contentView layoutIfNeeded];
        return cell;
    }
    RegistrationTerm *term = [self.registrationTabController.terms objectAtIndex:indexPath.section - 1];
    NSArray *plannedSections = [self.registrationTabController sectionsInCart:term.termId];
    RegistrationPlannedSection *plannedSection = [plannedSections objectAtIndex:indexPath.row];
    
    static NSString *CellIdentifier = @"Registration Planned Course Cell";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
        cell.accessoryType = UITableViewCellAccessoryNone;
    }
    
    UILabel *line1aLabel = (UILabel *)[cell viewWithTag:1];
    line1aLabel.text = [NSString stringWithFormat:@"%@-%@", plannedSection.courseName, plannedSection.courseSectionNumber];
    UILabel *line1bLabel = (UILabel *)[cell viewWithTag:6];
    if(plannedSection.instructionalMethod) {
        line1bLabel.text = [NSString stringWithFormat:@"(%@)", plannedSection.instructionalMethod];
    } else
    {
        line1bLabel.text = nil;
    }
    UILabel *line2Label = (UILabel *)[cell viewWithTag:2];
    line2Label.text = plannedSection.sectionTitle;
    UILabel *line3Label = (UILabel *)[cell viewWithTag:3];
    UILabel *line3bLabel = (UILabel *)[cell viewWithTag:5];
    NSString *faculty = [plannedSection facultyNames];
    if(faculty) {
        line3Label.text = [NSString stringWithFormat:@"%@", faculty];
        line3bLabel.text = [NSString stringWithFormat:@" | %@ %@", plannedSection.credits, NSLocalizedString(@"Credits", @"Credits label for registration cart") ];
    } else {
        line3Label.text = [NSString stringWithFormat:@"%@ %@", plannedSection.credits, NSLocalizedString(@"Credits", @"Credits label for registration cart") ];
        line3bLabel.text = nil;
    }
    UILabel *line4Label = (UILabel *)[cell viewWithTag:4];
    if(plannedSection.meetingPatternDescription) {
        line4Label.text = [NSString stringWithFormat:@"%@", plannedSection.meetingPatternDescription];
    } else {
        line4Label.text = nil;
    }
    
    UIImageView *checkmarkImageView = (UIImageView *)[cell viewWithTag:100];
    if(plannedSection.selectedForRegistration) {
        checkmarkImageView.image = [UIImage imageNamed:@"Registration Checkmark"];
    } else {
        checkmarkImageView.image = [UIImage imageNamed:@"Registration Circle"];
    }
    
    //    if ([[[UIDevice currentDevice] systemVersion] floatValue] >= 7) {
    //        cell.accessoryType = UITableViewCellAccessoryDetailButton;
    //    } else {
    //        cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
    //    }
    //    cell.accessoryView = [[UIImageView alloc ] initWithImage: [UIImage imageNamed:@"Registration Detail"]];
    
    UIImage *image = [UIImage imageNamed:@"Registration Detail"];
    UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
    CGRect frame = CGRectMake(0.0, 0.0, image.size.width, image.size.height);
    button.frame = frame;
    [button setBackgroundImage:image forState:UIControlStateNormal];
    
    [button addTarget: self
               action: @selector(accessoryButtonTapped:withEvent:)
     forControlEvents: UIControlEventTouchUpInside];
    cell.accessoryView = button;
    
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    
    return cell;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [self tableView:tableView configureCell:indexPath];
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    RegistrationTerm *term = [self.registrationTabController.terms objectAtIndex:indexPath.section - 1];
    NSArray *plannedSections = [self.registrationTabController sectionsInCart:term.termId];
    RegistrationPlannedSection *plannedSection = [plannedSections objectAtIndex:indexPath.row];
    plannedSection.selectedForRegistration = !plannedSection.selectedForRegistration;
    [tableView reloadRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationNone];
    [self updateStatusBar];
}

- (NSString *)tableView:(UITableView *)tableView stringForTitleForHeaderInSection:(NSInteger)section
{
    if(section > 0) {
        RegistrationTerm *term = [self.registrationTabController.terms objectAtIndex:section - 1];
        return term.name;
    } else {
        return NSLocalizedString(@"Ineligible for Registration", @"Ineligible for Registration");
    }
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if(section == 0) {
        return self.registrationTabController.ineligibleMessage ? 1 : 0;
    }
    RegistrationTerm *term = [self.registrationTabController.terms objectAtIndex:section - 1];
    NSArray *plannedSections = [self.registrationTabController sectionsInCart:term.termId];
    return [plannedSections count];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [self.registrationTabController.terms count] + 1;
}

- (void) accessoryButtonTapped: (UIControl *) button withEvent: (UIEvent *) event
{
    NSIndexPath * indexPath = [self.tableView indexPathForRowAtPoint: [[[event touchesForView: button] anyObject] locationInView: self.tableView]];
    if ( indexPath == nil )
        return;
    
    [self.tableView.delegate tableView: self.tableView accessoryButtonTappedForRowWithIndexPath: indexPath];
}

- (void)tableView:(UITableView *)tableView accessoryButtonTappedForRowWithIndexPath:(NSIndexPath *)indexPath
{
    RegistrationTerm *term = [self.registrationTabController.terms objectAtIndex:indexPath.section - 1];
    NSArray *plannedSections = [self.registrationTabController sectionsInCart:term.termId];
    RegistrationPlannedSection *plannedSection = [plannedSections objectAtIndex:indexPath.row];
#warning implement
    //    [self performSegueWithIdentifier:@"Show Planned Course Detail" sender:plannedSection];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    switch(section) {
        case 0:
            if(self.registrationTabController.ineligibleMessage) {
                return [super tableView:tableView heightForHeaderInSection:section];
            } else {
                return 0.0f;
            }
        default:
            return [super tableView:tableView heightForHeaderInSection:section];
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    switch (indexPath.section) {
        case 0:
        {
            UITableViewCell *cell = [self tableView:tableView configureCell:indexPath];
            CGFloat height = [cell.contentView systemLayoutSizeFittingSize:UILayoutFittingCompressedSize].height;
            return height + 14;
            break;
        }
        default:
            return 86.0f;
    }
}


#pragma mark - logic
-(void) updateBadge
{
    int count = [self.registrationTabController itemsInCartCount];
    
    if(!self.registrationTabController.ineligibleMessage && count == 0){
         [self showNoDataView:NSLocalizedString(@"Registration Cart Empty", @"empty registration cart message")];
    } else {
        [self hideNoDataView];
    }

    NSString *badgeValue = [NSString stringWithFormat:@"%d", count];
    [[[[[self tabBarController] tabBar] items] objectAtIndex:0] setBadgeValue:badgeValue];
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    [self.navigationController setToolbarHidden:YES animated:YES];
    if ([[segue identifier] isEqualToString:@"Show Planned Course Detail"]) {
        
    } else if ([[segue identifier] isEqualToString:@"Register"]) {
        NSDictionary *messages = (NSDictionary *)sender;
        id detailController = [segue destinationViewController];
        if([detailController isKindOfClass:[UINavigationController class]]) {
            detailController = ((UINavigationController *)detailController).childViewControllers[0];
        }
        
        RegistrationResultsViewController *resultsViewController = (RegistrationResultsViewController *)detailController;
        resultsViewController.module = self.module;
        resultsViewController.importantMessages = [messages objectForKey:@"messages"];
        resultsViewController.registeredMessages = [messages objectForKey:@"successes"];
        resultsViewController.warningMessages = [messages objectForKey:@"failures"];
        resultsViewController.delegate = self.registrationTabController;
        
        for(NSDictionary *registeredMessage in [messages objectForKey:@"successes"]) {
            NSString *section = [registeredMessage objectForKey:@"sectionId"];
            NSString *term = [registeredMessage objectForKey:@"termId"];
            RegistrationTabBarController *tab = self.registrationTabController;
            [tab removeSearchedSection:section term:term];
        }
    }
}


- (IBAction)startRegistration:(id)sender {
    
    UIActionSheet *actionSheet = [[UIActionSheet alloc] initWithTitle:nil
                                                             delegate:self
                                                    cancelButtonTitle:NSLocalizedString(@"Cancel", @"Cancel")
                                               destructiveButtonTitle:NSLocalizedString(@"Register", @"Register button")
                                                    otherButtonTitles:nil];
    
    
    if(UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        [actionSheet showFromBarButtonItem:self.registerButton animated:YES];
        } else {
        [actionSheet showFromToolbar:self.navigationController.toolbar];
    }
}

- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex {
    if (buttonIndex == actionSheet.destructiveButtonIndex) {
        [self sendEventWithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionButton_Press withLabel:@"Register" withValue:nil forModuleNamed:self.module.name];
        [self registerSelectedCourses];
    }
}

-(void) updateStatusBar
{
    int count = 0;
    for(RegistrationTerm *term in self.self.registrationTabController.terms) {
        NSArray *plannedSections = [self.registrationTabController sectionsInCart:term.termId];
        for(RegistrationPlannedSection *course in plannedSections) {
            if(course.selectedForRegistration) {
                count++;
            }
        }
    }
    [self.navigationController setToolbarHidden:!(count>0) animated:YES];
    self.registerButton.enabled = self.registrationAllowed;
    self.registerButton.title = [NSString stringWithFormat:NSLocalizedString(@"Register (%d)", @"label for register button in cart"), count];
}

-(BOOL)splitViewController:(UISplitViewController *)svc shouldHideViewController:(UIViewController *)vc inOrientation:(UIInterfaceOrientation)orientation
{
    return NO;
}

-(void) reloadData:(id)sender
{
    [self.tableView reloadData];
    [self updateBadge];
    
}

-(void) registerSelectedCourses
{
    MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo: self.view animated:YES];
    hud.labelText = NSLocalizedString(@"Registering", @"loading message while waiting for registration");
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 0.01 * NSEC_PER_SEC);
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void) {
        
        NSMutableArray *sectionRegistrations = [NSMutableArray new];
        
        for(RegistrationTerm *term in self.registrationTabController.terms) {
            NSArray *plannedSections = [self.registrationTabController sectionsInCart:term.termId];
            for(RegistrationPlannedSection *course in plannedSections) {
                if(course.selectedForRegistration) {
                    NSDictionary *sectionToRegister = @{
                                                        @"termId": term.termId,
                                                        @"sectionId": course.sectionId,
                                                        @"action": @"Add",
                                                        @"credits": course.credits
                                                        };
                    [sectionRegistrations addObject:sectionToRegister];
                }
            }
        }
        
        NSDictionary *postDictionary = @{
                                         @"planId": self.planId,
                                         @"sectionRegistrations": sectionRegistrations,
                                         };
        NSError *jsonError;
        NSData * jsonData = [NSJSONSerialization dataWithJSONObject:postDictionary options:NSJSONWritingPrettyPrinted error:&jsonError];
        
        NSString *urlString = [NSString stringWithFormat:@"%@/%@/register-sections", [self.module propertyForKey:@"registration"], [[CurrentUser userid]  stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding]];
        
        NSMutableURLRequest * urlRequest = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:urlString]];
        //[urlRequest setValue:@"application/json" forHTTPHeaderField:@"Accept"];
        [urlRequest setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
        
        // create a plaintext string in the format username:password
        [urlRequest addAuthenticationHeader];
        
        [urlRequest setHTTPMethod:@"PUT"];
        [urlRequest setHTTPBody:jsonData];
        NSError *error;
        NSURLResponse *response;
        NSData *responseData = [NSURLConnection sendSynchronousRequest:urlRequest returningResponse:&response error:&error];
        
        NSHTTPURLResponse* httpResponse = (NSHTTPURLResponse*)response;
        int code = [httpResponse statusCode];
        NSLog(@"put code: %d", code);
        NSDictionary *jsonResponse = [NSJSONSerialization JSONObjectWithData:responseData options:NSJSONReadingMutableContainers error:&error];
        BOOL success = YES;
        [MBProgressHUD hideHUDForView:self.view animated:YES];
        if(success) {
            [self.registrationTabController fetchRegistrationPlans];
            [self performSegueWithIdentifier:@"Register" sender:jsonResponse];
        }
    });
}



@end
