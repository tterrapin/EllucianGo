//
//  CalendarActionSheetDatePicker.m
//  Mobile
//
//  Created by Jason Hocker on 9/8/14.
//  Copyright (c) 2014 Ellucian. All rights reserved.
//

#import "CalendarActionSheetDatePicker.h"
#import "SWActionSheet.h"
#import "AppearanceChanger.h"

@interface CalendarActionSheetDatePicker()

@property(nonatomic, strong) NSDate *selectedDate;
@property(nonatomic, strong) UIBarButtonItem *originBarButtonItem;
@property(nonatomic, strong) UIBarButtonItem *doneBarButtonItem;
@property(nonatomic, strong) UIBarButtonItem *cancelBarButtonItem;
@property(nonatomic, strong) UIView *originContainerView;
@property(nonatomic, unsafe_unretained) id target;
@property(nonatomic, assign) SEL successAction;
@property(nonatomic, strong) SWActionSheet *actionSheet;
@property(nonatomic, strong) UIPopoverController *popoverController;
@property(nonatomic, strong) UIToolbar* toolbar;
@property(nonatomic, strong) UIView *pickerView;
@property(nonatomic, readonly) CGSize viewSize;
@property(nonatomic, assign) CGRect presentFromRect;
@property(nonatomic, assign) UIInterfaceOrientation interfaceOrientation;

@end

@implementation CalendarActionSheetDatePicker

- (id)initWithDate:(NSDate *)selectedDate target:(id)target action:(SEL)action origin:(id)origin
{
    self = [super init];
    if (self)
    {
        self.target = target;
        self.successAction = action;
        self.presentFromRect = CGRectZero;
        
        if ([origin isKindOfClass:[UIBarButtonItem class]]) {
            self.originBarButtonItem = origin;
        }
        else if ([origin isKindOfClass:[UIView class]]) {
            self.originContainerView = origin;
        }
        
        self.cancelBarButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCancel target:self
                                                                                 action:@selector(actionPickerCancel:)];
        self.doneBarButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self
                                                                               action:@selector(actionPickerDone:)];
        self.selectedDate = selectedDate;
    }
    return self;
}

- (UIView *)configuredPickerView {
    CGRect datePickerFrame = CGRectMake(0, 40, self.viewSize.width, 216);
    UIDatePicker *datePicker = [[UIDatePicker alloc] initWithFrame:datePickerFrame];
    datePicker.datePickerMode = UIDatePickerModeDate;

    [datePicker setDate:self.selectedDate animated:NO];
    [datePicker addTarget:self action:@selector(eventForDatePicker:) forControlEvents:UIControlEventValueChanged];
    
    self.pickerView = datePicker;
    
    return datePicker;
}

- (void)notifyTarget:(id)target didSucceedWithAction:(SEL)action origin:(id)origin {
    if ([target respondsToSelector:action])
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Warc-performSelector-leaks"
        [target performSelector:action withObject:self.selectedDate withObject:origin];
#pragma clang diagnostic pop
}

- (void)eventForDatePicker:(id)sender {
    if ([sender isKindOfClass:[UIDatePicker class]]) {
        UIDatePicker *datePicker = (UIDatePicker *)sender;
        self.selectedDate = datePicker.date;
    }
}

- (void)showActionSheetPicker
{
    UIView *masterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.viewSize.width, 260)];

    self.toolbar = [self createPickerToolbar];
    [masterView addSubview:self.toolbar];
    
    CGRect f = CGRectMake(0, self.toolbar.frame.origin.y, 8, masterView.frame.size.height - self.toolbar.frame.origin.y);
    UIToolbar *leftEdge = [[UIToolbar alloc] initWithFrame:f];
    f.origin.x = masterView.frame.size.width - 8;
    UIToolbar *rightEdge = [[UIToolbar alloc] initWithFrame:f];
    leftEdge.barTintColor = rightEdge.barTintColor = self.toolbar.barTintColor;
    [masterView insertSubview:leftEdge atIndex:0];
    [masterView insertSubview:rightEdge atIndex:0];
    
    self.pickerView = [self configuredPickerView];
    [masterView addSubview:self.pickerView];
    [self presentPickerForView:masterView];
}

- (IBAction)actionPickerDone:(id)sender
{
    [self notifyTarget:self.target didSucceedWithAction:self.successAction origin:[self storedOrigin]];
    [self dismissPicker];
}

