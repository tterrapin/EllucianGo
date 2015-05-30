//
//  AboutPolicyPageController.swift
//  Mobile
//
//  Created by Jason Hocker on 4/26/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import WatchKit
import Foundation
import CoreData


class AboutPolicyPageController: WKInterfaceController {
    
    @IBOutlet var policyLabelLabel: WKInterfaceLabel!
    @IBOutlet var policyLabel: WKInterfaceLabel!
    override func awakeWithContext(context: AnyObject?) {
        let defaults = AppGroupUtilities.userDefaults()
        self.policyLabelLabel.setText(defaults?.stringForKey("about-privacy-display"))
        self.policyLabel.setText(defaults?.stringForKey("about-privacy-url"))
    }
}
