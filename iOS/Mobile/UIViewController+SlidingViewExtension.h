//
//  UIViewController+SlidingViewExtension.h
//  Mobile
//
//  Created by Jason Hocker on 8/30/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SlidingViewController.h"

@interface UIViewController (SlidingViewExtension)

- (SlidingViewController *)slidingViewController;

- (IBAction)revealMenu:(id)sender;

@end
