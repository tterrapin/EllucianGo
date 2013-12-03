//
//  Feed+Create.h
//  Mobile
//
//  Created by Jason Hocker on 8/17/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "Feed.h"
#import "NSString+HTML.h"

@interface Feed (Create)

+ (Feed *)feedFromDictionary:(NSDictionary *)dictionary inManagedObjectContext:(NSManagedObjectContext *)managedObjectContext forModuleNamed:(NSString *)moduleName hiddenCategories:(NSSet *)hiddenCategories;


@end
