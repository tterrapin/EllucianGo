//
//  Module+Create.m
//  Mobile
//
//  Created by Jason Hocker on 7/31/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "Module+Create.h"
#import "ModuleRole.h"
#import "ModuleProperty.h"
#import "ImageCache.h"

@implementation Module (Create)

+ (Module *)moduleFromDictionary:(NSDictionary *)dictionary
inManagedObjectContext:(NSManagedObjectContext *)managedObjectContext withKey:(NSString *)internalKey
{
    Module *module = nil;
    
    NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"Module"];
    request.predicate = [NSPredicate predicateWithFormat:@"internalKey = %@", internalKey];
    NSSortDescriptor *sortDescriptor = [NSSortDescriptor sortDescriptorWithKey:@"internalKey" ascending:YES];
    request.sortDescriptors = [NSArray arrayWithObject:sortDescriptor];
    
    NSError *error = nil;
    NSArray *matches = [managedObjectContext executeFetchRequest:request error:&error];
    
    if (!matches || ([matches count] > 1)) {
        // handle error
    } else if ([matches count] == 0) {
        module = [NSEntityDescription insertNewObjectForEntityForName:@"Module" inManagedObjectContext:managedObjectContext];
        module.internalKey = internalKey;
        [Module populateModule:module withDictionary:dictionary inManagedObjectContext:managedObjectContext];
    } else {
        module = [matches lastObject];
       [Module populateModule:module withDictionary:dictionary inManagedObjectContext:managedObjectContext];        
    }
    return module;
}


+ (void) populateModule:(Module *)module withDictionary:(NSDictionary *) dictionary inManagedObjectContext:(NSManagedObjectContext *)managedObjectContext
{
    if([dictionary objectForKey:@"type"] != [NSNull null] ) {
        module.showForGuest = [NSNumber numberWithBool:YES];
        module.properties = nil;
        for (id key in [dictionary allKeys]) {
            id value = [dictionary objectForKey:key];
            
            if([key isEqualToString:@"type"]) {
                module.type = value;
            } else if([key isEqualToString:@"name"]) {
                module.name = value;
            } else if([key isEqualToString:@"icon"]) {
                module.iconUrl = [dictionary objectForKey:@"icon"];
                [[ImageCache sharedCache] getImage: module.iconUrl];
//            } else if([key isEqualToString:@"showGuest"]) {
//                module.showForGuest = [NSNumber numberWithBool:[[dictionary objectForKey:@"showGuest"] boolValue]];
            } else if([key isEqualToString:@"order"]) {
                module.index = [NSNumber numberWithInt:[value intValue]];
            } else if([key isEqualToString:@"roles"]) {
                module.roles = nil;
                for(NSString *role in [[dictionary objectForKey:@"roles"] componentsSeparatedByString:@","] ) {
                    ModuleRole *managedRole = [NSEntityDescription insertNewObjectForEntityForName:@"ModuleRole" inManagedObjectContext:managedObjectContext];
                    managedRole.role = role;
                    managedRole.module = module;
                    [module addRolesObject:managedRole];
                }
            } else {
                if([value isKindOfClass:[NSString class]]) {
            [self parseString:value withKey:key forModule:module inManagedObjectContext:managedObjectContext];
                } else if([value isKindOfClass:[NSDictionary class]]) {
                    [self parseDictionary:value forModule:module inManagedObjectContext:managedObjectContext];
                }
            }
        }
    }
}

+(void) parseDictionary:(NSDictionary *)dictionary forModule:(Module *)module inManagedObjectContext:(NSManagedObjectContext *)managedObjectContext

{
    for(NSString *key in [dictionary allKeys]) {
        id value = [dictionary objectForKey:key];
        if([value isKindOfClass:[NSString class]]) {
            [self parseString:value withKey:key forModule:module inManagedObjectContext:managedObjectContext];
        } else if([value isKindOfClass:[NSDictionary class]]) {
            [self parseDictionary:value forModule:module inManagedObjectContext:managedObjectContext];
        }
    }
}

+(void) parseString:(NSString *)value withKey:(NSString *)key forModule:(Module *)module inManagedObjectContext:(NSManagedObjectContext *)managedObjectContext
{
    ModuleProperty *managedProperty = [NSEntityDescription insertNewObjectForEntityForName:@"ModuleProperty" inManagedObjectContext:managedObjectContext];
    managedProperty.name = key;
    managedProperty.value = value;
    managedProperty.module = module;
    [module addPropertiesObject:managedProperty];
}

@end
