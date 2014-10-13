//
//  MapsViewController.m
//  Mobile
//
//  Created by Jason Hocker on 9/6/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "MapsViewController.h"
#import "Map.h"
#import "Module+Attributes.h"
#import "MapCampus.h"
#import "MapPOI.h"
#import "MapPinAnnotation.h"
#import "POIListViewController.h"
#import "POIDetailViewController.h"
#import "MapPOIType.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "SlidingViewController.h"

@interface MapsViewController ()

@property (strong, nonatomic) NSArray *campuses;
@property (strong, nonatomic) MapCampus *selectedCampus;
@property (nonatomic, strong) UIActionSheet *actionSheet;
@property (nonatomic, strong) CLLocationManager *locationManager;

@end

@implementation MapsViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    if(!self.locationManager) {
        self.locationManager = [[CLLocationManager alloc] init];
        self.locationManager.delegate = self;
        self.locationManager.desiredAccuracy = kCLLocationAccuracyBest;
    }
    
    self.zoomWithCurrentLocationButton.enabled = NO;
    
    self.mapView.delegate = self;
    
    self.title = self.module.name;
    self.navigationController.navigationBar.translucent = NO;
    self.searchDisplayController.searchBar.translucent = NO;
    self.toolbar.translucent = NO;
    
    [self fetchCachedMaps];
    if ([self.campuses count] == 0) {
        [self fetchMaps];
        [self fetchCachedMaps];
    } else {
        [self fetchMapsInBackground];
    }
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(menuOpened:) name:kSlidingViewOpenMenuAppearsNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(menuClosed:) name:kSlidingViewTopResetNotification object:nil];
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Map of campus" forModuleNamed:self.module.name];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(menuOpened:) name:kSlidingViewOpenMenuAppearsNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(menuClosed:) name:kSlidingViewTopResetNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(didBecomeActive:)
                                                 name:UIApplicationDidBecomeActiveNotification object:nil];

    [self startTrackingLocation];
}

- (void)viewWillDisappear:(BOOL)animated{
    [super viewWillDisappear:animated];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    self.mapView.showsUserLocation = NO;
    [self.locationManager stopUpdatingLocation];
}


- (IBAction)campusSelector:(id)sender {

    [self sendEventWithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionButton_Press withLabel:@"Tap campus selector" withValue:nil forModuleNamed:self.module.name];
    if([self.actionSheet isVisible]) {
        [self.actionSheet dismissWithClickedButtonIndex:[self.actionSheet cancelButtonIndex] animated:YES];
        
        self.actionSheet.delegate = nil;
        self.actionSheet = nil;
        
        return;
    }
    
    if(nil == self.actionSheet) {
        self.actionSheet = [[UIActionSheet alloc] initWithTitle:NSLocalizedString(@"Select Campus", @"title of action sheet for user to select which campus to see on the map")
                                                                 delegate:self
                                                        cancelButtonTitle:nil
                                                   destructiveButtonTitle:nil
                                                        otherButtonTitles:nil];
        
      
        if(![self shouldPresentActionSheet:self.actionSheet]) {
            self.actionSheet = nil;
            return;
        }
        
        
        [self.actionSheet setCancelButtonIndex:[self.actionSheet addButtonWithTitle:NSLocalizedString(@"Cancel", @"Cancel")]];
        
    }
    
    if(UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        [self.actionSheet showFromBarButtonItem:self.campusSelectionButton animated:YES];
    } else {
        [self.actionSheet showFromToolbar:self.navigationController.toolbar];
    }
     
}

-(void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (buttonIndex == actionSheet.cancelButtonIndex) { return; }
    
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionInvoke_Native withLabel:@"Select campus" withValue:nil forModuleNamed:self.module.name];
    MapCampus *campus = [self.campuses objectAtIndex:buttonIndex];
    NSUserDefaults *userDefaults =[NSUserDefaults standardUserDefaults];
    NSString *key = [NSString stringWithFormat:@"%@-%@", @"mapLastCampus", self.module.internalKey ];
    [userDefaults setObject:campus.campusId forKey:key];
    [self showCampus:campus];
}

