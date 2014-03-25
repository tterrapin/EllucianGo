//
//  AppDelegate.m
//  Mobile
//
//  Created by Jason Hocker on 7/12/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "AppDelegate.h"
#import "MenuViewController.h"
#import "ImageCache.h"
#import "ConfigurationSelectionViewController.h"
#import "URLParser.h"
#import "MBProgressHUD.h"
#import "GAI.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "NotificationManager.h"
#import "GAIFields.h"
#import "GAIDictionaryBuilder.h"

static BOOL openURL = NO;

@interface AppDelegate()

@property(nonatomic, readonly) NSString *defaultConfigUrl;

@end

@implementation AppDelegate

@synthesize window = _window;
@synthesize managedObjectContext = __managedObjectContext;
@synthesize managedObjectModel = __managedObjectModel;
@synthesize persistentStoreCoordinator = __persistentStoreCoordinator;
@synthesize privateWriterContext = __privateWriterContext;

NSDate *timestampLastActivity;
BOOL logoutOnStartup = YES;

#pragma mark - Monitoring App State Changes

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    NSSetUncaughtExceptionHandler(&onUncaughtException);
    
    //Google Analytics
    [GAI sharedInstance].trackUncaughtExceptions = YES;
    [GAI sharedInstance].dispatchInterval = 20;
    [[[GAI sharedInstance] logger] setLogLevel:kGAILogLevelError];
    
    SlidingViewController *slidingViewController = (SlidingViewController *)self.window.rootViewController;
    slidingViewController.managedObjectContext = self.managedObjectContext;
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    
    NSString *_configurationUrl = [prefs stringForKey:@"configurationUrl"];
    
    BOOL useDefaultConfigUrl = self.useDefaultConfiguration;
    if(!_configurationUrl && !useDefaultConfigUrl) {
        UINavigationController *navcontroller = [slidingViewController.storyboard instantiateViewControllerWithIdentifier:@"ConfigurationSelector"];
        navcontroller.navigationBar.translucent = NO;
        ConfigurationSelectionViewController *vc = navcontroller.childViewControllers[0];
        [vc setModalPresentationStyle:UIModalPresentationFullScreen];
        vc.managedObjectContext = self.managedObjectContext;
        [self.window setRootViewController:navcontroller];
        [AppearanceChanger applyAppearanceChanges:self.window];
    }
    else if (!_configurationUrl && useDefaultConfigUrl) {
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        [prefs setObject:self.defaultConfigUrl forKey:@"configurationUrl"];
        [prefs synchronize];
        [self loadDefaultConfiguration:self.defaultConfigUrl inView:slidingViewController];
    }
    else {
        [AppearanceChanger applyAppearanceChanges:self.window];
    }
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationDidTimeout:) name:kApplicationDidTimeoutNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationDidTouch:) name:kApplicationDidTouchNotification object:nil];

    CurrentUser *currentUser = [self getCurrentUser];
    if(currentUser && [currentUser isLoggedIn ] && ![currentUser remember] && logoutOnStartup != NO) {
        [currentUser logout];
    }

    // for when swapping the application in, honor the current logged in/out state
    logoutOnStartup = NO;
    return YES;
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    [self saveContext];
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    NSString *_configurationUrl = [prefs stringForKey:@"configurationUrl"];
    if (!_configurationUrl && self.useDefaultConfiguration) {
        SlidingViewController *slidingViewController = (SlidingViewController *)self.window.rootViewController;
        slidingViewController.managedObjectContext = self.managedObjectContext;
        [self loadDefaultConfiguration:self.defaultConfigUrl inView:slidingViewController];
    }
    
    if(!openURL) {
        [[NSNotificationCenter defaultCenter] postNotificationName:kRefreshConfigurationListIfPresent object:nil];
    }
    openURL = NO;
    CurrentUser *currentUser = [self getCurrentUser];
    if(currentUser && [currentUser isLoggedIn ] && ![currentUser remember]) {
        NSDate *compareDate = [timestampLastActivity dateByAddingTimeInterval:kApplicationTimeoutInMinutes*60];
        NSDate *currentDate = [NSDate new];
        if(([compareDate compare: currentDate] == NSOrderedAscending) || (logoutOnStartup != NO)) {
            [self sendEventWithCategory:kAnalyticsCategoryAuthentication withAction:kAnalyticsActionTimeout withLabel:@"Password Timeout" withValue:nil];
            [currentUser logout];
        }
    }
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    
    CurrentUser *currentUser = [self getCurrentUser];
    if([currentUser isLoggedIn ] && ![currentUser remember]) {
        [currentUser logout];
    }
    
    [self saveContext];
}

