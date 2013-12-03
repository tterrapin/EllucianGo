//
//  NotificationNoDescriptionDetailViewController
//  Mobile
//
//  Created by Jason Hocker on 9/27/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "NotificationNoDescriptionDetailViewController.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"

@interface NotificationNoDescriptionDetailViewController ()

@end

@implementation NotificationNoDescriptionDetailViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    self.notificationTitleLabel.text = self.notification.title;
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
    [dateFormatter setTimeStyle:NSDateFormatterShortStyle];
    self.notificationDateLabel.text = [dateFormatter stringFromDate:self.notification.noticeDate];


    self.view.backgroundColor = [UIColor accentColor];
    self.notificationTitleLabel.textColor = [UIColor subheaderTextColor];
    self.notificationDateLabel.textColor = [UIColor subheaderTextColor];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    if(self.notification.hyperlink) {
        UIBarButtonItem *twitterButtonItem = [[UIBarButtonItem alloc] initWithTitle:self.notification.linkLabel style:UIBarButtonItemStyleBordered target:self action:@selector(takeAction:)];
        UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
        [self.navigationController setToolbarHidden:NO animated:NO];
        self.toolbarItems = [ NSArray arrayWithObjects: flexibleSpace, twitterButtonItem, flexibleSpace, nil ];
    }
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Notifications Detail" forModuleNamed:self.module.name];
}

- (void)viewDidUnload {
    [self setNotificationDateLabel:nil];
    [super viewDidUnload];
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
    [self.navigationController setToolbarHidden:YES animated:NO];
    [super viewWillDisappear:animated];
}
        
- (IBAction) takeAction:(id)sender {
     [self performSegueWithIdentifier:@"Show Notification Link" sender:sender];
}

@end
