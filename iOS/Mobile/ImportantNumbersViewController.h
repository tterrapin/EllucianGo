//
//  ImportantNumbersViewController
//  Mobile
//
//  Created by Jason Hocker on 9/28/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Module+Attributes.h"
#import "ImportantNumbersDirectoryEntry.h"
#import "EllucianSectionedUITableViewController.h"

@interface ImportantNumbersViewController : EllucianSectionedUITableViewController<NSFetchedResultsControllerDelegate, UISearchBarDelegate, UISearchDisplayDelegate>

@property (strong, nonatomic) Module *module;
@property (strong, nonatomic) NSFetchedResultsController *fetchedResultsController;
@property (strong, nonatomic) NSFetchedResultsController *searchFetchedResultsController;
@property (weak, nonatomic) IBOutlet UISearchBar *searchBar;

@end
