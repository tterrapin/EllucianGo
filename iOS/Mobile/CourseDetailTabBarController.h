//
//  CourseDetailTabBarController.h
//  Mobile
//
//  Created by jkh on 2/11/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Module.h"

#define kCourseDetailInformationLoaded @"CourseDetailInformationLoaded"

@interface CourseDetailTabBarController : UITabBarController

@property (strong, nonatomic) Module *module;
@property (readwrite, assign) BOOL isInstructor;
@property (strong, nonatomic) NSString *termId;
@property (strong, nonatomic) NSString *sectionId;

@end
