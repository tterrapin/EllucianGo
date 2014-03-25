//
//  FeedViewController.h
//  Mobile
//
//  Created by Jason Hocker on 8/17/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Module+Attributes.h"
#import "Feed+Create.h"
#import "FeedDetailViewController.h"
#import "FeedFilterViewController.h"
#import "AsynchronousImageView.h"
#import "EllucianSectionedUITableViewController.h"
#import "DetailSelectionDelegate.h"

@interface FeedViewController : EllucianSectionedUITableViewController<NSFetchedResultsControllerDelegate, UISearchBarDelegate, UISearchDisplayDelegate, FeedFilterDelegate,UIGestureRecognizerDelegate>

@property (strong, nonatomic) NSFetchedResultsController *fetchedResultsController;
@property (strong, nonatomic) NSFetchedResultsController *searchFetchedResultsController;
@property (strong, nonatomic) Module *module;
@property (nonatomic, assign) id<DetailSelectionDelegate> detailSelectionDelegate;


@property (weak, nonatomic) IBOutlet UISearchBar *searchBar;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *filterButton;

-(IBAction)chooseFilterButtonTapped:(id)sender;


@end

