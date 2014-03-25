//
//  RegistrationTabBarController.m
//  Mobile
//
//  Created by jkh on 1/8/14.
//  Copyright (c) 2014 Ellucian. All rights reserved.
//

#import "RegistrationTabBarController.h"
#import "RegistrationSearchViewController.h"
#import "UIViewController+SlidingViewExtension.h"
#import "AppearanceChanger.h"
#import "RegistrationCartViewController.h"
#import "MBProgressHUD.h"
#import "NSData+AuthenticatedRequest.h"
#import "Module+Attributes.h"
#import "CurrentUser.h"
#import "LoginViewController.h"
#import "RegistrationTerm.h"
#import "RegistrationPlannedSection.h"
#import "RegistrationPlannedSectionMeetingPattern.h"
#import "RegistrationPlannedSectionInstructor.h"
#import "NSMutableURLRequest+BasicAuthentication.h"

@interface RegistrationTabBarController ()

@property (nonatomic, strong) NSDateFormatter *dateFormatter;
@property (nonatomic, strong) NSDateFormatter *timeFormatter;

@property (nonatomic, strong) NSArray *plannedSections;
@property (nonatomic, strong) NSMutableArray *searchedSections;

@end

@implementation RegistrationTabBarController

-(void)viewDidLoad
{
    [super viewDidLoad];
    if(SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"7.0")) {
        UITabBarItem *tabBarItem0 = self.tabBar.items[0];
        tabBarItem0.selectedImage = [UIImage imageNamed:@"Registration Cart Selected"];
        UITabBarItem *tabBarItem2 = self.tabBar.items[2];
        tabBarItem2.selectedImage = [UIImage imageNamed:@"Registration Registered Tab Image Selected"];
    }
    
    self.searchedSections = [NSMutableArray new];
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    hud.labelText = NSLocalizedString(@"Retrieving terms", @"loading message while fetching terms to use for search");
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 0.01 * NSEC_PER_SEC);
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
        [self fetchTerms];
        [MBProgressHUD hideHUDForView:self.view animated:YES];
        
        MBProgressHUD *hud2 = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
        hud2.labelText = NSLocalizedString(@"Checking Eligibility", @"checking registration eligibility message");
        dispatch_after(popTime, dispatch_get_main_queue(), ^(void){

            self.registrationAllowed = [self checkRegistrationEligibility];
            [MBProgressHUD hideHUDForView:self.view animated:YES];
    
            MBProgressHUD *hud3 = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
            hud3.labelText = NSLocalizedString(@"Fetching Registration Plan", @"fetching registration plan message");

            dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
                [self fetchRegistrationPlans];
                [MBProgressHUD hideHUDForView:self.view animated:YES];
                if([self itemsInCartCount] == 0 && !self.ineligibleMessage) {
                    self.selectedIndex = 1;
                }
                
                if(self.ineligibleMessage) {
                    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Ineligible for Registration", @"Ineligible for Registration") message:self.ineligibleMessage delegate:self cancelButtonTitle:NSLocalizedString(@"OK", @"OK") otherButtonTitles:nil];
                    [alert show];
                }

            });
        });
    });
}

- (IBAction)revealMenu:(id)sender
{
    if(UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        UISplitViewController *splitController = self.viewControllers[1];
        UINavigationController *navigationController = splitController.viewControllers[0];
        RegistrationSearchViewController *searchController = (RegistrationSearchViewController *)navigationController.childViewControllers[0];
        [searchController.termTextField resignFirstResponder];
        [searchController.searchTextField resignFirstResponder];
    } else {
        UINavigationController *navigationController = self.viewControllers[1];
        RegistrationSearchViewController *searchController = (RegistrationSearchViewController *)navigationController.childViewControllers[0];
        [searchController.termTextField resignFirstResponder];
        [searchController.searchTextField resignFirstResponder];
    }
    [super revealMenu:sender];
}

