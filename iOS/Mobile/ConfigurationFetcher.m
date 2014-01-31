//
//  ConfigurationFetcher.m
//  Mobile
//
//  Created by Jason Hocker on 9/16/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "ConfigurationFetcher.h"
#import "ImageCache.h"
#import "ConfigurationSelectionViewController.h"
#import "VersionChecker.h"

@implementation ConfigurationFetcher

+ (BOOL) fetchConfigurationFromURL:(NSString *) configurationUrl WithManagedObjectContext:(NSManagedObjectContext *)importContext
{
    
    [[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:YES];

    NSURL *aURL = [NSURL URLWithString:configurationUrl];
    NSURLRequest *postRequest = [NSURLRequest requestWithURL:aURL];
    NSHTTPURLResponse *response = nil;
    NSError *error = nil;
    NSData *responseData = [NSURLConnection sendSynchronousRequest:postRequest returningResponse:&response error:&error];
    NSLog(@"Status code from downloading configuration: %d", [response statusCode]);
    [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
    if([response statusCode] == 404 || [response statusCode] == 401 || [response statusCode] == 403) {
        NSLog(@"Unable to download data from %@", configurationUrl);
        return NO;
    } else {
        if(!responseData) {
            NSLog(@"Unable to download data from %@", configurationUrl);
            return NO;
        }
        NSDictionary* json = [NSJSONSerialization
                              JSONObjectWithData:responseData
                              options:kNilOptions
                              error:&error];
        
        if(error){
            NSLog(@"Unable to parse data from %@: %@", configurationUrl, error);
            return NO;
        } else {
            BOOL success = [self parseConfiguration:json WithManagedObjectContext:importContext];
            return success;
        }
    }
}

+(BOOL) parseConfiguration:(NSDictionary *)json WithManagedObjectContext:(NSManagedObjectContext *)importContext
{
    NSMutableArray *currentKeys = [[NSMutableArray alloc] init];
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
    
    NSArray *supportedVersions = [[json objectForKey:@"versions"] objectForKey:@"ios"];
    if([VersionChecker checkVersion:supportedVersions])
    {
    
        //set the new config for about
        [defaults setObject:[[json objectForKey:@"about"] objectForKey:@"contact"] forKey:@"about-contact"];
        [defaults setObject:[[[json objectForKey:@"about"] objectForKey:@"email"] objectForKey:@"address"] forKey:@"about-email-address"];
        [defaults setObject:[[[json objectForKey:@"about"] objectForKey:@"email"] objectForKey:@"display"] forKey:@"about-email-display"];
        [defaults setObject:[[json objectForKey:@"about"] objectForKey:@"icon"] forKey:@"about-icon"];
        if([defaults objectForKey:@"about-icon"]) {
            [[ImageCache sharedCache] getImage: [defaults objectForKey:@"about-icon"]];
        }
        NSString *aboutLogoUrlPhone = [[json objectForKey:@"about"] objectForKey:@"logoUrlPhone"];
        [defaults setObject:aboutLogoUrlPhone forKey:@"about-logoUrlPhone"];
        if(aboutLogoUrlPhone) {
            [[ImageCache sharedCache] getImage: [defaults objectForKey:@"about-logoUrlPhone"]];
        }
        [defaults setObject:[[[json objectForKey:@"about"] objectForKey:@"phone"] objectForKey:@"display"] forKey:@"about-phone-display"];
        [defaults setObject:[[[json objectForKey:@"about"] objectForKey:@"phone"] objectForKey:@"number"] forKey:@"about-phone-number"];
        [defaults setObject:[[[json objectForKey:@"about"] objectForKey:@"privacy"] objectForKey:@"display"] forKey:@"about-privacy-display"];
        [defaults setObject:[[[json objectForKey:@"about"] objectForKey:@"privacy"] objectForKey:@"url"] forKey:@"about-privacy-url"];
        [defaults setObject:[[[json objectForKey:@"about"] objectForKey:@"version"] objectForKey:@"url"] forKey:@"about-version-url"];
        [defaults setObject:[[[json objectForKey:@"about"] objectForKey:@"website"] objectForKey:@"display"] forKey:@"about-website-display"];
        [defaults setObject:[[[json objectForKey:@"about"] objectForKey:@"website"] objectForKey:@"url"] forKey:@"about-website-url"];
        [defaults setObject:[[json objectForKey:@"layout"] objectForKey:@"primaryColor"] forKey:@"primaryColor"];
        [defaults setObject:[[json objectForKey:@"layout"] objectForKey:@"headerTextColor"] forKey:@"headerTextColor"];
        [defaults setObject:[[json objectForKey:@"layout"] objectForKey:@"accentColor"] forKey:@"accentColor"];
        [defaults setObject:[[json objectForKey:@"layout"] objectForKey:@"subheaderTextColor"] forKey:@"subheaderTextColor"];
        
        [defaults setObject:[[json objectForKey:@"layout"] objectForKey:@"accentColor"] forKey:@"accentColor"];
        [defaults setObject:[[json objectForKey:@"layout"] objectForKey:@"subheaderTextColor"] forKey:@"subheaderTextColor"];
        
        [defaults setObject:[[json objectForKey:@"layout"] objectForKey:@"homeUrlPhone"]  forKey:@"home-background"];
        [defaults setObject:[[json objectForKey:@"layout"] objectForKey:@"homeUrlTablet"]  forKey:@"home-tablet-background"];
        if(UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
            [[ImageCache sharedCache] getImage: [defaults objectForKey:@"home-tablet-background"]];
        } else {
            [[ImageCache sharedCache] getImage: [defaults objectForKey:@"home-background"]];
        }

        [defaults setObject:[[json objectForKey:@"layout"] objectForKey:@"schoolLogoPhone"]  forKey:@"home-logo-stripe"];
        [[ImageCache sharedCache] getImage: [defaults objectForKey:@"home-logo-stripe"]];
        NSNumber * isSuccessNumber = (NSNumber *)[[json objectForKey:@"layout"] valueForKey:@"defaultMenuIcon"];
        
        if(!(isSuccessNumber && [isSuccessNumber boolValue] == YES))
        {
            NSString *url = [[json objectForKey:@"layout"] objectForKey:@"menuIconUrl"];
            [defaults setObject:url forKey:@"menu-icon"];
            //cache the menu icon image
            if ( url !=  nil )
            {
                [[ImageCache sharedCache] getImage: url];
            }
        }
        
        [defaults setObject:[[json objectForKey:@"security"] objectForKey:@"url"]  forKey:@"login-url"];
        [defaults setObject:[[json objectForKey:@"map"] objectForKey:@"buildings"]  forKey:@"urls-map-buildings"];
        [defaults setObject:[[json objectForKey:@"directory"] objectForKey:@"allSearch"]  forKey:@"urls-directory-allSearch"];
        [defaults setObject:[[json objectForKey:@"directory"] objectForKey:@"facultySearch"]  forKey:@"urls-directory-facultySearch"];
        [defaults setObject:[[json objectForKey:@"directory"] objectForKey:@"studentSearch"]  forKey:@"urls-directory-studentSearch"];
        
        if ([json objectForKey:@"notification"]) {
            [defaults setObject:[[json objectForKey:@"notification"] objectForKey:@"url"] forKey:@"notification-url"];
        }
        // remove notifications enabled flag for this configuration until it is determined that notifications are enabled
        [defaults removeObjectForKey:@"notification-enabled"];
        
        //Google Analytics
        if([[json objectForKey:@"analytics"] objectForKey:@"ellucian"] != [NSNull null]) {
            [defaults setObject:[[json objectForKey:@"analytics"] objectForKey:@"ellucian"] forKey:@"gaTracker1"];
        }
        if([[json objectForKey:@"analytics"] objectForKey:@"client"] != [NSNull null]) {
            [defaults setObject:[[json objectForKey:@"analytics"] objectForKey:@"client"] forKey:@"gaTracker2"];
        }
        
        [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
        
        dispatch_queue_t myQueue = dispatch_queue_create("com.ellucian.mappcreation", 0);
        
        NSDictionary *modules = [json objectForKey:@"mapp"];
        //create/update objects
        for(NSString *key in [modules allKeys]) {
            dispatch_async(myQueue, ^{NSDictionary *jsonMApp = [modules objectForKey:key];
                [Module moduleFromDictionary:jsonMApp inManagedObjectContext:importContext withKey:key];
                [currentKeys addObject:key];});
        }
        // wait for queue to empty
        dispatch_sync(myQueue, ^{
            NSLog(@"Tasks completed");
        });
        //find and delete old ones
        NSError *error = nil;

        NSFetchRequest *deleteRequest = [[NSFetchRequest alloc] init];
        [deleteRequest setEntity:[NSEntityDescription entityForName:@"Module" inManagedObjectContext:importContext]];
        deleteRequest.predicate = [NSPredicate predicateWithFormat:@"NOT (internalKey IN %@)", currentKeys];
        NSArray * oldObjects = [importContext executeFetchRequest:deleteRequest error:&error];
        for (NSManagedObject * oldObject in oldObjects) {
            [importContext deleteObject:oldObject];
        }
        
        //save to main context
        if (![importContext save:&error]) {
            NSLog(@"Could not save to main context after update to configuration: %@", [error userInfo]);
        }
        [defaults setObject:[[NSDate alloc] init] forKey:@"menu updated date"];
        [defaults synchronize];
        [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
    }
    return YES;
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex{
    if (buttonIndex == 0){
        //cancel clicked ...do your action
    }else if (buttonIndex == 1){
        //reset clicked
    }
}

+ (void) showErrorAlertView
{
    //Only show if alert is not already showing
    for (UIWindow* window in [UIApplication sharedApplication].windows) {
        NSArray* subviews = window.subviews;
        if ([subviews count] > 0){
            for (id cc in subviews) {
                if ([cc isKindOfClass:[UIAlertView class]]) {
                    return;
                }
            }
        }
    }
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Unable to launch configuration", @"unable to download configuration title") message:NSLocalizedString(@"Unable to download the configuration from that link", @"unable to download configuration message") delegate:self cancelButtonTitle:NSLocalizedString(@"OK", @"OK") otherButtonTitles:nil];
    [alert show];
}
@end
