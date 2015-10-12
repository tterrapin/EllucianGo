//
//  HomeViewController.h
//  Mobile
//
//  Created by Alan McEwan on 9/14/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UIViewController+SlidingViewExtension.h"

@interface HomeViewController : UIViewController

@property (weak, nonatomic) IBOutlet UIImageView *schoolLogoStripeImageView;
@property (weak, nonatomic) IBOutlet UIImageView *backgroundImageView;
@property (strong, nonatomic) IBOutlet UIButton *signInButton;
@property (weak, nonatomic) IBOutlet UIView *topMessageView;
@property (weak, nonatomic) IBOutlet UILabel *signInLabel;
@property (weak, nonatomic) IBOutlet UILabel *topMessageLabel;
@property (strong, nonatomic) IBOutlet UIImageView *arrowImageView;

- (IBAction)signIn:(id)sender;
- (IBAction)switchSchools:(id)sender;

@end
