//
//  MapCampus.h
//  Mobile
//
//  Created by jkh on 3/15/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class Map, MapPOI;

@interface MapCampus : NSManagedObject

@property (nonatomic, retain) NSNumber * centerLatitude;
@property (nonatomic, retain) NSNumber * centerLongitude;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSNumber * spanLatitude;
@property (nonatomic, retain) NSNumber * spanLongitude;
@property (nonatomic, retain) NSString * campusId;
@property (nonatomic, retain) Map *map;
@property (nonatomic, retain) NSSet *points;
@end

@interface MapCampus (CoreDataGeneratedAccessors)

- (void)addPointsObject:(MapPOI *)value;
- (void)removePointsObject:(MapPOI *)value;
- (void)addPoints:(NSSet *)values;
- (void)removePoints:(NSSet *)values;

@end
