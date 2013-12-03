//
//  AudioViewController.h
//  Mobile
//
//  Created by jkh on 7/2/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "Module+Attributes.h"
#import "AsynchronousImageView.h"

@interface AudioViewController : UIViewController<AVAudioPlayerDelegate>

@property (strong, nonatomic) Module *module;
@property (nonatomic, strong) AVPlayer *audioPlayer;
@property (weak, nonatomic) IBOutlet AsynchronousImageView *imageView;
@property (weak, nonatomic) IBOutlet UISlider *seeker;

@property (weak, nonatomic) IBOutlet UIButton *playButton;
@property (weak, nonatomic) IBOutlet UILabel *textLabel;
@property (weak, nonatomic) IBOutlet UIView *textLabelBackgroundView;
@property (weak, nonatomic) IBOutlet UIButton *backButton;
@property (weak, nonatomic) IBOutlet UIButton *forwardButton;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *scrollViewHeightConstraint;
@property (weak, nonatomic) IBOutlet UIView *controlsView;
@property (weak, nonatomic) IBOutlet UITextView *textTextView;

- (IBAction)goBack:(id)sender;
- (IBAction)goForward:(id)sender;
- (IBAction)play:(id)sender;

@end
