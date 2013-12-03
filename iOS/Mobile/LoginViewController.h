//
//  LoginViewController.h
//  Mobile
//
//  Created by Alan McEwan on 9/10/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "HomeViewController.h"

@interface LoginViewController : UIViewController

@property (nonatomic, strong) NSData *receivedData;
@property (strong, nonatomic) IBOutlet UITextField *usernameField;
@property (strong, nonatomic) IBOutlet UITextField *passwordField;
@property (weak, nonatomic) IBOutlet UIButton *cancelButton;
@property (weak, nonatomic) IBOutlet UIButton *signInButton;
@property (weak, nonatomic) IBOutlet UISwitch *rememberUserSwitch;
//@property (strong, nonatomic) NSSet *rolesForNextModule;
@property (weak, nonatomic) IBOutlet UILabel *rememberMeLabel;


- (IBAction)signInCanceled:(id)sender;
- (IBAction)signIn:(id)sender;
- (IBAction)backgroundTap:(id)sender;
- (IBAction)textFieldDoneEditing:(id)sender;
- (IBAction)progressToPasswordField:(id)sender;


@end
