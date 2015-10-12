//
//  OpenModuleOperation.swift
//  Mobile
//
//  Created by Jason Hocker on 6/29/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import UIKit

class OpenModuleOperation: OpenModuleAbstractOperation {
    
    private var module : Module?
    private var moduleName : String?
    private var moduleType : String?
    var properties = [String : AnyObject]() //not using optionals for interop with objc
    
    init (module: Module?) {
        self.module = module
    }
    
    init(name: String, type: String) {
        self.moduleName = name
        self.moduleType = type
     }
    
    init(type: String) {
        self.moduleName = nil
        self.moduleType = type
    }
    
    override func main() {
        if module == nil {
            findModuleFromNameAndType()
        }
        if let module = module {

            if module.requiresAuthentication() && !CurrentUser.sharedInstance().isLoggedIn  {
                dispatch_async(dispatch_get_main_queue(), {
                    let loginController = LoginExecutor.loginController()
                    loginController.modalPresentationStyle = UIModalPresentationStyle.FormSheet
                    if let controller = loginController.childViewControllers[0] as? LoginProtocol {
                        controller.completionBlock = {
                            self.openModule(module)
                        }
                    }

                    let slidingViewController = self.findSlidingViewController()
                    slidingViewController.presentViewController(loginController, animated: true, completion: nil)
                })
            } else {
                openModule(module)
            }
        }
    }
    
    private func findAndShowController(definition: Dictionary<String, AnyObject>, isEllucian: Bool) {
        var storyboardName = definition["Storyboard Name"] as! String?
        var storyboardIdentifier : String?
        if storyboardName != nil {
            
            storyboardIdentifier = definition["Storyboard Identifier"] as! String?
            
        } else {
            if UI_USER_INTERFACE_IDIOM() == .Pad {
                storyboardIdentifier = definition["iPad Storyboard Identifier"] as! String?
                storyboardName = definition["iPad Storyboard Name"] as! String?
                
                if storyboardName == nil {
                    storyboardName = isEllucian ? "MainStoryboard_iPad" as String? : "CustomizationStoryboard_iPad" as String?
                }
            } else {
                storyboardIdentifier = definition["iPhone Storyboard Identifier"] as! String?
                storyboardName = definition["iPhone Storyboard Name"] as! String?
                
                if storyboardName == nil {
                    storyboardName = isEllucian ? "MainStoryboard_iPhone" as String? : "CustomizationStoryboard_iPhone" as String?
                }
            }
        }
        
        if let storyboardName = storyboardName, let storyboardIdentifier = storyboardIdentifier {
            let storyboard = UIStoryboard(name: storyboardName, bundle: nil)
            let controller = storyboard.instantiateViewControllerWithIdentifier(storyboardIdentifier)
                
            setModuleOnController(controller)
            addPropertiesToController(controller, properties: properties)
            
            dispatch_async(dispatch_get_main_queue(), {
                self.showViewController(controller)
            })
            
        }
    }
    
    func setModuleOnController(controller: UIViewController?) {
        if let controller = controller {
            if controller.respondsToSelector(Selector("setModule:")) {
                controller.setValue(module, forKey: "module")
            }
            
            switch controller {
            case is UITabBarController:
                let tabController = controller as! UITabBarController
                if let controllers = tabController.viewControllers {
                    for c in controllers {
                        setModuleOnController(c)
                    }
                }
            case is UINavigationController:
                let navController = controller as! UINavigationController
                if let topViewController = navController.topViewController {
                    setModuleOnController(topViewController)
                }
            case is UISplitViewController:
                let splitController = controller as! UISplitViewController
                for c in splitController.viewControllers {
                    setModuleOnController(c)
                }
                
            default: ()
            }
        }
    }
    
    private func addPropertiesToController(controller: UIViewController, properties: [String: AnyObject?]) {
        for (key, value) in properties {
            controller.setValue(value, forKey: key)
        }
    }
    