-(void) showCampus:(MapCampus *)campus
{
    self.selectedCampus = campus;
    self.title = campus.name;
    //copy your annotations to an array
    NSMutableArray *annotationsToRemove = [[NSMutableArray alloc] initWithArray: self.mapView.annotations];
    //Remove the object userlocation
    [annotationsToRemove removeObject: self.mapView.userLocation];
    //Remove all annotations in the array from the mapView
    [self.mapView removeAnnotations: annotationsToRemove];
    
    CLLocationCoordinate2D locationCenter;
    locationCenter.latitude = [campus.centerLatitude doubleValue];
    locationCenter.longitude = [campus.centerLongitude doubleValue];
    
    MKCoordinateSpan locationSpan;
    locationSpan.latitudeDelta = [campus.spanLatitude doubleValue];
    locationSpan.longitudeDelta = [campus.spanLongitude doubleValue];
        
    MKCoordinateRegion region = MKCoordinateRegionMake(locationCenter, locationSpan);
    if(locationSpan.latitudeDelta < 180 && locationSpan.longitudeDelta < 360 && locationCenter.latitude <= 90 && locationCenter.latitude >= -90 && locationCenter.longitude <= 180 && locationCenter.longitude >= -180) {
        [self.mapView setRegion:region animated:YES];
    }
    
    for(MapPOI *poi in campus.points) {
        MapPinAnnotation *annotation = [[MapPinAnnotation alloc] initWithMapPOI:poi];
        [self.mapView addAnnotation:annotation];
    }
}

- (MKAnnotationView *)mapView:(MKMapView *)theMapView viewForAnnotation:(id <MKAnnotation>)annotation {
	// If it's the user location, just return nil.
	if ([annotation isKindOfClass:[MKUserLocation class]])
		return nil;
	// If it is our MapPinAnnotation, we create and return its view
	if ([annotation isKindOfClass:[MapPinAnnotation class]]) {
		// try to dequeue an existing pin view first
		static NSString* pinAnnotationIdentifier = @"PinAnnotationIdentifier";
		MKPinAnnotationView* pinView = (MKPinAnnotationView *)[self.mapView dequeueReusableAnnotationViewWithIdentifier:pinAnnotationIdentifier ];
		if (!pinView) {
			// If an existing pin view was not available, create one
			MKPinAnnotationView* customPinView = [[MKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:pinAnnotationIdentifier];
			customPinView.pinColor = MKPinAnnotationColorRed;
			customPinView.animatesDrop = YES;
			customPinView.canShowCallout = YES;
            
			// add a detail disclosure button to the callout which will open a new view controller page
			UIButton* rightButton = [UIButton buttonWithType:UIButtonTypeDetailDisclosure];
			customPinView.rightCalloutAccessoryView = rightButton;
            
			return customPinView;
		} else {
			pinView.annotation = annotation;
		}
		return pinView;
	}
    return nil;
}

- (void)mapView:(MKMapView *)_mapView annotationView:(MKAnnotationView *)view calloutAccessoryControlTapped:(UIControl *)control {
    if ([view.annotation isKindOfClass:[MapPinAnnotation class]]) {
        [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionButton_Press withLabel:@"Select Map Pin" withValue:nil forModuleNamed:self.module.name];
        MapPinAnnotation *annotation = view.annotation;
        [self performSegueWithIdentifier:@"Show POI" sender:annotation.poi];
    }
}

-(void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:@"Show POI List"])
    {
        [self sendEventWithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionButton_Press withLabel:@"Tap building icon" withValue:nil forModuleNamed:self.module.name];
        POIListViewController *vc = (POIListViewController *) [segue destinationViewController];
        vc.module = self.module;
    }  else if ([[segue identifier] isEqualToString:@"Show POI"])
    {
        MapPOI *poi = sender;
        POIDetailViewController *vc = (POIDetailViewController *)[segue destinationViewController];
        vc.imageUrl = poi.imageUrl;
        vc.name = poi.name;
        NSMutableArray *types = [[NSMutableArray alloc] init];
        for(MapPOIType *type in poi.types) {
            [types addObject:type.name];
        }
        vc.types = [types copy];
        vc.location = [[CLLocation alloc] initWithLatitude:[poi.latitude doubleValue] longitude:[poi.longitude doubleValue]];
        vc.address = poi.address;
        vc.poiDescription = poi.description_;
        vc.additionalServices = poi.additionalServices;
        vc.buildingId = poi.buildingId;
        vc.campusName = poi.campus.name;
        vc.module = self.module;
    }
}

