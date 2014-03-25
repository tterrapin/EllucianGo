//
//  NotificationDetailViewController.m
//  Mobile
//
//  Created by Jason Hocker on 9/27/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "NotificationDetailViewController.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "AppearanceChanger.h"
#import "NotificationsFetcher.h"

@interface NotificationDetailViewController ()

@end

@implementation NotificationDetailViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.navigationController.navigationBar.translucent = NO;
    
    if([AppearanceChanger isRTL]) {
        self.notificationDetailTextView.textAlignment = NSTextAlignmentRight;
        self.notificationTitleLabel.textAlignment = NSTextAlignmentRight;
        self.notificationDateLabel.textAlignment = NSTextAlignmentRight;
    }
    
    self.notificationTitleLabel.text = self.notification.title;
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
    [dateFormatter setTimeStyle:NSDateFormatterNoStyle];
    self.notificationDateLabel.text = [dateFormatter stringFromDate:self.notification.noticeDate];
    self.notificationDetailTextView.text = self.notification.notificationDescription;

    self.backgroundView.backgroundColor = [UIColor accentColor];

    self.notificationTitleLabel.textColor = [UIColor subheaderTextColor];
    self.notificationDateLabel.textColor = [UIColor subheaderTextColor];
    
    self.notification.read = @YES;
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    NSMutableArray *items = [[NSMutableArray alloc] initWithObjects:flexibleSpace, nil];
    if(!self.notification.sticky) {
        UIBarButtonItem *deleteButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemTrash target:self action:@selector(deleteNotification:)];
        [items addObject:deleteButtonItem];
        [items addObject:flexibleSpace];
    }
    if(self.notification.hyperlink) {
        UIBarButtonItem *actionButtonItem = [[UIBarButtonItem alloc] initWithTitle:self.notification.linkLabel style:UIBarButtonItemStyleBordered target:self action:@selector(takeAction:)];
        [items addObject:actionButtonItem];
        [items addObject:flexibleSpace];
    }
    [self.navigationController setToolbarHidden:NO animated:NO];
    self.toolbarItems = [items copy];
    self.navigationController.toolbar.translucent = NO;
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Notifications Detail" forModuleNamed:self.module.name];
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    
    if ([[segue identifier] isEqualToString:@"Show Notification Link"]) {
        WebViewController *detailController = (WebViewController *)[segue destinationViewController];
        detailController.loadRequest = [[NSURLRequest alloc] initWithURL:[NSURL URLWithString:self.notification.hyperlink]];
        detailController.title = self.notification.notificationDescription;
        detailController.analyticsLabel = self.module.name;
    }
}

-(void)viewWillDisappear:(BOOL)animated
{
    if(UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
        [self.navigationController setToolbarHidden:YES animated:NO];
    }
    [super viewWillDisappear:animated];
}
        
- (IBAction) takeAction:(id)sender {
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionFollow_web withLabel:@"Open notification in web frame" withValue:nil forModuleNamed:self.module.name];
     [self performSegueWithIdentifier:@"Show Notification Link" sender:sender];
}

- (IBAction) deleteNotification:(id)sender {
    [NotificationsFetcher deleteNotification:self.notification];
    [self.navigationController popViewControllerAnimated:YES];
}


@end
