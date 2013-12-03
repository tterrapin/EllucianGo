//
//  ConfigurationSelectionViewController.h
//  Mobile
//
//  Created by Jason Hocker on 11/26/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ConfigurationSelectionViewController : UITableViewController <UISearchBarDelegate, UISearchDisplayDelegate, UIAlertViewDelegate>

@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;

@end