#pragma mark - Core Data stack

- (NSManagedObjectContext *)managedObjectContext
{
    if (__managedObjectContext != nil) {
        return __managedObjectContext;
    }
    
    NSPersistentStoreCoordinator *coordinator = [self persistentStoreCoordinator];
    if (coordinator != nil) {
        __managedObjectContext = [[NSManagedObjectContext alloc] initWithConcurrencyType:NSMainQueueConcurrencyType];
        [__managedObjectContext setParentContext:self.privateWriterContext];
    }
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(saveContext:)
                                                 name:NSManagedObjectContextDidSaveNotification
                                               object:__managedObjectContext];
    return __managedObjectContext;
}

- (NSManagedObjectContext *)privateWriterContext
{
    if (__privateWriterContext != nil) {
        return __privateWriterContext;
    }
    
    NSPersistentStoreCoordinator *coordinator = [self persistentStoreCoordinator];
    if (coordinator != nil) {
        __privateWriterContext = [[NSManagedObjectContext alloc] initWithConcurrencyType:NSPrivateQueueConcurrencyType];
        [__privateWriterContext setPersistentStoreCoordinator:coordinator];
    }
    return __privateWriterContext;
}

// Returns the managed object model for the application.
// If the model doesn't already exist, it is created from the application's model.
- (NSManagedObjectModel *)managedObjectModel
{
    if (__managedObjectModel != nil) {
        return __managedObjectModel;
    }
    NSURL *modelURL = [[NSBundle mainBundle] URLForResource:@"Mobile" withExtension:@"momd"];
    __managedObjectModel = [[NSManagedObjectModel alloc] initWithContentsOfURL:modelURL];
    return __managedObjectModel;
}


// Returns the persistent store coordinator for the application.
// If the coordinator doesn't already exist, it is created and the application's store added to it.
- (NSPersistentStoreCoordinator *)persistentStoreCoordinator
{
    if (__persistentStoreCoordinator != nil) {
        return __persistentStoreCoordinator;
    }
    
    NSURL *storeURL = [[self applicationDocumentsDirectory] URLByAppendingPathComponent:@"Mobile.sqlite"];
    
    NSError *error = nil;
    NSDictionary *options = [NSDictionary dictionaryWithObjectsAndKeys:[NSNumber numberWithBool:YES],
                             NSMigratePersistentStoresAutomaticallyOption,
                             [NSNumber numberWithBool:YES],
                             NSInferMappingModelAutomaticallyOption, nil];
    __persistentStoreCoordinator = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel:[self managedObjectModel]];
    if (![__persistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType configuration:nil URL:storeURL options:options error:&error]) {
        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
        abort();
    }
    
    NSDictionary *fileAttributes = [NSDictionary
                                    dictionaryWithObject:NSFileProtectionComplete
                                    forKey:NSFileProtectionKey];
    if(![[NSFileManager defaultManager] setAttributes:fileAttributes
                                         ofItemAtPath:[storeURL path] error: &error]) {
        NSLog(@"Unresolved error with store encryption %@, %@",
              error, [error userInfo]);
        abort();
    }
    
    return __persistentStoreCoordinator;
}

