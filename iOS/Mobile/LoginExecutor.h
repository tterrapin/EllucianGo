//
//  LoginExecutor.h
//  Mobile
//
//  Created by jkh on 6/20/13.
//  Copyright (c) 2013-2014 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface LoginExecutor : NSObject

+(NSInteger) getUserInfo:(BOOL)refreshOnly;
+ (UINavigationController *) loginController;

@end
