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
#import "MenuTableViewHeaderFooterView.h"
#import "MenuSectionInfo.h"

@interface MenuViewController : UIViewController<UIAlertViewDelegate, UITableViewDelegate, UITableViewDataSource,UIGestureRecognizerDelegate, SectionHeaderViewDelegate>

@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;
@property (weak, nonatomic) IBOutlet UITableView *tableView;

-(void)reload;
-(void) showModule:(Module*) module;
- (UIViewController*) findControllerByModule:(Module*)module;
-(void) showViewController:(UIViewController*) newTopViewController;
-(BOOL) isSupportedModule:(Module *) module;

@end
