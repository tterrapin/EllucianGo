//
//  POIDetailViewController.m
//  Mobile
//
//  Created by Jason Hocker on 9/8/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "POIDetailViewController.h"
#import "MapPOIType.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "AppearanceChanger.h"

@implementation POIDetailViewController

- (void)viewDidLoad
{
    [super viewDidLoad];

    self.navigationController.navigationBar.translucent = NO;

    if([AppearanceChanger isRTL]) {
        self.descriptionTextView.textAlignment = NSTextAlignmentRight;
        self.addressLabel.textAlignment = NSTextAlignmentRight;
    }
    self.nameLabel.text = self.name;
    self.campusLabel.text = self.campusName;
    if([self.types count] > 0) {
        self.typeLabel.text = [self.types componentsJoinedByString:@", "];
    } else {
        self.typeLabel.text = nil;
    }

    if(self.buildingId) {
        NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"MapPOI"];
        request.predicate = [NSPredicate predicateWithFormat:@"buildingId = %@", self.buildingId];

        NSError *error = nil;
        NSArray *results = [self.module.managedObjectContext executeFetchRequest:request error:&error];
        MapPOI *building = [results lastObject];
        if(building) {
            if(!self.address) {
                self.address = [NSString stringWithFormat:NSLocalizedStringWithDefaultValue(@"building name/address", @"Localizable", [NSBundle mainBundle], @"%@/%@", @"building name/address"), building.name, building.address];
            }
            if(!self.imageUrl) {
                self.imageUrl = building.imageUrl;
            }
            if([building.latitude doubleValue] != 0 && [building.longitude doubleValue] != 0) {
                self.location = [[CLLocation alloc] initWithLatitude:[building.latitude doubleValue] longitude:[building.longitude doubleValue]];
            }
            if(!self.name) {
                self.name = building.name;
            }
            if(!self.description) {
                self.description = building.description_;
            }
            if(!self.additionalServices) {
                self.additionalServices = building.additionalServices;
            }
            if(!([self.types count] > 0)) {
                NSMutableArray *types = [[NSMutableArray alloc] init];
                for(MapPOIType *type in building.types) {
                    [types addObject:type.name];
                }
                self.types = [types copy];
            }
        } else {
            [self fetchBuilding];
        }
    }
    self.backgroundView.backgroundColor = [UIColor accentColor];
    self.nameLabel.textColor = [UIColor subheaderTextColor];
    self.typeLabel.textColor = [UIColor subheaderTextColor];
    self.campusLabel.textColor = [UIColor subheaderTextColor];
    if(self.imageUrl) {
        [self.imageView loadImageFromURLString:self.imageUrl];
    } else {
        self.imageHeightConstraint.constant = 0;
    }
    
    if([self.address length] == 0) {
        self.address = nil;
    }
    
    if(self.address) {
        [self.addressView setAction:@selector(tapAddress:) withTarget:self];
        self.addressLabel.text = self.address;
    } else {
        [[self.addressView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
        [self.addressView addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"V:[view(0)]"
                                                 options:0 metrics:nil
                                                   views:@{@"view":self.addressView}]];
        self.addressLabel.text = nil;
    }
    if((self.location && !(self.location.coordinate.latitude == 0 && self.location.coordinate.longitude == 0)) || self.address) {
        [self.directionsView setAction:@selector(tapDirections:) withTarget:self];
        self.directionsLabel.text = NSLocalizedString(@"Get Directions", @"User can get directions for this location");
    } else {
        [[self.directionsView subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];
        [self.directionsView addConstraints:
         [NSLayoutConstraint constraintsWithVisualFormat:@"V:[view(0)]"
                                                 options:0 metrics:nil
                                                   views:@{@"view":self.directionsView}]];
        self.directionsLabel.text = nil;
    }
    if(self.description && self.additionalServices) {
        NSString *text = [NSString stringWithFormat:@"%@\n%@", self.description, self.additionalServices];
        self.descriptionTextView.text = text;
    }
    else if(self.description) {
        self.descriptionTextView.text = self.description;
    }
    else if(self.additionalServices) {
        self.descriptionTextView.text = self.additionalServices;
    }
    self.widthConstraint.constant = [AppearanceChanger sizeInOrientation:self.interfaceOrientation].width;
    self.descriptionTextViewHeightConstraint.constant = self.descriptionTextView.contentSize.height;
    [self.scrollView invalidateIntrinsicContentSize];
    [self.descriptionTextView invalidateIntrinsicContentSize];

}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Building Detail" forModuleNamed:self.module.name];
    self.widthConstraint.constant = [AppearanceChanger sizeInOrientation:self.interfaceOrientation].width;
    self.descriptionTextViewHeightConstraint.constant = self.descriptionTextView.contentSize.height;
    [self.scrollView invalidateIntrinsicContentSize];
}

