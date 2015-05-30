//
//  MapsController.swift
//  Mobile
//
//  Created by Jason Hocker on 4/26/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import WatchKit
import Foundation
import CoreData


class MapsController: WKInterfaceController {
    
    @IBOutlet var mapsTable: WKInterfaceTable!
    var campuses : [Dictionary<String, AnyObject>]!
    var internalKey : String?
    var urlString : String?
    
    override func awakeWithContext(context: AnyObject?) {
        let dictionary = context! as! Dictionary<String, AnyObject>
        self.internalKey = dictionary["internalKey"] as? String
        self.setTitle(dictionary["title"] as? String)
        self.urlString = dictionary["campuses"] as? String
        
        fetchMaps()
    }
    
    override func contextForSegueWithIdentifier(segueIdentifier: String, inTable table: WKInterfaceTable, rowIndex: Int) -> AnyObject? {
        if (segueIdentifier == "maps buildings list") {
            return self.campuses![rowIndex]
        }
        return nil
    }
    
    func populateTable() {
        
        mapsTable.setNumberOfRows(self.campuses.count, withRowType: "CampusTableRowController")
                
        for (index, campus) in enumerate(self.campuses) {
            let row = mapsTable.rowControllerAtIndex(index) as! CampusTableRowController
            row.campusNameLabel.setText(campus["name"] as! String!)
        }
    }
    
    func fetchMaps() {
        let infoDictionary = ["action": "fetch maps", "url" : self.urlString!, "internalKey" : self.internalKey!]

        WKInterfaceController.openParentApplication(infoDictionary, reply: { (replyInfo, error) -> Void in
            if let dictionary = replyInfo {
                self.campuses = dictionary["campuses"] as! [[String:AnyObject]]
                dispatch_async(dispatch_get_main_queue(), {
                    self.populateTable()
                })
            }
        })
    }
}