- (IBAction)actionPickerCancel:(id)sender
{
    [self dismissPicker];
}

- (void)dismissPicker
{
    if (self.actionSheet) {
        [self.actionSheet dismissWithClickedButtonIndex:0 animated:YES];
    } else if (self.popoverController && self.popoverController.popoverVisible) {
        [self.popoverController dismissPopoverAnimated:YES];
    }
    self.actionSheet = nil;
    self.popoverController = nil;
}

- (UIToolbar *)createPickerToolbar
{
    CGRect frame = CGRectMake(0, 0, self.viewSize.width, 44);
    UIToolbar *pickerToolbar = [[UIToolbar alloc] initWithFrame:frame];
    pickerToolbar.barStyle = UIBarStyleDefault;
    
    NSMutableArray *barItems = [[NSMutableArray alloc] init];
    [barItems addObject:self.cancelBarButtonItem];
    UIBarButtonItem *flexSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    [barItems addObject:flexSpace];
    [barItems addObject:self.doneBarButtonItem];
    
    [pickerToolbar setItems:barItems animated:NO];
    return pickerToolbar;
}

- (CGSize)viewSize
{
    if ( UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad )
    {
        return CGSizeMake(320, 320);
    }

    CGSize size = [UIScreen mainScreen].bounds.size;
    if(SYSTEM_VERSION_LESS_THAN(@"8.0")) {
        if (UIInterfaceOrientationIsLandscape(self.interfaceOrientation))
        {
            size = CGSizeMake(size.height, size.width);
        }
    }
    return size;
}

- (id)storedOrigin
{
    if (self.originBarButtonItem) {
        return self.originBarButtonItem;
    } else {
        return self.originContainerView;
    }
}

- (void)dealloc
{
    if ( [self.pickerView respondsToSelector:@selector(setDelegate:)] )
        [self.pickerView performSelector:@selector(setDelegate:) withObject:nil];
    
    if ( [self.pickerView respondsToSelector:@selector(setDataSource:)] )
        [self.pickerView performSelector:@selector(setDataSource:) withObject:nil];
    
    self.target = nil;
    
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIDeviceOrientationDidChangeNotification object:nil];
}

#pragma mark - Popovers and ActionSheets

- (void)presentPickerForView:(UIView *)aView
{
    self.presentFromRect = aView.frame;
    
    if ( UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad )
        [self configureAndPresentPopoverForView:aView];
    else
        [self configureAndPresentActionSheetForView:aView];
}

- (void)configureAndPresentActionSheetForView:(UIView *)aView
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didRotate:) name:UIDeviceOrientationDidChangeNotification object:nil];
    
    self.interfaceOrientation = [[UIApplication sharedApplication] statusBarOrientation];
    
    self.actionSheet = [[SWActionSheet alloc] initWithView:aView];
    
    [self presentActionSheet:self.actionSheet];
}


- (void) didRotate:(NSNotification *)notification
{
    UIInterfaceOrientation  interfaceOrientation = [[UIApplication sharedApplication] statusBarOrientation];
    if(interfaceOrientation != self.interfaceOrientation) {
        [self dismissPicker];
    }
}

- (void)presentActionSheet:(SWActionSheet *)actionSheet
{
    NSParameterAssert(actionSheet != NULL);
    if (self.originBarButtonItem)
        [actionSheet showFromBarButtonItem:self.originBarButtonItem animated:YES];
    else
        [actionSheet showInContainerView];
}

- (void)configureAndPresentPopoverForView:(UIView *)aView
{
    UIViewController *viewController = [[UIViewController alloc] initWithNibName:nil bundle:nil];
    viewController.view = aView;
    viewController.preferredContentSize = aView.frame.size;
    
    self.popoverController = [[UIPopoverController alloc] initWithContentViewController:viewController];
    self.popoverController.delegate = self;
    [self presentPopover:self.popoverController];
}

- (void)presentPopover:(UIPopoverController *)popover
{
    if (self.originBarButtonItem)
    {
        [popover presentPopoverFromBarButtonItem:self.originBarButtonItem permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES];
    }
    else if (self.originContainerView)
    {
        [popover presentPopoverFromRect:self.originContainerView.bounds inView:self.originContainerView permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES];
    }
}

@end
