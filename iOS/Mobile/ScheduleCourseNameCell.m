//
//  ScheduleCourseNameCell.m
//  Mobile
//
//  Created by Jason Hocker on 9/25/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "ScheduleCourseNameCell.h"

@implementation ScheduleCourseNameCell
@synthesize courseName;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];
    
    // Configure the view for the selected state
}

@end
