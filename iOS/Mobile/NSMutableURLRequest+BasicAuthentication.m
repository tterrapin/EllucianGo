//
//  NSMutableURLRequest+BasicAuthentication.m
//  Mobile
//
//  Created by Jason Hocker on 12/9/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import "NSMutableURLRequest+BasicAuthentication.h"
#import "CurrentUser.h"
#import "Base64.h"
#import "Ellucian_GO-Swift.h"

@implementation NSMutableURLRequest (BasicAuthentication)

-(void) addAuthenticationHeader
{
    NSUserDefaults *prefs = [AppGroupUtilities userDefaults];
    
    NSString *authenticationMode = [prefs objectForKey:@"login-authenticationType"];
    if(!authenticationMode || [authenticationMode isEqualToString:@"native"]) {
        
        CurrentUser *user = [CurrentUser sharedInstance];
        NSString *loginString = [NSString stringWithFormat:@"%@:%@", user.userauth, user.getPassword];
        NSString *encodedLoginData = [Base64 encode:[loginString dataUsingEncoding:NSUTF8StringEncoding]];
        NSString *authHeader = [@"Basic " stringByAppendingFormat:@"%@", encodedLoginData];
        [self addValue:authHeader forHTTPHeaderField:@"Authorization"];
    }
    
}

@end
