//
//  MenuViewController.h
//  Mobile
//
//  Created by Jason Hocker on 8/27/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SlidingViewController.h"
#import "Module+Create.h"
#import "Module+Attributes.h"
#import "ConfigurationFetcher.h"
#import "SegmentsController.h"
#import "CoursesTabBarViewController.h"
#import "HomeViewController.h"


@interface MenuViewController : UIViewController<UIAlertViewDelegate, UITableViewDelegate,UITableViewDataSource,UIGestureRecognizerDelegate>

@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;
@property (weak, nonatomic) IBOutlet UITableView *tableView;

-(void)reload;
@end
