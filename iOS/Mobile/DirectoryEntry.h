//
//  DirectoryEntry.h
//  Mobile
//
//  Created by Jason Hocker on 10/4/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface DirectoryEntry : NSObject

@property (strong, nonatomic) NSString *personId;
@property (strong, nonatomic) NSString *username;
@property (strong, nonatomic) NSString *displayName;
@property (strong, nonatomic) NSString *firstName;
@property (strong, nonatomic) NSString *middleName;
@property (strong, nonatomic) NSString *lastName;
@property (strong, nonatomic) NSString *title;
@property (strong, nonatomic) NSString *office;
@property (strong, nonatomic) NSString *department;
@property (strong, nonatomic) NSString *phone;
@property (strong, nonatomic) NSString *mobile;
@property (strong, nonatomic) NSString *email;
@property (strong, nonatomic) NSString *street;
@property (strong, nonatomic) NSString *room;
@property (strong, nonatomic) NSString *postOfficeBox;
@property (strong, nonatomic) NSString *city;
@property (strong, nonatomic) NSString *state;
@property (strong, nonatomic) NSString *postalCode;
@property (strong, nonatomic) NSString *country;
@property (strong, nonatomic) NSString *prefix;
@property (strong, nonatomic) NSString *suffix;

-(NSString *) nameOrderedByFirstName:(BOOL)firstNameFirst;

@end
