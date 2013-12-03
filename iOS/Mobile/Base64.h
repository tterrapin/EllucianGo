//
//  Base64.h
//  Mobile
//
//  Created by Alan McEwan on 9/13/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Base64 : NSObject

+ (NSString *)encode:(NSData *)plainText;

@end
