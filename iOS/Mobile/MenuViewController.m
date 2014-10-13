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
#import "LoginExecutor.h"

#define kMenuViewControllerHeaderHeight 32.0

@interface MenuViewController()

@property (strong, nonatomic) NSString* configurationUrl;
@property (assign, nonatomic) BOOL useSwitchSchool;
@property (strong, nonatomic) NSDictionary *moduleDefinitions;
@property (strong, nonatomic) NSDictionary *customModuleDefinitions;
@property (nonatomic) NSMutableArray *sectionInfoArray;

@end

@implementation MenuViewController

#pragma mark controller loading

-(void) viewDidLoad
{
    [super viewDidLoad];
    
    [self readModuleDefinitionsPlist];
    
    self.tableView.delegate = self;
    self.tableView.dataSource = self;
    
    [self.tableView registerClass:[MenuTableViewHeaderFooterView class] forHeaderFooterViewReuseIdentifier:@"Header"];
    [self.tableView registerClass:[MenuTableViewHeaderFooterView class] forHeaderFooterViewReuseIdentifier:@"CollapseableHeader"];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(outdated:) name:kVersionCheckerOutdatedNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(updateAvailable:) name:kVersionCheckerUpdateAvailableNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationDidBecomeActive:) name:UIApplicationDidBecomeActiveNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(respondToSignOut:) name:kSignOutNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(notificationsUpdated:) name:kNotificationsUpdatedNotification object:nil];
}

- (void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:NO];
    
    self.sectionInfoArray = [NSMutableArray new];
    
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
    return [self.sectionInfoArray count];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)sectionIndex
{
    if([self isActionsSection:sectionIndex]) {
        return self.useSwitchSchool ? 4 : 3;
    }
    MenuSectionInfo *sectionInfo = [self.sectionInfoArray objectAtIndex:sectionIndex];
    NSInteger numberOfModulesInSection = [sectionInfo.modules count];
    return sectionInfo.isCollapsed ? 0 : numberOfModulesInSection;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSInteger sectionIndex = [indexPath section];
    if([self isActionsSection:sectionIndex]) {
        return [self tableView:tableView cellForActionsRow:indexPath];
    } else {
        return [self tableView:tableView cellForModulesRow:indexPath];
    }
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForModulesRow:(NSIndexPath *)indexPath
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
    UIImageView *lockImage = (UIImageView *)[cell viewWithTag:104];
    [lockImage setHidden:YES];
    
    MenuSectionInfo *sectionInfo = [self.sectionInfoArray objectAtIndex:[indexPath section]];
    NSArray *modules = sectionInfo.modules;
    
    Module *object = [modules objectAtIndex:[indexPath row]];
    label.text = [[object valueForKey:@"name"] description];
    
    if ( [object valueForKey:@"iconUrl"] != nil )
    {
        cellImage.image = [[ImageCache sharedCache] getCachedImage: [object valueForKey:@"iconUrl"]];
    } else {
        cellImage.image = nil;
    }
    
    if ( [CurrentUser sharedInstance].isLoggedIn )
    {
        if([[object valueForKey:@"type"] isEqualToString:@"notifications"]) {
            
            NSManagedObjectContext *moc = object.managedObjectContext;
            NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"Notification" inManagedObjectContext:moc];
            NSFetchRequest *request = [[NSFetchRequest alloc] init];
            [request setEntity:entityDescription];
            NSPredicate *predicate = [NSPredicate predicateWithFormat: @"read == %@",[NSNumber numberWithBool:NO]];
            [request setPredicate:predicate];
            
            NSError *error;
            NSArray *array = [moc executeFetchRequest:request error:&error];
            NSUInteger notificationCount = [array count];
            countLabel.text = [NSString stringWithFormat:@"%@", @(notificationCount)];
            
            CALayer *layer = [countLabel layer];
            layer.cornerRadius = countLabel.bounds.size.height / 2;
            countLabel.textColor = [UIColor blackColor];
            countLabel.font = [UIFont systemFontOfSize:14];
            countLabel.textAlignment = NSTextAlignmentCenter;
            countLabel.backgroundColor = [UIColor colorWithRed:102.0/255.0 green:102.0/255.0 blue:102.0/255.0 alpha:1.0f];
            
            [countLabel setHidden:(notificationCount == 0)];
        }
    } else {
        if([self requiresAuthentication:object]) {
            [lockImage setHidden:NO];
        }
    }
        return cell;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForActionsRow:(NSIndexPath *)indexPath
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
    UIImageView *lockImage = (UIImageView *)[cell viewWithTag:104];
    [lockImage setHidden:YES];
    
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
        if ( [CurrentUser sharedInstance].isLoggedIn  ) {
            label.text = NSLocalizedString(@"Sign Out", nil);
        } else {
            label.text = NSLocalizedString(@"Sign In", nil);
        }
        cellImage.image = [UIImage imageNamed:@"icon-sign-in"];
    }
    return cell;
    
}

