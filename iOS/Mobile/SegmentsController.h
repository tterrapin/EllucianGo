//
//  SegmentsController
//  Mobile
//
//  Created by Jason Hocker on 9/28/12
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface SegmentsController : NSObject {
    NSArray                *viewControllers;
    UINavigationController *navigationController;
}

@property (nonatomic, retain, readonly) NSArray                *viewControllers;
@property (nonatomic, retain, readonly) UINavigationController *navigationController;

- (id)initWithNavigationController:(UINavigationController *)aNavigationController
                   viewControllers:(NSArray *)viewControllers;

- (void)indexDidChangeForSegmentedControl:(UISegmentedControl *)aSegmentedControl;

@end