-(void) openPointOnAppleMaps:(CLLocationCoordinate2D) coordinate
{
    MKPlacemark *place = [[MKPlacemark alloc] initWithCoordinate:coordinate addressDictionary:nil];
    MKMapItem *destinationLocItem = [[MKMapItem alloc] initWithPlacemark:place];
    destinationLocItem.name = self.name;
    NSArray *mapItemsArray = [NSArray arrayWithObjects:destinationLocItem, nil];
    [MKMapItem openMapsWithItems:mapItemsArray launchOptions:nil];
}

-(void) openDirectionsOnAppleMaps:(CLLocationCoordinate2D) coordinate
{
    MKMapItem *currentLocationItem = [MKMapItem mapItemForCurrentLocation];
    MKPlacemark *place = [[MKPlacemark alloc] initWithCoordinate:coordinate addressDictionary:nil];
    MKMapItem *destinamtionLocItem = [[MKMapItem alloc] initWithPlacemark:place];
    destinamtionLocItem.name = self.name;
    NSArray *mapItemsArray = [NSArray arrayWithObjects:currentLocationItem, destinamtionLocItem, nil];
    NSDictionary *dictForDirections = [NSDictionary dictionaryWithObject:MKLaunchOptionsDirectionsModeDriving forKey:MKLaunchOptionsDirectionsModeKey];
    [MKMapItem openMapsWithItems:mapItemsArray launchOptions:dictForDirections];
}

-(void)tapAddress:(id)sender
{
    CLLocationCoordinate2D coordinate = self.location.coordinate;
    
    if(!self.location || (coordinate.latitude == 0 && coordinate.longitude == 0)) {
        // perform geocode
        CLGeocoder *geocoder = [[CLGeocoder alloc] init];

        [geocoder geocodeAddressString:self.address completionHandler:^(NSArray *placemarks, NSError *error) {

            dispatch_async(dispatch_get_main_queue(),^ {
                if (placemarks.count == 0) {
                    UIAlertView *alert = [[UIAlertView alloc] init];
                    alert.title = NSLocalizedString(@"Unknown address", @"error message when address cannot be used for getting directions");
                    [alert addButtonWithTitle:NSLocalizedString(@"OK", @"OK")];
                    [alert show];
                } else {
                    CLPlacemark* placemark = [placemarks objectAtIndex:0];
                    [self openPointOnAppleMaps:placemark.location.coordinate];
                }
            });
        }];
    } else {
        [self openPointOnAppleMaps:coordinate];
    }
    
}

