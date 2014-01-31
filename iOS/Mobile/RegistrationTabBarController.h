//
//  RegistrationTabBarController.h
//  Mobile
//
//  Created by jkh on 1/8/14.
//  Copyright (c) 2014 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>

#define kRegistrationPlanDataReloaded @"RegistrationPlanDataReloaded"

@class Module;
@class RegistrationPlannedSection;

@interface RegistrationTabBarController : UITabBarController

@property (strong, nonatomic) Module *module;

@property (nonatomic, assign) BOOL registrationAllowed;
@property (nonatomic, strong) NSString *ineligibleMessage;
@property (nonatomic, strong) NSString *planId;
@property (nonatomic, strong) NSArray *terms;


- (void)fetchRegistrationPlans;
-(int) itemsInCartCount;
-(NSString *)termName:(NSString *)termId;
-(NSArray *) sectionsInCart:(NSString *) termId;
-(NSArray *) registeredSections:(NSString *) termId;
-(void) addSearchedSection:(RegistrationPlannedSection *)section;
-(void) removeSearchedSection:(NSString *)sectionId term:(NSString *)termId;

@end
