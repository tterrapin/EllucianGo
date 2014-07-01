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
#import "RegistrationPlannedSectionDetailViewController.h"

@interface RegistrationRegisteredSectionsViewController ()
@property (nonatomic, strong) NSNumberFormatter *creditsFormatter;
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
    self.navigationController.navigationBar.translucent = NO;
    
    [self checkEmptyList];
}

-(void) checkEmptyList
{
    BOOL empty = YES;
    for(RegistrationTerm *term in self.registrationTabController.terms) {
        NSArray *plannedSections = [self.registrationTabController registeredSections:term.termId];
        if([plannedSections count] > 0) {
            empty = NO;
            break;
        }
        
    }

    if(empty){
        [self showNoDataView:NSLocalizedString(@"No registered classes for open terms.", @"No registered classes for open terms message")];
    } else {
        [self hideNoDataView];
    }
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
    
    NSString *credits = [self.creditsFormatter stringFromNumber:plannedSection.credits];
    NSString *ceus = [self.creditsFormatter stringFromNumber:plannedSection.ceus];
    NSString *gradingType = @"";
    if (plannedSection.isAudit) {
        gradingType = [NSString stringWithFormat: @"| %@", NSLocalizedString(@"Audit", @"Audit label for registration")];
    }
    else if (plannedSection.isPassFail) {
        gradingType = [NSString stringWithFormat: @"| %@", NSLocalizedString(@"P/F", @"PassFail abbrev label for registration cart")];
    }
    
    if(faculty) {
        line3Label.text = [NSString stringWithFormat:@"%@", faculty];
        if (credits){
            line3bLabel.text = [NSString stringWithFormat:@" | %@ %@ %@", credits, NSLocalizedString(@"Credits", @"Credits label for registration"), gradingType ];
        }
        else if (ceus) {
            line3bLabel.text = [NSString stringWithFormat:@" | %@ %@ %@", ceus, NSLocalizedString(@"CEUs", @"CEUs label for registration"), gradingType ];
        }
    } else {
        if (credits) {
            line3Label.text = [NSString stringWithFormat:@"%@ %@ %@", credits, NSLocalizedString(@"Credits", @"Credits label for registration"), gradingType ];
        }
        else if (ceus) {
            line3Label.text = [NSString stringWithFormat:@"%@ %@ %@", ceus, NSLocalizedString(@"CEUs", @"CEUs label for registration"), gradingType ];
        }
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

    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        UISplitViewController *split = [self.registrationTabController childViewControllers][2];
        split.presentsWithGesture = YES;
        
        UINavigationController *controller = split.viewControllers[0];
        UINavigationController *detailNavController = split.viewControllers[1];
        
        UIViewController *masterController = controller.topViewController;
        UIViewController *detailController = detailNavController.topViewController;
        
        if([masterController conformsToProtocol:@protocol(UISplitViewControllerDelegate)]) {
            split.delegate = (id)masterController;
        }
        if([detailController conformsToProtocol:@protocol(UISplitViewControllerDelegate)]) {
            split.delegate = (id)detailController;
        }
        if( [detailController conformsToProtocol:@protocol(DetailSelectionDelegate)]) {
            if ( [masterController respondsToSelector:@selector(detailSelectionDelegate) ])
            {
                [masterController setValue:detailController forKey:@"detailSelectionDelegate"];
            }
        }
        
        if (_detailSelectionDelegate) {
            [_detailSelectionDelegate selectedDetail:plannedSection withIndex:indexPath withModule:self.module withController:self];

        }
    }
    else if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
        [self performSegueWithIdentifier:@"Show Section Detail" sender:plannedSection];
    }

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

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return [super tableView:tableView heightForHeaderInSection:section];
}

-(NSNumberFormatter *)creditsFormatter
{
    if(_creditsFormatter == nil) {
        _creditsFormatter = [NSNumberFormatter new];
        _creditsFormatter.numberStyle = NSNumberFormatterDecimalStyle;
        [_creditsFormatter setMinimumFractionDigits:1];
    }
    return _creditsFormatter;
}

#pragma mark - logic

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    [self.navigationController setToolbarHidden:YES animated:YES];
    if ([[segue identifier] isEqualToString:@"Show Section Detail"]) {
        RegistrationPlannedSection *courseSection = sender;
        RegistrationPlannedSectionDetailViewController *detailController = [segue destinationViewController];
        detailController.registrationPlannedSection = courseSection;
    }
}

-(BOOL)splitViewController:(UISplitViewController *)svc shouldHideViewController:(UIViewController *)vc inOrientation:(UIInterfaceOrientation)orientation
{
    return NO;
}

-(void) reloadData:(id)sender
{
    [self.tableView reloadData];
    [self checkEmptyList];
}

@end
