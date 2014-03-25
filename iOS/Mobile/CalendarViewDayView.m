#import "CalendarViewDayView.h"
#import "CalendarViewEvent.h"
#import <QuartzCore/QuartzCore.h>
#import "AppearanceChanger.h"

static const unsigned int HOURS_IN_DAY                   = 25; // Beginning and end of day is include twice
static const unsigned int MINUTES_IN_HOUR                = 60;
static const unsigned int SPACE_BETWEEN_HOUR_LABELS      = 5;
static const unsigned int DEFAULT_LABEL_FONT_SIZE        = 12;
static const unsigned int ALL_DAY_VIEW_EMPTY_SPACE       = 3;
static const unsigned int MINIMUM_ALL_DAY_HEIGHT         = 60;

static const unsigned int ARROW_LEFT                     = 0;
static const unsigned int ARROW_RIGHT                    = 1;

#define DATE_COMPONENTS (NSYearCalendarUnit| NSMonthCalendarUnit | NSDayCalendarUnit | NSWeekCalendarUnit |  NSHourCalendarUnit | NSMinuteCalendarUnit | NSSecondCalendarUnit | NSWeekdayCalendarUnit | NSWeekdayOrdinalCalendarUnit)
#define CURRENT_CALENDAR [NSCalendar currentCalendar]


@interface CalendarView_AllDayGridView : UIView {
	CalendarViewDayView *_dayView;
	unsigned int _eventCount;
	NSDate *_day;
	CGFloat _eventHeight;
	UIFont *_textFont;
}

@property (nonatomic, assign) CGFloat eventHeight;
@property (nonatomic, strong) CalendarViewDayView *dayView;
@property (nonatomic, strong) UIFont *textFont;
@property (nonatomic,copy) NSDate *day;
@property (readonly) BOOL hasAllDayEvents;

- (void)addEvent:(CalendarViewEvent *)event;
- (void)resetCachedData;

@end

@interface CalendarViewDayGridView : UIView {
	UIColor *_textColor;
	UIFont *_textFont;
	CalendarViewDayView *_dayView;
	CGFloat _lineX;
	CGFloat _lineY[25], _dashedLineY[25];
	CGRect _textRect[25];
}

- (void)addEvent:(CalendarViewEvent *)event;

@property (nonatomic, strong) CalendarViewDayView *dayView;
@property (nonatomic, strong) UIColor *textColor;
@property (nonatomic, strong) UIFont *textFont;
@property (readonly) CGFloat lineX;

@end

@interface CalendarViewDayView (PrivateMethods)
- (void)setupCustomInitialisation;
- (void)changeDay:(UIButton *)sender;
- (NSDate *)nextDayFromDate:(NSDate *)date;
- (NSDate *)previousDayFromDate:(NSDate *)date;

@property (readonly) CalendarView_AllDayGridView *allDayGridView;
@property (readonly) CalendarViewDayGridView *gridView;
@property (readonly) UIFont *regularFont;
@property (readonly) UIFont *boldFont;
@property (readonly) NSString *titleText;
@end

@implementation CalendarViewDayView
@synthesize leftArrow;
@synthesize rightArrow;
@synthesize dateLabel;
@synthesize scrollView;

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
		[self setupCustomInitialisation];
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)decoder {
	if (self = [super initWithCoder:decoder]) {
		[self setupCustomInitialisation];
	}
	return self;
}

- (void)setupCustomInitialisation {
	self.labelFontSize = DEFAULT_LABEL_FONT_SIZE;
    self.leftArrow.layer.borderColor = [UIColor blackColor].CGColor;
    self.leftArrow.layer.borderWidth = 2.0;
	self.day = [NSDate date];
}

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer
{
    if ([otherGestureRecognizer.view isEqual:self.scrollView]) {
        return YES;
    }
    if ([otherGestureRecognizer.view isEqual:self]) {
        return YES;
    }
    return NO;
}

