//
//  CurrentUser.m
//  Mobile
//
//  Created by Alan McEwan on 9/13/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "CurrentUser.h"
#import "KeychainWrapper.h"
#import "AppDelegate.h"
#import "RNEncryptor.h"
#import "RNDecryptor.h"
#import "NotificationsFetcher.h"
#import "Ellucian_GO-Swift.h"

#define kAES256Key @"key"

@implementation CurrentUser

@synthesize email;

- (id)init
{
    self = [super init];
    NSUserDefaults* defaults = [AppGroupUtilities userDefaults];

    NSError *decryptionError = nil;
    NSData *decryptedUserAuthData = [RNDecryptor decryptData:[defaults objectForKey:kLoginUserauth] withPassword:kAES256Key error:&decryptionError];
    _userauth = [[NSString alloc] initWithData:decryptedUserAuthData encoding:NSUTF8StringEncoding];

    NSData *decryptedUserIdData = [RNDecryptor decryptData:[defaults objectForKey:kLoginUserid] withPassword:kAES256Key error:&decryptionError];
    _userid = [[NSString alloc] initWithData:decryptedUserIdData encoding:NSUTF8StringEncoding];

    _roles = [defaults objectForKey:kLoginRoles];
    _remember = [defaults boolForKey:kLoginRemember];
    _password = [self getPassword];
    _isLoggedIn = _password != nil;

    return self;
}

-(void)logoutWithoutUpdatingUI
{
    [self logoutWithNotification:NO requestedByUser:YES];
}

-(void)logout:(BOOL)requestedByUser
{
    [self logoutWithNotification:YES requestedByUser:requestedByUser];
}


