//
//  NotificationRegistration.h
//  Mobile
//
//  Created by Bret Hansen  on 1/14/14.
//  Copyright (c) 2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NotificationManager : NSObject

+(void) registerDeviceIfNeeded;
+(void) registerDeviceToken:(NSData*)deviceToken;

@end
