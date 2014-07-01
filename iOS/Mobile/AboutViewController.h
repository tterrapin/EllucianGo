//
//  AboutViewController.h
//  Mobile
//
//  Created by Alan McEwan on 8/17/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>

@class PseudoButtonView;

@interface AboutViewController : UIViewController

@property (weak, nonatomic) IBOutlet UIView *backgroundView;

@property (weak, nonatomic) IBOutlet UIImageView *schoolLogo;

@property (strong, nonatomic) IBOutlet UILabel *serverVersion;
@property (strong, nonatomic) IBOutlet UILabel *clientVersion;
@property (weak, nonatomic) IBOutlet UILabel *clientVersionLabel;
@property (weak, nonatomic) IBOutlet UILabel *serverVersionLabel;

@property (strong, nonatomic) IBOutlet UITextView *contactTextView;

@property (strong, nonatomic) IBOutlet UIButton *poweredByButton;
@property (strong, nonatomic) IBOutlet UIButton *ellPrivacyButton;

@property (weak, nonatomic) IBOutlet UILabel *phoneLabel;
@property (weak, nonatomic) IBOutlet UILabel *emailLabel;
@property (weak, nonatomic) IBOutlet UILabel *websiteLabel;
@property (weak, nonatomic) IBOutlet UILabel *privacyPolicyLabel;
@property (weak, nonatomic) IBOutlet UIView *separatorAfterPhoneView;
@property (weak, nonatomic) IBOutlet UIView *separatorAfterEmailView;
@property (weak, nonatomic) IBOutlet UIView *separatorAfterWebsiteView;

@property (weak, nonatomic) IBOutlet PseudoButtonView *phoneView;
@property (weak, nonatomic) IBOutlet PseudoButtonView *emailView;
@property (weak, nonatomic) IBOutlet PseudoButtonView *websiteView;
@property (weak, nonatomic) IBOutlet PseudoButtonView *privacyPolicyView;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *separatorAfterPhoneHeightConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *separatorAfterEmailHeightConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *separatorAfterWebsiteHeightConstraint;
@property (weak, nonatomic) IBOutlet UILabel *phoneLabelLabel;
@property (weak, nonatomic) IBOutlet UILabel *emailLabelLabel;
@property (weak, nonatomic) IBOutlet UILabel *privacyPolicyLabelLabel;

@property (weak, nonatomic) IBOutlet UILabel *websiteLabelLabel;
@property (weak, nonatomic) IBOutlet UIToolbar *toolbar;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *widthConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *contactTextViewHeightConstraint;
@property (weak, nonatomic) IBOutlet UIScrollView *scrollView;

@end
