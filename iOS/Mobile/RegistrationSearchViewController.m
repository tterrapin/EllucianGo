//
//  RegistrationSearchViewController.m
//  Mobile
//
//  Created by jkh on 1/7/14.
//  Copyright (c) 2014 Ellucian. All rights reserved.
//

#import "RegistrationSearchViewController.h"
#import "NSData+AuthenticatedRequest.h"
#import "Module+Attributes.h"
#import "CurrentUser.h"
#import "MBProgressHUD.h"
#import "RegistrationPlannedSection.h"
#import "RegistrationPlannedSectionMeetingPattern.h"
#import "RegistrationPlannedSectionInstructor.h"

#import "UIViewController+SlidingViewExtension.h"
#import "UIColor+SchoolCustomization.h"
#import "RegistrationSearchResultsViewController.h"
#import "RegistrationTabBarController.h"
#import "RegistrationTerm.h"
#import "Module.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"

@interface RegistrationSearchViewController ()

@property (nonatomic, strong) UIPickerView *termsPickerView;
@property (nonatomic, strong) UIToolbar *pickerToolbar;
@property (nonatomic, strong) NSString *selectedTermId;
@property (nonatomic, strong) NSDateFormatter *dateFormatter;
@property (nonatomic, strong) NSDateFormatter *timeFormatter;

@end

@implementation RegistrationSearchViewController


-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Registration Search" forModuleNamed:self.module.name];
}

-(void) viewDidLoad
{
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor accentColor];
    self.searchTextField.delegate = self;
    
    self.termTextField.leftView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"Registration Search Term Picker Image"]];
    self.termTextField.leftViewMode = UITextFieldViewModeAlways;
    self.searchTextField.leftView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"Registration Search Keywords Image"]];
    self.searchTextField.leftViewMode = UITextFieldViewModeAlways;
    self.termTextField.selectedTextRange = nil;
    //iOS 7 hack - concern about showing the cursor in a field that could not be typed in.
    if([self.termTextField respondsToSelector:@selector(setTintColor:)]) {
        self.termTextField.tintColor = [UIColor whiteColor];
    }
}

- (void)viewWillAppear:(BOOL)animated
{
    
    [super viewWillAppear:animated];
    
    self.navigationItem.title = [self.module name];
    
    self.termsPickerView = [[UIPickerView alloc] initWithFrame:CGRectMake(0, 43, 320, 480)];
    self.termsPickerView.delegate = self;
    self.termsPickerView.dataSource = self;
    [self.termsPickerView setShowsSelectionIndicator:YES];
    self.termsPickerView.backgroundColor = [UIColor whiteColor];
    self.termTextField.inputView = self.termsPickerView ;
    self.termTextField.delegate = self;
    
    self.pickerToolbar = [[UIToolbar alloc] initWithFrame:CGRectMake(0, 0, 320, 56)];
    self.pickerToolbar.barStyle = UIBarStyleBlackOpaque;
    [self.pickerToolbar sizeToFit];
    
    NSMutableArray *barItems = [[NSMutableArray alloc] init];
    UIBarButtonItem *flexSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:self action:nil];
    [barItems addObject:flexSpace];
    UIBarButtonItem *doneBtn = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(pickerDoneClicked)];
    [barItems addObject:doneBtn];
    [self.pickerToolbar setItems:barItems animated:YES];
    
    self.termTextField.inputAccessoryView = self.pickerToolbar;
}


-(void)pickerDoneClicked
{
  	[self.termTextField resignFirstResponder];
}

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView
{
    return 1;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component
{
    return [self.terms count];
}

- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    RegistrationTerm *term = [self.terms objectAtIndex:row];
    return term.name;
}

- (void) pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
    RegistrationTerm *term = [self.terms objectAtIndex:row];
    self.termTextField.text = term.name;
    self.termTextField.selectedTextRange = nil;
    self.selectedTermId = term.termId;
    [self updateSearchButton: pickerView];
}

-(NSDateFormatter *)dateFormatter
{
    if(_dateFormatter == nil) {
        _dateFormatter = [[NSDateFormatter alloc] init];
        [_dateFormatter setDateFormat:@"yyyy-MM-dd"];
        [_dateFormatter setTimeZone:[NSTimeZone timeZoneWithName:@"UTC"]];
    }
    return _dateFormatter;
}

