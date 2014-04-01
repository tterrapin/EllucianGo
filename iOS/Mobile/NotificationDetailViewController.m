//
//  NotificationDetailViewController.m
//  Mobile
//
//  Created by Jason Hocker on 9/27/12.
//  Copyright (c) 2012-2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import "NotificationDetailViewController.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "AppearanceChanger.h"
#import "NotificationsFetcher.h"
#import "NSMutableURLRequest+BasicAuthentication.h"
#import "Module+Attributes.h"
#import "CurrentUser.h"

@interface NotificationDetailViewController ()

@property (strong, nonatomic) UIBarButtonItem *deleteButtonItem;
@property (nonatomic, strong) UIPopoverController *popover;
@end

@implementation NotificationDetailViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.navigationController.navigationBar.translucent = NO;
    
    if([AppearanceChanger isRTL]) {
        self.notificationDescriptionLabel.textAlignment = NSTextAlignmentRight;
        self.notificationTitleLabel.textAlignment = NSTextAlignmentRight;
        self.notificationDateLabel.textAlignment = NSTextAlignmentRight;
    }
    
    self.backgroundView.backgroundColor = [UIColor accentColor];
    self.notificationTitleLabel.textColor = [UIColor subheaderTextColor];
    self.notificationDateLabel.textColor = [UIColor subheaderTextColor];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    if(self.notification) {
        [self refreshUI];
    }
    
    [self.navigationController setToolbarHidden:NO animated:NO];
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
    UIActionSheet *actionSheet = [[UIActionSheet alloc] initWithTitle:nil
                                                             delegate:self
                                                    cancelButtonTitle:NSLocalizedString(@"Cancel", @"Cancel")
                                               destructiveButtonTitle:NSLocalizedString(@"Delete", @"Delete button")
                                                    otherButtonTitles:nil];
    
    
    if(UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        [actionSheet showFromBarButtonItem:_deleteButtonItem animated:YES];
    } else {
        [actionSheet showFromToolbar:self.navigationController.toolbar];
    }
}

- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex {
    if (buttonIndex == actionSheet.destructiveButtonIndex) {
        [NotificationsFetcher deleteNotification:self.notification module:self.module];
        if(UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
            [self.navigationController popViewControllerAnimated:YES];
        }

    }
}

-(IBAction)share:(id)sender {
    
    
    if (self.popover) {
        //The filter picker popover is showing. Hide it.
        [self.popover dismissPopoverAnimated:YES];
        self.popover = nil;
        return;
    }
    
    NSString *text = self.notification.notificationDescription;
    if(self.notification.hyperlink) {
        text = [NSString stringWithFormat:@"%@ %@", self.notification.notificationDescription, self.notification.hyperlink];
    }
    
    NSArray *activityItems = [NSArray arrayWithObject:text];
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
    [super didRotateFromInterfaceOrientation:fromInterfaceOrientation];
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

-(void) markNotificationRead
{
    //mark deleted on server
    NSString *urlString = [NSString stringWithFormat:@"%@/%@/%@", [self.module propertyForKey:@"mobilenotifications"], [[[CurrentUser sharedInstance] userid] stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding],
                           self.notification.notificationId];

    NSMutableURLRequest * urlRequest = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:urlString]];
    [urlRequest setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];

    NSString *authenticationMode = [[NSUserDefaults standardUserDefaults] objectForKey:@"login-authenticationType"];
    if(!authenticationMode || [authenticationMode isEqualToString:@"native"]) {
        [urlRequest addAuthenticationHeader];
    }
    
    [urlRequest setHTTPMethod:@"POST"];
    
    NSError *jsonError;
    NSDictionary *postDictionary = @{
                                     @"uuid": self.notification.notificationId,
                                     @"statuses": @[@"READ"],
                                     };
    NSData * jsonData = [NSJSONSerialization dataWithJSONObject:postDictionary options:NSJSONWritingPrettyPrinted error:&jsonError];
    [urlRequest setHTTPBody:jsonData];
    
    NSOperationQueue *queue = [[NSOperationQueue alloc] init];
    
    [NSURLConnection sendAsynchronousRequest:urlRequest queue:queue completionHandler:^(NSURLResponse *response, NSData *data, NSError *error) {}];
    
    self.notification.read = @YES;
    NSError *error;
    if (![self.notification.managedObjectContext save:&error]) {
        NSLog(@"Could not record read notification: %@", [error userInfo]);
    }
}

//http://stackoverflow.com/a/13616052/1588409
- (void)viewDidLayoutSubviews {
    dispatch_async(dispatch_get_main_queue(), ^{
        self.notificationTitleLabel.preferredMaxLayoutWidth = [AppearanceChanger sizeInOrientation:self.interfaceOrientation].width - 20;
        self.notificationDescriptionLabel.preferredMaxLayoutWidth = self.notificationDescriptionLabel.bounds.size.width;
    });
}

- (void)dismissMasterPopover
{
    if (_masterPopover != nil) {
        [_masterPopover dismissPopoverAnimated:YES];
    }
}

-(void)selectedDetail:(id)newNotification
           withModule:(Module*)myModule
{
    if ( [newNotification isKindOfClass:[Notification class]] )
    {
        [self setNotification:newNotification];
        [self setModule:myModule];
        
        if (_masterPopover != nil) {
            [_masterPopover dismissPopoverAnimated:YES];
        }
    }
}

-(void)setNotification:(Notification *)notification
{
    if (_notification != notification) {
        _notification = notification;
        
        [self refreshUI];
    }
}

-(void)refreshUI
{
    UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    NSMutableArray *items = [[NSMutableArray alloc] initWithObjects:flexibleSpace, nil];
    if(![self.notification.sticky boolValue]) {
        _deleteButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemTrash target:self action:@selector(deleteNotification:)];
        [items addObject:_deleteButtonItem];
        [items addObject:flexibleSpace];
    }
    UIBarButtonItem *activityButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAction target:self action:@selector(share:)];
    [items addObject:activityButtonItem];
    [items addObject:flexibleSpace];
    
    self.toolbarItems = [items copy];
    //
    self.notificationTitleLabel.text = self.notification.title;
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
    [dateFormatter setTimeStyle:NSDateFormatterNoStyle];
    self.notificationDateLabel.text = [dateFormatter stringFromDate:self.notification.noticeDate];
    self.notificationDescriptionLabel.text = self.notification.notificationDescription;
    
    if(self.notification.hyperlink) {
        [self.notificationActionButton setTitle:self.notification.linkLabel forState:UIControlStateNormal];
        self.notificationActionButton.hidden = NO;
    } else {
        self.notificationActionButton.hidden = YES;
    }
    
    [self.view setNeedsDisplay];

    [self markNotificationRead];
    
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
    [_navBarItem setLeftBarButtonItem:barButtonItem animated:YES];
}

-(void)splitViewController:(UISplitViewController *)svc
    willShowViewController:(UIViewController *)aViewController
 invalidatingBarButtonItem:(UIBarButtonItem *)barButtonItem
{
    //Remove the barButtonItem.
    [_navBarItem setLeftBarButtonItem:nil animated:YES];
    
    //Nil out the pointer to the popover.
    _masterPopover = nil;
}


@end
