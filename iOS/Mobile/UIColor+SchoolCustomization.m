//
//  UIColor+SchoolCustomization.m
//  Mobile
//
//  Created by Jason Hocker on 10/15/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "UIColor+SchoolCustomization.h"
#import "UIColor+HexString.h"
#import "Ellucian_GO-Swift.h"

#define kSchoolCustomizationPrimaryColor @"#331640"
#define kSchoolCustomizationHeaderColor @"#FFFFFF"
#define kSchoolCustomizationAccentColor @"#D9C696"
#define kSchoolCustomizationSubheaderColor @"#736357"

@implementation UIColor (SchoolCustomization)

+ (BOOL) hasCustomizationColors
{
    NSUserDefaults *defaults = [AppGroupUtilities userDefaults];
    NSString *color = [defaults objectForKey:@"primaryColor"];
    return color ? YES : NO;
}

+ (UIColor *) primaryColor
{
    NSUserDefaults *defaults = [AppGroupUtilities userDefaults];
    NSString *color = [defaults objectForKey:@"primaryColor"];
    if(!color) color = kSchoolCustomizationPrimaryColor;
    return [UIColor colorWithHexString:color];
}


+ (UIColor *) headerTextColor
{
    NSUserDefaults *defaults = [AppGroupUtilities userDefaults];
    NSString *color = [defaults objectForKey:@"headerTextColor"];
    if(!color) color = kSchoolCustomizationHeaderColor;
    return [UIColor colorWithHexString:color];
}

+ (UIColor *) accentColor
{
    NSUserDefaults *defaults = [AppGroupUtilities userDefaults];
    NSString *color = [defaults objectForKey:@"accentColor"];
    if(!color) color = kSchoolCustomizationAccentColor;
    return [UIColor colorWithHexString:color];
}


+ (UIColor *) subheaderTextColor
{
    NSUserDefaults *defaults = [AppGroupUtilities userDefaults];
    NSString *color = [defaults objectForKey:@"subheaderTextColor"];
    if(!color) color = kSchoolCustomizationSubheaderColor;
    return [UIColor colorWithHexString:color];
}

+ (UIColor *) defaultPrimaryColor
{
    return [UIColor colorWithHexString:kSchoolCustomizationPrimaryColor];
}

+ (UIColor *) defaultHeaderColor
{
    return [UIColor colorWithHexString:kSchoolCustomizationHeaderColor];
}

@end
