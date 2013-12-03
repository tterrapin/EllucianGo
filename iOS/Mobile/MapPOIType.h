//
//  MapPOIType.h
//  Mobile
//
//  Created by jkh on 3/14/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class MapPOI;

@interface MapPOIType : NSManagedObject

@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSString * moduleInternalKey;
@property (nonatomic, retain) NSSet *pointsOfInterest;
@end

@interface MapPOIType (CoreDataGeneratedAccessors)

- (void)addPointsOfInterestObject:(MapPOI *)value;
- (void)removePointsOfInterestObject:(MapPOI *)value;
- (void)addPointsOfInterest:(NSSet *)values;
- (void)removePointsOfInterest:(NSSet *)values;

@end
