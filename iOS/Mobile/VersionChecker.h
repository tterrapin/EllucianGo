//
//  VersionChecker.h
//  Mobile
//
//  Created by jkh on 3/19/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>

extern NSString* const kVersionCheckerCurrentNotification;
extern NSString* const kVersionCheckerAppNewerNotification;
extern NSString* const kVersionCheckerUpdateAvailableNotification;
extern NSString* const kVersionCheckerOutdatedNotification;


@interface VersionChecker : NSObject

+(BOOL) checkVersion:(NSArray *)supportedVersions;

@end