-(void)logoutWithNotification:(BOOL)postNotification requestedByUser:(BOOL)requestedByUser
{
    //logging out resets this user's attributes and removes it from the keychain
    self.isLoggedIn  = false;
    self.userid      = nil;
    self.userauth    = nil;
    self.roles       = nil;
    self.remember    = NO;
    
    NSError *error = nil;
    
    [KeychainWrapper deleteItemForUsername:[self sha1:self.userauth]
                            andServiceName:[[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleIdentifier"]
                                     error:&error];
    
    NSUserDefaults* defaults = [AppGroupUtilities userDefaults];
    //set the new config for about
    [defaults removeObjectForKey:kLoginRoles];
    [defaults removeObjectForKey:kLoginRemember];
    [defaults removeObjectForKey:kLoginUserauth];
    [defaults removeObjectForKey:kLoginUserid];
    
    NSUserDefaults *appGroupUserDefaults = [AppGroupUtilities userDefaults];
    [appGroupUserDefaults removeObjectForKey:kLoginUserauth];
    [appGroupUserDefaults removeObjectForKey:kLoginUserid];
    [appGroupUserDefaults removeObjectForKey:kLoginRoles];
    [appGroupUserDefaults synchronize];
    
    //remove persisted cookies
    NSHTTPCookieStorage* cookies = [NSHTTPCookieStorage sharedHTTPCookieStorage];
    
    NSArray *allCookies = [cookies cookies];
    
    //remove all cookies persisted in app groups
    [appGroupUserDefaults removeObjectForKey:@"cookieArray"];
    
    for(NSHTTPCookie *cookie in allCookies) {
        [cookies deleteCookie:cookie];
    }
    
    [self removeSensitiveData:requestedByUser];
    [[UIApplication sharedApplication] cancelAllLocalNotifications];
    [UIApplication sharedApplication].applicationIconBadgeNumber = 0;
    
    if(postNotification) {
        [[NSNotificationCenter defaultCenter] postNotificationName:kSignOutNotification object:nil];
    }
}

- (void) login: (NSString *) auth andPassword: (NSString *) pass andUserid: (NSString *) uID andRoles: (NSSet *) roleSet andRemember: (BOOL) remember
{
    NSError *error = nil;
    
    self.userauth = auth;
    self.userid = uID;
    self.roles = roleSet;
    self.isLoggedIn = true;
    self.remember = remember;

    //logging in the user stores the user in the keychain
    [KeychainWrapper storeUsername:[self sha1:self.userauth]
                       andPassword:pass
                    forServiceName:[[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleIdentifier"]
                    updateExisting:true
                             error:&error];
    
    NSUserDefaults* defaults = [AppGroupUtilities userDefaults];
    //set the new config for about
    [defaults setObject:[roleSet allObjects] forKey:kLoginRoles];
    [defaults setBool:self.remember forKey:kLoginRemember];
    NSData* encryptedUserAuthData = [self.userauth dataUsingEncoding:NSUTF8StringEncoding];
    encryptedUserAuthData = [RNEncryptor encryptData:encryptedUserAuthData
                                      withSettings:kRNCryptorAES256Settings
                                          password:kAES256Key
                                             error:&error];

    [defaults setObject:encryptedUserAuthData forKey:kLoginUserauth];
    NSData* encryptedUserIdData = [self.userid dataUsingEncoding:NSUTF8StringEncoding];
    encryptedUserIdData = [RNEncryptor encryptData:encryptedUserIdData
                                        withSettings:kRNCryptorAES256Settings
                                            password:kAES256Key
                                               error:&error];

    [defaults setObject:encryptedUserIdData forKey:kLoginUserid];

    self.lastLoggedInDate = [NSDate date];
    
    [[NSNotificationCenter defaultCenter] postNotificationName:kSignInNotification object:nil];
}

- (void) login: (NSString *) auth andUserid: (NSString *) uID andRoles: (NSSet *) roleSet
{
    NSError *error = nil;
    
    self.userauth = auth;
    self.userid = uID;
    self.roles = roleSet;
    self.isLoggedIn = true;
          
    NSUserDefaults* defaults = [AppGroupUtilities userDefaults];
    //set the new config for about
    [defaults setObject:[roleSet allObjects] forKey:kLoginRoles];
    NSData* encryptedUserAuthData = [self.userauth dataUsingEncoding:NSUTF8StringEncoding];
    encryptedUserAuthData = [RNEncryptor encryptData:encryptedUserAuthData
                                        withSettings:kRNCryptorAES256Settings
                                            password:kAES256Key
                                               error:&error];
    
    [defaults setObject:encryptedUserAuthData forKey:kLoginUserauth];
    NSData* encryptedUserIdData = [self.userid dataUsingEncoding:NSUTF8StringEncoding];
    encryptedUserIdData = [RNEncryptor encryptData:encryptedUserIdData
                                      withSettings:kRNCryptorAES256Settings
                                          password:kAES256Key
                                             error:&error];
    
    [defaults setObject:encryptedUserIdData forKey:kLoginUserid];

    self.lastLoggedInDate = [NSDate date];
          
    [[NSNotificationCenter defaultCenter] postNotificationName:kSignInNotification object:nil];
}

-(NSString *)getPassword
{
    NSError *error = nil;
    return [KeychainWrapper getPasswordForUsername: [self sha1:self.userauth]
                                        andServiceName:[[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleIdentifier"]
                                             error:&error];
}

-(NSSet *)getRoles
{
    if (self.isLoggedIn)
    {
        NSUserDefaults* defaults = [AppGroupUtilities userDefaults];
        //set the new config for about
        NSArray* array = [defaults objectForKey:@"roles"];
        return [NSSet setWithArray:array];
    }
    else
        return [[NSSet alloc] init];
}

-(NSString*) sha1:(NSString*)input
{
    const char *cstr = [input cStringUsingEncoding:NSUTF8StringEncoding];
    NSData *data = [NSData dataWithBytes:cstr length:input.length];
    
    uint8_t digest[CC_SHA1_DIGEST_LENGTH];
    
    CC_SHA1(data.bytes, (CC_LONG)data.length, digest);
    
    NSMutableString* output = [NSMutableString stringWithCapacity:CC_SHA1_DIGEST_LENGTH * 2];
    
    for(int i = 0; i < CC_SHA1_DIGEST_LENGTH; i++)
        [output appendFormat:@"%02x", digest[i]];
    
    return output;
    
}

-(void) removeSensitiveData:(BOOL)requestedByUser
{
    NSArray *entities = @[ @"CourseAnnouncement", @"CourseDetail", @"CourseEvent", @"CourseRoster", @"CourseTerm", @"GradeTerm", @"Notification"];
    for(NSString *entity in entities) {
        [self removeData:entity];
        if([entity isEqualToString:@"Notification"]) {
            [[NSNotificationCenter defaultCenter] postNotificationName:kNotificationsUpdatedNotification object:nil];
        }
    }
    if(requestedByUser) {
        [self removeData:@"CourseAssignment"];
        NSUserDefaults *appGroupUserDefaults = [AppGroupUtilities userDefaults];
        [appGroupUserDefaults removeObjectForKey:@"today-widget-assignments"];
        [appGroupUserDefaults synchronize];
        
    }
}

-(void) removeData:(NSString *)entity
{
    AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
    NSManagedObjectContext *managedObjectContext = appDelegate.managedObjectContext;
    NSFetchRequest * request = [[NSFetchRequest alloc] init];
    [request setEntity:[NSEntityDescription entityForName:entity inManagedObjectContext:managedObjectContext]];
    [request setIncludesPropertyValues:NO]; //only fetch the managedObjectID
    
    NSError * error = nil;
    NSArray * objects = [managedObjectContext executeFetchRequest:request error:&error];
    for (NSManagedObject *object in objects) {
        [managedObjectContext deleteObject:object];
    }
    [managedObjectContext save:&error];
}

+ (id)sharedInstance
{
    // structure used to test whether the block has completed or not
    static dispatch_once_t p = 0;
    
    // initialize sharedObject as nil (first call only)
    __strong static id _sharedObject = nil;
    
    // executes a block object once and only once for the lifetime of an application
    dispatch_once(&p, ^{
        _sharedObject = [[self alloc] init];
    });
    
    // returns the same object each time
    return _sharedObject;
}


@end
