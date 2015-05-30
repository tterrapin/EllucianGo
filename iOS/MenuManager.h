//
//  MenuManager.h
//  Mobile
//
//  Created by Jason Hocker on 5/17/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface MenuManager : NSObject

+(NSArray *) findUserModules:(NSManagedObjectContext *) managedObjectContext withRoles:(NSSet *) roles ;

@end
