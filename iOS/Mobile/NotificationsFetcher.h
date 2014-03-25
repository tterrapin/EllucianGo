//
//  NotificationsFetcher.h
//  Mobile
//
//  Created by jkh on 1/24/13.
//  Copyright (c) 2013-2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Notification.h"
#import "Module.h"

#define kNotificationsUpdatedNotification @"NotificationsUpdated"

@interface NotificationsFetcher : NSObject

+ (void) fetchNotificationsFromURL:(NSString *) url withManagedObjectContext:(NSManagedObjectContext *)context showLocalNotification:(BOOL)showLocalNotification;
+ (void) fetchNotifications:(NSManagedObjectContext *)managedObjectContext;
+ (void) deleteNotification:(Notification *)notification module:(Module *)module;
@end
