//
//  UIColor+SchoolCustomization.h
//  Mobile
//
//  Created by Jason Hocker on 10/15/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UIColor (SchoolCustomization)

+ (UIColor *) primaryColor;
+ (UIColor *) headerTextColor;
+ (UIColor *) accentColor;
+ (UIColor *) subheaderTextColor;
+ (BOOL) hasCustomizationColors;
+ (UIColor *) defaultPrimaryColor;
+ (UIColor *) defaultHeaderColor;

@end
