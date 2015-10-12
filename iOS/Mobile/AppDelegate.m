//
//  AppDelegate.m
//  Mobile
//
//  Created by Jason Hocker on 7/12/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "AppDelegate.h"
#import "ImageCache.h"
#import "URLParser.h"
#import "MBProgressHUD.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "NotificationManager.h"
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
    self.watchConnectivityManager = [[WatchConnectivityManager instance] ensureWatchConnectivityInitialized];
    self.configurationManager = [ConfigurationManager instance];
    
    NSSetUncaughtExceptionHandler(&onUncaughtException);
    
    //Google Analytics
    [GAI sharedInstance].trackUncaughtExceptions = YES;
    [GAI sharedInstance].dispatchInterval = 20;
    [[[GAI sharedInstance] logger] setLogLevel:kGAILogLevelError];
    
    //Notifications registration
    UIUserNotificationType types = UIUserNotificationTypeBadge | UIUserNotificationTypeSound | UIUserNotificationTypeAlert;
    UIUserNotificationSettings *mySettings = [UIUserNotificationSettings settingsForTypes:types categories:nil];
    [[UIApplication sharedApplication] registerUserNotificationSettings:mySettings];
    
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
    
    self.slidingViewController = (ECSlidingViewController *)self.window.rootViewController;
    self.slidingViewController.anchorRightRevealAmount = 276;
    self.slidingViewController.anchorLeftRevealAmount = 276;
    self.slidingViewController.topViewAnchoredGesture = ECSlidingViewControllerAnchoredGestureTapping | ECSlidingViewControllerAnchoredGesturePanning;
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"HomeStoryboard" bundle:nil];
    UIViewController *menu = [storyboard instantiateViewControllerWithIdentifier:@"Menu"];
    
    if ([UIView respondsToSelector:@selector(userInterfaceLayoutDirectionForSemanticContentAttribute:)]) {
        UIUserInterfaceLayoutDirection direction = [UIView userInterfaceLayoutDirectionForSemanticContentAttribute:self.window.rootViewController.view.semanticContentAttribute];
        if (direction == UIUserInterfaceLayoutDirectionRightToLeft) {
            self.slidingViewController.underRightViewController = menu;
        } else {
            self.slidingViewController.underLeftViewController = menu;
        }
    } else { // iOS8
        self.slidingViewController.underLeftViewController = menu;
    }
    

    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationDidTimeout:) name:kApplicationDidTimeoutNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationDidTouch:) name:kApplicationDidTouchNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleDataModelChange:) name:NSManagedObjectContextObjectsDidChangeNotification object:self.managedObjectContext];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(returnHome:) name:kSignOutNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(returnHome:) name:kSignInReturnToHomeNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(forceConfigurationSelection:) name:kConfigurationFetcherError object:nil];
    
    CurrentUser *currentUser = [CurrentUser sharedInstance];
    if(currentUser && [currentUser isLoggedIn ] && ![currentUser remember] && logoutOnStartup != NO) {
        [currentUser logout:NO];
    }
    
    // for when swapping the application in, honor the current logged in/out state
    logoutOnStartup = NO;

    NSUserDefaults *prefs = [AppGroupUtilities userDefaults];
    
    NSString *_configurationUrl = [prefs stringForKey:@"configurationUrl"];
    
    BOOL useDefaultConfigUrl = self.useDefaultConfiguration;
    if(!_configurationUrl && !useDefaultConfigUrl) {
        UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"ConfigurationSelectionStoryboard" bundle:nil];
        UINavigationController *navcontroller = [storyboard instantiateViewControllerWithIdentifier:@"ConfigurationSelector"];
        ConfigurationSelectionViewController *vc = navcontroller.childViewControllers[0];
        [vc setModalPresentationStyle:UIModalPresentationFullScreen];
        [self.window setRootViewController:navcontroller];
        [AppearanceChanger applyAppearanceChanges:self.window];
    }
    else if (!_configurationUrl && useDefaultConfigUrl) {
        [prefs setObject:self.defaultConfigUrl forKey:@"configurationUrl"];
        [prefs synchronize];
        [self loadDefaultConfiguration:self.defaultConfigUrl inView:self.slidingViewController];
    }
    else {
        [AppearanceChanger applyAppearanceChanges:self.window];
        [[NSOperationQueue mainQueue] addOperation:[OpenModuleHomeOperation new]];

    }
    
    
    return YES;
}

-(void) returnHome:(id)sender
{
    [AppearanceChanger applyAppearanceChanges:self.window];
    [[NSOperationQueue mainQueue] addOperation:[OpenModuleHomeOperation new]];
}