- (void)layoutSubviews {
    [super layoutSubviews];
	self.backgroundView.backgroundColor = self.dateLabelBackgroundColor;
    self.dateLabel.textColor = self.dateLabelTextColor;
    
	[self.scrollView addSubview:self.allDayGridView];
	[self.scrollView addSubview:self.gridView];
	
	self.allDayGridView.frame = CGRectMake(0, 0,
										   CGRectGetWidth(self.bounds),
										   ALL_DAY_VIEW_EMPTY_SPACE);
	self.gridView.frame = CGRectMake(CGRectGetMinX(self.allDayGridView.bounds),
									 CGRectGetMaxY(self.allDayGridView.bounds),
									 CGRectGetWidth(self.bounds),
									 [@"A" sizeWithFont:self.boldFont].height * SPACE_BETWEEN_HOUR_LABELS * HOURS_IN_DAY);
	self.scrollView.contentSize = CGSizeMake(CGRectGetWidth(self.bounds),
											 CGRectGetHeight(self.allDayGridView.bounds) + CGRectGetHeight(self.gridView.bounds));
    
    if([AppearanceChanger isRTL]) {
        [self.leftArrow setImage:[UIImage imageNamed:@"calendarview_rightArrow"] forState:UIControlStateNormal];
        [self.rightArrow setImage:[UIImage imageNamed:@"calendarview_leftArrow"] forState:UIControlStateNormal];
    }
    self.leftArrow.layer.borderColor = [UIColor darkGrayColor].CGColor;
    self.leftArrow.layer.borderWidth = 1.0;
    self.rightArrow.layer.borderColor = [UIColor darkGrayColor].CGColor;
    self.rightArrow.layer.borderWidth = 1.0;
    self.backgroundView.layer.borderColor = [UIColor darkGrayColor].CGColor;
    self.backgroundView.layer.borderWidth = 1.0;
    self.dateLabel.text = [self titleText];
	[self.gridView setNeedsDisplay];
    
}

- (CalendarView_AllDayGridView *)allDayGridView {
	if (!allDayGridView) {
		allDayGridView = [[CalendarView_AllDayGridView alloc] init];
		allDayGridView.backgroundColor = [UIColor whiteColor]; //change for UX?
 		allDayGridView.dayView = self;
		allDayGridView.textFont = self.boldFont;
		allDayGridView.eventHeight = MAX([@"A" sizeWithFont:self.regularFont].height * 2.f, MINIMUM_ALL_DAY_HEIGHT);
	}
	return allDayGridView;
}

- (CalendarViewDayGridView *)gridView {
	if (!gridView){
		gridView = [[CalendarViewDayGridView alloc] init];
		gridView.backgroundColor = [UIColor whiteColor];		gridView.textFont = self.boldFont;
		gridView.textColor = [UIColor blackColor];
		gridView.dayView = self;
	}
	return gridView;
}

- (UIFont *)regularFont {
	if (!regularFont) {
		regularFont = [UIFont systemFontOfSize:self.labelFontSize];
	}
	return regularFont;
}

- (UIFont *)boldFont {
	if (!boldFont) {
		boldFont = [UIFont boldSystemFontOfSize:self.labelFontSize];
	}
	return boldFont;
}

- (void)setDataSource:(id <CalendarViewDayViewDataSource>)newDataSource {
	dataSource = newDataSource;
	[self reloadData];
}

- (id <CalendarViewDayViewDataSource>)dataSource {
	return dataSource;
}

- (void)setDay:(NSDate *)date {
	NSDateComponents *components = [CURRENT_CALENDAR components:DATE_COMPONENTS fromDate:date];
	[components setHour:0];
	[components setMinute:0];
	[components setSecond:0];
	_day = [CURRENT_CALENDAR dateFromComponents:components];
	
	self.allDayGridView.day = self.day;
	self.dateLabel.text = [self titleText];
	[self reloadData];
}

