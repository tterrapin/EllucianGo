#import "CoursesCalendarViewController.h"
#import "CalendarViewEvent.h"
#import "CurrentUser.h"
#import "NSData+AuthenticatedRequest.h"
#import "CourseDetailTabBarController.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"

#define DATE_COMPONENTS (NSYearCalendarUnit| NSMonthCalendarUnit | NSDayCalendarUnit | NSWeekCalendarUnit |  NSHourCalendarUnit | NSMinuteCalendarUnit | NSSecondCalendarUnit | NSWeekdayCalendarUnit | NSWeekdayOrdinalCalendarUnit)
#define CURRENT_CALENDAR [NSCalendar currentCalendar]

@interface CoursesCalendarViewController ()

@property (nonatomic,strong) NSDateFormatter *datetimeOutputFormatter;
@property (nonatomic, strong) NSDate *date;
@property (nonatomic, strong) NSMutableDictionary *cachedData;
@end


@implementation CoursesCalendarViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.navigationController.navigationBar.translucent = NO;
    
    self.cachedData = [[NSMutableDictionary alloc] init];
	CalendarViewDayView *dayView = (CalendarViewDayView *) self.view;
	dayView.autoScrollToFirstEvent = YES;
    self.navigationItem.title = self.module.name;
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    [self sendView:@"Schedule (daily view)" forModuleNamed:self.module.name];
    
    CalendarViewDayView *dayView = (CalendarViewDayView *) self.view;
    [dayView reloadData];
}

-(void) reloadScheduleNotification:(NSNotification *)notification
{
    CalendarViewDayView *dayView = (CalendarViewDayView *) self.view;
    [dayView reloadData];
}

