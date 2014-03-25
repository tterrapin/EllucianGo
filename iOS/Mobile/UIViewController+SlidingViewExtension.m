//
//  UIViewController+SlidingViewExtension.m
//  Mobile
//
//  Created by Jason Hocker on 8/30/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "UIViewController+SlidingViewExtension.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "DetailSelectionDelegate.h"

@implementation UIViewController (SlidingViewExtension)

- (SlidingViewController *)slidingViewController
{
    UIViewController *viewController = self.parentViewController;
    while (!(viewController == nil || [viewController isKindOfClass:[SlidingViewController class]])) {
        viewController = viewController.parentViewController;
    }
    
    return (SlidingViewController *)viewController;
}

- (IBAction)revealMenu:(id)sender
{
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    [defaults setBool:YES forKey:@"menu-discovered"];
    [defaults synchronize];
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionButton_Press withLabel:@"Click Menu Tray Icon" withValue:nil forModuleNamed:nil];
    [self.slidingViewController slideTop];
    
    if ([self isKindOfClass:[UISplitViewController class]])
    {
        UISplitViewController *split = (UISplitViewController *) self;
        UINavigationController *detailNavController = split.viewControllers[1];
        UIViewController *detailController = detailNavController.topViewController;
        
        if( [detailController respondsToSelector:@selector(dismissMasterPopover)] ) {
            UIViewController <DetailSelectionDelegate> *vc = (UIViewController <DetailSelectionDelegate> *) detailController;
            [vc dismissMasterPopover];
        }
    }
}

@end
