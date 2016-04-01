//
//  LoginSignInOperation.swift
//  Mobile
//
//  Created by Jason Hocker on 6/29/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import UIKit

class LoginSignInOperation: NSOperation {
    
    let controller: UIViewController
    var successCompletionHandler :  (() -> Void)?

    init(controller: UIViewController) {
        self.controller = controller
    }
    
    override func main() {
        let loginController = LoginExecutor.loginController()
        let loginProtocol = loginController.childViewControllers[0] as! LoginProtocol
        if let successCompletionHandler = successCompletionHandler {
            loginProtocol.completionBlock = successCompletionHandler
        }
        loginController.modalPresentationStyle = UIModalPresentationStyle.FormSheet
        controller.presentViewController(loginController, animated: true, completion: nil)
        
    }
}
