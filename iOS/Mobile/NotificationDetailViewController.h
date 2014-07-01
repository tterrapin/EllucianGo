//
//  NotificationDetailViewController.h
//  Mobile
//
//  Created by Jason Hocker on 9/27/12.
//  Copyright (c) 2012-2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Notification.h"
#import "WebViewController.h"
#import "UIColor+SchoolCustomization.h"
#import "Module.h"
#import "DetailSelectionDelegate.h"
#import "NotificationsViewController.h"
#import "EllucianUITableViewController.h"

@interface NotificationDetailViewController : UIViewController<UIActionSheetDelegate, UIPopoverControllerDelegate, DetailSelectionDelegate, UISplitViewControllerDelegate, UIWebViewDelegate>

@property (strong, nonatomic) Module *module;
@property (strong, nonatomic) Notification *notification;
@property (strong, nonatomic) id controller;


@property (weak, nonatomic) IBOutlet UILabel *notificationTitleLabel;
@property (weak, nonatomic) IBOutlet UILabel *notificationDateLabel;
@property (weak, nonatomic) IBOutlet UIView *backgroundView;
@property (weak, nonatomic) IBOutlet UIWebView *notificationDescriptionWebView;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *notificationActionButtonHeightConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *notificationActionButtonBottomSpaceConstraint;
@property (weak, nonatomic) IBOutlet UIButton *notificationActionButton;

@property (nonatomic, weak) IBOutlet UINavigationItem *navBarItem;
@property (nonatomic, strong) UIPopoverController *masterPopover;

- (IBAction)takeAction:(id)sender;
- (void)dismissMasterPopover;

@end
