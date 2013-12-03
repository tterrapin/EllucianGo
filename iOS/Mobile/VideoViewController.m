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

@interface VideoViewController ()

@end

@implementation VideoViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
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
    
    UIImage *thumbnail = [self.moviePlayerViewController.moviePlayer thumbnailImageAtTime:0.0
                                               timeOption:MPMovieTimeOptionNearestKeyFrame];
    [self.moviePlayerViewController.moviePlayer stop];
    
    self.imageView.image = thumbnail;
    
    self.imageView.userInteractionEnabled = YES;
    
    UITapGestureRecognizer *tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(playMovie:)];
    tapRecognizer.numberOfTapsRequired = 1;
    [self.imageView addGestureRecognizer:tapRecognizer];
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
