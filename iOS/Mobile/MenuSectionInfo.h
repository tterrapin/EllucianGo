//
//  MenuSectionInfo.h
//  Mobile
//
//  Created by Jason Hocker on 4/10/14.
//  Copyright (c) 2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import <Foundation/Foundation.h>

@class MenuTableViewHeaderFooterView, Module;

@interface MenuSectionInfo : NSObject

@property (getter = isCollapsed) BOOL collapsed;
@property (getter = isCollapseable) BOOL collapseable;
@property NSMutableArray *modules;
@property MenuTableViewHeaderFooterView *headerView;
@property NSString *headerTitle;
@property Module *headerModule;

@end
