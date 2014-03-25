//
//  NotificationsFetcher.m
//  Mobile
//
//  Created by jkh on 1/24/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import "NotificationsFetcher.h"
#import "Notification.h"
#import "NSData+AuthenticatedRequest.h"
#import "Module+Attributes.h"
#import "AppDelegate.h"

@interface NotificationsFetcher ()
@property (strong, nonatomic) NSDateFormatter *dateFormatter;
@end

@implementation NotificationsFetcher

static BOOL lock;

+ (void) fetchNotificationsFromURL:(NSString *) notificationsUrl withManagedObjectContext:(NSManagedObjectContext *)managedObjectContext showLocalNotification:(BOOL)showLocalNotification
{
    NSManagedObjectContext *importContext = [[NSManagedObjectContext alloc] initWithConcurrencyType:NSPrivateQueueConcurrencyType];
    importContext.parentContext = managedObjectContext;
    if(lock) return; //already in progress
    lock = YES;
    [importContext performBlock: ^{

        NSError *error;
        NSURLResponse *response;
        
        [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;

        NSData *responseData = [NSData dataWithContentsOfURLUsingCurrentUser:[NSURL URLWithString:notificationsUrl] returningResponse:&response error:&error];
        
        [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
        NSMutableSet *newKeys = [[NSMutableSet alloc] init];
        NSArray *notifications;
        if(responseData) {
            NSDictionary* json = [NSJSONSerialization
                                  JSONObjectWithData:responseData
                                  options:kNilOptions
                                  error:&error];
            
            NSMutableDictionary *previousNotifications = [[NSMutableDictionary alloc] init];
            NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"Notification"];
            
            NSArray * oldObjects = [importContext executeFetchRequest:request error:&error];
            for (Notification* oldObject in oldObjects) {
                [previousNotifications setObject:oldObject forKey:oldObject.notificationId];
            }
            notifications = [json objectForKey:@"notifications"];
            //create/update objects
            for(NSDictionary *notificationDictionary in notifications) {
                
                NSString *notificationId =  [notificationDictionary objectForKey:@"id"];
                Notification *notification = [previousNotifications objectForKey:notificationId];

                BOOL updateObject = NO;
                NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
                [dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss'Z'"];
                [dateFormatter setTimeZone:[NSTimeZone timeZoneWithName:@"UTC"]];
                
                if(notification) {
                    [previousNotifications removeObjectForKey:notificationId];
                    if(![notification.notificationId isEqualToString:[notificationDictionary objectForKey:@"id"]]) {
                        updateObject = YES;
                    } else if(![notification.title isEqualToString:[notificationDictionary objectForKey:@"title"]]) {
                         updateObject = YES;
                    } else if([notificationDictionary objectForKey:@"description"] != [NSNull null] && ![notification.notificationDescription isEqualToString:[notificationDictionary objectForKey:@"description"]]) {
                        updateObject = YES;
                    } else if([notificationDictionary objectForKey:@"description"] == [NSNull null] && !notification.notificationDescription) {
                        updateObject = YES;
                    } else if([notificationDictionary objectForKey:@"hyperlink"] != [NSNull null] && ![notification.hyperlink isEqualToString:[notificationDictionary objectForKey:@"hyperlink"]]) {
                        updateObject = YES;
                    } else if([notificationDictionary objectForKey:@"hyperlink"] == [NSNull null] && !notification.hyperlink) {
                        updateObject = YES;
                    } else if([notificationDictionary objectForKey:@"linkLabel"] != [NSNull null] && ![notification.linkLabel isEqualToString:[notificationDictionary objectForKey:@"linkLabel"]]) {
                        updateObject = YES;
                    } else if([notificationDictionary objectForKey:@"linkLabel"] == [NSNull null] && !notification.linkLabel) {
                        updateObject = YES;
                    } else if(![notification.noticeDate isEqualToDate:[dateFormatter dateFromString:[notificationDictionary objectForKey:@"noticeDate"]]]) {
                        updateObject = YES;
                    }
                } else {
                     notification = [NSEntityDescription insertNewObjectForEntityForName:@"Notification" inManagedObjectContext:importContext];
                     [newKeys addObject:notificationId];
                    updateObject = YES;
                }
                if(updateObject) {
                    notification.notificationId = [notificationDictionary objectForKey:@"id"];
                    notification.title = [notificationDictionary objectForKey:@"title"];
                    if([notificationDictionary objectForKey:@"description"] != [NSNull null]) {
                        notification.notificationDescription =
                        [[notificationDictionary objectForKey:@"description"] stringByReplacingOccurrencesOfString:@"\\n" withString:@"\n"];
                    }
                    if([notificationDictionary objectForKey:@"hyperlink"] != [NSNull null]) {
                        notification.hyperlink = [notificationDictionary objectForKey:@"hyperlink"];
                    }
                    if([notificationDictionary objectForKey:@"linkLabel"] != [NSNull null]) {
                        notification.linkLabel = [notificationDictionary objectForKey:@"linkLabel"];
                    }

                    notification.noticeDate = [dateFormatter dateFromString:[notificationDictionary objectForKey:@"noticeDate"]];
                }

            }

            for (NSManagedObject * oldObject in [previousNotifications allValues]) {
                [importContext deleteObject:oldObject];
            }
        }
     
        if (![importContext save:&error]) {
            NSLog(@"Could not save to main context after update to notifications: %@", [error userInfo]);
        }
        
        //persist to store and update fetched result controllers
        [importContext.parentContext performBlock:^{
            NSError *parentError = nil;
            if(![importContext.parentContext save:&parentError]) {
                NSLog(@"Could not save to store after update to notifications: %@", [error userInfo]);
            }
        }];
        
        if([newKeys count] > 0) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[NSNotificationCenter defaultCenter] postNotificationName:kNotificationsUpdatedNotification object:nil];

                if(showLocalNotification) {
                    [[UIApplication sharedApplication] cancelAllLocalNotifications];
                    UILocalNotification *localNotification = [[UILocalNotification alloc] init];
                    localNotification.alertBody = NSLocalizedString(@"You have new notifications", @"Message in notification center to alert that they have notifications");
                    localNotification.soundName = UILocalNotificationDefaultSoundName;
                    localNotification.applicationIconBadgeNumber = [notifications count];
                    localNotification.alertAction =  NSLocalizedString(@"View notifications", @"Label for user to start app from a notification in the notificaiton center");
                    [[UIApplication sharedApplication]presentLocalNotificationNow:localNotification];
                }
            });
        }
        lock = NO;
    }
     ];
}

+ (void) fetchNotifications:(NSManagedObjectContext *)managedObjectContext
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"Module" inManagedObjectContext:managedObjectContext];
    [fetchRequest setEntity:entity];

    NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"index" ascending:YES];
    NSArray *sortDescriptors = [NSArray arrayWithObjects:sortDescriptor, nil];
    [fetchRequest setSortDescriptors:sortDescriptors];

    NSError *error;
    NSArray *modules = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
    for(NSManagedObject *managedObject in modules) {
        Module* module = (Module*)managedObject;
        
        if([module.type isEqualToString:@"notifications"]) {
            NSString *urlString = [NSString stringWithFormat:@"%@/%@", [module propertyForKey:@"notifications"], [[CurrentUser userid] stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding]];
            [NotificationsFetcher fetchNotificationsFromURL:urlString withManagedObjectContext:managedObjectContext showLocalNotification:YES];
        }
    }
}

+(void) deleteNotification:(Notification *)notification
{
    NSError *error;
    NSManagedObjectContext *context = notification.managedObjectContext;
    [context deleteObject:notification];
    [context save:&error];
}

@end
