//
//  ConfigurationSelectionViewController.m
//  Mobile
//
//  Created by Jason Hocker on 11/26/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "ConfigurationSelectionViewController.h"
#import "Configuration.h"
#import "AppDelegate.h"
#import "SlidingViewController.h"
#import "ConfigurationFetcher.h"
#import "MBProgressHUD.h"
#import "VersionChecker.h"
#import "GAI.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "GAIDictionaryBuilder.h"
#import "GAIFields.h"

#define LIVE_CONFIGURATIONS_URL @"https://mobile.elluciancloud.com/mobilecloud/api/liveConfigurations"

@interface ConfigurationSelectionViewController()
    
@property(nonatomic, strong) NSArray *allItems;
@property(nonatomic, strong) NSArray *searchResults;
@property(nonatomic, readonly) NSString *liveConfigurationsUrl;
@property(nonatomic, assign) BOOL fetchInProgress;
@end

@implementation ConfigurationSelectionViewController

- (void)viewDidLoad {
    
    [super viewDidLoad];
    
    self.navigationController.navigationBar.translucent = NO;
    self.searchDisplayController.searchBar.translucent = NO;
    if([self.searchDisplayController.searchBar respondsToSelector:@selector(setBarTintColor:)]) {
        self.searchDisplayController.searchBar.barTintColor = [UIColor defaultPrimaryColor];
    }
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(outdated:) name:kVersionCheckerOutdatedNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(updateAvailable:) name:kVersionCheckerUpdateAvailableNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(fetchConfigurations) name:kRefreshConfigurationListIfPresent object:nil];

    self.allItems = [[NSMutableArray alloc] init];
    
    self.searchResults = [[NSMutableArray alloc] init];
    
    [self fetchConfigurations];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    NSInteger rows = 0;
    
    if ([tableView
         isEqual:self.searchDisplayController.searchResultsTableView]){
        rows = [self.searchResults count];
    }
    else{
        rows = [self.allItems count];
    }
    
    return rows;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    static NSString *CellIdentifier = @"ConfigurationCell";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }

    if ([tableView isEqual:self.searchDisplayController.searchResultsTableView]){
        Configuration *configuration = [self.searchResults objectAtIndex:indexPath.row];
        cell.textLabel.text = configuration.configurationName;
    }
    else {
        Configuration *configuration = [self.allItems objectAtIndex:indexPath.row];
        cell.textLabel.text = configuration.configurationName;
    }
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    
    if ([tableView isEqual:self.searchDisplayController.searchResultsTableView]){
        Configuration *configuration = [self.searchResults objectAtIndex:indexPath.row];
        [self.searchDisplayController.searchBar resignFirstResponder];
        [self schoolChosen:configuration];        
    }
    else{
        Configuration *configuration = [self.allItems objectAtIndex:indexPath.row];
        [self schoolChosen:configuration];
    }
}

#pragma mark - load configuration
-(void) schoolChosen:(Configuration *)selectedCandidate
{
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
    NSString *trackingId1 = [defaults objectForKey:@"gaTracker1"];
    if(trackingId1) {
        id tracker1 = [[GAI sharedInstance] trackerWithTrackingId:trackingId1];
        [tracker1 send:[[GAIDictionaryBuilder createEventWithCategory:kAnalyticsCategoryUI_Action action:kAnalyticsActionList_Select label:@"Choose Institution" value:nil] build]];
    }
    
    MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    hud.labelText = NSLocalizedString(@"Loading", @"loading message while waiting for data to load");
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 0.01 * NSEC_PER_SEC);
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
        NSString *configurationUrl = selectedCandidate.configurationUrl;
        NSString *name = selectedCandidate.configurationName;
        NSLog(@"User selected configuration %@", name);
        AppDelegate *delegate = (AppDelegate *)[UIApplication sharedApplication].delegate;
        [delegate reset];
        
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        [prefs setObject:configurationUrl forKey:@"configurationUrl"];
        [prefs setObject:name forKey:@"configurationName"];
        [prefs synchronize];

        [[NSNotificationCenter defaultCenter] removeObserver:self name:kVersionCheckerUpdateAvailableNotification object:nil];
        BOOL success = [ConfigurationFetcher fetchConfigurationFromURL:configurationUrl WithManagedObjectContext:self.managedObjectContext ];
        [MBProgressHUD hideHUDForView:self.view animated:YES];
        if(success) {
            [[NSNotificationCenter defaultCenter] removeObserver:self];
            
            [AppearanceChanger applyAppearanceChanges:self.view];
            
            SlidingViewController *vc = [self.storyboard instantiateViewControllerWithIdentifier:@"SlidingViewController"];
            vc.managedObjectContext = delegate.managedObjectContext;
            [self.view.window setRootViewController:vc];
        } else {
            [self.tableView deselectRowAtIndexPath:self.tableView.indexPathForSelectedRow animated:YES];
            [self fetchConfigurations];

            [ConfigurationFetcher showErrorAlertView];
        }
    });
}

