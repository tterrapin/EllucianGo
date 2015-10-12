//
//  CourseAssignmentDetailViewController.m
//  Mobile
//
//  Created by Jason Hocker on 6/6/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import "CourseAssignmentDetailViewController.h"
#import "WebViewController.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "AppearanceChanger.h"
#import "CourseAssignment.h"
#import "Ellucian_GO-Swift.h"


@interface CourseAssignmentDetailViewController ()
@property (nonatomic, strong) AllAssignmentsViewController *masterController;
@property (strong, nonatomic) NSDateFormatter *datetimeFormatter;
@property (strong, nonatomic) NSDateFormatter *datetimeOutputFormatter;
@end

@implementation CourseAssignmentDetailViewController

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
        [self.navigationController setToolbarHidden:NO animated:NO];
    } else if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        [self.padToolBar setHidden:NO];
    }
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
        [self.navigationController setToolbarHidden:NO animated:NO];
    }
    self.navigationController.navigationBar.translucent = NO;
    
    if([AppearanceChanger isIOS8AndRTL]) {
        self.titleLabel.textAlignment = NSTextAlignmentRight;
        self.descriptionTextView.textAlignment = NSTextAlignmentRight;
    }
    
    UIBarButtonItem *reminderButtonItem = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"ilp-reminder"] style:UIBarButtonItemStylePlain target:self action:@selector(createReminder:)];
    
    UIBarButtonItem *shareButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAction target:self action:@selector(takeAction:)];
    
    UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    
    //for courses(iPhone/iPad) and ilp modules(iPhone) add toolbar items to navigation
    if (UI_USER_INTERFACE_IDIOM() != UIUserInterfaceIdiomPad) {
        self.toolbarItems = [ NSArray arrayWithObjects: reminderButtonItem, flexibleSpace, shareButtonItem, nil ];
        self.navigationController.toolbar.translucent = NO;
    //for ilp module on ipad create add items to toolbar created in storyboard
    } else {
        [self.padToolBar setItems:[ NSArray arrayWithObjects: reminderButtonItem, flexibleSpace, shareButtonItem, nil ] animated:NO];
        self.padToolBar.translucent = NO;
        UIImage *registerButtonImage = [UIImage imageNamed:@"Registration Button"];
        [self.padToolBar setBackgroundImage:registerButtonImage forToolbarPosition:UIToolbarPositionBottom barMetrics:UIBarMetricsDefault];
        self.padToolBar.tintColor = [UIColor whiteColor];
    }

    self.titleLabel.text = self.itemTitle;
    if  ( self.courseName && self.courseSectionNumber ) {
        self.courseNameLabel.text = [NSString stringWithFormat:@"%@-%@", self.courseName, self.courseSectionNumber];
    } else {
        self.courseNameLabel.text = @"";
    }
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
    [dateFormatter setTimeStyle:NSDateFormatterShortStyle];
    
    if(self.itemPostDateTime) {
        self.dateLabel.text = [NSString stringWithFormat:NSLocalizedString(@"Due: %@", @"due date label with date"), [dateFormatter stringFromDate:self.itemPostDateTime]];
    } else {
        self.dateLabel.text = NSLocalizedString(@"Due: None assigned", "no due date for assignment");
    }
    self.descriptionTextView.text = self.itemContent;
    
    self.backgroundView.backgroundColor = [UIColor accentColor];

    self.titleLabel.textColor = [UIColor subheaderTextColor];
    self.courseNameLabel.textColor = [UIColor subheaderTextColor];
    self.dateLabel.textColor = [UIColor subheaderTextColor];
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionSearch withLabel:@"ILP Assignments Detail" withValue:nil forModuleNamed:self.module.name];

}

-(void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    if (UI_USER_INTERFACE_IDIOM() != UIUserInterfaceIdiomPad) {
        [self.navigationController setToolbarHidden:YES animated:NO];
    } else {
        [self.padToolBar setHidden:YES];
    }
    
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Course assignments detail" forModuleNamed:self.module.name];
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    
    if ([[segue identifier] isEqualToString:@"Show Website"]) {
        WebViewController *detailController = (WebViewController *)[segue destinationViewController];
        detailController.loadRequest = [[NSURLRequest alloc] initWithURL:[NSURL URLWithString:self.itemLink]];
        detailController.title = self.itemTitle;
        detailController.analyticsLabel = self.module.name;
    } else if ([[segue identifier] isEqualToString:@"Edit Reminder"]) {
        UIViewController *vc = [[segue destinationViewController] childViewControllers][0];
        EditReminderViewController *detailViewController = (EditReminderViewController *)vc;
        detailViewController.reminderTitle = self.itemTitle;
        detailViewController.reminderNotes = [NSString stringWithFormat:@"%@-%@\n%@", self.courseName, self.courseSectionNumber, self.itemContent];
        if(self.itemPostDateTime) {
            detailViewController.reminderDate = self.itemPostDateTime;
            NSString *localizedDue = [NSString stringWithFormat:NSLocalizedString(@"Due: %@", @"due date label with date"), [NSDateFormatter localizedStringFromDate:self.itemPostDateTime dateStyle:NSDateFormatterShortStyle timeStyle:NSDateFormatterShortStyle]];
            detailViewController.reminderNotes = [NSString stringWithFormat:@"%@-%@\n%@\n%@", self.courseName, self.courseSectionNumber, localizedDue, self.itemContent];
        } else {
            detailViewController.reminderNotes = [NSString stringWithFormat:@"%@-%@\n%@", self.courseName, self.courseSectionNumber, self.itemContent];
        }
    }
}