- (void)reloadData {
    for (id view in self.allDayGridView.subviews) {
        if ([NSStringFromClass([view class])isEqualToString:@"CalendarViewDayEventView"]) {
            [view removeFromSuperview];
        }
    }
    
    for (id view in self.gridView.subviews) {
        if ([NSStringFromClass([view class])isEqualToString:@"CalendarViewDayEventView"]) {
            [view removeFromSuperview];
        }
    }
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT,0), ^{
        NSDate *chosenDate = [self.day copy];
        NSArray *events = [self.dataSource dayView:self eventsForDate:self.day];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            
            if([self.day isEqualToDate:chosenDate]) {
                for (id view in self.allDayGridView.subviews) {
                    if ([NSStringFromClass([view class])isEqualToString:@"CalendarViewDayEventView"]) {
                        [view removeFromSuperview];
                    }
                }
                
                for (id view in self.gridView.subviews) {
                    if ([NSStringFromClass([view class])isEqualToString:@"CalendarViewDayEventView"]) {
                        [view removeFromSuperview];
                    }
                }
                
                [self.allDayGridView resetCachedData];
                
                for (id e in events) {
                    CalendarViewEvent *event = e;
                    event.displayDate = self.day;
                }
                
                NSArray *sortedEvents = [events sortedArrayUsingComparator:^(id ev1, id ev2) {
                    CalendarViewEvent *event1 = (CalendarViewEvent *)ev1;
                    CalendarViewEvent *event2 = (CalendarViewEvent *)ev2;
                    
                    NSUInteger v1 = [event1 minutesSinceMidnight];
                    NSUInteger v2 = [event2 minutesSinceMidnight];
                    
                    if (v1 < v2) {
                        return NSOrderedAscending;
                    } else if (v1 > v2) {
                        return NSOrderedDescending;
                    } else {
                        /* Event start time is the same, compare by duration.
                         */
                        NSUInteger d1 = [event1 durationInMinutes];
                        NSUInteger d2 = [event2 durationInMinutes];
                        
                        if (d1 < d2) {
                            /*
                             * Event with a shorter duration is after an event
                             * with a longer duration. Looks nicer when drawing the events.
                             */
                            return NSOrderedDescending;
                        } else if (d1 > d2) {
                            return NSOrderedAscending;
                        } else {
                            /*
                             * The last resort: compare by title.
                             */
                            return (NSInteger)[event1.line1 compare:event2.line1];
                        }
                    }
                    
                }];
                for (id e in sortedEvents) {
                    CalendarViewEvent *event = e;
                    event.displayDate = self.day;
                    
                    if (event.allDay) {
                        [self.allDayGridView addEvent:event];
                    } else {
                        [self.gridView addEvent:event];
                    }
                }
            }
        });
    });
}

- (IBAction)changeDay:(UIButton *)sender {
	if (ARROW_LEFT == sender.tag) {
		self.day = [self previousDayFromDate:_day];
	} else if (ARROW_RIGHT == sender.tag) {
		self.day = [self nextDayFromDate:_day];
	}
    
}

- (void)dateWasSelected:(NSDate *)selectedDate element:(id)element {
    self.day = selectedDate;
}

- (IBAction)showDatePickerForiPad:(id)sender {
    
    UIView *masterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 350, 260)];
    
    CGRect frame = CGRectMake(0, 0, 300, 44);
    
    UIToolbar *pickerToolbar = [[UIToolbar alloc] initWithFrame:frame];
    pickerToolbar.barStyle = UIBarStyleBlackOpaque;
    NSMutableArray *barItems = [[NSMutableArray alloc] init];
    
    UIBarButtonItem *flexSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    [barItems addObject:flexSpace];
    
    UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(actionPickerDone:)];
    [barItems addObject:doneButton];
    [pickerToolbar setItems:barItems animated:YES];
    pickerToolbar.translucent = NO;
    
    [masterView addSubview:pickerToolbar];
    
    CGRect datePickerFrame = CGRectMake(0, 40, 300, 216);
    self.datePicker = [[UIDatePicker alloc] initWithFrame:datePickerFrame];
    self.datePicker.datePickerMode = UIDatePickerModeDate;
    [self.datePicker setDate:self.day animated:NO];
    [masterView addSubview:self.datePicker];
    

    if (self.superview.window != nil) {
        UIViewController* popoverContent = [[UIViewController alloc] init];
            
        popoverContent.view = masterView;
            
        self.popoverController = [[UIPopoverController alloc] initWithContentViewController:popoverContent];
            
        [self.popoverController setPopoverContentSize:CGSizeMake(300, 264) animated:NO];
        [self.popoverController presentPopoverFromRect:_datePickerButton.frame inView:self permittedArrowDirections:UIPopoverArrowDirectionUp animated:YES];
    }
}

