//
//  HomeViewController.m
//  Mobile
//
//  Created by Alan McEwan on 9/14/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "HomeViewController.h"
#import "LoginViewController.h"
#import "ImageCache.h"
#import "AppDelegate.h"
#import "ImageCache.h"
#import "NotificationsFetcher.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "LoginExecutor.h"

@interface HomeViewController ()

@end

@implementation HomeViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.navigationController.navigationBar.translucent = NO;
    
    if([AppearanceChanger isRTL]) {
        self.topMessageLabel.textAlignment = NSTextAlignmentRight;
    }
    
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    if([defaults objectForKey:@"menu-discovered"]) {
        [self.topMessageView removeFromSuperview];
    } else {
        [UIView animateWithDuration:1 delay:2 options:UIViewAnimationOptionCurveEaseInOut| UIViewAnimationOptionTransitionNone
                     animations:^{ self.topMessageView.alpha = 0.7;}
                     completion:^(BOOL finished){  }];
    }

    NSString *homeTabletBackground = [defaults objectForKey:@"home-tablet-background"] ;
    NSString *schoolBackgroundImage = ([homeTabletBackground length] > 0 && UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) ? homeTabletBackground: [defaults objectForKey:@"home-background"];
    NSString *schoolLogoStripe = [defaults objectForKey:@"home-logo-stripe"];
    
    if (schoolBackgroundImage != nil)
    {
        UIImage *image = [[ImageCache sharedCache] getCachedImage: schoolBackgroundImage];
        image=[UIImage imageWithCGImage:[image CGImage] scale:2.0 orientation:UIImageOrientationUp];
        self.backgroundImageView.image = image;
    } else {
        //if its a default configuration but the images hasn't yet been downloaded, use the launch image.  Have to pull the right version out of the asset catalog.
        NSString *launchImage = @"LaunchImage";
        UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
        
        if (SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"7.0")) {

            if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)  {
                if (UIInterfaceOrientationIsLandscape(orientation)) {
                    launchImage = @"LaunchImage-700-Landscape";
                } else {
                    launchImage = @"LaunchImage-700-Portrait";
                }
            } else if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone &&[UIScreen mainScreen].bounds.size.height > 480.0f) {
                launchImage = @"LaunchImage-700-568h";
            } else {
                launchImage = @"LaunchImage-700";
            }
        } else {
            if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)  {
                if (UIInterfaceOrientationIsLandscape(orientation)) {
                    launchImage = @"LaunchImage-Landscape";
                } else {
                    launchImage = @"LaunchImage-Portrait";
                }
            } else if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone &&[UIScreen mainScreen].bounds.size.height > 480.0f) {
                launchImage = @"LaunchImage-568h";
            } else {
                launchImage = @"LaunchImage";
            }
        }
        
        UIImage *image = [UIImage imageNamed:launchImage];
        self.backgroundImageView.image = image;
    }
    
    if (schoolLogoStripe != nil)
    {
        UIImage *image = [[ImageCache sharedCache] getCachedImage: schoolLogoStripe];
        image=[UIImage imageWithCGImage:[image CGImage] scale:2.0 orientation:UIImageOrientationUp];
        self.schoolLogoStripeImageView.image = image;
    }
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(setLoginState)
                                                 name:kSignInNotification
                                               object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(setLoginState)
                                                 name:kSignOutNotification
                                               object:nil];
  
    [self setLoginState];
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Show Home Screen" forModuleNamed:@""];
}

- (IBAction)signIn:(id)sender {
    [self sendEventWithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionLogin withLabel:@"Home-Click Sign In" withValue:nil forModuleNamed:nil];
    UIViewController *vc = [LoginExecutor loginController];
    [self presentViewController:vc animated:YES completion:nil];
}

- (IBAction)signOut:(id)sender {
    [self sendEventWithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionLogout withLabel:@"Home-Click Sign Out" withValue:nil forModuleNamed:nil];
    [[CurrentUser sharedInstance] logout];
}

- (IBAction)switchSchools:(id)sender {
    UINavigationController *navcontroller = [self.storyboard instantiateViewControllerWithIdentifier:@"ConfigurationSelector"];
    navcontroller.navigationBar.translucent = NO;
    ConfigurationSelectionViewController *vc = navcontroller.childViewControllers[0];
    [vc setModalPresentationStyle:UIModalPresentationFullScreen];
    vc.managedObjectContext = self.managedObjectContext;
    [self presentViewController:navcontroller animated:YES completion:nil];
}

-(void) setLoginState
{
    [self.signInButton removeTarget:self action:NULL forControlEvents:UIControlEventTouchUpInside];
    if([CurrentUser sharedInstance].isLoggedIn) {
        self.signInLabel.text = NSLocalizedString(@"Sign Out", @"label to sign out");
         [self.signInButton addTarget:self action:@selector(signOut:) forControlEvents:UIControlEventTouchUpInside];
        [NotificationsFetcher fetchNotifications:self.managedObjectContext];
    } else {
        self.signInLabel.text = NSLocalizedString(@"Sign In", @"label to sign in");
        [self.signInButton addTarget:self action:@selector(signIn:) forControlEvents:UIControlEventTouchUpInside];
    }
}

-(void) viewWillAppear:(BOOL)animated
{
    if([UIViewController instancesRespondToSelector:@selector(edgesForExtendedLayout)]) {
        self.edgesForExtendedLayout=UIRectEdgeNone;
    }
}
@end
