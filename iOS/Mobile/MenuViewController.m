//
//  MenuViewController.m
//  Mobile
//
//  Created by Jason Hocker on 8/27/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "MenuViewController.h"
#import "WebViewController.h"
#import "AboutViewController.h"
#import "LoginViewController.h"
#import "UIViewController+SlidingViewExtension.h"
#import "ConfigurationSelectionViewController.h"
#import "CurrentUser.h"
#import "AppDelegate.h"
#import "ImageCache.h"
#import "ModuleRole.h"
#import "NotificationsFetcher.h"
#import "AsynchronousImageView.h"
#import "VersionChecker.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "DirectoryNavigationController.h"
#import "Module+Attributes.h"
#import "FeedViewController.h"
#import "FeedDetailViewController.h"
#import "EventsViewController.h"
#import "EventDetailViewController.h"


@interface MenuViewController()

@property (strong, nonatomic) NSString* configurationUrl;
@property (strong, nonatomic) NSMutableArray *headings;
@property (strong, nonatomic) NSMutableArray *moduleSections;
@property (assign, nonatomic) BOOL useSwitchSchool;
@property (strong, nonatomic) NSDictionary *moduleDefinitions;
@property (strong, nonatomic) NSDictionary *customModuleDefinitions;

@end

@implementation MenuViewController

#pragma mark controller loading

-(void) viewDidLoad
{
    [super viewDidLoad];
    
    [self readModuleDefinitionsPlist];

    self.tableView.delegate = self;
    self.tableView.dataSource = self;
    
    [self.tableView registerClass:[UITableViewHeaderFooterView class] forHeaderFooterViewReuseIdentifier:@"Header"];

    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(outdated:) name:kVersionCheckerOutdatedNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(updateAvailable:) name:kVersionCheckerUpdateAvailableNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationDidBecomeActive:) name:UIApplicationDidBecomeActiveNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(respondToSignOut:) name:kSignOutNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(respondToSignIn:) name:kSignInNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(notificationsUpdated:) name:kNotificationsUpdatedNotification object:nil];
}

- (void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:NO];

    self.headings = [[NSMutableArray alloc] init];
    self.moduleSections = [[NSMutableArray alloc] init];
    
    NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
    
    self.configurationUrl = [prefs stringForKey:@"configurationUrl"];
    
    if(self.configurationUrl) {
        [self reload];
        NSDate *updateDate = [[NSUserDefaults standardUserDefaults] objectForKey:@"menu updated date"];
        int days = [[[NSDate alloc] init] timeIntervalSinceDate:updateDate]/24/60/60;
        if(days > 0 || updateDate == nil) {
            [self fetchConfiguration];
        }
    } else {
        [self pushInstitutionSelectionController];
    }
}

#pragma mark notification observers

-(void)applicationDidBecomeActive:(id)sender
{
    [self reload];
    [NotificationsFetcher fetchNotifications:self.managedObjectContext];
}

-(void) respondToSignOut:(id)sender
{
    [self.tableView reloadRowsAtIndexPaths:[self.tableView indexPathsForVisibleRows]
                          withRowAnimation:UITableViewRowAnimationNone];
}

-(void) respondToSignIn:(id)sender
{
    [self.tableView reloadRowsAtIndexPaths:[self.tableView indexPathsForVisibleRows]
                          withRowAnimation:UITableViewRowAnimationNone];
    
}

-(void) notificationsUpdated:(id)sender
{
    [self reload];
}

-(void)outdated:(id)sender
{
    [self performSelectorOnMainThread:@selector(showOutdatedAlertView:) withObject:nil waitUntilDone:YES];
}

-(void)updateAvailable:(id)sender
{
    [self performSelectorOnMainThread:@selector(showUpdateAvailableAlertView:) withObject:nil waitUntilDone:YES];
}


