//
//  Module.h
//  Mobile
//
//  Created by jkh on 3/17/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class ModuleProperty, ModuleRole;

@interface Module : NSManagedObject

@property (nonatomic, retain) NSString * iconUrl;
@property (nonatomic, retain) NSNumber * index;
@property (nonatomic, retain) NSString * internalKey;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSNumber * showForGuest;
@property (nonatomic, retain) NSString * type;
@property (nonatomic, retain) NSSet *roles;
@property (nonatomic, retain) NSSet *properties;
@end

@interface Module (CoreDataGeneratedAccessors)

- (void)addRolesObject:(ModuleRole *)value;
- (void)removeRolesObject:(ModuleRole *)value;
- (void)addRoles:(NSSet *)values;
- (void)removeRoles:(NSSet *)values;

- (void)addPropertiesObject:(ModuleProperty *)value;
- (void)removePropertiesObject:(ModuleProperty *)value;
- (void)addProperties:(NSSet *)values;
- (void)removeProperties:(NSSet *)values;

@end
