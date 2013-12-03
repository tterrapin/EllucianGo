//
//  SafariActivity.m
//  Mobile
//
//  Created by Alan McEwan on 1/7/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import "SafariActivity.h"

@implementation SafariActivity
{
	NSURL *_targetURL;
}

- (NSString *)activityType
{
	return @"SafariActivity";
}

- (NSString *)activityTitle
{
	return NSLocalizedString(@"Open in Safari", @"label to open link in Safari");
}

- (UIImage *)activityImage
{
	return [UIImage imageNamed:@"icon_website"];
}

- (BOOL)canPerformWithActivityItems:(NSArray *)activityItems
{
    
	for (id activityItem in activityItems) {
        if ([activityItem isKindOfClass:[NSString class]]) {
            NSURL *url = [NSURL URLWithString:activityItem];
            if([[UIApplication sharedApplication] canOpenURL:url]) {
                return YES;
            }
		}
	}
	
	return NO;
}

- (void)prepareWithActivityItems:(NSArray *)activityItems
{
    //iterate through items and find the url
	for (id activityItem in activityItems) {
        if ([activityItem isKindOfClass:[NSString class]]) {
			_targetURL = [NSURL URLWithString:activityItem];
		}
	}
}

- (void)performActivity
{
	BOOL completed = [[UIApplication sharedApplication] openURL:_targetURL];

	[self activityDidFinish:completed];
}

@end
