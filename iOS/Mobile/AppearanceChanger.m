//
//  AppearanceChanger.m
//  Mobile
//
//  Created by Jason Hocker on 9/16/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

//Must stay as objc in iOS 8 because of lack of varargs support to use appearanceWhenContainedIn.

#import "AppearanceChanger.h"
#import "CalendarViewDayEventView.h"
#import "Ellucian_GO-Swift.h"

@implementation AppearanceChanger

+(void) applyAppearanceChanges:(UIView *)window
{
    UIColor *uiColorPrimary = [UIColor primaryColor];
    UIColor *uiColorHeaderTextColor = [UIColor headerTextColor];


    [[UINavigationBar appearance] setBarTintColor:uiColorPrimary];
    [[UINavigationBar appearance] setTintColor:uiColorHeaderTextColor];
    [[UIBarButtonItem appearanceWhenContainedIn:[UINavigationBar class], nil]
     setTintColor:[UIColor whiteColor]];
    [[UINavigationBar appearance] setTitleTextAttributes:
     @{NSForegroundColorAttributeName : uiColorHeaderTextColor }];
    [UINavigationBar appearance].translucent = NO;
    
    [[UISearchBar appearance] setBarTintColor:uiColorPrimary];
    [[UISearchBar appearance] setTintColor : [UIColor blackColor]];
    [[UISegmentedControl appearanceWhenContainedIn:[UISearchBar class], nil] setTintColor:uiColorHeaderTextColor];
    [[UIBarButtonItem appearanceWhenContainedIn:[UISearchBar class], nil] setTitleTextAttributes:@{NSForegroundColorAttributeName: uiColorHeaderTextColor} forState:UIControlStateNormal];
    
    [[UIToolbar appearance] setBarTintColor:[UIColor colorWithRed:.9 green:.9 blue:.9 alpha:1.0]];
    [[UIToolbar appearance] setTintColor:uiColorPrimary];
    [UIToolbar appearance].translucent = NO;
    
    
    [[UITabBar appearance] setBarTintColor:[UIColor colorWithRed:.9 green:.9 blue:.9 alpha:1.0]];
    [[UITabBar appearance] setTintColor:uiColorPrimary];
    [UITabBar appearance].translucent = NO;
    
    [[UIPageControl appearance] setBackgroundColor:uiColorPrimary];

    id configurationSelectionNavigationBarAppearance = [UINavigationBar appearanceWhenContainedIn:[ConfigurationSelectionNavigationController class], nil];
    [configurationSelectionNavigationBarAppearance setBarTintColor:
     [UIColor defaultPrimaryColor]];
    [[UIBarButtonItem appearanceWhenContainedIn:[ConfigurationSelectionNavigationController class], nil]
     setTintColor:[UIColor defaultHeaderColor]];
    [configurationSelectionNavigationBarAppearance setTitleTextAttributes:
     @{NSForegroundColorAttributeName : [UIColor defaultHeaderColor] }];
    id configurationSelectionSearchBarAppearance = [UISearchBar appearanceWhenContainedIn:[ConfigurationSelectionViewController class], nil];
    [configurationSelectionSearchBarAppearance setBarTintColor: [UIColor defaultPrimaryColor]];

//    [[CalendarViewDayEventView appearance] setBackgroundColor:uiColorAccentColor];
//    [[CalendarViewDayEventView appearance] setFontColor:uiColorSubheaderTextColor];
    [[CalendarViewDayEventView appearance] setBackgroundColor:[UIColor colorWithRed:0.30 green:0.30 blue:0.30 alpha:.85]];
    [[CalendarViewDayEventView appearance] setFontColor:[UIColor whiteColor]];

    
}

+ (BOOL) isIOS8AndRTL
{
    if ([UIView respondsToSelector:@selector(userInterfaceLayoutDirectionForSemanticContentAttribute:)]) {
        return NO;
    } else {
        return ([NSLocale characterDirectionForLanguage:[[NSLocale preferredLanguages] objectAtIndex:0]] == NSLocaleLanguageDirectionRightToLeft);
    }
}

//TODO: Delete after all references have been updated to not need this call in iOS 8.
+(CGSize) currentScreenBoundsDependOnOrientation
{
    return [UIScreen mainScreen].bounds.size;
}


@end
