//
//  LoginViewController.m
//  Mobile
//
//  Created by Alan McEwan on 9/10/12.
//  Copyright (c) 2012-2015 Ellucian. All rights reserved.
//

#import "LoginViewController.h"
#import "SlidingViewController.h"
#import "Base64.h"
#import "AppDelegate.h"
#import "CurrentUser.h"
#import "ModuleRole.h"
#import "LoginExecutor.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "NotificationManager.h"
#import "Ellucian_GO-Swift.h"

#define LOGIN_SUCCESS 200

@interface LoginViewController ()

@property (nonatomic, strong) NSString *url;
@property (nonatomic, assign) NSHTTPURLResponse *httpResponse;
@property (nonatomic, assign) BOOL canceled;

@end

@implementation LoginViewController
@synthesize usernameField;
@synthesize passwordField;

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.navigationController.navigationBar.translucent = NO;

    self.usernameField.leftView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"login_username"]];
    self.usernameField.leftViewMode = UITextFieldViewModeAlways;
    self.passwordField.leftView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"login_password"]];
    self.passwordField.leftViewMode = UITextFieldViewModeAlways;
    
    self.signInButton.tintColor =  [UIColor primaryColor];
    [[self.signInButton layer] setCornerRadius:10.0f];
    [[self.signInButton layer] setBorderWidth:5.0f];
    [self.signInButton.layer setBorderColor:[[UIColor grayColor] CGColor]];
    self.signInButton.backgroundColor = [UIColor primaryColor];
    [self.signInButton setTitleColor:[UIColor whiteColor] forState: UIControlStateNormal];
    [self.signInButton setTitleColor:[UIColor headerTextColor] forState: UIControlStateHighlighted];
    
    self.cancelButton.tintColor =  [UIColor primaryColor];
    [[self.cancelButton layer] setCornerRadius:10.0f];
    [[self.cancelButton layer] setBorderWidth:5.0f];
    [self.cancelButton.layer setBorderColor:[[UIColor grayColor] CGColor]];
    self.cancelButton.backgroundColor = [UIColor primaryColor];
    [self.cancelButton setTitleColor:[UIColor whiteColor] forState: UIControlStateNormal];
    [self.cancelButton setTitleColor:[UIColor headerTextColor] forState: UIControlStateHighlighted];

    self.view.backgroundColor = [UIColor accentColor];
    self.rememberMeLabel.textColor = [UIColor subheaderTextColor];
    self.contactInstitutionLabel.textColor = [UIColor subheaderTextColor];
    
    NSUserDefaults *defaults = [AppGroupUtilities userDefaults];
    self.url = [defaults objectForKey:@"login-url"];
    
    self.usernameField.text = [[CurrentUser sharedInstance] userauth];
    self.rememberUserSwitch.on = [[CurrentUser sharedInstance] remember];
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Sign In Page" forModuleNamed:nil];
}

- (IBAction)signInCanceled:(id)sender
{
    [self.activityIndicator stopAnimating];
    self.canceled = YES;
    [self sendEventWithCategory:kAnalyticsCategoryAuthentication withAction:kAnalyticsActionCancel withLabel:@"Click Cancel" withValue:nil forModuleNamed:nil];
    //For cases where the user was previously signed in and timedout and canceled the prompt
    [[CurrentUser sharedInstance] logoutWithoutUpdatingUI];
    [[NSNotificationCenter defaultCenter] postNotificationName:kSignInReturnToHomeNotification object:nil];
    [self dismissViewControllerAnimated:YES completion: nil];
}

- (IBAction)textFieldDoneEditing:(id)sender
{
    [sender resignFirstResponder];
    [self signIn:sender];
}

- (IBAction)progressToPasswordField:(id)sender
{
    [usernameField resignFirstResponder];
    [passwordField becomeFirstResponder];
}

- (IBAction)backgroundTap:(id)sender
{
    [usernameField resignFirstResponder];
    [passwordField resignFirstResponder];
}

- (IBAction)signIn:(id)sender {
    [self.activityIndicator startAnimating];
    if(self.rememberUserSwitch.isOn) {
        [self sendEventWithCategory:kAnalyticsCategoryAuthentication withAction:kAnalyticsActionLogin withLabel:@"Authentication with save credential" withValue:nil forModuleNamed:nil];
    } else {
        [self sendEventWithCategory:kAnalyticsCategoryAuthentication withAction:kAnalyticsActionLogin withLabel:@"Authentication without save credential" withValue:nil forModuleNamed:nil];
    }
    self.signInButton.enabled = NO;
    
    __block NSHTTPURLResponse *blockResponse = self.httpResponse;
    dispatch_async(dispatch_get_global_queue(0,0), ^{
        blockResponse = [self performLogin:self.url forUser:usernameField.text andPassword:passwordField.text andRememberUser:self.rememberUserSwitch.isOn];
        self.httpResponse = blockResponse;
        [self finishSignIn];
    });
}