-(void)saveContext:(id)sender
{
    [self saveContext];
}

- (void)saveContext
{
    [self.privateWriterContext performBlock:^{
        NSError *error = nil;
        [self.privateWriterContext save:&error];
        [[NSNotificationCenter defaultCenter] removeObserver:self name:NSManagedObjectContextDidSaveNotification object:self.privateWriterContext];
    }];
}

#pragma mark - Application's Documents directory

// Returns the URL to the application's Documents directory.
- (NSURL *)applicationDocumentsDirectory
{
    return [[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask] lastObject];
}

#pragma mark - Application's current user
- (CurrentUser *)getCurrentUser
{
    if (self.currentUser != nil ) {
        return self.currentUser;
    }
    
    self.currentUser = [[CurrentUser alloc] init];
    return self.currentUser;
}


#pragma mark - Configuration reset
-(void)reset
{
    NSLog(@"reset");
    
    NSURLCache *sharedCache = [[NSURLCache alloc] initWithMemoryCapacity:0 diskCapacity:0 diskPath:nil];
    [NSURLCache setSharedURLCache:sharedCache];
    
    NSString *appDomain = [[NSBundle mainBundle] bundleIdentifier];
    
    //must persist this property when switching configurations
    NSString *cloudUrl = [[NSUserDefaults standardUserDefaults] objectForKey:@"mobilecloud-url"];
    [[NSUserDefaults standardUserDefaults] removePersistentDomainForName:appDomain];
    if(cloudUrl) {
        NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
        [defaults setObject:cloudUrl forKey:@"mobilecloud-url"];
        [defaults synchronize];

    }
    
    [self.currentUser logoutWithoutUpdatingUI]; //has to be done before erasing the persistent store since it clears up notifications from the database.
    
    //Erase the persistent store from coordinator and also file manager.
    NSPersistentStore *store = [self.persistentStoreCoordinator.persistentStores lastObject];
    NSError *error = nil;
    NSURL *storeURL = store.URL;
    
    [self.managedObjectContext reset];
    [self.privateWriterContext reset];
    [self.persistentStoreCoordinator removePersistentStore:store error:&error];
    [[NSFileManager defaultManager] removeItemAtURL:storeURL error:&error];
    [[ImageCache sharedCache] reset];
    
    if (![self.persistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType configuration:nil URL:storeURL options:nil error:&error]) {
        NSLog(@"Error adding persistent store coordinator: %@", error);
    }
    
    NSDictionary *fileAttributes = [NSDictionary
                                    dictionaryWithObject:NSFileProtectionComplete
                                    forKey:NSFileProtectionKey];
    if(![[NSFileManager defaultManager] setAttributes:fileAttributes
                                         ofItemAtPath:[storeURL path] error: &error]) {
        NSLog(@"Unresolved error with store encryption %@, %@",
              error, [error userInfo]);
        abort();
    }
    
    [UIApplication sharedApplication].applicationIconBadgeNumber = 0;
    
}

#pragma mark - Credentials expiration
-(void)applicationDidTimeout:(NSNotification *) notif
{
    NSLog (@"time exceeded!!");
    
    CurrentUser *currentUser = [self getCurrentUser];
    if([currentUser isLoggedIn ] && ![currentUser remember]) {
        [currentUser logout];
    }
}

-(void)applicationDidTouch:(NSNotification *) notif
{
    timestampLastActivity = [NSDate new];
}

