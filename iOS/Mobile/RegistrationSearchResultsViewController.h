//
//  RegistrationSearchResultsViewController.h
//  Mobile
//
//  Created by Jason Hocker on 1/13/14.
//  Copyright (c) 2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "EllucianUITableViewController.h"
#import "DetailSelectionDelegate.h"

@class Module;
@interface RegistrationSearchResultsViewController : EllucianUITableViewController<UITableViewDataSource, UITableViewDelegate, UISplitViewControllerDelegate, UIAlertViewDelegate>

@property (nonatomic, strong) NSMutableArray *courses;
@property (strong, nonatomic) Module *module;
@property (nonatomic, assign) id<DetailSelectionDelegate> detailSelectionDelegate;
@property (nonatomic, assign) BOOL  allowAddToCart;

@end
