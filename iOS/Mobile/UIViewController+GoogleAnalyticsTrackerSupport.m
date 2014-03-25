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

@implementation UIViewController (GoogleAnalyticsTrackerSupport)

- (void)sendView:(NSString *)screen forModuleNamed:(NSString *)moduleName
{
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
    NSString *trackingId1 = [defaults objectForKey:@"gaTracker1"];
    NSString *trackingId2 = [defaults objectForKey:@"gaTracker2"];
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    [self sendViewToGoogleAnalytics:screen forModuleNamed:moduleName usingTracker1Id:trackingId1 usingTracker2Id:trackingId2 forConfigurationNamed:configurationName];
}

- (void)sendViewToTracker1:(NSString *)screen forModuleNamed:(NSString *)moduleName
{
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
    NSString *trackingId1 = [defaults objectForKey:@"gaTracker1"];
    NSString *trackingId2 = nil;
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    [self sendViewToGoogleAnalytics:screen forModuleNamed:moduleName usingTracker1Id:trackingId1 usingTracker2Id:trackingId2 forConfigurationNamed:configurationName];
}

- (void)sendViewToTracker2:(NSString *)screen forModuleNamed:(NSString *)moduleName
{
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
    NSString *trackingId1 = nil;
    NSString *trackingId2 = [defaults objectForKey:@"gaTracker2"];
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    [self sendViewToGoogleAnalytics:screen forModuleNamed:moduleName usingTracker1Id:trackingId1 usingTracker2Id:trackingId2 forConfigurationNamed:configurationName];
}


- (void)sendViewToGoogleAnalytics:(NSString *)screen forModuleNamed:(NSString *)moduleName usingTracker1Id:(NSString *)trackingId1 usingTracker2Id:(NSString *) trackingId2 forConfigurationNamed:(NSString *)configurationName
{
    GAIDictionaryBuilder *builder = [GAIDictionaryBuilder createAppView];
    [builder set:configurationName forKey:[GAIFields customMetricForIndex:1]];
    if(moduleName) [builder set:moduleName forKey:[GAIFields customMetricForIndex:2]];
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
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
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
    
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
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
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
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
    [builder set:configurationName forKey:[GAIFields customMetricForIndex:1]];
    [builder set:moduleName forKey:[GAIFields customMetricForIndex:2]];
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
