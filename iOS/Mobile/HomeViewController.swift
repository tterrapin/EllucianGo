//
//  HomeViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 1/25/16.
//  Copyright © 2016 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class HomeViewController : UIViewController {
    
    var backgroundImageView : UIImageView!
    
    @IBOutlet var menu1: UIView!
    @IBOutlet var menu2: UIView!
    @IBOutlet var menu3: UIView!
    @IBOutlet var menu4: UIView!
    @IBOutlet var menu5: UIView!
    
    @IBOutlet var blurredImageView: UIView!
    @IBOutlet var menuView: UIView!
    
    @IBOutlet weak var menuContainerView: UIView!
    @IBOutlet var lightVisualEffectView: UIVisualEffectView!
    @IBOutlet var darkVisualEffectView: UIVisualEffectView!
    
    @IBOutlet var animationConstraint: NSLayoutConstraint!
    
    var modules: [Module]?
    
    var originalImage : UIImage?
 
    @IBOutlet var cwrhMenu1Constraint: NSLayoutConstraint!
    @IBOutlet var cwrhMenu2Constraint: NSLayoutConstraint!
    @IBOutlet var cwrhMenu3Constraint: NSLayoutConstraint!
    @IBOutlet var cwrhMenu4Constraint: NSLayoutConstraint!

    
    @IBOutlet var anyMenu1Constraint: NSLayoutConstraint!
    @IBOutlet var anyMenu2Constraint: NSLayoutConstraint!
    @IBOutlet var anyMenu3Constraint: NSLayoutConstraint!
    @IBOutlet var anyMenu4Constraint: NSLayoutConstraint!
    
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.animationConstraint.priority = UILayoutPriorityDefaultHigh + 1
        
        if let navigationController = self.navigationController {
            navigationController.navigationBar.setBackgroundImage(UIImage(), forBarMetrics: UIBarMetrics.Default)
            navigationController.navigationBar.shadowImage = UIImage()
            navigationController.navigationBar.translucent = true
            navigationController.view.backgroundColor = UIColor.clearColor()
            navigationController.navigationBar.backgroundColor = UIColor.clearColor()
            
        }
        
        let defaults = AppGroupUtilities.userDefaults()
        
        let homeTabletBackground = defaults!.stringForKey("home-tablet-background")
        let schoolBackgroundImage = (homeTabletBackground?.characters.count > 0 && UIDevice.currentDevice().userInterfaceIdiom == .Pad) ? homeTabletBackground : defaults?.stringForKey("home-background")
        if schoolBackgroundImage != nil {
            if let image = ImageCache.sharedCache().getCachedImage(schoolBackgroundImage) {
                self.backgroundImageView.image = image
                self.originalImage = image
            }
        }
        
        if let color = defaults?.stringForKey("home-overlay-color") {
            if color == "light" {
                lightVisualEffectView.hidden = false
            } else if color == "dark" {
                darkVisualEffectView.hidden = false
            }
        }
        
        let buildMenuOperation = OpenModuleFindModulesOperation()
        buildMenuOperation.limitToHomeScreen = true
        buildMenuOperation.completionBlock = {
            dispatch_async(dispatch_get_main_queue(),{
                self.modules = buildMenuOperation.modules
                self.drawMenu()
                self.view.setNeedsDisplay()
                
                self.view.layoutIfNeeded()
                UIView.animateWithDuration(0.5, animations: {() -> Void in
                    self.animationConstraint.priority = UILayoutPriorityDefaultLow
                    self.view.layoutIfNeeded()
                })
                
            })
        }
        NSOperationQueue.mainQueue().addOperation(buildMenuOperation)
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        self.sendView("Show Home Screen", forModuleNamed: "")
    }
    
    func drawMenu() {
        if let modules = self.modules {
            
            if #available(iOS 9.0, *) {
                var shortcuts = [UIApplicationShortcutItem]()
                for index in 0..<modules.count {
                    let module = modules[index]
                    let shortcutItem = UIApplicationShortcutItem(type: module.internalKey, localizedTitle: module.name)
                    shortcuts.append(shortcutItem)
                }
                UIApplication.sharedApplication().shortcutItems = shortcuts
            }
            
            let menuItemCount = min(modules.count, 5) //in case cloud ever returns more than 5
            let menuViews = [menu1, menu2, menu3, menu4, menu5];
            
            switch menuItemCount {
            case 1:
                anyMenu1Constraint.priority = 995
                cwrhMenu1Constraint.priority = 995
            case 2:
                anyMenu2Constraint.priority = 995
                cwrhMenu2Constraint.priority = 995
            case 3:
                anyMenu3Constraint.priority = 995
                cwrhMenu3Constraint.priority = 995
            case 4:
                anyMenu4Constraint.priority = 995
                cwrhMenu4Constraint.priority = 995
            default:
                ()
            }
            
            for index in 0..<menuItemCount {
                let module = modules[index]
                let cell = menuViews[index]
                cell.hidden = false
                
                if let nameLabel = cell.viewWithTag(101) as? UILabel {
                    nameLabel.text = module.name
                    nameLabel.layer.shadowOffset = CGSizeMake(0, 1)
                    nameLabel.layer.shadowColor = UIColor.blackColor().CGColor
                    nameLabel.layer.shadowOpacity = 0.7
                    nameLabel.layer.shadowRadius = 1
                    cell.accessibilityLabel = module.name
                }
                if let imageView = cell.viewWithTag(102) as? UIImageView {
                    if let iconUrl = module.iconUrl where module.iconUrl.characters.count > 0 {
                        imageView.image = ImageCache.sharedCache().getCachedImage(iconUrl)
                        imageView.layer.shadowOffset = CGSizeMake(0, 1)
                        imageView.layer.shadowColor = UIColor.blackColor().CGColor
                        imageView.layer.shadowOpacity = 0.4
                        imageView.layer.shadowRadius = 1
                        if UIDevice.currentDevice().userInterfaceIdiom == .Pad {
                            
                            
                            if let iconBackgroundView = cell.viewWithTag(4) {
                                
                                let layer = iconBackgroundView.layer
                                
                                iconBackgroundView.layer.cornerRadius = iconBackgroundView.frame.size.width/2
                                iconBackgroundView.clipsToBounds = true

                                let gradientLayer = CAGradientLayer()
                                gradientLayer.frame =  CGRect(origin: CGPointZero, size: layer.bounds.size)
                                gradientLayer.startPoint = CGPointMake(0.5, 0.0)
                                gradientLayer.endPoint = CGPointMake(0.5, 1.0)
                                gradientLayer.colors =  [UIColor.clearColor().CGColor,UIColor.whiteColor().CGColor]
                                
                                let shapeLayer = CAShapeLayer()
                                shapeLayer.lineWidth  = 1
                                shapeLayer.path = UIBezierPath(ovalInRect: layer.bounds).CGPath
                                shapeLayer.fillColor = nil
                                shapeLayer.strokeColor = UIColor.blackColor().CGColor
                                gradientLayer.mask = shapeLayer
                                
                                layer.addSublayer(gradientLayer)
                            }
                        }
                    } else {
                        imageView.image = nil
                         if let iconBackgroundView = cell.viewWithTag(4) {
                            iconBackgroundView.hidden = true
                        }
                    }
                }
                if let countLabel = cell.viewWithTag(103) as? UILabel, lockImageView = cell.viewWithTag(104) as? UIImageView  {
                    
                    countLabel.text = nil
                    countLabel.hidden = true
                    lockImageView.hidden = true
                    
                    if CurrentUser.sharedInstance().isLoggedIn {
                        
                        if module.type == "notifications" {
                            do{
                                let managedObjectContext = CoreDataManager.shared.managedObjectContext
                                let request = NSFetchRequest(entityName: "Notification")
                                request.predicate = NSPredicate(format: "read == %@", false)
                                request.includesSubentities = false
                                let notifications = try managedObjectContext.executeFetchRequest(request)
                                let count = notifications.count
                                countLabel.text = "\(count)"
                                drawNotificationsLabel(countLabel)
                                countLabel.hidden = (count == 0)
                            } catch {
                            }
                        }
                        
                        lockImageView.hidden = true
                    } else {
                        if module.requiresAuthentication() {
                            lockImageView.hidden = false
                            lockImageView.layer.shadowOffset = CGSizeMake(0, 1)
                            lockImageView.layer.shadowColor = UIColor.blackColor().CGColor
                            lockImageView.layer.shadowOpacity = 0.4
                            lockImageView.layer.shadowRadius = 1
                        }
                    }
                }
                
                let gestureRecognizer = HomeMenuTapGestureRecognizer(target: self, action: "openMenuItem:")
                gestureRecognizer.module = module
                cell.addGestureRecognizer(gestureRecognizer)
            }
            for index in menuItemCount..<5 {
                let cell = menuViews[index]
                cell.removeFromSuperview()
            }
            if menuItemCount == 0 {
                menuContainerView.removeFromSuperview()
            }
        }
    }

    func drawNotificationsLabel(label: UILabel) {
        let layer = label.layer
        layer.cornerRadius = label.bounds.size.height / 2
        label.textColor = UIColor.blackColor()
        label.font = UIFont.systemFontOfSize(14)
        label.textAlignment = NSTextAlignment.Center
        label.backgroundColor = UIColor(red: 102/255, green: 102/255, blue: 102/255, alpha: 1)
    }
    
    func openMenuItem(gestureRecognizer: HomeMenuTapGestureRecognizer) {
        
        if let module = gestureRecognizer.module {
            let type = module.type
            if type == "web" && ( module.propertyForKey("external") ?? "false" ) == "true" {
                NSOperationQueue.mainQueue().addOperation(OpenModuleOperation(module: module))
            } else if module.type == "appLauncher" {
                NSOperationQueue.mainQueue().addOperation(OpenModuleOperation(module: module))
            } else {
                let operation = OpenModuleOperation(module: module)
                operation.performAnimation = true
                NSOperationQueue.mainQueue().addOperation(operation)
            }
        }
    }
}

class HomeMenuTapGestureRecognizer : UITapGestureRecognizer {
    var module : Module?
}
