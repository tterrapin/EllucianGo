//
//  NotificationsFetcher.h
//  Mobile
//
//  Created by jkh on 1/24/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>

#define kNotificationsUpdatedNotification @"NotificationsUpdated"

@interface NotificationsFetcher : NSObject

+ (void) fetchNotificationsFromURL:(NSString *) url withManagedObjectContext:(NSManagedObjectContext *)context showLocalNotification:(BOOL)showLocalNotification;
+ (void) fetchNotifications:(NSManagedObjectContext *)managedObjectContext;
@end
