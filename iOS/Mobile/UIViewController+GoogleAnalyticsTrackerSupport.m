//
//  UIViewController+GoogleAnalyticsTrackerSupport.m
//  Mobile
//
//  Created by jkh on 7/1/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.

#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "Module+Attributes.h"
#import "GAI.h"
#import "GAIFields.h"
#import "GAIDictionaryBuilder.h"
#import "Ellucian_GO-Swift.h"

NSString* const kAnalyticsCategoryAuthentication = @"Authentication";
NSString* const kAnalyticsCategoryCourses = @"Courses";
NSString* const kAnalyticsCategoryUI_Action = @"UI_Action";
NSString* const kAnalyticsCategoryPushNotification = @"Push_Notification";
NSString* const kAnalyticsCategoryWidget = @"Widget";

NSString* const kAnalyticsActionButton_Press = @"Button_Press";
NSString* const kAnalyticsActionCancel = @"Cancel";
NSString* const kAnalyticsActionFollow_web = @"Follow_web";
NSString* const kAnalyticsActionInvoke_Native = @"Invoke_Native";
NSString* const kAnalyticsActionList_Select = @"List_Select";
NSString* const kAnalyticsActionLogin = @"Login";
NSString* const kAnalyticsActionLogout = @"Logout";
NSString* const kAnalyticsActionMenu_selection = @"Menu_selection";
NSString* const kAnalyticsActionSearch = @"Search";
NSString* const kAnalyticsActionSlide_Action = @"Slide_Action";
NSString* const kAnalyticsActionTimeout = @"Timeout";
NSString* const kAnalyticsActionReceivedMessage = @"Received_Message";

@implementation UIViewController (GoogleAnalyticsTrackerSupport)

- (void)sendView:(NSString *)screen forModuleNamed:(NSString *)moduleName
{
    NSUserDefaults* defaults = [AppGroupUtilities userDefaults];
    NSString *trackingId1 = [defaults objectForKey:@"gaTracker1"];
    NSString *trackingId2 = [defaults objectForKey:@"gaTracker2"];
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    [self sendViewToGoogleAnalytics:screen forModuleNamed:moduleName usingTracker1Id:trackingId1 usingTracker2Id:trackingId2 forConfigurationNamed:configurationName];
}

- (void)sendViewToTracker1:(NSString *)screen forModuleNamed:(NSString *)moduleName
{
    NSUserDefaults* defaults = [AppGroupUtilities userDefaults];
    NSString *trackingId1 = [defaults objectForKey:@"gaTracker1"];
    NSString *trackingId2 = nil;
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    [self sendViewToGoogleAnalytics:screen forModuleNamed:moduleName usingTracker1Id:trackingId1 usingTracker2Id:trackingId2 forConfigurationNamed:configurationName];
}

- (void)sendViewToTracker2:(NSString *)screen forModuleNamed:(NSString *)moduleName
{
    NSUserDefaults* defaults = [AppGroupUtilities userDefaults];
    NSString *trackingId1 = nil;
    NSString *trackingId2 = [defaults objectForKey:@"gaTracker2"];
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    [self sendViewToGoogleAnalytics:screen forModuleNamed:moduleName usingTracker1Id:trackingId1 usingTracker2Id:trackingId2 forConfigurationNamed:configurationName];
}


- (void)sendViewToGoogleAnalytics:(NSString *)screen forModuleNamed:(NSString *)moduleName usingTracker1Id:(NSString *)trackingId1 usingTracker2Id:(NSString *) trackingId2 forConfigurationNamed:(NSString *)configurationName
{
    GAIDictionaryBuilder *builder = [GAIDictionaryBuilder createScreenView];
    [builder set:configurationName forKey:[GAIFields customDimensionForIndex:1]];
    if(moduleName) [builder set:moduleName forKey:[GAIFields customDimensionForIndex:2]];
    [builder set:screen forKey:kGAIScreenName];
    NSMutableDictionary *buildDictionary = [builder build];
    
    if(trackingId1) {
        id<GAITracker> tracker1 = [[GAI sharedInstance] trackerWithTrackingId:trackingId1];
        [tracker1 send:buildDictionary];
    }
    if(trackingId2) {
        id<GAITracker> tracker2 = [[GAI sharedInstance] trackerWithTrackingId:trackingId2];
        [tracker2 send:buildDictionary];
    }
}

- (void)sendEventWithCategory:(NSString *)category
                   withAction:(NSString *)action
                    withLabel:(NSString *)label
                    withValue:(NSNumber *)value forModuleNamed:(NSString *)moduleName
{
    NSUserDefaults* defaults = [AppGroupUtilities userDefaults];
    NSString *trackingId1 = [defaults objectForKey:@"gaTracker1"];
    NSString *trackingId2 = [defaults objectForKey:@"gaTracker2"];
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    [self sendEventToGoogleAnalyticsWithCategory:category withAction:action withLabel:label withValue:value forModuleNamed:moduleName usingTracker1Id:trackingId1 usingTracker2Id:trackingId2 forConfigurationNamed:configurationName];
    
}
- (void)sendEventToTracker1WithCategory:(NSString *)category
                             withAction:(NSString *)action
                              withLabel:(NSString *)label
                              withValue:(NSNumber *)value forModuleNamed:(NSString *)moduleName
{
    NSUserDefaults* defaults = [AppGroupUtilities userDefaults];
    NSString *trackingId1 = [defaults objectForKey:@"gaTracker1"];
    NSString *trackingId2 = nil;
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    [self sendEventToGoogleAnalyticsWithCategory:category withAction:action withLabel:label withValue:value forModuleNamed:moduleName usingTracker1Id:trackingId1 usingTracker2Id:trackingId2 forConfigurationNamed:configurationName];
    
    
}
- (void)sendEventToTracker2WithCategory:(NSString *)category
                             withAction:(NSString *)action
                              withLabel:(NSString *)label
                              withValue:(NSNumber *)value forModuleNamed:(NSString *)moduleName
{
    NSUserDefaults* defaults = [AppGroupUtilities userDefaults];
    NSString *trackingId1 =  nil;
    NSString *trackingId2 = [defaults objectForKey:@"gaTracker2"];
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    [self sendEventToGoogleAnalyticsWithCategory:category withAction:action withLabel:label withValue:value forModuleNamed:moduleName usingTracker1Id:trackingId1 usingTracker2Id:trackingId2 forConfigurationNamed:configurationName];
}

