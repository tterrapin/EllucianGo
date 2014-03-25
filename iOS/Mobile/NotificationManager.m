//
//  NotificationRegistration.m
//  Mobile
//
//  Created by Bret Hansen  on 1/14/14.
//  Copyright (c) 2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import "NotificationManager.h"
#import "AppDelegate.h"
#import "CurrentUser.h"
#import "NSMutableURLRequest+BasicAuthentication.h"

@implementation NotificationManager

+(void) registerDeviceIfNeeded
{
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
    NSString* notificationNotificationsUrl = [defaults stringForKey:@"notification-notifications-url"];
    
    AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
    CurrentUser *user = [appDelegate currentUser];
    
    // is notification url is defined and user is logged in
    if (user && [user isLoggedIn] && notificationNotificationsUrl) {
        // check if user change since last registered
        NSString* currentUserId = [user userid];
        NSString* registeredUserId = [defaults stringForKey:@"registered-user-id"];
        BOOL userIdChanged = registeredUserId == NULL || ![currentUserId isEqualToString:registeredUserId];
        
        // no need to register if user hasn't changed
        if (userIdChanged) {
            // register for PUSH notifications, results in didRegisterForRemoteNotificationsWithError or
            // didFailToRegisterForRemoteNotificationsWithError called on AppDelegate
            [[UIApplication sharedApplication] registerForRemoteNotificationTypes:
         (UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound | UIRemoteNotificationTypeAlert)];
        }
    }
}

+(void) registerDeviceToken:(NSData*)deviceToken
{
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
    
    NSString* notificationRegistrationUrl = [defaults stringForKey:@"notification-registration-url"];
    NSString* notificationEnabled = [defaults stringForKey:@"notification-enabled"];

    AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
    CurrentUser *user = [appDelegate currentUser];
    
    NSString* deviceTokenString = [[[NSString stringWithFormat:@"%@", deviceToken] stringByTrimmingCharactersInSet:[NSCharacterSet characterSetWithCharactersInString:@"<>"]] stringByReplacingOccurrencesOfString:@" " withString:@""];
    
    NSString* applicationName = [[[NSBundle mainBundle] infoDictionary] objectForKey:(NSString*)kCFBundleNameKey];
    
    // if urlString is set and either notificationEnabled hasn't been determined or is YES, then attempt to register
    if (notificationRegistrationUrl && (!notificationEnabled || [notificationEnabled boolValue] )) {
        NSMutableDictionary* registerDictionary =
            [
               @{
                   @"devicePushId": deviceTokenString,
                   @"platform": @"ios",
                   @"applicationName": applicationName,
                   @"loginId": user.userauth,
                   @"sisId": user.userid
               } mutableCopy ];
        if (user.email) {
            [registerDictionary setValue: user.email forKey:@"email"];
        }
        
        NSMutableURLRequest* urlRequest = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:notificationRegistrationUrl]];
        [urlRequest setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
        
        // create a plaintext string in the format username:password
        [urlRequest addAuthenticationHeader];
        
        [urlRequest setHTTPMethod:@"POST"];

        NSError *jsonError;
        NSData* jsonData = [NSJSONSerialization dataWithJSONObject:registerDictionary options:(NSJSONWritingOptions)NULL error:&jsonError];
        [urlRequest setHTTPBody:jsonData];

        NSError *error;
        NSURLResponse *response;
        NSData *responseData = [NSURLConnection sendSynchronousRequest:urlRequest returningResponse:&response error:&error];
        
        NSHTTPURLResponse* httpResponse = (NSHTTPURLResponse*)response;
        NSInteger statusCode = [httpResponse statusCode];

        NSDictionary *jsonResponse = [NSJSONSerialization JSONObjectWithData:responseData options:NSJSONReadingMutableContainers error:&error];
        // check if the status is "success" if not we should not continue to attempt to interact with the Notifications API
        if (statusCode != 200) {
            NSLog(@"Device token registration failed status: %li - %@", (long) (long)statusCode, [error localizedDescription]);
        } else {
            NSString* status = [jsonResponse objectForKey:@"status"] ? : @"disabled";
            BOOL enabled = [status isEqualToString:@"success"];
            NSString* notificationEnabled = enabled ? @"YES" : @"NO";
            [defaults setObject:notificationEnabled forKey:@"notification-enabled"];
            
            if (enabled) {
                // remember the registered user, so we re-register if user id changes
                [defaults setObject:[user userid] forKey:@"registered-user-id"];
            }
        }
    }
}

@end
