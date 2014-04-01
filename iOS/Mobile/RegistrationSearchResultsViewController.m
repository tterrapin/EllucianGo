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
#import "RegistrationPlannedSectionDetailViewController.h"

@interface RegistrationSearchResultsViewController ()
@property (strong, nonatomic) UIBarButtonItem *addToCartButton;
@end

@implementation RegistrationSearchResultsViewController

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    if([self.courses count] == 0){
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
    self.navigationController.navigationBar.translucent = NO;
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
    
    NSNumberFormatter *formatter = [NSNumberFormatter new];
    formatter.numberStyle = NSNumberFormatterDecimalStyle;
    [formatter setMinimumFractionDigits:1];
    
    NSString *minCredits = [formatter stringFromNumber:plannedSection.minimumCredits];
    NSString *maxCredits = [formatter stringFromNumber:plannedSection.maximumCredits];
    NSString *ceus = [formatter stringFromNumber:plannedSection.ceus];
    
    
    if(faculty) {
        line3Label.text = [NSString stringWithFormat:@"%@", faculty];
        if(plannedSection.maximumCredits) {
            line3bLabel.text = [NSString stringWithFormat:@" | %@-%@ %@", minCredits, maxCredits, NSLocalizedString(@"Credits", @"Credits label for registration") ];
        } else if (plannedSection.minimumCredits){
            line3bLabel.text = [NSString stringWithFormat:@" | %@ %@", minCredits, NSLocalizedString(@"Credits", @"Credits label for registration") ];
        } else if (plannedSection.ceus ) {
            line3bLabel.text = [NSString stringWithFormat:@" | %@ %@", ceus, NSLocalizedString(@"CEUs", @"CEUs label for registration") ];
        }
    } else {
        if(plannedSection.maximumCredits) {
            line3Label.text = [NSString stringWithFormat:@"%@-%@ %@", minCredits, maxCredits, NSLocalizedString(@"Credits", @"Credits label for registration") ];
        } else if(plannedSection.minimumCredits){
            line3Label.text = [NSString stringWithFormat:@"%@ %@", minCredits, NSLocalizedString(@"Credits", @"Credits label for registration") ];
        } else if (plannedSection.ceus) {
            line3Label.text = [NSString stringWithFormat:@"%@ %@", ceus, NSLocalizedString(@"CEUs", @"CEUs label for registration") ];
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
    CGRect frame = CGRectMake(0.0f, 0.0f, 44.0f, 44.0f);
    button.frame = frame;
    [button setImage:image forState:UIControlStateNormal];
    
    [button addTarget: self
               action: @selector(accessoryButtonTapped:withEvent:)
     forControlEvents: UIControlEventTouchUpInside];
    cell.accessoryView = button;
    
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    
    return cell;

}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if ( _allowAddToCart ) {
        RegistrationPlannedSection *plannedSection = [self.courses objectAtIndex:indexPath.row];
        if(!plannedSection.selectedForRegistration && [plannedSection minimumCredits] && [plannedSection maximumCredits]) {
            NSString *title;
            if(plannedSection.variableCreditIncrement) {
                title = NSLocalizedString(@"Enter course credit value between %@ and %@ in increments of %@", @"text for variable credits with credit incrementprompt");
                title = [NSString stringWithFormat:title, plannedSection.minimumCredits, plannedSection.maximumCredits, plannedSection.variableCreditIncrement];
            } else {
                title = NSLocalizedString(@"Enter course credit value between %@ and %@", @"text for variable credits prompt");
                title = [NSString stringWithFormat:title, plannedSection.minimumCredits, plannedSection.maximumCredits];
            }
            UIAlertView * alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Credits", @"Credits label for registration") message:title delegate:self cancelButtonTitle:NSLocalizedString(@"Cancel", @"Cancel") otherButtonTitles:NSLocalizedString(@"OK", @"OK"), nil];
            alert.tag = indexPath.row;
            alert.delegate = self;
            alert.alertViewStyle = UIAlertViewStylePlainTextInput;
            UITextField * alertTextField = [alert textFieldAtIndex:0];
            alertTextField.keyboardType = UIKeyboardTypeDecimalPad;
            alertTextField.placeholder = NSLocalizedString(@"Credits", @"Credits label for registration");
            [alert show];
        } else {
            [self tableView:tableView addSectionToCart:indexPath];
        }
    }
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if(alertView.tag == -1) return;
    NSInteger row = alertView.tag;
    if(buttonIndex == 1) {
        NSString *text = [[alertView textFieldAtIndex:0] text];
        RegistrationPlannedSection *plannedSection = [self.courses objectAtIndex:row];
        plannedSection.credits = @([text floatValue]);
        [self tableView:self.tableView addSectionToCart:[NSIndexPath indexPathForRow:row inSection:0]];
    }

}

-(void)tableView:(UITableView *)tableView addSectionToCart:(NSIndexPath *)indexPath
{
    RegistrationPlannedSection *plannedSection = [self.courses objectAtIndex:indexPath.row];
    plannedSection.selectedForRegistration = !plannedSection.selectedForRegistration;
    [tableView reloadRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationNone];
    [self updateStatusBar];
    
}

- (BOOL)alertViewShouldEnableFirstOtherButton:(UIAlertView *)alertView
{
    if(alertView.tag == -1) return YES;
    RegistrationPlannedSection *plannedSection = [self.courses objectAtIndex:alertView.tag];
    NSString *text = [[alertView textFieldAtIndex:0] text];
    if([text length] > 0) {
        float f = [text floatValue];
        if( (f >= [plannedSection.minimumCredits floatValue] ) && (f <= [plannedSection.maximumCredits floatValue])) {
            if(plannedSection.variableCreditIncrement) {
                float modulo = fmodf(f, [plannedSection.variableCreditIncrement floatValue]);
                if (modulo == 0) {
                    return YES;
                } else {
                    return NO;
                }
            } else {
                return YES;
            }
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
    //[self performSegueWithIdentifier:@"Show Planned Course Detail" sender:plannedSection];
    
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        
        UISplitViewController *split = [self.registrationTabController childViewControllers][1];
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
            [_detailSelectionDelegate selectedDetail:plannedSection withModule:self.module];
        }
    }
    else if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
                [self performSegueWithIdentifier:@"Show Section Detail" sender:plannedSection];
    }
    
    
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:@"Show Section Detail"]) {
        RegistrationPlannedSection *courseSection = sender;
        RegistrationPlannedSectionDetailViewController *detailController = [segue destinationViewController];
        detailController.registrationPlannedSection = courseSection;
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
