//
//  VersionChecker.m
//  Mobile
//
//  Created by jkh on 3/19/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import "VersionChecker.h"

@implementation VersionChecker

static NSString *latestVersionToCauseAlert;

+(BOOL) checkVersion:(NSArray *)supportedVersions
{
    NSString *plistPath = [[NSBundle mainBundle] pathForResource:@"Customizations" ofType:@"plist"];
    NSDictionary *plistDictionary = [[NSDictionary alloc] initWithContentsOfFile:plistPath];
    
    BOOL enableVersionChecking = YES;
    if([plistDictionary objectForKey:@"Enable Version Checking"]) {
        enableVersionChecking = [plistDictionary[@"Enable Version Checking"] boolValue];
    }
    
    if(!enableVersionChecking) return YES;
    
    //support legacy cloud servers
    BOOL notificationFired = NO;
    if([supportedVersions count] == 0) return YES;
    
    NSString *appVersion = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleVersion"];
    NSArray *appVersionComponents = [appVersion componentsSeparatedByString:@"."];
    NSString *appVersionWithoutBuildNumber = [[appVersionComponents subarrayWithRange:NSMakeRange(0, 3)] componentsJoinedByString:@"."];
    NSString *latestSupportedVersion = [supportedVersions lastObject];
    NSArray *latestSupportedVersionComponents = [latestSupportedVersion componentsSeparatedByString:@"."];
    
    
    //current
    if([[supportedVersions lastObject] isEqualToString:appVersionWithoutBuildNumber]) {
        notificationFired = YES;
       [[NSNotificationCenter defaultCenter] postNotificationName:kVersionCheckerCurrentNotification object:nil];
    } else if ([appVersionComponents count] > 2 && [latestSupportedVersionComponents count] > 2 && [appVersionWithoutBuildNumber isEqualToString:[[latestSupportedVersionComponents subarrayWithRange:NSMakeRange(0, 3)] componentsJoinedByString:@"."]]) {
        notificationFired = YES;
        [[NSNotificationCenter defaultCenter] postNotificationName:kVersionCheckerCurrentNotification object:nil];
        
    } else if([supportedVersions containsObject:appVersionWithoutBuildNumber]) {
        //if user hasn't been alerted, suggest upgrade
        notificationFired = YES;
        //Only tell them once... do not keep showing them the update alert
        if(![latestVersionToCauseAlert isEqualToString:latestSupportedVersion]) {
            latestVersionToCauseAlert = latestSupportedVersion;
            [[NSNotificationCenter defaultCenter] postNotificationName:kVersionCheckerUpdateAvailableNotification object:nil];
        }
    }
    
    //app newer than what server returns
    if([appVersionComponents count] > 0 && [latestSupportedVersionComponents count] > 0 && [[appVersionComponents objectAtIndex:0] intValue] > [[latestSupportedVersionComponents objectAtIndex:0] intValue]) {
        notificationFired = YES;
        [[NSNotificationCenter defaultCenter] postNotificationName:kVersionCheckerAppNewerNotification object:nil];
    } else if([appVersionComponents count] > 0 && [latestSupportedVersionComponents count] > 0 && [[appVersionComponents objectAtIndex:0] intValue] == [[latestSupportedVersionComponents objectAtIndex:0] intValue]) {
        if([appVersionComponents count] > 1 && [latestSupportedVersionComponents count] > 1 && [[appVersionComponents objectAtIndex:1] intValue] > [[latestSupportedVersionComponents objectAtIndex:1] intValue]) {
            notificationFired = YES;
            [[NSNotificationCenter defaultCenter] postNotificationName:kVersionCheckerAppNewerNotification object:nil];
        } else if([appVersionComponents count] > 1 && [latestSupportedVersionComponents count] > 1 && [[appVersionComponents objectAtIndex:1] intValue] == [[latestSupportedVersionComponents objectAtIndex:1] intValue]) {
            if([appVersionComponents count] > 2 && [latestSupportedVersionComponents count] > 2 && [[appVersionComponents objectAtIndex:2] intValue] > [[latestSupportedVersionComponents objectAtIndex:2] intValue]) {
                notificationFired = YES;
                [[NSNotificationCenter defaultCenter] postNotificationName:kVersionCheckerAppNewerNotification object:nil];
            }
        }
    }
    
    if(!notificationFired) {
        [[NSNotificationCenter defaultCenter] postNotificationName:kVersionCheckerOutdatedNotification object:nil];
        return NO;
    } else {
        return YES;
    }
}

@end
