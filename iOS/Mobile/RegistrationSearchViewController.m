//
//  RegistrationSearchViewController.m
//  Mobile
//
//  Created by jkh on 1/7/14.
//  Copyright (c) 2014 Ellucian. All rights reserved.
//

#import "RegistrationSearchViewController.h"
#import "AuthenticatedRequest.h"
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
    self.navigationController.navigationBar.translucent = NO;
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
    if([self.terms count] > row) {
        RegistrationTerm *term = [self.terms objectAtIndex:row];
        self.termTextField.text = term.name;
        self.termTextField.selectedTextRange = nil;
        self.selectedTermId = term.termId;
        [self updateSearchButton: pickerView];
    }
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

- (NSDateFormatter *)tzTimeFormatter:(NSString*)timeZone
{
    NSDateFormatter *tzTimeFormatter = [[NSDateFormatter alloc] init];
    [tzTimeFormatter setDateFormat:@"HH:mm'Z'"];
    [tzTimeFormatter setTimeZone:[NSTimeZone timeZoneWithName:timeZone]];
    return tzTimeFormatter;
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
    [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
    NSString *urlString = [NSString stringWithFormat:@"%@/%@/search-courses?pattern=%@&term=%@", [self.module propertyForKey:@"registration"], [[[CurrentUser sharedInstance] userid]  stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding], [self.searchTextField.text stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding], [self.selectedTermId stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding]];
    AuthenticatedRequest *authenticatedRequet = [AuthenticatedRequest new];
    NSDictionary *headers = @{@"Accept": @"application/vnd.hedtech.v1+json"};
    NSData *responseData = [authenticatedRequet requestURL:[NSURL URLWithString:urlString] fromView:self addHTTPHeaderFields:headers];

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
            
            if([plannedSectionJson objectForKey:@"termId"] != [NSNull null]) {
                plannedSection.termId = [plannedSectionJson objectForKey:@"termId"];
            }
            if([plannedSectionJson objectForKey:@"sectionId"] != [NSNull null]) {
                plannedSection.sectionId = [plannedSectionJson objectForKey:@"sectionId"];
            }
            if([plannedSectionJson objectForKey:@"courseId"] != [NSNull null]) {
                plannedSection.courseId = [plannedSectionJson objectForKey:@"courseId"];
            }
            if([plannedSectionJson objectForKey:@"courseName"] != [NSNull null]) {
                plannedSection.courseName = [plannedSectionJson objectForKey:@"courseName"];
            }
            if([plannedSectionJson objectForKey:@"courseSectionNumber"] != [NSNull null]) {
                plannedSection.courseSectionNumber = [plannedSectionJson objectForKey:@"courseSectionNumber"];
            }
            if([plannedSectionJson objectForKey:@"sectionTitle"] != [NSNull null]) {
                plannedSection.sectionTitle = [plannedSectionJson objectForKey:@"sectionTitle"];
            }
            if([plannedSectionJson objectForKey:@"courseDescription"] != [NSNull null]) {
                plannedSection.courseDescription = [plannedSectionJson objectForKey:@"courseDescription"];
            }
            if([plannedSectionJson objectForKey:@"firstMeetingDate"] != [NSNull null]) {
                plannedSection.firstMeetingDate = [self.dateFormatter dateFromString:[plannedSectionJson objectForKey:@"firstMeetingDate"]];
            }
            
            if([plannedSectionJson objectForKey:@"lastMeetingDate"] != [NSNull null]) {
                plannedSection.lastMeetingDate = [self.dateFormatter dateFromString:[plannedSectionJson objectForKey:@"lastMeetingDate"]];
            }
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
            if([plannedSectionJson objectForKey:@"variableCreditOperator"] != [NSNull null]) {
                plannedSection.variableCreditOperator = [plannedSectionJson objectForKey:@"variableCreditOperator"];
            }
            if([plannedSectionJson objectForKey:@"ceus"] != [NSNull null]) {
                plannedSection.ceus = [plannedSectionJson objectForKey:@"ceus"];
            }
            if([plannedSectionJson objectForKey:@"status"] != [NSNull null]) {
                plannedSection.status = [plannedSectionJson objectForKey:@"status"];
            }
            //if([plannedSectionJson objectForKey:@"gradingType"] != [NSNull null]) {
            //    plannedSection.gradingType = [plannedSectionJson objectForKey:@"gradingType"];
            //}
            plannedSection.gradingType = @"Graded";
//            plannedCourse.classification = [plannedCourseJson objectForKey:@"classification"];
            
            NSMutableArray *meetingPatterns = [NSMutableArray new];
            for(NSDictionary *meetingPatternJson in [plannedSectionJson objectForKey:@"meetingPatterns"]) {
                RegistrationPlannedSectionMeetingPattern *meetingPattern = [RegistrationPlannedSectionMeetingPattern new];
                
                if([meetingPatternJson objectForKey:@"instructionalMethodCode"] != [NSNull null]) {
                    meetingPattern.instructionalMethodCode = [meetingPatternJson objectForKey:@"instructionalMethodCode"];
                }
                
                if([meetingPatternJson objectForKey:@"startDate"] != [NSNull null]) {
                    meetingPattern.startDate =  [self.dateFormatter dateFromString:[meetingPatternJson objectForKey:@"startDate"]];
                }
                if([meetingPatternJson objectForKey:@"endDate"] != [NSNull null]) {
                    meetingPattern.endDate =  [self.dateFormatter dateFromString:[meetingPatternJson objectForKey:@"endDate"]];
                }
                if([meetingPatternJson objectForKey:@"startTime"] && [meetingPatternJson objectForKey:@"startTime"] != [NSNull null]) {
                    meetingPattern.startTime =  [self.timeFormatter dateFromString:[meetingPatternJson objectForKey:@"startTime"]];
                }
                if([meetingPatternJson objectForKey:@"endTime"] && [meetingPatternJson objectForKey:@"endTime"] != [NSNull null]) {
                    meetingPattern.endTime =  [self.timeFormatter dateFromString:[meetingPatternJson objectForKey:@"endTime"]];
                }
                
                if([meetingPatternJson objectForKey:@"sisStartTimeWTz"] && [meetingPatternJson objectForKey:@"sisStartTimeWTz"] != [NSNull null]) {
                    
                    NSString * sisStartTimeWTZ = [meetingPatternJson objectForKey:@"sisStartTimeWTz"];
                    NSArray  * startTimeComplex = [sisStartTimeWTZ componentsSeparatedByString:@" "];
                    if ( [startTimeComplex count] == 2 ) {
                        NSDateFormatter *tzTimeFormatter = [self tzTimeFormatter:startTimeComplex[1]];
                        meetingPattern.startTime = [tzTimeFormatter dateFromString:startTimeComplex[0]];
                    }
                }
                
                if([meetingPatternJson objectForKey:@"sisEndTimeWTz"] && [meetingPatternJson objectForKey:@"sisEndTimeWTz"] != [NSNull null]) {
                    
                    NSString * sisEndTimeWTZ = [meetingPatternJson objectForKey:@"sisEndTimeWTz"];
                    NSArray  * endTimeComplex = [sisEndTimeWTZ componentsSeparatedByString:@" "];
                    if ( [endTimeComplex count] == 2 ) {
                        NSDateFormatter *tzTimeFormatter = [self tzTimeFormatter:endTimeComplex[1]];
                        meetingPattern.endTime = [tzTimeFormatter dateFromString:endTimeComplex[0]];
                    }
                }
                
                if([meetingPatternJson objectForKey:@"daysOfWeek"] != [NSNull null]) {
                    meetingPattern.daysOfWeek = [meetingPatternJson objectForKey:@"daysOfWeek"];
                }
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
                
                if([instructorJson objectForKey:@"firstName"] != [NSNull null]) {
                    instructor.firstName = [instructorJson objectForKey:@"firstName"];
                }
                if([instructorJson objectForKey:@"lastName"] != [NSNull null]) {
                    instructor.lastName = [instructorJson objectForKey:@"lastName"];
                }
                if([instructorJson objectForKey:@"middleInitial"] != [NSNull null]) {
                    instructor.middleInitial = [instructorJson objectForKey:@"middleInitial"];
                }
                if([instructorJson objectForKey:@"instructorId"] != [NSNull null]) {
                    instructor.instructorId = [instructorJson objectForKey:@"instructorId"];
                }
                if([instructorJson objectForKey:@"primary"] != [NSNull null]) {
                    instructor.primary = [instructorJson objectForKey:@"primary"];
                }
                if([instructorJson objectForKey:@"formattedName"] != [NSNull null]) {
                    instructor.formattedName = [instructorJson objectForKey:@"formattedName"];
                }
                [instructors addObject:instructor];
            };
            plannedSection.instructors = [instructors copy];
            [plannedSections addObject:plannedSection];
        }
    } else {
        [self.registrationTabController reportError:authenticatedRequet.error.localizedDescription];
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
        resultsViewController.allowAddToCart = self.allowAddToCart;
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