-(void)tapDirections:(id)sender
{
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionInvoke_Native withLabel:@"Get Directions" withValue:nil forModuleNamed:self.module.name];
    
    CLLocationCoordinate2D coordinate = self.location.coordinate;
    
    if(!self.location || (coordinate.latitude == 0 && coordinate.longitude == 0)) {
        // perform geocode
        CLGeocoder *geocoder = [[CLGeocoder alloc] init];
        
        [geocoder geocodeAddressString:self.address completionHandler:^(NSArray *placemarks, NSError *error) {
            
            dispatch_async(dispatch_get_main_queue(),^ {
                if (placemarks.count == 0) {
                    UIAlertView *alert = [[UIAlertView alloc] init];
                    alert.title = NSLocalizedString(@"Unknown address", @"error message when address cannot be used for getting directions");
                    [alert addButtonWithTitle:NSLocalizedString(@"OK", @"OK")];
                    [alert show];
                } else {
                    CLPlacemark* placemark = [placemarks objectAtIndex:0];
                    [self openDirectionsOnAppleMaps:placemark.location.coordinate];
                }
            });
        }];
    } else {
        [self openDirectionsOnAppleMaps:coordinate];
    }
}

-(void) fetchBuilding
{
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSString *defaultUrlString = [defaults objectForKey:@"urls-map-buildings"];
    
    NSString *urlString = [NSString stringWithFormat:@"%@/%@", defaultUrlString, [self.buildingId  stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding]];
    
    NSError *error;
    [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
    
    NSData *responseData = [NSData dataWithContentsOfURL: [NSURL URLWithString: urlString]];
    
    [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
    
    if(responseData)
    {
        
        NSDictionary* json = [NSJSONSerialization
                              JSONObjectWithData:responseData
                              options:kNilOptions
                              error:&error];
        

        for ( NSDictionary *building in [json objectForKey:@"buildings"]) {
            if(!self.address && [building objectForKey:@"address"] != [NSNull null]) {
                NSString *address = [[building objectForKey:@"address"] stringByReplacingOccurrencesOfString:@"\\n" withString:@"\n"];
                if( [building objectForKey:@"name"] != [NSNull null]) {
                    address = [NSString stringWithFormat:@"%@\n%@", [building objectForKey:@"name"], address];
                }
                self.address = address;
            }
            if(!self.imageUrl && [building objectForKey:@"imageUrl"] != [NSNull null])
                self.imageUrl = [building objectForKey:@"imageUrl"];
            if([building objectForKey:@"latitude"] != [NSNull null] && [building objectForKey:@"longitude"] != [NSNull null]) {
                self.location = [[CLLocation alloc] initWithLatitude:[[building objectForKey:@"latitude"] doubleValue] longitude:[[building objectForKey:@"longitude"] doubleValue]];
            }
            if(!self.name && [building objectForKey:@"name"] != [NSNull null])
                self.name = [building objectForKey:@"name"];
            if(!self.description && [building objectForKey:@"longDescription"] != [NSNull null])
                self.description = [building objectForKey:@"longDescription"];
            if(!self.additionalServices && [building objectForKey:@"additionalServices"] != [NSNull null])
                self.additionalServices = [building objectForKey:@"additionalServices"];
            if(!([self.types count] > 0) && [building objectForKey:@"types"] != [NSNull null])
                self.types = [building objectForKey:@"types"];
            
            self.nameLabel.text = self.name;
            if([self.types count] > 0) {
                self.typeLabel.text = [self.types componentsJoinedByString:@", "];
            } else {
                self.typeLabel.text = nil;
            }

        }
        
        if(!self.name && !self.description && !self.additionalServices && !self.location && !self.address && !self.imageUrl) {
                self.nameLabel.text = NSLocalizedString(@"No information available for this building.", @"message when there is no information to show the user about a building they selected");
        }
    }
}

- (void) willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    self.widthConstraint.constant = [AppearanceChanger sizeInOrientation:toInterfaceOrientation].width;
}
-(void) didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    [self.descriptionTextView invalidateIntrinsicContentSize];
    [self resetScrollViewContentOffset];
}
-(void) resetScrollViewContentOffset
{
    [self.descriptionTextView setContentOffset:CGPointZero animated:YES];
    [self.scrollView setContentOffset:CGPointZero animated:YES];
}

@end
