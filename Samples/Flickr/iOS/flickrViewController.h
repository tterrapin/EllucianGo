//
//  flickrViewController.h
//  Mobile
//
//  Created by jkh on 11/1/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>

@class Module, AsynchronousImageView;

@interface flickrViewController : UIViewController

@property (strong, nonatomic) Module *module;
@property (weak, nonatomic) IBOutlet AsynchronousImageView *imageView;
@property (weak, nonatomic) IBOutlet UILabel *descriptionLabel;
@property (weak, nonatomic) IBOutlet UILabel *dateUploadedLabel;

@end
