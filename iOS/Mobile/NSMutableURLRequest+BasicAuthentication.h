//
//  NSMutableURLRequest+BasicAuthentication.h
//  Mobile
//
//  Created by Jason Hocker on 12/9/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSMutableURLRequest (BasicAuthentication)

-(void) addAuthenticationHeader;

@end
