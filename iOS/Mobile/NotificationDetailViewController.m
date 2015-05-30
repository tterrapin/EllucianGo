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
#import "Ellucian_GO-Swift.h"

@interface NotificationDetailViewController ()

@property (strong, nonatomic) UIBarButtonItem *deleteButtonItem;
@property (nonatomic, strong) UIPopoverController *popover;
@property (strong, nonatomic) IBOutlet UIView *maskView;

@end

@implementation NotificationDetailViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(popToRoot:) name:kNotificationsViewControllerItemSelected object:nil];
        self.navigationController.interactivePopGestureRecognizer.enabled = NO;
    }

    [self overlayDisplay];
    
    self.navigationController.navigationBar.translucent = NO;
    
    if([AppearanceChanger isRTL]) {
        self.notificationTitleLabel.textAlignment = NSTextAlignmentRight;
        self.notificationDateLabel.textAlignment = NSTextAlignmentRight;
    }
    
    self.backgroundView.backgroundColor = [UIColor accentColor];
    self.notificationTitleLabel.textColor = [UIColor subheaderTextColor];
    self.notificationDateLabel.textColor = [UIColor subheaderTextColor];
    [self.notificationDescriptionWebView setDelegate:self];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    self.navigationController.toolbarHidden = YES;
    
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
        detailController.title = self.notification.linkLabel;
        detailController.analyticsLabel = self.module.name;
        self.navigationController.toolbarHidden = YES;
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
        text = [NSString stringWithFormat:NSLocalizedStringWithDefaultValue(@"notification detail with hyperlink", @"Localizable", [NSBundle mainBundle], @"%@ %@", @"notification detail with hyperlink"), self.notification.notificationDescription, self.notification.hyperlink];
    }
    
    NSArray *activityItems = [NSArray arrayWithObject:text];
    UIActivityViewController *avc = [[UIActivityViewController alloc]
                                     initWithActivityItems: activityItems applicationActivities:nil];
    [avc setValue:self.notification.title forKey:@"subject"];
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
    if(self.notification.notificationId)
    {
        //mark deleted on server
        NSString *urlString = [NSString stringWithFormat:@"%@/%@/%@", [self.module propertyForKey:@"mobilenotifications"], [[[CurrentUser sharedInstance] userid] stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding],
                               self.notification.notificationId];
        
        NSMutableURLRequest * urlRequest = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:urlString]];
        [urlRequest setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
        
        NSString *authenticationMode = [[AppGroupUtilities userDefaults] objectForKey:@"login-authenticationType"];
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
}

//http://stackoverflow.com/a/13616052/1588409
- (void)viewDidLayoutSubviews {
    dispatch_async(dispatch_get_main_queue(), ^{
        self.notificationTitleLabel.preferredMaxLayoutWidth = [AppearanceChanger currentScreenBoundsDependOnOrientation].width- 20;
    });
}

- (void)dismissMasterPopover
{
    if (_masterPopover != nil) {
        [_masterPopover dismissPopoverAnimated:YES];
    }
}

-(void)selectedDetail:(id)newNotification
            withIndex:(NSIndexPath *)myIndex
           withModule:(Module*)myModule
           withController:(id)myController
{
    if ( [newNotification isKindOfClass:[Notification class]] )
    {
        [self setNotification:newNotification];
        [self setModule:myModule];
        [self setController:myController];
        
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
    if(self.maskView != nil)
    {
        [self.maskView removeFromSuperview];
        self.maskView = nil;
        self.navigationController.toolbarHidden = NO;
    }
    
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
    [self setDescriptionHtml:self.notification.notificationDescription];
    
    NSLog(@"hyperlink: %@", self.notification.hyperlink);
    if(self.notification.hyperlink) {
        [self.notificationActionButton setTitle:self.notification.linkLabel forState:UIControlStateNormal];
        self.notificationActionButton.hidden = NO;
        self.notificationActionButtonHeightConstraint.constant = 43.0;
        self.notificationActionButtonBottomSpaceConstraint.constant = 10.0;
    } else {
        self.notificationActionButton.hidden = YES;
        self.notificationActionButtonHeightConstraint.constant = 0.0;
        self.notificationActionButtonBottomSpaceConstraint.constant = 0.0;
    }
    
    [self.view setNeedsDisplay];

    [self markNotificationRead];
    
}

-(void)setDescriptionHtml:(NSString *) text
{
    // Create a div that the content will reside in formatting for RTL if necessary.
    NSString  *htmlStringwithFont;
    if ([AppearanceChanger isRTL] ){
        htmlStringwithFont = [NSString stringWithFormat:@"<div style=\"font-family: %@; color:%@;font-size: %i; direction:rtl;\" >%@</div>", kAppearanceChangerWebViewSystemFontName, kAppearanceChangerWebViewSystemFontColor, kAppearanceChangerWebViewSystemFontSize, text];
    }
    else{
        htmlStringwithFont = [NSString stringWithFormat:@"<div style=\"font-family: %@; color:%@;font-size: %i\">%@</div>", kAppearanceChangerWebViewSystemFontName, kAppearanceChangerWebViewSystemFontColor, kAppearanceChangerWebViewSystemFontSize, text];
    }
    
    // Replace '\n' characters with <br /> for content that isn't html based to begin with...
    // One issue is if html text also has \n characters in it. In that case we'll be changing the spacing of the content.
    htmlStringwithFont = [htmlStringwithFont stringByReplacingOccurrencesOfString:@"\n" withString:@"<br/>"];
    
    // NaTausha Hansen - I added this code with my daddy! :) ;) 8-) 8-o
    [self.notificationDescriptionWebView loadHTMLString:htmlStringwithFont baseURL:nil];
}

#pragma mark - UIWebViewDelegate

// Delegate method to launch the URL links in an external browser or app. We don't want to just replace
// the contents of this webview with the destination of the URL being clicked on.
- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType
{
    // is not getting called on a touch of the link??
    if (navigationType == UIWebViewNavigationTypeLinkClicked) {
        [[UIApplication sharedApplication] openURL:request.URL];
        return false;
    }
    return true;
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

-(void) overlayDisplay
{
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {

        self.navigationController.toolbarHidden = YES;
        self.maskView = [[UIView alloc] initWithFrame:self.view.bounds];
        [self.maskView setBackgroundColor:[UIColor whiteColor]];
        [self.view addSubview:self.maskView];

        UILabel *label = [UILabel new];
        label.font = [UIFont systemFontOfSize:18];
        label.minimumScaleFactor = .5f;
        label.textAlignment =  NSTextAlignmentCenter;
        label.text = NSLocalizedString(@"No Notifications to display", @"message when there are no notifications to display");

        label.translatesAutoresizingMaskIntoConstraints = NO;
        [self.maskView addSubview:label];
        
        NSLayoutConstraint *xCenterConstraint = [NSLayoutConstraint constraintWithItem:self.maskView attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:label attribute:NSLayoutAttributeCenterX multiplier:1.0 constant:0];
        [self.view addConstraint:xCenterConstraint];
        
        [self.view addConstraints:[NSLayoutConstraint
                                   constraintsWithVisualFormat:@"V:|-60-[label]"
                                   options:NSLayoutFormatDirectionLeadingToTrailing
                                   metrics:nil
                                   views:NSDictionaryOfVariableBindings(label)]];
    }
}

-(void) popToRoot:(id) sender
{
    [self.navigationController popToRootViewControllerAnimated:NO];
}


@end