- (void)tableView:(UITableView *)tableView willDisplayCell:(UITableViewCell *)cell forRowAtIndexPath:(NSIndexPath *)indexPath
{
    //hack to solve problem with white line appearing
    cell.backgroundColor = [UIColor colorWithWhite:0.163037 alpha:1.0];
}

#pragma mark UITableViewDelegate
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 34;
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)sectionIndex
{
    MenuSectionInfo *sectionInfo = (self.sectionInfoArray)[sectionIndex];
    MenuTableViewHeaderFooterView *sectionHeaderView;
    if(sectionInfo.isCollapseable) {
        sectionHeaderView = [self.tableView dequeueReusableHeaderFooterViewWithIdentifier:@"CollapseableHeader"];
    } else {
        sectionHeaderView = [self.tableView dequeueReusableHeaderFooterViewWithIdentifier:@"Header"];
    }
    
    UILabel* headerLabel = sectionHeaderView.headerLabel;
    sectionInfo.headerView = sectionHeaderView;
    headerLabel.text = sectionInfo.headerTitle;
    sectionHeaderView.section = sectionIndex;
    sectionHeaderView.delegate = self;
    sectionHeaderView.collapseable = sectionInfo.isCollapseable;
    
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSArray *collapsedHeaders = [defaults stringArrayForKey:@"menu-collapsed"];
    BOOL collapsed = [collapsedHeaders containsObject:sectionInfo.headerTitle];
    sectionHeaderView.collapsibleButton.selected = collapsed;
    
    return sectionHeaderView;
}

