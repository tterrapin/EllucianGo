//
//  Notification.h
//  Mobile
//
//  Created by Jason Hocker on 2/17/14.
//  Copyright (c) 2014 Ellucian Company L.P. and its affiliates. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface Notification : NSManagedObject

@property (nonatomic, retain) NSString * hyperlink;
@property (nonatomic, retain) NSString * linkLabel;
@property (nonatomic, retain) NSDate * noticeDate;
@property (nonatomic, retain) NSString * notificationDescription;
@property (nonatomic, retain) NSString * notificationId;
@property (nonatomic, retain) NSString * title;
@property (nonatomic, retain) NSNumber * read;
@property (nonatomic, retain) NSNumber * sticky;

@end
