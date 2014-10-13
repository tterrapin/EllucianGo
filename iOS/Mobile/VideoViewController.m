//
//  VideoViewController.m
//  Mobile
//
//  Created by jkh on 6/28/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import "VideoViewController.h"
#import "AppearanceChanger.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "MBProgressHUD.h"
#import <AVFoundation/AVFoundation.h>

@interface VideoViewController ()

@end

@implementation VideoViewController

- (void)viewDidLoad
{
    [super viewDidLoad];

    MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    hud.labelText = NSLocalizedString(@"Loading", @"loading message while waiting for data to load");
    [self.mediaPlayOverlay setHidden:YES];

    self.navigationController.navigationBar.translucent = NO;
    
    self.title = self.module.name;
    self.label.text = [self.module propertyForKey:@"description"];
    if([self.label.text length] == 0) {
        [self.textBackgroundView setHidden:YES];
        [self.label setHidden:YES];
    }
    self.playBackgroundView.backgroundColor = [UIColor primaryColor];
    NSString *urlString = [self.module propertyForKey:@"video"];
    NSURL *fileURL = [NSURL URLWithString:urlString];

    self.moviePlayerViewController = [[MPMoviePlayerViewController alloc] initWithContentURL:fileURL];
    
    dispatch_async(dispatch_get_global_queue(0,0), ^{


        AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:fileURL options:nil];
        AVAssetImageGenerator *generate = [[AVAssetImageGenerator alloc] initWithAsset:asset];
        generate.appliesPreferredTrackTransform = YES;
        NSError *error = NULL;
        CMTime time = CMTimeMake(0, 600);
        CGImageRef imageRef = [generate copyCGImageAtTime:time actualTime:NULL error:&error];
        UIImage *image = [[UIImage alloc] initWithCGImage:imageRef];
        dispatch_async(dispatch_get_main_queue(), ^{
            if(image) {

                    [MBProgressHUD hideHUDForView:self.view animated:YES];
                    [self.mediaPlayOverlay setHidden:NO];
                    [self.imageView setImage:image];
                    self.imageView.userInteractionEnabled = YES;
        
                    UITapGestureRecognizer *tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(playMovie:)];
                    tapRecognizer.numberOfTapsRequired = 1;
                    [self.imageView addGestureRecognizer:tapRecognizer];
                }
        });
    });
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Video" forModuleNamed:self.module.name];
}

-(void) playMovie:(id)sender
{
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionButton_Press withLabel:@"Play button pressed" withValue:nil forModuleNamed:self.module.name];
    [self.moviePlayerViewController.moviePlayer prepareToPlay];
    [self presentMoviePlayerViewControllerAnimated:self.moviePlayerViewController];
}

@end
