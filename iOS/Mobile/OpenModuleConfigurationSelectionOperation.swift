//
//  OpenModuleConfigurationSelectionOperation.swift
//  Mobile
//
//  Created by Jason Hocker on 6/26/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import UIKit

class OpenModuleConfigurationSelectionOperation: OpenModuleAbstractOperation {

    
    override func main() {
        let storyboard = UIStoryboard(name: "ConfigurationSelectionStoryboard", bundle: nil)
        let controller = storyboard.instantiateViewControllerWithIdentifier("ConfigurationSelector")
        controller.modalPresentationStyle = .FullScreen
        showViewController(controller)
        
        //todo is this acceptible that it is "cancelable" to go back to menu?
    }
}
