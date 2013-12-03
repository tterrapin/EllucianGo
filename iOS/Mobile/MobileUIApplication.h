//
//  MobileUIApplication.h
//  Mobile
//
//  Created by Jason Hocker on 11/19/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>

//the length of time before your application "times out".
#define kApplicationTimeoutInMinutes 30

//the notification your AppDelegate needs to watch for in order to know that it has indeed "timed out"
#define kApplicationDidTimeoutNotification @"AppTimeOut"
#define kApplicationDidTouchNotification @"AppTouch"


@interface MobileUIApplication : UIApplication
{
    NSTimer *myidleTimer;
}

-(void)resetIdleTimer;

@end