//
//  OpenModuleAbstractOperation.swift
//  Mobile
//
//  Created by Jason Hocker on 6/25/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import UIKit

class OpenModuleAbstractOperation: NSOperation {

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
            
            //TODO if no references to DetailSelectionDelegate
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
            
            let segue = ECSlidingSegue(identifier: "", source: slidingViewController.topViewController, destination: controllerToShow)
            slidingViewController.topViewController.prepareForSegue(segue, sender: nil)
            segue.perform()
        })
        
        
    }
    
    func findSlidingViewController() -> ECSlidingViewController {
        
        let appDelegate = UIApplication.sharedApplication().delegate as! AppDelegate
        return appDelegate.slidingViewController
    }

    private func addMenuButton(controller: UIViewController?) {
        if let controller = controller where controller.respondsToSelector(Selector("revealMenu:")) {

            var buttonImage = UIImage(named: "icon-menu-iphone")
            var originalImage = true
            if let menuImageName = AppGroupUtilities.userDefaults()?.stringForKey("menu-icon") {
                if let theirImage = ImageCache.sharedCache().getCachedImage(menuImageName) {
                    buttonImage = UIImage(CGImage: theirImage.CGImage!, scale: 2.0, orientation: UIImageOrientation.Up)
                    buttonImage = buttonImage!.imageWithRenderingMode(.AlwaysOriginal)
                } else {
                    buttonImage = buttonImage!.imageWithRenderingMode(.AlwaysTemplate)
                    originalImage = false
                }
            } else {
                buttonImage = buttonImage!.imageWithRenderingMode(.AlwaysTemplate)
                originalImage = false
            }
            buttonImage?.isAccessibilityElement = false
            
            let button = UIBarButtonItem(image: buttonImage, style: UIBarButtonItemStyle.Plain, target: controller, action:"revealMenu:")
            
            if !originalImage {
                button.tintColor = UIColor.headerTextColor()
            }
            button.accessibilityLabel = NSLocalizedString("Menu", comment: "Accessibility menu label")
            controller.navigationItem.leftBarButtonItem = button
        }
    }
    
    //MARK: find modules
    
    class func findUserModules() -> [Module] {
        let userRoles : Set<String>?
        if let currentUser = CurrentUser.sharedInstance() where currentUser.isLoggedIn {
            userRoles = currentUser.roles as! Set<String>?
        } else {
            userRoles = nil
        }
        
        var results : [Module]
        let request = NSFetchRequest(entityName: "Module")
        request.sortDescriptors = [NSSortDescriptor(key: "index" , ascending: true)]
        if let userRoles = userRoles {
            var parr = [NSPredicate]()
            parr.append(NSPredicate(format: "roles.@count == 0"))
            parr.append(NSPredicate(format: "ANY roles.role like %@", "Everyone"))
            for role in userRoles {
                parr.append(NSPredicate(format: "ANY roles.role like %@", role))
            }
            let joinOnRolesPredicate = NSCompoundPredicate(type: .OrPredicateType, subpredicates: parr)
            let allModules = CoreDataManager.shared.executeFetchRequest(request) as! [Module]
            
            
            results = allModules.filter{ joinOnRolesPredicate.evaluateWithObject($0) }
            
        } else {
            request.predicate = NSPredicate(format: "(hideBeforeLogin == %@) || (hideBeforeLogin = nil)", NSNumber(bool: false))
            results = CoreDataManager.shared.executeFetchRequest(request) as! [Module]
        }
        
        results = results.filter( { isSupported($0) } )
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
