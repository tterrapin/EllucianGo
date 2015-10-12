//
//  ScheduleViewController.h
//  Mobile
//
//  Created by Jason Hocker on 9/25/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>

#import <UIKit/UIKit.h>
#import "Module+Attributes.h"
#import "CoursesCalendarViewController.h"
#import "ScheduleCourseNameCell.h"
#import "ScheduleCell.h"
#import "CourseDetailViewController.h"
#import "CoursesPageSelectionViewController.h"

@interface ScheduleViewController : UIViewController <UIScrollViewDelegate, UITableViewDataSource, UITableViewDelegate, NSFetchedResultsControllerDelegate, ChangePageDelegate, CourseTermDelegate> {
    BOOL pageControlBeingUsed;
}

@property (weak, nonatomic) IBOutlet UIPageControl *pageControl;
@property (weak, nonatomic) IBOutlet UIScrollView *scrollView;
@property (strong, nonatomic) Module *module;
@property (strong, nonatomic) NSFetchedResultsController *fetchedResultsController;

-(IBAction)chooseTermButtonTapped:(id)sender;

@end