-(void) finishSignIn {
    [self.activityIndicator performSelectorOnMainThread:@selector(stopAnimating) withObject:nil waitUntilDone:YES];
    if(self.canceled) {
        return;
    } else if ([self.httpResponse statusCode] == LOGIN_SUCCESS)
    {
        BOOL match = NO;
        if(self.access) {
            for(ModuleRole *role in self.access) {
                if([[CurrentUser sharedInstance].roles containsObject:role.role]) {
                    match = YES;
                    break;
                } else if ([role.role isEqualToString:@"Everyone"]) {
                    match = YES;
                    break;
                }
            }
            if([self.access count] == 0) { //upgrades from 3.0 or earlier
                match = YES;
            }
            if(!match) {
                
                dispatch_async(dispatch_get_main_queue(), ^{
                    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Access Denied", nil)
                                                                    message:NSLocalizedString(@"You do not have permission to use this feature.", nil)
                                                                   delegate:nil
                                                          cancelButtonTitle:NSLocalizedString(@"OK", @"OK")
                                                          otherButtonTitles:nil];
                    [alert performSelectorOnMainThread:@selector(show) withObject:nil waitUntilDone:YES];
                    [[NSNotificationCenter defaultCenter] postNotificationName:kSignInReturnToHomeNotification object:nil];
                });
                
            }
        }
        [self dismissViewControllerAnimated:YES completion: nil];
    }
    else //display an alert
    {
        UIAlertView *alert = [[UIAlertView alloc]
                              initWithTitle:NSLocalizedString(@"Sign In Failed", @"title for failed sign in")
                              message:NSLocalizedString(@"The password or user name you entered is incorrect. Please try again.", @"message for failed sign in")
                              delegate:self
                              cancelButtonTitle:NSLocalizedString(@"OK", @"OK")
                              otherButtonTitles:nil];
        self.signInButton.enabled = YES;
        
        [alert performSelectorOnMainThread:@selector(show) withObject:nil waitUntilDone:YES];
    }

}

-(NSInteger) backgroundLogin
{
    CurrentUser *user = [CurrentUser sharedInstance];
    NSString *loginUrl = [[AppGroupUtilities userDefaults] objectForKey:@"login-url"];
    NSHTTPURLResponse *response = [self performLogin:loginUrl forUser:[user userauth] andPassword:[user getPassword] andRememberUser:[user remember]];
    return response.statusCode;
}

-(NSHTTPURLResponse *) performLogin:(NSString *)urlString forUser:(NSString *)username andPassword:(NSString *)password andRememberUser:(BOOL)rememberUser
{
    NSError *error;
    NSURLResponse *response;

    // create a plaintext string in the format username:password
    NSString *loginString = [NSString stringWithFormat:@"%@:%@", username, password];
    NSString *encodedLoginData = [Base64 encode:[loginString dataUsingEncoding:NSUTF8StringEncoding]];
    NSString *authHeader = [@"Basic " stringByAppendingFormat:@"%@", encodedLoginData];

    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL: [NSURL URLWithString:urlString]
                                                           cachePolicy: NSURLRequestReloadIgnoringCacheData
                                                       timeoutInterval: 90];
    
    // add the header to the request.
    [request addValue:authHeader forHTTPHeaderField:@"Authorization"];
    
    NSData *data = [NSURLConnection
                    sendSynchronousRequest: request
                    returningResponse: &response
                    error: &error];
    
    
    NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *)response;
    
    NSInteger responseStatusCode = [httpResponse statusCode];
    if (responseStatusCode == 200 )
    {
        NSDictionary* json = [NSJSONSerialization
                              JSONObjectWithData:data
                              options:kNilOptions
                              error:&error];

        NSString *userId = [json objectForKey:@"userId"];
        NSString *authId = [json objectForKey:@"authId"];
        NSArray *roles = [json objectForKey:@"roles"];
        
        CurrentUser *user = [CurrentUser sharedInstance];
        [user login:authId andPassword:password andUserid:userId andRoles:[NSSet setWithArray:roles] andRemember:rememberUser];
        
        NSDictionary *headers = [(NSHTTPURLResponse *)response allHeaderFields];
        NSArray *cookies = [NSHTTPCookie cookiesWithResponseHeaderFields:headers forURL:response.URL];
        for(NSHTTPCookie *cookie in cookies) {
            [[NSHTTPCookieStorage sharedHTTPCookieStorage] setCookie:cookie];
        }

        //save cookies
        NSMutableArray *cookieArray = [[NSMutableArray alloc] init];
        for (NSHTTPCookie *cookie in [[NSHTTPCookieStorage sharedHTTPCookieStorage] cookies]) {
            NSMutableDictionary *cookieProperties = [NSMutableDictionary dictionary];
            [cookieProperties setObject:cookie.name forKey:NSHTTPCookieName];
            [cookieProperties setObject:cookie.value forKey:NSHTTPCookieValue];
            [cookieProperties setObject:cookie.domain forKey:NSHTTPCookieDomain];
            [cookieProperties setObject:cookie.path forKey:NSHTTPCookiePath];
            [cookieProperties setObject:[NSNumber numberWithInt:(int)cookie.version] forKey:NSHTTPCookieVersion];
            
            if( cookie.expiresDate) {
                [cookieProperties setObject:cookie.expiresDate forKey:NSHTTPCookieExpires];
            }

            [cookieArray addObject:cookieProperties];
            
        }
        [[AppGroupUtilities userDefaults] setValue:cookieArray forKey:@"cookieArray"];
        
        [[NSNotificationCenter defaultCenter] postNotificationName:kLoginExecutorSuccess object:nil];
        
        // register the device if needed
        [NotificationManager registerDeviceIfNeeded];
    }
    
    return httpResponse;
}

@end