- (CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)sectionIndex
{
    //if the first section doesn't have a title, change the height so it does not show
    if([self isActionsSection:sectionIndex]) {
        return kMenuViewControllerHeaderHeight;
    } else if([self.sectionInfoArray count] > 0 && sectionIndex == 0 ) {
        MenuSectionInfo *sectionInfo = (self.sectionInfoArray)[sectionIndex];
        return ([sectionInfo.headerTitle length] == 0 || [sectionInfo.modules count] == 0) ? 0 : kMenuViewControllerHeaderHeight;
    } else {
        MenuSectionInfo *sectionInfo = (self.sectionInfoArray)[sectionIndex];
        return [sectionInfo.modules count] > 0 ? kMenuViewControllerHeaderHeight : 0;
    }
    
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    UIViewController *newTopViewController = nil;
    
    NSInteger sectionIndex = [indexPath section];
    if([self isActionsSection:sectionIndex]) {
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
            if ( [CurrentUser sharedInstance].isLoggedIn )
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
                UIViewController *vc = [LoginExecutor loginController];
                [self presentViewController:vc animated:YES completion:nil];
            }
        }
    } else {
        MenuSectionInfo *sectionInfo = [self.sectionInfoArray objectAtIndex:[indexPath section]];
        NSArray *modules = sectionInfo.modules;
        
        Module *module = [modules objectAtIndex:[indexPath row]];
        
        newTopViewController = [self findControllerByModule:module];
        
        if (!newTopViewController) {
            // nothing to launch
            [tableView deselectRowAtIndexPath:indexPath animated:YES];
            return;
        }
    }
    
    if (newTopViewController) {
        [self showViewController:newTopViewController];
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
    
    CurrentUser *currentUser = [CurrentUser sharedInstance];
    if(currentUser && [currentUser isLoggedIn ]) {
        NSSet *roles = currentUser.roles;
        NSMutableArray *parr = [NSMutableArray array];
        [parr addObject: [NSPredicate predicateWithFormat:@"roles.@count == 0"] ];
        [parr addObject: [NSPredicate predicateWithFormat:@"ANY roles.role like %@", @"Everyone"] ];
        for(NSString *role in roles) {
            [parr addObject: [NSPredicate predicateWithFormat:@"ANY roles.role like %@", role] ];
        }
        fetchRequest.predicate = [NSCompoundPredicate orPredicateWithSubpredicates:parr];
    } else {
        fetchRequest.predicate = [NSPredicate predicateWithFormat:@"(hideBeforeLogin == %@) || (hideBeforeLogin = nil)", [NSNumber numberWithBool:NO]];
    }
    
    NSError *error;
    NSArray *definedModules = [self.managedObjectContext executeFetchRequest:fetchRequest error:&error];
    NSMutableArray *infoArray = [[NSMutableArray alloc] init];
    
    MenuSectionInfo *info;
    if([definedModules count] > 0) {
        for (int i = 0; i < [definedModules count]; i++) {
            Module *module = [definedModules objectAtIndex:i];
            
            if([module.type isEqualToString:@"header"]) {
                
                NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
                NSArray *collapsedHeaders = [defaults stringArrayForKey:@"menu-collapsed"];
                BOOL collapsed = [collapsedHeaders containsObject:module.name];
                
                info = [MenuSectionInfo new];
                info.headerTitle = module.name;
                info.collapsed = collapsed;
                info.collapseable = YES;
                info.modules = [NSMutableArray new];
                info.headerModule = module;
                [infoArray addObject:info];
            }
            else {
                if(i == 0) {
                    NSString *localizedApplications = NSLocalizedString(@"Applications", @"Applications menu heading");
                    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
                    NSArray *collapsedHeaders = [defaults stringArrayForKey:@"menu-collapsed"];
                    BOOL collapsed = [collapsedHeaders containsObject:localizedApplications];
                    
                    info = [MenuSectionInfo new];
                    info.headerTitle = localizedApplications;
                    info.collapsed = collapsed;
                    info.collapseable = YES;
                    info.modules = [NSMutableArray new];
                    [infoArray addObject:info];
                }
                if([self isSupportedModule:module]) {
                    [info.modules addObject:module];
                }
            }
            
        }
    } else {
        //nothing in list
        NSString *localizedApplications = NSLocalizedString(@"Applications", @"Applications menu heading");
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        NSArray *collapsedHeaders = [defaults stringArrayForKey:@"menu-collapsed"];
        BOOL collapsed = [collapsedHeaders containsObject:localizedApplications];
        
        info = [MenuSectionInfo new];
        info.headerTitle = localizedApplications;
        info.collapsed = collapsed;
        info.collapseable = YES;
        info.modules = [NSMutableArray new];
        [infoArray addObject:info];
    }
    
    //Actions
    info = [MenuSectionInfo new];
    NSString *applicationsLabel = NSLocalizedString(@"Actions", @"Actions menu heading");
    
    info.headerTitle = applicationsLabel;
    info.collapsed = NO;
    info.modules = [NSMutableArray new];
    [infoArray addObject:info];
    
    self.sectionInfoArray = infoArray;
    [self.tableView reloadData];
}

#pragma mark - login