- (IBAction) takeAction:(id)sender {
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionFollow_web withLabel:@"Open assignment in web frame" withValue:nil forModuleNamed:self.module.name];
    [self performSegueWithIdentifier:@"Show Website" sender:sender];
}

-(void)setCourseAssignment:(CourseAssignment *)courseAssignment
{
    if (_courseAssignment != courseAssignment) {
        _courseAssignment = courseAssignment;
        
        [self refreshUI];
    }
}

-(void)refreshUI
{
    _titleLabel.text = _courseAssignment.name;
    _courseNameLabel.text = [NSString stringWithFormat:@"%@-%@", _courseAssignment.courseName, _courseAssignment.courseSectionNumber];
    _descriptionTextView.text = _courseAssignment.assignmentDescription;
    _itemLink = [_courseAssignment.url stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
    [dateFormatter setTimeStyle:NSDateFormatterShortStyle];
    
    _itemPostDateTime = _courseAssignment.dueDate;
    if(_itemPostDateTime) {
        _dateLabel.text = [NSString stringWithFormat:NSLocalizedString(@"Due: %@", @"due date label with date"), [dateFormatter stringFromDate:_itemPostDateTime]];
    } else {
        _dateLabel.text = NSLocalizedString(@"Due: None assigned", "no due date for assignment");
    }
    
    [self.view setNeedsDisplay];
    
}


-(void)selectedDetail:(id)newCourseAssignment withIndex:(NSIndexPath*)myIndex withModule:(Module*)myModule withController:(id)myController
{
    if ( [newCourseAssignment isKindOfClass:[CourseAssignment class]] )
    {
        [self setCourseAssignment:(CourseAssignment *)newCourseAssignment];
        [self setModule:myModule];
        [self.view setNeedsDisplay];
    }
    
}


- (NSDateFormatter *)datetimeFormatter {
    if (_datetimeFormatter == nil) {
        _datetimeFormatter = [[NSDateFormatter alloc] init];
        [_datetimeFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss'Z'"];
        [_datetimeFormatter setTimeZone:[NSTimeZone timeZoneWithName:@"UTC"]];
    }
    return _datetimeFormatter;
}

- (NSDateFormatter *)datetimeOutputFormatter {
    if (_datetimeOutputFormatter == nil) {
        _datetimeOutputFormatter = [[NSDateFormatter alloc] init];
        [_datetimeOutputFormatter setDateStyle:NSDateFormatterShortStyle];
        [_datetimeOutputFormatter setTimeStyle:NSDateFormatterShortStyle];
    }
    return _datetimeOutputFormatter;
}

-(void) createReminder:(id)sender {
    NSString *reminderType = [[NSUserDefaults standardUserDefaults] stringForKey:@"settings-assignments-reminder"];
    if([reminderType isEqualToString:@"Calendar"]) {
        [self addToCalendar];
    } else if ([reminderType isEqualToString:@"Reminders"]) {
        [self addToReminders];
    } else {
        if(NSClassFromString(@"UIAlertController")) {
            UIAlertController *alert = [UIAlertController alertControllerWithTitle:NSLocalizedString(@"Reminder Type", @"Reminder setting title") message:NSLocalizedString(@"What application would you list to use for reminders?", @"Reminder setting message") preferredStyle:UIAlertControllerStyleAlert];
            
            UIAlertAction *calendarAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"Calendar", @"Calendar app name") style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
                [[NSUserDefaults standardUserDefaults] setObject:@"Calendar" forKey:@"settings-assignments-reminder"];
                [self addToCalendar];
            }];
            
            UIAlertAction *reminderAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"Reminders", @"Reminders app name") style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
                [[NSUserDefaults standardUserDefaults] setObject:@"Reminders" forKey:@"settings-assignments-reminder"];
                [self addToReminders];
            }];
            
            [alert addAction:calendarAction];
            [alert addAction:reminderAction];
            
            dispatch_async(dispatch_get_main_queue(),^{
                    [self presentViewController:alert animated:YES completion:nil];
                }); 
        } else {
            UIAlertView *alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Reminder Type", comment:@"Reminder setting title")
                                                            message: NSLocalizedString(@"What application would you list to use for reminders?", @"Reminder setting message")
                                                           delegate:self
                                                  cancelButtonTitle:nil
                                                  otherButtonTitles: NSLocalizedString(@"Calendar", @"Calendar app name"), NSLocalizedString(@"Reminders", @"Reminders app name"), nil];
            alert.tag = 1;
            [alert performSelectorOnMainThread:@selector(show) withObject:nil waitUntilDone:YES];
        }
    }
}

