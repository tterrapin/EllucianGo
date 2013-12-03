//
//  Feed+Create.m
//  Mobile
//
//  Created by Jason Hocker on 8/17/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "Feed+Create.h"
#import "NSString+HTML.h"
@implementation Feed (Create)

+ (Feed *)feedFromDictionary:(NSDictionary *)dictionary inManagedObjectContext:(NSManagedObjectContext *)managedObjectContext forModuleNamed:(NSString* )moduleName hiddenCategories:(NSSet *)hiddenCategories;
{
    Feed *feed = nil;
    
    NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"Feed"];
    request.predicate = [NSPredicate predicateWithFormat:@"entryId = %@ AND feedName = %@ AND moduleName = %@", [dictionary objectForKey:@"entryId"], [dictionary objectForKey:@"feedName"], moduleName];
    
    NSSortDescriptor *sortDescriptor = [NSSortDescriptor sortDescriptorWithKey:@"entryId" ascending:YES];
    request.sortDescriptors = [NSArray arrayWithObject:sortDescriptor];
    
    NSError *error = nil;
    NSArray *matches = [managedObjectContext executeFetchRequest:request error:&error];
    
    if (!matches || ([matches count] > 1)) {
        // handle error
    } else if ([matches count] == 0) {
        feed = [NSEntityDescription insertNewObjectForEntityForName:@"Feed" inManagedObjectContext:managedObjectContext];
        feed.moduleName = moduleName;
        [Feed populate:feed withDictionary:dictionary inManagedObjectContext:managedObjectContext hiddenCategories:hiddenCategories];
    } else {
        feed = [matches lastObject];
        [Feed populate:feed withDictionary:dictionary inManagedObjectContext:managedObjectContext hiddenCategories:hiddenCategories];
        
    }
    
    return feed;
}

+ (void) populate:(Feed *)feed withDictionary:(NSDictionary *) dictionary inManagedObjectContext:(NSManagedObjectContext *)managedObjectContext hiddenCategories:(NSSet *)hiddenCategories;
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss'Z'"];
    dateFormatter.timeZone = [NSTimeZone timeZoneForSecondsFromGMT:0];

    feed.entryId = [dictionary objectForKey:@"entryId"];
    feed.postDateTime = [dateFormatter dateFromString:[dictionary objectForKey:@"postDate"]];
    feed.link = [[dictionary objectForKey:@"link"] objectAtIndex:0];
    if([dictionary objectForKey:@"title"] != [NSNull null]) {
        feed.title = [[dictionary objectForKey:@"title"] stringByConvertingHTMLToPlainText];
    }
    if([dictionary objectForKey:@"content"] != [NSNull null]) {
        feed.content = [[dictionary objectForKey:@"content"]  stringByConvertingHTMLToPlainText];
    }

    if([dictionary objectForKey:@"logo"] != [NSNull null]) {
        feed.logo = [dictionary objectForKey:@"logo"];
    }
    feed.feedName = [dictionary objectForKey:@"feedName"];

    feed.show = [NSNumber numberWithBool:![hiddenCategories containsObject:feed.feedName] ];
    
}
@end
