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

typedef NS_ENUM(NSInteger, DirectoryViewType) {
    DirectoryViewTypeUndefined,
    DirectoryViewTypeStudent,
    DirectoryViewTypeFaculty,
    DirectoryViewTypeAll
};


@interface DirectoryViewController : EllucianSectionedUITableViewController<UISearchDisplayDelegate, UITableViewDataSource, UITableViewDelegate>

@property (strong, nonatomic) Module *module;
@property (weak, nonatomic) IBOutlet UISearchBar *searchBar;
@property (strong, nonatomic) NSString *initialQueryString;
@property (nonatomic, assign) DirectoryViewType initialScope;
@property (nonatomic, assign) BOOL hideFaculty;
@property (nonatomic, assign) BOOL hideStudents;

@end
