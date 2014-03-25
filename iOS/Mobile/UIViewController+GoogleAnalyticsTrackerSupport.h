//
//  UIViewController+GoogleAnalyticsTrackerSupport.h
//  Mobile
//
//  Created by jkh on 7/1/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

/*
 Views that want to participate in the tracking of Google Analytics should import this category.
 
 The views and events can be logged by calling the methods in this header.
 
 Example:
 
 -(void) viewDidAppear:(BOOL)animated
 {
    [self sendView:@"About" forModuleNamed:self.module.name];
 }

 */

#import <UIKit/UIKit.h>
#import "GAI.h"
#import "GAIFields.h"
#import "GAIDictionaryBuilder.h"

#define kAnalyticsCategoryAuthentication @"Authentication"
#define kAnalyticsCategoryCourses @"Courses"
#define kAnalyticsCategoryUI_Action @"UI_Action"

#define kAnalyticsActionButton_Press @"Button_Press"
#define kAnalyticsActionCancel @"Cancel"
#define kAnalyticsActionFollow_web @"Follow_web"
#define kAnalyticsActionInvoke_Native @"Invoke_Native"
#define kAnalyticsActionList_Select @"List_Select"
#define kAnalyticsActionLogin @"Login"
#define kAnalyticsActionLogout @"Logout"
#define kAnalyticsActionMenu_selection @"Menu_selection"
#define kAnalyticsActionSearch @"Search"
#define kAnalyticsActionSlide_Action @"Slide_Action"
#define kAnalyticsActionTimeout @"Timeout"


@interface UIViewController (GoogleAnalyticsTrackerSupport)

- (void)sendView:(NSString *)screen forModuleNamed:(NSString *)moduleName;
- (void)sendViewToTracker1:(NSString *)screen forModuleNamed:(NSString *)moduleName;
- (void)sendViewToTracker2:(NSString *)screen forModuleNamed:(NSString *)moduleName;

- (void)sendEventWithCategory:(NSString *)category withAction:(NSString *)action withLabel:(NSString *)label withValue:(NSNumber *)value forModuleNamed:(NSString *) moduleName;
- (void)sendEventToTracker1WithCategory:(NSString *)category withAction:(NSString *)action withLabel:(NSString *)label withValue:(NSNumber *)value forModuleNamed:(NSString *) moduleName;
- (void)sendEventToTracker2WithCategory:(NSString *)category withAction:(NSString *)action withLabel:(NSString *)label withValue:(NSNumber *)value forModuleNamed:(NSString *) moduleName;

@end
