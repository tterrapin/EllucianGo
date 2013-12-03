//
//  SlidingViewController.h
//  Mobile
//
//  Created by Jason Hocker on 8/27/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <QuartzCore/QuartzCore.h>
#import "UIImage+ImageWithUIView.h"

@class MenuViewController;

@interface SlidingViewController : UIViewController <UIGestureRecognizerDelegate> {
    CGPoint startTouchPosition;
    BOOL topViewHasFocus;
}

@property (nonatomic, strong) MenuViewController *menuViewController;
@property (nonatomic, strong) UIViewController *topViewController;
@property (nonatomic, strong) UIPanGestureRecognizer *panGesture;
@property (nonatomic, strong) UITapGestureRecognizer *resetTapGesture;

@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;

- (void)slideTop;
- (void)slideTop:(void(^)())animations onComplete:(void(^)())complete;
- (void)animateTopChange;
- (void)animateTopChange:(void(^)())animations onComplete:(void(^)())complete;
- (void)resetTopView;
- (void)resetTopViewWithAnimations:(void(^)())animations onComplete:(void(^)())complete;
- (BOOL)topViewIsOffScreen;
- (void)showHome;
- (void)showNotifications;

@end