#pragma mark - Launch from URL
- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation {

    openURL = YES;
    
    URLParser *parser = [[URLParser alloc] initWithURLString:[[url absoluteString] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    
    NSString *type = [url host];
    NSArray *pathComponents = [url pathComponents];
    if([pathComponents count] > 3) {
        NSArray *newPathComponents = [pathComponents subarrayWithRange:NSMakeRange(3, [pathComponents count] - 3)] ;
        NSString *newPath = [newPathComponents componentsJoinedByString:@"/"];
        NSString *scheme = [pathComponents objectAtIndex:1];
        NSString *host = [pathComponents objectAtIndex:2];

        if([type isEqualToString:@"mobilecloud"]) {
            NSURL *newUrl = [[NSURL alloc] initWithScheme:scheme host:host path:[NSString stringWithFormat:@"/%@", newPath]];
            NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
            [defaults setObject:[newUrl absoluteString] forKey:@"mobilecloud-url"];
            [defaults synchronize];
            UINavigationController *navcontroller = [self.window.rootViewController.storyboard instantiateViewControllerWithIdentifier:@"ConfigurationSelector"];
            navcontroller.navigationBar.translucent = NO;
            ConfigurationSelectionViewController *vc = navcontroller.childViewControllers[0];
            [vc setModalPresentationStyle:UIModalPresentationFullScreen];
            vc.managedObjectContext = self.managedObjectContext;
            [self.window setRootViewController:navcontroller];
        } else if([type isEqualToString:@"configuration"]) {
            [[self getCurrentUser] logoutWithoutUpdatingUI];
            [self.window.rootViewController dismissViewControllerAnimated:NO completion:nil];
            NSString *passcode = [parser valueForVariable:@"passcode"];
            UIViewController *vc = [self.window.rootViewController.storyboard instantiateViewControllerWithIdentifier:@"Loading"];
            [self.window.rootViewController presentViewController:vc animated:NO completion:nil];

            dispatch_async(dispatch_get_main_queue(), ^{
                [self loadConfigurationInBackground:scheme host:host newPath:newPath passcode:passcode];
            });
            
        }
    }
    
    return YES;
}

- (void) loadConfigurationInBackground:(NSString*) scheme host:(NSString *) host newPath:(NSString *)newPath passcode:(NSString* ) passcode
{
    [self reset];
    
    NSURL *newUrl = [[NSURL alloc] initWithScheme:scheme host:host path:[NSString stringWithFormat:@"/%@", newPath]];
    if(passcode) {
        newUrl = [[NSURL alloc] initWithScheme:scheme host:host path:[NSString stringWithFormat:@"/%@?passcode=%@", newPath, passcode]];
    }
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    [prefs setObject:[newUrl absoluteString] forKey:@"configurationUrl"];
    [prefs synchronize];
    
    BOOL success = [ConfigurationFetcher fetchConfigurationFromURL:[newUrl absoluteString] WithManagedObjectContext:self.managedObjectContext ];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if(success || self.useDefaultConfiguration) {
            [AppearanceChanger applyAppearanceChanges:self.window];
            
            //the case may be that the user was on the modal "configuration selection" screen.  dismiss in case that's the case.
            [self.window.rootViewController dismissViewControllerAnimated:NO completion:nil];
            
            SlidingViewController *vc = [self.window.rootViewController.storyboard instantiateViewControllerWithIdentifier:@"SlidingViewController"];
            vc.managedObjectContext = self.managedObjectContext;
            [self.window setRootViewController:vc];
        } else {
            [prefs removeObjectForKey:@"configurationUrl"];
            [prefs synchronize];
            UINavigationController *navcontroller = [self.window.rootViewController.storyboard instantiateViewControllerWithIdentifier:@"ConfigurationSelector"];
            navcontroller.navigationBar.translucent = NO;
            ConfigurationSelectionViewController *vc = navcontroller.childViewControllers[0];
            [vc setModalPresentationStyle:UIModalPresentationFullScreen];
            vc.managedObjectContext = self.managedObjectContext;
            [self.window setRootViewController:navcontroller];
            [ConfigurationFetcher showErrorAlertView];
        }
    });
}