- (void)sendEventToGoogleAnalyticsWithCategory:(NSString *)category
                                    withAction:(NSString *)action
                                     withLabel:(NSString *)label
                                     withValue:(NSNumber *)value forModuleNamed:(NSString *)moduleName usingTracker1Id:(NSString *)trackingId1 usingTracker2Id:(NSString *) trackingId2 forConfigurationNamed:(NSString *)configurationName

{
    GAIDictionaryBuilder *builder = [GAIDictionaryBuilder createEventWithCategory:category action:action label:label value:value];
    [builder set:configurationName forKey:[GAIFields customDimensionForIndex:1]];
    [builder set:moduleName forKey:[GAIFields customDimensionForIndex:2]];
    NSMutableDictionary *buildDictionary = [builder build];
    
    if(trackingId1) {
        id<GAITracker> tracker1 = [[GAI sharedInstance] trackerWithTrackingId:trackingId1];
        [tracker1 send:buildDictionary];
    }
    if(trackingId2) {
        id<GAITracker> tracker2 = [[GAI sharedInstance] trackerWithTrackingId:trackingId2];
        [tracker2 send:buildDictionary];
    }
}

- (void)sendUserTimingWithCategory:(NSString *)category
                  withTimeInterval:(NSTimeInterval)time
                          withName:(NSString *)name
                         withLabel:(NSString *)label
                    forModuleNamed:(NSString *)moduleName
{
    NSUserDefaults* defaults = [AppGroupUtilities userDefaults];
    NSString *trackingId1 = [defaults objectForKey:@"gaTracker1"];
    NSString *trackingId2 = [defaults objectForKey:@"gaTracker2"];
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    [self sendUserTimingToGoogleAnalyticsWithCategory:category withTimeInterval:time withName:name withLabel:label forModuleNamed:moduleName usingTracker1Id:trackingId1 usingTracker2Id:trackingId2 forConfigurationNamed:configurationName];
    
}

- (void)sendUserTimingToTracker1WithCategory:(NSString *)category
                            withTimeInterval:(NSTimeInterval)time
                                    withName:(NSString *)name
                                   withLabel:(NSString *)label
                              forModuleNamed:(NSString *)moduleName
{
    NSUserDefaults* defaults = [AppGroupUtilities userDefaults];
    NSString *trackingId1 = [defaults objectForKey:@"gaTracker1"];
    NSString *trackingId2 = nil;
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    [self sendUserTimingToGoogleAnalyticsWithCategory:category withTimeInterval:time withName:name withLabel:label forModuleNamed:moduleName usingTracker1Id:trackingId1 usingTracker2Id:trackingId2 forConfigurationNamed:configurationName];
//
    
}
- (void)sendUserTimingToTracker2WithCategory:(NSString *)category
                            withTimeInterval:(NSTimeInterval)time
                                    withName:(NSString *)name
                                   withLabel:(NSString *)label
                              forModuleNamed:(NSString *)moduleName
{
    NSUserDefaults* defaults = [AppGroupUtilities userDefaults];
    NSString *trackingId1 = nil;
    NSString *trackingId2 = [defaults objectForKey:@"gaTracker2"];
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    [self sendUserTimingToGoogleAnalyticsWithCategory:category withTimeInterval:time withName:name withLabel:label forModuleNamed:moduleName usingTracker1Id:trackingId1 usingTracker2Id:trackingId2 forConfigurationNamed:configurationName];
    
    
}

- (void)sendUserTimingToGoogleAnalyticsWithCategory:(NSString *)category
                                          withTimeInterval:(NSTimeInterval)time
                                           withName:(NSString *)name
                                          withLabel:(NSString *)label
                                     forModuleNamed:(NSString *)moduleName
                                    usingTracker1Id:(NSString *)trackingId1
                                    usingTracker2Id:(NSString *) trackingId2
                              forConfigurationNamed:(NSString *)configurationName

{
    NSNumber *interval = [NSNumber numberWithInt:((int)(time*1000))];
    GAIDictionaryBuilder *builder = [GAIDictionaryBuilder createTimingWithCategory:category interval:interval name:name label:label];
    [builder set:configurationName forKey:[GAIFields customDimensionForIndex:1]];
    [builder set:moduleName forKey:[GAIFields customDimensionForIndex:2]];
    NSMutableDictionary *buildDictionary = [builder build];
    
    if(trackingId1) {
        id<GAITracker> tracker1 = [[GAI sharedInstance] trackerWithTrackingId:trackingId1];
        [tracker1 send:buildDictionary];
    }
    if(trackingId2) {
        id<GAITracker> tracker2 = [[GAI sharedInstance] trackerWithTrackingId:trackingId2];
        [tracker2 send:buildDictionary];
    }
}


@end
