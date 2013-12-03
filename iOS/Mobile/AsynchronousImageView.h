//
//  AsynchronousImageView.h
//  Mobile
//
//  Created by Jason Hocker on 8/18/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AsynchronousImageView : UIImageView {
    NSURLConnection *connection;
    NSMutableData *data;
}

- (void)loadImageFromURLString:(NSString *)theUrlString;

@end