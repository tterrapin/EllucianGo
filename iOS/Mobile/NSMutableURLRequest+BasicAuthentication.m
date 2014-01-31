//
//  NSMutableURLRequest+BasicAuthentication.m
//  Mobile
//
//  Created by Jason Hocker on 12/9/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import "NSMutableURLRequest+BasicAuthentication.h"
#import "AppDelegate.h"
#import "CurrentUser.h"
#import "Base64.h"

@implementation NSMutableURLRequest (BasicAuthentication)

-(void) addAuthenticationHeader
{
    AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
    CurrentUser *user = [appDelegate currentUser];
    NSMutableString *loginString = (NSMutableString*)[@"" stringByAppendingFormat:@"%@:%@", user.userauth, user.getPassword];
    NSString *encodedLoginData = [Base64 encode:[loginString dataUsingEncoding:NSUTF8StringEncoding]];
    NSString *authHeader = [@"Basic " stringByAppendingFormat:@"%@", encodedLoginData];
    [self addValue:authHeader forHTTPHeaderField:@"Authorization"];
    
}

@end
