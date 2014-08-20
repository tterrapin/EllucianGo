//
//  CourseDetailViewController.m
//  Mobile
//
//  Created by Jason Hocker on 9/26/12.
//  Copyright (c) 2012-2014 Ellucian. All rights reserved.
//

#import "CourseDetailViewController.h"
#import "CurrentUser.h"
#import "CourseDetail.h"
#import "CourseDetailInstructor.h"
#import "CourseMeetingPattern.h"
#import "POIDetailViewController.h"
#import "DirectoryViewController.h"
#import "CourseDetailTabBarController.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "AppearanceChanger.h"
#import "UIScrollView+Size.h"

@interface CourseDetailViewController ()
@property (strong, nonatomic) NSOrderedSet *meetingPatterns;
@property (strong, nonatomic) NSOrderedSet *instructors;
@property (nonatomic, strong) NSDateFormatter *dateFormatter;
@property (nonatomic, strong) NSDateFormatter *timeFormatter;
@property (nonatomic, strong) NSDateFormatter *displayDateFormatter;
@property (nonatomic, strong) NSDateFormatter *displayTimeFormatter;
@property (nonatomic, strong) NSDateFormatter *sisTimeFormatterWTz;
@end

@implementation CourseDetailViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.navigationController.navigationBar.translucent = NO;
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(setupData) name:kCourseDetailInformationLoaded object:nil];
    
    self.sectionTitleLabel.text = nil;
    self.dateLabel.text = nil;
    
    self.facultyLabel.textColor = [UIColor subheaderTextColor];
    self.sectionTitleLabel.textColor = [UIColor subheaderTextColor];
    self.dateLabel.textColor = [UIColor subheaderTextColor];
    
    if([AppearanceChanger isRTL]) {
        self.sectionTitleLabel.textAlignment = NSTextAlignmentRight;
    }
    
    self.navigationItem.title = self.courseNameAndSectionNumber;
    
    self.backgroundView.backgroundColor = [UIColor accentColor];
    
    self.facultyLabelViewHeightConstraint.constant = 0;
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Course Overview" forModuleNamed:self.module.name];
    self.widthConstraint.constant = [AppearanceChanger sizeInOrientation:self.interfaceOrientation].width;
    self.courseDescriptionTextViewHeightConstraint.constant = [self.courseDescriptionTextView heightOfContent];
}

