//
//  RegistrationPlannedSection.h
//  Mobile
//
//  Created by jkh on 11/18/13.
//  Copyright (c) 2013 - 2014 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface RegistrationPlannedSection : NSObject

@property (nonatomic, strong) NSString *sectionId;
@property (nonatomic, strong) NSString *courseId;
@property (nonatomic, strong) NSString *courseName;
@property (nonatomic, strong) NSString *courseSectionNumber;
@property (nonatomic, strong) NSString *sectionTitle;
@property (nonatomic, strong) NSString *courseDescription;
@property (nonatomic, strong) NSNumber *credits;
@property (nonatomic, strong) NSNumber *ceus;
@property (nonatomic, strong) NSString *status;
@property (nonatomic, strong) NSString *gradingType;
@property (nonatomic, strong) NSArray *meetingPatterns;
@property (nonatomic, strong) NSArray *instructors;
@property (nonatomic, strong) NSString *termId;
@property (nonatomic, strong) NSString *classification;
@property (nonatomic, strong) NSNumber *minimumCredits;
@property (nonatomic, strong) NSNumber *maximumCredits;
@property (nonatomic, strong) NSNumber *variableCreditIncrement;

@property (nonatomic, assign) BOOL selectedForRegistration;

-(NSString *)facultyNames;
-(NSString *)meetingPatternDescription;
-(NSString *)instructionalMethod;

@end
