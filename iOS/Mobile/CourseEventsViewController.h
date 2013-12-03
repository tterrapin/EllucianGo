//
//  CourseEventsViewController.h
//  Mobile
//
//  Created by jkh on 6/4/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import "EllucianUITableViewController.h"
#import "Module+Attributes.h"
#import <UIKit/UIKit.h>
#import <EventKit/EventKit.h>
#import <EventKitUI/EventKitUI.h>
#import <MessageUI/MessageUI.h>
#import <Twitter/Twitter.h>
#import <Accounts/Accounts.h>
#import "Event.h"
#import "UIColor+SchoolCustomization.h"

@interface CourseEventsViewController : EllucianUITableViewController<NSFetchedResultsControllerDelegate>

@property (strong, nonatomic) NSString *termId;
@property (strong, nonatomic) NSString *sectionId;
@property (strong, nonatomic) NSString *courseNameAndSectionNumber;
@property (strong, nonatomic) Module *module;
@property (strong, nonatomic) NSFetchedResultsController *fetchedResultsController;
- (IBAction)dismiss:(id)sender;

@end