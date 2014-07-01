//
//  DirectoryEntryViewController.m
//  Mobile
//
//  Created by Jason Hocker on 10/5/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "DirectoryEntryViewController.h"
#import <AddressBook/AddressBook.h>
#import <AddressBookUI/AddressBookUI.h>
#import "DirectoryEntry.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "AppearanceChanger.h"

@implementation DirectoryEntryViewController

- (void) viewDidLoad
{
    [super viewDidLoad];
    
    self.navigationController.navigationBar.translucent = NO;
    
    self.backgroundView.backgroundColor = [UIColor accentColor];
    
    if([AppearanceChanger isRTL]) {
        self.phoneLabelLabel.textAlignment = NSTextAlignmentRight;
        self.mobileLabelLabel.textAlignment = NSTextAlignmentRight;
        self.officeLabelLabel.textAlignment = NSTextAlignmentRight;
        self.departmentLabelLabel.textAlignment = NSTextAlignmentRight;
        self.emailLabelLabel.textAlignment = NSTextAlignmentRight;
        self.addressLabel.textAlignment = NSTextAlignmentRight;
    }
    
    self.separatorAfterPhoneView.backgroundColor = [UIColor accentColor];
    self.separatorAfterMobileView.backgroundColor = [UIColor accentColor];
    self.separatorAfterOfficeView.backgroundColor = [UIColor accentColor];
    self.separatorAfterDepartmentView.backgroundColor = [UIColor accentColor];
    self.separatorAfterEmailView.backgroundColor = [UIColor accentColor];
    
    self.nameLabel.textColor = [UIColor subheaderTextColor];
    self.titleLabel.textColor = [UIColor subheaderTextColor];
    
    self.nameLabel.text = [self.entry nameOrderedByFirstName:YES];
    self.titleLabel.text = self.entry.title;
    
    NSLayoutConstraint *lastSeparator = nil;
    
    if(self.entry.phone) {
        self.phoneLabel.text = self.entry.phone;
        [self.phoneView setAction:@selector(tapPhone:) withTarget:self];
        lastSeparator = self.separatorAfterPhoneHeightConstraint;
    } else {
        [[self.phoneView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
        self.separatorAfterPhoneHeightConstraint.constant = 0;
        [self.phoneView addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"V:[view(0)]"
                                                 options:0 metrics:nil
                                                   views:@{@"view":self.phoneView}]];
    }
    if(self.entry.mobile) {
        self.mobileLabel.text = self.entry.mobile;
        [self.mobileView setAction:@selector(tapMobile:) withTarget:self];
        lastSeparator = self.separatorAfterMobileHeightConstraint;
    } else {
        [[self.mobileView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
        self.separatorAfterMobileHeightConstraint.constant = 0;
        [self.mobileView addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"V:[view(0)]"
                                                 options:0 metrics:nil
                                                   views:@{@"view":self.mobileView}]];
    }
    if(self.entry.office) {
        self.officeLabel.text = self.entry.office;
        lastSeparator = self.separatorAfterOfficeHeightConstraint;
    } else {
        [[self.officeView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
        self.separatorAfterOfficeHeightConstraint.constant = 0;
        [self.officeView addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"V:[view(0)]"
                                                 options:0 metrics:nil
                                                   views:@{@"view":self.officeView}]];
    }
    if(self.entry.department) {
        self.departmentLabel.text = self.entry.department;
        lastSeparator = self.separatorAfterDepartmentHeightConstraint;
    } else {
        [[self.departmentView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
        self.separatorAfterDepartmentHeightConstraint.constant = 0;
        [self.departmentView addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"V:[view(0)]"
                                                 options:0 metrics:nil
                                                   views:@{@"view":self.departmentView}]];
    }
    if(self.entry.email) {
        self.emailLabel.text = self.entry.email;
        [self.emailView setAction:@selector(tapEmail:) withTarget:self];
        lastSeparator = self.separatorAfterEmailHeightConstraint;
    } else {
        [[self.emailView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
        self.separatorAfterEmailHeightConstraint.constant = 0;
        [self.emailView addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"V:[view(0)]"
                                                 options:0 metrics:nil
                                                   views:@{@"view":self.emailView}]];
    }
    if([self getAddress]) {
        self.addressLabel.text = [self getAddress];
    } else {
        [[self.addressView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
        lastSeparator.constant = 0;
        [self.addressView addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"V:[view(0)]"
                                                 options:0 metrics:nil
                                                   views:@{@"view":self.addressView}]];
    }
    
    self.widthConstraint.constant = [AppearanceChanger sizeInOrientation:self.interfaceOrientation].width;
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Directory card" forModuleNamed:self.module.name];
    
    self.widthConstraint.constant = [AppearanceChanger sizeInOrientation:self.interfaceOrientation].width;
}

-(void)tapEmail:(id)sender
{
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionInvoke_Native withLabel:@"Send e-mail" withValue:nil forModuleNamed:self.module.name];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"mailto://%@",self.entry.email]]];
}

-(void)tapPhone:(id)sender
{
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionInvoke_Native withLabel:@"Call Phone Number" withValue:nil forModuleNamed:self.module.name];
    NSString *phone = [[self.entry.phone componentsSeparatedByCharactersInSet: [NSCharacterSet characterSetWithCharactersInString:@"() -"]] componentsJoinedByString: @""];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", phone]]];
}

-(void)tapMobile:(id)sender
{
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionInvoke_Native withLabel:@"Call Phone Number" withValue:nil forModuleNamed:self.module.name];
    NSString *phone = [[self.entry.mobile componentsSeparatedByCharactersInSet: [NSCharacterSet characterSetWithCharactersInString:@"() -"]] componentsJoinedByString: @""];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", phone]]];

}

