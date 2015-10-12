//
//  ConfigurationFetcher.m
//  Mobile
//
//  Created by Jason Hocker on 9/16/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "ConfigurationFetcher.h"
#import "ImageCache.h"
#import "VersionChecker.h"
#import "Ellucian_GO-Swift.h"

@implementation ConfigurationFetcher

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex{
    if (buttonIndex == 0){
        //cancel clicked ...do your action
    }else if (buttonIndex == 1){
        //reset clicked
    }
}

+ (void) showErrorAlertView
{
    //Only show if alert is not already showing
    for (UIWindow* window in [UIApplication sharedApplication].windows) {
        NSArray* subviews = window.subviews;
        if ([subviews count] > 0){
            for (id cc in subviews) {
                if ([cc isKindOfClass:[UIAlertView class]]) {
                    return;
                }
            }
        }
    }
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Unable to launch configuration", @"unable to download configuration title") message:NSLocalizedString(@"Unable to download the configuration from that link", @"unable to download configuration message") delegate:self cancelButtonTitle:NSLocalizedString(@"OK", @"OK") otherButtonTitles:nil];
    [alert show];
}
@end
