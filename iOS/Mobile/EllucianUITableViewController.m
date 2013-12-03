//
//  EllucianUITableViewController.m
//  Mobile
//
//  Created by Jason Hocker on 12/12/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "EllucianUITableViewController.h"

@interface EllucianUITableViewController ()

@property (strong, nonatomic) UILabel *noMatchesLabel;
@property (strong, nonatomic) NSString *message;
@end

@implementation EllucianUITableViewController

-(void) viewDidLoad
{
    [super viewDidLoad];
    
    [self.tableView registerClass:[UITableViewHeaderFooterView class] forHeaderFooterViewReuseIdentifier:@"Header"];
}

-(UIView *) noDataView
{
    if(_noDataView == nil) {
        _noDataView = [[UIView alloc] initWithFrame:self.view.frame];
        _noDataView.backgroundColor = [UIColor clearColor];
        
        self.noMatchesLabel = [[UILabel alloc] initWithFrame:CGRectMake(0,0,self.view.frame.size.width,40)];
        self.noMatchesLabel.font = [UIFont boldSystemFontOfSize:18];
        self.noMatchesLabel.minimumScaleFactor = .5f;
        self.noMatchesLabel.numberOfLines = 1;
        self.noMatchesLabel.lineBreakMode = NSLineBreakByWordWrapping;
        self.noMatchesLabel.shadowColor = [UIColor lightTextColor];
        self.noMatchesLabel.textColor = [UIColor darkGrayColor];
        self.noMatchesLabel.shadowOffset = CGSizeMake(0, 1);
        self.noMatchesLabel.backgroundColor = [UIColor whiteColor];
        self.noMatchesLabel.textAlignment =  NSTextAlignmentCenter;
        if(self.message) {
            self.noMatchesLabel.text = self.message;
        } else {
            self.noMatchesLabel.text = NSLocalizedString(@"No Data", @"generic message for no data present");
        }
        _noDataView.hidden = YES;
        [_noDataView addSubview:self.noMatchesLabel];
        [self.tableView insertSubview:_noDataView belowSubview:self.tableView];
    }
    return _noDataView;
}

-(void) showNoDataView:(NSString *) message
{
    self.noDataView.hidden = NO;
    
    if(message) {
        self.message = message;
        self.noMatchesLabel.text = message;
    }
}
-(void) hideNoDataView
{
    self.noDataView.hidden = YES;
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    if(self.noDataView && self.noDataView.hidden == NO) {
        [_noDataView removeFromSuperview];
        _noDataView = nil;
        self.noDataView.hidden = NO;
    }
}

@end
