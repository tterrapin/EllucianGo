//
//  MapsFetchOperation.swift
//  Mobile
//
//  Created by Bret Hansen on 9/11/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class MapsFetchOperation: NSOperation {
    private let internalKey: String
    private let url: String
    
    var campuses: [[String: AnyObject]] = []
    
    init(internalKey: String, url: String) {
        self.internalKey = internalKey
        self.url = url
    }
    
    override func main() {
        
        // load maps data from server
        MapsFetcher.fetch(CoreDataManager.shared.managedObjectContext, withURL: url, moduleKey: internalKey)
        
        let request = NSFetchRequest(entityName: "Map")
        request.predicate = NSPredicate(format: "moduleName = %@", internalKey)
        request.sortDescriptors = [NSSortDescriptor(key: "moduleName", ascending: true)]
        
        do {
            let maps = try CoreDataManager.shared.managedObjectContext.executeFetchRequest(request)
            for map in maps as! [Map] {
                for campus in map.campuses as! Set<MapCampus> {
                    var pois: [[String: AnyObject]] = []
                    for poi in campus.points as! Set<MapPOI> {
                        var poiDictionary: [String: AnyObject] = [
                            "name": poi.name
                        ]
                        
                        if (poi.additionalServices != nil) {
                            poiDictionary["additionalServices"] = poi.additionalServices
                        }
                        if (poi.address != nil) {
                            poiDictionary["address"] = poi.address
                        }
                        if (poi.description_ != nil) {
                            poiDictionary["description"] = poi.description_
                        }
                        if (poi.latitude != nil) {
                            poiDictionary["latitude"] = poi.latitude
                        }
                        if (poi.longitude != nil) {
                            poiDictionary["longitude"] = poi.longitude
                        }
                        
                        pois.append(poiDictionary)
                    }
                    campuses.append( [
                        "name": campus.name,
                        "buildings": pois
                    ])
                }
            }
        } catch {
            NSLog("Unable to query for Maps")
        }
    }
}