- (IBAction)addToAddressBook:(id)sender {
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionInvoke_Native withLabel:@"Add contact" withValue:nil forModuleNamed:self.module.name];
    ABAddressBookRef ab = NULL;

    if(&ABAddressBookCreateWithOptions) {
        ab = ABAddressBookCreateWithOptions(NULL, NULL);
    }
    __block BOOL accessGranted = NO;
    if(ab) {

        if (ABAddressBookRequestAccessWithCompletion != NULL) { // we're on iOS 6
            dispatch_semaphore_t sema = dispatch_semaphore_create(0);
            ABAddressBookRequestAccessWithCompletion(ab, ^(bool granted, CFErrorRef error) {
                accessGranted = granted;
                dispatch_semaphore_signal(sema);
            });
            dispatch_semaphore_wait(sema, DISPATCH_TIME_FOREVER);
            //dispatch_release(sema);
        }
        else { // we're on iOS 5 or older
            accessGranted = YES;
        }
    }
     
    if (accessGranted) {

        ABRecordRef person = ABPersonCreate(); // create a person
        
        CFErrorRef  anError = NULL;
        ABMutableMultiValueRef phoneNumberMultiValue =
        ABMultiValueCreateMutable(kABMultiStringPropertyType);
        if(self.entry.mobile)
            ABMultiValueAddValueAndLabel(phoneNumberMultiValue ,(__bridge CFTypeRef)self.entry.mobile,kABPersonPhoneMobileLabel, NULL);
        if(self.entry.phone)
            ABMultiValueAddValueAndLabel(phoneNumberMultiValue ,(__bridge CFTypeRef)self.entry.phone,kABPersonPhoneMainLabel, NULL);
        ABRecordSetValue(person, kABPersonPhoneProperty, phoneNumberMultiValue, nil);
        CFRelease(phoneNumberMultiValue);
        
        // Address
        unsigned int addressRowCount = 0;
        if(self.entry.street) addressRowCount++;
        if(self.entry.city) addressRowCount++;
        if(self.entry.state) addressRowCount++;
        if(self.entry.postalCode) addressRowCount++;
        if(self.entry.country) addressRowCount++;
        if(addressRowCount > 0) {
            ABMutableMultiValueRef address = ABMultiValueCreateMutable(kABDictionaryPropertyType);
            CFStringRef keys[addressRowCount];
            CFStringRef values[addressRowCount];
            NSUInteger position = 0;
            if(self.entry.street) {
                keys[position] = kABPersonAddressStreetKey;
                values[position] = CFStringCreateWithFormat(NULL, NULL, CFSTR("%@"), self.entry.street);
                position++;
            }
            if(self.entry.city) {
                keys[position] = kABPersonAddressCityKey;
                values[position] = CFStringCreateWithFormat(NULL, NULL, CFSTR("%@"), self.entry.city);
                position++;
            }
            if(self.entry.state) {
                keys[position] = kABPersonAddressStateKey;
                values[position] = CFStringCreateWithFormat(NULL, NULL, CFSTR("%@"), self.entry.state);
                position++;
            }
            if(self.entry.state) {
                keys[position] = kABPersonAddressZIPKey;
                values[position] = CFStringCreateWithFormat(NULL, NULL, CFSTR("%@"), self.entry.postalCode);
                position++;
            }
            if(self.entry.state) {
                keys[position] = kABPersonAddressCountryKey;
                values[position] = CFStringCreateWithFormat(NULL, NULL, CFSTR("%@"), self.entry.country);
                position++;
            }

            CFDictionaryRef aDict = CFDictionaryCreate(NULL, (void *)keys, (void *)values, addressRowCount, &kCFCopyStringDictionaryKeyCallBacks,
                                                       &kCFTypeDictionaryValueCallBacks);
            ABMultiValueAddValueAndLabel(address, aDict, kABHomeLabel, NULL);
            ABRecordSetValue(person, kABPersonAddressProperty, address, &anError);
            CFRelease(aDict);
            CFRelease(address);
        }
        
        if (self.entry.firstName)
            ABRecordSetValue(person, kABPersonFirstNameProperty, (__bridge CFTypeRef)self.entry.firstName , nil);
        if (self.entry.middleName)
            ABRecordSetValue(person, kABPersonLastNameProperty, (__bridge CFTypeRef)self.entry.middleName, nil);
        if (self.entry.lastName)
            ABRecordSetValue(person, kABPersonLastNameProperty, (__bridge CFTypeRef)self.entry.lastName, nil);
        if (self.entry.office)
            ABRecordSetValue(person, kABPersonOrganizationProperty, (__bridge CFTypeRef)self.entry.office, nil);
        if (self.entry.title)
            ABRecordSetValue(person, kABPersonJobTitleProperty, (__bridge CFTypeRef)self.entry.title, nil);
        if (self.entry.department)
            ABRecordSetValue(person, kABPersonDepartmentProperty, (__bridge CFTypeRef)self.entry.department, nil);
        if (self.entry.prefix)
            ABRecordSetValue(person, kABPersonPrefixProperty, (__bridge CFTypeRef)self.entry.prefix, nil);
        if (self.entry.suffix)
            ABRecordSetValue(person, kABPersonSuffixProperty, (__bridge CFTypeRef)self.entry.suffix, nil);
        
        if(self.entry.email)
        {
            ABMutableMultiValueRef emailMultiValue = ABMultiValueCreateMutable(kABMultiStringPropertyType);
            ABMultiValueAddValueAndLabel(emailMultiValue, (__bridge CFTypeRef)self.entry.email, (__bridge CFStringRef)NSLocalizedString(@"Email", @"Email label for address book property"), NULL);
            ABRecordSetValue(person, kABPersonEmailProperty, emailMultiValue, nil);
            CFRelease(emailMultiValue);
        }
        
        ABUnknownPersonViewController* pvc = [ABUnknownPersonViewController new];
        pvc.unknownPersonViewDelegate = self;
        pvc.displayedPerson = person;
        pvc.allowsAddingToAddressBook = YES;
        pvc.allowsActions = YES;
        [self.navigationController pushViewController:pvc animated:YES];
        
        
        //ABAddressBookAddRecord(addressBook, person, nil); //add the new person to the record
        
        //    ABRecordRef group = ABGroupCreate(); //create a group
        //    ABRecordSetValue(group, kABGroupNameProperty,@"My Group", nil); // set group's name
        //    ABGroupAddMember(group, person, nil); // add the person to the group
        //    ABAddressBookAddRecord(addressBook, group, nil); // add the group
        
        
        //ABAddressBookSave(addressBook, &anError); //save the record
        CFRelease(person);
    } else {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Unable to access contacts", @"unable to access contacts alert view title") message:NSLocalizedString(@"Unable to access contacts. Go to Settings to grant permission to Ellucian GO", @"unable to access contacts alert view message") delegate:self cancelButtonTitle:NSLocalizedString(@"OK", @"OK") otherButtonTitles:nil];
        [alert show];
    }
    if(ab != NULL) {
       CFRelease(ab); 
    }
}