    private func findModuleFromNameAndType() {
        let request = NSFetchRequest(entityName: "Module")
        request.sortDescriptors = [NSSortDescriptor(key: "index", ascending: true)]
        
        let modules = OpenModuleAbstractOperation.findUserModules()
        
        for module in modules {
            if (module.name == moduleName || moduleName == nil) && module.type == moduleType {
                self.module = module
                break
            }
        }
    }
    
    private func openModule(module: Module) {
            var match = false
        if module.roles.count == 0 { //upgrades from 3.0 or earlier
            match = true
        }
        let moduleRoles = Array(module.roles)
        let filteredRoles = moduleRoles.filter {
            let role = $0 as! ModuleRole
            if role.role == "Everyone" {
                return true
            }
            if let roles = CurrentUser.sharedInstance().roles {
                for tempRole in roles {
                    if tempRole == role.role {
                        return true;
                    }
                }
                return false;
            }
            return false
        }
        
        if filteredRoles.count > 0 {
            match = true
        }
        
        if !match {
            dispatch_async(dispatch_get_main_queue(), {
                self.showAccessDeniedAlert()
            })
            return
        }

        
        if module.type == "web" {
            if let property = module.propertyForKey("external") where property == "true" {
                dispatch_async(dispatch_get_main_queue(), {
                    if let url = NSURL(string: module.propertyForKey("url")) {
                        UIApplication.sharedApplication().openURL(url)
                    }
                })
            } else {
                let storyboard = UIStoryboard(name: "WebStoryboard", bundle: nil)
                let webController = storyboard.instantiateViewControllerWithIdentifier("Web") as! WebViewController
                let controller = UINavigationController(rootViewController: webController)
                if let url = NSURL(string: module.propertyForKey("url")) {
                    webController.loadRequest = NSURLRequest(URL: url)
                    webController.title = module.name
                    var secure = false
                    if let secureProperty = module.propertyForKey("secure") {
                        secure = secureProperty == "true"
                    }
                    
                    webController.secure = secure
                    webController.analyticsLabel = module.name
                    
                    showViewController(controller)
                }
            }
        } else {
            if module.type == "custom" {
                let customModuleType = module.propertyForKey("custom-type")
                
                if let customizationsPath = NSBundle.mainBundle().pathForResource("Customizations", ofType: "plist"), let customizationsDictionary = NSDictionary(contentsOfFile: customizationsPath) as? Dictionary<String, AnyObject> {
                    
                    let customModuleDefinitions = customizationsDictionary["Custom Modules"] as! Dictionary<String, AnyObject>
                    let moduleDefinition = customModuleDefinitions[customModuleType] as! Dictionary<String, AnyObject>
                    
                    findAndShowController(moduleDefinition, isEllucian: false)
                }
                
            } else {
                if let ellucianPath = NSBundle.mainBundle().pathForResource("EllucianModules", ofType: "plist"), let ellucianDictionary = NSDictionary(contentsOfFile: ellucianPath) as? Dictionary<String, AnyObject> {
                    
                    let moduleDefinitions = ellucianDictionary
                    let moduleDefinition = moduleDefinitions[module.type] as! Dictionary<String, AnyObject>
                    findAndShowController(moduleDefinition, isEllucian: true)
                }
            }
        }

    }
    
    private func showAccessDeniedAlert() {
        let alertController = UIAlertController(title: NSLocalizedString("Access Denied", comment:"access denied error message"), message: NSLocalizedString("You do not have permission to use this feature.", comment:"permission access error message"), preferredStyle: .Alert)
        let cancelAction = UIAlertAction(title: "OK", style: .Default, handler: nil)
        alertController.addAction(cancelAction)
        let slidingViewController = self.findSlidingViewController()
        let topController = slidingViewController.topViewController
        topController.presentViewController(alertController, animated: true, completion: nil)
    }
}
