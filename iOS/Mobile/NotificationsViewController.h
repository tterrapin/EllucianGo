//
//  NotificationsViewController.h
//  Mobile
//
//  Created by Jason Hocker on 9/27/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Module+Attributes.h"
#import "Notification.h"
#import "NotificationDetailViewController.h"
#import "NotificationsFetcher.h"
#import "NotificationNoDescriptionDetailViewController.h"
#import "EllucianUITableViewController.h"

@interface NotificationsViewController : EllucianUITableViewController<NSFetchedResultsControllerDelegate>

@property (strong, nonatomic) Module *module;
@property (strong, nonatomic) NSFetchedResultsController *fetchedResultsController;

@end
