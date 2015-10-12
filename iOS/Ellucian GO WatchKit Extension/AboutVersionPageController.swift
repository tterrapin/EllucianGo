//
//  AboutVersionPageController.swift
//  Mobile
//
//  Created by Jason Hocker on 4/26/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import WatchKit
import Foundation

class AboutVersionPageController: WKInterfaceController {
    
    @IBOutlet var clientVersionLabel: WKInterfaceLabel!
    @IBOutlet var serverVersionLabel: WKInterfaceLabel!
    override func awakeWithContext(context: AnyObject?) {
        if let clientVersion = NSBundle.mainBundle().objectForInfoDictionaryKey("CFBundleVersion") as? String {
            self.clientVersionLabel.setText(clientVersion)
        }
        
        if let urlString = AppGroupUtilities.userDefaults()?.stringForKey("about-version-url") {
            let url = NSURL(string: urlString)
            let task =  NSURLSession.sharedSession().dataTaskWithURL(url!, completionHandler : {data, response, error -> Void in
                if let httpRes = response as? NSHTTPURLResponse {
                    if let data = data where httpRes.statusCode == 200 {
                        let json = JSON(data: data)
                        let application = json["application"]
                        let version = application["version"].stringValue
                        
                        dispatch_async(dispatch_get_main_queue(), {
                            self.serverVersionLabel.setText(version)
                        })
                    }
                }
            })
            task.resume()
        }
    }
}
