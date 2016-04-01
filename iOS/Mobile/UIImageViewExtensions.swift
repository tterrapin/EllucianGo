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
        layer.borderColor = UIColor(red: 0.8, green: 0.8, blue: 0.8, alpha: 1.0).CGColor
        layer.cornerRadius = frame.size.width/2
        clipsToBounds = true
    }
    
    func loadImagefromURL(url: String, successHandler: (() -> Void)? = nil, failureHandler: (() -> Void)? = nil) {
        NSURLSession.sharedSession().dataTaskWithURL(NSURL(string: url)!) { (data, response, error) in
            if let _ = error {
                if let failureHandler = failureHandler {
                    failureHandler()
                }
            } else {
                if let httpResponse = response as? NSHTTPURLResponse {
                    
                    switch httpResponse.statusCode {
                        
                    case 200:
                        
                        dispatch_async(dispatch_get_main_queue()) {
                            self.image = UIImage(data: data!)
                        }
                        if let successHandler = successHandler {
                            successHandler()
                        }
                    default:
                        if let failureHandler = failureHandler {
                            failureHandler()
                        }
                    }
                    
                } else {
                    if let failureHandler = failureHandler {
                        failureHandler()
                    }
                }
            }
            }.resume()
    }
}