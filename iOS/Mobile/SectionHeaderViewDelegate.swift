//
//  SectionHeaderViewDelegate.swift
//  Mobile
//
//  Created by Jason Hocker on 7/13/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

@objc protocol SectionHeaderViewDelegate {
    
    optional func sectionHeaderView(sectionHeaderView: MenuTableViewHeaderFooterView, sectionOpened: Int)
    optional func sectionHeaderView(sectionHeaderView: MenuTableViewHeaderFooterView, sectionClosed: Int)

}