- (IBAction)showDatePicker:(id)sender {
    UIView *masterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.frame.size.width, 260)];
    
    CGRect frame = CGRectMake(0, 0, self.frame.size.width, 44);
    UIToolbar *pickerToolbar = [[UIToolbar alloc] initWithFrame:frame];
    pickerToolbar.barStyle = UIBarStyleBlackOpaque;
    NSMutableArray *barItems = [[NSMutableArray alloc] init];
    
    UIBarButtonItem *cancelBtn = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCancel target:self action:@selector(actionPickerCancel:)];
    [barItems addObject:cancelBtn];
    
    UIBarButtonItem *flexSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    [barItems addObject:flexSpace];
    
    UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(actionPickerDone:)];
    [barItems addObject:doneButton];
    [pickerToolbar setItems:barItems animated:YES];
    pickerToolbar.translucent = NO;
    
    [masterView addSubview:pickerToolbar];
    
    CGRect datePickerFrame = CGRectMake(0, 40, self.frame.size.width, 216);
    self.datePicker = [[UIDatePicker alloc] initWithFrame:datePickerFrame];
    self.datePicker.datePickerMode = UIDatePickerModeDate;
    [self.datePicker setDate:self.day animated:NO];
    [masterView addSubview:self.datePicker];
    
    self.actionSheet = [[UIActionSheet alloc] initWithTitle:nil delegate:nil cancelButtonTitle:NSLocalizedString(@"Cancel", @"Cancel") destructiveButtonTitle:nil otherButtonTitles:nil];
    [self.actionSheet setActionSheetStyle:UIActionSheetStyleBlackTranslucent];
    [self.actionSheet addSubview:masterView];
    UITabBarController *tabBarController;
    for (UIView* next = [self superview]; next; next = next.superview)
    {
        UIResponder* nextResponder = [next nextResponder];
            
        if ([nextResponder isKindOfClass:[UITabBarController class]])
        {
            tabBarController = (UITabBarController*)nextResponder;
            break;
        }
    }
        
    [self.actionSheet showFromTabBar:tabBarController.tabBar];
        
    self.actionSheet.bounds = CGRectMake(0, 0, self.frame.size.width,380); //self.frame.size.width, self.frame.size.height);
}

- (IBAction)actionPickerDone:(id)sender {
    [ self.actionSheet dismissWithClickedButtonIndex:0 animated:YES];
    self.day = self.datePicker.date;
    self.datePicker = nil;
    self.actionSheet = nil;
    [self dismissPopover];
}

- (IBAction)actionPickerCancel:(id)sender {
    [ self.actionSheet dismissWithClickedButtonIndex:0 animated:YES];
    self.actionSheet = nil;
    self.datePicker = nil;
}

- (NSDate *)nextDayFromDate:(NSDate *)date {
	NSDateComponents *components = [CURRENT_CALENDAR components:DATE_COMPONENTS fromDate:date];
	[components setDay:[components day] + 1];
	[components setHour:0];
	[components setMinute:0];
	[components setSecond:0];
	return [CURRENT_CALENDAR dateFromComponents:components];
}