-(BOOL)shouldPresentActionSheet:(UIActionSheet *)actionSheet
{
    if(actionSheet == self.actionSheet) {
        if([self.campuses count] == 0) {
            return NO;
        }
        for(MapCampus *campus in self.campuses) {
            [self.actionSheet addButtonWithTitle:campus.name];
        }
                
    }
    return YES;
}

- (IBAction)showMyLocation:(id)sender {
    
    [self sendEventWithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionInvoke_Native withLabel:@"Geolocate user" withValue:nil forModuleNamed:self.module.name];
    
    MKUserLocation *userLocation = self.mapView.userLocation;
    
    float maxNorth = MAX([self.selectedCampus.centerLatitude floatValue] + [self.selectedCampus.spanLatitude floatValue], userLocation.location.coordinate.latitude);
    float maxSouth = MIN([self.selectedCampus.centerLatitude floatValue] - [self.selectedCampus.spanLatitude floatValue], userLocation.location.coordinate.latitude);
    float maxEast = MAX([self.selectedCampus.centerLongitude floatValue] + [self.selectedCampus.spanLongitude floatValue], userLocation.location.coordinate.longitude);
    float maxWest = MIN([self.selectedCampus.centerLongitude floatValue] - [self.selectedCampus.spanLongitude floatValue], userLocation.location.coordinate.longitude);
    
    float centerLatitude = (maxNorth + maxSouth) / 2.0f;
    float centerLongitude = (maxEast + maxWest) / 2.0f;
    float spanLatitude = ABS(maxNorth - maxSouth);
    float spanLongitude = ABS(maxEast - maxWest);
    
    CLLocationCoordinate2D locationCenter;
    locationCenter.latitude = centerLatitude;
    locationCenter.longitude = centerLongitude;
    
    MKCoordinateSpan locationSpan;
    locationSpan.latitudeDelta = spanLatitude;
    locationSpan.longitudeDelta = spanLongitude;
    
    MKCoordinateRegion region = MKCoordinateRegionMake(locationCenter, locationSpan);
    [self.mapView setRegion:region animated:YES];
}

- (IBAction)mapTypeChanged:(id)sender {
    [self sendEventWithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionInvoke_Native withLabel:@"Change map view" withValue:nil forModuleNamed:self.module.name];
    UISegmentedControl *segmentedControl = (UISegmentedControl *)sender;
	switch([segmentedControl selectedSegmentIndex]) {
        case 0: {
            self.mapView.mapType = MKMapTypeStandard;
            break;
        }
        case 1: {
            self.mapView.mapType = MKMapTypeSatellite;
            break;
        }
        case 2: {
            self.mapView.mapType = MKMapTypeHybrid;
            break;
        }
    }
}


- (void) fetchMaps {
    
    [self downloadMaps:self.module.managedObjectContext WithURL:[self.module propertyForKey:@"campuses"]];
    NSError *error = nil;
    if(![self.module.managedObjectContext save:&error]) {
        NSLog(@"Could not save to store after update to maps: %@", [error userInfo]);
    }
}

- (void) fetchMapsInBackground {

    NSManagedObjectContext *importContext = [[NSManagedObjectContext alloc] initWithConcurrencyType:NSPrivateQueueConcurrencyType];
    importContext.parentContext = self.module.managedObjectContext;
    NSString *urlString = [self.module propertyForKey:@"campuses"];
    [importContext performBlock: ^{
        NSError *error;
        [self downloadMaps:importContext WithURL:urlString];
            //save to main context
            if (![importContext save:&error]) {
                NSLog(@"Could not save to main context after update to map: %@", [error userInfo]);
            }
            
            [importContext.parentContext performBlock:^{

            NSError *parentError = nil;
            if(![importContext.parentContext save:&parentError]) {
                NSLog(@"Could not save to store after update to maps: %@", [parentError userInfo]);
            }
            [self fetchCachedMaps];
        }];
    }];
}

