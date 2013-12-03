//
//  SlidingViewController.m
//  Mobile
//
//  Created by Jason Hocker on 8/27/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.

#import "SlidingViewController.h"
#import "MenuViewController.h"
#import "HomeViewController.h"
#import "LoginViewController.h"
#import "CurrentUser.h"
#import "ImageCache.h"
#import "AppDelegate.h"
#import "WebViewController.h"
#import "UIViewController+GoogleAnalyticsTrackerSupport.h"

@interface SlidingViewController()

@property (nonatomic, strong) UIView *topViewSnapshot;
@property (nonatomic, unsafe_unretained) CGFloat initialTouchPositionX;
@property (nonatomic, unsafe_unretained) CGFloat initialHoizontalCenter;

@property (nonatomic, unsafe_unretained) BOOL menuShowing;
@property (nonatomic, unsafe_unretained) BOOL topViewIsOffScreen;

@end


@implementation SlidingViewController


- (void)setTopViewController:(UIViewController *)theTopViewController
{
    [self removeTopViewSnapshot];
    [_topViewController.view removeFromSuperview];
    [_topViewController willMoveToParentViewController:nil];
    [_topViewController removeFromParentViewController];
    
    _topViewController = theTopViewController;
    
    _topViewController.view.layer.shadowOpacity = 0.75f;
    _topViewController.view.layer.shadowRadius = 10.0f;
    _topViewController.view.layer.shadowColor = [UIColor blackColor].CGColor;
    
    [self addChildViewController:_topViewController];
    [self.topViewController didMoveToParentViewController:self];
    
    [_topViewController.view setAutoresizingMask:self.autoResizeToFillScreen];
    [_topViewController.view setFrame:self.view.bounds];
    _topViewController.view.layer.shadowOffset = CGSizeZero;
    _topViewController.view.layer.shadowPath = [UIBezierPath bezierPathWithRect:self.view.layer.bounds].CGPath;
    
    [self.view addSubview:_topViewController.view];
    

}

- (void)setMenuViewController:(MenuViewController *)theMenuController
{
    [_menuViewController.view removeFromSuperview];
    [_menuViewController willMoveToParentViewController:nil];
    [_menuViewController removeFromParentViewController];
    
    _menuViewController = theMenuController;
    
    if (_menuViewController) {
        [self addChildViewController:_menuViewController];
        [self.menuViewController didMoveToParentViewController:self];
        
        [self updateMenuLayout];
        
        [self.view insertSubview:_menuViewController.view atIndex:0];
        UITableView *menuTable = self.menuViewController.tableView;
        [menuTable setScrollsToTop:NO];
    }
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.resetTapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(resetTopView)];
    self.panGesture = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(updateTopViewHorizontalCenterWithRecognizer:)];
    self.panGesture.delegate = self;
    
    MenuViewController * menuController = (MenuViewController *)[self.storyboard instantiateViewControllerWithIdentifier:@"Menu"];
    menuController.managedObjectContext = self.managedObjectContext;
    self.menuViewController = menuController;
    self.menuViewController.tableView.hidden = YES;   
   
    [self showHome];
    
    self.topViewSnapshot = [[UIView alloc] initWithFrame:self.topView.bounds];
    [self.topViewSnapshot setAutoresizingMask:self.autoResizeToFillScreen];
    [self.topViewSnapshot addGestureRecognizer:self.resetTapGesture];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(returnToHome:)
                                                 name:kSignInReturnToHomeNotification
                                               object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(returnToHome:)
                                                 name:kSignOutNotification
                                               object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(forceConfigurationSelection:)
                                                 name:kConfigurationFetcherError
                                               object:nil];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    self.topView.layer.shadowOffset = CGSizeZero;
    self.topView.layer.shadowPath = [UIBezierPath bezierPathWithRect:self.view.layer.bounds].CGPath;
    [self adjustLayout];
}

- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    if(self.topViewIsOffScreen) return;
    self.topView.layer.shadowPath = nil;
    self.topView.layer.shouldRasterize = YES;
    
    if(![self topViewHasFocus]){
        [self removeTopViewSnapshot];
    }
    
    [self adjustLayout];
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation{
    self.topView.layer.shadowPath = [UIBezierPath bezierPathWithRect:self.view.layer.bounds].CGPath;
    self.topView.layer.shouldRasterize = NO;
    
    if(![self topViewHasFocus]){
        [self addTopViewSnapshot];
    }
}

