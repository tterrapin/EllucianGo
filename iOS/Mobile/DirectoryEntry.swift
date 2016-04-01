//
//  DirectoryEntry.swift
//  Mobile
//
//  Created by Jason Hocker on 12/3/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation

class DirectoryEntry : NSObject {
    var personId : String?
    var username : String?
    var displayName : String?
    var firstName : String?
    var middleName : String?
    var lastName : String?
    var title : String?
    var office : String?
    var department : String?
    var phone : String?
    var mobile : String?
    var email : String?
    var street : String?
    var room : String?
    var postOfficeBox : String?
    var city : String?
    var state : String?
    var postalCode : String?
    var country : String?
    var prefix : String?
    var suffix : String?
    var imageUrl : String?
    var type : String?
    var nickName : String?

    
    func nameOrderedByFirstName(firstNameFirst: Bool) -> String {
        if let displayName = self.displayName {
            return displayName
        }
        else {
            if let firstName = self.firstName, lastName = self.lastName where firstNameFirst {
                return "\(firstName) \(lastName)"
            }
            else {
                if let firstName = self.firstName, lastName = self.lastName where !firstNameFirst {
                    return "\(lastName), \(firstName)"
                }
                else {
                    if let firstName = self.firstName {
                        return firstName
                    }
                    else {
                        if let lastName = self.lastName {
                            return lastName
                        }
                        else {
                            return ""
                        }
                    }
                }
            }
        }
    }
    
    func firstNameSort() -> String {
        if let firstName = self.firstName {
            return firstName
        } else if let displayName = self.displayName {
            return displayName
        }
        else {
            return ""
        }
    }
    
    func lastNameSort() -> String {
        if let lastName = self.lastName {
            return lastName
        } else if let displayName = self.displayName {
            return displayName
        }
        else {
            return ""
        }
    }
    
    class func parseResponse(responseData : NSData) -> [DirectoryEntry] {
        var entries =  [DirectoryEntry]()

        let json = JSON(data: responseData)
        //create/update objects
        if json["entries"] != JSON.null {
            for entry in json["entries"].array! {
                let directoryEntry = DirectoryEntry()
                if let personId = entry["personId"].string where personId.characters.count > 0 {
                    directoryEntry.personId = personId
                }
                if let username = entry["username"].string where username.characters.count > 0 {
                    directoryEntry.username = username
                }
                if let displayName = entry["displayName"].string where displayName.characters.count > 0 {
                    directoryEntry.displayName = displayName
                }
                if let firstName = entry["firstName"].string where firstName.characters.count > 0 {
                    directoryEntry.firstName = firstName
                }
                if let middleName = entry["middleName"].string where middleName.characters.count > 0 {
                    directoryEntry.middleName = middleName
                }
                if let lastName = entry["lastName"].string where lastName.characters.count > 0 {
                    directoryEntry.lastName = lastName
                }
                if let nickName = entry["nickName"].string where nickName.characters.count > 0 {
                    directoryEntry.nickName = nickName
                }
                if let title = entry["title"].string where title.characters.count > 0 {
                    directoryEntry.title = title
                }
                if let office = entry["office"].string where office.characters.count > 0 {
                    directoryEntry.office = office
                }
                if let department = entry["department"].string where department.characters.count > 0 {
                    directoryEntry.department = department
                }
                if let phone = entry["phone"].string where phone.characters.count > 0 {
                    directoryEntry.phone = phone
                }
                if let mobile = entry["mobile"].string where mobile.characters.count > 0 {
                    directoryEntry.mobile = mobile
                }
                if let email = entry["email"].string where email.characters.count > 0 {
                    directoryEntry.email = email
                }
                if let street = entry["street"].string where street.characters.count > 0 {
                    directoryEntry.street = street.stringByReplacingOccurrencesOfString("\\n", withString: "\n")
                }
                if let room = entry["room"].string where room.characters.count > 0 {
                    directoryEntry.room = room
                }
                if let postOfficeBox = entry["postOfficeBox"].string where postOfficeBox.characters.count > 0 {
                    directoryEntry.postOfficeBox = postOfficeBox
                }
                if let city = entry["city"].string where city.characters.count > 0 {
                    directoryEntry.city = city
                }
                if let state = entry["state"].string where state.characters.count > 0 {
                    directoryEntry.state = state
                }
                if let postalCode = entry["postalCode"].string where postalCode.characters.count > 0 {
                    directoryEntry.postalCode = postalCode
                }
                if let country = entry["country"].string where country.characters.count > 0 {
                    directoryEntry.country = country
                }
                if let prefix = entry["prefix"].string where prefix.characters.count > 0 {
                    directoryEntry.prefix = prefix
                }
                if let suffix = entry["suffix"].string where suffix.characters.count > 0 {
                    directoryEntry.suffix = suffix
                }
                if let type = entry["type"].string where type.characters.count > 0 {
                    directoryEntry.type = type
                }
                if let imageUrl = entry["imageUrl"].string where imageUrl.characters.count > 0 {
                    directoryEntry.imageUrl = imageUrl
                }
                entries.append(directoryEntry)
            }
        }
        return entries
    }
}