#pragma mark UITableViewDataSource
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [self.moduleSections count] + 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)sectionIndex
{
    if(sectionIndex == [self.moduleSections count]) {
        return self.useSwitchSchool ? 4 : 3;
    }
    NSArray *modules = [self.moduleSections objectAtIndex:sectionIndex];
    return [modules count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Menu Cell"];
    
    if([AppearanceChanger isRTL]) {
        cell = [tableView dequeueReusableCellWithIdentifier:@"Menu RTL Cell"];
    }
    
    UILabel *label = (UILabel *)[cell viewWithTag:101];
    AsynchronousImageView *cellImage = (AsynchronousImageView *)[ cell viewWithTag:102];
    UILabel *countLabel = (UILabel *)[cell viewWithTag:103];
    countLabel.text = nil;
    [countLabel setHidden:YES];
    
    if([self.moduleSections count] > [indexPath section]) {
        NSArray *modules = [self.moduleSections objectAtIndex:[indexPath section]];
        
        NSManagedObject *object = [modules objectAtIndex:[indexPath row]];
        label.text = [[object valueForKey:@"name"] description];
        
        if ( [object valueForKey:@"iconUrl"] != nil )
        {
            cellImage.image = [[ImageCache sharedCache] getCachedImage: [object valueForKey:@"iconUrl"]];
        } else {
            cellImage.image = nil;
        }
        
        if([[object valueForKey:@"type"] isEqualToString:@"notifications"]) {
            NSFetchRequest *request = [[NSFetchRequest alloc] init];
            [request setEntity:[NSEntityDescription entityForName:@"Notification" inManagedObjectContext:self.managedObjectContext]];
            
            [request setIncludesSubentities:NO];
            
            NSError *err;
            NSUInteger count = [self.managedObjectContext countForFetchRequest:request error:&err];
            if(count == NSNotFound) {
                //Handle error
            }
            
            countLabel.text = [NSString stringWithFormat:@"%@", @(count)];
            [countLabel setHidden:(count == 0)];
        }
        
    } else {
        if(indexPath.row == 0) {
            label.text = NSLocalizedString(@"Home", @"Home menu item");
            cellImage.image = [UIImage imageNamed:@"icon-home"];
        } else if(indexPath.row == 1) {
            label.text = NSLocalizedString(@"About", @"About menu item");
            NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
            NSString *iconUrl = [defaults objectForKey:@"about-icon"];
            if(iconUrl) {
                cellImage.image = [[ImageCache sharedCache] getCachedImage:iconUrl];
            } else {
                cellImage.image = [UIImage imageNamed:@"icon-about"];
            }
        } else if(indexPath.row == 2 && self.useSwitchSchool) {
            label.text = NSLocalizedString(@"Switch School", @"Switch school menu item");
            cellImage.image = [UIImage imageNamed:@"icon-switch-schools"];
        } else if(indexPath.row == 3 || (indexPath.row == 2 && !self.useSwitchSchool)) {
            if ( [self userIsLoggedIn ] ) {
                label.text = NSLocalizedString(@"Sign Out", nil);
            } else {
                label.text = NSLocalizedString(@"Sign In", nil);
            }
            cellImage.image = [UIImage imageNamed:@"icon-sign-in"];
        }
    }
    
    return cell;
}


#pragma mark UITableViewDelegate
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 34;
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    UITableViewHeaderFooterView* h =
    [tableView dequeueReusableHeaderFooterViewWithIdentifier:@"Header"];
    
    UIColor *backgroundColor = [UIColor colorWithRed:40.0f/255.0f green:39.0f/255.0f blue:40.0f/255.0f alpha:1.0f];
    if (![h.backgroundColor isEqual: backgroundColor]) {
        
        h.contentView.backgroundColor = backgroundColor;
        h.contentView.opaque = YES;
        
        UILabel* headerLabel = [UILabel new];
        headerLabel.tag = 1;
        headerLabel.font = [UIFont fontWithName:@"Helvetica-Bold" size:14.0];
        headerLabel.textColor = [UIColor colorWithRed:255.0f/255.0f green:196.0f/255.0f blue:77.0f/255.0f alpha:1.0f];
        headerLabel.backgroundColor = [UIColor clearColor];
        headerLabel.opaque = NO;
        [h.contentView addSubview:headerLabel];

        headerLabel.translatesAutoresizingMaskIntoConstraints = NO;
        if([AppearanceChanger isRTL]) {
            if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
                [h.contentView addConstraint:[NSLayoutConstraint constraintWithItem:headerLabel                 attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:h.contentView attribute:NSLayoutAttributeLeft multiplier:1.0 constant:307.0]];
            } else
                [h.contentView addConstraint:[NSLayoutConstraint constraintWithItem:headerLabel                 attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:h.contentView attribute:NSLayoutAttributeLeft multiplier:1.0 constant:267.0]];
        } else {
            [h.contentView addConstraints:
             [NSLayoutConstraint constraintsWithVisualFormat:@"H:|-13-[headerLabel]"
                                                     options:0 metrics:nil
                                                       views:@{@"headerLabel":headerLabel}]];
        }

        [h.contentView addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"V:|[headerLabel]|"
                                                 options:0 metrics:nil
                                                   views:@{@"headerLabel":headerLabel}]];
        
        
    }
    UILabel* headerLabel = (UILabel*)[h.contentView viewWithTag:1];
    if(section == [self.moduleSections count]) {
        headerLabel.text = NSLocalizedString(@"Actions", @"Actions menu heading");
    } else {
        headerLabel.text = [self.headings count] > section ? [self.headings objectAtIndex:section] : @"";
    }

    return h;
}

