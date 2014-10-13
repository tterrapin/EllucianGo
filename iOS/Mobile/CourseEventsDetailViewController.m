//
//  CourseEventsDetailViewController.m
//  Mobile
//
//  Created by Jason Hocker on 8/7/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "CourseEventsDetailViewController.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "AppearanceChanger.h"

@interface CourseEventsDetailViewController ()
@property (nonatomic, strong) UIActionSheet *actionSheet;
@property (nonatomic, strong) EKEventStore *eventStore;
@property (nonatomic, strong) NSDateFormatter *dateFormatterShare;
@property (nonatomic, strong) UIPopoverController *popover;
@end

@implementation CourseEventsDetailViewController


- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.navigationController.navigationBar.translucent = NO;
    
    if([AppearanceChanger isRTL]) {
        self.titleLabel.textAlignment = NSTextAlignmentRight;
        self.locationLabel.textAlignment = NSTextAlignmentRight;
        self.descriptionTextView.textAlignment = NSTextAlignmentRight;
        self.startDateLabelLabel.textAlignment = NSTextAlignmentRight;
        self.endDateLabelLabel.textAlignment = NSTextAlignmentRight;
        self.locationLabelLabel.textAlignment = NSTextAlignmentRight;
    }
    
    [self.navigationController setToolbarHidden:NO animated:NO];
    
    self.eventStore = [[EKEventStore alloc] init];
    
    self.title = NSLocalizedString(@"Event Detail", @"heading for event detail page");
    
    self.titleLabel.text = self.eventTitle;
    NSDateFormatter *datetimeFormatter = [[NSDateFormatter alloc] init];
    [datetimeFormatter setDateStyle:NSDateFormatterMediumStyle];
    [datetimeFormatter setTimeStyle:NSDateFormatterShortStyle];
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
    [dateFormatter setTimeStyle:NSDateFormatterNoStyle];
    
    if(self.allDay) {
        self.startDateLabel.text = [NSString stringWithFormat:NSLocalizedString(@"%@, All Day", @"label for all day event"), [dateFormatter stringFromDate:self.startDate]];
        NSCalendar *calendar = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
        // now build a NSDate object for the next day
        NSDateComponents *offsetComponents = [[NSDateComponents alloc] init];
        [offsetComponents setDay:-1];
        NSDate *nextDate = [calendar dateByAddingComponents:offsetComponents toDate:self.endDate options:0];
        
        self.endDateLabel.text = [NSString stringWithFormat:NSLocalizedString(@"%@, All Day", @"label for all day event"), [dateFormatter stringFromDate:nextDate]];
    } else {
        self.startDateLabel.text = [datetimeFormatter stringFromDate:self.startDate];
        self.endDateLabel.text = [datetimeFormatter stringFromDate:self.endDate];
    }
    
    self.locationLabel.text = self.location;
    self.descriptionTextView.text = self.eventDescription;
    
    self.backgroundView.backgroundColor = [UIColor accentColor];
    self.titleLabel.textColor = [UIColor subheaderTextColor];
    
}

-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    
    UIBarButtonItem *addCalendarButtonItem = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbar-add-to-calendar-icon"] style:UIBarButtonItemStylePlain target:self action:@selector(addToMyCalendar:)];
    
    
    UIBarButtonItem *shareButtonItem = [[UIBarButtonItem alloc]  initWithBarButtonSystemItem:UIBarButtonSystemItemAction target:self action:@selector(share:)];
    
    if ([MFMailComposeViewController canSendMail])
    {
        self.toolbarItems = [ NSArray arrayWithObjects: addCalendarButtonItem, flexibleSpace, shareButtonItem, nil ];
        
    } else {
        self.toolbarItems = [ NSArray arrayWithObjects: addCalendarButtonItem, nil ];
        
    }
     self.navigationController.toolbar.translucent = NO;
    [self.navigationController setToolbarHidden:NO animated:NO];
}

-(void)viewWillDisappear:(BOOL)animated
{
    [self.navigationController setToolbarHidden:YES animated:NO];
    [super viewWillDisappear:animated];
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Course events detail" forModuleNamed:self.module.name];
}

