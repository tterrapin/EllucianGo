//
//  ConfigurationDefinition.h
//  Mobile
//
//  Created by Jason Hocker on 11/26/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class ConfigurationDefinitionKeyword;

@interface ConfigurationDefinition : NSManagedObject

@property (nonatomic, retain) NSNumber * configurationId;
@property (nonatomic, retain) NSString * configurationUrl;
@property (nonatomic, retain) NSNumber * institutionId;
@property (nonatomic, retain) NSString * institutionName;
@property (nonatomic, retain) NSString * configurationName;
@property (nonatomic, retain) NSSet *keywords;
@end

@interface ConfigurationDefinition (CoreDataGeneratedAccessors)

- (void)addKeywordsObject:(ConfigurationDefinitionKeyword *)value;
- (void)removeKeywordsObject:(ConfigurationDefinitionKeyword *)value;
- (void)addKeywords:(NSSet *)values;
- (void)removeKeywords:(NSSet *)values;

@end
