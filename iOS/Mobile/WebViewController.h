//
//  WebViewController.h
//  Mobile
//
//  Created by Jason Hocker on 8/2/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <Accounts/Accounts.h>

@interface WebViewController : UIViewController <UIWebViewDelegate, UIPopoverControllerDelegate>

@property (nonatomic, strong) NSURLRequest *loadRequest;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *backButton;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *forwardButton;
@property (strong, nonatomic) IBOutlet UIBarButtonItem *refreshButton;

@property (weak, nonatomic) IBOutlet UIBarButtonItem *actionButton;
@property (weak, nonatomic) IBOutlet UIWebView *webView;
@property (weak, nonatomic) IBOutlet UITextField *urlTextField; 
@property (weak, nonatomic) IBOutlet UIToolbar *toolbar;
@property (strong, nonatomic) IBOutlet UIBarButtonItem *stopButton;

@property (strong, nonatomic) NSURL * actionSheetUrl;
@property (nonatomic, assign) BOOL secure;

@property (strong, nonatomic) NSString *analyticsLabel;

- (IBAction)didTapBackButton:(id)sender;
- (IBAction)didTapForwardButton:(id)sender;
- (IBAction)didTapShareButton:(id)sender;
- (IBAction)didTapStopButton:(id)sender;
-(BOOL)shouldPresentActionSheet:(UIActionSheet *)actionSheet;
- (IBAction)didTapRefreshButton:(id)sender;

@end
