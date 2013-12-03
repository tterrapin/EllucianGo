//
//  ScopedUISearchBar.m
//  Mobile
//
//  Created by jkh on 3/13/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//
//http://stackoverflow.com/questions/3444907/how-to-have-uisearchbar-scope-bar-always-visible

#import "ScopedUISearchBar.h"

@implementation ScopedUISearchBar

- (void) setShowsScopeBar:(BOOL) show
{
    [super setShowsScopeBar: YES]; // always show!
    [self sizeToFit];

}


@end
