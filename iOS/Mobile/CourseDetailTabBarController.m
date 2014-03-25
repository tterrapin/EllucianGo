//
//  CourseDetailTabBarController.m
//  Mobile
//
//  Created by jkh on 2/11/13.
//  Copyright (c) 2013-2014 Ellucian. All rights reserved.
//

#import "CourseDetailTabBarController.h"
#import "Module+Attributes.h"
#import "CurrentUser.h"
#import "AuthenticatedRequest.h"
#import "CourseDetail.h"
#import "CourseDetailInstructor.h"
#import "CourseMeetingPattern.h"
#import "UIColor+SchoolCustomization.h"

@interface CourseDetailTabBarController ()

@property (nonatomic, strong) NSDateFormatter *dateFormatter;
@property (nonatomic, strong) NSDateFormatter *timeFormatter;
@property (nonatomic, strong) NSArray *originalViewControllers;

@end

@implementation CourseDetailTabBarController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    UINavigationController *navBarController = self.moreNavigationController;
    navBarController.navigationBar.translucent = NO;
    if([self.searchDisplayController.searchBar respondsToSelector:@selector(setBarTintColor:)]) {
        navBarController.navigationBar.barTintColor = [UIColor primaryColor];
    }
    
    if([self.tabBar respondsToSelector:@selector(setTranslucent:)]) {
        self.tabBar.translucent = NO;
    }
    
    self.originalViewControllers = [[NSArray alloc] initWithArray:self.viewControllers];
    [self renderTabs];

    if([CurrentUser sharedInstance].isLoggedIn) {
        [self fetchCourseDetail:self];
    }
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(fetchCourseDetail:) name:kLoginExecutorSuccess object:nil];
}

-(void)renderTabs
{
    NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"CourseDetail"];
    request.predicate = [NSPredicate predicateWithFormat:@"termId == %@ && sectionId == %@", self.termId, self.sectionId];
    
    NSError *error;
    CourseDetail *courseDetail = [[self.module.managedObjectContext executeFetchRequest:request error:&error] lastObject];
    NSMutableArray *tempArray = [[NSMutableArray alloc] init ];
    [tempArray addObject:[self.originalViewControllers objectAtIndex:0]];
    [tempArray addObject:[self.originalViewControllers objectAtIndex:1]];
    
    NSString *rosterVisible = [self.module propertyForKey:@"visible"];
    //for backwards compatible to the way data was stored before 3.0.
    if(!rosterVisible) {
        rosterVisible = [self.module propertyForKey:@"rosterVisible"];
    }
    if(!( [rosterVisible isEqualToString:@"none"] || ([rosterVisible isEqualToString:@"faculty"] && !self.isInstructor) )) {
        [tempArray addObject:[self.originalViewControllers objectAtIndex:2]];
    }
    
