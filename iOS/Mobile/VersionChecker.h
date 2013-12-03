//
//  VersionChecker.h
//  Mobile
//
//  Created by jkh on 3/19/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>

#define kVersionCheckerCurrentNotification @"VersionCheckerCurrentNotification"
#define kVersionCheckerAppNewerNotification @"VersionCheckerAppNewerNotification"
#define kVersionCheckerUpdateAvailableNotification @"VersionCheckerUpdateAvailableNotification"
#define kVersionCheckerOutdatedNotification @"VersionCheckerOutdatedNotification"

@interface VersionChecker : NSObject<UIAlertViewDelegate>

+(BOOL) checkVersion:(NSArray *)supportedVersions;

@end
