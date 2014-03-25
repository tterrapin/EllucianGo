//
//  CalendarViewDayEventView
//  Mobile
//
//  Created by Jason Hocker on 9/18/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "CalendarViewDayEventView.h"
#import <QuartzCore/QuartzCore.h>

static const CGFloat kAlpha        = 0.8;
static const CGFloat kCornerRadius = 6.0;

@implementation CalendarViewDayEventView
@dynamic backgroundColor;

- (IBAction)eventTapped:(UIGestureRecognizer *)sender {
    if ([self.dayView.delegate respondsToSelector:@selector(dayView:eventTapped:)]) {
        [self.dayView.delegate dayView:self.dayView eventTapped:self.event];
    }
}

- (id)initWithFrame:(CGRect)frame {
    #ifndef __clang_analyzer__
    if (self = [super initWithFrame:frame]) {
        NSArray *theView =  [[NSBundle mainBundle] loadNibNamed:@"CalendarViewDayEventView" owner:self options:nil];
        UIView *nv = [theView objectAtIndex:0];
        self = (CalendarViewDayEventView *)nv;
        
        UITapGestureRecognizer *tap =
        [[UITapGestureRecognizer alloc] initWithTarget: self
                                                action: @selector(eventTapped:)];
        [self addGestureRecognizer: tap];
        
        self.alpha = kAlpha;
        CALayer *layer = [self layer];
        layer.masksToBounds = YES;
        [layer setCornerRadius:kCornerRadius];
    }
    #endif
    return self;
}

- (id)initWithCoder:(NSCoder *)decoder {
	if (self = [super initWithCoder:decoder]) {
	}
	return self;
}

- (void)setBackgroundColor:(UIColor *)backgroundColor
{
    [super setBackgroundColor:backgroundColor];
}

- (UIColor *)backgroundColor
{
    return [super backgroundColor];
}

- (void)setFontColor:(UIColor *)fontColor
{
    self.label1.textColor = fontColor;
    self.label2.textColor = fontColor;
    self.label3.textColor = fontColor;
}




@end