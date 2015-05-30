//
//  MenuManager.m
//  Mobile
//
//  Created by Jason Hocker on 5/17/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import "MenuManager.h"

@implementation MenuManager

+(NSArray *) findUserModules:(NSManagedObjectContext *) managedObjectContext withRoles:(NSSet *) roles {
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"Module" inManagedObjectContext:managedObjectContext];
    [fetchRequest setEntity:entity];
    
    NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"index" ascending:YES];
    NSArray *sortDescriptors = [NSArray arrayWithObjects:sortDescriptor, nil];
    [fetchRequest setSortDescriptors:sortDescriptors];
    
    NSError *error;
    NSArray *definedModules;
    
    if(roles) {
        
        NSMutableArray *parr = [NSMutableArray array];
        
        [parr addObject: [NSPredicate predicateWithFormat:@"roles.@count == 0"] ];
        [parr addObject: [NSPredicate predicateWithFormat:@"ANY roles.role like %@", @"Everyone"] ];
        for(NSString *role in roles) {
            [parr addObject: [NSPredicate predicateWithFormat:@"ANY roles.role like %@", role] ];
        }
        
        NSPredicate *joinOnRolesPredicate = [NSCompoundPredicate orPredicateWithSubpredicates:parr];
        NSArray *allModules = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        definedModules = [allModules filteredArrayUsingPredicate:joinOnRolesPredicate];
        
    } else {
        
        fetchRequest.predicate = [NSPredicate predicateWithFormat:@"(hideBeforeLogin == %@) || (hideBeforeLogin = nil)", [NSNumber numberWithBool:NO]];
        definedModules = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
    }
    return definedModules;
}

@end
