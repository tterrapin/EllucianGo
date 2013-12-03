//
//  NoAnimationSegue.m
//  Mobile
//
//  Created by Jason Hocker on 8/9/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "NoAnimationSegue.h"

@implementation NoAnimationSegue

-(void) perform
{
    [[self destinationViewController] setModalPresentationStyle:UIModalPresentationFullScreen];
    [[self sourceViewController] presentViewController:[self destinationViewController] animated:NO completion:nil];
}
@end