- (NSDate *)previousDayFromDate:(NSDate *)date {
	NSDateComponents *components = [CURRENT_CALENDAR components:DATE_COMPONENTS fromDate:date];
	[components setDay:[components day] - 1];
	[components setHour:0];
	[components setMinute:0];
	[components setSecond:0];
	return [CURRENT_CALENDAR dateFromComponents:components];
}

- (NSString *)titleText {
	NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
	[formatter setDateStyle:NSDateFormatterMediumStyle];
	NSDateComponents *components = [CURRENT_CALENDAR components:DATE_COMPONENTS fromDate:_day];
	
	NSArray *weekdaySymbols = [formatter shortWeekdaySymbols];
	
	return [NSString stringWithFormat:@"%@ %@",
			[weekdaySymbols objectAtIndex:[components weekday] - 1], [formatter stringFromDate:_day]];
}

+ (NSArray *) hourLabels
{
    static NSArray *hours;
    if(!hours) {
        NSDateFormatter *timeFormatter = [[NSDateFormatter alloc] init];
        [timeFormatter setDateStyle:NSDateFormatterNoStyle];
        [timeFormatter setTimeStyle:NSDateFormatterShortStyle];
        
        NSMutableArray *dates = [[NSMutableArray alloc] init];
        NSDate *date = [NSDate date];
        NSCalendar *calendar = [NSCalendar autoupdatingCurrentCalendar];
        NSUInteger preservedComponents = (NSYearCalendarUnit | NSMonthCalendarUnit | NSDayCalendarUnit);
        date = [calendar dateFromComponents:[calendar components:preservedComponents fromDate:date]];
        register unsigned int i;
        
        for (i=0; i < HOURS_IN_DAY; i++) {
            [dates addObject:[timeFormatter stringFromDate:date]];
            date = [date dateByAddingTimeInterval:60*60]; 
        }
        hours = [dates copy];
    }
    return hours;
}
- (void)popoverControllerDidDismissPopover:(UIPopoverController *)popoverController
{
    [self dismissPopover];
}

- (void)dismissPopover
{
    if ( self.popoverController )
    {
        [self.popoverController dismissPopoverAnimated:YES];
        self.popoverController = nil;
    }
}


@end

@implementation CalendarView_AllDayGridView

@synthesize dayView=_dayView;
@synthesize eventHeight=_eventHeight;
@synthesize textFont=_textFont;

- (BOOL)hasAllDayEvents {
	for (id view in self.subviews) {
		if ([NSStringFromClass([view class])isEqualToString:@"CalendarViewDayEventView"]) {
			return YES;
		}
	}
	return NO;
}

- (void)resetCachedData {
	_eventCount = 0;
}

- (void)setDay:(NSDate *)day {
	[self resetCachedData];
	
	_day = [day copy];
	
	[self setNeedsLayout];
	[self.dayView.gridView setNeedsLayout];
}

- (NSDate *)day {
	return _day;
}

- (void)layoutSubviews {
    [super layoutSubviews];
	self.frame = CGRectMake(self.frame.origin.x,
							self.frame.origin.y,
							self.frame.size.width,
							ALL_DAY_VIEW_EMPTY_SPACE + (ALL_DAY_VIEW_EMPTY_SPACE + self.eventHeight) * _eventCount);
	
	self.dayView.gridView.frame =  CGRectMake(self.dayView.gridView.frame.origin.x, self.frame.size.height,
											  self.dayView.gridView.frame.size.width, self.dayView.gridView.frame.size.height);
	
	self.dayView.scrollView.contentSize = CGSizeMake(self.dayView.scrollView.contentSize.width,
													 CGRectGetHeight(self.bounds) + CGRectGetHeight(self.dayView.gridView.bounds));
	
	for (id view in self.subviews) {
		if ([NSStringFromClass([view class])isEqualToString:@"CalendarViewDayEventView"]) {
			CalendarViewDayEventView *ev = view;
			
			CGFloat x = (int)self.dayView.gridView.lineX,
            y = (int)ev.frame.origin.y,
            w = (int)((self.frame.size.width - self.dayView.gridView.lineX) * 0.99),
            h = (int)ev.frame.size.height;
			
			ev.frame = CGRectMake(x, y, w, h);
			[ev setNeedsDisplay];
		}
	}
}

