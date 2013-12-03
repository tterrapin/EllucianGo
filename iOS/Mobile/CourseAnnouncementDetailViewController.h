//
//  CourseAnnouncementDetailViewController.h
//  Mobile
//
//  Created by Jason Hocker on 6/6/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UIColor+SchoolCustomization.h"
#import "Module.h"

@interface CourseAnnouncementDetailViewController : UIViewController

@property (weak, nonatomic) IBOutlet UILabel *dateLabel;
@property (weak, nonatomic) IBOutlet UITextView *descriptionTextView;
@property (weak, nonatomic) IBOutlet UILabel *titleLabel;
@property (weak, nonatomic) IBOutlet UIView *backgroundView;

@property (strong, nonatomic) NSString *itemTitle;
@property (strong, nonatomic) NSString *itemContent;
@property (strong, nonatomic) NSString *itemLink;
@property (strong, nonatomic) NSDate *itemPostDateTime;

@property (strong, nonatomic) Module *module;

@end
