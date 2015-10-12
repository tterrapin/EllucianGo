//
//  ConfigurationFetcher.h
//  Mobile
//
//  Created by Jason Hocker on 9/16/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "Module+Create.h"

#define kConfigurationFetcherError @"ConfigurationFetcherErrorNotification"

@interface ConfigurationFetcher : NSObject<UIAlertViewDelegate>

+ (void) showErrorAlertView;
@end
