//
//  AppearanceChanger.m
//  Mobile
//
//  Created by Jason Hocker on 9/16/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "AppearanceChanger.h"
#import "CalendarViewDayEventView.h"
#import "ConfigurationSelectionViewController.h"
#import "UIColor+HexString.h"
#import "ConfigurationSelectionNavigationController.h"

@implementation AppearanceChanger

+(void) applyAppearanceChanges:(UIView *)window
{
    UIColor *uiColorPrimary = [UIColor primaryColor];
    UIColor *uiColorHeaderTextColor = [UIColor headerTextColor];
    UIColor *uiColorAccentColor = [UIColor accentColor];
    UIColor *uiColorSubheaderTextColor = [UIColor subheaderTextColor];
    
    
    [[UINavigationBar appearance] setBarTintColor:uiColorPrimary];
    [[UINavigationBar appearance] setTintColor:[UIColor whiteColor]];
    [[UINavigationBar appearance] setTitleTextAttributes:
     [NSDictionary dictionaryWithObjectsAndKeys:
      uiColorHeaderTextColor,
      NSForegroundColorAttributeName,
      nil]];
    
    [[UISearchBar appearance] setBarTintColor:uiColorPrimary];
    [[UISearchBar appearance] setTintColor : [UIColor blackColor]];
    [[UIBarButtonItem appearanceWhenContainedIn:[UISearchBar class], nil] setTintColor:[UIColor whiteColor]];
    [[UISegmentedControl appearanceWhenContainedIn:[UISearchBar class], nil] setTintColor:[UIColor whiteColor]];
    
    [[UIToolbar appearance] setBarTintColor:uiColorPrimary];
    [[UIToolbar appearance] setTintColor:[UIColor whiteColor]];
    [[UIPageControl appearance] setBackgroundColor:uiColorPrimary];
    
    [[UITabBar appearance] setBarTintColor:uiColorPrimary];
    [[UITabBar appearance] setTintColor:[UIColor whiteColor]];
    
    
    id configurationSelectionNavigationBarAppearance = [UINavigationBar appearanceWhenContainedIn:[ConfigurationSelectionNavigationController class], nil];
    [configurationSelectionNavigationBarAppearance setBarTintColor:
     [UIColor defaultPrimaryColor]];
    [configurationSelectionNavigationBarAppearance setTitleTextAttributes:
     [NSDictionary dictionaryWithObjectsAndKeys:
      [UIColor defaultHeaderColor],
      NSForegroundColorAttributeName,
      nil]];
    id configurationSelectionSearchBarAppearance = [UISearchBar appearanceWhenContainedIn:[ConfigurationSelectionViewController class], nil];
    [configurationSelectionSearchBarAppearance setBarTintColor: [UIColor defaultPrimaryColor]];
    
    
    [[CalendarViewDayEventView appearance] setBackgroundColor:uiColorAccentColor];
    [[CalendarViewDayEventView appearance] setFontColor:uiColorSubheaderTextColor];
    
}

+ (BOOL) isRTL
{
    return ([NSLocale characterDirectionForLanguage:[[NSLocale preferredLanguages] objectAtIndex:0]] == NSLocaleLanguageDirectionRightToLeft);
}

+(CGSize) currentScreenBoundsDependOnOrientation
{
    if(SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"8.0")) {
        return [UIScreen mainScreen].bounds.size;
    }
    
    CGRect screenBounds = [UIScreen mainScreen].bounds ;
    CGFloat width = CGRectGetWidth(screenBounds)  ;
    CGFloat height = CGRectGetHeight(screenBounds) ;
    UIInterfaceOrientation interfaceOrientation = [UIApplication sharedApplication].statusBarOrientation;
    
    if(UIInterfaceOrientationIsPortrait(interfaceOrientation)){
        screenBounds.size = CGSizeMake(width, height);
    } else if(UIInterfaceOrientationIsLandscape(interfaceOrientation)){
        screenBounds.size = CGSizeMake(height, width);
    }
    
    return screenBounds.size ;
}


@end
