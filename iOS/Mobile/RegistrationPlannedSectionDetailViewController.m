//
//  RegistrationPlannedSectionDetailViewController.m
//  Mobile
//
//  Created by jkh on 1/6/14.
//  Edited by amcewan on 3/10/2014
//  Copyright (c) 2014 Ellucian. All rights reserved.
//

#import "RegistrationPlannedSectionDetailViewController.h"
#import "Module.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "AppearanceChanger.h"
#import "CourseDetailInstructor.h"
#import "RegistrationPlannedSectionMeetingPattern.h"
#import "CourseDetail.h"

@interface RegistrationPlannedSectionDetailViewController ()
@property (strong, nonatomic) NSOrderedSet *meetingPatterns;
@property (strong, nonatomic) NSOrderedSet *instructors;
@property (nonatomic, strong) NSDateFormatter *dateFormatter;
@property (nonatomic, strong) NSDateFormatter *timeFormatter;
@property (nonatomic, strong) NSDateFormatter *displayDateFormatter;
@property (nonatomic, strong) NSDateFormatter *displayTimeFormatter;
@property (nonatomic, strong) NSNumberFormatter *creditsFormatter;
@end

@implementation RegistrationPlannedSectionDetailViewController

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self adjustContraintsAccordingToContent];
    [self sendView:@"Registration Section Detail" forModuleNamed:self.module.name];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        
        self.maskView = [[UIView alloc] initWithFrame:self.view.bounds];
        [self.maskView setBackgroundColor:[UIColor whiteColor]];
        [self.view addSubview:self.maskView];
        
    }
    
    [self.scrollView invalidateIntrinsicContentSize];
    
    self.navigationController.navigationBar.translucent = NO;
    
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
        
        if([AppearanceChanger isRTL]) {
            self.courseSectionNumberLabel.textAlignment = NSTextAlignmentRight;
            self.courseNameLabel.textAlignment = NSTextAlignmentRight;
        }
        self.courseSectionNumberLabel.text = [NSString stringWithFormat:@"%@-%@", self.registrationPlannedSection.courseName, self.registrationPlannedSection.courseSectionNumber];
        self.courseNameLabel.text = self.registrationPlannedSection.sectionTitle;
        
        if (self.registrationPlannedSection.isPassFail){
            self.gradingContent.text = NSLocalizedString(@"Pass/Fail", @"PassFail label for registration");
        } else if (self.registrationPlannedSection.isGraded){
            self.gradingContent.text = NSLocalizedString(@"Graded", @"Graded label for registration");
        } else if (self.registrationPlannedSection.isAudit){
            self.gradingContent.text = NSLocalizedString(@"Audit", @"Audit label for registration");
        }
    
        if(self.registrationPlannedSection.firstMeetingDate && self.registrationPlannedSection.lastMeetingDate) {
            NSString *dates = [NSString stringWithFormat:@"%@ - %@", [self.displayDateFormatter stringFromDate:self.registrationPlannedSection.firstMeetingDate], [self.displayDateFormatter stringFromDate:self.registrationPlannedSection.lastMeetingDate]];
            self.meetingDateLabel.text = dates;
        } else {
            self.meetingDateLabel.text = @"";
        }
        //self.sectionLabel.text = @"Section ID:";
        self.sectionContent.text = self.registrationPlannedSection.sectionId;
    
        //self.creditsLabel.text = @"Credits:";
        if ( self.registrationPlannedSection.credits ) {
            //self.creditsLabel.text = @"Credits:";
            if ( self.registrationPlannedSection.isVariableCredit ) {
                NSString *creditRange = [NSString stringWithFormat:@"%@ - %@", [self.creditsFormatter stringFromNumber:self.registrationPlannedSection.minimumCredits], [self.creditsFormatter stringFromNumber:self.registrationPlannedSection.maximumCredits]];
                self.creditsContent.text = creditRange;
            } else {
                self.creditsContent.text = [self.creditsFormatter stringFromNumber:self.registrationPlannedSection.credits];
            }
        }
        else if ( self.registrationPlannedSection.ceus ) {
            NSString * ceusText = [self.creditsFormatter stringFromNumber:self.registrationPlannedSection.ceus];
            
            if ([self.registrationPlannedSection.ceus doubleValue] > 1.0 ){
                self.creditsContent.text = [NSString stringWithFormat:@"%@ %@", ceusText, NSLocalizedString(@"CEUs", @"CEUs label for registration")];

            }
            else if ([self.registrationPlannedSection.ceus doubleValue] <= 1.0 ){
                self.creditsContent.text = [NSString stringWithFormat:@"%@ %@", ceusText, NSLocalizedString(@"CEU", @"CEU (singular) label for registration")];
            }
        }
        
        
        //self.meetingLabel.text = @"Meets";
        [self extractMeetingContent];

        //self.facultyLabel.text = @"Faculty";
        [self extractFacultyContentForPhone];
        
        self.descriptionContent.text = self.registrationPlannedSection.courseDescription;
        [self adjustContraintsAccordingToContent];
        
        self.titleBackgroundView.backgroundColor = [UIColor accentColor];
    }
}

