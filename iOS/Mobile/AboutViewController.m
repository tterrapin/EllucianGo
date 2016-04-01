//
//  AboutViewController.m
//  Mobile
//
//  Created by Alan McEwan on 8/17/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "AboutViewController.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "AppearanceChanger.h"
#import "PseudoButtonView.h"
#import "Ellucian_GO-Swift.h"

@interface AboutViewController ()

@end

@implementation AboutViewController;

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.navigationController.navigationBar.translucent = NO;
    self.toolbar.translucent = NO;
    
    if([AppearanceChanger isIOS8AndRTL]) {
        self.contactTextView.textAlignment = NSTextAlignmentRight;
        self.phoneLabelLabel.textAlignment = NSTextAlignmentRight;
        self.emailLabelLabel.textAlignment = NSTextAlignmentRight;
        self.websiteLabelLabel.textAlignment = NSTextAlignmentRight;
        self.privacyPolicyLabel.textAlignment = NSTextAlignmentRight;
    }
    
    self.serverVersion.textColor = [UIColor subheaderTextColor];
    self.serverVersionLabel.textColor = [UIColor subheaderTextColor];
    self.clientVersion.textColor = [UIColor subheaderTextColor];
    self.clientVersionLabel.textColor = [UIColor subheaderTextColor];
    
    NSUserDefaults* defaults = [AppGroupUtilities userDefaults];

    self.backgroundView.backgroundColor = [UIColor accentColor];
    
    self.separatorAfterPhoneView.backgroundColor = [UIColor accentColor];
    self.separatorAfterEmailView.backgroundColor = [UIColor accentColor];
    self.separatorAfterWebsiteView.backgroundColor = [UIColor accentColor];

    NSString *contactInfo = [defaults objectForKey:@"about-contact"];
    self.contactTextView.text = contactInfo;
    
    NSLayoutConstraint *lastSeparator = nil;

    NSString *phoneNumber = [defaults objectForKey:@"about-phone-number"];
    if ([phoneNumber length] > 0 )
    {
        self.phoneLabel.text = phoneNumber;
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
    NSString *email = [defaults objectForKey:@"about-email-address"];
    if ([email length] > 0)
    {
        self.emailLabel.text = email;
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
    NSString *website = [defaults objectForKey:@"about-website-url"];
    if ([website length] > 0)
    {
        self.websiteLabel.text = website;
        [self.websiteView setAction:@selector(tapWebsite:) withTarget:self];
        lastSeparator = self.separatorAfterWebsiteHeightConstraint;
    } else {
        [[self.websiteView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
        self.separatorAfterWebsiteHeightConstraint.constant = 0;
        [self.websiteView addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"V:[view(0)]"
                                                 options:0 metrics:nil
                                                   views:@{@"view":self.websiteView}]];

    }
    NSString *privacy = [defaults objectForKey:@"about-privacy-url"];
    NSString *privacyDisplayString = [defaults objectForKey:@"about-privacy-display"];
    if ([privacy length] > 0)
    {
        self.privacyPolicyLabel.text = privacyDisplayString;
        [self.privacyPolicyView setAction:@selector(tapPrivacyPolicy:) withTarget:self];
    } else {
        [[self.privacyPolicyView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
        lastSeparator.constant = 0;
        [self.privacyPolicyView addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"V:[view(0)]"
                                                 options:0 metrics:nil
                                                   views:@{@"view":self.privacyPolicyView}]];

        
    }

    self.clientVersion.text = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleVersion"];
    
    [self retrieveVersion];
    [self loadImage];
    
    self.toolbar.barTintColor = [UIColor primaryColor];
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"About Page" forModuleNamed:nil];

    self.widthConstraint.constant = [AppearanceChanger currentScreenBoundsDependOnOrientation].width;
    
    CGSize sizeThatShouldFitTheContent = [self.contactTextView sizeThatFits:self.contactTextView.frame.size];
    self.contactTextViewHeightConstraint.constant = sizeThatShouldFitTheContent.height;
}

- (void) loadImage
{
    NSUserDefaults* defaults = [AppGroupUtilities userDefaults];
    NSString *myUrl = [defaults objectForKey:@"about-logoUrlPhone"];
    
    dispatch_async(dispatch_get_global_queue(0,0), ^{
        
        NSData *imageData = [[NSData alloc] initWithContentsOfURL:[NSURL URLWithString:myUrl]];
        UIImage *myimage = [[UIImage alloc] initWithData:imageData];
        
        //assume it's a retina image and scale accordingly
        myimage=[UIImage imageWithCGImage:[myimage CGImage] scale:2.0 orientation:UIImageOrientationUp];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            [_schoolLogo setImage:myimage];
        });
    });
}

