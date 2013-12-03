//
//  ConfigurationDefinitionKeyword.h
//  Mobile
//
//  Created by Jason Hocker on 11/26/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class ConfigurationDefinition;

@interface ConfigurationDefinitionKeyword : NSManagedObject

@property (nonatomic, retain) NSString * keyword;
@property (nonatomic, retain) ConfigurationDefinition *configuration;

@end