- (void)addEvent:(CalendarViewEvent *)event {
	CalendarViewDayEventView *eventView = [[CalendarViewDayEventView alloc] initWithFrame: CGRectMake(0, ALL_DAY_VIEW_EMPTY_SPACE + (ALL_DAY_VIEW_EMPTY_SPACE + self.eventHeight) * _eventCount,
                                                                                                      self.bounds.size.width, self.eventHeight)];
	eventView.dayView = self.dayView;
	eventView.event = event;
	eventView.label1.text = event.line1;
    eventView.label2.text = event.line2;
    eventView.label3.text = event.line3;
    
	
	[self addSubview:eventView];
	
	_eventCount++;
	
	[self setNeedsLayout];
	[self.dayView.gridView setNeedsLayout];
}

@end

@implementation CalendarViewDayGridView

@synthesize dayView=_dayView;
@synthesize textColor=_textColor;
@synthesize textFont=_textFont;

- (CGFloat)lineX
{
	return _lineX;
}

- (void)addEvent:(CalendarViewEvent *)event {
	CalendarViewDayEventView *eventView = [[CalendarViewDayEventView alloc] initWithFrame:CGRectZero];
	eventView.dayView = self.dayView;
	eventView.event = event;
	eventView.label1.text = event.line1;
    eventView.label2.text = event.line2;
    eventView.label3.text = event.line3;
	
	[self addSubview:eventView];
	
	[self setNeedsLayout];
}

