//
//  Notification.h
//  Mobile
//
//  Created by jkh on 1/24/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface Notification : NSManagedObject

@property (nonatomic, retain) NSDate * noticeDate;
@property (nonatomic, retain) NSString * notificationDescription;
@property (nonatomic, retain) NSString * hyperlink;
@property (nonatomic, retain) NSString * linkLabel;
@property (nonatomic, retain) NSString * title;
@property (nonatomic, retain) NSString * notificationId;

@end
