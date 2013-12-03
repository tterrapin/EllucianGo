//
//  NSData+AuthenticatedRequest.m
//  Mobile
//
//  Created by jkh on 1/9/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import "NSData+AuthenticatedRequest.h"
#import "Base64.h"
#import "CurrentUser.h"
#import "AppDelegate.h"

@implementation NSData (AuthenticatedRequest)

+ (id)dataWithContentsOfURLUsingCurrentUser:(NSURL *)aURL returningResponse:(NSURLResponse **) response error:(NSError **)error
{
    AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
    CurrentUser *user = [appDelegate currentUser];
    NSData *data = [NSData dataWithContentsOfURL:aURL user:user.userauth password:user.getPassword returningResponse:response error:error];
    return data;
}

+ (id)dataWithContentsOfURL:(NSURL *)aURL user:(NSString *)user password:(NSString *)password returningResponse:(NSURLResponse **)response error:(NSError **)error
{
    // create a plaintext string in the format username:password
    NSMutableString *loginString = (NSMutableString*)[@"" stringByAppendingFormat:@"%@:%@", user, password];
    
    // employ the Base64 encoding above to encode the authentication tokens
    NSString *encodedLoginData = [Base64 encode:[loginString dataUsingEncoding:NSUTF8StringEncoding]];
    
    // create the contents of the header
    NSString *authHeader = [@"Basic " stringByAppendingFormat:@"%@", encodedLoginData];
    
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL: aURL
                                                           cachePolicy: NSURLRequestReloadIgnoringCacheData
                                                       timeoutInterval: 90];
    
    // add the header to the request.
    [request addValue:authHeader forHTTPHeaderField:@"Authorization"];
    
    NSData *data = [NSURLConnection
                    sendSynchronousRequest: request
                    returningResponse: response
                    error: error];
    
    // here's the content of the webserver's response.
    //NSString *result = [NSString stringWithUTF8String:[data bytes]];
    return data;
}
@end
