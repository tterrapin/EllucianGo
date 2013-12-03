//
//  DirectoryEntry.m
//  Mobile
//
//  Created by Jason Hocker on 10/4/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "DirectoryEntry.h"

@implementation DirectoryEntry

-(NSString *) nameOrderedByFirstName:(BOOL)firstNameFirst
{
    if(self.displayName) return self.displayName;
    else if(firstNameFirst && self.firstName && self.lastName) return [NSString stringWithFormat:@"%@ %@", self.firstName, self.lastName];
    else if(!firstNameFirst && self.firstName && self.lastName) return [NSString stringWithFormat:@"%@, %@", self.lastName, self.firstName];
    else if (self.firstName) return self.firstName;
    else if (self.lastName) return self.lastName;
    else return @"";
}

@end
