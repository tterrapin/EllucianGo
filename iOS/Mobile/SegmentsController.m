//
//  SegmentsController.m
//  Mobile
//
//  Created by Jason Hocker on 9/28/12
//  Copyright (c) 2012 Ellucian. All rights reserved.
//
#import "SegmentsController.h"
#import "UIViewController+SlidingViewExtension.h"

@interface SegmentsController ()
@property (nonatomic, retain, readwrite) NSArray                *viewControllers;
@property (nonatomic, retain, readwrite) UINavigationController *navigationController;
@end

@implementation SegmentsController

@synthesize viewControllers, navigationController;

- (id)initWithNavigationController:(UINavigationController *)aNavigationController
                   viewControllers:(NSArray *)theViewControllers {
    if (self = [super init]) {
        self.navigationController   = aNavigationController;
        self.viewControllers = theViewControllers;
    }
    return self;
}

- (void)indexDidChangeForSegmentedControl:(id)a {
    UISegmentedControl *aSegmentedControl = (UISegmentedControl *)a;
    NSUInteger index = aSegmentedControl.selectedSegmentIndex;
    UIViewController * incomingViewController = [self.viewControllers objectAtIndex:index];
    
    NSArray * theViewControllers = [NSArray arrayWithObject:incomingViewController];
    [self.navigationController setViewControllers:theViewControllers animated:NO];
    
    incomingViewController.navigationItem.titleView = aSegmentedControl;
}

- (void)dealloc {
    self.viewControllers = nil;
    self.navigationController = nil;
}

@end