#pragma mark - fetch configurations
- (void) fetchConfigurations {
    if(self.fetchInProgress) return;
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT,0), ^{
            //download data
            self.fetchInProgress = YES;
            NSError *error;
            [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
            
            NSString *urlString = [[NSUserDefaults standardUserDefaults] objectForKey:@"mobilecloud-url"];
            
            if(!urlString) {
                urlString = self.liveConfigurationsUrl;
                
                NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
                [defaults setObject:urlString forKey:@"mobilecloud-url"];
                [defaults synchronize];
                urlString = [[NSUserDefaults standardUserDefaults] objectForKey:@"mobilecloud-url"];
            }
            NSURL *url = [NSURL URLWithString:urlString];
            
            NSData *responseData = [NSData dataWithContentsOfURL:url options:NSDataReadingUncached error:&error];
            
            if( error )
            {
                NSLog(@"fetchConfigurations error = %@", error);
                [self performSelectorOnMainThread:@selector(showErrorAlertView:) withObject:nil waitUntilDone:YES];
            }
            
            [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
            if(responseData) {
                NSDictionary* json = [NSJSONSerialization
                                      JSONObjectWithData:responseData
                                      options:kNilOptions
                                      error:&error];
                
                NSArray *supportedVersions = [[json objectForKey:@"versions"] objectForKey:@"ios"];
                if([VersionChecker checkVersion:supportedVersions])
                {
                    //Google Analytics
                    if([[json objectForKey:@"analytics"] objectForKey:@"ellucian"] != [NSNull null]) {
                        
                        NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
                        [defaults setObject:[[json objectForKey:@"analytics"] objectForKey:@"ellucian"] forKey:@"gaTracker1"];
                        [defaults synchronize];
                        NSString *trackingId1 = [[json objectForKey:@"analytics"] objectForKey:@"ellucian"];
                        if(trackingId1) {
                            id tracker1 = [[GAI sharedInstance] trackerWithTrackingId:trackingId1];
                            GAIDictionaryBuilder *builder = [GAIDictionaryBuilder createScreenView];
                            [builder set:@"Show Institution List" forKey:kGAIScreenName];
                            NSMutableDictionary *buildDictionary = [builder build];
                            [tracker1 send:buildDictionary];
                        }
                    }
                    
                    //institutions
                    NSMutableArray *configurations = [[NSMutableArray alloc] init];
                    NSArray *institutions = [json objectForKey:@"institutions"];
                    
                    //create/update objects
                    for(NSDictionary *jsonInstitution in institutions) {
                        for(NSDictionary *jsonConfiguration in [jsonInstitution objectForKey:@"configurations"]) {
                            Configuration *configuration = [[Configuration alloc] init];
                            configuration.institutionId = [jsonInstitution objectForKey:@"id"];
                            configuration.institutionName = [jsonInstitution objectForKey:@"name"];
                            configuration.configurationId = [jsonConfiguration objectForKey:@"id"];
                            configuration.configurationName = [jsonConfiguration objectForKey:@"name"];
                            configuration.configurationUrl = [jsonConfiguration objectForKey:@"configurationUrl"];
                            NSMutableSet *keywords = [[NSMutableSet alloc] init];

                            for(NSString *keyword in [jsonConfiguration objectForKey:@"keywords"]) {     
                                [keywords addObject:keyword];
                            }
                            configuration.keywords = [keywords copy];
                            [configurations addObject:configuration];
                        }
                    }
                    
                    NSArray * descriptors = [NSArray arrayWithObject: [[NSSortDescriptor alloc] initWithKey:@"configurationName"
                                                                                                  ascending:YES
                                                                                                   selector:@selector(localizedCaseInsensitiveCompare:)]];
                    self.allItems = [configurations sortedArrayUsingDescriptors:descriptors];
                }
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                self.fetchInProgress = NO;
                [self.tableView reloadData];
            });
        });
}

