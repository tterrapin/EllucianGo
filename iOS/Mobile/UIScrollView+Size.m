//
//  UIScrollView+Size.m
//  Mobile
//
//  Created by jkh on 10/9/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import "UIScrollView+Size.h"

@implementation UIScrollView (Size)

-(CGFloat) heightOfContent
{
    self.scrollEnabled = NO;
    CGFloat fixedWidth = self.frame.size.width;
    CGSize newSize = [self sizeThatFits:CGSizeMake(fixedWidth, MAXFLOAT)];
    CGRect newFrame = self.frame;
    newFrame.size = CGSizeMake(fmaxf(newSize.width, fixedWidth), newSize.height);
    return newFrame.size.height;
}

@end