-(NSDateFormatter *)timeFormatter
{
    if(_timeFormatter == nil) {
        _timeFormatter = [[NSDateFormatter alloc] init];
        [_timeFormatter setDateFormat:@"HH:mm'Z'"];
        [_timeFormatter setTimeZone:[NSTimeZone timeZoneWithName:@"UTC"]];
    }
    return _timeFormatter;
}

- (IBAction)search:(id)sender
{
    [self.searchTextField resignFirstResponder];
    MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    hud.labelText = NSLocalizedString(@"Searching", @"searching message");
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 0.01 * NSEC_PER_SEC);
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
        NSMutableArray *searchCourses = [self searchForCourses];
        [MBProgressHUD hideHUDForView:self.view animated:YES];
        [self performSegueWithIdentifier:@"Search" sender:searchCourses];
    });
    
}

-(NSMutableArray *) searchForCourses
{
    NSError *error;
    NSURLResponse *response;
    [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
    NSString *urlString = [NSString stringWithFormat:@"%@/%@/search-courses?pattern=%@&term=%@", [self.module propertyForKey:@"registration"], [[CurrentUser userid]  stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding], [self.searchTextField.text stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding], [self.selectedTermId stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding]];
    NSData *responseData = [NSData dataWithContentsOfURLUsingCurrentUser:[NSURL URLWithString:urlString] returningResponse:&response error:&error];
    [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
    
    NSMutableArray *plannedSections = [NSMutableArray new];
    if(responseData)
    {
        NSDictionary* json = [NSJSONSerialization
                              JSONObjectWithData:responseData
                              options:kNilOptions
                              error:&error];
        
        
        for(NSDictionary *plannedSectionJson in [json objectForKey:@"sections"]) {
            RegistrationPlannedSection *plannedSection = [RegistrationPlannedSection new];
            plannedSection.termId = [plannedSectionJson objectForKey:@"termId"];
            plannedSection.sectionId = [plannedSectionJson objectForKey:@"sectionId"];
            plannedSection.courseId = [plannedSectionJson objectForKey:@"courseId"];
            plannedSection.courseName = [plannedSectionJson objectForKey:@"courseName"];
            plannedSection.courseSectionNumber = [plannedSectionJson objectForKey:@"courseSectionNumber"];
            plannedSection.sectionTitle = [plannedSectionJson objectForKey:@"sectionTitle"];
            plannedSection.courseDescription = [plannedSectionJson objectForKey:@"courseDescription"];
            
            //use minimumCredits
//            if([plannedSectionJson objectForKey:@"credits"] != [NSNull null]) {
//                plannedSection.credits = [plannedSectionJson objectForKey:@"credits"];
//            }
            if([plannedSectionJson objectForKey:@"minimumCredits"] != [NSNull null]) {
                plannedSection.minimumCredits = [plannedSectionJson objectForKey:@"minimumCredits"];
                plannedSection.credits = plannedSection.minimumCredits;
            }
            if([plannedSectionJson objectForKey:@"maximumCredits"] != [NSNull null]) {
                plannedSection.maximumCredits = [plannedSectionJson objectForKey:@"maximumCredits"];
            }
            if([plannedSectionJson objectForKey:@"variableCreditIncrement"] != [NSNull null]) {
                plannedSection.variableCreditIncrement = [plannedSectionJson objectForKey:@"variableCreditIncrement"];
            }
            if([plannedSectionJson objectForKey:@"ceus"] != [NSNull null]) {
                plannedSection.ceus = [plannedSectionJson objectForKey:@"ceus"];
            }
            plannedSection.status = [plannedSectionJson objectForKey:@"status"];
            plannedSection.gradingType = [plannedSectionJson objectForKey:@"gradingType"];
//            plannedCourse.classification = [plannedCourseJson objectForKey:@"classification"];
            
            NSMutableArray *meetingPatterns = [NSMutableArray new];
            for(NSDictionary *meetingPatternJson in [plannedSectionJson objectForKey:@"meetingPatterns"]) {
                RegistrationPlannedSectionMeetingPattern *meetingPattern = [RegistrationPlannedSectionMeetingPattern new];
                meetingPattern.instructionalMethodCode = [meetingPatternJson objectForKey:@"instructionalMethodCode"];
                meetingPattern.startDate =  [self.dateFormatter dateFromString:[meetingPatternJson objectForKey:@"startDate"]];
                meetingPattern.endDate =  [self.dateFormatter dateFromString:[meetingPatternJson objectForKey:@"endDate"]];
                meetingPattern.startTime =  [self.timeFormatter dateFromString:[meetingPatternJson objectForKey:@"startTime"]];
                meetingPattern.endTime =  [self.timeFormatter dateFromString:[meetingPatternJson objectForKey:@"endTime"]];
                meetingPattern.daysOfWeek = [meetingPatternJson objectForKey:@"daysOfWeek"];
                if([meetingPatternJson objectForKey:@"room"] != [NSNull null]) {
                    meetingPattern.room = [meetingPatternJson objectForKey:@"room"];
                }
                if([meetingPatternJson objectForKey:@"building"] != [NSNull null]) {
                    meetingPattern.building = [meetingPatternJson objectForKey:@"building"];
                }
                if([meetingPatternJson objectForKey:@"buildingId"] != [NSNull null]) {
                    meetingPattern.buildingId = [meetingPatternJson objectForKey:@"buildingId"];
                }
                if([meetingPatternJson objectForKey:@"campusId"] != [NSNull null]) {
                    meetingPattern.campusId = [meetingPatternJson objectForKey:@"campusId"];
                }
                [meetingPatterns addObject:meetingPattern];
            };
            plannedSection.meetingPatterns = [meetingPatterns copy];
            
            NSMutableArray *instructors = [NSMutableArray new];
            for(NSDictionary *instructorJson in [plannedSectionJson objectForKey:@"instructors"]) {
                RegistrationPlannedSectionInstructor *instructor = [RegistrationPlannedSectionInstructor new];
                instructor.firstName = [instructorJson objectForKey:@"firstName"];
                instructor.lastName = [instructorJson objectForKey:@"lastName"];
                instructor.middleInitial = [instructorJson objectForKey:@"middleInitial"];
                instructor.instructorId = [instructorJson objectForKey:@"instructorId"];
                instructor.primary = [instructorJson objectForKey:@"primary"];
                instructor.formattedName = [instructorJson objectForKey:@"formattedName"];
                [instructors addObject:instructor];
            };
            plannedSection.instructors = [instructors copy];
            [plannedSections addObject:plannedSection];
        }
    }
    return plannedSections;
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range     replacementString:(NSString *)string
{
    if([textField isEqual:self.termTextField])
        return NO;
    return YES;
}

- (BOOL)textFieldShouldBeginEditing:(UITextField*)textField
{
    if([textField isEqual:self.termTextField])
    {
        if(!self.selectedTermId) {
            //iOS 6 hack - the placeholder text was shown while the picker was open.
            self.termTextField.placeholder = nil;
            [self pickerView:self.termsPickerView didSelectRow:0 inComponent:0];
            self.termTextField.selectedTextRange = nil;
        }
    }
    return YES;
}

-(BOOL)splitViewController:(UISplitViewController *)svc shouldHideViewController:(UIViewController *)vc inOrientation:(UIInterfaceOrientation)orientation
{
    return NO;
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    
    return YES;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    [self.navigationController setToolbarHidden:YES animated:YES];
    if ([[segue identifier] isEqualToString:@"Show Section Detail"]) {
        
    } else if ([[segue identifier] isEqualToString:@"Search"]) {
        id detailController = [segue destinationViewController];
        if([detailController isKindOfClass:[UINavigationController class]]) {
            detailController = ((UINavigationController *)detailController).childViewControllers[0];
        }
        
        RegistrationSearchResultsViewController *resultsViewController = (RegistrationSearchResultsViewController *)detailController;
        resultsViewController.courses  = sender;
    }
}

- (IBAction)updateSearchButton:(id)sender
{
    if(self.selectedTermId && [self.searchTextField.text length] > 0)
    {
        self.searchButton.enabled = YES;
    } else {
        self.searchButton.enabled = NO;
        
    }
}

-(RegistrationTabBarController *) registrationTabController
{
    return  (RegistrationTabBarController *)[self tabBarController];
}

-(NSArray *)terms
{
    return self.registrationTabController.terms;
}


@end
