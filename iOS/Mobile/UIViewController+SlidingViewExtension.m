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
#import "Ellucian_GO-Swift.h"
#import "UIViewController+ECSlidingViewController.h"

@implementation UIViewController (SlidingViewExtension)

- (IBAction)revealMenu:(id)sender
{
    self.slidingViewController.underLeftViewController.isAccessibilityElement = YES;
    
    UITableViewController *menu = (UITableViewController *)self.slidingViewController.underLeftViewController;
    
    UIAccessibilityPostNotification(UIAccessibilityScreenChangedNotification, menu.tableView);
    
    NSUserDefaults *defaults = [AppGroupUtilities userDefaults];
    [defaults setBool:YES forKey:@"menu-discovered"];
    [defaults synchronize];
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionButton_Press withLabel:@"Click Menu Tray Icon" withValue:nil forModuleNamed:nil];

    if ([UIView respondsToSelector:@selector(userInterfaceLayoutDirectionForSemanticContentAttribute:)]) {
        UIUserInterfaceLayoutDirection direction = [UIView userInterfaceLayoutDirectionForSemanticContentAttribute:self.view.semanticContentAttribute];
        if (direction == UIUserInterfaceLayoutDirectionRightToLeft) {
            [ self.slidingViewController anchorTopViewToLeftAnimated:YES];
        } else {
            [ self.slidingViewController anchorTopViewToRightAnimated:YES];
        }
    } else { // iOS8
        [self.slidingViewController anchorTopViewToRightAnimated:YES];
    }
    
    //TODO remove after soft-deprecated popovers removed
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
