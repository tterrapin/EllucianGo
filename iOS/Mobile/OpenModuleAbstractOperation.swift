//
//  OpenModuleAbstractOperation.swift
//  Mobile
//
//  Created by Jason Hocker on 6/25/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import UIKit

class OpenModuleAbstractOperation: NSOperation {
    
    var performAnimation = false
    
    func showViewController(controller: UIViewController) {
        
        let slidingViewController = findSlidingViewController()
        
        var controllerToShow = controller
        switch controller {
        case is UINavigationController:
            let navigationController = controller as! UINavigationController
            navigationController.navigationBar.translucent = false
            addMenuButton(navigationController.topViewController)
            controllerToShow = controller
        case is UITabBarController:
            let tabBarController = controller as! UITabBarController
            tabBarController.tabBar.translucent = false
            if let viewControllers = tabBarController.viewControllers {
                for c in viewControllers {
                    switch c {
                    case is UISplitViewController:
                        let splitViewController = c as! UISplitViewController
                        let navController = splitViewController.viewControllers[0] as! UINavigationController
                        let masterController = navController.topViewController!
                        
                        if masterController.conformsToProtocol(UISplitViewControllerDelegate) {
                            let delegate : UISplitViewControllerDelegate = masterController as! UISplitViewControllerDelegate
                            splitViewController.delegate = delegate
                            
                        }
                        if masterController.respondsToSelector(Selector("revealMenu:")) {
                            addMenuButton(masterController)
                        }
                    case is UINavigationController:
                        let navigationController = c as! UINavigationController
                        addMenuButton(navigationController.topViewController)
                        
                    default:
                        ()
                    }
                }
            }
            controllerToShow = controller
        case is UISplitViewController:
            let splitViewController = controller as! UISplitViewController
            splitViewController.presentsWithGesture = true
            
            var masterController = splitViewController.viewControllers[0]
            if masterController.isKindOfClass(UINavigationController) {
                let navMasterController = masterController as! UINavigationController
                masterController = navMasterController.topViewController!
            }
            
            var detailController = splitViewController.viewControllers[1]
            if detailController.isKindOfClass(UINavigationController) {
                let navDetailController = detailController as! UINavigationController
                detailController = navDetailController.topViewController!
            }
            
            
            if masterController.conformsToProtocol(UISplitViewControllerDelegate) {
                let delegate : UISplitViewControllerDelegate = masterController as! UISplitViewControllerDelegate
                splitViewController.delegate = delegate
                
            }
            if detailController.conformsToProtocol(UISplitViewControllerDelegate) {
                let delegate : UISplitViewControllerDelegate = detailController as! UISplitViewControllerDelegate
                splitViewController.delegate = delegate
            }
            
            if detailController.conformsToProtocol(DetailSelectionDelegate) {
                if masterController.respondsToSelector(Selector("detailSelectionDelegate")) {
                    masterController.setValue(detailController, forKey: "detailSelectionDelegate")
                    
                }
            }
            addMenuButton(masterController)
            
            controllerToShow = controller
            
        default:
            let navigationController = UINavigationController(rootViewController: controller)
            addMenuButton(controllerToShow)
            controllerToShow = navigationController
        }
        
        if let panGesture = slidingViewController.panGesture {
            controllerToShow.view.addGestureRecognizer(panGesture)
        }
        
        dispatch_async(dispatch_get_main_queue(), {
            
            if self.performAnimation {
                let transition = CATransition()
                transition.type = kCATransitionPush
                transition.subtype = kCATransitionFromRight
                transition.duration = 0.5
                transition.timingFunction = CAMediaTimingFunction(name: kCAMediaTimingFunctionDefault)
                transition.fillMode = kCAFillModeRemoved
                let slidingViewController = self.findSlidingViewController()
                slidingViewController.topViewController.view.window?.layer.addAnimation(transition, forKey: "transition")
            }
            
            let segue = ECSlidingSegue(identifier: "", source: slidingViewController.topViewController, destination: controllerToShow)
            slidingViewController.topViewController.prepareForSegue(segue, sender: nil)
            segue.perform()
        })
        
        
    }
    
