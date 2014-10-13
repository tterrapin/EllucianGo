//
//  CalendarActionSheetDatePicker.h
//  Mobile
//
//  Created by Jason Hocker on 9/8/14.
//  Copyright (c) 2014 Ellucian. All rights reserved.
//

@interface CalendarActionSheetDatePicker : NSObject<UIPopoverControllerDelegate>

- (id)initWithDate:(NSDate *)selectedDate target:(id)target action:(SEL)action origin:(id)origin;

- (void)showActionSheetPicker;

@end
