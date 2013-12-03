//
//  WebViewController.m
//  Mobile
//
//  Created by Jason Hocker on 8/2/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "WebViewController.h"
#import "SafariActivity.h"
#import "CurrentUser.h"
#import "AppDelegate.h"
#import "NSData+AuthenticatedRequest.h"
#import "LoginExecutor.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"

@interface WebViewController ()

@property (nonatomic, strong) NSURL* loadingUrl;
@property (nonatomic, strong) UIActionSheet *actionSheet;
@property (nonatomic, strong) UIPopoverController *popover;
@end

@implementation WebViewController

-(void) viewDidLoad
{
    [super viewDidLoad];
    
    self.navigationController.navigationBar.translucent = NO;
    self.toolbar.translucent = NO;
}
- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];

    [self sendView:@"Display web frame" forModuleNamed:self.analyticsLabel];

    self.webView.backgroundColor = [UIColor underPageBackgroundColor];
    
    AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
    CurrentUser *user = [appDelegate getCurrentUser];
    
    NSDate *lastLoggedInDate = [user lastLoggedInDate];
    
    NSDate* timeNow = [NSDate date];
    
    // If more than 30 minutes, do background login
    if(self.secure)
    {
        if (!lastLoggedInDate || [timeNow timeIntervalSinceDate:lastLoggedInDate] > 1800)
        {
            [self sendEventWithCategory:@"Authentication" withAction:@"Login" withLabel:@"Background re-authenticate" withValue:nil forModuleNamed:self.analyticsLabel];
            
            NSArray *roles;
            NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
            NSString *loginUrl = [defaults objectForKey:@"login-url"];
        
            LoginExecutor *executor = [[LoginExecutor alloc] init];
            NSInteger responseStatusCode = [executor performLogin:loginUrl forUser:[user userauth] andPassword:[user getPassword] andRememberUser:[user remember] returningRoles:&roles];

            if (responseStatusCode != 200 )
            {
                UIAlertView *alert = [[UIAlertView alloc]
                                  initWithTitle:NSLocalizedString(@"Sign In Failed", @"title for failed sign in")
                                  message:NSLocalizedString(@"The password or user name you entered is incorrect. Please try again.", @"message for failed sign in")
                                  delegate:self
                                  cancelButtonTitle:NSLocalizedString(@"OK", @"OK")
                                  otherButtonTitles:nil];
            
                [alert show];
                [user logout];
            }
        
        }
    }
    
    if(nil != self.loadRequest) {
        [self.webView loadRequest:self.loadRequest];
    }
}

- (void)loginUser:(NSData *)data andPassword:(NSString*) password {
    
    NSError *error;
    NSDictionary* json = [NSJSONSerialization
                          JSONObjectWithData:data
                          options:kNilOptions
                          error:&error];
    NSString *userId = [json objectForKey:@"userId"];
    NSString *authId = [json objectForKey:@"authId"];
    NSMutableArray *roles = [json objectForKey:@"roles"];
    
    AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
    CurrentUser *user = [appDelegate getCurrentUser];
    [user login:authId andPassword:password andUserid:userId andRoles:[NSSet setWithArray:roles] andRemember:[user remember]];
}

- (IBAction)didTapBackButton:(id)sender {
    [self closePopover];
    [self.webView goBack];
}

- (IBAction)didTapForwardButton:(id)sender {
    [self closePopover];
    [self.webView goForward];
}
- (IBAction)didTapRefreshButton:(id)sender {
    [self closePopover];
    [self.webView reload];
}
- (IBAction)didTapShareButton:(id)sender {
    
    if (self.popover) {
        //The filter picker popover is showing. Hide it.
        [self.popover dismissPopoverAnimated:YES];
        self.popover = nil;
        return;
    }

    NSArray *activityItems = [NSArray arrayWithObjects:[self.URL absoluteString], nil];
    UIActivityViewController *avc = [[UIActivityViewController alloc]
                                         initWithActivityItems: activityItems applicationActivities:@[[[SafariActivity alloc] init]]];
    [avc setCompletionHandler:^(NSString *activityType, BOOL completed) {
            NSString *label = [NSString stringWithFormat:@"Tap Share Icon - %@", activityType];
            [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionInvoke_Native withLabel:label withValue:nil forModuleNamed:self.analyticsLabel];
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


- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType
{
    self.loadingUrl = [request.mainDocumentURL copy];
    self.backButton.enabled = [self.webView canGoBack];
    self.forwardButton.enabled = [self.webView canGoForward];
    return YES;
}

- (void)webViewDidStartLoad:(UIWebView *)webView
{
    [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
    
    NSInteger buttonIndex = 0;
    for(UIBarButtonItem *button in self.toolbar.items) {
        if(button.tag == 3) {
            NSMutableArray *newItems = [NSMutableArray arrayWithArray:self.toolbar.items];
            [newItems replaceObjectAtIndex:buttonIndex withObject:self.stopButton];
            self.toolbar.items = newItems;
            break;
        }
        ++buttonIndex;
    }
    self.backButton.enabled = [self.webView canGoBack];
    self.forwardButton.enabled = [self.webView canGoForward];
    self.urlTextField.text = [[self.webView.request URL] absoluteString];
    
}

-(void)webViewDidFinishLoad:(UIWebView *)webView {
    self.loadingUrl = nil;
    //self.title = [self.webView stringByEvaluatingJavaScriptFromString:@"document.title"];
    [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
    
    NSInteger buttonIndex = 0;
    for (UIBarButtonItem *button in self.toolbar.items) {
        if(button.tag == 3) {
            NSMutableArray *newItems = [NSMutableArray arrayWithArray:self.toolbar.items];
            [newItems replaceObjectAtIndex:buttonIndex withObject:self.refreshButton];
            self.toolbar.items = newItems;
            break;
        }
        ++buttonIndex;
    }
    self.backButton.enabled = [self.webView canGoBack];
    self.forwardButton.enabled = [self.webView canGoForward];
    self.urlTextField.text = [[self.webView.request URL] absoluteString];
}

-(void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error {
    self.loadingUrl = nil;
    [self webViewDidFinishLoad:self.webView];
    
    if(error.code == NSURLErrorCancelled) {
        return; // this is Error -999
    } else {
    
        NSString *titleString = NSLocalizedString(@"Error Loading Page", @"title when error loading webpage");
        NSString *messageString = [error localizedFailureReason] ? [NSString stringWithFormat:@"%@ %@", [error  localizedDescription], [error localizedFailureReason]] : [error localizedDescription];
        UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:titleString
                                                        message:messageString delegate:self
                                              cancelButtonTitle:NSLocalizedString(@"OK", @"OK") otherButtonTitles:nil];
        [alertView show];
    }   
}

- (IBAction)didTapStopButton:(id)sender {
    [self.webView stopLoading];
}

-(NSURL *)URL {
    return self.loadingUrl ? self.loadingUrl : [self.webView.request mainDocumentURL];
}

-(BOOL)shouldPresentActionSheet:(UIActionSheet *)actionSheet
{
    if(actionSheet == self.actionSheet) {
        [self.actionSheet addButtonWithTitle:NSLocalizedString(@"Open in Safari", @"label to open link in Safari")];
        [self.actionSheet addButtonWithTitle:NSLocalizedString(@"Copy URL", "button label to copy webpage's URL to clipboard")];
        [self.actionSheet addButtonWithTitle:NSLocalizedString(@"Twitter", @"button label to compose tweet on Twitter")];

    }
    return YES;
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