    func findSlidingViewController() -> ECSlidingViewController {
        
        let appDelegate = UIApplication.sharedApplication().delegate as! AppDelegate
        return appDelegate.slidingViewController!
    }
    
    private func addMenuButton(controller: UIViewController?) {
        if let controller = controller where controller.respondsToSelector(Selector("revealMenu:")) {
            
            var buttonImage = UIImage(named: "icon-menu-iphone")
            
            //exception for home screens
            if controller is HomeViewController {
                buttonImage = UIImage(named: "home-menu-icon")
            }

            buttonImage?.isAccessibilityElement = false

            let button = UIBarButtonItem(image: buttonImage, style: UIBarButtonItemStyle.Plain, target: controller, action:"revealMenu:")
            button.accessibilityLabel = NSLocalizedString("Menu", comment: "Accessibility menu label")
            controller.navigationItem.leftBarButtonItem = button
        }
    }
    
    //MARK: find modules
    
    class func findUserModules(limitToHomeScreen: Bool = false) -> [Module] {
        let userRoles : Set<String>?
        if let currentUser = CurrentUser.sharedInstance() where currentUser.isLoggedIn {
            userRoles = currentUser.roles as! Set<String>?
        } else {
            userRoles = nil
        }
        
        var parr = [NSPredicate]()
        
        var results : [Module]
        let request = NSFetchRequest(entityName: "Module")
        if(limitToHomeScreen) {
            request.sortDescriptors = [NSSortDescriptor(key: "homeScreenOrder" , ascending: true)]
        } else {
            request.sortDescriptors = [NSSortDescriptor(key: "index" , ascending: true)]
        }
        
        if let userRoles = userRoles {
            
            parr.append(NSPredicate(format: "roles.@count == 0"))
            parr.append(NSPredicate(format: "ANY roles.role like %@", "Everyone"))
            for role in userRoles {
                parr.append(NSPredicate(format: "ANY roles.role like %@", role))
            }
            
        } else {
            parr.append(NSPredicate(format: "(hideBeforeLogin == %@) || (hideBeforeLogin = nil)", NSNumber(bool: false)))
            
        }
        
        let joinOnRolesPredicate = NSCompoundPredicate(type: .OrPredicateType, subpredicates: parr)
        let allModules = CoreDataManager.shared.executeFetchRequest(request) as! [Module]
        results = allModules.filter{ joinOnRolesPredicate.evaluateWithObject($0) }
        results = results.filter( { isSupported($0) } )
        
        if limitToHomeScreen {
            results = results.filter( { $0.homeScreenOrder != nil && $0.homeScreenOrder > 0 && $0.homeScreenOrder <= 5 } )
        }
        
        return results
    }
    
    class func isSupported(module: Module) -> Bool {
        if let type = module.type {
            switch type {
            case "header":
                return true
            case "web":
                return true
            case "custom":
                guard let customModuleType = module.propertyForKey("custom-type") else { return false }
                let moduleDefinition = self.readCustomizationsPropertyList()[customModuleType]
                return moduleDefinition != nil
            default:
                let moduleDefinition = readEllucainPropertyList()[module.type]
                return moduleDefinition != nil
                
                
            }
        } else {
            return false
        }
    }
    
    class func readCustomizationsPropertyList() -> Dictionary<String, AnyObject> {
        if let customizationsPath = NSBundle.mainBundle().pathForResource("Customizations", ofType: "plist"), let customizationsDictionary = NSDictionary(contentsOfFile: customizationsPath) as? Dictionary<String, AnyObject> {
            
            return customizationsDictionary["Custom Modules"]  as! Dictionary<String, AnyObject>
            
        } else {
            return Dictionary<String, AnyObject> ()
        }
        
    }
    
    class func readEllucainPropertyList() -> Dictionary<String, AnyObject> {
        if let ellucianPath = NSBundle.mainBundle().pathForResource("EllucianModules", ofType: "plist"), let ellucianDictionary = NSDictionary(contentsOfFile: ellucianPath) as? Dictionary<String, AnyObject> {
            
            return ellucianDictionary
        } else {
            return Dictionary<String, AnyObject> ()
        }
    }
    
    
    
    
}
