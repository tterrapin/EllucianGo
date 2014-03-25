//
//  RegistrationRegisteredSectionsViewController.m
//  Mobile
//
//  Created by Jason Hocker on 1/24/14.
//  Copyright (c) 2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import "RegistrationRegisteredSectionsViewController.h"
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

@interface RegistrationRegisteredSectionsViewController ()

@end

@implementation RegistrationRegisteredSectionsViewController

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Registration Registered Sections list" forModuleNamed:self.module.name];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(reloadData:) name:kRegistrationPlanDataReloaded object:nil];
    
    self.navigationItem.title = [self.module name];
}

#pragma mark - variables from tab
-(RegistrationTabBarController *) registrationTabController
{
    return  (RegistrationTabBarController *)[self tabBarController];
}

#pragma mark - table

- (UITableViewCell *)tableView:(UITableView *)tableView configureCell:(NSIndexPath *)indexPath
{
    RegistrationTerm *term = [self.registrationTabController.terms objectAtIndex:indexPath.section];
    NSArray *plannedSections = [self.registrationTabController registeredSections:term.termId];
    RegistrationPlannedSection *plannedSection = [plannedSections objectAtIndex:indexPath.row];
    
    static NSString *CellIdentifier = @"Registration Section Cell";
    
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
    
    return cell;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [self tableView:tableView configureCell:indexPath];
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    RegistrationTerm *term = [self.registrationTabController.terms objectAtIndex:indexPath.section];
    NSArray *plannedSections = [self.registrationTabController registeredSections:term.termId];
    RegistrationPlannedSection *plannedSection = [plannedSections objectAtIndex:indexPath.row];
#warning implement
    //    [self performSegueWithIdentifier:@"Show Planned Course Detail" sender:plannedSection];
}

- (NSString *)tableView:(UITableView *)tableView stringForTitleForHeaderInSection:(NSInteger)section
{

    RegistrationTerm *term = [self.registrationTabController.terms objectAtIndex:section];
    return term.name;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    RegistrationTerm *term = [self.registrationTabController.terms objectAtIndex:section ];
    NSArray *plannedSections = [self.registrationTabController registeredSections:term.termId];
    return [plannedSections count];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [self.registrationTabController.terms count];
}

//- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
//{
//    return 86.0f;
//}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return [super tableView:tableView heightForHeaderInSection:section];
}


#pragma mark - logic

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    [self.navigationController setToolbarHidden:YES animated:YES];
    if ([[segue identifier] isEqualToString:@"Show Section Detail"]) {
        
    }
}

-(BOOL)splitViewController:(UISplitViewController *)svc shouldHideViewController:(UIViewController *)vc inOrientation:(UIInterfaceOrientation)orientation
{
    return NO;
}

-(void) reloadData:(id)sender
{
    [self.tableView reloadData];
}

@end
