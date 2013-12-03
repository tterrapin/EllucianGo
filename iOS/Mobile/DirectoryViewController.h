//
//  DirectoryViewController.h
//  Mobile
//
//  Created by Jason Hocker on 10/4/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Module+Attributes.h"
#import <AddressBookUI/AddressBookUI.h>
#import "EllucianSectionedUITableViewController.h"

#define kDirectoryViewTypeStudent NSLocalizedString(@"Students", @"student search scope in directory")
#define kDirectoryViewTypeFaculty NSLocalizedString(@"Faculty/Staff", @"facilty/staff search scope in directory")
#define kDirectoryViewTypeAll NSLocalizedString(@"All", @"all search scope in directory")

@interface DirectoryViewController : EllucianSectionedUITableViewController<UISearchDisplayDelegate, UITableViewDataSource, UITableViewDelegate>

@property (strong, nonatomic) Module *module;
@property (weak, nonatomic) IBOutlet UISearchBar *searchBar;
@property (strong, nonatomic) NSString *initialQueryString;
@property (nonatomic, strong) NSString *initialScope;
@property (nonatomic, assign) BOOL hideFaculty;
@property (nonatomic, assign) BOOL hideStudents;

@end
