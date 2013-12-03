//
//  UIViewController+GoogleAnalyticsTrackerSupport.m
//  Mobile
//
//  Created by jkh on 7/1/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.

#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "Module+Attributes.h"
#import "GAI.h"

@implementation UIViewController (GoogleAnalyticsTrackerSupport)

- (BOOL)sendView:(NSString *)screen forModuleNamed:(NSString *)moduleName
{
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
    NSString *trackingId1 = [defaults objectForKey:@"gaTracker1"];
    NSString *trackingId2 = [defaults objectForKey:@"gaTracker2"];
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    return [self sendViewToGoogleAnalytics:screen forModuleNamed:moduleName usingTracker1Id:trackingId1 usingTracker2Id:trackingId2 forConfigurationNamed:configurationName];
}

- (BOOL)sendViewToTracker1:(NSString *)screen forModuleNamed:(NSString *)moduleName
{
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
    NSString *trackingId1 = [defaults objectForKey:@"gaTracker1"];
    NSString *trackingId2 = nil;
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    return [self sendViewToGoogleAnalytics:screen forModuleNamed:moduleName usingTracker1Id:trackingId1 usingTracker2Id:trackingId2 forConfigurationNamed:configurationName];
}

- (BOOL)sendViewToTracker2:(NSString *)screen forModuleNamed:(NSString *)moduleName
{
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
    NSString *trackingId1 = nil;
    NSString *trackingId2 = [defaults objectForKey:@"gaTracker2"];
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    return [self sendViewToGoogleAnalytics:screen forModuleNamed:moduleName usingTracker1Id:trackingId1 usingTracker2Id:trackingId2 forConfigurationNamed:configurationName];
}


- (BOOL)sendViewToGoogleAnalytics:(NSString *)screen forModuleNamed:(NSString *)moduleName usingTracker1Id:(NSString *)trackingId1 usingTracker2Id:(NSString *) trackingId2 forConfigurationNamed:(NSString *)configurationName
{
    BOOL returnValue = YES;
    if(trackingId1) {
        id tracker1 = [[GAI sharedInstance] trackerWithTrackingId:trackingId1];
        [tracker1 setCustom:1 dimension:configurationName];
        [tracker1 setCustom:2 dimension:moduleName];
        returnValue &= [tracker1 sendView:screen];
    }
    if(trackingId2) {
        id tracker2 = [[GAI sharedInstance] trackerWithTrackingId:trackingId2];
        [tracker2 setCustom:1 dimension:configurationName];
        [tracker2 setCustom:2 dimension:moduleName];
        returnValue &= [tracker2 sendView:screen];
    }
    return returnValue;
}

- (BOOL)sendEventWithCategory:(NSString *)category
                   withAction:(NSString *)action
                    withLabel:(NSString *)label
                    withValue:(NSNumber *)value forModuleNamed:(NSString *)moduleName
{
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
    NSString *trackingId1 = [defaults objectForKey:@"gaTracker1"];
    NSString *trackingId2 = [defaults objectForKey:@"gaTracker2"];
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    return [self sendEventToGoogleAnalyticsWithCategory:category withAction:action withLabel:label withValue:value forModuleNamed:moduleName usingTracker1Id:trackingId1 usingTracker2Id:trackingId2 forConfigurationNamed:configurationName];
    
}
- (BOOL)sendEventToTracker1WithCategory:(NSString *)category
                             withAction:(NSString *)action
                              withLabel:(NSString *)label
                              withValue:(NSNumber *)value forModuleNamed:(NSString *)moduleName
{
    
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
    NSString *trackingId1 = [defaults objectForKey:@"gaTracker1"];
    NSString *trackingId2 = nil;
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    return [self sendEventToGoogleAnalyticsWithCategory:category withAction:action withLabel:label withValue:value forModuleNamed:moduleName usingTracker1Id:trackingId1 usingTracker2Id:trackingId2 forConfigurationNamed:configurationName];
    
    
}
- (BOOL)sendEventToTracker2WithCategory:(NSString *)category
                             withAction:(NSString *)action
                              withLabel:(NSString *)label
                              withValue:(NSNumber *)value forModuleNamed:(NSString *)moduleName
{
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
    NSString *trackingId1 =  nil;
    NSString *trackingId2 = [defaults objectForKey:@"gaTracker2"];
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    return [self sendEventToGoogleAnalyticsWithCategory:category withAction:action withLabel:label withValue:value forModuleNamed:moduleName usingTracker1Id:trackingId1 usingTracker2Id:trackingId2 forConfigurationNamed:configurationName];
}

- (BOOL)sendEventToGoogleAnalyticsWithCategory:(NSString *)category
                                    withAction:(NSString *)action
                                     withLabel:(NSString *)label
                                     withValue:(NSNumber *)value forModuleNamed:(NSString *)moduleName usingTracker1Id:(NSString *)trackingId1 usingTracker2Id:(NSString *) trackingId2 forConfigurationNamed:(NSString *)configurationName

{
    BOOL returnValue = YES;
    if(trackingId1) {
        id tracker1 = [[GAI sharedInstance] trackerWithTrackingId:trackingId1];
        [tracker1 setCustom:1 dimension:configurationName];
        [tracker1 setCustom:2 dimension:moduleName];
        returnValue &= [tracker1 sendEventWithCategory:category withAction:action withLabel:label withValue:value];
    }
    if(trackingId2) {
        id tracker2 = [[GAI sharedInstance] trackerWithTrackingId:trackingId2];
        [tracker2 setCustom:1 dimension:configurationName];
        [tracker2 setCustom:2 dimension:moduleName];
        returnValue &= [tracker2 sendEventWithCategory:category withAction:action withLabel:label withValue:value];
    }
    return returnValue;
}

@end
