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

- (BOOL)isEqual:(id)other {
    if (other == self)
        return YES;
    if (!other || ![other isKindOfClass:[self class]])
        return NO;
    return [self isEqualToCalendarViewEvent:other];
}

- (BOOL)isEqualToCalendarViewEvent:(CalendarViewEvent *)event {
    if (self == event)
        return YES;
    
    if (![self.line1 isEqual:[event line1]])
        return NO;
    if (![self.line2 isEqual:[event line2]])
        return NO;
    if (![self.line3 isEqual:[event line3]])
        return NO;
    if (![self.start isEqualToDate:[event start]])
        return NO;
    if (![self.end isEqualToDate:[event end]])
        return NO;
    if (self.allDay != [event allDay])
        return NO;
    if(![self.userInfo isEqualToDictionary:[event userInfo]])
        return NO;

    return YES;
}

- (NSUInteger)hash {
    return [self.line1 hash];
}

-(NSString *)description
{
	return [NSString stringWithFormat:@"<%@: %p, %@, %@, %@>",
            NSStringFromClass([self class]), self, self.line1, self.line2, self.line3];
}

#undef DATE_CMP


@end