- (IBAction)dismiss:(id)sender {
     [self dismissViewControllerAnimated:YES completion:nil];
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

-(NSDateFormatter *)displayDateFormatter
{
    if(_displayDateFormatter == nil) {
        _displayDateFormatter = [[NSDateFormatter alloc] init];
        [_displayDateFormatter setDateStyle:NSDateFormatterShortStyle];
        [_displayDateFormatter setTimeStyle:NSDateFormatterNoStyle];
        [_displayDateFormatter setTimeZone:[NSTimeZone timeZoneWithName:@"UTC"]];
    }
    return _displayDateFormatter;
}

-(NSDateFormatter *)displayTimeFormatter
{
    if(_displayTimeFormatter == nil) {
        _displayTimeFormatter = [[NSDateFormatter alloc] init];
        [_displayTimeFormatter setDateStyle:NSDateFormatterNoStyle];
        [_displayTimeFormatter setTimeStyle:NSDateFormatterShortStyle];
    }
    return _displayTimeFormatter;
}

-(void) setupData
{
    NSString *buildingsUrl = [[NSUserDefaults standardUserDefaults] objectForKey:@"urls-map-buildings"];
    
    NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"CourseDetail"];
    request.predicate = [NSPredicate predicateWithFormat:@"termId == %@ && sectionId == %@", self.termId, self.sectionId];
    
    NSError *error;
    CourseDetail *courseDetail = [[self.module.managedObjectContext executeFetchRequest:request error:&error] lastObject];
    self.instructors = courseDetail.instructors;
    self.meetingPatterns = courseDetail.meetingPatterns;
    
    //title
    self.sectionTitleLabel.text = courseDetail.sectionTitle;
    
    //date
    if(courseDetail.firstMeetingDate && courseDetail.lastMeetingDate) {
        NSString *dates = [NSString stringWithFormat:NSLocalizedStringWithDefaultValue(@"course first meeting - last meeting", @"Localizable", [NSBundle mainBundle], @"%@ - %@", @"course first meeting - last meeting"), [self.displayDateFormatter stringFromDate:courseDetail.firstMeetingDate], [self.displayDateFormatter stringFromDate:courseDetail.lastMeetingDate]];        self.dateLabel.text = dates;
    }
    
    //meeting patterns
    id lastObject = self.view;
    if([self.meetingPatterns count] > 0) {
        for (int i = 0; i < [self.meetingPatterns count]; i++) {
            
            CourseMeetingPattern *mp = [self.meetingPatterns objectAtIndex:i];
            
            PseudoButtonView *meetingPatternView = [PseudoButtonView new];
            meetingPatternView.tag = 100 + i;
            meetingPatternView.translatesAutoresizingMaskIntoConstraints = NO;
            
            //mp:days
            NSMutableArray *daysOfClass = [[mp.daysOfWeek componentsSeparatedByString:@"," ] mutableCopy];
            NSArray *localizedDays = [self.dateFormatter shortStandaloneWeekdaySymbols];
            for(int i = 0; i < [daysOfClass count]; i++) {
                NSInteger value = [[daysOfClass objectAtIndex:i] intValue] - 1;
                [daysOfClass replaceObjectAtIndex:i withObject:[localizedDays objectAtIndex:value]];
            }
            
            //mp:time
            NSString *days = [NSString stringWithFormat:NSLocalizedStringWithDefaultValue(@"days:", @"Localizable", [NSBundle mainBundle], @"%@: ", @"days:"), [daysOfClass componentsJoinedByString:@", "]];
            NSString *line1;
            if(mp.instructionalMethodCode) {
                line1 = [NSString stringWithFormat:NSLocalizedStringWithDefaultValue(@"days start - end method", @"Localizable", [NSBundle mainBundle], @"%@ %@ - %@ %@", @"days start - end method"), days, [self.displayTimeFormatter stringFromDate: mp.startTime], [self.displayTimeFormatter stringFromDate:mp.endTime], mp.instructionalMethodCode];
            } else {
                line1 = [NSString stringWithFormat:NSLocalizedStringWithDefaultValue(@"days start - end", @"Localizable", [NSBundle mainBundle], @"%@ %@ - %@", @"days start - end"), days, [self.displayTimeFormatter stringFromDate: mp.startTime], [self.displayTimeFormatter stringFromDate:mp.endTime]];
            }
           

            NSMutableAttributedString *attributedLine1 =[[NSMutableAttributedString alloc]initWithString:line1];
            [attributedLine1 addAttribute:NSFontAttributeName value:[UIFont boldSystemFontOfSize:14.0f] range:NSMakeRange(0, [days length])];
            UILabel *line1Label = [UILabel new];
            line1Label.translatesAutoresizingMaskIntoConstraints = NO;
            line1Label.backgroundColor = [UIColor clearColor];
            [line1Label setFont:[UIFont systemFontOfSize:14.0f]];
            [line1Label setTextColor:[UIColor subheaderTextColor]];
            line1Label.minimumScaleFactor = .5f;
            line1Label.adjustsFontSizeToFitWidth = YES;
            line1Label.textAlignment = [AppearanceChanger isRTL] ? NSTextAlignmentRight : NSTextAlignmentLeft;

            line1Label.attributedText = attributedLine1;
            [meetingPatternView addSubview:line1Label];
            
            //mp:first line
            [meetingPatternView addConstraints: [NSLayoutConstraint constraintsWithVisualFormat:@"|-10-[label]-10-|"
                                                                                        options:NSLayoutFormatAlignAllCenterY metrics:nil
                                                                                          views:@{@"label":line1Label}]];
            
            
            //mp:location
            NSString *location = @"";
            if(mp.building && mp.room) {
                location = [NSString stringWithFormat:NSLocalizedString(@"%@, Room %@", @"label - building name, room number"), mp.building, mp.room];
            } else if(mp.building) {
                location = mp.building;
            } else  if(mp.room) {
                location = [NSString stringWithFormat:NSLocalizedString(@"Room %@", @"label - room number"), mp.room];
            }
            UILabel *locationLabel = [UILabel new];
            locationLabel.backgroundColor = [UIColor clearColor];
            [locationLabel setTextColor:[UIColor subheaderTextColor]];
            [locationLabel setFont:[UIFont systemFontOfSize:14.0f]];
            locationLabel.text = location;
            if(buildingsUrl && mp.buildingId) {
                [meetingPatternView setAction:@selector(gotoLocation:) withTarget:self];
                
                NSMutableAttributedString *underlinedString = [[NSMutableAttributedString alloc] initWithString:location];
                [underlinedString addAttribute:NSUnderlineStyleAttributeName
                                         value:[NSNumber numberWithInt:1]
                                         range:(NSRange){0,[location length]}];
                [underlinedString addAttribute:NSForegroundColorAttributeName
                                         value:[UIColor blueColor]
                                         range:(NSRange){0,[location length]}];
                locationLabel.attributedText = underlinedString;
            }
            locationLabel.translatesAutoresizingMaskIntoConstraints = NO;
            [meetingPatternView addSubview:locationLabel];
            
            //mp:vertical alignment
            [meetingPatternView addConstraints:
             [NSLayoutConstraint constraintsWithVisualFormat:@"V:|-8-[days][location]-8-|"
                                                     options:NSLayoutFormatAlignAllLeading metrics:nil
                                                       views:@{@"days":line1Label, @"location" :locationLabel}]];
            [self.meetingPatternsView addSubview:meetingPatternView];
            
            NSString *vf = @"V:[previous][meeting]";
            if([courseDetail.meetingPatterns count] == 1) {
                vf = @"V:|[meeting]|";
            } else if(i == 0) {
                vf = @"V:|[meeting]";
            } else if(i + 1 == [courseDetail.meetingPatterns count]) {
                vf = @"V:[previous][meeting]|";
            }
            [self.meetingPatternsView addConstraints:
             [NSLayoutConstraint constraintsWithVisualFormat:vf
                                                     options:NSLayoutFormatAlignAllLeading metrics:nil
                                                       views:@{@"previous":lastObject, @"meeting" :meetingPatternView}]];
            [self.meetingPatternsView addConstraints:
             [NSLayoutConstraint constraintsWithVisualFormat:@"|[meeting]|"
                                                     options:0 metrics:nil
                                                       views:@{ @"meeting" :meetingPatternView}]];
            lastObject = meetingPatternView;
        }
    } else {
        self.meetingPatternsView.translatesAutoresizingMaskIntoConstraints = NO;
        [self.meetingPatternsView addConstraint:[NSLayoutConstraint constraintWithItem:self.meetingPatternsView
                                                                              attribute:NSLayoutAttributeHeight
                                                                              relatedBy:NSLayoutRelationEqual
                                                                                 toItem:nil
                                                                              attribute:NSLayoutAttributeNotAnAttribute
                                                                             multiplier:1
                                                                               constant:0]];
    
        
    }

    lastObject = self.view;
    for (int i = 0; i < [self.instructors count]; i++) {
        
        [self.facultyLabelView removeConstraint:self.facultyLabelViewHeightConstraint];
        CourseDetailInstructor *instructor = [self.instructors objectAtIndex:i];
        
        PseudoButtonView *facultyView = [PseudoButtonView new];
        facultyView.tag = 200 + i;
        facultyView.translatesAutoresizingMaskIntoConstraints = NO;
        
        //faculty name
        UILabel *facultyLabel = [UILabel new];
        facultyLabel.backgroundColor = [UIColor clearColor];
        facultyLabel.translatesAutoresizingMaskIntoConstraints = NO;
        facultyLabel.text = instructor.formattedName;
        facultyLabel.font = [UIFont systemFontOfSize:16.0f];
        facultyLabel.textAlignment = [AppearanceChanger isRTL] ? NSTextAlignmentRight : NSTextAlignmentLeft;
        NSString *directoryUrl = [[NSUserDefaults standardUserDefaults] objectForKey:@"urls-directory-facultySearch"];
        if(directoryUrl) {
            [facultyView setAction:@selector(gotoFaculty:) withTarget:self];
        }
        
        [facultyView addSubview:facultyLabel];
        [facultyView addConstraint: [NSLayoutConstraint constraintWithItem:facultyLabel
                                     attribute:NSLayoutAttributeCenterY
                                     relatedBy:NSLayoutRelationEqual
                                        toItem:facultyView
                                     attribute:NSLayoutAttributeCenterY
                                    multiplier:1.0
                                      constant:0]];
        [facultyView addConstraint:[NSLayoutConstraint constraintWithItem:facultyView
                                                                             attribute:NSLayoutAttributeHeight
                                                                             relatedBy:NSLayoutRelationEqual
                                                                                toItem:nil
                                                                             attribute:NSLayoutAttributeNotAnAttribute
                                                                            multiplier:1
                                                                              constant:36.0f]];
        [facultyView addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"|-(10)-[faculty]-(10)-|"
                                                 options:0 metrics:nil
                                                   views:@{@"faculty":facultyLabel}]];
        [self.facultyView addSubview:facultyView];
        
        NSString *vf = @"V:[previous][faculty]";
        if([courseDetail.instructors count] == 1) {
            vf = @"V:|[faculty]|";
        } else if(i == 0) {
            vf = @"V:|[faculty]";
        } else if(i + 1 == [courseDetail.instructors count]) {
            vf = @"V:[previous][faculty]|";
        }
        [self.facultyView addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:vf
                                                 options:NSLayoutFormatAlignAllLeading metrics:nil
                                                   views:@{@"previous":lastObject, @"faculty" :facultyView}]];
        [self.facultyView addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"|[faculty]|"
                                                 options:0 metrics:nil
                                                   views:@{@"faculty":facultyView}]];
        lastObject = facultyView;
        
    }

    //description
    self.courseDescriptionTextView.text = courseDetail.courseDescription;
    self.courseDescriptionTextView.textAlignment = [AppearanceChanger isRTL] ? NSTextAlignmentRight : NSTextAlignmentLeft;
    
    self.widthConstraint.constant = [AppearanceChanger sizeInOrientation:self.interfaceOrientation].width;

    self.courseDescriptionTextViewHeightConstraint.constant = [self.courseDescriptionTextView heightOfContent];
    self.courseDescriptionTextViewBottomConstraint.constant = 0;
}

