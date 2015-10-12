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

extern NSString* const kAnalyticsCategoryAuthentication;
extern NSString* const kAnalyticsCategoryCourses;
extern NSString* const kAnalyticsCategoryUI_Action;
extern NSString* const kAnalyticsCategoryPushNotification;
extern NSString* const kAnalyticsCategoryWidget;

extern NSString* const kAnalyticsActionButton_Press;
extern NSString* const kAnalyticsActionCancel;
extern NSString* const kAnalyticsActionFollow_web;
extern NSString* const kAnalyticsActionInvoke_Native;
extern NSString* const kAnalyticsActionList_Select;
extern NSString* const kAnalyticsActionLogin;
extern NSString* const kAnalyticsActionLogout;
extern NSString* const kAnalyticsActionMenu_selection;
extern NSString* const kAnalyticsActionSearch;
extern NSString* const kAnalyticsActionSlide_Action;
extern NSString* const kAnalyticsActionTimeout;
extern NSString* const kAnalyticsActionReceivedMessage;

@interface UIViewController (GoogleAnalyticsTrackerSupport)

- (void)sendView:(NSString *)screen forModuleNamed:(NSString *)moduleName;
- (void)sendViewToTracker1:(NSString *)screen forModuleNamed:(NSString *)moduleName;
- (void)sendViewToTracker2:(NSString *)screen forModuleNamed:(NSString *)moduleName;

- (void)sendEventWithCategory:(NSString *)category withAction:(NSString *)action withLabel:(NSString *)label withValue:(NSNumber *)value forModuleNamed:(NSString *) moduleName;
- (void)sendEventToTracker1WithCategory:(NSString *)category withAction:(NSString *)action withLabel:(NSString *)label withValue:(NSNumber *)value forModuleNamed:(NSString *) moduleName;
- (void)sendEventToTracker2WithCategory:(NSString *)category withAction:(NSString *)action withLabel:(NSString *)label withValue:(NSNumber *)value forModuleNamed:(NSString *) moduleName;


- (void)sendUserTimingWithCategory:(NSString *)category
                  withTimeInterval:(NSTimeInterval)time
                          withName:(NSString *)name
                         withLabel:(NSString *)label
                    forModuleNamed:(NSString *)moduleName;
- (void)sendUserTimingToTracker1WithCategory:(NSString *)category
                            withTimeInterval:(NSTimeInterval)time
                                    withName:(NSString *)name
                                   withLabel:(NSString *)label
                              forModuleNamed:(NSString *)moduleName;
- (void)sendUserTimingToTracker2WithCategory:(NSString *)category
                            withTimeInterval:(NSTimeInterval)time
                                    withName:(NSString *)name
                                   withLabel:(NSString *)label
                              forModuleNamed:(NSString *)moduleName;
@end
