//
//  CurrentUser.h
//  Mobile
//
//  Created by Alan McEwan on 9/13/12.
//  Copyright (c) 2012-2014 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>

extern NSString* const kSignInReturnToHomeNotification;
extern NSString* const kSignOutNotification;
extern NSString* const kSignInNotification;

extern NSString* const kLoginRoles;
extern NSString* const kLoginRemember;
extern NSString* const kLoginUserauth;
extern NSString* const kLoginUserid;

extern NSString* const kLoginExecutorSuccess;

@interface CurrentUser : NSObject

@property (strong, nonatomic) NSString* userauth;
@property (strong, nonatomic) NSString* userid;
@property (strong, nonatomic, readonly) NSString* password;
@property (nonatomic, assign) BOOL isLoggedIn;
@property (strong, nonatomic) NSSet* roles;
@property (nonatomic, assign) BOOL remember;
@property (nonatomic, strong) NSDate* lastLoggedInDate;
@property (nonatomic, strong) NSString* email;

-(void)logoutWithoutUpdatingUI;
-(void)logout:(BOOL)requestedByUser;
-(void)logoutWithNotification:(BOOL)postNotification requestedByUser:(BOOL)requestedByUser;
-(NSDictionary*)userAsPropertyListDictionary;
- (void) login: (NSString *) auth
   andPassword: (NSString *) pass
     andUserid: (NSString *) uID
      andRoles: (NSSet *) roleSet
   andRemember: (BOOL) remember;
- (void) login: (NSString *) auth
     andUserid: (NSString *) uID
      andRoles: (NSSet *) roleSet;
-(NSString *)getPassword;

+(CurrentUser *) sharedInstance;
@end
