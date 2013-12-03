//
//  Configuration.h
//  Mobile
//
//  Created by jkh on 5/3/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Configuration : NSObject

@property (nonatomic, strong) NSNumber * configurationId;
@property (nonatomic, strong) NSString * configurationUrl;
@property (nonatomic, strong) NSNumber * institutionId;
@property (nonatomic, strong) NSString * institutionName;
@property (nonatomic, strong) NSString * configurationName;
@property (nonatomic, strong) NSSet *keywords;

@end
