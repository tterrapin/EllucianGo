//
//  AppearanceChanger.h
//  Mobile
//
//  Created by Jason Hocker on 9/16/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "UIColor+SchoolCustomization.h"

#define SYSTEM_VERSION_EQUAL_TO(v)                  ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] == NSOrderedSame)
#define SYSTEM_VERSION_GREATER_THAN(v)              ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] == NSOrderedDescending)
#define SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(v)  ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] != NSOrderedAscending)
#define SYSTEM_VERSION_LESS_THAN(v)                 ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] == NSOrderedAscending)
#define SYSTEM_VERSION_LESS_THAN_OR_EQUAL_TO(v)     ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] != NSOrderedDescending)

// Used for the UIWebViews in the app.
#define kAppearanceChangerWebViewSystemFontSize 14
#define kAppearanceChangerWebViewSystemFontName @"HelveticaNeue"
#define kAppearanceChangerWebViewSystemFontColor @"black"

@interface AppearanceChanger : NSObject

+(void) applyAppearanceChanges:(UIView *)window;
+ (BOOL) isRTL;
+(CGSize) currentScreenBoundsDependOnOrientation;
@end
