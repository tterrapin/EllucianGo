//
//  MenuTableViewHeaderFooterView.swift
//  Mobile
//
//  Created by Jason Hocker on 7/13/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class MenuTableViewHeaderFooterView : UITableViewHeaderFooterView {
    
    var headerLabel : UILabel?
    var collapsibleButton : UIButton?
    var delegate : SectionHeaderViewDelegate?
    var section : Int?
    
    override init(reuseIdentifier: String?) {
        super.init(reuseIdentifier: reuseIdentifier)
        
        let collapsible = reuseIdentifier == "CollapseableHeader"
        let backgroundColor = UIColor.blackColor()
        if self.backgroundColor != backgroundColor {
            self.contentView.backgroundColor = backgroundColor
            self.contentView.opaque = true
            
            headerLabel = UILabel()
            headerLabel!.font = UIFont.preferredFontForTextStyle(UIFontTextStyleHeadline)
            headerLabel!.textColor = UIColor(red: 179/255, green: 179/255, blue: 179/255, alpha: 1)
            headerLabel!.opaque = false
            self.contentView.addSubview(headerLabel!)
            headerLabel!.translatesAutoresizingMaskIntoConstraints = false
            headerLabel?.accessibilityTraits |= UIAccessibilityTraitHeader

    
            if(collapsible) {
                collapsibleButton = UIButton()
                collapsibleButton!.setImage(UIImage(named:"menu header expanded"), forState: .Normal)
                collapsibleButton!.setImage(UIImage(named:"menu header collapsed"), forState: .Selected)
                collapsibleButton!.addTarget(self, action: Selector("toggleHeader"), forControlEvents: .TouchUpInside)
                self.contentView.addSubview(collapsibleButton!)
                collapsibleButton!.translatesAutoresizingMaskIntoConstraints = false
                collapsibleButton!.accessibilityLabel = NSLocalizedString("Toggle menu section", comment:"Accessibility label for toggle menu section button")
            }

            if AppearanceChanger.isIOS8AndRTL() {
                self.contentView .addConstraint(NSLayoutConstraint(item: headerLabel!, attribute: .Right, relatedBy: .Equal, toItem: self.contentView, attribute: .Left, multiplier: 1.0, constant: 267))
                
                if(collapsible) {
                    self.contentView.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("H:[collapsibleButton]-13-|",
                        options:NSLayoutFormatOptions(rawValue: 0),
                        metrics: nil, views: ["collapsibleButton": collapsibleButton!]))
                }
            } else {
                self.contentView.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("H:|-13-[headerLabel]", options: NSLayoutFormatOptions(rawValue: 0), metrics: nil, views: ["headerLabel" : headerLabel!]))
                
                if(collapsible) {
                    self.contentView.addConstraint(NSLayoutConstraint(item: collapsibleButton!, attribute: .Trailing, relatedBy: .Equal, toItem: self.contentView, attribute: .Leading, multiplier: 1.0, constant: 267.0))
                }
            }
            
            self.contentView.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("V:|[headerLabel]|", options: NSLayoutFormatOptions(rawValue: 0), metrics: nil, views: ["headerLabel": headerLabel!]))
            
            if(collapsible) {
                self.contentView.addConstraint(NSLayoutConstraint(item: collapsibleButton!, attribute: .CenterY, relatedBy: .Equal, toItem: headerLabel!, attribute: .CenterY, multiplier: 1.0, constant: 0))
            }
        }
        
        if(collapsible) {
            let tapGestureRecognizer = UITapGestureRecognizer(target: self, action: Selector("toggleHeader"))
            self.addGestureRecognizer(tapGestureRecognizer)
        }

    }
    
    required init?(coder aDecoder: NSCoder)
    {
        fatalError("init(coder:) has not been implemented")
    }
    
    func toggleHeader() {
        self.collapsibleButton!.selected = !self.collapsibleButton!.selected
        
        if self.collapsibleButton!.selected {
            self.delegate?.sectionHeaderView!(self, sectionClosed: self.section!)
        } else {
            self.delegate?.sectionHeaderView!(self, sectionOpened: self.section!)
        }
    }
}

