//
//  flickrViewController.m
//  Mobile
//
//  Created by jkh on 11/1/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import "flickrViewController.h"
#import "Module+Attributes.h"
#import "UIColor+SchoolCustomization.h"
#import "AsynchronousImageView.h"
#import "MBProgressHUD.h"

@interface flickrViewController ()

@end

@implementation flickrViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    
    // Use the name of the module that was defined in the cloud as the title of this
    self.title = self.module.name;
    
    // Set the labels to be the color that was set in the cloud
    self.descriptionLabel.textColor = [UIColor accentColor];
    self.dateUploadedLabel.textColor = [UIColor accentColor];
    
    // Show the progess HUD
    MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo: self.view animated:YES];
    hud.labelText = NSLocalizedString(@"Loading", @"loading message while waiting for data to load");
    
    // After showing the progrss HUD
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 0.01 * NSEC_PER_SEC), dispatch_get_main_queue(), ^(void){
        
        // Get the API key from the Customizations.plist
        NSString *api_key = [self.module propertyForKey:@"apiKey"];
        
        // Get the user id from the cloud
        NSString *user_id = [self.module propertyForKey:@"userId"];
        
        // Build the flickr URL
        NSString *urlString = [NSString stringWithFormat:@"http://api.flickr.com/services/rest/?method=flickr.people.getPublicPhotos&api_key=%@&per_page=1&format=json&nojsoncallback=1&user_id=%@&extras=description,date_taken,url_m", api_key, user_id];
        
        // Download the response from flickr
        NSData *responseData = [NSData dataWithContentsOfURL: [NSURL URLWithString: urlString]];
        
        NSError *error;
        if(responseData)
        {
            // Parse the json response
            NSDictionary* json = [NSJSONSerialization
                                  JSONObjectWithData:responseData
                                  options:kNilOptions
                                  error:&error];
            NSDictionary *photoDictionary = [[json objectForKey:@"photos"] objectForKey:@"photo"][0] ;
            NSString *url_m = [photoDictionary objectForKey:@"url_m"];
            NSString *description = [[photoDictionary objectForKey:@"description"] objectForKey:@"_content"];
            NSString *dateTaken = [photoDictionary objectForKey:@"datetaken"];
            
            // Download the image from flickr asychronously
            [self.imageView loadImageFromURLString:url_m];
            
            // Set the text in the labels
            self.descriptionLabel.text = description;
            self.dateUploadedLabel.text = dateTaken;
            
            // Hide the progress hud
            [MBProgressHUD hideHUDForView:self.view animated:YES];
        }
    });
}

@end