- (CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    //if the first section doesn't have a title, change the height so it does not show
	return ([self.headings count] > 0 && section == 0 && [[self.headings objectAtIndex:section] length] == 0) ? 0 : 32.0;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    UIViewController *newTopViewController = nil;
    BOOL requiresAuthenticatedUser = NO;
    
    if([self.moduleSections count] > [indexPath section]) {
        NSArray *modules = [self.moduleSections objectAtIndex:[indexPath section]];

        Module *module = [modules objectAtIndex:[indexPath row]];
        if([module.type isEqualToString:@"web"]) {
            
            if([[module propertyForKey:@"external"] isEqualToString:@"true"]) {
                NSURL *url = [NSURL URLWithString:[module propertyForKey:@"url"]];
                [[UIApplication sharedApplication] openURL:url];
                [tableView deselectRowAtIndexPath:indexPath animated:YES];
                [self.slidingViewController resetTopView];
                return;
            }
            WebViewController *vc = (WebViewController *)[self.storyboard instantiateViewControllerWithIdentifier:@"Web"];
            UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController:vc];
            vc.loadRequest = [[NSURLRequest alloc] initWithURL:[NSURL URLWithString:[module propertyForKey:@"url" ]]];
            vc.title = module.name;
            newTopViewController = nav;
            BOOL secure = [[module propertyForKey:@"secure"] isEqualToString:@"true"];
            requiresAuthenticatedUser = secure;
            vc.secure = secure;
            vc.analyticsLabel = module.name;
        } else {
            id storyboardViewController;
            if([module.type isEqualToString:@"custom"]) {
                NSString *customModuleType = [module propertyForKey:@"custom-type"];
                
                NSDictionary *moduleDefinition = [self.customModuleDefinitions objectForKey:customModuleType];
                NSString *storyboardIdentifier = nil;
                UIStoryboard* storyboard = nil;
                if(UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ) {
                    storyboardIdentifier = [moduleDefinition objectForKey:@"iPad Storyboard Identifier"];
                    NSString *storyboardName = [moduleDefinition objectForKey:@"iPad Storyboard Name"];
                    if(!storyboardName) storyboardName = @"CustomizationStoryboard_iPad";
                    storyboard = [UIStoryboard storyboardWithName:storyboardName bundle:nil];
                } else {
                    storyboardIdentifier = [moduleDefinition objectForKey:@"iPhone Storyboard Identifier"];
                    NSString *storyboardName = [moduleDefinition objectForKey:@"iPhone Storyboard Name"];
                    if(!storyboardName) storyboardName = @"CustomizationStoryboard_iPhone";
                    storyboard = [UIStoryboard storyboardWithName:storyboardName bundle:nil];
                }
                
                storyboardViewController = [storyboard instantiateViewControllerWithIdentifier:storyboardIdentifier];
                requiresAuthenticatedUser = [[moduleDefinition objectForKey:@"Needs Authentication"] boolValue];
               
            } else {
                NSDictionary *moduleDefinition = [self.moduleDefinitions objectForKey:module.type];
                NSString *storyboardIdentifier = nil;
                UIStoryboard* storyboard = nil;
                if(UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ) {
                    storyboardIdentifier = [moduleDefinition objectForKey:@"iPad Storyboard Identifier"];
                    NSString *storyboardName = [moduleDefinition objectForKey:@"iPad Storyboard Name"];
                    if(!storyboardName) storyboardName = @"MainStoryboard_iPad";
                    storyboard = [UIStoryboard storyboardWithName:storyboardName bundle:nil];
                } else {
                    storyboardIdentifier = [moduleDefinition objectForKey:@"iPhone Storyboard Identifier"];
                    NSString *storyboardName = [moduleDefinition objectForKey:@"iPad Storyboard Name"];
                    if(!storyboardName) storyboardName = @"MainStoryboard_iPhone";
                    storyboard = [UIStoryboard storyboardWithName:storyboardName bundle:nil];
                }
                
                storyboardViewController = [storyboard instantiateViewControllerWithIdentifier:storyboardIdentifier];
                                requiresAuthenticatedUser = [[moduleDefinition objectForKey:@"Needs Authentication"] boolValue];
            }
            
            //Set top view controller
            if ([storyboardViewController isKindOfClass:[UITabBarController class]]) {
                newTopViewController = storyboardViewController;
            }
            else if([storyboardViewController isKindOfClass:[UISplitViewController class]]) {
                newTopViewController = storyboardViewController;
            }
            else if([storyboardViewController isKindOfClass:[UINavigationController class]]) {
                newTopViewController = storyboardViewController;
            }
            else {
                UINavigationController *navigationViewController = [[UINavigationController alloc] initWithRootViewController:storyboardViewController];
                newTopViewController = navigationViewController;
            }
            
            //set Module object, if wanted
            [self setModule:module onViewController:newTopViewController];
        }
        
        if(requiresAuthenticatedUser) {
            if([self getCurrentUser].isLoggedIn) {
                
                BOOL match = module.roles.count == 0;
                
                for(ModuleRole *role in module.roles) {
                    if([[self getCurrentUser].roles containsObject:role.role]) {
                        match = YES;
                    }
                }
                if(!match) {
                    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Access Denied",@"access denied error message")
                                                                    message:NSLocalizedString(@"You do not have permission to use this feature.", @"permission access error message")
                                                                   delegate:nil
                                                          cancelButtonTitle:NSLocalizedString(@"OK", @"OK")
                                                          otherButtonTitles:nil];
                    [alert show];
                    
                    newTopViewController = nil;
                    [tableView deselectRowAtIndexPath:indexPath animated:YES];
                    
                }
            } else {
                LoginViewController *vc = [self.storyboard instantiateViewControllerWithIdentifier:@"Login"];
                //vc.rolesForNextModule = module.roles;
                [vc setModalPresentationStyle:UIModalPresentationFullScreen];
                [self presentViewController:vc animated:YES completion:nil];
            }
        }
    } else {
        if(indexPath.row == 0) {
            HomeViewController *vc = (HomeViewController *)[self.storyboard instantiateViewControllerWithIdentifier:@"LandingPage"];
            UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController:vc];
            vc.managedObjectContext = self.managedObjectContext;
            vc.title = NSLocalizedString(@"Home", @"Home menu item");
            newTopViewController = nav;
        } else if(indexPath.row == 1) {
            AboutViewController *vc = (AboutViewController *)[self.storyboard instantiateViewControllerWithIdentifier:@"About"];
            UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController:vc];
            vc.title = NSLocalizedString(@"About", @"About menu item");
            newTopViewController = nav;
        } else if(indexPath.row == 2 && self.useSwitchSchool) {
            UINavigationController *navcontroller = [self.storyboard instantiateViewControllerWithIdentifier:@"ConfigurationSelector"];
            ConfigurationSelectionViewController *vc = navcontroller.childViewControllers[0];
            [vc setModalPresentationStyle:UIModalPresentationFullScreen];
            vc.managedObjectContext = self.managedObjectContext;
            [self presentViewController:navcontroller animated:YES completion:nil];
        } else if(indexPath.row == 3 || (indexPath.row == 2 && !self.useSwitchSchool)) {
            if ( [self userIsLoggedIn] )
            {
                [self sendEventWithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionMenu_selection withLabel:@"Menu-Click Sign Out" withValue:nil forModuleNamed:nil];
                [self logOutUser];
                UITableViewCell *cell = [tableView cellForRowAtIndexPath:indexPath];
                UILabel *label = (UILabel *)[cell viewWithTag:101];
                label.text = NSLocalizedString(@"Sign In", @"label to sign in");
                [tableView deselectRowAtIndexPath:indexPath animated:YES];
                [self reload];
            }
            else
            {
                [self sendEventWithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionMenu_selection withLabel:@"Menu-Click Sign In" withValue:nil forModuleNamed:nil];
                LoginViewController *vc = [self.storyboard instantiateViewControllerWithIdentifier:@"Login"];
                [vc setModalPresentationStyle:UIModalPresentationFullScreen];
                [self presentViewController:vc animated:YES completion:nil];
            }
        }
    }
    
    UIImage *buttonImage = [UIImage imageNamed:@"icon-menu-iphone"];
    NSString *menuImageName = [[NSUserDefaults standardUserDefaults] objectForKey:@"menu-icon"];
    
    if(menuImageName)
    {
        buttonImage = [[ImageCache sharedCache] getCachedImage: menuImageName];
        buttonImage=[UIImage imageWithCGImage:[buttonImage CGImage] scale:2.0 orientation:UIImageOrientationUp];
        if (SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"7.0")) {
            buttonImage = [buttonImage imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];
        }
    }
    
    if (newTopViewController) {
        
        if ([newTopViewController isKindOfClass:[UINavigationController class]]) {
            UINavigationController *nav = (UINavigationController *) newTopViewController;
            nav.navigationBar.translucent = NO;
            if([nav.topViewController respondsToSelector:@selector(revealMenu:)]) {
                UIBarButtonItem *menuButton = [[UIBarButtonItem alloc] initWithImage:buttonImage style:UIBarButtonItemStyleBordered target:newTopViewController action:@selector(revealMenu:)];
                nav.topViewController.navigationItem.leftBarButtonItem = menuButton;
            }
        } else if ([newTopViewController isKindOfClass:[UITabBarController class]]) {
            UITabBarController *tab = (UITabBarController *) newTopViewController;
            
            if([newTopViewController respondsToSelector:@selector(setTranslucent:)]) {
                tab.tabBar.translucent = NO;
            }
            for(UIViewController *tabbedController in tab.viewControllers) {
                
                if ([tabbedController isKindOfClass:[UISplitViewController class]]) {
                    UISplitViewController *split = (UISplitViewController *) tabbedController;
                    UINavigationController *controller = split.viewControllers[0];
                    UIViewController *masterController = controller.topViewController;
                    
                    if([masterController conformsToProtocol:@protocol(UISplitViewControllerDelegate)]) {
                        split.delegate = (id)masterController;
                    }
                    if([masterController respondsToSelector:@selector(revealMenu:)]) {
                        UIBarButtonItem *menuButton = [[UIBarButtonItem alloc] initWithImage:buttonImage style:UIBarButtonItemStyleBordered target:newTopViewController action:@selector(revealMenu:)];
                        masterController.navigationItem.leftBarButtonItem = menuButton;
                        
                    }
                }
                else if ([tabbedController isKindOfClass:[UINavigationController class]]) {
                    UINavigationController *nav = (UINavigationController *) tabbedController;
                    if([nav.topViewController respondsToSelector:@selector(revealMenu:)]) {
                        UIBarButtonItem *menuButton = [[UIBarButtonItem alloc] initWithImage:buttonImage style:UIBarButtonItemStyleBordered target:newTopViewController action:@selector(revealMenu:)];
                        nav.topViewController.navigationItem.leftBarButtonItem = menuButton;
                    }
                }
            }
        } else if ([newTopViewController isKindOfClass:[UISplitViewController class]]) {
            
            UISplitViewController *split = (UISplitViewController *) newTopViewController;
            split.presentsWithGesture = YES;
            
            UINavigationController *controller = split.viewControllers[0];
            UINavigationController *detailNavController = split.viewControllers[1];
            
            UIViewController *masterController = controller.topViewController;
            UIViewController *detailController = detailNavController.topViewController;
            
            if([masterController conformsToProtocol:@protocol(UISplitViewControllerDelegate)]) {
                split.delegate = (id)masterController;
            }
            if([detailController conformsToProtocol:@protocol(UISplitViewControllerDelegate)]) {
                split.delegate = (id)detailController;
            }
            if( [detailController conformsToProtocol:@protocol(DetailSelectionDelegate)]) {
                if ( [masterController respondsToSelector:@selector(detailSelectionDelegate) ])
                {
                    [masterController setValue:detailController forKey:@"detailSelectionDelegate"];
                }
            }
            
            if([masterController respondsToSelector:@selector(revealMenu:)]) {
                UIBarButtonItem *menuButton = [[UIBarButtonItem alloc] initWithImage:buttonImage style:UIBarButtonItemStyleBordered target:newTopViewController action:@selector(revealMenu:)];
                masterController.navigationItem.leftBarButtonItem = menuButton;
            }
        }
    
        [newTopViewController.view addGestureRecognizer:self.slidingViewController.panGesture];
        [self.slidingViewController animateTopChange:nil onComplete:^{
            CGRect frame = self.slidingViewController.topViewController.view.frame;
            self.slidingViewController.topViewController = newTopViewController;
            self.slidingViewController.topViewController.view.frame = frame;
            [self.slidingViewController resetTopView];
        }];
    }
    
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

