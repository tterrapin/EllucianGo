//
//  RegistrationRegisteredSectionsViewController.h
//  Mobile
//
//  Created by Jason Hocker on 1/24/14.
//  Copyright (c) 2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "EllucianSectionedUITableViewController.h"
#import "DetailSelectionDelegate.h"

@class Module;

@interface RegistrationRegisteredSectionsViewController : EllucianSectionedUITableViewController<UITableViewDataSource, UITableViewDelegate, UIActionSheetDelegate, UISplitViewControllerDelegate>

@property (strong, nonatomic) Module *module;
@property (nonatomic, assign) id<DetailSelectionDelegate> detailSelectionDelegate;

@end