- (IBAction) addToMyCalendar:(id)sender {
    
    [self closePopover];
    
    [self.eventStore requestAccessToEntityType:EKEntityTypeEvent completion:^(BOOL granted, NSError *error)
     {
         [self performSelectorOnMainThread:@selector(addEventToCalendar) withObject:nil waitUntilDone:YES];
     }];
}

-(void) addEventToCalendar
{
    EKEvent * event = [EKEvent eventWithEventStore:self.eventStore];
    event.title     = self.eventTitle;
    event.location  = self.location;
    event.startDate = self.startDate;
    event.endDate   = self.endDate;
    event.notes     = self.eventDescription;
    event.allDay    = self.allDay;
    
    EKEventEditViewController *controller = [[EKEventEditViewController alloc] init];
    
    controller.eventStore       = self.eventStore;
    controller.event            = event;
    controller.editViewDelegate = self;
    
    [self presentViewController:controller animated:YES completion:nil];
    
}

-(void)eventEditViewController:(EKEventEditViewController *)controller
         didCompleteWithAction:(EKEventEditViewAction)action {
    
    switch (action) {
        case EKEventEditViewActionCanceled:
            // User tapped "cancel"
            break;
        case EKEventEditViewActionSaved:
            // User tapped "save"
            break;
        case EKEventEditViewActionDeleted:
            // User tapped "delete"
            break;
        default:
            break;
    }
    
    [self dismissViewControllerAnimated:YES completion:nil];
}

-(NSDateFormatter *)dateFormatterShare
{
    if(_dateFormatterShare == nil) {
        _dateFormatterShare = [[NSDateFormatter alloc] init];
        [_dateFormatterShare setDateStyle:NSDateFormatterShortStyle];
        [_dateFormatterShare setTimeStyle:NSDateFormatterNoStyle];
        [_dateFormatterShare setDoesRelativeDateFormatting:NO];
    }
    return _dateFormatterShare;
}

-(IBAction)share:(id)sender {
    
    NSString* eventDate = [self.dateFormatterShare stringFromDate:self.startDate];

    NSString *text = @"";
    if (eventDate && self.location) {
        text = [NSString stringWithFormat:@"%@\n%@\n%@\n%@", self.titleLabel.text, eventDate, self.location, self.eventDescription];
    } else if (eventDate) {
        text = [NSString stringWithFormat:@"%@\n%@\n%@", self.titleLabel.text, eventDate, self.eventDescription];
    } else if (self.location) {
        text = [NSString stringWithFormat:@"%@\n%@\n%@", self.titleLabel.text, self.location, self.eventDescription];
    } else {
        text = [NSString stringWithFormat:@"%@\n%@", self.titleLabel.text, self.eventDescription];
    }

    NSArray *activityItems = [NSArray arrayWithObjects:text, nil];
    
    UIActivityViewController *avc = [[UIActivityViewController alloc]
                                     initWithActivityItems: activityItems applicationActivities:nil];
    [avc setCompletionHandler:^(NSString *activityType, BOOL completed) {
        NSString *label = [NSString stringWithFormat:@"Tap Share Icon - %@", activityType];
        [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionInvoke_Native withLabel:label withValue:nil forModuleNamed:self.module.name];
        self.popover = nil;
    }];
    
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
        [self presentViewController:avc animated:YES completion:nil];
    }
    else if (self.popover) {
        //The filter picker popover is showing. Hide it.
        [self.popover dismissPopoverAnimated:YES];
        self.popover = nil;
    }
    else {
        self.popover = [[UIPopoverController alloc] initWithContentViewController:avc];
        self.popover.delegate = self;
        self.popover.passthroughViews = nil;
        [self.popover presentPopoverFromBarButtonItem:(UIBarButtonItem *)sender
                             permittedArrowDirections:UIPopoverArrowDirectionDown animated:YES];
    }
    
}
- (void)popoverControllerDidDismissPopover:(UIPopoverController *)popoverController
{
    self.popover = nil;
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    [self closePopover];
}

- (void)closePopover
{
    if (self.popover)
    {
        [self.popover dismissPopoverAnimated:YES];
        self.popover = nil;
    }
}

@end