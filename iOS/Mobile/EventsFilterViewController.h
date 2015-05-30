//
//  EventsFilterViewController.h
//  Mobile
//
//  Created by Jason Hocker on 8/7/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "EventModule.h"
#import "Module.h"

@protocol EventsFilterDelegate <NSObject>
@required
- (void) reloadData;
- (void) resetPopover;
@end

@interface EventsFilterViewController : UITableViewController <UIPopoverControllerDelegate>

@property (strong, nonatomic) Module *module;
@property (strong, nonatomic) EventModule *eventModule;
@property (nonatomic,strong) NSArray *categories;
@property (nonatomic,strong) NSMutableSet *hiddenCategories;
@property (nonatomic, weak) id<EventsFilterDelegate> delegate;

- (void)initializeCategories;
- (void)sizeForPopover;

- (IBAction)dismiss:(id)sender;
@end