- (void)gotoLocation:(UIView *)view {
    NSInteger tag = view.tag - 100;
    CourseMeetingPattern *mp = [self.meetingPatterns objectAtIndex:tag];
    [self performSegueWithIdentifier:@"Show Course Location" sender:mp.buildingId];
}

- (void)gotoFaculty:(UIView *)view {
    NSInteger tag = view.tag - 200;
    CourseDetailInstructor *instructor = [self.instructors objectAtIndex:tag];
    [self performSegueWithIdentifier:@"Show Faculty Person" sender:instructor];
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    [self resetScrollViewContentOffset];
    if ([[segue identifier] isEqualToString:@"Show Course Location"])
    {
        [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionList_Select withLabel:@"Map Detail" withValue:nil forModuleNamed:self.module.name];
        POIDetailViewController *vc = (POIDetailViewController *)[segue destinationViewController];
        vc.buildingId = sender;
        vc.module = self.module;
    } else if ([[segue identifier] isEqualToString:@"Show Faculty Person"])
    {
        [self sendEventWithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionList_Select withLabel:@"Faculty detail" withValue:nil forModuleNamed:self.module.name];
        DirectoryViewController *detailController = [segue destinationViewController];
        CourseDetailInstructor *instructor = (CourseDetailInstructor *)sender;
        detailController.initialQueryString = [NSString stringWithFormat:@"%@ %@", instructor.firstName, instructor.lastName];
        detailController.initialScope = DirectoryViewTypeFaculty;
        detailController.hideStudents = YES;
        detailController.module = self.module;
    }
}

- (void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    self.widthConstraint.constant = [AppearanceChanger sizeInOrientation:toInterfaceOrientation].width;
}

-(void) didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    self.courseDescriptionTextViewHeightConstraint.constant = [self.courseDescriptionTextView heightOfContent];
    [self resetScrollViewContentOffset];
}

-(void) resetScrollViewContentOffset
{
    [self.courseDescriptionTextView setContentOffset:CGPointZero animated:YES];
    [self.scrollView setContentOffset:CGPointZero animated:YES];
}


@end
