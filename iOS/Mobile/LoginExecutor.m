//
//  LoginExecutor.m
//  Mobile
//
//  Created by jkh on 6/20/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import "LoginExecutor.h"
#import "AppDelegate.h"
#import "NSData+AuthenticatedRequest.h"

@implementation LoginExecutor

-(NSInteger) performLogin:(NSString *)urlString forUser:(NSString *)username andPassword:(NSString *)password andRememberUser:(BOOL)rememberUser returningRoles:(NSArray**)roles
{
    NSError *error;
    NSURLResponse *response;
    
    [[NSHTTPCookieStorage sharedHTTPCookieStorage] setCookieAcceptPolicy:NSHTTPCookieAcceptPolicyAlways];
    
    NSData *data = [NSData dataWithContentsOfURL:[NSURL URLWithString:urlString] user:username password:password returningResponse: &response error: &error];
    
    NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *)response;
    
    int responseStatusCode = [httpResponse statusCode];
    if (responseStatusCode == 200 )
    {
        NSDictionary* json = [NSJSONSerialization
                              JSONObjectWithData:data
                              options:kNilOptions
                              error:&error];
        NSString *userId = [json objectForKey:@"userId"];
        NSString *authId = [json objectForKey:@"authId"];
        *roles = [json objectForKey:@"roles"];
        
        AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
        CurrentUser *user = [appDelegate getCurrentUser];
        [user login:authId andPassword:password andUserid:userId andRoles:[NSSet setWithArray:*roles] andRemember:rememberUser];
        
        NSDictionary *headers = [(NSHTTPURLResponse *)response allHeaderFields];
        NSArray *cookies = [NSHTTPCookie cookiesWithResponseHeaderFields:headers forURL:response.URL];
        for(NSHTTPCookie *cookie in cookies) {
            [[NSHTTPCookieStorage sharedHTTPCookieStorage] setCookie:cookie];
        }
    }

    return responseStatusCode;
}


@end