- (void)adjustLayout
{
    self.topViewSnapshot.frame = self.topView.bounds;
    
    if ([self menuShowing] && ![self topViewIsOffScreen]) {
        [self updateMenuLayout];
        [self updateTopViewHorizontalCenter:self.anchorRightTopViewCenter];
    } else if ([self menuShowing] && [self topViewIsOffScreen]) {
        [self updateMenuLayout];
        [self updateTopViewHorizontalCenter:self.screenWidth + self.resettedCenter];
    }
}

- (void)updateTopViewHorizontalCenterWithRecognizer:(UIPanGestureRecognizer *)recognizer
{
    CGPoint currentTouchPoint     = [recognizer locationInView:self.view];
    CGFloat currentTouchPositionX = currentTouchPoint.x;
    
    if (recognizer.state == UIGestureRecognizerStateBegan) {
        self.initialTouchPositionX = currentTouchPositionX;
        self.initialHoizontalCenter = self.topView.center.x;
    } else if (recognizer.state == UIGestureRecognizerStateChanged) {
        CGFloat panAmount = self.initialTouchPositionX - currentTouchPositionX;
        CGFloat newCenterPosition = self.initialHoizontalCenter - panAmount;
        
        if (newCenterPosition < self.resettedCenter) {
            newCenterPosition = self.resettedCenter;
        }
        
        [self topViewHorizontalCenterWillChange:newCenterPosition];
        [self updateTopViewHorizontalCenter:newCenterPosition];
        [self topViewHorizontalCenterDidChange:newCenterPosition];
    } else if (recognizer.state == UIGestureRecognizerStateEnded || recognizer.state == UIGestureRecognizerStateCancelled) {
        CGPoint currentVelocityPoint = [recognizer velocityInView:self.view];
        CGFloat currentVelocityX     = currentVelocityPoint.x;
        
        if ([self menuShowing] && currentVelocityX > 100) {
            NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
            [defaults setBool:YES forKey:@"menu-discovered"];
            [defaults synchronize];
            [self sendEventToTracker1WithCategory:kAnalyticsCategoryUI_Action withAction:kAnalyticsActionSlide_Action withLabel:@"Activate sliding menu (Apple only)" withValue:nil forModuleNamed:nil];
            [self slideTop];
        } else {
            [self resetTopView];
        }
    }
}

- (void)slideTop
{
    [self slideTop:nil onComplete:nil];
}

- (void)slideTop:(void (^)())animations onComplete:(void (^)())complete
{
    CGFloat newCenter = self.anchorRightTopViewCenter;
    
    [self topViewHorizontalCenterWillChange:newCenter];
    
    [UIView animateWithDuration:0.25f animations:^{
        if (animations) {
            animations();
        }
        [self updateTopViewHorizontalCenter:newCenter];
    } completion:^(BOOL finished){
            self.panGesture.enabled = YES;
        
        if (complete) {
            complete();
        }
        self.topViewIsOffScreen = NO;
        [self addTopViewSnapshot];
        dispatch_async(dispatch_get_main_queue(), ^{
            [[NSNotificationCenter defaultCenter] postNotificationName:@"SlideOpenTopController" object:self userInfo:nil];
        });
    }];
}

- (void)animateTopChange
{
    [self animateTopChange:nil onComplete:nil];
}

- (void)animateTopChange:(void(^)())animations onComplete:(void(^)())complete
{
    CGFloat newCenter = self.screenWidth + self.resettedCenter;

    [self topViewHorizontalCenterWillChange:newCenter];
    
    [UIView animateWithDuration:0.25f animations:^{
        if (animations) {
            animations();
        }
        [self updateTopViewHorizontalCenter:newCenter];
    } completion:^(BOOL finished){
        if (complete) {
            complete();
        }
        self.topViewIsOffScreen = YES;
        [self addTopViewSnapshot];
        dispatch_async(dispatch_get_main_queue(), ^{
            [[NSNotificationCenter defaultCenter] postNotificationName:@"SlideChangeTopController" object:self userInfo:nil];
        });
    }];
}

- (void)resetTopView
{
    [self resetTopViewWithAnimations:nil onComplete:nil];
}

