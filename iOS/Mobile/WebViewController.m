//
//  WebViewController.m
//  Mobile
//
//  Created by Jason Hocker on 8/2/12.
//  Copyright (c) 2012-2014 Ellucian. All rights reserved.
//

#import "WebViewController.h"
#import "CurrentUser.h"
#import "LoginExecutor.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "LoginViewController.h"
#import <JavaScriptCore/JavaScriptCore.h>
#import "WebViewJavascriptInterface.h"
#import "Ellucian_GO-Swift.h"

@interface WebViewController ()

@property (nonatomic, strong) NSURL* loadingUrl;
@property (nonatomic, strong) UIActionSheet *actionSheet;
@property (nonatomic, strong) UIPopoverController *popover;
@property (nonatomic, strong) NSURL *originalUrlCopy;
@property (nonatomic, strong) JSContext *context;
@end

@implementation WebViewController

-(void) viewDidLoad
{
    [super viewDidLoad];
    
    self.navigationController.navigationBar.translucent = NO;
    self.toolbar.translucent = NO;
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(endEditing:) name:kSlidingViewOpenMenuAppearsNotification object:nil];
}
- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    self.originalUrlCopy = [self.loadRequest.URL copy];

    [self sendView:@"Display web frame" forModuleNamed:self.analyticsLabel];
    
    CurrentUser *user = [CurrentUser sharedInstance];
    
    NSDate *lastLoggedInDate = [user lastLoggedInDate];
    
    NSDate* timeNow = [NSDate date];
    
    // If more than 30 minutes, do background login
    if(self.secure)
    {
        if (!lastLoggedInDate || [timeNow timeIntervalSinceDate:lastLoggedInDate] > 1800)
        {
            [self sendEventWithCategory:@"Authentication" withAction:@"Login" withLabel:@"Background re-authenticate" withValue:nil forModuleNamed:self.analyticsLabel];

            UIViewController *controller = [LoginExecutor loginController].childViewControllers[0];
            if([controller isKindOfClass:[LoginViewController class]]) {
                LoginViewController *loginViewController = (LoginViewController *)controller;
                NSInteger responseStatusCode = [loginViewController backgroundLogin];
                
                if (responseStatusCode != 200 )
                {
                    UIAlertView *alert = [[UIAlertView alloc]
                                          initWithTitle:NSLocalizedString(@"Sign In Failed", @"title for failed sign in")
                                          message:NSLocalizedString(@"The password or user name you entered is incorrect. Please try again.", @"message for failed sign in")
                                          delegate:self
                                          cancelButtonTitle:NSLocalizedString(@"OK", @"OK")
                                          otherButtonTitles:nil];
                    
                    [alert show];
                    [user logout:YES];
                }
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

    CurrentUser *user = [CurrentUser sharedInstance];
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
    [avc setCompletionWithItemsHandler:^(NSString *activityType, BOOL completed, NSArray *returnedItems, NSError *activityError) {
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
    //As soon as we can, get a reference to JSContext from the UIWebView and create the javascript EllucianMobileDevice object
    [self observeJSContext];
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
    
    //Once the page is loaded, call the EllucianMobile method _ellucianMobileInternalReady so its queue can start calling the functions.
    JSValue *jsFunction = self.context[@"EllucianMobile"][@"_ellucianMobileInternalReady"];
    [jsFunction callWithArguments:nil];
    
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
        NSString *messageString = [error localizedFailureReason] ? [NSString stringWithFormat:NSLocalizedStringWithDefaultValue(@"WebView loading error", @"Localizable", [NSBundle mainBundle], @"%@ %@", @"WebView loading error (description failure)"), [error  localizedDescription], [error localizedFailureReason]] : [error localizedDescription];
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

-(NSURL *) originalUrl
{
    return self.originalUrlCopy;
}

-(void) endEditing:(id)sender
{
    [self.webView endEditing:YES];
}

//https://gist.github.com/shuoshi/f1757a7aa7ab8ec67483
-(void)observeJSContext
{
    CFRunLoopRef runLoop = CFRunLoopGetCurrent();
    
    // This is a idle mode of RunLoop, when UIScrollView scrolls, it jumps into "UITrackingRunLoopMode"
    // and won't perform any cache task to keep a smooth scroll.
    CFStringRef runLoopMode = kCFRunLoopDefaultMode;
    
    CFRunLoopObserverRef observer = CFRunLoopObserverCreateWithHandler
    (kCFAllocatorDefault, kCFRunLoopBeforeWaiting, true, 0, ^(CFRunLoopObserverRef observer, CFRunLoopActivity _) {
        JSContext *context = [self.webView valueForKeyPath:@"documentView.webView.mainFrame.javaScriptContext"];
        if (![_context isEqual:context]) {
            CFRunLoopRemoveObserver(runLoop, observer, runLoopMode);
            _context = context;
            
            //Once the JSContext was established, define the EllucianMobileDevice object, and send console.log to the native log.
            self.context[@"EllucianMobileDevice"] = [WebViewJavascriptInterface class];
            self.context[@"console"][@"log"] = ^(NSString *message) {
                NSLog(@"%@", message);
            };
            //once page loads call EllucianMobile._ellucianMobileInternalReady in webViewDidFinishLoad
            //calling here too in case the javascript is ready to receive this call
            NSString * js = @"typeof EllucianMobile != 'undefined' && EllucianMobile._ellucianMobileInternalReady()";
            [self.webView stringByEvaluatingJavaScriptFromString:js];
        }
    });
    
    CFRunLoopAddObserver(runLoop, observer, runLoopMode);
}


@end