//    // assignments, announcements, events
//    // no learningProvider = none
//    // sharepoint = announcements, events
//    // other (moodle, bb) = assignments, announcements, events
//    if (courseDetail.learningProvider != NULL && ![[courseDetail.learningProvider uppercaseString] isEqualToString:@"SHAREPOINT"]) {
//        [tempArray addObject:[originalViewControllers objectAtIndex:3]];
//    }
//    if (courseDetail.learningProvider != NULL) { // and is SHAREPOINT
//        [tempArray addObject:[originalViewControllers objectAtIndex:4]];
//        [tempArray addObject:[originalViewControllers objectAtIndex:5]];
//    }
    
    if ([self.module propertyForKey:@"ilp"] && courseDetail.learningProvider && ![[courseDetail.learningProvider uppercaseString] isEqualToString:@"SHAREPOINT"])
    {
        [tempArray addObject:[self.originalViewControllers objectAtIndex:3]];
        [tempArray addObject:[self.originalViewControllers objectAtIndex:4]];
        [tempArray addObject:[self.originalViewControllers objectAtIndex:5]];

    }
    self.viewControllers = [tempArray copy];
    self.customizableViewControllers = nil;
    
    UINavigationController *navBarController = self.moreNavigationController;
    navBarController.navigationBar.translucent = NO;
    if([self.searchDisplayController.searchBar respondsToSelector:@selector(setBarTintColor:)]) {
        navBarController.navigationBar.barTintColor = [UIColor primaryColor];
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

-(NSDateFormatter *)timeFormatter
{
    if(_timeFormatter == nil) {
        _timeFormatter = [[NSDateFormatter alloc] init];
        [_timeFormatter setDateFormat:@"HH:mm'Z'"];
        [_timeFormatter setTimeZone:[NSTimeZone timeZoneWithName:@"UTC"]];
    }
    return _timeFormatter;
}

- (NSDateFormatter *)tzTimeFormatter:(NSString*)timeZone
{
    NSDateFormatter *tzTimeFormatter = [[NSDateFormatter alloc] init];
    [tzTimeFormatter setDateFormat:@"HH:mm'Z'"];
    [tzTimeFormatter setTimeZone:[NSTimeZone timeZoneWithName:timeZone]];
    return tzTimeFormatter;
}

-(void) fetchCourseDetail:(id)sender
{
    NSManagedObjectContext *importContext = [[NSManagedObjectContext alloc] initWithConcurrencyType:NSPrivateQueueConcurrencyType];
    importContext.parentContext = self.module.managedObjectContext;
    NSString *urlString = [NSString stringWithFormat:@"%@/%@?term=%@&section=%@", [self.module propertyForKey:@"overview"], [[[CurrentUser sharedInstance] userid]  stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding], [self.termId stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding] ,[self.sectionId stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding]];
    [importContext performBlock: ^{
        
        NSError *error;
        [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
        AuthenticatedRequest *authenticatedRequet = [AuthenticatedRequest new];
        NSData *responseData = [authenticatedRequet requestURL:[NSURL URLWithString:urlString] fromView:self];
        
        [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
        if(responseData) {
            NSArray* json = [NSJSONSerialization
                             JSONObjectWithData:responseData
                             options:kNilOptions
                             error:&error];
            NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"CourseDetail"];
            request.predicate = [NSPredicate predicateWithFormat:@"termId == %@ && sectionId == %@", self.termId, self.sectionId];
            
            NSArray * oldObjects = [importContext executeFetchRequest:request error:&error];
            for (CourseDetail* oldObject in oldObjects) {
                [importContext deleteObject:oldObject];
            }

            id value = [json valueForKey:@"terms"];
            if(value != [NSNull null]) {
                for(NSDictionary *jsonTerms in [json valueForKey:@"terms"]) {
                    for(NSDictionary *jsonSections in [jsonTerms valueForKey:@"sections"]) {
                        CourseDetail *courseDetail = [NSEntityDescription insertNewObjectForEntityForName:@"CourseDetail" inManagedObjectContext:importContext];
                        courseDetail.termId = self.termId;
                        courseDetail.sectionId = [jsonSections objectForKey:@"sectionId"];
                        if([jsonSections objectForKey:@"sectionTitle"] != [NSNull null]) {
                            courseDetail.sectionTitle = [jsonSections objectForKey:@"sectionTitle"];
                        }
                        if([jsonSections objectForKey:@"courseName"] != [NSNull null]) {
                            courseDetail.courseName = [jsonSections objectForKey:@"courseName"];
                        }
                        if([jsonSections objectForKey:@"courseDescription"] != [NSNull null]) {
                            courseDetail.courseDescription = [jsonSections objectForKey:@"courseDescription"];
                        }
                        if([jsonSections objectForKey:@"courseSectionNumber"] != [NSNull null]) {
                            courseDetail.courseSectionNumber = [jsonSections objectForKey:@"courseSectionNumber"];
                        }
                        if([jsonSections objectForKey:@"firstMeetingDate"] != [NSNull null]) {
                            courseDetail.firstMeetingDate = [self.dateFormatter dateFromString:[jsonSections objectForKey:@"firstMeetingDate"]];
                        }
                        if([jsonSections objectForKey:@"lastMeetingDate"] != [NSNull null]) {
                            courseDetail.lastMeetingDate = [self.dateFormatter dateFromString:[jsonSections objectForKey:@"lastMeetingDate"]];
                        }
                        //                    if([jsonSections objectForKey:@"credits"] != [NSNull null]) {
                        //                        courseDetail.credits = [jsonSections objectForKey:@"credits"];
                        //                    }
                        //                    if([jsonSections objectForKey:@"ceus"] != [NSNull null]) {
                        //                         courseDetail.ceus = [jsonSections objectForKey:@"ceus"];
                        //                    }
                        for(NSDictionary *instructorJson in [jsonSections objectForKey:@"instructors"]) {
                            CourseDetailInstructor *instructor = [NSEntityDescription insertNewObjectForEntityForName:@"CourseDetailInstructor" inManagedObjectContext:importContext];
                            if([instructorJson objectForKey:@"firstName"] != [NSNull null]) {
                                instructor.firstName = [instructorJson objectForKey:@"firstName"];
                            }
                            if([instructorJson objectForKey:@"lastName"] != [NSNull null]) {
                                instructor.lastName = [instructorJson objectForKey:@"lastName"];
                            }
                            if([instructorJson objectForKey:@"middleInitial"] != [NSNull null]) {
                                instructor.middleInitial = [instructorJson objectForKey:@"middleInitial"];
                            }
                            instructor.instructorId = [instructorJson objectForKey:@"instructorId"];
                            instructor.primary = [NSNumber numberWithBool:[[instructorJson objectForKey:@"primary"] boolValue]];
                            instructor.formattedName = [instructorJson objectForKey:@"formattedName"];
                            instructor.course = courseDetail;
                            [courseDetail addInstructorsObject:instructor];
                            
                        }
                        if([jsonSections objectForKey:@"learningProvider"] != [NSNull null]) {
                            courseDetail.learningProvider = [jsonSections objectForKey:@"learningProvider"];
                        }
                        if([jsonSections objectForKey:@"learningProviderSiteId"] != [NSNull null]) {
                            courseDetail.learningProviderSiteId = [jsonSections objectForKey:@"learningProviderSiteId"];
                        }
                        if([jsonSections objectForKey:@"primarySectionId"] != [NSNull null]) {
                            courseDetail.primarySectionId = [jsonSections objectForKey:@"primarySectionId"];
                        }
                        for(NSDictionary *meetingPatternJson in [jsonSections objectForKey:@"meetingPatterns"]) {
                            CourseMeetingPattern *mp = [NSEntityDescription insertNewObjectForEntityForName:@"CourseMeetingPattern" inManagedObjectContext:importContext];
                            mp.instructionalMethodCode = [meetingPatternJson objectForKey:@"instructionalMethodCode"];
                            mp.startDate =  [self.dateFormatter dateFromString:[meetingPatternJson objectForKey:@"startDate"]];
                            mp.endDate =  [self.dateFormatter dateFromString:[meetingPatternJson objectForKey:@"endDate"]];
                            
                            //check if it exists and if its value is not null
                            if([meetingPatternJson objectForKey:@"startTime"] && [meetingPatternJson objectForKey:@"startTime"] != [NSNull null]) {
                                mp.startTime =  [self.timeFormatter dateFromString:[meetingPatternJson objectForKey:@"startTime"]];
                            }
                            if([meetingPatternJson objectForKey:@"endTime"] && [meetingPatternJson objectForKey:@"endTime"] != [NSNull null]) {
                                mp.endTime =  [self.timeFormatter dateFromString:[meetingPatternJson objectForKey:@"endTime"]];
                            }
                            
                            if([meetingPatternJson objectForKey:@"sisStartTimeWTz"] && [meetingPatternJson objectForKey:@"sisStartTimeWTz"] != [NSNull null]) {
                                
                                NSString * sisStartTimeWTZ = [meetingPatternJson objectForKey:@"sisStartTimeWTz"];
                                NSArray  * startTimeComplex = [sisStartTimeWTZ componentsSeparatedByString:@" "];
                                if ( [startTimeComplex count] == 2 ) {
                                    NSDateFormatter *tzTimeFormatter = [self tzTimeFormatter:startTimeComplex[1]];
                                    mp.startTime = [tzTimeFormatter dateFromString:startTimeComplex[0]];
                                }
                            }
                            
                            if([meetingPatternJson objectForKey:@"sisEndTimeWTz"] && [meetingPatternJson objectForKey:@"sisEndTimeWTz"] != [NSNull null]) {
                                
                                NSString * sisEndTimeWTZ = [meetingPatternJson objectForKey:@"sisEndTimeWTz"];
                                NSArray  * endTimeComplex = [sisEndTimeWTZ componentsSeparatedByString:@" "];
                                if ( [endTimeComplex count] == 2 ) {
                                    NSDateFormatter *tzTimeFormatter = [self tzTimeFormatter:endTimeComplex[1]];
                                    mp.endTime = [tzTimeFormatter dateFromString:endTimeComplex[0]];
                                }
                            }
                            
                            NSArray *daysOfWeek = [meetingPatternJson objectForKey:@"daysOfWeek"];
                            {
                                mp.daysOfWeek = [daysOfWeek componentsJoinedByString:@","];
                            }
                            if([meetingPatternJson objectForKey:@"room"] != [NSNull null]) {
                                mp.room = [meetingPatternJson objectForKey:@"room"];
                            }
                            //mp.frequency = [meetingPatternJson objectForKey:@"frequency"];
                            if([meetingPatternJson objectForKey:@"building"] != [NSNull null]) {
                                mp.building = [meetingPatternJson objectForKey:@"building"];
                            }
                            if([meetingPatternJson objectForKey:@"buildingId"] != [NSNull null]) {
                                mp.buildingId = [meetingPatternJson objectForKey:@"buildingId"];
                            }
                            if([meetingPatternJson objectForKey:@"campusId"] != [NSNull null]) {
                                mp.campusId = [meetingPatternJson objectForKey:@"campusId"];
                            }
                            mp.course = courseDetail;
                            [courseDetail addMeetingPatternsObject:mp];
                        }
                    }
                }
            }
        }
        
        if (![importContext save:&error]) {
            NSLog(@"Could not save to main context after update to course: %@", [error userInfo]);
        }
        
        [importContext.parentContext performBlock:^{
            NSError *parentError = nil;
            if(![importContext.parentContext save:&parentError]) {
                NSLog(@"Could not save to store after update to course: %@", [error userInfo]);
            }
            
            [self renderTabs];
            
            [[NSNotificationCenter defaultCenter] postNotificationName:kCourseDetailInformationLoaded object:nil];
        }];
    }
     ];
    
    
}


@end
