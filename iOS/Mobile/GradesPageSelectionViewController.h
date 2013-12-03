//
//  GradesPageSelectionViewController.h
//  Mobile
//
//  Created by Jason Hocker on 9/27/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "GradeTerm.h"
#import "ChangePageDelegate.h"
#import "Module.h"

@protocol GradeTermDelegate <NSObject>
@required
- (void) resetPopover;
- (void) dismissPopover;
@end

@interface GradesPageSelectionViewController : UITableViewController <UIPopoverControllerDelegate>{

    //id<ChangePageDelegate> gradesChangePageDelegate;
}

@property(nonatomic,assign) id<ChangePageDelegate> gradesChangePageDelegate;
@property(nonatomic, strong) NSArray *terms;
@property(nonatomic, strong) Module *module;
@property (nonatomic, weak) id<GradeTermDelegate> delegate;
- (IBAction)dismiss:(id)sender;

- (void) sizeForPopover;
@end
