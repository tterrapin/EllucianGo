//
//  FeedFilterViewController.h
//  Mobile
//
//  Created by Jason Hocker on 8/17/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Module.h"
#import "FeedModule.h"

@protocol FeedFilterDelegate <NSObject>
@required
- (void) reloadData;
- (void) resetPopover;
@end

@interface FeedFilterViewController : UITableViewController <UIPopoverControllerDelegate>

- (IBAction)dismiss:(id)sender;
@property (weak, nonatomic) IBOutlet UINavigationBar *navigationBar;
@property (strong, nonatomic) Module *module;
@property (strong, nonatomic) FeedModule *feedModule;
@property (strong, nonatomic) IBOutlet UITableView *tableView;
@property (nonatomic,strong) NSArray *categories;
@property (nonatomic,strong) NSMutableSet *hiddenCategories;
@property (nonatomic, weak) id<FeedFilterDelegate> delegate;

- (void) initializeCategories;
- (void) sizeForPopover;
@end
