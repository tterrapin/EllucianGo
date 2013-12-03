//
//  Module+Attributes.m
//  Mobile
//
//  Created by Jason Hocker on 7/31/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "Module+Attributes.h"
#import "ModuleProperty.h"

@implementation Module (Attributes)

@dynamic definedProperties;

- (NSString *)propertyForKey:(NSString *)key
{
    NSString *value = [self propertyForKeyFromModule:key];
    if(value) {
        return value;
    } else {
        NSString *plistPath = [[NSBundle mainBundle] pathForResource:@"Customizations" ofType:@"plist"];
        NSDictionary *plistDictionary = [[NSDictionary alloc] initWithContentsOfFile:plistPath];
        NSDictionary *customModulesDictionary = [plistDictionary objectForKey:@"Custom Modules"];
        NSString *customType = [self propertyForKeyFromModule:@"custom-type"];
        if(customType) {
            NSDictionary *customModuleDictionary = [customModulesDictionary objectForKey:customType];
            
            if(customModuleDictionary) {
                NSDictionary *properties = [customModuleDictionary objectForKey:@"Properties"];
                return [properties objectForKey:key];
            } else {
                return nil;
            }
        } else {
            return nil;
        }
    }
}

- (NSString *)propertyForKeyFromModule:(NSString *)key
{
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"name == %@", key];
    NSSet *properties = [self.properties filteredSetUsingPredicate:predicate];
    ModuleProperty *moduleProperty = [[properties allObjects] lastObject];
    if(moduleProperty) {
        return moduleProperty.value;
    } else {
        return nil;
    }
}

@end
