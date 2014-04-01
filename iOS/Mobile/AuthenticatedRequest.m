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

@interface AuthenticatedRequest ()

@property (nonatomic, assign) BOOL downloadFinished;
@property (nonatomic, strong) NSMutableData *data;

@end

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
    
    [NSURLConnection connectionWithRequest:self.request delegate:self];
    
    while (!self.downloadFinished)
    {
        [[NSRunLoop currentRunLoop] runMode:NSDefaultRunLoopMode beforeDate:[NSDate distantFuture]];
    }
    
    //treat redirects from cas just like a log in is needed.  We can't detect which redirects are for cas and which are not.
    if([self.error code] == kCFURLErrorUserCancelledAuthentication || [self.response statusCode] == 401 || [self.response statusCode] == 302) {
        NSLog(@"AuthenticatedRequest for url: %@ errorCode: %ld", [url absoluteString] , (long)[self.error code]);
        if(viewController) {
            dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 0.5 * NSEC_PER_SEC);
            dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
                UIViewController *loginController = [LoginExecutor loginController];
                [viewController presentViewController:loginController animated:YES completion:nil];
            });
        }
    }
    
    return self.data;
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection;
{
    self.downloadFinished = YES;
}

- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error;
{
    NSLog(@"An error occurred: %@", error);
    self.data = nil;
    self.downloadFinished = YES;
}

- (void)connection:(NSURLConnection *)theConnection didReceiveResponse:(NSURLResponse *)response
{
    self.response = (NSHTTPURLResponse *)response;
}

- (void)connection:(NSURLConnection *)theConnection didReceiveData:(NSData *)data
{
    if (!self.data) {
        self.data = [[NSMutableData alloc] initWithData:data];
    } else {
        [self.data appendData:data];
    }
    NSLog(@"response connection");
}

- (NSURLRequest *)connection:(NSURLConnection *)connection willSendRequest:(NSURLRequest *)request redirectResponse:(NSURLResponse *)redirectResponse
{
    NSURLRequest *newRequest = request;
    if (redirectResponse) {
        newRequest = nil;
    }
    return newRequest;
}

@end
