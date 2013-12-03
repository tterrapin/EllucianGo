//
//  LoginExecutor.h
//  Mobile
//
//  Created by jkh on 6/20/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface LoginExecutor : NSObject

-(NSInteger) performLogin:(NSString *)urlString forUser:(NSString *)username andPassword:(NSString *)password andRememberUser:(BOOL)rememberUser returningRoles:(NSArray**)roles;

@end
