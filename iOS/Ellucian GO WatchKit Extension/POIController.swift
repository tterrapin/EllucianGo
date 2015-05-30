//
//  POIController.swift
//  Mobile
//
//  Created by Jason Hocker on 4/26/15.
//  Copyright (c) 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//


import WatchKit
import Foundation
import CoreData
import MapKit

class POIController: WKInterfaceController {
   
    
    @IBOutlet var nameLabel: WKInterfaceLabel!
    @IBOutlet var addressLabel: WKInterfaceLabel!
    @IBOutlet var descriptionLabel: WKInterfaceLabel!
    
    @IBOutlet var additionalServicesLabel: WKInterfaceLabel!
    @IBOutlet var map: WKInterfaceMap!
    
    
    override func awakeWithContext(context: AnyObject?) {
        let poi = context as! Dictionary<String, AnyObject>
        
        self.nameLabel.setText(poi["name"] as? String)
        self.addressLabel.setText(poi["address"] as? String)
        self.descriptionLabel.setText(poi["description"] as? String)
        self.additionalServicesLabel.setText(poi["additionalServices"] as? String)
        let location = CLLocationCoordinate2D(latitude: poi["latitude"] as! Double, longitude: poi["longitude"] as! Double)
        
        let coordinateSpan = MKCoordinateSpan(latitudeDelta: 0.005, longitudeDelta: 0.005)
        self.map.addAnnotation(location, withPinColor: .Purple)
        self.map.setRegion(MKCoordinateRegion(center: location, span: coordinateSpan))
        
    }
    
}