- (void)resetTopViewWithAnimations:(void(^)())animations onComplete:(void(^)())complete
{
    [self topViewHorizontalCenterWillChange:self.resettedCenter];
    
    [UIView animateWithDuration:0.25f animations:^{
        if (animations) {
            animations();
        }
        [self updateTopViewHorizontalCenter:self.resettedCenter];
    } completion:^(BOOL finished) {
        if (complete) {
            complete();
        }
        [self topViewHorizontalCenterDidChange:self.resettedCenter];
    }];
}

- (NSUInteger)autoResizeToFillScreen
{
    return (UIViewAutoresizingFlexibleWidth |
            UIViewAutoresizingFlexibleHeight |
            UIViewAutoresizingFlexibleTopMargin |
            UIViewAutoresizingFlexibleBottomMargin |
            UIViewAutoresizingFlexibleLeftMargin |
            UIViewAutoresizingFlexibleRightMargin);
}

- (UIView *)topView
{
    return self.topViewController.view;
}

- (UIView *)menuView
{
    return self.menuViewController.view;
}

- (void)updateTopViewHorizontalCenter:(CGFloat)newHorizontalCenter
{
    CGPoint center = self.topView.center;
    center.x = newHorizontalCenter;
    self.topView.layer.position = center;
}

- (void)topViewHorizontalCenterWillChange:(CGFloat)newHorizontalCenter
{
    CGPoint center = self.topView.center;
    
    if (center.x <= self.resettedCenter && newHorizontalCenter > self.resettedCenter) {
        [self menuWillAppear];
    }
}

- (void)topViewHorizontalCenterDidChange:(CGFloat)newHorizontalCenter
{
    if (newHorizontalCenter == self.resettedCenter) {
        [self topDidReset];
    }
}

- (void)addTopViewSnapshot
{
    if (!self.topViewSnapshot.superview) {
        self.topViewSnapshot.layer.contents = (id)[UIImage imageWithUIView:self.topView].CGImage;
        [self.topView addSubview:self.topViewSnapshot];
    }
}

- (void)removeTopViewSnapshot
{
    if (self.topViewSnapshot.superview) {
        [self.topViewSnapshot removeFromSuperview];
    }
}

- (CGFloat)anchorRightTopViewCenter
{
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        return self.resettedCenter + 320.0;
    } else {
        return self.resettedCenter + 280.0;
    }
}

- (CGFloat)resettedCenter
{
    return ceil(self.screenWidth / 2);
}

- (CGFloat)screenWidth
{
    return [self screenWidthForOrientation:[UIApplication sharedApplication].statusBarOrientation];
}

- (CGFloat)screenWidthForOrientation:(UIInterfaceOrientation)orientation
{
    CGSize size = [UIScreen mainScreen].bounds.size;
    UIApplication *application = [UIApplication sharedApplication];
    if (UIInterfaceOrientationIsLandscape(orientation))
    {
        size = CGSizeMake(size.height, size.width);
    }
    if (application.statusBarHidden == NO)
    {
        size.height -= MIN(application.statusBarFrame.size.width, application.statusBarFrame.size.height);
    }
    return size.width;
}

- (void)menuWillAppear
{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:@"SlideOpenMenuAppears" object:self userInfo:nil];
    });
    [self.menuViewController viewWillAppear:NO];
    self.menuView.hidden = NO;
    [self updateMenuLayout];
    self.menuShowing  = YES;
    self.menuViewController.tableView.hidden = NO;
}

- (void)topDidReset
{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:@"SlideTopReset" object:self userInfo:nil];
    });
    [self.topView removeGestureRecognizer:self.resetTapGesture];
    [self removeTopViewSnapshot];
    self.panGesture.enabled = YES;
    self.menuShowing   = NO;
    self.menuViewController.tableView.hidden = YES;   
    self.topViewIsOffScreen = NO;
}

- (BOOL)topViewHasFocus
{
    return !self.menuShowing && !self.topViewIsOffScreen;
}

- (void)updateMenuLayout
{
    [self.menuView setAutoresizingMask:self.autoResizeToFillScreen];
    [self.menuView setFrame:self.view.bounds];
}

-(void)returnToHome:(id)sender
{
    [self showHome];
}

-(void)forceConfigurationSelection:(id)sender
{
    [self performSelectorOnMainThread:@selector(showConfigurationSelector) withObject:nil waitUntilDone:YES];
}

