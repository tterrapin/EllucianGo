//
//  MenuTableViewHeaderFooterView.h
//  Mobile
//
//  Created by Jason Hocker on 4/4/14.
//  Copyright (c) 2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol SectionHeaderViewDelegate;

@interface MenuTableViewHeaderFooterView : UITableViewHeaderFooterView

@property (nonatomic, weak) UILabel *headerLabel;
@property (nonatomic, weak) IBOutlet UIButton *collapsibleButton;
@property (nonatomic, weak) IBOutlet id <SectionHeaderViewDelegate> delegate;
@property (getter = isCollapseable) BOOL collapseable;
@property (nonatomic) NSInteger section;

@end

#pragma mark -

/*
 Protocol to be adopted by the section header's delegate; the section header tells its delegate when the section should be opened and closed.
 */
@protocol SectionHeaderViewDelegate <NSObject>

@optional
- (void)sectionHeaderView:(MenuTableViewHeaderFooterView *)sectionHeaderView sectionOpened:(NSInteger)section;
- (void)sectionHeaderView:(MenuTableViewHeaderFooterView *)sectionHeaderView sectionClosed:(NSInteger)section;

@end

