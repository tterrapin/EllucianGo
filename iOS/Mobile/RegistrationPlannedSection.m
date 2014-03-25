//
//  RegistrationPlannedSection.m
//  Mobile
//
//  Created by jkh on 11/18/13.
//  Copyright (c) 2013 - 2014 Ellucian. All rights reserved.
//

#import "RegistrationPlannedSection.h"
#import "RegistrationPlannedSectionInstructor.h"
#import "RegistrationPlannedSectionMeetingPattern.h"

@interface RegistrationPlannedSection()
@property (nonatomic, strong) NSDateFormatter *dateFormatter;
@property (nonatomic, strong) NSDateFormatter *timeFormatter;
@property (nonatomic, strong) NSDateFormatter *displayDateFormatter;
@property (nonatomic, strong) NSDateFormatter *displayTimeFormatter;
@end


@implementation RegistrationPlannedSection



-(NSString *) facultyNames
{
    NSMutableArray *names = [NSMutableArray new];
    for(RegistrationPlannedSectionInstructor *instructor in self.instructors) {
        if(instructor.firstName) {
            NSString *name = [NSString stringWithFormat:@"%@, %@", instructor.lastName, [instructor.firstName substringToIndex:1]];
            [names addObject:name];
        } else {
            [names addObject:instructor.lastName];
        }
    }
    if([names count] == 0) return nil;
    return [names componentsJoinedByString:@"; "];
}

-(NSString *)meetingPatternDescription
{
    NSMutableArray *patterns = [NSMutableArray new];
    for(RegistrationPlannedSectionMeetingPattern *mp in self.meetingPatterns) {
        
        NSMutableArray *daysOfClass = [NSMutableArray new];
        NSArray *localizedDays = [self.dateFormatter shortStandaloneWeekdaySymbols];
        NSArray *daysOfWeek = mp.daysOfWeek;
        for(int i = 0; i < [daysOfWeek count]; i++) {
            NSInteger value = [[mp.daysOfWeek objectAtIndex:i] intValue] - 1;
            [daysOfClass addObject:[localizedDays objectAtIndex:value]];
        }

        NSString *line = [NSString stringWithFormat:@"%@: %@ - %@", [daysOfClass componentsJoinedByString:@", "], [self.displayTimeFormatter stringFromDate: mp.startTime], [self.displayTimeFormatter stringFromDate:mp.endTime]];

        [patterns addObject:line];
    }
    if([patterns count] == 0) return nil;
    return [patterns componentsJoinedByString:@"; "];
}

-(NSString *)instructionalMethod
{
    NSMutableSet *types = [NSMutableSet new];
    for(RegistrationPlannedSectionMeetingPattern *mp in self.meetingPatterns) {
        [types addObject:mp.instructionalMethodCode];
    }
    if([types count] == 0) return nil;
    return [[types allObjects] componentsJoinedByString:@", "];

}

-(NSDateFormatter *)dateFormatter
{
    if(_dateFormatter == nil) {
        _dateFormatter = [[NSDateFormatter alloc] init];
        [_dateFormatter setDateFormat:@"yyyy-MM-dd"];
        [_dateFormatter setTimeZone:[NSTimeZone timeZoneWithName:@"UTC"]];
    }
    return _dateFormatter;
}

-(NSDateFormatter *)timeFormatter
{
    if(_timeFormatter == nil) {
        _timeFormatter = [[NSDateFormatter alloc] init];
        [_timeFormatter setDateFormat:@"HH:mm'Z'"];
        [_timeFormatter setTimeZone:[NSTimeZone timeZoneWithName:@"UTC"]];
    }
    return _timeFormatter;
}

-(NSDateFormatter *)displayDateFormatter
{
    if(_displayDateFormatter == nil) {
        _displayDateFormatter = [[NSDateFormatter alloc] init];
        [_displayDateFormatter setDateStyle:NSDateFormatterShortStyle];
        [_displayDateFormatter setTimeStyle:NSDateFormatterNoStyle];
        [_displayDateFormatter setTimeZone:[NSTimeZone timeZoneWithName:@"UTC"]];
    }
    return _displayDateFormatter;
}

-(NSDateFormatter *)displayTimeFormatter
{
    if(_displayTimeFormatter == nil) {
        _displayTimeFormatter = [[NSDateFormatter alloc] init];
        [_displayTimeFormatter setDateStyle:NSDateFormatterNoStyle];
        [_displayTimeFormatter setTimeStyle:NSDateFormatterShortStyle];
    }
    return _displayTimeFormatter;
}

- (void) setGradingType:(NSString *)gradingType
{
    _gradingType = gradingType;
    
    if ([_gradingType caseInsensitiveCompare:kGraded] == NSOrderedSame){
        _isGraded = YES;
    } else if ([_gradingType caseInsensitiveCompare:kAudit] == NSOrderedSame){
        _isAudit = YES;
    } else if ([_gradingType caseInsensitiveCompare:kPassNoPass] == NSOrderedSame){
        _isPassFail = YES;
    }
}

@end
