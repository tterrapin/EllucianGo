#import "CalendarViewEvent.h"

#define DATE_COMPONENTS (NSYearCalendarUnit| NSMonthCalendarUnit | NSDayCalendarUnit | NSWeekCalendarUnit |  NSHourCalendarUnit | NSMinuteCalendarUnit | NSSecondCalendarUnit | NSWeekdayCalendarUnit | NSWeekdayOrdinalCalendarUnit)
#define CURRENT_CALENDAR [NSCalendar currentCalendar]

static const NSUInteger MINUTES_IN_HOUR = 60;
static const NSUInteger DAY_IN_MINUTES = 1440;

@implementation CalendarViewEvent

#define DATE_CMP(X, Y) ([X year] == [Y year] && [X month] == [Y month] && [X day] == [Y day])

- (NSUInteger)minutesSinceMidnight {
	NSUInteger fromMidnight = 0;
	
	NSDateComponents *displayComponents = [CURRENT_CALENDAR components:DATE_COMPONENTS fromDate:_displayDate];
	NSDateComponents *startComponents = [CURRENT_CALENDAR components:DATE_COMPONENTS fromDate:_start];
	
	if (DATE_CMP(startComponents, displayComponents)) {
		fromMidnight = [startComponents hour] * MINUTES_IN_HOUR + [startComponents minute];
	}

	return fromMidnight;
}

- (NSUInteger)durationInMinutes {
	NSUInteger duration = 0;
	
	NSDateComponents *displayComponents = [CURRENT_CALENDAR components:DATE_COMPONENTS fromDate:_displayDate];
	NSDateComponents *startComponents = [CURRENT_CALENDAR components:DATE_COMPONENTS fromDate:_start];
	NSDateComponents *endComponents = [CURRENT_CALENDAR components:DATE_COMPONENTS fromDate:_end];
	
	if (DATE_CMP(endComponents, displayComponents)) {
		if (DATE_CMP(startComponents, displayComponents)) {
			duration = (int) (([_end timeIntervalSince1970] - [_start timeIntervalSince1970]) / (double) MINUTES_IN_HOUR);
		} else {
			duration = [endComponents hour] * MINUTES_IN_HOUR + [endComponents minute];
		}
	} else {
		duration = DAY_IN_MINUTES - [self minutesSinceMidnight];
	}
	return duration;
}

#undef DATE_CMP


@end