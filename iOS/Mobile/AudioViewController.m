//
//  AudioViewController.m
//  Mobile
//
//  Created by jkh on 7/2/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import "AudioViewController.h"
#import "UIColor+SchoolCustomization.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"

@interface AudioViewController ()

@property (nonatomic, strong) NSTimer *sliderTimer;

@end

@implementation AudioViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.navigationController.navigationBar.translucent = NO;
    
    NSString *urlString = [self.module propertyForKey:@"audio"];
    NSURL *url = [[NSURL alloc] initWithString:urlString];
    AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:url options:nil];
    AVPlayerItem* playerItem = [AVPlayerItem playerItemWithAsset:asset];
    
    
    NSArray *keys = @[@"duration"];
    [asset loadValuesAsynchronouslyForKeys:keys completionHandler:^() {
        
        NSError *error = nil;
        AVKeyValueStatus tracksStatus = [asset statusOfValueForKey:@"duration" error:&error];
        switch (tracksStatus) {
            case AVKeyValueStatusLoaded:
            {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self.playButton setEnabled:YES];
                    [self.forwardButton setEnabled:YES];
                    [self.backButton setEnabled:YES];
                    [self.seeker setEnabled:YES];
                    [self setSlider];
                });

                break;
            }
            case AVKeyValueStatusFailed:
                break;
            case AVKeyValueStatusCancelled:
                break;
            case AVKeyValueStatusUnknown:
                break;
            case AVKeyValueStatusLoading:
                break;
        }
    }];
    
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(itemDidFinishPlaying) name:AVPlayerItemDidPlayToEndTimeNotification object:playerItem];
    self.audioPlayer = [[AVPlayer alloc] initWithPlayerItem:playerItem];

    NSString *description = [self.module propertyForKey:@"description"];
    self.textLabel.text = description;
    self.textTextView.text = description;
    
    //iOS 7 needs this set again
    self.textTextView.textColor = [UIColor whiteColor];
    self.textTextView.backgroundColor = [UIColor colorWithRed:0.0f green:0.0f blue:0.0f alpha:1.0f];
    self.textTextView.font = [UIFont systemFontOfSize:17.0];

    NSString *imageUrl = [self.module propertyForKey:@"image"];
    [self.imageView loadImageFromURLString:imageUrl];
    self.title = self.module.name;
    self.seeker.thumbTintColor = [UIColor primaryColor];
    self.seeker.minimumTrackTintColor = [UIColor primaryColor];
    
    if([self.textLabel.text length] > 0) {
        UITapGestureRecognizer *tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(expandText:)];
        tapRecognizer.numberOfTapsRequired = 1;
        [self.textLabelBackgroundView addGestureRecognizer:tapRecognizer];
        
        UITapGestureRecognizer *tapTextViewRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(expandText:)];
        tapTextViewRecognizer.numberOfTapsRequired = 1;
        [self.textTextView addGestureRecognizer:tapTextViewRecognizer];
    } else {
        [self.textLabelBackgroundView setHidden:YES];
        [self.textLabel setHidden:YES];        
    }
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Audio" forModuleNamed:self.module.name];
}

- (IBAction)goBack:(id)sender {
    CMTime newTime = CMTimeMakeWithSeconds(0, 1);
    [self.audioPlayer seekToTime:newTime];
    [self updateSlider];
}

- (IBAction)goForward:(id)sender {
    CMTime newTime = CMTimeMakeWithSeconds([self durationInSeconds]+1, 1);
    [self.audioPlayer seekToTime:newTime];
    [self updateSlider];
}

- (IBAction)play:(id)sender {
    if([self isPlaying]) {
        [self.audioPlayer pause];
        [self.playButton setImage:[UIImage imageNamed:@"media_play"] forState:UIControlStateNormal];
    } else {
        [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionButton_Press withLabel:@"Play button pressed" withValue:nil forModuleNamed:self.module.name];
        [self.audioPlayer play ];
        [self.playButton setImage:[UIImage imageNamed:@"media_pause"] forState:UIControlStateNormal];
    }
}

-(BOOL)isPlaying
{
    if (self.audioPlayer.currentItem && self.audioPlayer.rate != 0)
    {
        return YES;
    }
    return NO;
}

- (void)viewWillDisappear:(BOOL)animated {
    [self.sliderTimer invalidate];
    self.sliderTimer = nil;
    [self.audioPlayer pause];
    [self setImageView:nil];
    [self setSeeker:nil];

    [super viewWillDisappear:animated];
}

-(IBAction)sliding:(id)sender{
    
    CMTime newTime = CMTimeMakeWithSeconds(self.seeker.value, 1);
    [self.audioPlayer seekToTime:newTime];
}

-(void)setSlider{
    
    self.sliderTimer = [NSTimer scheduledTimerWithTimeInterval:0.1 target:self selector:@selector(updateSlider) userInfo:nil repeats:YES];
    Float64 duration = [self durationInSeconds];
    if(!isnan(duration)) {
        self.seeker.maximumValue = duration;
    } else {
        self.seeker.maximumValue = 0.0;
    }
    [self.seeker addTarget:self action:@selector(sliding:) forControlEvents:UIControlEventValueChanged];
    self.seeker.minimumValue = 0.0;
    self.seeker.continuous = YES;
}

- (void)updateSlider
{
    if([self durationInSeconds] > 0) {
        self.seeker.maximumValue = [self durationInSeconds];
        self.seeker.value = [self currentTimeInSeconds];
    } else {
        self.seeker.enabled = NO;
    }
}

- (Float64)durationInSeconds {
    Float64 dur = CMTimeGetSeconds([self.audioPlayer.currentItem duration]);
    return dur;
}


- (Float64)currentTimeInSeconds {
    Float64 dur = CMTimeGetSeconds([self.audioPlayer currentTime]);
    return dur;
}

-(void)itemDidFinishPlaying {
    [self.playButton setImage:[UIImage imageNamed:@"media_play"] forState:UIControlStateNormal];
    CMTime newTime = CMTimeMakeWithSeconds(0, 1);
    [self.audioPlayer seekToTime:newTime];
    [self updateSlider];
}

- (IBAction)expandText:(id)sender {
    
    if(self.textLabelBackgroundView.hidden) { //shrink
        self.textLabel.hidden = NO;
        self.textLabelBackgroundView.hidden = NO;
        self.textTextView.hidden = YES;
    } else { //grow
        self.textLabel.hidden = YES;
        self.textLabelBackgroundView.hidden = YES;
        self.textTextView.hidden = NO;
    }
}

@end
