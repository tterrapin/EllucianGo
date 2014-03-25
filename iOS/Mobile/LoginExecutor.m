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

@implementation LoginExecutor

+(NSInteger) getUserInfo
{
    NSError *error;
    
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSString *loginUrl = [defaults objectForKey:@"login-url"];
    
    AuthenticatedRequest *authenticatedRequet = [AuthenticatedRequest new];
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
        
        [[NSNotificationCenter defaultCenter] postNotificationName:kLoginExecutorSuccess object:nil];
        
        // register the device if needed
        [NotificationManager registerDeviceIfNeeded];
    }
    return responseStatusCode;
}

+ (UIViewController *) loginController
{
    NSString *storyboardName;
    if(UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ) {
        storyboardName = @"MainStoryboard_iPad";
    } else {
        storyboardName = @"MainStoryboard_iPhone";
    }
    UIStoryboard* storyboard = [UIStoryboard storyboardWithName:storyboardName bundle:nil];
    
    UIViewController *vc;
    NSString *authenticationMode = [[NSUserDefaults standardUserDefaults] objectForKey:@"login-authenticationType"];
    if([authenticationMode isEqualToString:@"browser"]) {
        vc = [storyboard instantiateViewControllerWithIdentifier:@"Web Login"];
    } else {
        vc = [storyboard instantiateViewControllerWithIdentifier:@"Login"];
    }
    [vc setModalPresentationStyle:UIModalPresentationFullScreen];
    return vc;
}


@end