-(void) logOutUser
{
    [[CurrentUser sharedInstance] logout];
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

- (void)showModule:(Module *)module
{
    UIViewController* newTopViewController = [self findControllerByModule:module];
    
    if (newTopViewController) {
        [self showViewController:newTopViewController];
    }
}

- (UIViewController*) findControllerByModule:(Module*)module
{
    UIViewController* newTopViewController = nil;
    BOOL requiresAuthenticatedUser = NO;
    
    if([module.type isEqualToString:@"web"]) {
        
        if([[module propertyForKey:@"external"] isEqualToString:@"true"]) {
            NSURL *url = [NSURL URLWithString:[module propertyForKey:@"url"]];
            [[UIApplication sharedApplication] openURL:url];
            
            // done - since no controler will be used
            return nil;
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
    
    NSSet *access = [module.roles filteredSetUsingPredicate:[NSPredicate  predicateWithFormat:@"role != %@", @"Everyone"] ];
    if([access count] > 0) {
        requiresAuthenticatedUser = YES;
    }
    
    if([CurrentUser sharedInstance].isLoggedIn) {
        BOOL match = NO;
        for(ModuleRole *role in module.roles) {
            if([[CurrentUser sharedInstance].roles containsObject:role.role]) {
                match = YES;
                break;
            } else if ([role.role isEqualToString:@"Everyone"]) {
                match = YES;
                break;
            }
        }
        if([module.roles count] == 0) { //upgrades from 3.0 or earlier
            match = YES;
        }
        if(!match) {
            UIAlertView *alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Access Denied",@"access denied error message")
                                                            message:NSLocalizedString(@"You do not have permission to use this feature.", @"permission access error message")
                                                           delegate:nil
                                                  cancelButtonTitle:NSLocalizedString(@"OK", @"OK")
                                                  otherButtonTitles:nil];
            [alert show];
            
            newTopViewController = nil;

        }
    } else if(requiresAuthenticatedUser) {

        UIViewController *vc = [LoginExecutor loginController];
        UIViewController *vc2 = vc.childViewControllers[0];
        [vc2 setValue:module.roles forKey:@"access"];
        [vc setModalPresentationStyle:UIModalPresentationFullScreen];
        [self presentViewController:vc animated:YES completion:nil];
    }
    
    return newTopViewController;
}

- (BOOL) requiresAuthentication:(Module*)module
{
    BOOL requiresAuthenticatedUser = NO;
    
    if([module.type isEqualToString:@"web"]) {
        
        if([[module propertyForKey:@"external"] isEqualToString:@"true"]) {
            return NO;
        }
        BOOL secure = [[module propertyForKey:@"secure"] isEqualToString:@"true"];
        requiresAuthenticatedUser = secure;
    } else if([module.type isEqualToString:@"custom"]) {
        NSString *customModuleType = [module propertyForKey:@"custom-type"];
        NSDictionary *moduleDefinition = [self.customModuleDefinitions objectForKey:customModuleType];
        requiresAuthenticatedUser = [[moduleDefinition objectForKey:@"Needs Authentication"] boolValue];
        
    } else {
        NSDictionary *moduleDefinition = [self.moduleDefinitions objectForKey:module.type];
        requiresAuthenticatedUser = [[moduleDefinition objectForKey:@"Needs Authentication"] boolValue];
    }
    
    NSSet *access = [module.roles filteredSetUsingPredicate:[NSPredicate  predicateWithFormat:@"role != %@", @"Everyone"] ];
    if([access count] > 0) {
        requiresAuthenticatedUser = YES;
    }
    
    return requiresAuthenticatedUser;
}


-(void) showViewController:(UIViewController*) newTopViewController
{
    UIImage *buttonImage = [UIImage imageNamed:@"icon-menu-iphone"];
    NSString *menuImageName = [[NSUserDefaults standardUserDefaults] objectForKey:@"menu-icon"];
    
    if(menuImageName)
    {
        buttonImage = [[ImageCache sharedCache] getCachedImage: menuImageName];
        buttonImage=[UIImage imageWithCGImage:[buttonImage CGImage] scale:2.0 orientation:UIImageOrientationUp];
        buttonImage = [buttonImage imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];
    }
    
    if ([newTopViewController isKindOfClass:[UINavigationController class]]) {
        UINavigationController *nav = (UINavigationController *) newTopViewController;
        nav.navigationBar.translucent = NO;
        if([nav.topViewController respondsToSelector:@selector(revealMenu:)]) {
            UIBarButtonItem *menuButton = [[UIBarButtonItem alloc] initWithImage:buttonImage style:UIBarButtonItemStyleBordered target:newTopViewController action:@selector(revealMenu:)];
            nav.topViewController.navigationItem.leftBarButtonItem = menuButton;
        }
    } else if ([newTopViewController isKindOfClass:[UITabBarController class]]) {
        UITabBarController *tab = (UITabBarController *) newTopViewController;
        
        tab.tabBar.translucent = NO;
        
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
            UIBarButtonItem *menuButton = [[UIBarButtonItem alloc] initWithImage:buttonImage
                                                                           style:UIBarButtonItemStyleBordered target:newTopViewController action:@selector(revealMenu:)];
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

#pragma mark - SectionHeaderViewDelegate

- (void)sectionHeaderView:(MenuTableViewHeaderFooterView *)sectionHeaderView sectionOpened:(NSInteger)sectionOpened {
    
    MenuSectionInfo *sectionInfo = [self.sectionInfoArray objectAtIndex:sectionOpened];
    sectionInfo.collapsed = NO;

    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSArray *collapsedHeaders = [defaults stringArrayForKey:@"menu-collapsed"];
    NSMutableSet *collapsedHeadersSet = [[NSMutableSet alloc] initWithArray:collapsedHeaders];
    [collapsedHeadersSet removeObject:sectionInfo.headerTitle];
    collapsedHeaders = [collapsedHeadersSet allObjects];
    [defaults setObject:collapsedHeaders forKey:@"menu-collapsed"];
    [defaults synchronize];

    NSArray *modules = sectionInfo.modules;
    
    NSInteger countOfRowsToInsert = [modules count];
    NSMutableArray *indexPathsToInsert = [[NSMutableArray alloc] init];
    for (NSInteger i = 0; i < countOfRowsToInsert; i++) {
        [indexPathsToInsert addObject:[NSIndexPath indexPathForRow:i inSection:sectionOpened]];
    }
    
    [self.tableView beginUpdates];
    [self.tableView insertRowsAtIndexPaths:indexPathsToInsert withRowAnimation:UITableViewRowAnimationNone];
    [self.tableView endUpdates];
}

- (void)sectionHeaderView:(MenuTableViewHeaderFooterView *)sectionHeaderView sectionClosed:(NSInteger)sectionClosed {
    
    MenuSectionInfo *sectionInfo = (self.sectionInfoArray)[sectionClosed];
    
    sectionInfo.collapsed = YES;
    
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSArray *collapsedHeaders = [defaults stringArrayForKey:@"menu-collapsed"];
    NSMutableSet *collapsedHeadersSet = [[NSMutableSet alloc] initWithArray:collapsedHeaders];
    [collapsedHeadersSet addObject:sectionInfo.headerTitle];
    collapsedHeaders = [collapsedHeadersSet allObjects];
    [defaults setObject:collapsedHeaders forKey:@"menu-collapsed"];
    [defaults synchronize];
    NSInteger countOfRowsToDelete = [self.tableView numberOfRowsInSection:sectionClosed];
    
    if (countOfRowsToDelete > 0) {
        NSMutableArray *indexPathsToDelete = [[NSMutableArray alloc] init];
        for (NSInteger i = 0; i < countOfRowsToDelete; i++) {
            [indexPathsToDelete addObject:[NSIndexPath indexPathForRow:i inSection:sectionClosed]];
        }
        [self.tableView deleteRowsAtIndexPaths:indexPathsToDelete withRowAnimation:UITableViewRowAnimationNone];
    }
}

#pragma mark - menu support methods
-(BOOL) isActionsSection:(NSInteger)sectionIndex
{
    return ([self.sectionInfoArray count] == 1 + sectionIndex);
}

-(NSArray *) modulesInSection:(NSInteger)sectionIndex
{
    MenuSectionInfo *sectionInfo = [self.sectionInfoArray objectAtIndex:sectionIndex];
    return sectionInfo.modules;
}


@end