-(BOOL) checkRegistrationEligibility
{
    NSError *error;
    NSURLResponse *response;
    [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
    NSString *urlString = [NSString stringWithFormat:@"%@/%@/eligibility", [self.module propertyForKey:@"registration"], [[CurrentUser userid]  stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding]];
    NSData *responseData = [NSData dataWithContentsOfURLUsingCurrentUser:[NSURL URLWithString:urlString] returningResponse:&response error:&error];
    [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
    
    if(responseData)
    {
        NSDictionary* json = [NSJSONSerialization
                              JSONObjectWithData:responseData
                              options:kNilOptions
                              error:&error];
        BOOL eligible = [[json objectForKey:@"eligible"] boolValue];

        if(eligible)
        {
            self.ineligibleMessage = nil;
        } else {
            NSMutableArray *messagesArray = [NSMutableArray new];
            for(NSDictionary *dict in [json objectForKey:@"messages"]) {
                [messagesArray addObject:[dict objectForKey:@"message"]];
            }
            self.ineligibleMessage = [messagesArray componentsJoinedByString:@"\n"];
        }
        return eligible;
        
    }
    else return NO;
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

- (void)fetchRegistrationPlans
{
    NSError *error;
    NSURLResponse *response;
    [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
    NSString *urlString = [NSString stringWithFormat:@"%@/%@/plans", [self.module propertyForKey:@"registration"], [[CurrentUser userid]  stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding]];
    NSData *responseData = [NSData dataWithContentsOfURLUsingCurrentUser:[NSURL URLWithString:urlString] returningResponse:&response error:&error];
    [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
    
    if(responseData)
    {
        NSDictionary* json = [NSJSONSerialization
                              JSONObjectWithData:responseData
                              options:kNilOptions
                              error:&error];
        
        NSMutableArray *plannedSections = [NSMutableArray new];
        for(NSDictionary *planJson in [json objectForKey:@"plans"]) {
            self.planId = [planJson objectForKey:@"planId"];
            for(NSDictionary *termJson in [planJson objectForKey:@"terms"]) {
                NSString *termId = [termJson objectForKey:@"termId"];
                for(NSDictionary *plannedSectionJson in [termJson objectForKey:@"plannedCourses"]) {
                    RegistrationPlannedSection *plannedSection = [RegistrationPlannedSection new];
                    plannedSection.sectionId = [plannedSectionJson objectForKey:@"sectionId"];
                    plannedSection.courseId = [plannedSectionJson objectForKey:@"courseId"];
                    plannedSection.courseName = [plannedSectionJson objectForKey:@"courseName"];
                    plannedSection.courseSectionNumber = [plannedSectionJson objectForKey:@"courseSectionNumber"];
                    plannedSection.sectionTitle = [plannedSectionJson objectForKey:@"sectionTitle"];
                    plannedSection.courseDescription = [plannedSectionJson objectForKey:@"courseDescription"];
                    if([plannedSectionJson objectForKey:@"credits"] != [NSNull null]) {
                        plannedSection.credits = [plannedSectionJson objectForKey:@"credits"];
                    }
                    if([plannedSectionJson objectForKey:@"minimumCredits"] != [NSNull null]) {
                        plannedSection.minimumCredits = [plannedSectionJson objectForKey:@"minimumCredits"];
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
                    plannedSection.termId = termId;
                    plannedSection.classification = [plannedSectionJson objectForKey:@"classification"];
                    
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
                        //mp.frequency = [meetingPatternJson objectForKey:@"frequency"];
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
        }
        self.plannedSections = [plannedSections copy];
            
        [[NSNotificationCenter defaultCenter] postNotificationName:kRegistrationPlanDataReloaded object:nil];
    }
}

-(int) itemsInCartCount
{
    int count = 0;
    for(RegistrationTerm *term in self.terms) {
        NSArray *plannedSections = [self sectionsInCart:term.termId];
        count += [plannedSections count];
    }
    return count;
}


-(void) addSearchedSection:(RegistrationPlannedSection *)section
{
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"sectionId == %@", section.sectionId];
    NSArray *results = [[self sectionsInCart:section.termId ] filteredArrayUsingPredicate:predicate];
    if([results count] == 0) {
        [self.searchedSections addObject:section];
    }
    [[NSNotificationCenter defaultCenter] postNotificationName:kRegistrationPlanDataReloaded object:nil];
}


-(void) fetchTerms
{
    NSError *error;
    NSURLResponse *response;
    [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
    NSString *urlString = [NSString stringWithFormat:@"%@/%@/terms", [self.module propertyForKey:@"registration"], [[CurrentUser userid]  stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding]];
    NSData *responseData = [NSData dataWithContentsOfURLUsingCurrentUser:[NSURL URLWithString:urlString] returningResponse:&response error:&error];
    [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
    
    if(responseData)
    {
        NSDictionary *termsJson = [NSJSONSerialization
                                   JSONObjectWithData:responseData
                                   options:kNilOptions
                                   error:&error];
        NSMutableArray *terms = [NSMutableArray new];
        for(NSDictionary *termJson in [termsJson objectForKey:@"terms"]) {
            RegistrationTerm *term = [RegistrationTerm new];
            [terms addObject:term];
            NSString *termId = [termJson objectForKey:@"id"];
            term.termId = termId;
            term.name = [termJson objectForKey:@"name"];
            term.startDate = [termJson objectForKey:@"startDate"];
            term.endDate = [termJson objectForKey:@"endDate"];
        }
        self.terms = [terms copy];
    }
}

-(NSString *) termName:(NSString *)termId
{
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"termId == %@", termId];
    NSArray *results = [self.terms filteredArrayUsingPredicate:predicate];
    RegistrationTerm *term = results[0];
    return term.name;
}

-(NSArray *) sectionsInCart:(NSString *) termId
{
    NSArray *plannedSections = [self plannedSections:termId];
    NSArray *searchedSections = [self searchedSections:termId];
    NSMutableArray *results = [NSMutableArray new];
    [results addObjectsFromArray:plannedSections];
    [results addObjectsFromArray:searchedSections];
    return results;
}

-(NSArray *) searchedSections:(NSString *) termId
{
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"termId == %@", termId];
    NSArray *results = [self.searchedSections filteredArrayUsingPredicate:predicate];
    return results;
}

-(NSArray *) plannedSections:(NSString *) termId
{
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"termId == %@ AND classification != %@", termId, @"registered"];
    NSArray *results = [self.plannedSections filteredArrayUsingPredicate:predicate];
    return results;
}

-(NSArray *) registeredSections:(NSString *) termId
{
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"termId == %@ AND classification == %@", termId, @"registered"];
    NSArray *results = [self.plannedSections filteredArrayUsingPredicate:predicate];
    return results;
}

-(void) removeSearchedSection:(NSString *)sectionId term:(NSString *)termId
{
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"termId == %@ AND sectionId == %@", termId, sectionId];
    NSArray *results = [self.searchedSections filteredArrayUsingPredicate:predicate];
    for(id section in results) {
        [self.searchedSections removeObject:section];
    }
    [[NSNotificationCenter defaultCenter] postNotificationName:kRegistrationPlanDataReloaded object:nil];
}
@end
