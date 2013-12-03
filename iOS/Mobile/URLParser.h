//
//  URLParser.h
//  Mobile
//
//  Created by jkh on 1/8/13.
//  Copyright (c) 2013 Ellucian. All rights reserved.
//

@interface URLParser : NSObject {
    NSArray *variables;
}

@property (nonatomic, retain) NSArray *variables;

- (id)initWithURLString:(NSString *)url;
- (NSString *)valueForVariable:(NSString *)varName;

@end