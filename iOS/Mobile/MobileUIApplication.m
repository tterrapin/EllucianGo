//
//  MobileUIApplication.m
//  Mobile
//
//  Created by Jason Hocker on 11/19/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "MobileUIApplication.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "Ellucian_GO-Swift.h"

@implementation MobileUIApplication

//here we are listening for any touch. If the screen receives touch, the timer is reset
-(void)sendEvent:(UIEvent *)event
{
    [super sendEvent:event];
    
    if (!myidleTimer)
    {
        [self resetIdleTimer];
    }
    
    NSSet *allTouches = [event allTouches];
    if ([allTouches count] > 0)
    {
        UITouchPhase phase = ((UITouch *)[allTouches anyObject]).phase;
        if (phase == UITouchPhaseBegan)
        {
            [self resetIdleTimer];
            [[NSNotificationCenter defaultCenter] postNotificationName:kApplicationDidTouchNotification object:nil];
        }
        
    }
}
//as labeled...reset the timer
-(void)resetIdleTimer
{
    if (myidleTimer)
    {
        [myidleTimer invalidate];
    }
    //convert the wait period into seconds rather than minutes
    int timeout = kApplicationTimeoutInMinutes * 60;
    myidleTimer = [NSTimer scheduledTimerWithTimeInterval:timeout target:self selector:@selector(idleTimerExceeded) userInfo:nil repeats:NO];
    
}
//if the timer reaches the limit as defined in kApplicationTimeoutInMinutes, post this notification
-(void)idleTimerExceeded
{
    NSString *authenticationMode = [[AppGroupUtilities userDefaults] objectForKey:@"login-authenticationType"];
    if(!authenticationMode || [authenticationMode isEqualToString:@"native"]) {
        [self sendEventWithCategory:kAnalyticsCategoryAuthentication withAction:kAnalyticsActionTimeout withLabel:@"Password Timeout" withValue:nil];
        [[NSNotificationCenter defaultCenter] postNotificationName:kApplicationDidTimeoutNotification object:nil];
    }
}

- (void)sendEventWithCategory:(NSString *)category
                   withAction:(NSString *)action
                    withLabel:(NSString *)label
                    withValue:(NSNumber *)value
{
    NSUserDefaults* defaults = [AppGroupUtilities userDefaults];
    NSString *trackingId1 = [defaults objectForKey:@"gaTracker1"];
    NSString *trackingId2 = [defaults objectForKey:@"gaTracker2"];
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    GAIDictionaryBuilder *builder = [GAIDictionaryBuilder createEventWithCategory:category action:action label:label value:value];
    [builder set:configurationName forKey:[GAIFields customMetricForIndex:1]];
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
