//
//  LoginSignOutOperation.swift
//  Mobile
//
//  Created by Jason Hocker on 6/29/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import UIKit

class LoginSignOutOperation: NSOperation {

    override func main() {
        CurrentUser.sharedInstance().logout(true)
        NSNotificationCenter.defaultCenter().postNotificationName(kSignInReturnToHomeNotification, object: nil)
    }
}
