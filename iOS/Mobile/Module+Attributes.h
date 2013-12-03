//
//  Module+Attributes.h
//  Mobile
//
//  Created by Jason Hocker on 7/31/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "Module.h"

@interface Module (Attributes)

@property (nonatomic, strong) NSDictionary *definedProperties;

- (NSString *)propertyForKey:(NSString *)key;

@end
