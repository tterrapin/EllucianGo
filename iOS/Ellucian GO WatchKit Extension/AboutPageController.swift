//
//  AboutPageController.swift
//  Mobile
//
//  Created by Jason Hocker on 4/26/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import WatchKit
import Foundation
import CoreData


class AboutPageController: WKInterfaceController {

    @IBOutlet var phoneLabel: WKInterfaceLabel!
    @IBOutlet var emailLabel: WKInterfaceLabel!
    @IBOutlet var websiteLabel: WKInterfaceLabel!

    @IBOutlet var contactButton: WKInterfaceButton!
    @IBOutlet var privacyButton: WKInterfaceButton!
    
    override func awakeWithContext(context: AnyObject?) {
        let defaults = AppGroupUtilities .userDefaults()
        if let phoneNumber : String = defaults?.stringForKey("about-phone-number") where phoneNumber.characters.count > 0  {
            phoneLabel.setText(phoneNumber)
        } else {
            phoneLabel.setHidden(true)
        }
        if let email : String = defaults?.stringForKey("about-email-address") where email.characters.count > 0  {
            emailLabel.setText(email)
        } else {
            emailLabel.setHidden(true)
        }
        if let web : String = defaults?.stringForKey("about-website-url") where web.characters.count > 0  {
            websiteLabel.setText(web)
        } else {
            websiteLabel.setHidden(true)
        }
        
        if let contact : String = defaults?.stringForKey("about-contact") where contact.characters.count > 0 {
            contactButton.setHidden(false)
        } else {
            contactButton.setHidden(true)
        }
        
        if let privacy : String = defaults?.stringForKey("about-privacy-url") where privacy.characters.count > 0 {
            privacyButton.setHidden(false)
        } else {
            privacyButton.setHidden(true)
        }


    }
}
