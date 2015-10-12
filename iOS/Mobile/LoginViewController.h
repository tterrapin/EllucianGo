//
//  LoginViewController.h
//  Mobile
//
//  Created by Alan McEwan on 9/10/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "LoginProtocol.h"

@interface LoginViewController : UIViewController<LoginProtocol>

@property (strong, nonatomic) IBOutlet UITextField *usernameField;
@property (strong, nonatomic) IBOutlet UITextField *passwordField;
@property (weak, nonatomic) IBOutlet UIButton *cancelButton;
@property (weak, nonatomic) IBOutlet UIButton *signInButton;
@property (weak, nonatomic) IBOutlet UISwitch *rememberUserSwitch;
@property (weak, nonatomic) IBOutlet UILabel *rememberMeLabel;
@property (weak, nonatomic) IBOutlet UILabel *contactInstitutionLabel;
@property (strong, nonatomic) IBOutlet UIActivityIndicatorView *activityIndicator;
@property (nonatomic, copy) void (^completionBlock)(void);

- (IBAction)signInCanceled:(id)sender;
- (IBAction)signIn:(id)sender;

- (IBAction)textFieldDoneEditing:(id)sender;
- (IBAction)progressToPasswordField:(id)sender;
-(NSInteger) backgroundLogin;

@end
