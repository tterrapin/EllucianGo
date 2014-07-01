//
//  MenuTableViewHeaderFooterView.m
//  Mobile
//
//  Created by Jason Hocker on 4/4/14.
//  Copyright (c) 2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import "MenuTableViewHeaderFooterView.h"
#import "AppearanceChanger.h"

@implementation MenuTableViewHeaderFooterView

- (id)initWithReuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithReuseIdentifier:reuseIdentifier];
    if (self)
    {
        BOOL collapseable = [reuseIdentifier isEqualToString:@"CollapseableHeader"];
        UIColor *backgroundColor = [UIColor blackColor];
        if (![self.backgroundColor isEqual: backgroundColor]) {
            
            self.contentView.backgroundColor = backgroundColor;
            self.contentView.opaque = YES;
            
            UILabel* headerLabel = [UILabel new];
            _headerLabel = headerLabel;
            headerLabel.font = [UIFont boldSystemFontOfSize:14.0];
            headerLabel.textColor = [UIColor colorWithRed:179.0f/255.0f green:179.0f/255.0f blue:179.0f/255.0f alpha:1.0f];
            headerLabel.backgroundColor = [UIColor clearColor];
            headerLabel.opaque = NO;
            [self.contentView addSubview:headerLabel];
            headerLabel.translatesAutoresizingMaskIntoConstraints = NO;
            
            UIButton *collapsibleButton = nil;
            if(collapseable) {
                collapsibleButton = [UIButton new];
                _collapsibleButton = collapsibleButton;
                [collapsibleButton setImage:[UIImage imageNamed:@"menu header expanded"] forState:UIControlStateNormal];
                [collapsibleButton setImage:[UIImage imageNamed:@"menu header collapsed"] forState:UIControlStateSelected];
                [collapsibleButton addTarget:self
                                      action:@selector(toggleHeader:)forControlEvents:UIControlEventTouchUpInside];
                [self.contentView addSubview:collapsibleButton];
                collapsibleButton.translatesAutoresizingMaskIntoConstraints = NO;
            }
            
            NSDictionary *viewBindings = NSDictionaryOfVariableBindings(headerLabel);
            if([AppearanceChanger isRTL]) {
                if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
                    [self.contentView addConstraint:[NSLayoutConstraint constraintWithItem:headerLabel attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:self.contentView attribute:NSLayoutAttributeLeft multiplier:1.0 constant:307.0]];
                } else {
                    [self.contentView addConstraint:[NSLayoutConstraint constraintWithItem:headerLabel attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:self.contentView attribute:NSLayoutAttributeLeft multiplier:1.0 constant:267.0]];
                }
                if(collapseable) {
                    [self.contentView addConstraints:
                     [NSLayoutConstraint constraintsWithVisualFormat:@"H:[collapsibleButton]-13-|"
                                                             options:0 metrics:nil
                                                               views:@{@"collapsibleButton":collapsibleButton}]];
                }
            } else {
                [self.contentView addConstraints:
                 [NSLayoutConstraint constraintsWithVisualFormat:@"H:|-13-[headerLabel]"
                                                         options:0 metrics:nil
                                                           views:viewBindings]];
                if(collapseable) {
                    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
                        [self.contentView addConstraint:[NSLayoutConstraint constraintWithItem:collapsibleButton attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:self.contentView attribute:NSLayoutAttributeLeft multiplier:1.0 constant:307.0]];
                    } else {
                        [self.contentView addConstraint:[NSLayoutConstraint constraintWithItem:collapsibleButton attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:self.contentView attribute:NSLayoutAttributeLeft multiplier:1.0 constant:267.0]];
                    }
                }
            }
            
            [self.contentView addConstraints:
             [NSLayoutConstraint constraintsWithVisualFormat:@"V:|[headerLabel]|"
                                                     options:0 metrics:nil
                                                       views:viewBindings]];
            if(collapseable) {
                
                [self.contentView addConstraint: [NSLayoutConstraint constraintWithItem:collapsibleButton
                                                                              attribute:NSLayoutAttributeCenterY
                                                                              relatedBy:NSLayoutRelationEqual
                                                                                 toItem:headerLabel
                                                                              attribute:NSLayoutAttributeCenterY
                                                                             multiplier:1.0
                                                                               constant:0]];
            }
        }
        
        // set up the tap gesture recognizer
        if(collapseable) {
            UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self
                                                                                         action:@selector(toggleHeader:)];
            [self addGestureRecognizer:tapGesture];
        }
    }
    return self;
}

- (IBAction)toggleHeader:(id)sender {
    
    [self toggleHeaderWithUserAction:YES];
}

- (void)toggleHeaderWithUserAction:(BOOL)userAction {
    
    // toggle the disclosure button state
    self.collapsibleButton.selected = !self.collapsibleButton.selected;
    
    // if this was a user action, send the delegate the appropriate message
    if (userAction) {
        if (self.collapsibleButton.selected) {
            if ([self.delegate respondsToSelector:@selector(sectionHeaderView:sectionClosed:)]) {
                [self.delegate sectionHeaderView:self sectionClosed:self.section];
            }
        }
        else {
            if ([self.delegate respondsToSelector:@selector(sectionHeaderView:sectionOpened:)]) {
                [self.delegate sectionHeaderView:self sectionOpened:self.section];
            }
        }
    }
}

@end