- (void) downloadMaps:(NSManagedObjectContext *)context WithURL:(NSString *)urlString
{
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
        
        Map *map = nil;
        
        NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"Map"];
        request.predicate = [NSPredicate predicateWithFormat:@"moduleName = %@", self.module.internalKey];
        NSSortDescriptor *sortDescriptor = [NSSortDescriptor sortDescriptorWithKey:@"moduleName" ascending:YES];
        request.sortDescriptors = [NSArray arrayWithObject:sortDescriptor];
        
        NSArray *matches = [context executeFetchRequest:request error:&error];
        
        if ([matches count] == 1) {
            map = [matches lastObject];
            [context deleteObject:map];
        }
        map = [NSEntityDescription insertNewObjectForEntityForName:@"Map" inManagedObjectContext:context];
        map.moduleName = self.module.internalKey;
        
        //fetch types
        NSFetchRequest *typeRequest = [[NSFetchRequest alloc] init];
        NSEntityDescription *typeEntity = [NSEntityDescription entityForName:@"MapPOIType" inManagedObjectContext:context];
        [typeRequest setEntity:typeEntity];
        NSPredicate *typePredicate =[NSPredicate predicateWithFormat:@"moduleInternalKey = %@",self.module.internalKey];
        [typeRequest setPredicate:typePredicate];
        
        NSArray *typeArray = [context executeFetchRequest:typeRequest error:&error];
        NSMutableDictionary *typeMap = [[NSMutableDictionary alloc] init];
        for(MapPOIType *poiType in typeArray) {
            [typeMap setObject:poiType forKey:poiType.name];
        }
        
        for(NSDictionary *campus in [json objectForKey:@"campuses"]) {
            MapCampus *managedCampus = [NSEntityDescription insertNewObjectForEntityForName:@"MapCampus" inManagedObjectContext:context];
            managedCampus.name = [campus objectForKey:@"name"];
            managedCampus.campusId = [campus objectForKey:@"id"];
            
            float nwLatitude = [[campus valueForKey:@"northWestLatitude"] floatValue];
            float nwLongitude = [[campus valueForKey:@"northWestLongitude"] floatValue];
            float seLatitude = [[campus valueForKey:@"southEastLatitude"] floatValue];
            float seLongitude = [[campus valueForKey:@"southEastLongitude"] floatValue];
            
            managedCampus.centerLatitude = [NSNumber numberWithFloat:(nwLatitude + seLatitude) / 2.0f];
            managedCampus.centerLongitude = [NSNumber numberWithFloat:(nwLongitude + seLongitude) / 2.0f];
            managedCampus.spanLatitude = [NSNumber numberWithFloat:ABS(nwLatitude - seLatitude)];
            managedCampus.spanLongitude = [NSNumber numberWithFloat:ABS(nwLongitude - seLongitude)];
            [map addCampusesObject:managedCampus];
            managedCampus.map = map;
            

            for(NSDictionary *building in [campus objectForKey:@"buildings"]) {       
                MapPOI *managedPOI = [NSEntityDescription insertNewObjectForEntityForName:@"MapPOI" inManagedObjectContext:context];
                managedPOI.campus = managedCampus;
                managedPOI.moduleInternalKey = self.module.internalKey;

                [managedCampus addPointsObject:managedPOI];
                if([building objectForKey:@"type"] != [NSNull null]) {
                    //managedPOI.type = [building objectForKey:@"type"];
                
                    for (NSString *type in [building objectForKey:@"type"]) {
                        if(type != (NSString *)[NSNull null]) {
                            MapPOIType* typeObject = [typeMap objectForKey:type];
                            if(!typeObject) {
                                typeObject = [NSEntityDescription insertNewObjectForEntityForName:@"MapPOIType" inManagedObjectContext:context];
                                typeObject.name = type;
                                typeObject.moduleInternalKey = self.module.internalKey;
                                [typeMap setObject:typeObject forKey:typeObject.name];
                            }
                            [managedPOI addTypesObject:typeObject];
                            [typeObject addPointsOfInterestObject:managedPOI];
                        }
                    }
                }
                managedPOI.name = [building objectForKey:@"name"];
                if([building objectForKey:@"address"] != [NSNull null])
                    managedPOI.address = [[building objectForKey:@"address"] stringByReplacingOccurrencesOfString:@"\\n" withString:@"\n"];
                if([building objectForKey:@"longDescription"] != [NSNull null])
                    managedPOI.description_ = [building objectForKey:@"longDescription"];
                if([building objectForKey:@"latitude"] != [NSNull null])
                    managedPOI.latitude = [NSNumber numberWithFloat:[[building objectForKey:@"latitude"] floatValue]];
                if([building objectForKey:@"longitude"] != [NSNull null])
                    managedPOI.longitude = [NSNumber numberWithFloat:[[building objectForKey:@"longitude"] floatValue]];
                if([building objectForKey:@"imageUrl"] != [NSNull null])
                    managedPOI.imageUrl = [building objectForKey:@"imageUrl"];
                if([building objectForKey:@"additionalServices"] != [NSNull null])
                    managedPOI.additionalServices = [[building objectForKey:@"additionalServices"] stringByReplacingOccurrencesOfString:@"\\n" withString:@"\n"];
                if([building objectForKey:@"buildingId"] != [NSNull null])
                    managedPOI.buildingId = [building objectForKey:@"buildingId"];
                
            }
        }
    }

}

