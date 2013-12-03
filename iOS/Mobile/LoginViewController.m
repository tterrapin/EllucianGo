//
//  LoginViewController.m
//  Mobile
//
//  Created by Alan McEwan on 9/10/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "LoginViewController.h"
#import "SlidingViewController.h"
#import "Base64.h"
#import "AppDelegate.h"
#import "CurrentUser.h"
#import "ModuleRole.h"
#import "NSData+AuthenticatedRequest.h"
#import "LoginExecutor.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"

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
    
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    self.url = [defaults objectForKey:@"login-url"];
    
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Sign In Page" forModuleNamed:nil];
}

- (IBAction)signInCanceled:(id)sender
{
    [self sendEventWithCategory:kAnalyticsCategoryAuthentication withAction:kAnalyticsActionCancel withLabel:@"Click Cancel" withValue:nil forModuleNamed:nil];
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
    
    LoginExecutor *executor = [[LoginExecutor alloc] init];
    NSInteger responseStatusCode = [executor performLogin:self.url forUser:usernameField.text andPassword:passwordField.text andRememberUser:self.rememberUserSwitch.isOn returningRoles:&roles];

    if (responseStatusCode == LOGIN_SUCCESS )
    {
        /*
        BOOL match = self.rolesForNextModule.count == 0;
        if(self.rolesForNextModule) {
            for(ModuleRole *role in self.rolesForNextModule) {
                if([roles containsObject:role.role]) {
                    match = YES;
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
        */
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


@end