#pragma mark - Launch from local notification
- (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification
{
    UIApplicationState state = [application applicationState];
    if (state == UIApplicationStateInactive) {
        //the case may be that the user was on the modal "configuration selection" screen.  dismiss in case that's the case.
        [self.window.rootViewController dismissViewControllerAnimated:NO completion:nil];
        
        SlidingViewController *slidingViewController = (SlidingViewController *)self.window.rootViewController;
        slidingViewController.managedObjectContext = self.managedObjectContext;
        [slidingViewController showNotifications];
        
        [self.window setRootViewController:slidingViewController];
    }
}

#pragma mark - Hard-coded configuration launch
// This function will only be called by customers that are hard coding a single
// configuration URL from the cloud.
- (void) loadDefaultConfiguration:(NSString *)defaultConfigUrl inView:(SlidingViewController *) slidingViewController
{
    UIView * hudView = self.window.rootViewController.view;
    MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo: hudView animated:YES];
    hud.labelText = NSLocalizedString(@"Loading", @"loading message while waiting for data to load");
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 0.01 * NSEC_PER_SEC);
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
        BOOL success = [ConfigurationFetcher fetchConfigurationFromURL:defaultConfigUrl WithManagedObjectContext:self.managedObjectContext ];
        [MBProgressHUD hideHUDForView:hudView animated:YES];
        if(success) {
            
            [AppearanceChanger applyAppearanceChanges:self.window];
            [slidingViewController showHome];
            
        } else {
            //[ConfigurationFetcher showErrorAlertView];
        }
    });
}

void onUncaughtException(NSException* exception)
{
    NSLog(@"uncaught exception: %@", exception.description);
}

- (void)sendEventWithCategory:(NSString *)category
                   withAction:(NSString *)action
                    withLabel:(NSString *)label
                    withValue:(NSNumber *)value
{
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
    NSString *trackingId1 = [defaults objectForKey:@"gaTracker1"];
    NSString *trackingId2 = [defaults objectForKey:@"gaTracker2"];
    NSString *configurationName = [defaults objectForKey:@"configurationName"];
    
    GAIDictionaryBuilder *builder = [GAIDictionaryBuilder createEventWithCategory:category action:action label:label value:value];
    [builder set:configurationName forKey:[GAIFields customMetricForIndex:1]];
    NSMutableDictionary *buildDictionary = [builder build];
    
    if(trackingId1) {
        id<GAITracker> tracker1 = [[GAI sharedInstance] trackerWithTrackingId:trackingId1];
        [tracker1 send:buildDictionary];
    }
    if(trackingId2) {
        id<GAITracker> tracker2 = [[GAI sharedInstance] trackerWithTrackingId:trackingId2];
        [tracker2 send:buildDictionary];
    }
}


-(BOOL) useDefaultConfiguration
{
    NSString *plistPath = [[NSBundle mainBundle] pathForResource:@"Customizations" ofType:@"plist"];
    NSDictionary *plistDictionary = [[NSDictionary alloc] initWithContentsOfFile:plistPath];
    
    if([plistDictionary objectForKey:@"Use Default Configuration"]) {
        return [plistDictionary[@"Use Default Configuration"] boolValue];
    } else {
        return NO;
    }
}

-(NSString *) defaultConfigUrl
{
    NSString *plistPath = [[NSBundle mainBundle] pathForResource:@"Customizations" ofType:@"plist"];
    NSDictionary *plistDictionary = [[NSDictionary alloc] initWithContentsOfFile:plistPath];
    
    if([plistDictionary objectForKey:@"Default Configuration URL"]) {
        return plistDictionary[@"Default Configuration URL"];
    } else {
        return nil;
    }
}

- (void)application:(UIApplication*)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData*)deviceToken
{
	NSLog(@"Application deviceToken: %@", deviceToken);
    [NotificationManager registerDeviceToken:deviceToken];
}

- (void)application:(UIApplication*)application didFailToRegisterForRemoteNotificationsWithError:(NSError*)error
{
	NSLog(@"Failed to get token, error: %@", error);
}

@end
