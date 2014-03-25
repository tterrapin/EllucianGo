//
//  FeedDetailViewController.h
//  Mobile
//
//  Created by Jason Hocker on 8/17/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MessageUI/MessageUI.h>
#import <Accounts/Accounts.h>
#import "Feed.h"
#import "AsynchronousImageView.h"
#import "UIColor+SchoolCustomization.h"
#import "Module.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "DetailSelectionDelegate.h"

@interface FeedDetailViewController : UIViewController <UIPopoverControllerDelegate, DetailSelectionDelegate, UISplitViewControllerDelegate>


@property (nonatomic, strong) Feed *feed;

@property (weak, nonatomic) IBOutlet UILabel *dateLabel;
@property (weak, nonatomic) IBOutlet UITextView *descriptionTextView;
@property (weak, nonatomic) IBOutlet UILabel *titleLabel;
@property (weak, nonatomic) IBOutlet UILabel *categoryValue;
@property (weak, nonatomic) IBOutlet AsynchronousImageView *imageView;
@property (nonatomic, weak) IBOutlet UINavigationItem *navBarItem;
@property (nonatomic, strong) UIPopoverController *popover;
@property (nonatomic, strong) UIPopoverController *masterPopover;

@property (weak, nonatomic) IBOutlet UIView *backgroundView;

@property (strong, nonatomic) NSString *itemTitle;
@property (strong, nonatomic) NSString *itemContent;
@property (strong, nonatomic) NSString *itemLink;
@property (strong, nonatomic) NSDate *itemPostDateTime;
@property (strong, nonatomic) NSString *itemImageUrl;
@property (nonatomic, retain) NSSet *itemCategory;

@property (strong, nonatomic) Module *module;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *horizontalSpaceConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *imageWidthConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *imageHeightConstraint;

-(void)dismissMasterPopover;

@end