- (NSArray *)dayView:(CalendarViewDayView *)dayView eventsForDate:(NSDate *)startDate {
	self.date = startDate;
    
    NSString *userid = [CurrentUser userid];
    
    if(userid) {
        static NSDateFormatter *dateFormatter;
        if(dateFormatter == nil) {
            dateFormatter = [[NSDateFormatter alloc] init];
            [dateFormatter setDateFormat:@"yyyy-MM-dd"];
        }
        
        NSString *dateValue = [dateFormatter stringFromDate:startDate];
        NSArray *cachedValues = [self.cachedData objectForKey:dateValue];
        if(!cachedValues) {
            
            
            static NSDateFormatter *dateFormatterISO8601;
            if(dateFormatterISO8601 == nil) {
                
                dateFormatterISO8601 = [[NSDateFormatter alloc] init];
                [dateFormatterISO8601 setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss'Z'"];
                [dateFormatterISO8601 setTimeZone:[NSTimeZone timeZoneWithName:@"UTC"]];
            }
            
            static NSDateFormatter *timeFormatter;
            if(timeFormatter == nil) {
                timeFormatter = [[NSDateFormatter alloc] init];
                [timeFormatter setDateStyle:NSDateFormatterNoStyle];
                [timeFormatter setTimeStyle:NSDateFormatterShortStyle];
            }
            NSString *urlString = [NSString stringWithFormat:@"%@/%@?start=%@", [self.module propertyForKey:@"daily"], [[CurrentUser userid] stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding], [dateValue  stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding]];
            
            [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
            //        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT,0), ^{
            NSError *error;
            NSURLResponse *response;
            
            NSData *responseData = [NSData dataWithContentsOfURLUsingCurrentUser:[NSURL URLWithString:urlString] returningResponse:&response error:&error];
            
            if(responseData) {
                NSDictionary* json = [NSJSONSerialization
                                      JSONObjectWithData:responseData
                                      options:kNilOptions
                                      error:&error];
                
                NSArray* courseDays = [json objectForKey:@"coursesDays"];
                for(NSDictionary *selectedDateDictionary in courseDays) {
                    NSDate *date = [dateFormatter dateFromString:[selectedDateDictionary objectForKey:@"date"]];
                    
                    NSMutableArray *events = [[NSMutableArray alloc] init];
                    NSString *dateString = [dateFormatter stringFromDate:date];
                    NSArray *courseMeetingsJson = [selectedDateDictionary objectForKey:@"coursesMeetings"];
                    for(NSDictionary *meetingJson in courseMeetingsJson) {
                        
                        CalendarViewEvent *event = [[CalendarViewEvent alloc] init];
                        event.allDay = NO;
                        NSString *sectionTitle = [meetingJson objectForKey:@"sectionTitle"];
                        NSString *courseName = [meetingJson objectForKey:@"courseName"];
                        NSString *courseSectionNumber = [meetingJson objectForKey:@"courseSectionNumber"];
                        event.line1 = [NSString stringWithFormat:@"%@-%@ - %@", courseName, courseSectionNumber, sectionTitle];
                        NSDate *startDate = [dateFormatterISO8601 dateFromString:[meetingJson objectForKey:@"start"]];
                        NSDate *endDate = [dateFormatterISO8601 dateFromString:[meetingJson objectForKey:@"end"]];
                        NSString *startLabel = [timeFormatter stringFromDate:startDate];
                        NSString *endLabel = [timeFormatter stringFromDate:endDate];
                        if([meetingJson objectForKey:@"building"] != [NSNull null] && [meetingJson objectForKey:@"room"] != [NSNull null]) {
                            event.line3 = [NSString stringWithFormat:@"%@ - %@", startLabel, endLabel];
                            event.line2 = [NSString stringWithFormat:NSLocalizedString(@"%@, Room %@", @"label - building name, room number"), [meetingJson objectForKey:@"building"], [meetingJson objectForKey:@"room"]];
                        } else if([meetingJson objectForKey:@"building"] != [NSNull null]) {
                            event.line3 = [NSString stringWithFormat:@"%@ - %@", startLabel, endLabel];
                            event.line2 = [meetingJson objectForKey:@"building"];
                        } else  if([meetingJson objectForKey:@"room"] != [NSNull null]) {
                            event.line3 = [NSString stringWithFormat:@"%@ - %@", startLabel, endLabel];
                            event.line2 = [NSString stringWithFormat:NSLocalizedString(@"Room %@", @"label - room number"), [meetingJson objectForKey:@"room"]];
                        } else {
                            event.line2 = [NSString stringWithFormat:@"%@ - %@", startLabel, endLabel];   
                        }
                        
                        event.start = startDate;
                        event.end = endDate;
                        NSArray *userInfoKeys = [[NSArray alloc] initWithObjects:@"courseName", @"sectionId", @"termId", @"isInstructor", @"courseSectionNumber", nil];
                        NSArray *userInfoObjects = [[NSArray alloc] initWithObjects:[meetingJson objectForKey:@"courseName"], [meetingJson objectForKey:@"sectionId"], [meetingJson objectForKey:@"termId"], [meetingJson objectForKey:@"isInstructor"], [meetingJson objectForKey:@"courseSectionNumber"], nil];
                        NSDictionary *userInfo = [[NSDictionary alloc] initWithObjects:userInfoObjects forKeys:userInfoKeys];
                        event.userInfo = userInfo;
                        
                        [events addObject:event];
                    }
                    [self.cachedData setObject:events forKey:dateString];
                }
            }
            
            
            [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
            
        }
        
        return [self.cachedData objectForKey:dateValue];
    }
    
    return [[NSArray alloc] init];
}

- (void)dayView:(CalendarViewDayView *)dayView eventTapped:(CalendarViewEvent *)event
{
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryCourses withAction:kAnalyticsActionButton_Press withLabel:@"Click Course" withValue:nil forModuleNamed:self.module.name];
    [self performSegueWithIdentifier:@"Show Course Detail" sender:event];
}

-(NSDateFormatter *)datetimeOutputFormatter
{
    if(_datetimeOutputFormatter == nil) {
        _datetimeOutputFormatter = [[NSDateFormatter alloc] init];
        [_datetimeOutputFormatter setDateStyle:NSDateFormatterNoStyle];
        [_datetimeOutputFormatter setTimeStyle:NSDateFormatterShortStyle];
    }
    return _datetimeOutputFormatter;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:@"Show Course Detail"])
    {
        CalendarViewEvent *viewEvent = (CalendarViewEvent *)sender;
        NSDictionary *userInfo = viewEvent.userInfo;
        CourseDetailTabBarController *tabBarController = (CourseDetailTabBarController *)[segue destinationViewController];
        tabBarController.isInstructor = [[userInfo objectForKey:@"isInstructor"] boolValue];
        tabBarController.module = self.module;
        tabBarController.termId = [userInfo objectForKey:@"termId"];
        tabBarController.sectionId = [userInfo objectForKey:@"sectionId"];
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
                [vc setValue:[userInfo objectForKey:@"sectionId"] forKey:@"sectionId"];
            }
            if([vc respondsToSelector:@selector(setTermId:)]) {
                [vc setValue:[userInfo objectForKey:@"termId"] forKey:@"termId"];
            }
            if([vc respondsToSelector:@selector(setCourseName:)]) {
                [vc setValue:[userInfo objectForKey:@"courseName"] forKey:@"courseName"];
            }
            if([vc respondsToSelector:@selector(setCourseNameAndSectionNumber:)]) {
                NSString *courseNameAndSectionNumber = [NSString stringWithFormat:@"%@-%@", [userInfo objectForKey:@"courseName"], [userInfo objectForKey:@"courseSectionNumber"] ];
                [vc setValue:courseNameAndSectionNumber forKey:@"courseNameAndSectionNumber"];
            }
        }
    }
}

@end