- (void)layoutSubviews {
    [super layoutSubviews];
	CGFloat maxTextWidth = 0, totalTextHeight = 0;
	CGSize hourSize[25];

	register unsigned int i;
	
	for (i=0; i < HOURS_IN_DAY; i++) {
		hourSize[i] = [[[CalendarViewDayView hourLabels] objectAtIndex:i ] sizeWithFont:self.textFont];
		totalTextHeight += hourSize[i].height;
		
		if (hourSize[i].width > maxTextWidth) {
			maxTextWidth = hourSize[i].width;
		}
	}
	
	CGFloat y;
	const CGFloat spaceBetweenHours = (self.bounds.size.height - totalTextHeight) / (HOURS_IN_DAY - 1);
	CGFloat rowY = 0;
	
	for (i=0; i < HOURS_IN_DAY; i++) {
		_textRect[i] = CGRectMake(CGRectGetMinX(self.bounds),
								  rowY,
								  maxTextWidth,
								  hourSize[i].height);
		
		y = rowY + ((CGRectGetMaxY(_textRect[i]) - CGRectGetMinY(_textRect[i])) / 2.f);
		_lineY[i] = y;
		_dashedLineY[i] = CGRectGetMaxY(_textRect[i]) + (spaceBetweenHours / 2.f);
		
		rowY += hourSize[i].height + spaceBetweenHours;
	}
	
	_lineX = maxTextWidth + (maxTextWidth * 0.3);
	
	NSArray *subviews = self.subviews;
	NSUInteger max = [subviews count];
	CalendarViewDayEventView *curEv = nil, *prevEv = nil, *firstEvent = nil;
	const CGFloat spacePerMinute = (_lineY[1] - _lineY[0]) / 60.f;
	
	for (i=0; i < max; i++) {
		if (![NSStringFromClass([[subviews objectAtIndex:i] class])isEqualToString:@"CalendarViewDayEventView"]) {
			continue;
		}
		
		prevEv = curEv;
		curEv = [subviews objectAtIndex:i];
        
		curEv.frame = CGRectMake((int) _lineX,
								 (int) (spacePerMinute * [curEv.event minutesSinceMidnight] + _lineY[0] + 1), //for 1px padding
								 (int) (self.bounds.size.width - _lineX),
								 (int) ((spacePerMinute * [curEv.event durationInMinutes])-1));
		
		/*
		 * Layout intersecting events to two columns.
		 */
		if (CGRectIntersectsRect(curEv.frame, prevEv.frame))
		{
			prevEv.frame = CGRectMake((int) (prevEv.frame.origin.x),
									  (int) (prevEv.frame.origin.y),
									  (int) (prevEv.frame.size.width / 2.f),
									  (int) (prevEv.frame.size.height));
            
			curEv.frame = CGRectMake((int) (curEv.frame.origin.x + (curEv.frame.size.width / 2.f)),
									 (int) (curEv.frame.origin.y),
									 (int) (curEv.frame.size.width / 2.f),
									 (int) (curEv.frame.size.height));
		}
        
        if(curEv.frame.size.height < 52) {
            curEv.label3.text = @"";
            if(curEv.frame.size.height < 37) {
                curEv.label2.text = @"";
                if(curEv.frame.size.height < 22) {
                    curEv.label1.text = @"";
                }
            }
        }
		
		[curEv setNeedsDisplay];
		
		if (!firstEvent || curEv.frame.origin.y < firstEvent.frame.origin.y) {
			firstEvent = curEv;
		}
	}
	
	if (self.dayView.autoScrollToFirstEvent) {
        CGPoint autoScrollPoint;
		if (!firstEvent || self.dayView.allDayGridView.hasAllDayEvents) {
			autoScrollPoint = CGPointMake(0, 0);
		} else {
			int minutesSinceLastHour = ([firstEvent.event minutesSinceMidnight] % 60);
			CGFloat padding = minutesSinceLastHour * spacePerMinute + 7.5;
			
			autoScrollPoint = CGPointMake(0, firstEvent.frame.origin.y - padding);
			CGFloat maxY = self.dayView.scrollView.contentSize.height - CGRectGetHeight(self.dayView.scrollView.bounds);
			
			if (autoScrollPoint.y > maxY) {
				autoScrollPoint.y = maxY;
			}
		}
		
		[self.dayView.scrollView setContentOffset:autoScrollPoint animated:YES];
	}
}

- (void)drawRect:(CGRect)rect {
	register unsigned int i;
	
	const CGContextRef c = UIGraphicsGetCurrentContext();
    
	CGContextSetStrokeColorWithColor(c, [[UIColor lightGrayColor] CGColor]);
	CGContextSetLineWidth(c, 0.5);
	CGContextBeginPath(c);
	
	for (i=0; i < HOURS_IN_DAY; i++) {
		[[[CalendarViewDayView hourLabels] objectAtIndex:i ] drawInRect: _textRect[i]
					withFont:self.textFont
			   lineBreakMode:NSLineBreakByTruncatingTail
				   alignment:NSTextAlignmentRight];
		
		CGContextMoveToPoint(c, _lineX, _lineY[i]);
		CGContextAddLineToPoint(c, self.bounds.size.width, _lineY[i]);
	}
	
	CGContextClosePath(c);
	CGContextSaveGState(c);
	CGContextDrawPath(c, kCGPathFillStroke);
	CGContextRestoreGState(c);
	
	CGContextSetLineWidth(c, 0.5);
	CGFloat dash1[] = {2.0, 1.0};
	CGContextSetLineDash(c, 0.0, dash1, 2);
	
	CGContextBeginPath(c);
    
	for (i=0; i < (HOURS_IN_DAY - 1); i++) {
		CGContextMoveToPoint(c, _lineX, _dashedLineY[i]);
		CGContextAddLineToPoint(c, self.bounds.size.width, _dashedLineY[i]);
	}
	
	CGContextClosePath(c);
	CGContextSaveGState(c);
	CGContextDrawPath(c, kCGPathFillStroke);
	CGContextRestoreGState(c);
}


@end