-(void) forceConfigurationSelection:(id)sender
{
    dispatch_async(dispatch_get_main_queue(), ^(void){
        [ConfigurationFetcher showErrorAlertView];
        [AppearanceChanger applyAppearanceChanges:self.window];
        [[NSOperationQueue mainQueue] addOperation:[OpenModuleConfigurationSelectionOperation new]];
    });
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
        ECSlidingViewController *slidingViewController = (ECSlidingViewController *)self.window.rootViewController;
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
    return CoreDataManager.shared.managedObjectContext;
}

-(void)saveContext:(id)sender
{
    [self saveContext];
}

- (void)saveContext
{
    [CoreDataManager.shared save];
}

#pragma mark - Application's Documents directory

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
        UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"ConfigurationSelectionStoryboard" bundle:nil];
        UINavigationController *navcontroller = [storyboard instantiateViewControllerWithIdentifier:@"ConfigurationSelector"];
        ConfigurationSelectionViewController *vc = navcontroller.childViewControllers[0];
        [vc setModalPresentationStyle:UIModalPresentationFullScreen];
        [self.window setRootViewController:navcontroller];
    } else if([type isEqualToString:@"configuration"]) {
        NSString *scheme = [pathComponents objectAtIndex:1];
        NSString *host = [pathComponents objectAtIndex:2];
        NSArray *newPathComponents = [pathComponents subarrayWithRange:NSMakeRange(3, [pathComponents count] - 3)] ;
        NSString *newPath = [newPathComponents componentsJoinedByString:@"/"];
        
        [[CurrentUser sharedInstance] logoutWithoutUpdatingUI];
        [self.window.rootViewController dismissViewControllerAnimated:NO completion:nil];
        NSString *passcode = [parser valueForVariable:@"passcode"];

        dispatch_async(dispatch_get_main_queue(), ^{
            UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"HomeStoryboard" bundle:nil];
            UIViewController *vc = [storyboard instantiateViewControllerWithIdentifier:@"Loading"];
            [self.window.rootViewController presentViewController:vc animated:NO completion:nil];
            dispatch_async(dispatch_get_global_queue(0,0), ^{
                [self loadConfigurationInBackground:scheme host:host newPath:newPath passcode:passcode];
            });
        });
        
    } else if([type isEqualToString:@"module-type"]) {
        NSString *moduleType = [pathComponents objectAtIndex:1];
        if([moduleType isEqualToString:@"ilp"]) {
            NSString *urlToAssignment = [parser valueForVariable:@"url"];
            
            [self sendEventWithCategory:kAnalyticsCategoryWidget withAction:kAnalyticsActionList_Select withLabel:@"Assignments" withValue:nil];
            
            [self.window.rootViewController dismissViewControllerAnimated:NO completion:nil];

            OpenModuleOperation* operation = [[OpenModuleOperation alloc] initWithType:@"ilp"];
            if (urlToAssignment) { //sign in will not pass a url to open
                operation.properties =  @ {@"requestedAssignmentId" : urlToAssignment } ;
            }
            [[NSOperationQueue mainQueue] addOperation:operation];
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
    
    ConfigurationManager *manager = [ConfigurationManager instance];
    [manager loadConfigurationWithConfigurationUrl:[newUrl absoluteString] completionHandler:^(id result){
        dispatch_async(dispatch_get_main_queue(), ^{
            NSNumber *resultNumber = nil;
            if ([result isKindOfClass:[NSNumber class]]) {
                resultNumber = result;
            }
            BOOL resultBool = false;
            if (resultNumber) {
                resultBool = [resultNumber boolValue];
            }
            if (resultBool || self.useDefaultConfiguration) {
                [AppearanceChanger applyAppearanceChanges:self.window];
            
                //the case may be that the user was on the modal "configuration selection" screen.  dismiss in case that's the case.
                [self.window.rootViewController dismissViewControllerAnimated:NO completion:nil];
                
                UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"HomeStoryboard" bundle:nil];
                ECSlidingViewController *slidingVC = [storyboard instantiateViewControllerWithIdentifier:@"SlidingViewController"];
                slidingVC.anchorRightRevealAmount = 276;
                slidingVC.anchorLeftRevealAmount = 276;
                slidingVC.topViewAnchoredGesture = ECSlidingViewControllerAnchoredGestureTapping | ECSlidingViewControllerAnchoredGesturePanning;
                UIViewController *menu = [storyboard instantiateViewControllerWithIdentifier:@"Menu"];
                
                if ([UIView respondsToSelector:@selector(userInterfaceLayoutDirectionForSemanticContentAttribute:)]) {
                    UIUserInterfaceLayoutDirection direction = [UIView userInterfaceLayoutDirectionForSemanticContentAttribute:slidingVC.view.semanticContentAttribute];
                    if (direction == UIUserInterfaceLayoutDirectionRightToLeft) {
                        slidingVC.underRightViewController = menu;
                    } else {
                         slidingVC.underLeftViewController = menu;
                    }
                } else { // iOS8
                     slidingVC.underLeftViewController = menu;
                }
                
                [self.window setRootViewController:slidingVC];
                self.slidingViewController = slidingVC;
                [[NSOperationQueue mainQueue] addOperation:[OpenModuleHomeOperation new]];
            } else {
                [prefs removeObjectForKey:@"configurationUrl"];
                [prefs synchronize];
                UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"ConfigurationSelectionStoryboard" bundle:nil];
                UINavigationController *navcontroller = [storyboard instantiateViewControllerWithIdentifier:@"ConfigurationSelector"];
                ConfigurationSelectionViewController *vc = navcontroller.childViewControllers[0];
                [vc setModalPresentationStyle:UIModalPresentationFullScreen];

                dispatch_async(dispatch_get_main_queue(), ^(void){
                    [self.window setRootViewController:navcontroller];
                    [ConfigurationFetcher showErrorAlertView];
                });
            }
        });
    }];
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
- (void) loadDefaultConfiguration:(NSString *)defaultConfigUrl inView:(ECSlidingViewController *) slidingViewController
{
    UIView * hudView = self.window.rootViewController.view;
    MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo: hudView animated:YES];
    hud.labelText = NSLocalizedString(@"Loading", @"loading message while waiting for data to load");
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 0.01 * NSEC_PER_SEC);
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
        ConfigurationManager *manager = [ConfigurationManager instance];
        [manager loadConfigurationWithConfigurationUrl:defaultConfigUrl completionHandler:^(id result){
            dispatch_async(dispatch_get_main_queue(), ^{
                [MBProgressHUD hideHUDForView:hudView animated:YES];
                
                NSNumber *resultNumber = nil;
                if ([result isKindOfClass:[NSNumber class]]) {
                    resultNumber = result;
                }
                BOOL resultBool = false;
                if (resultNumber) {
                    resultBool = [resultNumber boolValue];
                }

                if(resultBool) {
                    [AppearanceChanger applyAppearanceChanges:self.window];
                    [[NSOperationQueue mainQueue] addOperation:[OpenModuleHomeOperation new]];
                }
            });
        }];
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
//-(void) application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler
{
    
    NSString* uuid = [userInfo objectForKey:@"uuid"];
    NSLog(@"didReceiveRemoteNotification - for uuid: %@", uuid);
    
    UIApplicationState state = [application applicationState];
    if (state == UIApplicationStateActive) {
        NSLog(@"application active - show notification message alert");
        
        // log activity to Google Analytics
        [self sendEventWithCategory:kAnalyticsCategoryPushNotification withAction:kAnalyticsActionReceivedMessage withLabel:@"whileActive" withValue:nil];
        
        NSString* alertMessage = [[userInfo objectForKey:@"aps"] objectForKey:@"alert"];
        UIAlertController * alert=   [UIAlertController
                                      alertControllerWithTitle:NSLocalizedString(@"New Notification", @"new notification has arrived")
                                      message:alertMessage
                                      preferredStyle:UIAlertControllerStyleAlert];
        
        UIAlertAction* view = [UIAlertAction
                             actionWithTitle:NSLocalizedString(@"View", @"view label")
                             style:UIAlertActionStyleDefault
                             handler:^(UIAlertAction * action)
                             {
                                 OpenModuleOperation* operation = [[OpenModuleOperation alloc] initWithType:@"notifications"];
                                 operation.properties =  @ {@"uuid" : uuid } ;
                                 [[NSOperationQueue mainQueue] addOperation:operation];
                                 
                             }];
        UIAlertAction* cancel = [UIAlertAction
                                 actionWithTitle:NSLocalizedString(@"Close", @"Close")
                                 style:UIAlertActionStyleCancel
                                 handler:^(UIAlertAction * action)
                                 {
                                     [alert dismissViewControllerAnimated:YES completion:nil];
                                     
                                 }];
        
        [alert addAction:cancel];
        [alert addAction:view];
        
        [self.window makeKeyAndVisible];
        [self.window.rootViewController presentViewController:alert animated:YES completion:NULL];
    } else {
        // navigate to notifications
        NSLog(@"application not active - open from notifications");
        
        // log activity to Google Analytics
        [self sendEventWithCategory:kAnalyticsCategoryPushNotification withAction:kAnalyticsActionReceivedMessage withLabel:@"whileInActive" withValue:nil];
        
        [self.window.rootViewController dismissViewControllerAnimated:NO completion:nil];
        
        OpenModuleOperation* operation = [[OpenModuleOperation alloc] initWithType:@"notifications"];
        operation.properties =  @ {@"uuid" : uuid } ;
        [[NSOperationQueue mainQueue] addOperation:operation];
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


@end
