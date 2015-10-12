//
//  FeedSplitViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 7/31/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class GradesSplitViewController : UISplitViewController, UISplitViewControllerDelegate {
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.delegate = self
        let navigationController = self.viewControllers[self.viewControllers.count-1] as! UINavigationController
        navigationController.topViewController!.navigationItem.leftBarButtonItem = self.displayModeButtonItem()
        navigationController.topViewController!.navigationItem.leftItemsSupplementBackButton = true
        self.view.setNeedsLayout()
    }
    
    func splitViewController(splitViewController: UISplitViewController, collapseSecondaryViewController secondaryViewController: UIViewController, ontoPrimaryViewController primaryViewController: UIViewController) -> Bool {
        if let secondaryViewController = secondaryViewController as? UINavigationController {
            let childViewController = secondaryViewController.childViewControllers[0]
            if childViewController.isKindOfClass(GradesTermTableViewController) {
                return false;
            }
        }
        return true;
    }
    
    override internal func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        adjustPreferredDisplayMode()
    }
    
    override internal func viewWillTransitionToSize(size: CGSize, withTransitionCoordinator coordinator: UIViewControllerTransitionCoordinator) {
        adjustPreferredDisplayMode()
        super.viewWillTransitionToSize(size, withTransitionCoordinator: coordinator)
    }
    
    func adjustPreferredDisplayMode() {
        if UIScreen.mainScreen().traitCollection.userInterfaceIdiom == .Pad {
            self.preferredDisplayMode = .AllVisible;
        } else {
            self.preferredDisplayMode = .Automatic;
        }
    }
}