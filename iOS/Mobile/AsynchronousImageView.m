//
//  AsynchronousImageView.m
//  Mobile
//
//  Created by Jason Hocker on 8/18/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "AsynchronousImageView.h"

@implementation AsynchronousImageView

- (void)loadImageFromURLString:(NSString *)theUrlString
{
    self.image = nil;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        //store the image in the cache
        NSURLRequest *request = [NSURLRequest requestWithURL:
                                 [NSURL URLWithString:theUrlString]
                                                 cachePolicy:NSURLRequestReturnCacheDataElseLoad
                                             timeoutInterval:30.0];
        
        connection = [[NSURLConnection alloc]
                      initWithRequest:request delegate:self];
    });
}

- (void)connection:(NSURLConnection *)theConnection
    didReceiveData:(NSData *)incrementalData
{
    if (data == nil)
        data = [[NSMutableData alloc] initWithCapacity:2048];
    
    [data appendData:incrementalData];
}

- (void)connectionDidFinishLoading:(NSURLConnection *)theConnection
{
    self.image = [UIImage imageWithData:data];
    data = nil;
    connection = nil;
}


@end
