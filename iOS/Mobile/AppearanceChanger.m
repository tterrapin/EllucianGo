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
    
    if (SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"7.0")) {
        
//        UIImage *navBackground = [UIImage imageNamed:@"nav background"];
//        [[UINavigationBar appearance] setBackgroundImage:navBackground forBarMetrics:UIBarMetricsDefault];
//        
        
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
          UITextAttributeTextColor,
          nil]];
        id configurationSelectionSearchBarAppearance = [UISearchBar appearanceWhenContainedIn:[ConfigurationSelectionViewController class], nil];
        [configurationSelectionSearchBarAppearance setBarTintColor: [UIColor defaultPrimaryColor]];
        
    }
    
    else {
        
        UIImage *headerFooterPattern44 = [UIImage imageNamed:@"bg-header-trans"];
        UIImage *searchBarPattern44 = [UIImage imageNamed:@"bg-search-trans"];
        UIImage *barButton30 = [[UIImage imageNamed:@"navbar-button-trans"]
                                resizableImageWithCapInsets:UIEdgeInsetsMake(0, 5, 0, 5)];
        UIImage *navBarBackButton30 = [[UIImage imageNamed:@"navbar-back-button-trans"]
                                       resizableImageWithCapInsets:UIEdgeInsetsMake(0, 13, 0, 5)];
        
        [[UISearchBar appearance] setBackgroundImage:searchBarPattern44];
        [[UISearchBar appearance] setScopeBarBackgroundImage:searchBarPattern44];
        
        [[UIToolbar appearance] setBackgroundImage:headerFooterPattern44
                                forToolbarPosition:UIToolbarPositionAny barMetrics:UIBarMetricsDefault];
        [[UIToolbar appearance] setBackgroundColor:uiColorPrimary];
        
        [[UIBarButtonItem appearance] setBackgroundImage:barButton30 forState:UIControlStateNormal barMetrics:UIBarMetricsDefault];
        [[UIBarButtonItem appearance] setBackButtonBackgroundImage:navBarBackButton30 forState:UIControlStateNormal barMetrics:UIBarMetricsDefault];
        [[UIBarButtonItem appearance] setTintColor:[UIColor whiteColor]];
        
        [[UINavigationBar appearance] setBackgroundImage:headerFooterPattern44 forBarMetrics:UIBarMetricsDefault];
        [[UINavigationBar appearance] setBackgroundColor:uiColorPrimary];
        [[UINavigationBar appearance] setTitleTextAttributes:
         [NSDictionary dictionaryWithObjectsAndKeys: [UIColor defaultHeaderColor], UITextAttributeTextColor, nil]];
        id configurationSelectionNavigationBarAppearance = [UINavigationBar appearanceWhenContainedIn:[ConfigurationSelectionNavigationController class], nil];
        [configurationSelectionNavigationBarAppearance setBackgroundColor:
         [UIColor defaultPrimaryColor]];
        [configurationSelectionNavigationBarAppearance setTitleTextAttributes:
         [NSDictionary dictionaryWithObjectsAndKeys:
          [UIColor defaultHeaderColor],
          UITextAttributeTextColor,
          nil]];
        
        [[UISearchBar appearance] setBackgroundColor:uiColorPrimary];
        id configurationSelectionSearchBarAppearance = [UISearchBar appearanceWhenContainedIn:[ConfigurationSelectionViewController class], nil];
        [configurationSelectionSearchBarAppearance setBackgroundColor: [UIColor defaultPrimaryColor]];
        
        [[UISegmentedControl appearance] setTintColor:uiColorPrimary];
        
        [[UIPageControl appearance] setBackgroundColor:uiColorPrimary];

        [[UISwitch appearance] setOnTintColor:uiColorPrimary];
        
    }
    
    [[CalendarViewDayEventView appearance] setBackgroundColor:uiColorAccentColor];
    [[CalendarViewDayEventView appearance] setFontColor:uiColorSubheaderTextColor];
    
}

+ (BOOL) isRTL
{
    return ([NSLocale characterDirectionForLanguage:[[NSLocale preferredLanguages] objectAtIndex:0]] == NSLocaleLanguageDirectionRightToLeft);
}

+(CGSize) sizeInOrientation:(UIInterfaceOrientation)orientation
{
    CGSize size = [UIScreen mainScreen].bounds.size;
    UIApplication *application = [UIApplication sharedApplication];
    if (UIInterfaceOrientationIsLandscape(orientation))
    {
        size = CGSizeMake(size.height, size.width);
    }
    if (application.statusBarHidden == NO)
    {
        size.height -= MIN(application.statusBarFrame.size.width, application.statusBarFrame.size.height);
    }
    return size;
}


@end