#pragma mark - institution selection

- (void) pushInstitutionSelectionController {
    [self performSegueWithIdentifier:@"showInstitutionSelection" sender:self];
}

#pragma mark - fetch configuration

- (void) fetchConfiguration {
    
    
    NSManagedObjectContext *importContext = [[NSManagedObjectContext alloc] initWithConcurrencyType:NSPrivateQueueConcurrencyType];
    importContext.parentContext = self.managedObjectContext;
    [importContext performBlock: ^{
        
        BOOL success = [ConfigurationFetcher fetchConfigurationFromURL: self.configurationUrl WithManagedObjectContext:importContext];
        if(success) {
            [AppearanceChanger applyAppearanceChanges:self.view];
        } else {
            [[NSNotificationCenter defaultCenter] postNotificationName:kConfigurationFetcherError object:nil];
        }
        
        //persist to store and update fetched result controllers
        [importContext.parentContext performBlock:^{
            NSError *parentError = nil;
            if(![importContext.parentContext save:&parentError]) {
                NSLog(@"Could not save to store after update to configuration: %@", [parentError userInfo]);
            }
            [self reload];
        }];
    }];
}

-(void) reload
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"Module" inManagedObjectContext:self.managedObjectContext];
    [fetchRequest setEntity:entity];
    
    NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"index" ascending:YES];
    NSArray *sortDescriptors = [NSArray arrayWithObjects:sortDescriptor, nil];
    [fetchRequest setSortDescriptors:sortDescriptors];
    
    AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
    CurrentUser *user = [appDelegate getCurrentUser];
    NSSet *roles = user.roles;
    NSMutableArray *parr = [NSMutableArray array];
    [parr addObject: [NSPredicate predicateWithFormat:@"showForGuest == %@", [NSNumber numberWithBool:YES]] ];
    [parr addObject: [NSPredicate predicateWithFormat:@"showForGuest == %@", [NSNumber numberWithBool:NO]] ];
    //  [parr addObject: [NSPredicate predicateWithFormat:@"roles.@count == 0"] ];
    for(NSString *role in roles) {
    //       [parr addObject: [NSPredicate predicateWithFormat:@"ANY roles.role like %@", role] ];
    }
    fetchRequest.predicate = [NSCompoundPredicate orPredicateWithSubpredicates:parr];

    NSError *error;
    NSArray *definedModules = [self.managedObjectContext executeFetchRequest:fetchRequest error:&error];
    NSMutableArray *sectionModules = [[NSMutableArray alloc] init];
    NSMutableArray *tempModuleSections = [[NSMutableArray alloc] init];
    NSMutableArray *tempHeadings = [[NSMutableArray alloc] init];
    if([definedModules count] > 0) {
        for (int i = 0; i < [definedModules count]; i++) {
            Module *module = [definedModules objectAtIndex:i];
        
            if([module.type isEqualToString:@"header"]) {
                if(i > 0) {
                    [tempModuleSections addObject:sectionModules];
                }
                sectionModules = [[NSMutableArray alloc] init];
                [tempHeadings addObject:module.name];
            }
            else {
                if(i == 0) {
                    BOOL useLegacyLabel = YES;
                    for (int j = 0; j < [definedModules count]; j++) {
                        Module *tempModule = [definedModules objectAtIndex:j];
                        if([tempModule.type isEqualToString:@"header"]) {
                            useLegacyLabel = NO;
                        }
                    }

                    NSString *applicationsLabel = useLegacyLabel ? NSLocalizedString(@"Applications", @"Applications menu heading") : @"";
                    [tempHeadings addObject:applicationsLabel];
                }
                if([self isSupportedModule:module]) {
                    [sectionModules addObject:module];
                }
            }
            
        }
    } else {
        NSString *applicationsLabel = NSLocalizedString(@"Applications", @"Applications menu heading");
        [tempHeadings addObject:applicationsLabel];
    }
    self.headings = tempHeadings;
    self.moduleSections = tempModuleSections;
    [self.moduleSections addObject:sectionModules];
    [self.tableView reloadData];
}

