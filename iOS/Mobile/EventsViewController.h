//
//  EventsViewController.h
//  Mobile
//
//  Created by Jason Hocker on 7/31/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Module+Attributes.h"
#import "EllucianSectionedUITableViewController.h"
#import "EventsFilterViewController.h"
#import "DetailSelectionDelegate.h"

@interface EventsViewController : EllucianSectionedUITableViewController<NSFetchedResultsControllerDelegate,
    UISearchBarDelegate,
    UISearchDisplayDelegate,
    EventsFilterDelegate>

@property (strong, nonatomic) NSFetchedResultsController *fetchedResultsController;
@property (strong, nonatomic) NSFetchedResultsController *searchFetchedResultsController;
@property (strong, nonatomic) Module *module;
@property (nonatomic, assign) id<DetailSelectionDelegate> detailSelectionDelegate;

@property (weak, nonatomic) IBOutlet UISearchBar *searchBar;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *filterButton;


- (IBAction)chooseFilterButtonTapped:(id)sender;

@end
