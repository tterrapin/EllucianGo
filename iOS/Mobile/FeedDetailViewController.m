//
//  FeedDetailViewController.m
//  Mobile
//
//  Created by Jason Hocker on 8/17/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "FeedDetailViewController.h"
#import "SafariActivity.h"
#import "AppearanceChanger.h"
#import "FeedCategory.h"

@interface FeedDetailViewController ()

@end

@implementation FeedDetailViewController

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
        self.dateLabel.textAlignment = NSTextAlignmentRight;
        self.categoryValue.textAlignment = NSTextAlignmentRight;
    }
    
    UIBarButtonItem *shareButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAction target:self action:@selector(share:)];
    
    UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    
    self.toolbarItems = [ NSArray arrayWithObjects: flexibleSpace, shareButtonItem, nil ];
     self.navigationController.toolbar.translucent = NO;
    
    self.titleLabel.text = self.itemTitle;
    
    NSMutableArray *categoryValues = [[NSMutableArray alloc] init];
    for(FeedCategory* value in self.itemCategory) {
        [categoryValues addObject:value.name];
    }
    if ( [self.itemCategory count] > 0 ) {
        self.categoryValue.text = [NSString stringWithFormat:@"%@: %@", NSLocalizedString(@"Category", "label for the categories"), [categoryValues componentsJoinedByString:@", "]];
    } else {
        self.categoryValue.text = @"";
    }
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
    [dateFormatter setTimeStyle:NSDateFormatterShortStyle];
    self.dateLabel.text = [dateFormatter stringFromDate:self.itemPostDateTime];
    [self setDescriptionText:self.itemContent];
    if(self.itemImageUrl) {
        [self.imageView loadImageFromURLString: self.itemImageUrl];

    } else {
        self.imageHeightConstraint.constant = 0;
        self.imageWidthConstraint.constant = 0;
        self.horizontalSpaceConstraint.constant = 0;
        
    }
    
    self.backgroundView.backgroundColor = [UIColor accentColor];
    self.titleLabel.textColor = [UIColor subheaderTextColor];
    self.dateLabel.textColor = [UIColor subheaderTextColor];
    self.categoryValue.textColor = [UIColor subheaderTextColor];
    
    //hack to set description to be correct size
//    CGRect frame = self.descriptionTextView.frame;
//    frame.size.height = self.descriptionTextView.contentSize.height;
//    self.descriptionTextView.frame = frame;
    
    [self.descriptionWebView setDelegate:self];
    
       
}

-(void)viewWillDisappear:(BOOL)animated
{
    if(UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
        [self.navigationController setToolbarHidden:YES animated:NO];
    }
    [super viewWillDisappear:animated];
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    [self sendView:@"News Detail" forModuleNamed:self.module.name];
}

-(IBAction)share:(id)sender {

        NSURL *url = [NSURL URLWithString:self.itemLink];
        UIActivityViewController *avc = nil;
        if (self.itemLink && url) {
            NSArray *activityItems = [NSArray arrayWithObject:self.itemLink];
            
             avc = [[UIActivityViewController alloc]
                                             initWithActivityItems: activityItems applicationActivities:@[[[SafariActivity alloc] init]]];
            [avc setCompletionHandler:^(NSString *activityType, BOOL completed) {
                NSString *label = [NSString stringWithFormat:@"Tap Share Icon - %@", activityType];
                [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionInvoke_Native withLabel:label withValue:nil forModuleNamed:self.module.name];
                self.popover = nil;
            }];
        } else {
            NSArray *activityItems = [NSArray arrayWithObject:self.itemTitle];
            
            avc = [[UIActivityViewController alloc]
                                             initWithActivityItems: activityItems applicationActivities:nil];
            [avc setCompletionHandler:^(NSString *activityType, BOOL completed) {
                NSString *label = [NSString stringWithFormat:@"Tap Share Icon - %@", activityType];
                [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionInvoke_Native withLabel:label withValue:nil forModuleNamed:self.module.name];
                self.popover = nil;
            }];
        }
    
        if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
            [self presentViewController:avc animated:YES completion:nil];
        }
        else if (self.popover) {
            //The filter picker popover is showing. Hide it.
            [self.popover dismissPopoverAnimated:YES];
            self.popover = nil;
        }
        else {
            self.popover = [[UIPopoverController alloc] initWithContentViewController:avc];
            self.popover.delegate = self;
            self.popover.passthroughViews = nil;
            [self.popover presentPopoverFromBarButtonItem:(UIBarButtonItem *)sender
                                 permittedArrowDirections:UIPopoverArrowDirectionDown animated:YES];
        }

 }

- (void)popoverControllerDidDismissPopover:(UIPopoverController *)popoverController
{
    self.popover = nil;
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    if (self.popover)
    {
        [self.popover dismissPopoverAnimated:YES];
        self.popover = nil;
    }
}

- (void)dismissMasterPopover
{
    if (_masterPopover != nil) {
        [_masterPopover dismissPopoverAnimated:YES];
    }
}

