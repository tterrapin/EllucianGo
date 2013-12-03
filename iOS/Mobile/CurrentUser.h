//
//  CurrentUser.h
//  Mobile
//
//  Created by Alan McEwan on 9/13/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>

#define kSignInReturnToHomeNotification @"SignInReturnToHomeNotification"
#define kSignOutNotification @"SignOutNotification"
#define kSignInNotification @"SignInNotification"

#define kLoginRoles @"login-roles"
#define kLoginRemember @"login-remember"
#define kLoginUserauth @"login-userauth"
#define kLoginUserid @"login-userid"

@interface CurrentUser : NSObject

@property (strong, nonatomic) NSString* userauth;
@property (strong, nonatomic) NSString* userid;
@property (strong, nonatomic, readonly) NSString* password;
@property (nonatomic, assign) BOOL isLoggedIn;
@property (strong, nonatomic) NSSet* roles;
@property (nonatomic, assign) BOOL remember;
@property (nonatomic, strong) NSDate* lastLoggedInDate;

-(void)logoutWithoutUpdatingUI;
-(void)logout;
- (void) login: (NSString *) auth
   andPassword: (NSString *) pass
     andUserid: (NSString *) uID
      andRoles: (NSSet *) roleSet
   andRemember: (BOOL) remember;
-(NSString *)getPassword;

+(NSString *) userid;

@end