-(void) showConfigurationSelector
{
    [ConfigurationFetcher showErrorAlertView];
    
    AppDelegate *delegate = (AppDelegate *)[UIApplication sharedApplication].delegate;
    
    if(!delegate.useDefaultConfiguration) {
        UINavigationController *navcontroller = [self.storyboard instantiateViewControllerWithIdentifier:@"ConfigurationSelector"];
        navcontroller.navigationBar.translucent = NO;
        ConfigurationSelectionViewController *vc = navcontroller.childViewControllers[0];
        [vc setModalPresentationStyle:UIModalPresentationFullScreen];
        vc.managedObjectContext = self.managedObjectContext;
        [self presentViewController:navcontroller animated:NO completion:nil];
    }

}

-(void) showHome
{
    
    HomeViewController *homeController = (HomeViewController *) [self.storyboard instantiateViewControllerWithIdentifier:@"LandingPage"];
    if([homeController respondsToSelector:@selector(setManagedObjectContext:)]) {
        [homeController setValue:self.managedObjectContext forKey:@"managedObjectContext"];
    }
    UINavigationController *newTopViewController = [[UINavigationController alloc] initWithRootViewController:homeController];
    newTopViewController.navigationBar.translucent = NO;
    
    
    //newTopViewController.navigationItem.leftBarButtonItem.enabled = NO;
    //homeController .navigationItem.leftBarButtonItem.enabled = NO;
    UIImage *buttonImage = [UIImage imageNamed:@"icon-menu-iphone"];
    NSString *menuImageName = [[NSUserDefaults standardUserDefaults] objectForKey:@"menu-icon"];

    if(menuImageName)
    {
        buttonImage = [[ImageCache sharedCache] getCachedImage: menuImageName];
        buttonImage=  [UIImage imageWithCGImage:[buttonImage CGImage] scale:2.0 orientation:UIImageOrientationUp];
        if (SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"7.0")) {
            buttonImage = [buttonImage imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];
        }
    }
    homeController.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithImage:buttonImage style:UIBarButtonItemStylePlain target:homeController action:@selector(revealMenu:)];
    
    self.topViewController = newTopViewController;
    
    [self.topViewController.view addGestureRecognizer:self.panGesture];
}

-(void) showNotifications
{
    AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
    CurrentUser *user = [appDelegate getCurrentUser];
    if(!user.isLoggedIn) {
        [self showHome];
    } else {
        NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
        NSEntityDescription *entity = [NSEntityDescription entityForName:@"Module" inManagedObjectContext:self.managedObjectContext];
        [fetchRequest setEntity:entity];
        
        NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"index" ascending:YES];
        NSArray *sortDescriptors = [NSArray arrayWithObjects:sortDescriptor, nil];
        [fetchRequest setSortDescriptors:sortDescriptors];
        
        NSError *error;
        Module* module;
        NSArray *modules = [self.managedObjectContext executeFetchRequest:fetchRequest error:&error];
        for(NSManagedObject *managedObject in modules) {
            if([[managedObject valueForKey:@"type"] isEqualToString:@"notifications"]) {
                module = (Module*)managedObject;
            }
        }

        if(!module) {
            [self showHome];
        } else {
            UIViewController *notificationViewController = (UIViewController *)[self.storyboard instantiateViewControllerWithIdentifier:@"Notifications"];
            [notificationViewController setValue:module forKey:@"module"];
            
            UINavigationController *newTopViewController = [[UINavigationController alloc] initWithRootViewController:notificationViewController];
            newTopViewController.navigationBar.translucent = NO;
            UIImage *buttonImage = [UIImage imageNamed:@"icon-menu-iphone"];
            NSString *menuImageName = [[NSUserDefaults standardUserDefaults] objectForKey:@"menu-icon"];
            
            if(menuImageName)
            {
                buttonImage = [[ImageCache sharedCache] getCachedImage: menuImageName];
                buttonImage=  [UIImage imageWithCGImage:[buttonImage CGImage] scale:2.0 orientation:UIImageOrientationUp];
            }
            notificationViewController.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithImage:buttonImage style:UIBarButtonItemStylePlain target:newTopViewController action:@selector(revealMenu:)];
            
            self.topViewController = newTopViewController;
            
            [self.topViewController.view addGestureRecognizer:self.panGesture];
        }
    }
}

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldReceiveTouch:(UITouch *)touch {
    if ([touch.view isKindOfClass:[UISlider class]]) {
        // prevent recognizing touches on the slider
        return NO;
    }
    return YES;
}

@end
