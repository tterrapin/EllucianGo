//
//  AppDelegate.h
//  Mobile
//
//  Created by Jason Hocker on 7/12/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SlidingViewController.h"
#import "CurrentUser.h"
#import "AppearanceChanger.h"
#import "MobileUIApplication.h"

#define kRefreshConfigurationListIfPresent @"RefreshConfigurationListIfPresent"

@interface AppDelegate : UIResponder <UIApplicationDelegate>

@property (strong, nonatomic) UIWindow *window;

@property (readonly, strong, nonatomic) NSManagedObjectContext *managedObjectContext;
@property (readonly, strong, nonatomic) NSManagedObjectModel *managedObjectModel;
@property (readonly, strong, nonatomic) NSPersistentStoreCoordinator *persistentStoreCoordinator;
@property (readonly, strong, nonatomic) NSManagedObjectContext *privateWriterContext;
@property(nonatomic, readonly) BOOL useDefaultConfiguration;

- (void)saveContext;
- (NSURL *)applicationDocumentsDirectory;
- (void)reset;

@end
