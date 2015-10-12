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
#import "RegistrationAcademicLevel.h"
#import "RegistrationLocation.h"
#import "Module+Attributes.h"

#if TARGET_OS_WATCH
#import "Ellucian_GO_WatchKit_Extension-Swift.h"
#else
#import "Ellucian_GO-Swift.h"
#endif

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
    
    //special logic to handle extensions
    if([module.type isEqualToString:@"ilp"]) {
        NSString *url = [module propertyForKey:@"ilp"];
        NSUserDefaults *userDefaults = [AppGroupUtilities userDefaults];
        if(userDefaults) {
            [userDefaults setObject:url forKey:@"ilp-url"];
            [userDefaults synchronize];
        }
    }
    return module;
}


+ (void) populateModule:(Module *)module withDictionary:(NSDictionary *) dictionary inManagedObjectContext:(NSManagedObjectContext *)managedObjectContext
{
    if([dictionary objectForKey:@"type"] != [NSNull null] ) {
        module.properties = nil;
        if([dictionary objectForKey:@"type"] != [NSNull null]) {
            module.type = [dictionary objectForKey:@"type"];
        }
        if([dictionary objectForKey:@"name"] != [NSNull null]) {
            module.name = [dictionary objectForKey:@"name"];
        }
        if([dictionary objectForKey:@"icon"] != [NSNull null]) {
            module.iconUrl = [dictionary objectForKey:@"icon"];
            [[ImageCache sharedCache] getImage: module.iconUrl];
        }
        if([dictionary objectForKey:@"hideBeforeLogin"] != [NSNull null]) {
            module.hideBeforeLogin = [NSNumber numberWithBool:[[dictionary objectForKey:@"hideBeforeLogin"] boolValue]];
        }
        if([dictionary objectForKey:@"order"] != [NSNull null]) {
            module.index = [NSNumber numberWithInt:[[dictionary objectForKey:@"order"] intValue]];
        }
        if([dictionary objectForKey:@"access"] != [NSNull null]) {
            module.roles = nil;
            NSArray *access = [dictionary objectForKey:@"access"];
            for(NSString *role in access) {
                ModuleRole *managedRole = [NSEntityDescription insertNewObjectForEntityForName:@"ModuleRole" inManagedObjectContext:managedObjectContext];
                managedRole.role = role;
                managedRole.module = module;
                [module addRolesObject:managedRole];
            }
        }
        
        
        for (id key in [dictionary allKeys]) {
            id value = [dictionary objectForKey:key];
            
            if([key isEqualToString:@"type"]) {
            } else if([key isEqualToString:@"name"]) {
            } else if([key isEqualToString:@"icon"]) {
            } else if([key isEqualToString:@"hideBeforeLogin"]) {
            } else if([key isEqualToString:@"order"]) {
            } else if([key isEqualToString:@"access"]) {
            } else {
                if([value isKindOfClass:[NSString class]]) {
                    [self parseString:value withKey:key forModule:module inManagedObjectContext:managedObjectContext];
                } else if([value isKindOfClass:[NSDictionary class]]) {
                    [self parseDictionary:value forModule:module inManagedObjectContext:managedObjectContext];
                } else if([value isKindOfClass:[NSArray class]]) {
                    [self parseArray:value forModule:module inManagedObjectContext:managedObjectContext key:key];
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
        } else if([value isKindOfClass:[NSArray class]]) {
            [self parseArray:value forModule:module inManagedObjectContext:managedObjectContext key:key];
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

+(void) parseArray:(NSArray *)array forModule:(Module *)module inManagedObjectContext:(NSManagedObjectContext *)managedObjectContext key:(NSString *) key
{
    if([module.type isEqualToString:@"registration"]) {
        if([key isEqualToString:@"locations"]) {
            NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] initWithEntityName:@"RegistrationLocation"];
            fetchRequest.predicate = [NSPredicate predicateWithFormat:@"moduleId == %@", module.internalKey];
            [fetchRequest setIncludesPropertyValues:NO];
            
            NSError *error;
            NSArray *fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
            for (NSManagedObject *object in fetchedObjects)
            {
                [managedObjectContext deleteObject:object];
            }
            
            for(NSDictionary *dictionary in array) {
                RegistrationLocation *location = [NSEntityDescription insertNewObjectForEntityForName:@"RegistrationLocation" inManagedObjectContext:managedObjectContext];
                location.name = [dictionary objectForKey:@"name"];
                location.code = [dictionary objectForKey:@"code"];
                location.moduleId = module.internalKey;
            }
            [managedObjectContext save:&error];
        } else if([key isEqualToString:@"academic levels"]) {
            NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] initWithEntityName:@"RegistrationAcademicLevel"];
            fetchRequest.predicate = [NSPredicate predicateWithFormat:@"moduleId == %@", module.internalKey];
            [fetchRequest setIncludesPropertyValues:NO];
            
            NSError *error;
            NSArray *fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
            for (NSManagedObject *object in fetchedObjects)
            {
                [managedObjectContext deleteObject:object];
            }
            
            for(NSDictionary *dictionary in array) {
                RegistrationAcademicLevel *academicLevel = [NSEntityDescription insertNewObjectForEntityForName:@"RegistrationAcademicLevel" inManagedObjectContext:managedObjectContext];
                academicLevel.name = [dictionary objectForKey:@"name"];
                academicLevel.code = [dictionary objectForKey:@"code"];
                academicLevel.moduleId = module.internalKey;
            }
            [managedObjectContext save:&error];
        }
    }
}


@end
