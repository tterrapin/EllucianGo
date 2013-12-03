//
//  Module+Create.h
//  Mobile
//
//  Created by Jason Hocker on 7/31/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "Module.h"

@interface Module (Create)

+ (Module *)moduleFromDictionary:(NSDictionary *)dictionary
      inManagedObjectContext:(NSManagedObjectContext *)context withKey:(NSString *)internalKey;

@end
