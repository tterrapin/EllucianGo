//
//  GradesCell.h
//  Mobile
//
//  Created by Jason Hocker on 8/2/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface GradesCell : UITableViewCell
@property (weak, nonatomic) IBOutlet UILabel *gradeTypeLabel;
@property (weak, nonatomic) IBOutlet UILabel *gradeValueLabel;
@property (weak, nonatomic) IBOutlet UILabel *gradeLastUpdated;

@end