#pragma mark - search

- (void)filterContentForSearchText:(NSString*)searchText
                             scope:(NSString*)scope
{
    NSPredicate *resultPredicate = [NSPredicate predicateWithFormat:@"(configurationName CONTAINS[cd] %@) OR (institutionName CONTAINS[cd] %@) OR (ANY keywords CONTAINS[cd] %@)", searchText, searchText, searchText];

    self.searchResults = [self.allItems filteredArrayUsingPredicate:resultPredicate];
}

#pragma mark - UISearchDisplayController delegate methods
-(BOOL)searchDisplayController:(UISearchDisplayController *)controller
shouldReloadTableForSearchString:(NSString *)searchString
{
    [self filterContentForSearchText:searchString
                               scope:[[self.searchDisplayController.searchBar scopeButtonTitles]
                                      objectAtIndex:[self.searchDisplayController.searchBar
                                                     selectedScopeButtonIndex]]];
    
    return YES;
}

- (BOOL)searchDisplayController:(UISearchDisplayController *)controller
shouldReloadTableForSearchScope:(NSInteger)searchOption
{
    [self filterContentForSearchText:[self.searchDisplayController.searchBar text]
                               scope:[[self.searchDisplayController.searchBar scopeButtonTitles]
                                      objectAtIndex:searchOption]];
    
    return YES;
}

- (void)searchBarSearchButtonClicked:(UISearchBar *)searchBar
{
    [searchBar resignFirstResponder];
    [self filterContentForSearchText:[searchBar text]
                               scope:[[self.searchDisplayController.searchBar scopeButtonTitles]
                                      objectAtIndex:[self.searchDisplayController.searchBar
                                                     selectedScopeButtonIndex]]];

}

#pragma mark - version checking
-(void)outdated:(id)sender
{
    [self performSelectorOnMainThread:@selector(showOutdatedAlertView:) withObject:nil waitUntilDone:YES];
}

-(void)updateAvailable:(id)sender
{
    [self performSelectorOnMainThread:@selector(showUpdateAvailableAlertView:) withObject:nil waitUntilDone:YES];
}

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

- (void)searchBarTextDidBeginEditing:(UISearchBar *)searchBar
{
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionSearch withLabel:@"Search" withValue:nil forModuleNamed:nil];
}

//workaround for iOS 7 double tap on search bar, search disappears
-(void)searchDisplayControllerDidEndSearch:(UISearchDisplayController *)controller
{
    if (floor(NSFoundationVersionNumber) > NSFoundationVersionNumber_iOS_6_1) {
        [self.tableView insertSubview:self.searchDisplayController.searchBar aboveSubview:self.tableView];
    }
    return;
}

-(NSString *) liveConfigurationsUrl
{
    NSString *plistPath = [[NSBundle mainBundle] pathForResource:@"Customizations" ofType:@"plist"];
    NSDictionary *plistDictionary = [[NSDictionary alloc] initWithContentsOfFile:plistPath];
    
    if([plistDictionary objectForKey:@"Live Configurations URL"]) {
        return plistDictionary[@"Live Configurations URL"];
    } else {
        return LIVE_CONFIGURATIONS_URL;
    }
}

- (void) showErrorAlertView:(id)sender
{
    if([self.allItems count] == 0) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:nil message:NSLocalizedString(@"There are no institutions to display at this time.", @"configurations cannot be downloaded") delegate:self cancelButtonTitle:NSLocalizedString(@"OK", @"OK") otherButtonTitles:nil, nil];
        alert.tag=2;
        [alert show];
    }
}


@end

