
#import <UIKit/UIKit.h>
#import "CalendarViewDayEventView.h"
#import "CalendarActionSheetDatePicker.h"

@class CalendarView_AllDayGridView;
@class CalendarViewDayHourView;
@class CalendarViewDayGridView;
@class CalendarViewEvent;

@protocol CalendarViewDayViewDataSource, CalendarViewDayViewDelegate;

@interface CalendarViewDayView : UIView <UIActionSheetDelegate, UIGestureRecognizerDelegate, UIPopoverControllerDelegate>{
	CalendarView_AllDayGridView *allDayGridView;
	CalendarViewDayHourView *hourView;
	CalendarViewDayGridView *gridView;
	
	UIFont *regularFont;
	UIFont *boldFont;

	id<CalendarViewDayViewDataSource> dataSource;
    

}

@property (nonatomic,assign) BOOL autoScrollToFirstEvent;
@property (readwrite,assign) unsigned int labelFontSize;
@property (nonatomic,strong) NSDate *day;
@property (nonatomic,unsafe_unretained) IBOutlet id<CalendarViewDayViewDataSource> dataSource;
@property (nonatomic,unsafe_unretained) IBOutlet id<CalendarViewDayViewDelegate> delegate;
@property (weak, nonatomic) IBOutlet UIImageView *topBackground;
@property (weak, nonatomic) IBOutlet UIButton *leftArrow;
@property (weak, nonatomic) IBOutlet UIButton *rightArrow;
@property (weak, nonatomic) IBOutlet UIButton *datePickerButton;
@property (weak, nonatomic) IBOutlet UILabel *dateLabel;
@property (weak, nonatomic) IBOutlet UIScrollView *scrollView;
@property (weak, nonatomic) IBOutlet UIView *backgroundView;
@property (nonatomic, retain) UIColor *dateLabelBackgroundColor UI_APPEARANCE_SELECTOR;
@property (nonatomic, retain) UIColor *dateLabelTextColor UI_APPEARANCE_SELECTOR;
@property (nonatomic, strong) CalendarActionSheetDatePicker *datePicker;

- (void)reloadData;
- (IBAction)changeDay:(id)sender;
- (IBAction)showDatePicker:(id)sender;
- (IBAction)showDatePickerForiPad:(id)sender;
+ (NSArray *) hourLabels;

@end

@protocol CalendarViewDayViewDataSource <NSObject>

- (NSArray *)dayView:(CalendarViewDayView *)dayView eventsForDate:(NSDate *)date;

@end

@protocol CalendarViewDayViewDelegate <NSObject>

@optional
- (void)dayView:(CalendarViewDayView *)dayView eventTapped:(CalendarViewEvent *)event;

@end