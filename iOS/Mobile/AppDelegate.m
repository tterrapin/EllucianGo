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
#import "Notification.h"
#import "NotificationsFetcher.h"
#import "Ellucian_GO-Swift.h"
#import "MapsFetcher.h"
#import "MenuManager.h"
#import "ModuleProperty.h"
#import "ModuleRole.h"
#import "Map.h"
#import "MapCampus.h"
#import "MapPOI.h"
#import "CourseAssignment.h"
#import "CurrentUser.h"

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
    
    //Notifications registration
    if ([[UIApplication sharedApplication] respondsToSelector:@selector(registerUserNotificationSettings:)]) {
        UIUserNotificationType types = UIUserNotificationTypeBadge | UIUserNotificationTypeSound | UIUserNotificationTypeAlert;
        UIUserNotificationSettings *mySettings = [UIUserNotificationSettings settingsForTypes:types categories:nil];
        [[UIApplication sharedApplication] registerUserNotificationSettings:mySettings];
    }
    
    if (![[NSUserDefaults standardUserDefaults] boolForKey:@"didMigrateToAppGroups"])
    {
        NSDictionary *oldDefaults = [[NSUserDefaults standardUserDefaults] dictionaryRepresentation];
        
        // Massive kudos to Sean for pointing this out
        for (id key in oldDefaults.allKeys)
        {
            [[AppGroupUtilities userDefaults] setObject:oldDefaults[key] forKey:key];
        }
        NSString *appDomain = [[NSBundle mainBundle] bundleIdentifier];
        [[NSUserDefaults standardUserDefaults] removePersistentDomainForName:appDomain];
        [[NSUserDefaults standardUserDefaults] setBool:YES forKey:@"didMigrateToAppGroups"];
    }
    
    SlidingViewController *slidingViewController = (SlidingViewController *)self.window.rootViewController;
    slidingViewController.managedObjectContext = self.managedObjectContext;
    
    NSUserDefaults *prefs = [AppGroupUtilities userDefaults];
    
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
        [prefs setObject:self.defaultConfigUrl forKey:@"configurationUrl"];
        [prefs synchronize];
        [self loadDefaultConfiguration:self.defaultConfigUrl inView:slidingViewController];
    }
    else {
        [AppearanceChanger applyAppearanceChanges:self.window];
    }
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationDidTimeout:) name:kApplicationDidTimeoutNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationDidTouch:) name:kApplicationDidTouchNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleDataModelChange:) name:NSManagedObjectContextObjectsDidChangeNotification object:self.managedObjectContext];
    
    CurrentUser *currentUser = [CurrentUser sharedInstance];
    if(currentUser && [currentUser isLoggedIn ] && ![currentUser remember] && logoutOnStartup != NO) {
        [currentUser logout:NO];
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
    NSUserDefaults *prefs = [AppGroupUtilities userDefaults];
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
    
    NSString *authenticationMode = [prefs objectForKey:@"login-authenticationType"];
    if(!authenticationMode || [authenticationMode isEqualToString:@"native"]) {
        CurrentUser *currentUser = [CurrentUser sharedInstance];
        if(currentUser && [currentUser isLoggedIn ] && ![currentUser remember]) {
            NSDate *compareDate = [timestampLastActivity dateByAddingTimeInterval:kApplicationTimeoutInMinutes*60];
            NSDate *currentDate = [NSDate new];
            if(([compareDate compare: currentDate] == NSOrderedAscending) || (logoutOnStartup != NO)) {
                [self sendEventWithCategory:kAnalyticsCategoryAuthentication withAction:kAnalyticsActionTimeout withLabel:@"Password Timeout" withValue:nil];
                [currentUser logout:NO];
            }
        }
    }
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    
    CurrentUser *currentUser = [CurrentUser sharedInstance];
    if([currentUser isLoggedIn ] && ![currentUser remember]) {
        [currentUser logout:NO];
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
    
    NSError *error = nil;
    
    __persistentStoreCoordinator = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel:[self managedObjectModel]];
    
    // create a new URL
    NSURL *newStoreURL = [[self applicationDocumentsDirectory] URLByAppendingPathComponent:@"Mobile.sqlite"];
    
    
    NSURL *applicationDocumentsDirectory = [[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask] lastObject];
    
    NSURL *oldStoreURL = [applicationDocumentsDirectory URLByAppendingPathComponent:@"Mobile.sqlite"];
    NSURL *storeURL = [[self applicationDocumentsDirectory] URLByAppendingPathComponent:@"Mobile.sqlite"];
    
    // Change journal mode from WAL to MEMORY
    NSDictionary *pragmaOptions = [NSDictionary dictionaryWithObject:@"MEMORY" forKey:@"journal_mode"];
    
    NSDictionary *options = [NSDictionary dictionaryWithObjectsAndKeys:
                             [NSNumber numberWithBool:YES], NSMigratePersistentStoresAutomaticallyOption,
                             [NSNumber numberWithBool:YES], NSInferMappingModelAutomaticallyOption,
                             pragmaOptions, NSSQLitePragmasOption, nil];
    
    if ([[NSFileManager defaultManager] fileExistsAtPath:[oldStoreURL path]]) {
        
        //migrate
        
        
        if (![__persistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType configuration:nil URL:oldStoreURL options:options error:&error]) {
            NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
            abort();
        } else {
            NSPersistentStore *sourceStore = [__persistentStoreCoordinator persistentStoreForURL:oldStoreURL];
            if(sourceStore) {
                NSPersistentStore *destinationStore = [self.persistentStoreCoordinator migratePersistentStore:sourceStore toURL:newStoreURL options:options withType:NSSQLiteStoreType error:nil];
                if(destinationStore) {
                    [[NSFileManager defaultManager] removeItemAtURL: oldStoreURL error: &error];
                }
                
            }
        }
    } else {
        //no migrate - store normal
        if (![__persistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType configuration:nil URL:newStoreURL options:options error:&error]) {
            NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
            abort();
        }
        
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
//- (NSURL *)applicationDocumentsDirectory
//{
//    return [[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask] lastObject];
//}

- (NSURL *)applicationDocumentsDirectory
{
    return [AppGroupUtilities applicationDocumentsDirectory];
}

#pragma mark - Configuration reset
-(void)reset
{
    NSLog(@"reset");
    
    NSURLCache *sharedCache = [[NSURLCache alloc] initWithMemoryCapacity:0 diskCapacity:0 diskPath:nil];
    [NSURLCache setSharedURLCache:sharedCache];
    
    NSString *appDomain = [[NSBundle mainBundle] bundleIdentifier];
    
    //must persist this property when switching configurations
    NSString *cloudUrl = [[AppGroupUtilities userDefaults] objectForKey:@"mobilecloud-url"];
    [[NSUserDefaults standardUserDefaults] removePersistentDomainForName:appDomain];
    NSUserDefaults *appGroupDefaults = [AppGroupUtilities userDefaults];
    NSDictionary *appGroupDefaultsDictionary = [appGroupDefaults dictionaryRepresentation];
    for (NSString *key in [appGroupDefaultsDictionary allKeys]) {
        [appGroupDefaults removeObjectForKey:key];
    }
    
    if(cloudUrl) {
        NSUserDefaults* defaults = [AppGroupUtilities userDefaults];
        [defaults setObject:cloudUrl forKey:@"mobilecloud-url"];
        [defaults synchronize];
        
    }
    
    [[CurrentUser sharedInstance] logoutWithoutUpdatingUI]; //has to be done before erasing the persistent store since it clears up notifications from the database.
    
    //Erase the persistent store from coordinator and also file manager.
    NSPersistentStore *store = [self.persistentStoreCoordinator.persistentStores lastObject];
    NSError *error = nil;
    NSURL *storeURL = store.URL;
    
    [self.managedObjectContext reset];
    [self.privateWriterContext reset];
    if ([self.persistentStoreCoordinator removePersistentStore:store error:&error]) {
        [[NSFileManager defaultManager] removeItemAtURL:storeURL error:&error];
    }
    [[ImageCache sharedCache] reset];
    
    /*
     // retrieve the store URL
     storeURL = [[self.managedObjectContext persistentStoreCoordinator] URLForPersistentStore:[[[self.managedObjectContext persistentStoreCoordinator] persistentStores] lastObject]];
     // lock the current context
     [self.managedObjectContext lock];
     [self.managedObjectContext reset];//to drop pending changes
     //delete the store from the current managedObjectContext
     if ([[self.managedObjectContext persistentStoreCoordinator] removePersistentStore:[[[self.managedObjectContext persistentStoreCoordinator] persistentStores] lastObject] error:&error])
     {
     // remove the file containing the data
     [[NSFileManager defaultManager] removeItemAtURL:storeURL error:&error];
     //recreate the store like in the  appDelegate method
     [[self.managedObjectContext persistentStoreCoordinator] addPersistentStoreWithType:NSSQLiteStoreType configuration:nil URL:storeURL options:nil error:&error];//recreates the persistent store
     }
     [self.managedObjectContext unlock];
     
     */
    
    if (error) {
        NSLog(@"Failed to remove persistent store: %@", [error localizedDescription]);
        NSArray *detailedErrors = [[error userInfo] objectForKey:NSDetailedErrorsKey];
        if (detailedErrors != nil && [detailedErrors count] > 0) {
            for (NSError *detailedError in detailedErrors) {
                NSLog(@" DetailedError: %@", [detailedError userInfo]);
            }
        }
        else {
            NSLog(@" %@", [error userInfo]);
        }
    }
    NSDictionary *pragmaOptions = [NSDictionary dictionaryWithObject:@"MEMORY" forKey:@"journal_mode"];
    
    NSDictionary *options = [NSDictionary dictionaryWithObjectsAndKeys:
                             [NSNumber numberWithBool:YES], NSMigratePersistentStoresAutomaticallyOption,
                             [NSNumber numberWithBool:YES], NSInferMappingModelAutomaticallyOption,
                             pragmaOptions, NSSQLitePragmasOption, nil];
    if (![self.persistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType configuration:nil URL:storeURL options:options error:&error]) {
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
    
    [[UIApplication sharedApplication] cancelAllLocalNotifications];
    [UIApplication sharedApplication].applicationIconBadgeNumber = 0;
    
}

#pragma mark - Credentials expiration
-(void)applicationDidTimeout:(NSNotification *) notif
{
    NSLog (@"time exceeded!!");
    
    CurrentUser *currentUser = [CurrentUser sharedInstance];
    if([currentUser isLoggedIn ] && ![currentUser remember]) {
        NSString *authenticationMode = [[AppGroupUtilities userDefaults] objectForKey:@"login-authenticationType"];
        if(!authenticationMode || [authenticationMode isEqualToString:@"native"]) {
            [currentUser logout:NO];
        }
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
    
    
    if([type isEqualToString:@"mobilecloud"]) {
        NSString *scheme = [pathComponents objectAtIndex:1];
        NSString *host = [pathComponents objectAtIndex:2];
        NSArray *newPathComponents = [pathComponents subarrayWithRange:NSMakeRange(3, [pathComponents count] - 3)] ;
        NSString *newPath = [newPathComponents componentsJoinedByString:@"/"];
        
        NSURL *newUrl = [[NSURL alloc] initWithScheme:scheme host:host path:[NSString stringWithFormat:@"/%@", newPath]];
        NSUserDefaults* defaults = [AppGroupUtilities userDefaults];
        [defaults setObject:[newUrl absoluteString] forKey:@"mobilecloud-url"];
        [defaults synchronize];
        UINavigationController *navcontroller = [self.window.rootViewController.storyboard instantiateViewControllerWithIdentifier:@"ConfigurationSelector"];
        navcontroller.navigationBar.translucent = NO;
        ConfigurationSelectionViewController *vc = navcontroller.childViewControllers[0];
        [vc setModalPresentationStyle:UIModalPresentationFullScreen];
        vc.managedObjectContext = self.managedObjectContext;
        [self.window setRootViewController:navcontroller];
    } else if([type isEqualToString:@"configuration"]) {
        NSString *scheme = [pathComponents objectAtIndex:1];
        NSString *host = [pathComponents objectAtIndex:2];
        NSArray *newPathComponents = [pathComponents subarrayWithRange:NSMakeRange(3, [pathComponents count] - 3)] ;
        NSString *newPath = [newPathComponents componentsJoinedByString:@"/"];
        
        [[CurrentUser sharedInstance] logoutWithoutUpdatingUI];
        [self.window.rootViewController dismissViewControllerAnimated:NO completion:nil];
        NSString *passcode = [parser valueForVariable:@"passcode"];
        UIViewController *vc = [self.window.rootViewController.storyboard instantiateViewControllerWithIdentifier:@"Loading"];
        [self.window.rootViewController presentViewController:vc animated:NO completion:nil];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            [self loadConfigurationInBackground:scheme host:host newPath:newPath passcode:passcode];
        });
        
    } else if([type isEqualToString:@"module-type"]) {
        NSString *moduleType = [pathComponents objectAtIndex:1];
        if([moduleType isEqualToString:@"ilp"]) {
            NSString *urlToAssignment = [parser valueForVariable:@"url"];
            
            [self sendEventWithCategory:kAnalyticsCategoryWidget withAction:kAnalyticsActionList_Select withLabel:@"Assignments" withValue:nil];
            
            [self.window.rootViewController dismissViewControllerAnimated:NO completion:nil];
            
            SlidingViewController *slidingViewController = (SlidingViewController *)self.window.rootViewController;
            slidingViewController.managedObjectContext = self.managedObjectContext;
            
            [slidingViewController showAssignments:urlToAssignment];
            
            [self.window setRootViewController:slidingViewController];
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
    
    NSUserDefaults *prefs = [AppGroupUtilities userDefaults];
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
    //[self showNotifications:application withOnlyLaunchIfInactive:YES];
}

/*
 - (void) showNotifications:(UIApplication*) application withOnlyLaunchIfInactive:(BOOL)onlyLaunchIfInactive
 {
 UIApplicationState state = [application applicationState];
 if (!onlyLaunchIfInactive || state == UIApplicationStateInactive) {
 //the case may be that the user was on the modal "configuration selection" screen.  dismiss in case that's the case.
 [self.window.rootViewController dismissViewControllerAnimated:NO completion:nil];
 
 SlidingViewController *slidingViewController = (SlidingViewController *)self.window.rootViewController;
 slidingViewController.managedObjectContext = self.managedObjectContext;
 
 
 [slidingViewController showNotifications];
 
 [self.window setRootViewController:slidingViewController];
 }
 }
 */

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
    NSUserDefaults* defaults = [AppGroupUtilities userDefaults];
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

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo
{
    
    NSString* uuid = [userInfo objectForKey:@"uuid"];
    
    UIApplicationState state = [application applicationState];
    if (state == UIApplicationStateActive) {
        NSLog(@"application active - show notification message alert");
        
        // log activity to Google Analytics
        [self sendEventWithCategory:kAnalyticsCategoryPushNotification withAction:kAnalyticsActionReceivedMessage withLabel:@"whileActive" withValue:nil];
        
        NSString* alertMessage = [[userInfo objectForKey:@"aps"] objectForKey:@"alert"];
        SlidingViewController *slidingViewController = (SlidingViewController *)self.window.rootViewController;
        
        [slidingViewController showNotificationAlert:alertMessage withNotificationId:uuid];
    } else {
        // navigate to notifications
        NSLog(@"application not active - open notifications");
        
        // log activity to Google Analytics
        [self sendEventWithCategory:kAnalyticsCategoryPushNotification withAction:kAnalyticsActionReceivedMessage withLabel:@"whileInActive" withValue:nil];
        
        [self.window.rootViewController dismissViewControllerAnimated:NO completion:nil];
        
        SlidingViewController *slidingViewController = (SlidingViewController *)self.window.rootViewController;
        slidingViewController.managedObjectContext = self.managedObjectContext;
        
        [slidingViewController showNotifications:uuid];
        
        [self.window setRootViewController:slidingViewController];
    }
}

- (void)handleDataModelChange:(NSNotification *)note
{
    //if Notifications changed, update badge
    NSSet *updatedObjects = [[note userInfo] objectForKey:NSUpdatedObjectsKey];
    NSSet *deletedObjects = [[note userInfo] objectForKey:NSDeletedObjectsKey];
    NSSet *insertedObjects = [[note userInfo] objectForKey:NSInsertedObjectsKey];
    
    NSPredicate *predicate = [NSPredicate predicateWithFormat: @"self isKindOfClass: %@", [Notification class]];
    updatedObjects = [updatedObjects filteredSetUsingPredicate:predicate];
    deletedObjects = [deletedObjects filteredSetUsingPredicate:predicate];
    insertedObjects = [insertedObjects filteredSetUsingPredicate:predicate];
    NSUInteger changesCount = [updatedObjects count] + [deletedObjects count] + [insertedObjects count];
    if(changesCount > 0) {
        NSManagedObjectContext *moc = [self managedObjectContext];
        NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"Notification" inManagedObjectContext:moc];
        NSFetchRequest *request = [[NSFetchRequest alloc] init];
        [request setEntity:entityDescription];
        NSPredicate *predicate = [NSPredicate predicateWithFormat: @"read == %@",[NSNumber numberWithBool:NO]];
        [request setPredicate:predicate];
        
        NSError *error;
        NSUInteger notificationCount = [[moc executeFetchRequest:request error:&error] count];
        [[UIApplication sharedApplication] cancelAllLocalNotifications];
        
        [UIApplication sharedApplication].applicationIconBadgeNumber = notificationCount;
    }
    
}

#pragma mark - Apple Watch

- (void)application:(UIApplication *)application handleWatchKitExtensionRequest:(NSDictionary *)userInfo reply:(void (^)(NSDictionary *))reply {
    NSUserDefaults *prefs = [AppGroupUtilities userDefaults];
    
    NSString *action = userInfo[@"action"];
    if ([action isEqualToString:@"fetch configuration"]) {
        NSString *configurationUrl = [prefs stringForKey:@"configurationUrl"];
        
        if(configurationUrl) {
            NSDate *updateDate = [prefs objectForKey:@"menu updated date"];
            int days = [[[NSDate alloc] init] timeIntervalSinceDate:updateDate]/24/60/60;
            if(days > 0 || updateDate == nil) {
                [ConfigurationFetcher fetchConfigurationFromURL:configurationUrl WithManagedObjectContext:self.managedObjectContext];
            }
        }
        NSSet *roles = nil;
        CurrentUser *currentUser = [CurrentUser sharedInstance];
        if(currentUser && [currentUser isLoggedIn ]) {
            roles = currentUser.roles;
        }
        NSArray *definedModules = [MenuManager findUserModules:self.managedObjectContext withRoles:roles];
        NSMutableArray *moduleDicts = [NSMutableArray new];
        for(Module *module in definedModules) {
            NSMutableDictionary *properties = [NSMutableDictionary new];
            for(ModuleProperty *prop in module.properties) {
                [properties setValue:prop.value  forKey:prop.name];
                
            }
            NSMutableArray *roles = [NSMutableArray new];
            for(ModuleRole *role in module.roles) {
                [roles addObject:role.role];
                
            }
            NSDictionary *dict = [NSMutableDictionary new];
            [dict setValue:module.hideBeforeLogin forKey:@"hideBeforeLogin"];
            [dict setValue:module.index forKey:@"index"];
            [dict setValue:module.internalKey forKey:@"internalKey"];
            [dict setValue:module.name forKey:@"name"];
            [dict setValue:module.type forKey:@"type"];
            [dict setValue:properties forKey:@"properties"];
            [dict setValue:roles forKey:@"roles"];
            if (module.iconUrl ) {
                [dict setValue:module.iconUrl forKey:@"iconUrl"];
                
            }
            NSLog(@"%@", dict);
            [moduleDicts addObject:dict];
        }
        NSDictionary *response = @{@"modules":moduleDicts};
        reply(response);
    } else if ([action isEqualToString:@"fetch maps"]) {
        
        NSString *internalKey = userInfo[@"internalKey"];
        NSString *urlString = userInfo[@"url"];
        [MapsFetcher fetch:self.managedObjectContext WithURL:urlString moduleKey:internalKey];
        
        NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"Map"];
        request.predicate = [NSPredicate predicateWithFormat:@"moduleName = %@", internalKey];
        NSSortDescriptor *sortDescriptor = [NSSortDescriptor sortDescriptorWithKey:@"moduleName" ascending:YES];
        request.sortDescriptors = [NSArray arrayWithObject:sortDescriptor];
        
        NSMutableArray *campuesDicts = [NSMutableArray new];
        NSError *error;
        NSArray *matches = [self.managedObjectContext executeFetchRequest:request error:&error];
        for (Map *map in matches) {
            for(MapCampus *mc in map.campuses) {
                
                NSMutableDictionary *dict = [NSMutableDictionary new];
                [dict setValue:mc.name forKey:@"name"];
                
                NSMutableArray *points = [NSMutableArray new];
                for(MapPOI *point in mc.points) {
                    NSMutableDictionary *poiDict = [NSMutableDictionary new];
                    poiDict[@"name"] = point.name;
                    if (point.additionalServices ) {
                        poiDict[@"additionalServices"] = point.additionalServices;
                    }
                    if (point.address ) {
                        poiDict[@"address"] = point.address;
                    }
                    if (point.description_ ) {
                        poiDict[@"description"] = point.description_;
                    }
                    if (point.latitude ) {
                        poiDict[@"latitude"] = point.latitude;
                    }
                    if (point.longitude ) {
                        poiDict[@"longitude"] = point.longitude;
                    }
                    [points addObject:poiDict];
                    
                }
                [dict setValue:points forKey:@"buildings"];
                
                [campuesDicts addObject:dict];
            }
         
        }
        
        
        NSDictionary *response = @{@"campuses":campuesDicts};
        reply(response);
    } else if ([action isEqualToString:@"fetch assignments"]) {
        NSString *urlString = userInfo[@"url"];
        [AssignmentsFetcher fetch:self.managedObjectContext url:urlString];

        NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"CourseAssignment"];
        NSSortDescriptor *sortDescriptor = [NSSortDescriptor sortDescriptorWithKey:@"dueDate" ascending:YES];
        request.sortDescriptors = [NSArray arrayWithObject:sortDescriptor];
        
        NSMutableArray *assignmentsDict = [NSMutableArray new];
        NSError *error;
        NSArray *matches = [self.managedObjectContext executeFetchRequest:request error:&error];
        
        for(CourseAssignment *assignment in matches) {
                
            NSMutableDictionary *assignmentDict = [NSMutableDictionary new];
            if (assignment.sectionId ) {
                [assignmentDict setValue:assignment.sectionId forKey:@"sectionId"];
            }
            if (assignment.assignmentDescription ) {
                [assignmentDict setValue:assignment.assignmentDescription forKey:@"assignmentDescription"];
            }
            if (assignment.dueDate ) {
                [assignmentDict setValue:assignment.dueDate forKey:@"dueDate"];
            }

            if (assignment.name ) {
                [assignmentDict setValue:assignment.name forKey:@"name"];
            }

            if (assignment.courseName ) {
                [assignmentDict setValue:assignment.courseName forKey:@"courseName"];
            }

            if (assignment.courseSectionNumber ) {
                [assignmentDict setValue:assignment.courseSectionNumber forKey:@"courseSectionNumber"];
            }

            if (assignment.url ) {
                [assignmentDict setValue:assignment.url forKey:@"url"];
            }


            [assignmentsDict addObject:assignmentDict];
            
        }
        
        
        NSDictionary *response = @{@"assignments":assignmentsDict, @"loggedInStatus": [NSNumber numberWithBool:[[CurrentUser sharedInstance] isLoggedIn ]]};
        reply(response);
    }
    
    
}

@end
