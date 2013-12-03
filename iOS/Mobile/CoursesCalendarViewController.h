#import <UIKit/UIKit.h>
#import "CalendarViewDayView.h"
#import "CourseTerm.h"
#import "Module+Attributes.h"
#import "CourseDetailViewController.h"
#import "MapPOI.h"

@interface CoursesCalendarViewController : UIViewController<CalendarViewDayViewDataSource,CalendarViewDayViewDelegate,
UIPickerViewDelegate> {

}

@property (strong, nonatomic) Module *module;

@end
