//
//  NSData+AuthenticatedRequest.h
//  Mobile
//
//  Created by jkh on 1/9/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSData (AuthenticatedRequest)

+ (id)dataWithContentsOfURLUsingCurrentUser:(NSURL *)aURL returningResponse:(NSURLResponse **)response error:(NSError **)error;

+ (id)dataWithContentsOfURL:(NSURL *)aURL user:(NSString *)user password:(NSString *)password returningResponse:(NSURLResponse **)response error:(NSError **)error;

@end