#pragma mark - login

-(CurrentUser *) getCurrentUser
{
    AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
    return [appDelegate getCurrentUser];
}

-(BOOL) userIsLoggedIn
{
    return [self getCurrentUser].isLoggedIn;
}

-(void) logOutUser
{
    [[self getCurrentUser] logout];
    [[NSNotificationCenter defaultCenter] postNotificationName:kSignInReturnToHomeNotification object:nil];
}


#pragma mark - UIAlertViewDelegate
- (void) showUpdateAvailableAlertView:(id)sender
{
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Outdated", @"Outdated alert title") message:NSLocalizedString(@"A new version is available.", @"Outdated alert message") delegate:self cancelButtonTitle:NSLocalizedString(@"OK", @"OK") otherButtonTitles:NSLocalizedString(@"Upgrade", @"Upgrade software button label"), nil];
    alert.tag=1;
    [alert show];
}

- (void) showOutdatedAlertView:(id)sender
{
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Outdated", @"Outdated alert title") message:NSLocalizedString(@"The application must be upgraded to the latest version.", @"Force update alert message") delegate:self cancelButtonTitle:NSLocalizedString(@"Upgrade", @"Upgrade software button label") otherButtonTitles:nil];
    alert.tag=0;
    [alert show];
}

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if(alertView.tag == 0) {
        [self openITunes];
    } else if(alertView.tag == 1) {
        if(buttonIndex == 1) {
            [self openITunes];
        }
    }
}

