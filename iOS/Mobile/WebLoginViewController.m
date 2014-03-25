//
//  WebLoginViewController.m
//  Mobile
//
//  Created by Jason Hocker on 2/28/14.
//  Copyright (c) 2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import "WebLoginViewController.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "HomeViewController.h"
#import "CurrentUser.h"
#import "NotificationManager.h"
#import "LoginExecutor.h"

@interface WebLoginViewController ()

@property (assign, nonatomic) BOOL dismissed;

@end

@implementation WebLoginViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    NSString *urlString = [[NSUserDefaults standardUserDefaults] objectForKey:@"login-web-url"];
    NSURL *url = [[NSURL alloc] initWithString:urlString];
    NSURLRequest *request = [[NSURLRequest alloc] initWithURL:url];
    self.webView.delegate = self;
    self.webView.scalesPageToFit = YES;
    [self.webView loadRequest:request];
}

- (void)webViewDidStartLoad:(UIWebView *)webView
{
    [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
}

- (void)webViewDidFinishLoad:(UIWebView *)webView
{
    [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
    if(_dismissed) return;

    NSString *title = [webView stringByEvaluatingJavaScriptFromString:@"document.title"];
    if([title isEqualToString:@"Authentication Success"]) {
        [self sendEventWithCategory:kAnalyticsCategoryAuthentication withAction:kAnalyticsActionLogin withLabel:@"Authentication using web login" withValue:nil forModuleNamed:nil];
        [LoginExecutor getUserInfo];
        [[NSNotificationCenter defaultCenter] postNotificationName:kLoginExecutorSuccess object:nil];
        
        // register the device if needed
        _dismissed = YES;
        [self dismissViewControllerAnimated:YES completion: nil];
    }
}

- (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error
{
    [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
}

- (IBAction)cancel:(id)sender {
    [self sendEventWithCategory:kAnalyticsCategoryAuthentication withAction:kAnalyticsActionCancel withLabel:@"Click Cancel" withValue:nil forModuleNamed:nil];
    //For cases where the user was previously signed in and timedout and canceled the prompt
    [[CurrentUser sharedInstance] logoutWithoutUpdatingUI];
    [[NSNotificationCenter defaultCenter] postNotificationName:kSignInReturnToHomeNotification object:nil];
    [self dismissViewControllerAnimated:YES completion: nil];
}

@end
