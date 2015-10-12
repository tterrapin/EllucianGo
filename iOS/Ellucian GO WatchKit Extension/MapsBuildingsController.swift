//
//  MapsBuildingsController.swift
//  Mobile
//
//  Created by Jason Hocker on 4/26/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//


import WatchKit
import Foundation
import CoreData


class MapsBuildingsController: WKInterfaceController {
    
    var poiArray : [Dictionary<String, AnyObject>]!
    
    @IBOutlet var buildingsTable: WKInterfaceTable!
    
    
    override func awakeWithContext(context: AnyObject?) {
        let campus = context as? Dictionary<String, AnyObject>
        
        if let campus = campus {
            setTitle(campus["name"] as? String)
            self.poiArray = campus["buildings"]  as! [Dictionary<String, AnyObject>]
            
            self.poiArray = self.poiArray.sort {
                return ($0["name"] as! String) < ($1["name"] as! String)
             }
            
            buildingsTable.setNumberOfRows(self.poiArray.count, withRowType: "BuildingTableRowController")
            
            for (index, poi) in self.poiArray.enumerate() {
                let row = buildingsTable.rowControllerAtIndex(index) as! BuildingTableRowController
                row.buildingNameLabel.setText(poi["name"] as? String)
            }
        }
    }
    
    override func contextForSegueWithIdentifier(segueIdentifier: String, inTable table: WKInterfaceTable, rowIndex: Int) -> AnyObject? {
        if (segueIdentifier == "maps building detail") {
            return self.poiArray![rowIndex]
        }
        return nil
    }
    
}
