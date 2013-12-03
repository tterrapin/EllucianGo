//
//  ImageCache.h
//  Mobile
//
//  Created by Alan McEwan on 11/20/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface ImageCache : NSObject

@property (nonatomic, retain) NSString * cachePath;

+ (ImageCache*) sharedCache;

- (UIImage *) getImage: (NSString *) filename;
- (UIImage *) getCachedImage: (NSString *) filename;
- (void) reset;

@end
