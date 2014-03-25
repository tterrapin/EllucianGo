//
//  WebLoginViewController.h
//  Mobile
//
//  Created by Jason Hocker on 2/28/14.
//  Copyright (c) 2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface WebLoginViewController : UIViewController<UIWebViewDelegate>

@property (weak, nonatomic) IBOutlet UIWebView *webView;

- (IBAction)cancel:(id)sender;

@end
