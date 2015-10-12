//
//  UIViewController+SlidingViewExtension.h
//  Mobile
//
//  Created by Jason Hocker on 8/30/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ECSlidingViewController.h"

#define kSlidingViewOpenMenuAppearsNotification @"SlidingViewOpenMenuAppearsNotification"
#define kSlidingViewTopResetNotification @"SlidingViewTopResetNotification"
#define kSlidingViewOpenTopControllerNotification @"SlidingViewOpenTopControllerNotification"
#define kSlidingViewChangeTopControllerNotification @"SlidingViewChangeTopControllerNotification"

@interface UIViewController (SlidingViewExtension)

- (IBAction)revealMenu:(id)sender;

@end
