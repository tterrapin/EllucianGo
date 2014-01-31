//
//  RegistrationCartViewController.h
//  Mobile
//
//  Created by jkh on 11/18/13.
//  Copyright (c) 2013 - 2014 Ellucian. All rights reserved.
//

#import "EllucianSectionedUITableViewController.h"

@class Module;

@interface RegistrationCartViewController : EllucianSectionedUITableViewController<UITableViewDataSource, UITableViewDelegate, UIActionSheetDelegate, UISplitViewControllerDelegate>

@property (strong, nonatomic) Module *module;

@end