- (void) fetchCachedMaps
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"MapCampus" inManagedObjectContext:self.module.managedObjectContext];
    [fetchRequest setEntity:entity];
    
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"map.moduleName = %@", self.module.internalKey];
    NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"name" ascending:YES];
    
    NSArray *sortDescriptors = [NSArray arrayWithObject:sortDescriptor];
    [fetchRequest setSortDescriptors:sortDescriptors];
    
    [fetchRequest setPredicate:predicate];
    
    NSError *error = nil;
    self.campuses = [self.module.managedObjectContext executeFetchRequest:fetchRequest error:&error];
    if([self.campuses count] > 1) {
        self.campusSelectionButton.enabled = YES;
        self.buildingsButton.enabled = YES;
        
        NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
        NSString *key = [NSString stringWithFormat:@"%@-%@", @"mapLastCampus", self.module.internalKey ];
        NSString *previousCampus = [userDefaults objectForKey:key];
        
        if(previousCampus) {
            for(MapCampus *campus in self.campuses) {
                if([campus.campusId isEqualToString:previousCampus]) {
                    [self showCampus:campus];
                }
            }
        } else if([CLLocationManager locationServicesEnabled]) {
            
            double distance = DBL_MAX;
            for(MapCampus *campus in self.campuses) {
            
                double lat1 = self.mapView.userLocation.coordinate.latitude*M_PI/180.0;
                double lon1 = self.mapView.userLocation.coordinate.longitude*M_PI/180.0;
                double lat2 = [campus.centerLatitude doubleValue]*M_PI/180.0;
                double lon2 = [campus.centerLongitude doubleValue]*M_PI/180.0;
            
                double calculatedDistance = acos(sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(lon2 - lon1)) * 6368500.0;
            
                if(calculatedDistance < distance) {
                    self.selectedCampus = campus;
                    distance = calculatedDistance;
                }
            
            }
            [self showCampus:self.selectedCampus];
        } else {
            MapCampus *campus = [self.campuses firstObject];
            [self showCampus:campus];
        }
    } else if ([self.campuses count] == 1) {
        self.campusSelectionButton.enabled = YES;
        self.buildingsButton.enabled = YES;
        self.selectedCampus = [self.campuses lastObject];
        [self showCampus:self.selectedCampus];
    } else {
        self.campusSelectionButton.enabled = NO;
        self.buildingsButton.enabled = NO;
    }
    
}

