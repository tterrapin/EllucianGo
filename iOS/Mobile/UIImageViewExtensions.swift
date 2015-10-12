//  UIImageViewExtensions.swift
//  Mobile
//
//  Created by Jason Hocker on 8/4/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

extension UIImageView {
    
    func convertToCircleImage() {
        layer.borderWidth = 1.0
        layer.masksToBounds = false
        layer.borderColor = UIColor.whiteColor().CGColor
        layer.cornerRadius = frame.size.width/2
        clipsToBounds = true
    }
    
    func loadImagefromURL(url: String) {
        NSURLSession.sharedSession().dataTaskWithURL(NSURL(string: url)!) { (data, response, error) in
            if let _ = error {
                return
            } else {
                dispatch_async(dispatch_get_main_queue()) {
                    self.image = UIImage(data: data!)
                }
            }
            }.resume()
    }
}