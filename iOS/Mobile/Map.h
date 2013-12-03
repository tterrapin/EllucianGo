//
//  Map.h
//  Mobile
//
//  Created by jkh on 2/5/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class MapCampus;

@interface Map : NSManagedObject

@property (nonatomic, retain) NSString * moduleName;
@property (nonatomic, retain) NSSet *campuses;
@end

@interface Map (CoreDataGeneratedAccessors)

- (void)addCampusesObject:(MapCampus *)value;
- (void)removeCampusesObject:(MapCampus *)value;
- (void)addCampuses:(NSSet *)values;
- (void)removeCampuses:(NSSet *)values;

@end
