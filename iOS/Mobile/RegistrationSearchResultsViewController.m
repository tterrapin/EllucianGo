//
//  RegistrationSearchResultsViewController.m
//  Mobile
//
//  Created by Jason Hocker on 1/13/14.
//  Copyright (c) 2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import "RegistrationSearchResultsViewController.h"
#import "RegistrationPlannedSection.h"
#import "RegistrationTabBarController.h"
#import "AppearanceChanger.h"
#import "Module.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"

@interface RegistrationSearchResultsViewController ()
@property (strong, nonatomic) UIBarButtonItem *addToCartButton;
@end

@implementation RegistrationSearchResultsViewController

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    if(self.courses == 0){
        [self showNoDataView:NSLocalizedString(@"Search Results Empty", @"empty Search Results message")];
    } else {
        [self hideNoDataView];
    }
    
    [self updateStatusBar];
    [self sendView:@"Registration search results list" forModuleNamed:self.module.name];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    //setup toolbar
    UIBarButtonItem *flexibleItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:self action:nil];
    self.addToCartButton = [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"Add To Cart", "Add To Cart button") style:UIBarButtonItemStyleBordered target:self action:@selector(addToCart:)];
    if (SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"7.0")) {
        UIImage *registerButtonImage = [UIImage imageNamed:@"Registration Button"];
        [self.navigationController.toolbar setBackgroundImage:registerButtonImage forToolbarPosition:UIToolbarPositionBottom barMetrics:UIBarMetricsDefault];
    }
    
    self.toolbarItems = [NSArray arrayWithObjects:flexibleItem, self.addToCartButton, flexibleItem, nil];
    
}

-(void) viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [self.navigationController setToolbarHidden:YES animated:YES];
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.courses count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    RegistrationPlannedSection *plannedSection = [self.courses objectAtIndex:indexPath.row];
    
    static NSString *CellIdentifier = @"Registration Course Cell";
    
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
        if(plannedSection.maximumCredits) {
            line3bLabel.text = [NSString stringWithFormat:@" | %@-%@ %@", plannedSection.minimumCredits, plannedSection.maximumCredits, NSLocalizedString(@"Credits", @"Credits label for registration cart") ];
        } else {
            line3bLabel.text = [NSString stringWithFormat:@" | %@ %@", plannedSection.minimumCredits, NSLocalizedString(@"Credits", @"Credits label for registration cart") ];
        }
    } else {
        if(plannedSection.maximumCredits) {
            line3Label.text = [NSString stringWithFormat:@"%@-%@ %@", plannedSection.minimumCredits, plannedSection.maximumCredits, NSLocalizedString(@"Credits", @"Credits label for registration cart") ];
        } else {
            line3Label.text = [NSString stringWithFormat:@"%@ %@", plannedSection.minimumCredits, NSLocalizedString(@"Credits", @"Credits label for registration cart") ];
        }
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

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    RegistrationPlannedSection *plannedSection = [self.courses objectAtIndex:indexPath.row];
    plannedSection.selectedForRegistration = !plannedSection.selectedForRegistration;
    [tableView reloadRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationNone];
    [self updateStatusBar];
    
    if(plannedSection.selectedForRegistration && [plannedSection minimumCredits] && [plannedSection maximumCredits]) {
        UIAlertView * alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Credits", @"title of variable credits input alert") message:NSLocalizedString(@"Enter the number of Credits you want for this course", @"text for variable credits input alert") delegate:self cancelButtonTitle:nil otherButtonTitles:@"Submit",nil];
        alert.tag = indexPath.row;
        alert.delegate = self;
        alert.alertViewStyle = UIAlertViewStylePlainTextInput;
        UITextField * alertTextField = [alert textFieldAtIndex:0];
        alertTextField.keyboardType = UIKeyboardTypeDecimalPad;
        alertTextField.placeholder = NSLocalizedString(@"credits", @"variable credits input alert placeholder");
        [alert show];
    }
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if(alertView.tag == -1) return;
    NSString *text = [[alertView textFieldAtIndex:0] text];
    RegistrationPlannedSection *plannedSection = [self.courses objectAtIndex:alertView.tag];
    plannedSection.credits = @([text floatValue]);
    
}

- (BOOL)alertViewShouldEnableFirstOtherButton:(UIAlertView *)alertView
{
    if(alertView.tag == -1) return YES;
    RegistrationPlannedSection *plannedSection = [self.courses objectAtIndex:alertView.tag];
    NSString *text = [[alertView textFieldAtIndex:0] text];
    if([text length] > 0) {
        float f = [text floatValue];
        if( (f >= [plannedSection.minimumCredits floatValue] ) && (f <= [plannedSection.maximumCredits floatValue])) {
            return YES;
        }
    }
    return NO;
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{
    NSString *candidate = [[textField text] stringByReplacingCharactersInRange:range withString:string];
    if (!candidate || [candidate length] < 1 || [candidate isEqualToString:@""])
    {
        return YES;
    }
    NSDecimalNumber *number = [NSDecimalNumber decimalNumberWithString:candidate];
    if (!number || [number isEqualToNumber:[NSDecimalNumber notANumber]])
    {
        return NO;
    }
    return YES;
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
    RegistrationPlannedSection *plannedSection = [self.courses objectAtIndex:indexPath.row];
#warning implement
    //[self performSegueWithIdentifier:@"Show Planned Course Detail" sender:plannedSection];
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    //[self.navigationController setToolbarHidden:YES animated:YES];
    if ([[segue identifier] isEqualToString:@"Show Section Detail"]) {
        
    }
}

-(void) addToCart:(id)sender
{
    [self sendEventWithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionButton_Press withLabel:@"Add to cart" withValue:nil forModuleNamed:self.module.name];
    RegistrationTabBarController *tabController = self.registrationTabController;
    for(RegistrationPlannedSection *course in self.courses) {
        if(course.selectedForRegistration) {
            [tabController addSearchedSection:course];
            course.selectedForRegistration = NO;
        }
    }
    [self updateStatusBar];
    [self.tableView reloadData];
    
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Registration", @"message for add to cart title")
                                                    message:NSLocalizedString(@"Items added to the cart available for this session only.", @"message for add to cart alert that items added to cart available for this session only")
                                                   delegate:self
                                          cancelButtonTitle:NSLocalizedString(@"OK", @"OK")
                                          otherButtonTitles:nil];
    alert.tag = -1;
    [alert show];
}

-(RegistrationTabBarController *) registrationTabController
{
    return  (RegistrationTabBarController *)[self tabBarController];
}

-(void) updateStatusBar
{
    int count = 0;
    for(RegistrationPlannedSection *course in self.courses) {
        if(course.selectedForRegistration) {
            count++;
        }
        
    }
    [self.navigationController setToolbarHidden:!(count>0) animated:YES];
    self.addToCartButton.title = [NSString stringWithFormat:NSLocalizedString(@"Add To Cart (%d)", @"label for add to cart button in registration search"), count];
}

@end
