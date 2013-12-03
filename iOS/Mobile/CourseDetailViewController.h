//
//  CourseDetailViewController.h
//  Mobile
//
//  Created by Jason Hocker on 9/26/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MapPOI.h"
#import "UIColor+SchoolCustomization.h"
#import "UIColor+HexString.h"
#import "Module+Attributes.h"

@interface CourseDetailViewController : UIViewController

@property (strong, nonatomic) NSString *termId;
@property (strong, nonatomic) NSString *sectionId;
@property (strong, nonatomic) NSString *courseNameAndSectionNumber;
@property (strong, nonatomic) Module *module;

@property (weak, nonatomic) IBOutlet UITextView *courseDescriptionTextView;
@property (weak, nonatomic) IBOutlet UIView *backgroundView;
@property (weak, nonatomic) IBOutlet UILabel *sectionTitleLabel;
@property (weak, nonatomic) IBOutlet UILabel *dateLabel;
@property (weak, nonatomic) IBOutlet UIView *meetingPatternsView;
@property (weak, nonatomic) IBOutlet UIView *facultyView;

@property (weak, nonatomic) IBOutlet UIView *facultyLabelView;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *facultyLabelViewHeightConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *courseDescriptionTextViewHeightConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *widthConstraint;
@property (weak, nonatomic) IBOutlet UIScrollView *scrollView;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *courseDescriptionTextViewBottomConstraint;

- (IBAction)dismiss:(id)sender;
@end
