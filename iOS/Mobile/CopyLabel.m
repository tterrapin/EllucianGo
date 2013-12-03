#import "CopyLabel.h"

@implementation CopyLabel

#pragma mark Initialization

- (void) attachTapHandler
{
    [self setUserInteractionEnabled:YES];
    UIGestureRecognizer *touchy = [[UILongPressGestureRecognizer alloc]
                                   initWithTarget:self action:@selector(handleTap:)];
    [self addGestureRecognizer:touchy];
}

- (id) initWithFrame: (CGRect) frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self attachTapHandler];
        
    }
    return self;
}
- (id)initWithCoder:(NSCoder *)aDecoder{
    self = [super initWithCoder:aDecoder];
    if (self) {
        [self attachTapHandler];
    }
    return self;
}

#pragma mark Clipboard

- (void) copy: (id) sender
{
    [[UIPasteboard generalPasteboard] setString:self.text];
}

- (void) unhighlight{
    self.highlighted = NO;
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIMenuControllerWillHideMenuNotification object:nil];
}

- (BOOL) canPerformAction: (SEL) action withSender: (id) sender
{
    return (action == @selector(copy:));
}

- (void) handleTap: (UIGestureRecognizer*) recognizer
{
    [self becomeFirstResponder];
    UIMenuController *menu = [UIMenuController sharedMenuController];
    if (![menu isMenuVisible]) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(unhighlight) name:UIMenuControllerWillHideMenuNotification object:nil];
        [menu setTargetRect:self.frame inView:self.superview];
        [menu setMenuVisible:YES animated:YES];
        self.highlighted = YES;
    }
    
}

- (BOOL) canBecomeFirstResponder
{
    return YES;
}

@end