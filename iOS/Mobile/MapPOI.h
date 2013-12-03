//
//  MapPOI.h
//  Mobile
//
//  Created by jkh on 3/14/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class MapCampus, MapPOIType;

@interface MapPOI : NSManagedObject

@property (nonatomic, retain) NSString * additionalServices;
@property (nonatomic, retain) NSString * address;
@property (nonatomic, retain) NSString * buildingId;
@property (nonatomic, retain) NSString * description_;
@property (nonatomic, retain) NSString * imageUrl;
@property (nonatomic, retain) NSString * key;
@property (nonatomic, retain) NSNumber * latitude;
@property (nonatomic, retain) NSNumber * longitude;
@property (nonatomic, retain) NSString * moduleInternalKey;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) MapCampus *campus;
@property (nonatomic, retain) NSSet *types;
@end

@interface MapPOI (CoreDataGeneratedAccessors)

- (void)addTypesObject:(MapPOIType *)value;
- (void)removeTypesObject:(MapPOIType *)value;
- (void)addTypes:(NSSet *)values;
- (void)removeTypes:(NSSet *)values;

@end
