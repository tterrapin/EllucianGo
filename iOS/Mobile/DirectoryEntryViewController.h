//
//  DirectoryEntryViewController.h
//  Mobile
//
//  Created by Jason Hocker on 10/5/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DirectoryEntry.h"
#import "DirectoryEntryViewController.h"
#import "AsynchronousImageView.h"
#import "UIColor+SchoolCustomization.h"
#import <AddressBookUI/AddressBookUI.h>
#import "Module.h"
#import "PseudoButtonView.h"

@interface DirectoryEntryViewController : UIViewController <UITextViewDelegate, UITableViewDelegate, ABUnknownPersonViewControllerDelegate>

@property (nonatomic, strong) DirectoryEntry *entry;
@property (strong, nonatomic) Module *module;

@property (weak, nonatomic) IBOutlet UIView *backgroundView;
@property (weak, nonatomic) IBOutlet UILabel *nameLabel;
@property (weak, nonatomic) IBOutlet UILabel *titleLabel;
@property (weak, nonatomic) IBOutlet UILabel *phoneLabel;
@property (weak, nonatomic) IBOutlet UILabel *mobileLabel;
@property (weak, nonatomic) IBOutlet UILabel *officeLabel;
@property (weak, nonatomic) IBOutlet UILabel *departmentLabel;
@property (weak, nonatomic) IBOutlet UILabel *emailLabel;
@property (weak, nonatomic) IBOutlet UILabel *addressLabel;

@property (weak, nonatomic) IBOutlet UIView *separatorAfterPhoneView;
@property (weak, nonatomic) IBOutlet UIView *separatorAfterMobileView;
@property (weak, nonatomic) IBOutlet UIView *separatorAfterOfficeView;
@property (weak, nonatomic) IBOutlet UIView *separatorAfterDepartmentView;
@property (weak, nonatomic) IBOutlet UIView *separatorAfterEmailView;

@property (weak, nonatomic) IBOutlet PseudoButtonView *phoneView;
@property (weak, nonatomic) IBOutlet PseudoButtonView *mobileView;
@property (weak, nonatomic) IBOutlet UIView *officeView;
@property (weak, nonatomic) IBOutlet UIView *departmentView;
@property (weak, nonatomic) IBOutlet PseudoButtonView *emailView;
@property (weak, nonatomic) IBOutlet UIView *addressView;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *separatorAfterPhoneHeightConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *separatorAfterMobileHeightConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *separatorAfterOfficeHeightConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *separatorAfterDepartmentHeightConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *separatorAfterEmailHeightConstraint;

- (IBAction)addToAddressBook:(id)sender;
@property (weak, nonatomic) IBOutlet UILabel *addressLabelLabel;

@property (weak, nonatomic) IBOutlet UILabel *phoneLabelLabel;
@property (weak, nonatomic) IBOutlet UILabel *mobileLabelLabel;
@property (weak, nonatomic) IBOutlet UILabel *officeLabelLabel;
@property (weak, nonatomic) IBOutlet UILabel *departmentLabelLabel;
@property (weak, nonatomic) IBOutlet UILabel *emailLabelLabel;
@property (weak, nonatomic) IBOutlet UIScrollView *scrollView;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *widthConstraint;

@end
