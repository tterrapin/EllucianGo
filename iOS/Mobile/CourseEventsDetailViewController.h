//
//  CourseEventsDetailViewController.h
//  Mobile
//
//  Created by jkh on 6/6/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <EventKit/EventKit.h>
#import <EventKitUI/EventKitUI.h>
#import <MessageUI/MessageUI.h>
#import <Accounts/Accounts.h>
#import "Event.h"
#import "UIColor+SchoolCustomization.h"
#import "Module.h"

@interface CourseEventsDetailViewController : UIViewController<EKEventEditViewDelegate,UIPopoverControllerDelegate>


@property (strong, nonatomic) NSString *eventTitle;
@property (strong, nonatomic) NSDate *startDate;
@property (strong, nonatomic) NSDate *endDate;
@property (strong, nonatomic) NSString *location;
@property (strong, nonatomic) NSString *eventDescription;
@property (assign, readwrite) BOOL allDay;

@property (weak, nonatomic) IBOutlet UIView *backgroundView;
@property (weak, nonatomic) IBOutlet UILabel *titleLabel;
@property (weak, nonatomic) IBOutlet UILabel *startDateLabel;
@property (weak, nonatomic) IBOutlet UILabel *endDateLabel;
@property (weak, nonatomic) IBOutlet UITextView *descriptionTextView;
@property (weak, nonatomic) IBOutlet UILabel *locationLabel;
@property (weak, nonatomic) IBOutlet UILabel *startDateLabelLabel;
@property (weak, nonatomic) IBOutlet UILabel *endDateLabelLabel;
@property (weak, nonatomic) IBOutlet UILabel *locationLabelLabel;

@property (strong, nonatomic) Module *module;

@end
