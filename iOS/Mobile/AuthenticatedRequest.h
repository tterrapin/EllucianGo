//
//  AuthenticatedRequest.h
//  Mobile
//
//  Created by Jason Hocker on 3/2/14.
//  Copyright (c) 2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface AuthenticatedRequest : NSObject

@property (nonatomic, strong) NSMutableURLRequest *request;
@property (nonatomic, strong) NSURL *url;
@property (nonatomic, strong) NSHTTPURLResponse *response;
@property (nonatomic, strong) NSError *error;

-(NSData *) requestURL:(NSURL *)url fromView:(UIViewController *) controller;

@end
