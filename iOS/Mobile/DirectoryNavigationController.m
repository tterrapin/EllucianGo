//
//  DirectoryNavigationController.m
//  Mobile
//
//  Created by jkh on 9/3/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import "DirectoryNavigationController.h"
#import "DirectoryViewController.h"
#import "UIViewController+SlidingViewExtension.h"

@interface DirectoryNavigationController ()

@end

@implementation DirectoryNavigationController

- (IBAction)revealMenu:(id)sender
{
    DirectoryViewController *vc = self.viewControllers[0];
    UISearchBar *bar = vc.searchBar;
    [bar resignFirstResponder];
    [super revealMenu:sender];
}

@end
