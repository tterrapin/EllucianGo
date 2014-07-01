//
//  LoginViewController.m
//  Mobile
//
//  Created by Alan McEwan on 9/10/12.
//  Copyright (c) 2012-2014 Ellucian. All rights reserved.
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

#define LOGIN_SUCCESS 200

@interface LoginViewController ()

@property (nonatomic, strong) NSString *url;

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
    
//    UIImage* image = [UIImage imageNamed:@"btn-home-sign-in"];
//    UIEdgeInsets insets = UIEdgeInsetsMake(0,11,0,11);
//    image = [image resizableImageWithCapInsets:insets];
//    [self.signInButton setBackgroundImage:image forState:UIControlStateNormal];
//    [self.cancelButton setBackgroundImage:image forState:UIControlStateNormal];
//    
//    UIImage* imagePressed = [UIImage imageNamed:@"btn-home-sign-in-pressed"];
//    imagePressed = [imagePressed resizableImageWithCapInsets:insets];
//    [self.signInButton setBackgroundImage:imagePressed forState:UIControlStateHighlighted];
//    [self.cancelButton setBackgroundImage:imagePressed forState:UIControlStateHighlighted];
    
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
    
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
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
    if(self.rememberUserSwitch.isOn) {
        [self sendEventWithCategory:kAnalyticsCategoryAuthentication withAction:kAnalyticsActionLogin withLabel:@"Authentication with save credential" withValue:nil forModuleNamed:nil];
    } else {
        [self sendEventWithCategory:kAnalyticsCategoryAuthentication withAction:kAnalyticsActionLogin withLabel:@"Authentication without save credential" withValue:nil forModuleNamed:nil];
    }
    
    NSArray *roles;
    NSInteger responseStatusCode = [self performLogin:self.url forUser:usernameField.text andPassword:passwordField.text andRememberUser:self.rememberUserSwitch.isOn returningRoles:&roles];

    if (responseStatusCode == LOGIN_SUCCESS )
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
            if(!match) {
                UIAlertView *alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Access Denied", nil)
                                                                message:NSLocalizedString(@"You do not have permission to use this feature.", nil)
                                                               delegate:nil
                                                      cancelButtonTitle:NSLocalizedString(@"OK", @"OK")
                                                      otherButtonTitles:nil];
                [alert show];
                
                [[NSNotificationCenter defaultCenter] postNotificationName:kSignInReturnToHomeNotification object:nil];
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
        
        [alert show];
    }

}

-(NSInteger) backgroundLogin
{
    CurrentUser *user = [CurrentUser sharedInstance];
    NSArray *roles;
    NSString *loginUrl = [[NSUserDefaults standardUserDefaults] objectForKey:@"login-url"];
    return [self performLogin:loginUrl forUser:[user userauth] andPassword:[user getPassword] andRememberUser:[user remember] returningRoles:&roles];

}

-(NSInteger) performLogin:(NSString *)urlString forUser:(NSString *)username andPassword:(NSString *)password andRememberUser:(BOOL)rememberUser returningRoles:(NSArray**)roles
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
        *roles = [json objectForKey:@"roles"];
        
        CurrentUser *user = [CurrentUser sharedInstance];
        [user login:authId andPassword:password andUserid:userId andRoles:[NSSet setWithArray:*roles] andRemember:rememberUser];
        
        NSDictionary *headers = [(NSHTTPURLResponse *)response allHeaderFields];
        NSArray *cookies = [NSHTTPCookie cookiesWithResponseHeaderFields:headers forURL:response.URL];
        for(NSHTTPCookie *cookie in cookies) {
            [[NSHTTPCookieStorage sharedHTTPCookieStorage] setCookie:cookie];
        }
        
        [[NSNotificationCenter defaultCenter] postNotificationName:kLoginExecutorSuccess object:nil];
        
        // register the device if needed
        [NotificationManager registerDeviceIfNeeded];
    }
    
    return responseStatusCode;
}

@end