- (void) retrieveVersion
{
    //retrieve server version in the background
    NSUserDefaults* defaults = [AppGroupUtilities userDefaults];

    NSString *versionURL = [defaults objectForKey:@"about-version-url"];
    
    dispatch_async(dispatch_get_global_queue(0,0), ^{
        NSError *error;
        NSData *versionResponseData = [NSData dataWithContentsOfURL:
                                   [NSURL URLWithString: versionURL]];
        if(!versionResponseData) {
            NSLog(@"Unable to download data from %@", versionURL);
            return;
        }
        NSDictionary* versionJson = [NSJSONSerialization
                                 JSONObjectWithData:versionResponseData
                                 options:kNilOptions
                                 error:&error];
        NSDictionary *mVersionProps = [versionJson objectForKey:@"application"];
        
        [defaults setObject:mVersionProps forKey:@"application"];
        [defaults synchronize];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            
            _serverVersion.text = [NSString stringWithFormat:@"%@", [mVersionProps objectForKey:@"version"]];
        });
    });
    
}

#pragma mark - Table view data source

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    NSUserDefaults* defaults = [AppGroupUtilities userDefaults];
    [self resetScrollViewContentOffset];
    if ([[segue identifier] isEqualToString:@"webView"]) {
        [self sendEventWithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionFollow_web withLabel:@"About Web" withValue:nil forModuleNamed:nil];
        WebViewController *detailController = (WebViewController *)[segue destinationViewController];
        detailController.loadRequest = [[NSURLRequest alloc] initWithURL:[NSURL URLWithString:[defaults objectForKey:@"about-website-url"]]];
        detailController.title =[defaults objectForKey:@"about-website-display"];
        detailController.analyticsLabel = NSLocalizedString(@"About", @"About menu item");
    }
    else if ([[segue identifier] isEqualToString:@"policyView"]) {
        [self sendView:@"School Privacy" forModuleNamed:nil];
        WebViewController *detailController = (WebViewController *)[segue destinationViewController];
        detailController.loadRequest = [[NSURLRequest alloc] initWithURL:[NSURL URLWithString:[defaults objectForKey:@"about-privacy-url"]]];
        detailController.title =[defaults objectForKey:@"about-privacy-display"];
        detailController.analyticsLabel = NSLocalizedString(@"About", @"About menu item");
    }
    else if ([[segue identifier] isEqualToString:@"poweredBy"]) {
        [self sendEventWithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionInvoke_Native withLabel:@"About Text" withValue:nil forModuleNamed:nil];
        WebViewController *detailController = (WebViewController *)[segue destinationViewController];
        detailController.loadRequest = [[NSURLRequest alloc] initWithURL:[NSURL URLWithString:@"http://www.ellucian.com"]];
        detailController.title = NSLocalizedString(@"Ellucian", @"Ellucian");
        detailController.analyticsLabel = NSLocalizedString(@"About", @"About menu item");
    }
    else if ([[segue identifier] isEqualToString:@"ellucianPrivacy"]) {
        [self sendView:@"Ellucian Privacy" forModuleNamed:nil];
        WebViewController *detailController = (WebViewController *)[segue destinationViewController];
        detailController.loadRequest = [[NSURLRequest alloc] initWithURL:[NSURL URLWithString:@"http://www.ellucian.com/privacy"]];
        detailController.title = NSLocalizedString(@"Ellucian Privacy", @"Ellucian privacy policy label");
        detailController.analyticsLabel = NSLocalizedString(@"About", @"About menu item");
    }
}

-(void)tapPhone:(id)sender
{
    [self sendEventWithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionInvoke_Native withLabel:@"About Phone" withValue:nil forModuleNamed:nil];
    NSString *phoneNumber = [[AppGroupUtilities userDefaults] objectForKey:@"about-phone-number"];
    NSString *phone = [[phoneNumber componentsSeparatedByCharactersInSet: [NSCharacterSet characterSetWithCharactersInString:@"() -"]] componentsJoinedByString: @""];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", phone]]];

}

-(void)tapEmail:(id)sender
{
    [self sendEventWithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionInvoke_Native withLabel:@"About Email" withValue:nil forModuleNamed:nil];
    NSString *email = [[AppGroupUtilities userDefaults] objectForKey:@"about-email-address"];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"mailto://%@", email]]];
}

-(void)tapWebsite:(id)sender
{
    [self performSegueWithIdentifier:@"webView" sender:nil];
}

-(void)tapPrivacyPolicy:(id)sender
{
    [self performSegueWithIdentifier:@"policyView" sender:nil];
}

-(void) didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    CGSize sizeThatShouldFitTheContent = [self.contactTextView sizeThatFits:self.contactTextView.frame.size];
    self.contactTextViewHeightConstraint.constant = sizeThatShouldFitTheContent .height;
    
    self.widthConstraint.constant = [AppearanceChanger currentScreenBoundsDependOnOrientation].width;

    [self resetScrollViewContentOffset];
}

-(void) resetScrollViewContentOffset
{
    [self.contactTextView setContentOffset:CGPointZero animated:YES];
    [self.scrollView setContentOffset:CGPointZero animated:YES];
    
}
@end
