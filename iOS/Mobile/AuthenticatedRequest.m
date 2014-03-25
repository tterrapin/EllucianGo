//
//  AuthenticatedRequest.m
//  Mobile
//
//  Created by Jason Hocker on 3/2/14.
//  Copyright (c) 2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import "AuthenticatedRequest.h"
#import "CurrentUser.h"
#import "Base64.h"
#import "LoginExecutor.h"
#import "NSMutableURLRequest+BasicAuthentication.h"

@implementation AuthenticatedRequest

-(NSData *) requestURL:(NSURL *)url fromView:(UIViewController *)viewController
{
    self.url = url;
    self.request = [NSMutableURLRequest requestWithURL: self.url
                                           cachePolicy: NSURLRequestReloadIgnoringCacheData
                                       timeoutInterval: 90];
    NSString *authenticationMode = [[NSUserDefaults standardUserDefaults] objectForKey:@"login-authenticationType"];
    if(!authenticationMode || [authenticationMode isEqualToString:@"native"]) {
        [self.request addAuthenticationHeader];
    }
    
    NSHTTPURLResponse *response;
    NSError *error;
    
    NSData *data = [NSURLConnection
                    sendSynchronousRequest: self.request
                    returningResponse: &response
                    error: &error];
    if([error code] == kCFURLErrorUserCancelledAuthentication || [response statusCode] == 401) {
        NSLog(@"AuthenticatedRequest for url: %@ errorCode: %ld", [url absoluteString] , (long)[error code]);
        if(viewController) {
            dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 0.5 * NSEC_PER_SEC);
            dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
                UIViewController *loginController = [LoginExecutor loginController];
                [viewController presentViewController:loginController animated:YES completion:nil];
            });
        }
    }
    self.error = error;
    self.response = response;
    
    return data;
}

@end
