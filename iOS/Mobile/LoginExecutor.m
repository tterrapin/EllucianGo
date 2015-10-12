//
//  LoginExecutor.m
//  Mobile
//
//  Created by jkh on 6/20/13.
//  Copyright (c) 2013-2014 Ellucian. All rights reserved.
//

#import "LoginExecutor.h"
#import "AppDelegate.h"
#import "AuthenticatedRequest.h"
#import "NotificationManager.h"
#import "Base64.h"
#import "Ellucian_GO-Swift.h"

@implementation LoginExecutor

+(NSInteger) getUserInfo:(BOOL)refreshOnly
{
    NSError *error;
    
    NSUserDefaults *defaults = [AppGroupUtilities userDefaults];
    NSString *loginUrl = [defaults objectForKey:@"login-url"];
    
    AuthenticatedRequest *authenticatedRequet = [AuthenticatedRequest new];
    if(refreshOnly) {
        loginUrl = [NSString stringWithFormat:@"%@?refresh=true", loginUrl];
    }
    NSData *data = [authenticatedRequet requestURL:[NSURL URLWithString:loginUrl] fromView:nil];
    
    NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *)authenticatedRequet.response;
    
    NSInteger responseStatusCode = [httpResponse statusCode];
    if (responseStatusCode == 200 )
    {
        NSDictionary* json = [NSJSONSerialization
                              JSONObjectWithData:data
                              options:kNilOptions
                              error:&error];
        NSString *userId = [json objectForKey:@"userId"];
        NSString *authId = [json objectForKey:@"authId"];
        NSArray *roles = [json objectForKey:@"roles"];

        CurrentUser *user = [CurrentUser sharedInstance];
        [user login:authId andUserid:userId andRoles:[NSSet setWithArray:roles]];
        
        NSDictionary *headers = [httpResponse allHeaderFields];
        NSArray *cookies = [NSHTTPCookie cookiesWithResponseHeaderFields:headers forURL:httpResponse.URL];
        for(NSHTTPCookie *cookie in cookies) {
            [[NSHTTPCookieStorage sharedHTTPCookieStorage] setCookie:cookie];
        }
        
        NSUserDefaults *appGroupUserDefaults = [AppGroupUtilities userDefaults];
        
        //save cookies
        NSMutableArray *cookieArray = [[NSMutableArray alloc] init];
        for (NSHTTPCookie *cookie in [[NSHTTPCookieStorage sharedHTTPCookieStorage] cookies]) {
            NSMutableDictionary *cookieProperties = [NSMutableDictionary dictionary];
            [cookieProperties setObject:cookie.name forKey:NSHTTPCookieName];
            [cookieProperties setObject:cookie.value forKey:NSHTTPCookieValue];
            [cookieProperties setObject:cookie.domain forKey:NSHTTPCookieDomain];
            [cookieProperties setObject:cookie.path forKey:NSHTTPCookiePath];
            [cookieProperties setObject:[NSNumber numberWithInt:(int)cookie.version] forKey:NSHTTPCookieVersion];
            
            if( cookie.expiresDate) {
                [cookieProperties setObject:cookie.expiresDate forKey:NSHTTPCookieExpires];
            }
            
            [cookieArray addObject:cookieProperties];
        }
        [appGroupUserDefaults setValue:cookieArray forKey:@"cookieArray"];
        
        if(!refreshOnly) {
            [[NSNotificationCenter defaultCenter] postNotificationName:kLoginExecutorSuccess object:nil];
        }
        
        // register the device if needed
        [NotificationManager registerDeviceIfNeeded];
    }
    return responseStatusCode;
}

+ (UINavigationController *) loginController
{
    UIStoryboard* storyboard = [UIStoryboard storyboardWithName:@"LoginStoryboard" bundle:nil];
    
    UINavigationController *vc;
    NSString *authenticationMode = [[AppGroupUtilities userDefaults] objectForKey:@"login-authenticationType"];
    if([authenticationMode isEqualToString:@"browser"]) {
        vc = [storyboard instantiateViewControllerWithIdentifier:@"Web Login"];
    } else {
        vc = [storyboard instantiateViewControllerWithIdentifier:@"Login"];
    }
    [vc setModalPresentationStyle:UIModalPresentationFullScreen];
    return vc;
}


@end
