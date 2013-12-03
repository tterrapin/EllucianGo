//
//  CourseAssignmentDetailViewController.m
//  Mobile
//
//  Created by Jason Hocker on 6/6/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import "CourseAssignmentDetailViewController.h"
#import "SafariActivity.h"
#import "WebViewController.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "AppearanceChanger.h"

@interface CourseAssignmentDetailViewController ()

@end

@implementation CourseAssignmentDetailViewController


- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.navigationController setToolbarHidden:NO animated:NO];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.navigationController.navigationBar.translucent = NO;
    
    if([AppearanceChanger isRTL]) {
        self.titleLabel.textAlignment = NSTextAlignmentRight;
        self.descriptionTextView.textAlignment = NSTextAlignmentRight;
    }
    
    UIBarButtonItem *shareButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAction target:self action:@selector(takeAction:)];
    
    UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    
    self.toolbarItems = [ NSArray arrayWithObjects: flexibleSpace, shareButtonItem, nil ];
    self.navigationController.toolbar.translucent = NO;
    
    self.titleLabel.text = self.itemTitle;
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
    [dateFormatter setTimeStyle:NSDateFormatterShortStyle];
    
    if(self.itemPostDateTime) {
        self.dateLabel.text = [NSString stringWithFormat:NSLocalizedString(@"Due: %@", @"due date label with date"), [dateFormatter stringFromDate:self.itemPostDateTime]];
    } else {
        self.dateLabel.text = NSLocalizedString(@"Due: None assigned", "no due date for assignment");
    }
    self.descriptionTextView.text = self.itemContent;
    
    self.backgroundView.backgroundColor = [UIColor accentColor];

    self.titleLabel.textColor = [UIColor subheaderTextColor];
    self.dateLabel.textColor = [UIColor subheaderTextColor];

}

-(void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [self.navigationController setToolbarHidden:YES animated:NO];
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self sendView:@"Course assignments detail" forModuleNamed:self.module.name];
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    
    if ([[segue identifier] isEqualToString:@"Show Website"]) {
        WebViewController *detailController = (WebViewController *)[segue destinationViewController];
        detailController.loadRequest = [[NSURLRequest alloc] initWithURL:[NSURL URLWithString:self.itemLink]];
        detailController.title = self.itemTitle;
        detailController.analyticsLabel = self.module.name;
    }
}

- (IBAction) takeAction:(id)sender {
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionFollow_web withLabel:@"Open assignment in web frame" withValue:nil forModuleNamed:self.module.name];
    [self performSegueWithIdentifier:@"Show Website" sender:sender];
}


@end