- (void)unknownPersonViewController:(ABUnknownPersonViewController *)unknownPersonView didResolveToPerson:(ABRecordRef)person
{
    //required
}

-(NSString *)getAddress
{
    NSString *address;
    NSString *csp;
    if(self.entry.city && self.entry.state && self.entry.postalCode) {
        csp = [NSString stringWithFormat:@"%@, %@ %@", self.entry.city, self.entry.state, self.entry.postalCode];
    } else if(self.entry.city && self.entry.state) {
        csp = [NSString stringWithFormat:@"%@, %@", self.entry.city, self.entry.state];
    } else if(self.entry.city && self.entry.postalCode) {
        csp = [NSString stringWithFormat:@"%@, %@", self.entry.city, self.entry.postalCode];
    } else if(self.entry.state && self.entry.postalCode) {
        csp = [NSString stringWithFormat:@"%@ %@", self.entry.state, self.entry.postalCode];
    } else if(self.entry.city) {
        csp = self.entry.city;
    } else if(self.entry.state) {
        csp = self.entry.state;
    } else if(self.entry.postalCode) {
        csp = self.entry.postalCode;
    }
    
    if(self.entry.street && csp && self.entry.country) {
        address = [NSString stringWithFormat:@"%@\n%@\n%@", self.entry.street, csp, self.entry.country];
    } else if(self.entry.street && csp) {
        address = [NSString stringWithFormat:@"%@\n%@", self.entry.street, csp];
    } else if(self.entry.street && self.entry.country) {
        address = [NSString stringWithFormat:@"%@\n%@", self.entry.street, self.entry.country];
    } else if(csp && self.entry.country) {
        address = [NSString stringWithFormat:@"%@\n%@", csp, self.entry.country];
    } else if(self.entry.street) {
        address = self.entry.street;
    } else if(csp) {
        address = csp;
    } else if(self.entry.postalCode) {
        address = self.entry.postalCode;
    }
    return address;
}

- (void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    self.widthConstraint.constant = [AppearanceChanger sizeInOrientation:toInterfaceOrientation].width;
}

-(void) didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    [self.scrollView setContentOffset:CGPointZero animated:YES];
}


@end
