//
//  GradesPageSelectionViewController.m
//  Mobile
//
//  Created by Jason Hocker on 9/27/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "GradesPageSelectionViewController.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"

@interface GradesPageSelectionViewController ()

@end

@implementation GradesPageSelectionViewController

-(void) viewDidLoad
{
    [super viewDidLoad];
    self.navigationController.navigationBar.translucent = NO;    
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];

    [self sendView:@"Term List" forModuleNamed:self.module.name];
}

#pragma mark - Table view data source

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.terms count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"Grades Term Selection Cell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    
    GradeTerm *term = [self.terms objectAtIndex:[indexPath row]];
    cell.textLabel.text = term.name;
    
    return cell;
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{

    //Is anyone listening
    if([self.gradesChangePageDelegate respondsToSelector:@selector(changeToPageNumber:)])
    {
        //send the delegate function with the amount entered by the user
        [self.gradesChangePageDelegate changeToPageNumber:[indexPath row]];
    }
        
    [self dismissViewControllerAnimated:YES completion:nil];
    
    if (_delegate)
    {
        [_delegate dismissPopover];
    }

}

- (IBAction)dismiss:(id)sender {
    [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionButton_Press withLabel:@"Cancel Term List" withValue:nil forModuleNamed:self.module.name];
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)popoverControllerDidDismissPopover:(UIPopoverController *)popoverController
{
    if (_delegate)
    {
        [_delegate resetPopover];
    }
}

-(void)sizeForPopover
{
    //Make row selections persist.
    self.clearsSelectionOnViewWillAppear = NO;
    
    NSInteger rowsCount = [_terms count];
    NSInteger singleRowHeight = [self.tableView.delegate tableView:self.tableView
                                           heightForRowAtIndexPath:[NSIndexPath indexPathForRow:0 inSection:0]];
    NSInteger totalRowsHeight = rowsCount * singleRowHeight;
    
    //Calculate how wide the view should be by finding how
    //wide each string is expected to be
    CGFloat largestLabelWidth = 200;
    for (GradeTerm *term in _terms) {
        //Checks size of text using the default font for UITableViewCell's textLabel.
        CGSize labelSize = [term.name sizeWithFont:[UIFont boldSystemFontOfSize:20.0f]];
        if (labelSize.width > largestLabelWidth) {
            largestLabelWidth = labelSize.width;
        }
    }
    
    //Add a little padding to the width
    CGFloat popoverWidth = largestLabelWidth + 100;
    
    //Set the property to tell the popover container how big this view will be.
    self.contentSizeForViewInPopover = CGSizeMake(popoverWidth, totalRowsHeight);
}


@end