-(void)selectedDetail:(id)newFeed withModule:(Module*)myModule
{
    if ( [newFeed isKindOfClass:[Feed class]] )
    {
        [self setFeed:(Feed *)newFeed];
        [self setModule:myModule];
        
        if (_masterPopover != nil) {
            [_masterPopover dismissPopoverAnimated:YES];
        }
    }
}

-(void)setFeed:(Feed *)feed
{
    if (_feed != feed) {
        _feed = feed;
        
        [self refreshUI];
    }
}

-(void)refreshUI
{
    _titleLabel.text = _feed.title;
    _dateLabel.text = _feed.dateLabel;
    [self setDescriptionText:_feed.description];
    _itemTitle = _feed.title;
    _itemContent = _feed.content;
    _itemLink = [_feed.link stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    _itemPostDateTime = _feed.postDateTime;
    _itemCategory = _feed.category;
    
    if ( _feed.logo ) {
        _itemImageUrl = _feed.logo;
    }
    else {
        _itemImageUrl = nil;
    }
    
    self.titleLabel.text = self.itemTitle;
    
    NSMutableArray *categoryValues = [[NSMutableArray alloc] init];
    for(FeedCategory* value in _itemCategory) {
        [categoryValues addObject:value.name];
    }
    if ( [_itemCategory count] > 0 ) {
        self.categoryValue.text = [NSString stringWithFormat:@"%@: %@", NSLocalizedString(@"Category", "label for the categories"), [categoryValues componentsJoinedByString:@", "]];
    } else {
        self.categoryValue.text = @"";
    }
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
    [dateFormatter setTimeStyle:NSDateFormatterShortStyle];
    self.dateLabel.text = [dateFormatter stringFromDate:self.itemPostDateTime];
    [self setDescriptionText:self.itemContent];
    if(self.itemImageUrl) {
        self.imageHeightConstraint.constant = 120;
        self.imageWidthConstraint.constant = 120;
        self.horizontalSpaceConstraint.constant = 8;
        [self.imageView loadImageFromURLString: self.itemImageUrl];
        
    } else {
        self.imageHeightConstraint.constant = 0;
        self.imageWidthConstraint.constant = 0;
        self.horizontalSpaceConstraint.constant = 0;
    }
    
    [self.view setNeedsDisplay];
    
}

-(void)setDescriptionText:(NSString *) descText
{
    // Create a div that the content will reside in formatting for RTL if necessary.
    NSString  *htmlStringwithFont;
    if ([AppearanceChanger isRTL] ){
        htmlStringwithFont = [NSString stringWithFormat:@"<div style=\"font-family: %@; color:%@;font-size: %i; direction:rtl;\" >%@</div>", kAppearanceChangerWebViewSystemFontName, kAppearanceChangerWebViewSystemFontColor, kAppearanceChangerWebViewSystemFontSize, descText];
    }
    else{
        htmlStringwithFont = [NSString stringWithFormat:@"<div style=\"font-family: %@; color:%@;font-size: %i\">%@</div>", kAppearanceChangerWebViewSystemFontName, kAppearanceChangerWebViewSystemFontColor, kAppearanceChangerWebViewSystemFontSize, descText];
    }
    
    // Replace '\n' characters with <br /> for content that isn't html based to begin with...
    // One issue is if html text also has \n characters in it. In that case we'll be changing the spacing of the content.
    htmlStringwithFont = [htmlStringwithFont stringByReplacingOccurrencesOfString:@"\n" withString:@"<br/>"];
    [self.descriptionWebView loadHTMLString:htmlStringwithFont baseURL:nil];
}


#pragma mark - UISplitViewDelegate methods
-(void)splitViewController:(UISplitViewController *)svc
    willHideViewController:(UIViewController *)aViewController
         withBarButtonItem:(UIBarButtonItem *)barButtonItem
      forPopoverController:(UIPopoverController *)pc
{
    //Grab a reference to the popover
    self.masterPopover = pc;
    
    //Set the title of the bar button item
    //barButtonItem.title = @"University News";
    barButtonItem.title = self.module.name;
    
    //Set the bar button item as the Nav Bar's leftBarButtonItem
    [_navBarItem setLeftBarButtonItem:barButtonItem animated:YES];
}

-(void)splitViewController:(UISplitViewController *)svc
    willShowViewController:(UIViewController *)aViewController
 invalidatingBarButtonItem:(UIBarButtonItem *)barButtonItem
{
    //Remove the barButtonItem.
    [_navBarItem setLeftBarButtonItem:nil animated:YES];
    
    
    //Nil out the pointer to the popover.
    _masterPopover = nil;
}

#pragma mark - UIWebViewDelegate

// Delegate method to launch the URL links in an external browser or app. We don't want to just replace
// the contents of this webview with the destination of the URL being clicked on.
- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType
{
    if (navigationType == UIWebViewNavigationTypeLinkClicked) {
        [[UIApplication sharedApplication] openURL:request.URL];
        return false;
    }
    return true;
}


@end
