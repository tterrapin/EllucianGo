//
//  WebLoginViewController.h
//  Mobile
//
//  Created by Jason Hocker on 2/28/14.
//  Copyright (c) 2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "LoginProtocol.h"

@interface WebLoginViewController : UIViewController<UIWebViewDelegate, LoginProtocol>

@property (weak, nonatomic) IBOutlet UIWebView *webView;
@property (nonatomic, copy) void (^completionBlock)(void);

- (IBAction)cancel:(id)sender;

@end
