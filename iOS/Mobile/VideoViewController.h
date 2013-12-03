//
//  VideoViewController.h
//  Mobile
//
//  Created by jkh on 6/28/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Module+Attributes.h"
#import <MediaPlayer/MediaPlayer.h>
#import "AsynchronousImageView.h"

@interface VideoViewController : UIViewController

@property (strong, nonatomic) Module *module;
@property (strong, nonatomic) MPMoviePlayerViewController *moviePlayerViewController;
@property (strong, nonatomic) IBOutlet UIView *videoView;
@property (weak, nonatomic) IBOutlet UILabel *label;
@property (weak, nonatomic) IBOutlet AsynchronousImageView *imageView;
@property (weak, nonatomic) IBOutlet UIView *playBackgroundView;
@property (weak, nonatomic) IBOutlet UIView *textBackgroundView;

- (IBAction)playMovie:(id)sender;

@end