-(void) alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    if(alertView.tag == 1) {
        if(buttonIndex == 1) {
            [[NSUserDefaults standardUserDefaults] setObject:@"Reminders" forKey:@"settings-assignments-reminder"];
            [self addToReminders];
        } else if(buttonIndex == 0) {
            [[NSUserDefaults standardUserDefaults] setObject:@"Calendar" forKey:@"settings-assignments-reminder"];
            [self addToCalendar];
        }
    }
}

-(void) addToCalendar
{
    EKEventStore *eventStore = [EKEventStore new];
    [eventStore requestAccessToEntityType:EKEntityTypeEvent completion: ^(BOOL granted, NSError *error)
     {
         if(granted && error == nil) {
             EKEvent *event = [EKEvent eventWithEventStore:eventStore];
             event.title = self.itemTitle;
             event.location = [NSString stringWithFormat:@"%@-%@", self.courseName, self.courseSectionNumber];
             if(self.itemPostDateTime) {
                 event.startDate = self.itemPostDateTime;
                 event.endDate = self.itemPostDateTime;
             }
             event.notes = self.itemContent;
             event.calendar = eventStore.defaultCalendarForNewEvents;
             
             EKEventEditViewController *controller = [[EKEventEditViewController alloc] init];
             
             controller.eventStore = eventStore;
             controller.event = event;
             controller.editViewDelegate = self;
             
             dispatch_async(dispatch_get_main_queue(),
                            ^{
                                [self presentViewController:controller animated:YES completion:nil];
                            });
         } else {
             UIAlertView * alert = [[UIAlertView alloc ] initWithTitle:NSLocalizedString(@"Permission not granted", @"Permission not granted title")
                                                               message:NSLocalizedString(@"You must give permission in Settings to allow access", @"Permission not granted message")
                                                              delegate:self
                                                     cancelButtonTitle:@"OK"
                                                     otherButtonTitles: nil];
             alert.tag = 2;
             [alert performSelectorOnMainThread:@selector(show) withObject:nil waitUntilDone:YES];
         }
     }];
}

-(void) eventEditViewController:(EKEventEditViewController *)controller didCompleteWithAction:(EKEventEditViewAction)action {
    [self dismissViewControllerAnimated:YES completion:nil];
}

-(void) addToReminders {
    
    EKEventStore *eventStore = [EKEventStore new];
    [eventStore requestAccessToEntityType:EKEntityTypeReminder completion: ^(BOOL granted, NSError *error)
     {
         if(granted && error == nil) {
             [self performSegueWithIdentifier:@"Edit Reminder" sender: self];
         } else {
             [self showPermissionNotGrantedAlert];
         }
     }];
}
        

-(void) showPermissionNotGrantedAlert {
    
    UIAlertController * alertController = [UIAlertController
                                           alertControllerWithTitle: NSLocalizedString(@"Permission not granted", @"Permission not granted title")
                                           message:NSLocalizedString(@"You must give permission in Settings to allow access", @"Permission not granted message")
                                           preferredStyle:UIAlertControllerStyleAlert];
    
    
    UIAlertAction* settingsAction = [UIAlertAction
                                     actionWithTitle:NSLocalizedString(@"Settings", @"Settings application name")
                                     style:UIAlertActionStyleDefault
                                     handler: ^(UIAlertAction * action)
                                     {
                                         if (UIApplicationOpenSettingsURLString != NULL) {
                                             [[UIApplication sharedApplication] openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString]];
                                         }
                                         
                                     }];
    
    UIAlertAction* cancelAction = [UIAlertAction
                                   actionWithTitle:NSLocalizedString(@"Cancel", @"Cancel")
                                   style:UIAlertActionStyleDefault
                                   handler: nil];
    
    [alertController addAction:settingsAction];
    [alertController addAction:cancelAction];
    dispatch_async(dispatch_get_main_queue(),
                   ^{
                       [self presentViewController:alertController animated:YES completion:nil];
                   });
}

@end
