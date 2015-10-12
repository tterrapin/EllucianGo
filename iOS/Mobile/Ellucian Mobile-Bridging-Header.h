//
//  Use this file to import your target's public headers that you would like to expose to Swift.
//

#import <CommonCrypto/CommonCrypto.h>

// Model objects
#import "Module.h"
#import "Module+Attributes.h"
#import "CourseAssignment.h"
#import "CourseAnnouncement.h"
#import "CourseEvent.h"
#import "Event.h"
#import "EventModule.h"
#import "EventCategory.h"
#import "Feed.h"
#import "FeedModule.h"
#import "FeedCategory.h"
#import "GradeCourse.h"
#import "GradeTerm.h"
#import "Grade.h"

//UI Customizations - Must stay in objc in iOS 8 since using varargs function
#import "AppearanceChanger.h"

//Login
#import "AuthenticatedRequest.h"
#import "CurrentUser.h"

//Ellucian views
#import "AsynchronousImageView.h"
#import "CopyLabel.h"
#import "DetailSelectionDelegate.h"
#import "EllucianSectionedUITableViewController.h"
#import "EllucianUITableViewController.h"
#import "PseudoButtonView.h"

//Analytics
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"

//Views
#import "MBProgressHUD.h"


#import "EmptyTableViewCell.h"

#import "UIViewController+GoogleAnalyticsTrackerSupport.h"
#import "CourseAssignmentDetailViewController.h"
#import "CourseEventsDetailViewController.h"
#import "CourseAnnouncementDetailViewController.h"

//Web
#import "WebViewController.h"

#import "AppDelegate.h"
#import "ImageCache.h"
#import "HomeViewController.h"
#import "AboutViewController.h"
#import "NotificationsFetcher.h"
#import "LoginExecutor.h"
#import "ConfigurationFetcher.h"
#import "ModuleRole.h"
#import "LoginProtocol.h"
#import "NSString+HTML.h"
#import "VersionChecker.h"
#import "NSMutableURLRequest+BasicAuthentication.h"

//Menu
#import "ECSlidingViewController.h"
#import "ECSlidingSegue.h"
#import "ECSlidingConstants.h"

//Maps
#import "Map.h"
#import "MapCampus.h"
#import "MapPinAnnotation.h"
#import "MapPOI.h"
#import "MapPOIType.h"
#import "MapsFetcher.h"