- (void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    [self.scrollView setContentOffset:CGPointZero animated:YES];
}

//- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
//{
//    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
//    if (self) {
//        // Custom initialization
//    }
//    return self;
//}
//
//- (void)viewDidLoad
//{
//    [super viewDidLoad];
//	// Do any additional setup after loading the view.
//}
//
//- (void)didReceiveMemoryWarning
//{
//    [super didReceiveMemoryWarning];
//    // Dispose of any resources that can be recreated.
//}
//
//-(BOOL)splitViewController:(UISplitViewController *)svc shouldHideViewController:(UIViewController *)vc inOrientation:(UIInterfaceOrientation)orientation
//{
//    return NO;
//}

-(void) didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    //if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
        [self adjustContraintsAccordingToContent];
    //}
}

-(void) extractFacultyContentForPhone
{
    id lastObject = self.facultyContent;
    
    CGFloat fontSize = 13.0f;
    NSInteger facultyContentHeight = 10;
    
    NSArray * instructors = self.registrationPlannedSection.instructors;
    //int instructorCount = 10;
    NSInteger instructorCount = [instructors count];

    if ( instructorCount > 0 )
        facultyContentHeight = 0;
    
    //remove all subviews
    [[self.facultyContent subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
    
    for (NSInteger iter = 0; iter < instructorCount; iter++) {
        
        CourseDetailInstructor *instructor = [instructors objectAtIndex:iter];
        
        //faculty name
        UILabel *facultyLabel = [UILabel new];
        facultyLabel.backgroundColor = [UIColor clearColor];
        facultyLabel.translatesAutoresizingMaskIntoConstraints = NO;
        facultyLabel.text = instructor.formattedName;
        facultyLabel.font = [UIFont systemFontOfSize:fontSize];
        facultyLabel.textAlignment = [AppearanceChanger isRTL] ? NSTextAlignmentRight : NSTextAlignmentLeft;
        [facultyLabel sizeToFit];
        [self.facultyContent addSubview:facultyLabel];
        
        NSInteger labelHeight = facultyLabel.frame.size.height;
        
        facultyContentHeight += labelHeight;

        [self.facultyContent addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"|-(0)-[facultyLabel]-(10)-|"
                                                 options:0 metrics:nil
                                                   views:@{@"facultyLabel":facultyLabel}]];

        if ( instructorCount == 1 )
        {
            [self.facultyContent addConstraints:
             [NSLayoutConstraint constraintsWithVisualFormat:@"V:|-(0)-[facultyLabel]-(1)-|"
                                                     options:0 metrics:nil
                                                       views:@{@"facultyLabel":facultyLabel}]];
            facultyContentHeight += 6;
            
        } else if ( instructorCount > 1 ){
            if ( iter == 0 ) {
                [self.facultyContent addConstraints:
                 [NSLayoutConstraint constraintsWithVisualFormat:@"V:|-(0)-[facultyLabel]"
                                                         options:0 metrics:nil
                                                           views:@{@"facultyLabel":facultyLabel}]];
                facultyContentHeight += 3;
                
            } else if ( 0 < iter < (instructorCount-1) ){
                [self.facultyContent addConstraints:
                 [NSLayoutConstraint constraintsWithVisualFormat:@"V:[prevLabel]-(1)-[facultyLabel]"
                                                         options:0 metrics:nil
                                                           views:@{@"facultyLabel":facultyLabel, @"prevLabel":lastObject}]];
                facultyContentHeight += 3;
                
            } else if ( iter == (instructorCount - 1) ){
                [self.facultyContent addConstraints:
                 [NSLayoutConstraint constraintsWithVisualFormat:@"V:[facultyLabel]-(1)-|"
                                                         options:0 metrics:nil
                                                           views:@{@"facultyLabel":facultyLabel}]];
                facultyContentHeight += 3;
            }
        }
        
        lastObject = facultyLabel;
    }
    self.facultyConstraint.constant = facultyContentHeight;
}

-(void) extractFacultyContentForPad
{
    id lastSeparator = nil;
    NSInteger constraintA=6;
    NSInteger constraintC=12;
    NSInteger constraintD=3;
    
    NSArray * instructors = self.registrationPlannedSection.instructors;
    //int instructorCount = 10;
    NSInteger instructorCount = [instructors count];
    CGFloat fontSize = 13.0f;
    NSInteger facultyContentHeight = 20;
    
    if ( instructorCount > 0 )
        facultyContentHeight = 0;
    
    //remove all subviews
    [[self.facultyContent subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
    
    for (NSInteger iter = 0; iter < instructorCount; iter++) {
        
        CourseDetailInstructor *instructor = [instructors objectAtIndex:iter];
        
        //faculty name
        UILabel *facultyLabel = [UILabel new];
        facultyLabel.backgroundColor = [UIColor clearColor];
        facultyLabel.translatesAutoresizingMaskIntoConstraints = NO;
        facultyLabel.text = instructor.formattedName;
        facultyLabel.font = [UIFont systemFontOfSize:fontSize];
        facultyLabel.textAlignment = [AppearanceChanger isRTL] ? NSTextAlignmentRight : NSTextAlignmentLeft;
        [facultyLabel sizeToFit];
        [self.facultyContent addSubview:facultyLabel];
        
        NSInteger labelHeight = facultyLabel.frame.size.height;
        
        facultyContentHeight += labelHeight;
        
        [self.facultyContent addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"|-(0)-[facultyLabel]-(10)-|"
                                                 options:0 metrics:nil
                                                   views:@{@"facultyLabel":facultyLabel}]];
        
        if ( instructorCount == 1 )
        {
            [self.facultyContent addConstraints:
             [NSLayoutConstraint constraintsWithVisualFormat:@"V:|-(0)-[facultyLabel]-(0)-|"
                                                     options:0 metrics:nil
                                                       views:@{@"facultyLabel":facultyLabel}]];
            facultyContentHeight += constraintA;
            
        } else if ( instructorCount > 1 ){
            if ( iter == 0 ) {
                [self.facultyContent addConstraints:
                 [NSLayoutConstraint constraintsWithVisualFormat:@"V:|-(0)-[facultyLabel]"
                                                         options:0 metrics:nil
                                                           views:@{@"facultyLabel":facultyLabel}]];
                facultyContentHeight += constraintD;
                
                UIView *separatorLine = [self createSeparatorLine];
                
                [self.facultyContent addSubview:separatorLine];
                
                [self.facultyContent addConstraints:
                 [NSLayoutConstraint constraintsWithVisualFormat:@"V:[facultyLabel]-(2)-[separator]"
                                                         options:0 metrics:nil
                                                           views:@{@"facultyLabel":facultyLabel, @"separator":separatorLine}]];
                [self.facultyContent addConstraints:
                 [NSLayoutConstraint constraintsWithVisualFormat:@"|-(0)-[separator]-(0)-|"
                                                         options:0 metrics:nil
                                                           views:@{@"separator":separatorLine}]];
                
                [separatorLine addConstraint:[NSLayoutConstraint constraintWithItem:separatorLine
                                                                          attribute:NSLayoutAttributeHeight
                                                                          relatedBy:NSLayoutRelationEqual
                                                                             toItem:nil
                                                                          attribute:NSLayoutAttributeNotAnAttribute
                                                                         multiplier:1.0
                                                                           constant:1]];
                
                lastSeparator = separatorLine;
                
            } else if ( (0 < iter) && (iter < (instructorCount-1)) ){
                
                [self.facultyContent addConstraints:
                 [NSLayoutConstraint constraintsWithVisualFormat:@"V:[prevSeparator]-(2)-[facultyLabel]"
                                                         options:0 metrics:nil
                                                           views:@{@"facultyLabel":facultyLabel, @"prevSeparator":lastSeparator}]];
                
                facultyContentHeight += constraintC;
                
                UIView *separatorLine = [self createSeparatorLine];
                
                [self.facultyContent addSubview:separatorLine];
                
                [self.facultyContent addConstraints:
                 [NSLayoutConstraint constraintsWithVisualFormat:@"V:[facultyLabel]-(2)-[separator]"
                                                         options:0 metrics:nil
                                                           views:@{@"facultyLabel":facultyLabel, @"separator":separatorLine}]];
                [self.facultyContent addConstraints:
                 [NSLayoutConstraint constraintsWithVisualFormat:@"|-(0)-[separator]-(0)-|"
                                                         options:0 metrics:nil
                                                           views:@{@"separator":separatorLine}]];
                
                [separatorLine addConstraint:[NSLayoutConstraint constraintWithItem:separatorLine
                                                                          attribute:NSLayoutAttributeHeight
                                                                          relatedBy:NSLayoutRelationEqual
                                                                             toItem:nil
                                                                          attribute:NSLayoutAttributeNotAnAttribute
                                                                         multiplier:1.0
                                                                           constant:1]];
                
                lastSeparator = separatorLine;
                
            } else if ( iter == (instructorCount - 1) ){
                
                [self.facultyContent addConstraints:
                 [NSLayoutConstraint constraintsWithVisualFormat:@"V:[prevSeparator]-(2)-[facultyLabel]"
                                                         options:0 metrics:nil
                                                           views:@{@"facultyLabel":facultyLabel, @"prevSeparator":lastSeparator}]];
                facultyContentHeight += constraintD;
                
            }
        }
    }
    self.facultyConstraint.constant = facultyContentHeight;
}

-(void) extractMeetingContent
{
    id lastSeparator = nil;
    
    NSArray *meetingPatterns = self.registrationPlannedSection.meetingPatterns;
    
    //NSInteger meetingCount = 1;
    NSInteger meetingCount = [meetingPatterns count];
    
    NSInteger meetingContentHeight = 20;
    NSInteger constraintA=6;
    NSInteger constraintB=8;
    NSInteger constraintC=12;
    CGFloat fontSize = 13.0f;
    
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
        meetingContentHeight = 10;
    } else if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        meetingContentHeight = 20;
        fontSize = 13.0f;
    }
    
    if (meetingCount > 0)
        meetingContentHeight = 0;
    
    //remove all subviews
    [[self.meetingContent subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
    
    for (NSInteger iter = 0; iter < meetingCount; iter++) {
        
        RegistrationPlannedSectionMeetingPattern *mp = [meetingPatterns objectAtIndex:iter];
        
        NSString * meetingContent = @"";
        //mp:days
        NSMutableArray *daysOfClass = [mp.daysOfWeek mutableCopy];
        NSArray *localizedDays = [self.dateFormatter shortStandaloneWeekdaySymbols];
        for(int i = 0; i < [daysOfClass count]; i++) {
            NSInteger value = [[daysOfClass objectAtIndex:i] intValue] - 1;
                [daysOfClass replaceObjectAtIndex:i withObject:[localizedDays objectAtIndex:value]];
        }
        
        //mp:time
        NSString *days = [NSString stringWithFormat:@"%@: ",  [daysOfClass componentsJoinedByString:@", "]];
        NSString *line1 = [NSString stringWithFormat:@"%@ %@ - %@ %@", days, [self.displayTimeFormatter stringFromDate: mp.startTime], [self.displayTimeFormatter stringFromDate:mp.endTime], mp.instructionalMethodCode];

        NSMutableAttributedString *attributedLine1 =[[NSMutableAttributedString alloc]initWithString:line1];
        [attributedLine1 addAttribute:NSFontAttributeName value:[UIFont boldSystemFontOfSize:fontSize+2.0f] range:NSMakeRange(0, [days length])];

        if ([meetingContent length] == 0){
            meetingContent = attributedLine1.string;
        } else {
            meetingContent = [meetingContent stringByAppendingString:@"\r"];
            meetingContent = [meetingContent stringByAppendingString:attributedLine1.string];
        }

        NSString *location = @"";
        if(mp.building && mp.room) {
            location = [NSString stringWithFormat:NSLocalizedString(@"%@, Room %@", @"label - building name, room number"), mp.building, mp.room];
            } else if(mp.building) {
                location = mp.building;
            } else  if(mp.room) {
                location = [NSString stringWithFormat:NSLocalizedString(@"Room %@", @"label - room number"), mp.room];
            }

        meetingContent = [meetingContent stringByAppendingString:@"\r"];
        meetingContent = [meetingContent stringByAppendingString:location];
    
        UILabel *meetingLabel = [UILabel new];
        meetingLabel.backgroundColor = [UIColor clearColor];
        meetingLabel.translatesAutoresizingMaskIntoConstraints = NO;
        meetingLabel.text = meetingContent;
        meetingLabel.font = [UIFont systemFontOfSize:fontSize];
        meetingLabel.textAlignment = [AppearanceChanger isRTL] ? NSTextAlignmentRight : NSTextAlignmentLeft;
        meetingLabel.numberOfLines = 0;
        [meetingLabel sizeToFit];
        [self.meetingContent addSubview:meetingLabel];
        
        NSInteger labelHeight = meetingLabel.frame.size.height;
        
        meetingContentHeight += labelHeight;
        
        [self.meetingContent addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"|-(0)-[meetingLabel]-(10)-|"
                                                 options:0 metrics:nil
                                                   views:@{@"meetingLabel":meetingLabel}]];
        
        if ( meetingCount == 1 )
        {
            [self.meetingContent addConstraints:
             [NSLayoutConstraint constraintsWithVisualFormat:@"V:|-(0)-[meetingLabel]-(3)-|"
                                                     options:0 metrics:nil
                                                       views:@{@"meetingLabel":meetingLabel}]];
            meetingContentHeight += constraintA;
            
        } else if ( meetingCount > 1 ){
            if ( iter == 0 ) {
                [self.meetingContent addConstraints:
                 [NSLayoutConstraint constraintsWithVisualFormat:@"V:|-(0)-[meetingLabel]"
                                                         options:0 metrics:nil
                                                           views:@{@"meetingLabel":meetingLabel}]];
                meetingContentHeight += constraintB;
                
                UIView *separatorLine = [self createSeparatorLine];
                
                [self.meetingContent addSubview:separatorLine];
                
                [self.meetingContent addConstraints:
                 [NSLayoutConstraint constraintsWithVisualFormat:@"V:[meetingLabel]-(3)-[separator]"
                                                         options:0 metrics:nil
                                                           views:@{@"meetingLabel":meetingLabel, @"separator":separatorLine}]];
                [self.meetingContent addConstraints:
                 [NSLayoutConstraint constraintsWithVisualFormat:@"|-(0)-[separator]-(0)-|"
                                                         options:0 metrics:nil
                                                           views:@{@"separator":separatorLine}]];
                
                [separatorLine addConstraint:[NSLayoutConstraint constraintWithItem:separatorLine
                                                                          attribute:NSLayoutAttributeHeight
                                                                          relatedBy:NSLayoutRelationEqual
                                                                             toItem:nil
                                                                          attribute:NSLayoutAttributeNotAnAttribute
                                                                         multiplier:1.0
                                                                           constant:1]];
                
                lastSeparator = separatorLine;
                
            } else if ( (0 < iter) && (iter < (meetingCount-1)) ){
                [self.meetingContent addConstraints:
                 [NSLayoutConstraint constraintsWithVisualFormat:@"V:[prevSeparator]-(3)-[meetingLabel]"
                                                         options:0 metrics:nil
                                                           views:@{@"meetingLabel":meetingLabel, @"prevSeparator":lastSeparator}]];
                
                meetingContentHeight += constraintC;
                
                UIView *separatorLine = [self createSeparatorLine];
                
                [self.meetingContent addSubview:separatorLine];
                
                [self.meetingContent addConstraints:
                 [NSLayoutConstraint constraintsWithVisualFormat:@"V:[meetingLabel]-(5)-[separator]"
                                                         options:0 metrics:nil
                                                           views:@{@"meetingLabel":meetingLabel, @"separator":separatorLine}]];
                [self.meetingContent addConstraints:
                 [NSLayoutConstraint constraintsWithVisualFormat:@"|-(0)-[separator]-(0)-|"
                                                         options:0 metrics:nil
                                                           views:@{@"separator":separatorLine}]];
                
                [separatorLine addConstraint:[NSLayoutConstraint constraintWithItem:separatorLine
                                                                          attribute:NSLayoutAttributeHeight
                                                                          relatedBy:NSLayoutRelationEqual
                                                                             toItem:nil
                                                                          attribute:NSLayoutAttributeNotAnAttribute
                                                                         multiplier:1.0
                                                                           constant:1]];
                
                lastSeparator = separatorLine;
                
            } else if ( iter == (meetingCount-1) ){
                
                [self.meetingContent addConstraints:
                 [NSLayoutConstraint constraintsWithVisualFormat:@"V:[prevSeparator]-(5)-[meetingLabel]"
                                                         options:0 metrics:nil
                                                           views:@{@"meetingLabel":meetingLabel, @"prevSeparator":lastSeparator}]];
                meetingContentHeight += constraintC;
            }
        }
    }
    self.meetingConstraint.constant = meetingContentHeight;
}

-(UIView*)createSeparatorLine
{
    UIView *separatorLine = [UIView new];
    separatorLine.translatesAutoresizingMaskIntoConstraints = NO;
    [separatorLine setBackgroundColor:[[UIColor blackColor] colorWithAlphaComponent:0.3]];
    return separatorLine;
}

-(void) adjustContraintsAccordingToContent
{
    UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
    
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        if (UIInterfaceOrientationIsLandscape(orientation)) {
            self.descriptionContent.frame = CGRectMake(15, 206, 510, 3000.0f);
            self.creditLabelConstraint.constant = 197;
        } else {
            self.descriptionContent.frame = CGRectMake(15, 206, 393, 3000.0f);
            self.creditLabelConstraint.constant = 40;
        }
    } else {
        self.widthConstraint.constant = [AppearanceChanger sizeInOrientation:self.interfaceOrientation].width;
        self.descriptionContent.frame = CGRectMake(10, 139, self.widthConstraint.constant-20, 30000.0f);
    }
    
    [self.descriptionContent sizeToFit];
    self.descriptionConstraint.constant = self.descriptionContent.bounds.size.height;

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

-(NSNumberFormatter *)creditsFormatter
{
    if(_creditsFormatter == nil) {
        _creditsFormatter = [NSNumberFormatter new];
        _creditsFormatter.numberStyle = NSNumberFormatterDecimalStyle;
        [_creditsFormatter setMinimumFractionDigits:1];
    }
    return _creditsFormatter;
}

- (void)dismissMasterPopover
{
    if (_masterPopover != nil) {
        [_masterPopover dismissPopoverAnimated:YES];
    }
}

-(void)selectedDetail:(id)newSection withModule:(Module*)myModule
{
    if ( [newSection isKindOfClass:[RegistrationPlannedSection class]] )
    {
        [self setSection:(RegistrationPlannedSection *)newSection];
        [self setModule:myModule];
        
        if (_masterPopover != nil) {
            [_masterPopover dismissPopoverAnimated:YES];
        }
    }
}

-(void)setSection:(RegistrationPlannedSection *)section
{
    if (_registrationPlannedSection != section) {
        _registrationPlannedSection = section;
        
        [self refreshUI];
    }
}

-(void)refreshUI
{
    if(self.maskView != nil)
    {
        [self.maskView removeFromSuperview];
        self.maskView = nil;
    }
    
    [self.scrollView invalidateIntrinsicContentSize];
    
    self.navigationController.navigationBar.translucent = NO;
    
    if([AppearanceChanger isRTL]) {
        self.courseSectionNumberLabel.textAlignment = NSTextAlignmentRight;
        self.courseNameLabel.textAlignment = NSTextAlignmentRight;
    }
    
    self.courseSectionNumberLabel.text = [NSString stringWithFormat:@"%@-%@", self.registrationPlannedSection.courseName, self.registrationPlannedSection.courseSectionNumber];
    self.courseNameLabel.text = self.registrationPlannedSection.sectionTitle;
    
    //self.sectionLabel.text = @"Section ID:";
    self.sectionContent.text = self.registrationPlannedSection.sectionId;
    
    if (self.registrationPlannedSection.isPassFail){
        self.gradingContent.text = NSLocalizedString(@"Pass/Fail", @"PassFail label for registration");
    } else if (self.registrationPlannedSection.isGraded){
        self.gradingContent.text = NSLocalizedString(@"Graded", @"Graded label for registration");
    } else if (self.registrationPlannedSection.isAudit){
        self.gradingContent.text = NSLocalizedString(@"Audit", @"Audit label for registration");
    }
    
    if(self.registrationPlannedSection.firstMeetingDate && self.registrationPlannedSection.lastMeetingDate) {
        NSString *dates = [NSString stringWithFormat:@"%@ - %@", [self.displayDateFormatter stringFromDate:self.registrationPlannedSection.firstMeetingDate], [self.displayDateFormatter stringFromDate:self.registrationPlannedSection.lastMeetingDate]];
        self.meetingDateLabel.text = dates;
    } else {
        self.meetingDateLabel.text = @"";
    }
    
    if ( self.registrationPlannedSection.credits ) {
        //self.creditsLabel.text = @"Credits:";
        if ( self.registrationPlannedSection.isVariableCredit ) {
            NSString *creditRange = [NSString stringWithFormat:@"%@ - %@", [self.creditsFormatter stringFromNumber:self.registrationPlannedSection.minimumCredits], [self.creditsFormatter stringFromNumber:self.registrationPlannedSection.maximumCredits]];
            self.creditsContent.text = creditRange;
        } else {
                self.creditsContent.text = [self.creditsFormatter stringFromNumber:self.registrationPlannedSection.credits];
        }
    } else if ( self.registrationPlannedSection.ceus ) {
        NSString * ceusText = [self.creditsFormatter stringFromNumber:self.registrationPlannedSection.ceus];
        if ( [self.registrationPlannedSection.ceus doubleValue] > 1.0 ) {
            
            self.creditsContent.text = [ceusText stringByAppendingString:NSLocalizedString(@"CEUs", @"CEUs label for registration") ];
        } else if ([self.registrationPlannedSection.ceus doubleValue] <= 1.0 ){
            self.creditsContent.text = [ceusText stringByAppendingString:NSLocalizedString(@"CEU", @"CEU (singular) label for registration")];
        }
        
    }
    
    //self.meetingLabel.text = @"Meets";
    [self extractMeetingContent];
    
    //self.facultyLabel.text = @"Faculty";
    [self extractFacultyContentForPad];
    
    //self.descriptionLabel.text = @"Description";
    self.descriptionContent.text = self.registrationPlannedSection.courseDescription;
    
    self.titleBackgroundView.backgroundColor = [UIColor accentColor];
    [self adjustContraintsAccordingToContent];
    
    [self.view setNeedsDisplay];
    
}

#pragma mark - UISplitViewDelegate methods
-(void)splitViewController:(UISplitViewController *)svc
    willHideViewController:(UIViewController *)aViewController
         withBarButtonItem:(UIBarButtonItem *)barButtonItem
      forPopoverController:(UIPopoverController *)pc
{
    //Grab a reference to the popover
    self.masterPopover = pc;
    
    //Set the title of the bar button item
    barButtonItem.title = self.module.name;
    
    //Set the bar button item as the Nav Bar's leftBarButtonItem
    //[_tabBarItem setLeftBarButtonItem:barButtonItem animated:YES];
}

-(void)splitViewController:(UISplitViewController *)svc
    willShowViewController:(UIViewController *)aViewController
 invalidatingBarButtonItem:(UIBarButtonItem *)barButtonItem
{
    //Remove the barButtonItem.
    //[_navBarItem setLeftBarButtonItem:nil animated:YES];
    
    //Nil out the pointer to the popover.
    _masterPopover = nil;
}

@end
