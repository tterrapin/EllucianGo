//
//  CoursesTabBarViewController.m
//  Mobile
//
//  Created by Jason Hocker on 10/3/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "CoursesTabBarViewController.h"

@interface CoursesTabBarViewController ()

@end

@implementation CoursesTabBarViewController

-(void) viewDidLoad
{
    if([self.tabBar respondsToSelector:@selector(setTranslucent:)]) {
        self.tabBar.translucent = NO;
    }
}

@end