-(void)openITunes
{
    AppDelegate *delegate = (AppDelegate *)[UIApplication sharedApplication].delegate;
    [delegate reset];
    NSString *iTunesLink = @"http://appstore.com/elluciango";
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:iTunesLink]];
}

#pragma mark - Menu
-(BOOL) isSupportedModule:(Module *) module
{
    NSString * moduleType = module.type;
    

    if([moduleType isEqualToString:@"web"]) {
        return YES;
    } else if([moduleType isEqualToString:@"custom"]) {
        NSString *customModuleType = [module propertyForKey:@"custom-type"];
        NSDictionary *module = [self.customModuleDefinitions objectForKey:customModuleType];
        if(module) {
            return YES;
        }
    } else {
        NSDictionary *module = [self.moduleDefinitions objectForKey:moduleType];
        if(module) {
            return YES;
        }

    }
    return NO;
}

-(void) readModuleDefinitionsPlist
{
    NSString *plistPath = [[NSBundle mainBundle] pathForResource:@"Customizations" ofType:@"plist"];
    NSDictionary *plistDictionary = [[NSDictionary alloc] initWithContentsOfFile:plistPath];
    
    if([plistDictionary objectForKey:@"Allow Switch School"]) {
        self.useSwitchSchool = [plistDictionary[@"Allow Switch School"] boolValue];
    } else {
        self.useSwitchSchool = YES;
    }
    
    if([plistDictionary objectForKey:@"Custom Modules"]) {
        self.customModuleDefinitions = plistDictionary[@"Custom Modules"];
    }
    
    plistPath = [[NSBundle mainBundle] pathForResource:@"EllucianModules" ofType:@"plist"];
    plistDictionary = [[NSDictionary alloc] initWithContentsOfFile:plistPath];
    
    self.moduleDefinitions = plistDictionary;
}

-(void) setModule:(Module *)module onViewController:(UIViewController *) viewController
{
    if([viewController respondsToSelector:@selector(setModule:)]) {
        [viewController setValue:module forKey:@"module"];
    }
    
    if ([viewController isKindOfClass:[UITabBarController class]]) {
        UITabBarController *tabController = (UITabBarController *) viewController;
        for(UIViewController *tabbedViewController in tabController.viewControllers) {
            [self setModule:module onViewController:tabbedViewController];
        }
    }
    else if([viewController isKindOfClass:[UINavigationController class]]) {
        UINavigationController *navigationController = (UINavigationController *) viewController;
        [self setModule:module onViewController:navigationController.topViewController];
    }
    else if([viewController isKindOfClass:[UISplitViewController class]]) {
        UISplitViewController *splitViewController = (UISplitViewController *) viewController;
        for(UIViewController *viewController in splitViewController.viewControllers) {
            [self setModule:module onViewController:viewController];
        }
    }
}


@end
