//
//  MobileUIApplication.m
//  Mobile
//
//  Created by Jason Hocker on 11/19/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "MobileUIApplication.h"
#import "GAI.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"

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
    [self sendEventWithCategory:kAnalyticsCategoryAuthentication withAction:kAnalyticsActionTimeout withLabel:@"Password Timeout" withValue:nil];
    [[NSNotificationCenter defaultCenter] postNotificationName:kApplicationDidTimeoutNotification object:nil];
}

- (BOOL)sendEventWithCategory:(NSString *)category
                   withAction:(NSString *)action
                    withLabel:(NSString *)label
                    withValue:(NSNumber *)value
{
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
    NSString *trackingId1 = [defaults objectForKey:@"gaTracker1"];
    NSString *trackingId2 = [defaults objectForKey:@"gaTracker2"];
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    BOOL returnValue = YES;
    if(trackingId1) {
        id tracker1 = [[GAI sharedInstance] trackerWithTrackingId:trackingId1];
        [tracker1 setCustom:1 dimension:configurationName];
        returnValue &= [tracker1 sendEventWithCategory:category withAction:action withLabel:label withValue:value];
    }
    if(trackingId2) {
        id tracker2 = [[GAI sharedInstance] trackerWithTrackingId:trackingId2];
        [tracker2 setCustom:1 dimension:configurationName];
        returnValue &= [tracker2 sendEventWithCategory:category withAction:action withLabel:label withValue:value];
    }
    return returnValue;
}



@end
