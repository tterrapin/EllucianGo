//
//  CoreDataManager.swift
//  Mobile
//
//  Created by Jason Hocker on 6/22/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation
import CoreData


class CoreDataManager: NSObject {
    
    var _managedObjectContext: NSManagedObjectContext? = nil
    var _managedObjectModel: NSManagedObjectModel? = nil
    var _persistentStoreCoordinator: NSPersistentStoreCoordinator? = nil
    
    class var shared:CoreDataManager{
        get {
            struct Static {
                static var instance : CoreDataManager? = nil
                static var token : dispatch_once_t = 0
            }
            dispatch_once(&Static.token) { Static.instance = CoreDataManager() }
            
            return Static.instance!
        }
    }
    
    
    func initialize(){
        self.managedObjectContext
    }
    
    // MARK: Core Data stack
    
    var managedObjectContext: NSManagedObjectContext{
        
        if NSThread.isMainThread() {
            
            if !(_managedObjectContext != nil) {
                let coordinator = self.persistentStoreCoordinator
                
                _managedObjectContext = NSManagedObjectContext(concurrencyType: .MainQueueConcurrencyType)
                _managedObjectContext!.persistentStoreCoordinator = coordinator
                
                
                return _managedObjectContext!
            }
            
        } else {
            
            var threadContext : NSManagedObjectContext? = NSThread.currentThread().threadDictionary["NSManagedObjectContext"] as? NSManagedObjectContext;
            
            print(NSThread.currentThread().threadDictionary)
            
            if threadContext == nil {
                print("creating new context")
                threadContext = NSManagedObjectContext(concurrencyType: .PrivateQueueConcurrencyType)
                threadContext!.parentContext = _managedObjectContext
                threadContext!.name = NSThread.currentThread().description
                
                NSThread.currentThread().threadDictionary["NSManagedObjectContext"] = threadContext
                
                NSNotificationCenter.defaultCenter().addObserver(self, selector:"contextWillSave:" , name: NSManagedObjectContextWillSaveNotification, object: threadContext)
                
            }else{
                print("using old context")
            }
            return threadContext!;
        }
        
        return _managedObjectContext!
    }
    
    // Returns the managed object model for the application.
    // If the model doesn't already exist, it is created from the application's model.
    var managedObjectModel: NSManagedObjectModel {
        if !(_managedObjectModel != nil) {
            let modelURL = NSBundle.mainBundle().URLForResource("Mobile", withExtension: "momd")
            _managedObjectModel = NSManagedObjectModel(contentsOfURL: modelURL!)
        }
        return _managedObjectModel!
    }
    
    // Returns the persistent store coordinator for the application.
    // If the coordinator doesn't already exist, it is created and the application's store added to it.
    var persistentStoreCoordinator: NSPersistentStoreCoordinator {
        if (_persistentStoreCoordinator != nil) {
            return _persistentStoreCoordinator!
        }
        
        _persistentStoreCoordinator = NSPersistentStoreCoordinator(managedObjectModel: self.managedObjectModel)
        
        let storeURL = AppGroupUtilities.applicationDocumentsDirectory()!.URLByAppendingPathComponent("Mobile.sqlite")
        
        let applicationDocumentsDirectoryOld = NSFileManager.defaultManager().URLsForDirectory(.DocumentDirectory, inDomains: .UserDomainMask).first
        
        let oldStoreURL : NSURL! = applicationDocumentsDirectoryOld?.URLByAppendingPathComponent("Mobile.sqlite")

        
        if NSFileManager.defaultManager().fileExistsAtPath(oldStoreURL.path!) {
            //migrate
            do {
                
                try _persistentStoreCoordinator!.addPersistentStoreWithType(NSSQLiteStoreType, configuration: nil, URL: oldStoreURL, options: self.databaseOptions())
                if let sourceStore = _persistentStoreCoordinator?.persistentStoreForURL(oldStoreURL)
                {
                    do {
                        
                        let destinationStore = try _persistentStoreCoordinator?.migratePersistentStore(sourceStore, toURL: storeURL, options: self.databaseOptions(), withType: NSSQLiteStoreType)
                        
                        if let _ = destinationStore {
                            
                            try NSFileManager.defaultManager().removeItemAtURL(oldStoreURL)
                        }
                    } catch {
                        
                    }
                }
            }
            catch {
                abort()
            }
        } else {
            // no migrate - store normal
            do {
                
                try _persistentStoreCoordinator!.addPersistentStoreWithType(NSSQLiteStoreType, configuration: nil, URL: storeURL, options: self.databaseOptions())
            }
            catch {
                abort()
            }

        }
        
        return _persistentStoreCoordinator!
    }
    
    
    
    // MARK: fetches
    
    func executeFetchRequest(request:NSFetchRequest)-> Array<AnyObject>?{
        
        var results:Array<AnyObject>?
        self.managedObjectContext.performBlockAndWait{
            do {
                results = try self.managedObjectContext.executeFetchRequest(request)
            } catch let error as NSError {
                print("Warning!! \(error.description)")
            } catch {
                fatalError()
            }
        }
        return results
        
    }
    
    
    func executeFetchRequest(request:NSFetchRequest, completionHandler:(results: Array<AnyObject>?) -> Void)-> (){
        
        self.managedObjectContext.performBlock{
            var results:Array<AnyObject>?
            do {
                results = try self.managedObjectContext.executeFetchRequest(request)
            } catch let error as NSError {
                print("Warning!! \(error.description)")
            } catch {
                fatalError()
            }
            completionHandler(results: results)
        }
        
    }
    
    
    
    // MARK: save methods
    
    func save() {
        
        let context:NSManagedObjectContext = self.managedObjectContext;
        if context.hasChanges {
            
            context.performBlockAndWait{
                
                do {
                    try context.save()
                } catch let error as NSError {
                    print("Warning!! Saving error \(error.description)")
                } catch {
                    fatalError()
                }
                
                if let parentContext = context.parentContext {
                    
                    parentContext.performBlockAndWait {
                        do {
                            
                            try parentContext.save()
                            
                        } catch let error as NSError {
                            print("Warning!! Saving parent error \(error.description)")
                        } catch {
                            fatalError()
                        }
                    }
                }
            }
        }
    }
    
    
    
    
    
    func contextWillSave(notification:NSNotification){
        
        let context : NSManagedObjectContext! = notification.object as! NSManagedObjectContext
        let insertedObjects : Set<NSManagedObject> = context.insertedObjects
        
        if insertedObjects.count != 0 {
            
            do {
                try context.obtainPermanentIDsForObjects(Array(insertedObjects))
            } catch let error as NSError {
                print("Warning!! obtaining ids error \(error.description)")
            }
            
        }
        
    }
    
    
    // MARK: Utilities
    
    
    func deleteEntity(object:NSManagedObject)-> () {
        object.managedObjectContext?.deleteObject(object)
    }
    
    
    
    // MARK: Application's Documents directory

    func databaseOptions() -> Dictionary <String,AnyObject> {
        var options =  Dictionary<String,AnyObject>()
        options[NSMigratePersistentStoresAutomaticallyOption] = true
        options[NSInferMappingModelAutomaticallyOption] = true
        options[NSSQLitePragmasOption] = ["journal_mode":"MEMORY"]
        return options
    }
    
    
    
}