- (NSFetchRequest *)searchFetchRequest
{
    if(_searchFetchRequest != nil) {
        return _searchFetchRequest;
    }
    _searchFetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"MapPOI" inManagedObjectContext:self.module.managedObjectContext];
    [_searchFetchRequest setEntity:entity];
    
    NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"name" ascending:YES];
    [_searchFetchRequest setSortDescriptors:[NSArray arrayWithObject:sortDescriptor]];
    return _searchFetchRequest;
}

-(void)searchForText:(NSString *)searchText
{
    //NSPredicate *predicate = [NSPredicate predicateWithFormat:@"modname CONTAINS[cd] %@", searchText];
    self.searchFetchRequest.predicate = [NSPredicate predicateWithFormat:@"moduleInternalKey = %@ AND (name CONTAINS[cd] %@ OR ANY types.name CONTAINS[cd] %@ OR campus.name CONTAINS[cd] %@)", self.module.internalKey, searchText, searchText, searchText];
    
    NSError *error;
    self.filteredList = [self.module.managedObjectContext executeFetchRequest:self.searchFetchRequest error:&error];
}

-(void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    self.searchFetchRequest = nil;
}

- (BOOL) searchDisplayController:(UISearchDisplayController *)controller shouldReloadTableForSearchString:(NSString *)searchString
{
    [self searchForText:searchString];
    return YES;
}

-(NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.filteredList count];
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"MapPOI Search"];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:@"MapPOI Search"];
    }
    
    MapPOI *poi = [self.filteredList objectAtIndex:indexPath.row];
    cell.textLabel.text = poi.name;
    cell.detailTextLabel.text = poi.campus.name;
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    MapPOI *poi = [self.filteredList objectAtIndex:indexPath.row];
    [self performSegueWithIdentifier:@"Show POI" sender:poi];

}

- (void)searchBarTextDidBeginEditing:(UISearchBar *)searchBar
{
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionSearch withLabel:@"Search" withValue:nil forModuleNamed:nil];
}

-(void) menuOpened:(id)sender
{
    self.mapView.scrollEnabled = NO;
}

-(void) menuClosed:(id)sender
{
    self.mapView.scrollEnabled = YES;
}

- (void)startTrackingLocation
{
    CLAuthorizationStatus status = [CLLocationManager authorizationStatus];
    if (status == kCLAuthorizationStatusNotDetermined) {
        if([self.locationManager respondsToSelector:@selector(requestWhenInUseAuthorization)]) {
            [self.locationManager requestWhenInUseAuthorization];
        }
    }
    else if (status == kCLAuthorizationStatusAuthorizedWhenInUse || status == kCLAuthorizationStatusAuthorizedAlways) {
        [self.locationManager startUpdatingLocation];
        self.zoomWithCurrentLocationButton.enabled = YES;
        self.mapView.showsUserLocation = YES;
    } else if (status == kCLAuthorizationStatusDenied) {
        self.zoomWithCurrentLocationButton.enabled = NO;
    }
}

- (void)locationManager:(CLLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status {
    switch (status) {
        case kCLAuthorizationStatusAuthorizedAlways:
        case kCLAuthorizationStatusAuthorizedWhenInUse:
            [self startTrackingLocation];
            self.zoomWithCurrentLocationButton.enabled = YES;
            self.mapView.showsUserLocation = YES;
            break;
        case kCLAuthorizationStatusNotDetermined:
            if([self.locationManager respondsToSelector:@selector(requestWhenInUseAuthorization)]) {
                [self.locationManager requestWhenInUseAuthorization];
            }
            break;
        default:
            break;
    }
}

-(void) didBecomeActive:(id)sender
{
    CLAuthorizationStatus status = [CLLocationManager authorizationStatus];
    if (status == kCLAuthorizationStatusAuthorizedWhenInUse || status == kCLAuthorizationStatusAuthorizedAlways) {
        self.zoomWithCurrentLocationButton.enabled = YES;
    } else if (status == kCLAuthorizationStatusDenied) {
        self.zoomWithCurrentLocationButton.enabled = NO;
    }

}

@end
