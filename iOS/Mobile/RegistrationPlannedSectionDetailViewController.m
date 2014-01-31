//
//  RegistrationPlannedSectionDetailViewController.m
//  Mobile
//
//  Created by jkh on 1/6/14.
//  Copyright (c) 2014 Ellucian. All rights reserved.
//

#import "RegistrationPlannedSectionDetailViewController.h"
#import "Module.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"

@interface RegistrationPlannedSectionDetailViewController ()

@end

@implementation RegistrationPlannedSectionDetailViewController

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Registration Section Detail" forModuleNamed:self.module.name];
}

//- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
//{
//    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
//    if (self) {
//        // Custom initialization
//    }
//    return self;
//}
//
//- (void)viewDidLoad
//{
//    [super viewDidLoad];
//	// Do any additional setup after loading the view.
//}
//
//- (void)didReceiveMemoryWarning
//{
//    [super didReceiveMemoryWarning];
//    // Dispose of any resources that can be recreated.
//}
//
//-(BOOL)splitViewController:(UISplitViewController *)svc shouldHideViewController:(UIViewController *)vc inOrientation:(UIInterfaceOrientation)orientation
//{
//    return NO;
//}

@end
