//
//  CoursesPageSelectionViewController.h
//  Mobile
//
//  Created by Jason Hocker on 9/27/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CourseTerm.h"
#import "ChangePageDelegate.h"
#import "Module.h"

@protocol CourseTermDelegate <NSObject>
@required
- (void) resetPopover;
- (void) dismissPopover;
@end

@interface CoursesPageSelectionViewController : UITableViewController <UIPopoverControllerDelegate> {

    //id<ChangePageDelegate> coursesChangePageDelegate;
}

@property (strong, nonatomic) Module *module;
@property(nonatomic,assign) id<ChangePageDelegate> coursesChangePageDelegate;
@property(nonatomic, strong) NSArray *terms;
@property (nonatomic, weak) id<CourseTermDelegate> delegate;

- (IBAction)dismiss:(id)sender;

- (void) sizeForPopover;
@end
