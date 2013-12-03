//
//  ConfigurationFetcher.h
//  Mobile
//
//  Created by Jason Hocker on 9/16/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Module+Create.h"
#import "UIColor+HexString.h"

#define kConfigurationFetcherError @"ConfigurationFetcherErrorNotification"

@interface ConfigurationFetcher : NSObject<UIAlertViewDelegate>

+ (BOOL) fetchConfigurationFromURL:(NSString *) url WithManagedObjectContext:(NSManagedObjectContext *)context;

+ (void) showErrorAlertView;
@end
