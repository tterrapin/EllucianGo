//
//  DirectoryEntryViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 12/7/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation
import AddressBook
import AddressBookUI
import Contacts;
import ContactsUI;

class DirectoryEntryViewController : UITableViewController, ABUnknownPersonViewControllerDelegate, CNContactViewControllerDelegate {
    
    var entry : DirectoryEntry?
    var module : Module?
    @IBOutlet var headingWithoutImageCell: UITableViewCell!
    @IBOutlet var headingWithImageCell: UITableViewCell!
    @IBOutlet var emailCell: UITableViewCell!
    @IBOutlet var mobileCell: UITableViewCell!
    @IBOutlet var phoneCell: UITableViewCell!
    @IBOutlet var officeCell: UITableViewCell!
    @IBOutlet var roomCell: UITableViewCell!
    @IBOutlet var addressCell: UITableViewCell!
    
    @IBOutlet var nameInHeaderWithoutImageCellLabel: UILabel!
    @IBOutlet var nameInHeaderWithImageCellLabel: UILabel!
    @IBOutlet var titleInHeaderWithoutImageCellLabel: UILabel!
    @IBOutlet var titleInHeaderWithImageCellLabel: UILabel!
    @IBOutlet var departmentInHeaderWithoutImageCellLabel: UILabel!
    @IBOutlet var departmentInHeaderWithImageCellLabel: UILabel!
    @IBOutlet var emailLabel: UILabel!
    @IBOutlet var mobileLabel: UILabel!
    @IBOutlet var phoneLabel: UILabel!
    @IBOutlet var imageView: UIImageView!
    @IBOutlet weak var moreInfoInHeaderWithoutImageCellButton: UIButton!
    @IBOutlet weak var moreInfoInHeaderWithImageCellButton: UIButton!
    
    @IBOutlet var officeLabel: UILabel!
    @IBOutlet var roomLabel: UILabel!
    @IBOutlet var addressLabel: UILabel!
    
    @IBOutlet var emailIconImageView: UIImageView!
    @IBOutlet var mobileButton: UIButton!
    @IBOutlet var mobileIconImageView: UIImageView!
    @IBOutlet var phoneIconImageView: UIImageView!
    @IBOutlet var addressIconImageView: UIImageView!
    
    @IBOutlet var leftConstraint: NSLayoutConstraint!
    
    var cellsToShow = [UITableViewCell]()
    
    override func viewDidLoad() {
        tableView.rowHeight = UITableViewAutomaticDimension
        tableView.estimatedRowHeight = 160.0
        tableView.tableFooterView = UIView(frame: CGRectZero)
        populateCells()
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        sendView("Directory card", forModuleNamed: self.module?.name)
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        return cellsToShow[indexPath.row]
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return cellsToShow.count
    }
    
    func populateCells() {
        if let entry = entry {
            if entry.imageUrl != nil {
                cellsToShow.append(headingWithImageCell)
                nameInHeaderWithImageCellLabel.text = name()
                titleInHeaderWithImageCellLabel.text = entry.title
                leftConstraint.constant = 68
                departmentInHeaderWithImageCellLabel.text = entry.department
                if let logo = entry.imageUrl where logo != "" {
                    imageView.loadImagefromURL(logo, successHandler: {  self.imageView.convertToCircleImage() }, failureHandler:  { self.imageView.hidden = true
                        dispatch_async(dispatch_get_main_queue()) {
                            () -> Void in
                            self.leftConstraint.constant = 0
                            self.view.setNeedsLayout()
                        }
                    })
                }
                if CurrentUser.sharedInstance().isLoggedIn {
                    moreInfoInHeaderWithImageCellButton.hidden = true
                } else {
                    moreInfoInHeaderWithImageCellButton.tintColor = UIColor.primaryColor()
                }
                
            } else {
                cellsToShow.append(headingWithoutImageCell)
                nameInHeaderWithoutImageCellLabel.text = name()
                titleInHeaderWithoutImageCellLabel.text = entry.title
                departmentInHeaderWithoutImageCellLabel.text = entry.department
                if CurrentUser.sharedInstance().isLoggedIn {
                    moreInfoInHeaderWithoutImageCellButton.hidden = true
                } else {
                    moreInfoInHeaderWithoutImageCellButton.tintColor = UIColor.primaryColor()
                }
            }

            if let email = entry.email where email.characters.count > 0 {
                cellsToShow.append(emailCell)
                emailLabel.text = email
                emailIconImageView.image = emailIconImageView.image!.imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate)
                emailIconImageView.tintColor = UIColor.primaryColor()
            }
            if let mobile = entry.mobile where mobile.characters.count > 0 {
                cellsToShow.append(mobileCell)
                mobileLabel.text = mobile
                mobileIconImageView.image = mobileIconImageView.image!.imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate)
                mobileIconImageView.tintColor = UIColor.primaryColor()

                let image = UIImage(named: "sms-icon")
      
                mobileButton.setImage(image?.imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate), forState: .Normal)
                mobileButton.tintColor = UIColor.primaryColor()
            }
            if let phone = entry.phone where phone.characters.count > 0 {
                cellsToShow.append(phoneCell)
                phoneLabel.text = phone
                phoneIconImageView.image = phoneIconImageView.image!.imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate)
                phoneIconImageView.tintColor = UIColor.primaryColor()
            }
            if let office = entry.office where office.characters.count > 0 {
                cellsToShow.append(officeCell)
                officeLabel.text = office
            }
            if let room = entry.room where room.characters.count > 0 {
                cellsToShow.append(roomCell)
                roomLabel.text = room
            }
            if let address = address() where address.characters.count > 0 {
                cellsToShow.append(addressCell)
                addressLabel.text = address
                addressIconImageView.image = addressIconImageView.image!.imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate)
                addressIconImageView.tintColor = UIColor.primaryColor()
            }
        }
    }
    
    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return UITableViewAutomaticDimension
    }
    
    func name() -> String {
        if let displayName = entry?.displayName where displayName.characters.count > 0 {
            return displayName
        } else {
            if let entry = entry {
                let nameComponents = [entry.prefix, entry.firstName, entry.nickName, entry.middleName, entry.lastName, entry.suffix]
                let flatNameComponents = nameComponents.flatMap { $0 }
                let name = flatNameComponents.joinWithSeparator(" ")
                return name
            } else {
                return ""
            }
        }
    }
    
    func address() -> String? {
        var address : String? = nil
        var csp : String? = nil
        var street : String? = nil
        
        if let entry = self.entry {
            if let city = entry.city, state = entry.state, postalCode = entry.postalCode where city.characters.count > 0 &&
                state.characters.count > 0 && postalCode.characters.count > 0 {
                    csp = "\(city), \(state) \(postalCode)"
            }
            else if let city = entry.city, state = entry.state where city.characters.count > 0 &&
                state.characters.count > 0 {
                    csp = "\(city), \(state)"
            }
            else if let city = entry.city, postalCode = entry.postalCode where city.characters.count > 0 && postalCode.characters.count > 0 {
                csp = "\(city), \(postalCode)"
            }
            else if let state = entry.state, postalCode = entry.postalCode where state.characters.count > 0 && postalCode.characters.count > 0 {
                csp = "\(state) \(postalCode)"
            }
            else if let city = entry.city {
                csp = city
            }
            else if let state = entry.state {
                csp = state
            }
            else if let postalCode = entry.postalCode {
                csp = postalCode
            }
            
            if let eStreet = entry.street, postOfficeCode = entry.postOfficeBox where eStreet.characters.count > 0 && postOfficeCode.characters.count > 0 {
                street = "\(postOfficeCode)\n\(eStreet)"
            } else if let eStreet = entry.street where eStreet.characters.count > 0 {
                street = eStreet
            } else if let postOfficeCode = entry.postOfficeBox where postOfficeCode.characters.count > 0 {
                street = "\(postOfficeCode)"
            }
            
            if let street = street, csp = csp, country = entry.country where street.characters.count > 0 && country.characters.count > 0 {
                address = "\(street)\n\(csp)\n\(country)"
            }
            else if let street = street, csp = csp where street.characters.count > 0 {
                address = "\(street)\n\(csp)"
            }
            else if let street = street, country = entry.country where street.characters.count > 0 && country.characters.count > 0 {
                address = "\(street)\n\(country)"
            }
            else if let csp = csp, country = entry.country {
                address = "\(csp)\n\(country)"
            }
            else if let street = entry.street where street.characters.count > 0 {
                
                address = street
            }
            else if let csp = csp {
                address = csp
            }
            else if let postalCode = entry.postalCode where postalCode.characters.count > 0 {
                address = postalCode
            }
        }
        return address
        
        
        
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        let cell = tableView.cellForRowAtIndexPath(indexPath)
        if cell == emailCell {
            if let email = self.entry?.email {
                self.sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionInvoke_Native, withLabel: "Send e-mail", withValue: nil, forModuleNamed: self.module?.name)
                UIApplication.sharedApplication().openURL(NSURL(string: "mailto://\(email)")!)
            }
            
        } else if cell == mobileCell {
            self.sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionInvoke_Native, withLabel: "Call Phone Number", withValue: nil, forModuleNamed: self.module?.name)
            let phone = self.entry?.mobile?.stringByTrimmingCharactersInSet(NSCharacterSet.init(charactersInString: "() -"))
            if let phone = phone {
                UIApplication.sharedApplication().openURL(NSURL(string: "tel://\(phone)")!)
            }
            
        } else if cell == phoneCell {
            self.sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionInvoke_Native, withLabel: "Call Phone Number", withValue: nil, forModuleNamed: self.module?.name)
            let phone = self.entry?.phone?.stringByTrimmingCharactersInSet(NSCharacterSet.init(charactersInString: "() -"))
            if let phone = phone {
                UIApplication.sharedApplication().openURL(NSURL(string: "tel://\(phone)")!)
            }
        } else if cell == addressCell {
            self.sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionInvoke_Native, withLabel: "Open Maps", withValue: nil, forModuleNamed: self.module?.name)
            if let address = address() {
                
                let geocoder = CLGeocoder()
                geocoder.geocodeAddressString(address, completionHandler: {(placemarks, error) -> Void in
                    if((error) != nil){
                        print("Error", error)
                    }
                    if let placemark = placemarks?.first {
                        let coordinates:CLLocationCoordinate2D = placemark.location!.coordinate
                        let placeMark: MKPlacemark = MKPlacemark(coordinate: coordinates, addressDictionary: nil)
                        let mapItem: MKMapItem = MKMapItem(placemark: placeMark)
                        mapItem.name = self.name()
                        let options = [MKLaunchOptionsDirectionsModeKey: MKLaunchOptionsDirectionsModeDriving]
                        mapItem.openInMapsWithLaunchOptions(options)

                    }
                })
            }
        }
    }
    
    @IBAction func sendText(sender: AnyObject) {
        self.sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionInvoke_Native, withLabel: "Text Phone Number", withValue: nil, forModuleNamed: self.module?.name)
        let phone = self.entry?.mobile?.stringByTrimmingCharactersInSet(NSCharacterSet.init(charactersInString: "() -"))
        if let phone = phone {
            UIApplication.sharedApplication().openURL(NSURL(string: "sms:\(phone)")!)
        }
    }
    
    @IBAction func addToAddressBook(sender: AnyObject) {
        self.sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionInvoke_Native, withLabel: "Add contact", withValue: nil, forModuleNamed: self.module?.name)
        
        if #available(iOS 9, *) {
            let store = CNContactStore()
            
            switch CNContactStore.authorizationStatusForEntityType(.Contacts) {
                // Update our UI if the user has granted access to their Contacts
            case .Authorized:
                self.accessGrantedForContacts()
                
                // Prompt the user for access to Contacts if there is no definitive answer
            case .NotDetermined :
                store.requestAccessForEntityType(.Contacts) {granted, error in
                    if granted {
                        dispatch_async(dispatch_get_main_queue()) {
                            self.accessGrantedForContacts()
                            return
                        }
                    }
                }
            case .Denied, .Restricted:
                accessDeniedForContacts()
            }
            
        } else {
            
            
            let stat = ABAddressBookGetAuthorizationStatus()
            switch stat {
            case .Denied, .Restricted:
                accessDeniedForContacts()
                return
            case .Authorized, .NotDetermined:
                var err : Unmanaged<CFError>? = nil
                let adbk : ABAddressBook? = ABAddressBookCreateWithOptions(nil, &err).takeRetainedValue()
                if adbk == nil {
                    return
                }
                ABAddressBookRequestAccessWithCompletion(adbk) {
                    (granted:Bool, err:CFError!) in
                    if granted {
                        
                        if let entry = self.entry {
                            
                            func createMultiStringRef() -> ABMutableMultiValueRef {
                                let propertyType: NSNumber = kABMultiStringPropertyType
                                return Unmanaged.fromOpaque(ABMultiValueCreateMutable(propertyType.unsignedIntValue).toOpaque()).takeUnretainedValue() as NSObject as ABMultiValueRef
                            }
                            
                            let person:ABRecord = ABPersonCreate().takeRetainedValue()
                            if let firstName = entry.firstName {
                                ABRecordSetValue(person, kABPersonFirstNameProperty, firstName, nil)
                            }
                            if let middleName = entry.middleName {
                                ABRecordSetValue(person, kABPersonMiddleNameProperty, middleName, nil)
                            }
                            if let lastName = entry.lastName {
                                ABRecordSetValue(person, kABPersonLastNameProperty, lastName, nil)
                            }
                            if let office = entry.office {
                                ABRecordSetValue(person, kABPersonOrganizationProperty, office, nil)
                            }
                            if let title = entry.title {
                                ABRecordSetValue(person, kABPersonJobTitleProperty, title, nil)
                            }
                            if let department = entry.department {
                                ABRecordSetValue(person, kABPersonDepartmentProperty, department, nil)
                            }
                            
                            if let prefix = entry.prefix {
                                ABRecordSetValue(person, kABPersonPrefixProperty, prefix, nil)
                            }
                            if let suffix = entry.suffix {
                                ABRecordSetValue(person, kABPersonSuffixProperty, suffix, nil)
                            }
                            let phoneNumbers: ABMutableMultiValueRef = createMultiStringRef()
                            if let mobile = entry.mobile {
                                
                                ABMultiValueAddValueAndLabel(phoneNumbers, mobile, kABPersonPhoneMobileLabel, nil)
                            }
                            if let phone = entry.phone {
                                ABMultiValueAddValueAndLabel(phoneNumbers, phone, kABPersonPhoneMainLabel, nil)
                            }
                            ABRecordSetValue(person, kABPersonPhoneProperty, phoneNumbers, nil)
                            
                            let emails: ABMutableMultiValueRef = createMultiStringRef()
                            if let email = entry.email {
                                
                                ABMultiValueAddValueAndLabel(emails, email, NSLocalizedString("Email", comment: "Email label for address book property"), nil)
                            }
                            ABRecordSetValue(person, kABPersonPhoneProperty, emails, nil)
                            
                            var addressComponents = [String : String]()
                            
                            if let street = entry.street {
                                addressComponents[kABPersonAddressStreetKey as String] =  street
                            }
                            
                            if let city = entry.city {
                                addressComponents[kABPersonAddressCityKey as String] = city
                            }
                            
                            if let state = entry.state {
                                addressComponents[kABPersonAddressStateKey as String] = state
                            }
                            
                            if let postalCode = entry.postalCode {
                                addressComponents[kABPersonAddressZIPKey as String] = postalCode
                            }
                            
                            if let country = entry.country {
                                addressComponents[kABPersonAddressCountryKey as String] = country
                            }
                            
                            if let image = self.imageView.image {
                                let imageData = NSData(data: UIImagePNGRepresentation(image)!)
                                ABPersonSetImageData(person, imageData, nil);
                            }
                            
                            let address: ABMutableMultiValue = ABMultiValueCreateMutable(ABPropertyType(kABMultiStringPropertyType)).takeRetainedValue()
                            ABMultiValueAddValueAndLabel(address, addressComponents, kABHomeLabel, nil)
                            ABRecordSetValue(person, kABPersonAddressProperty, address, nil)
                            
                            let unk = ABUnknownPersonViewController()
                            unk.allowsAddingToAddressBook = true
                            unk.allowsActions = true
                            unk.displayedPerson = person
                            unk.unknownPersonViewDelegate = self
                            
                            dispatch_async(dispatch_get_main_queue(), {
                                self.navigationController?.pushViewController(unk, animated: true)
                            })
                        }
                        
                    } else {
                        return
                    }
                }
            }
        }
    }
    
    func unknownPersonViewController(unknownCardViewController: ABUnknownPersonViewController, didResolveToPerson person: ABRecord?) {
        
    }
    
    func accessDeniedForContacts() {
        let alert = UIAlertController(title: NSLocalizedString("Unable to access contacts", comment: "unable to access contacts alert view title"), message: NSLocalizedString("Unable to access contacts. Go to Settings to grant permission to Ellucian GO", comment: "unable to access contacts alert view message"), preferredStyle: .Alert)
        alert.addAction(UIAlertAction(title: "No", style: .Cancel, handler: nil))
        alert.addAction(UIAlertAction(title: "OK", style: .Default, handler: {
            _ in
            let url = NSURL(string:UIApplicationOpenSettingsURLString)!
            UIApplication.sharedApplication().openURL(url)
        }))
        self.presentViewController(alert, animated:true, completion:nil)
    }
    
    @available(iOS 9, *)
    func accessGrantedForContacts() {
        let contact = CNMutableContact()
        if let entry = self.entry {
            
            if let firstName = entry.firstName {
                contact.givenName = firstName
            }
            if let middleName = entry.middleName {
                contact.middleName = middleName
            }
            if let lastName = entry.lastName {
                contact.familyName = lastName
            }
            if let nickName = entry.nickName {
                contact.nickname = nickName
            }
            if let office = entry.office {
                contact.organizationName = office
            }
            if let title = entry.title {
                contact.jobTitle = title
            }
            if let department = entry.department {
                contact.departmentName = department
            }
            
            if let prefix = entry.prefix {
                contact.namePrefix = prefix
            }
            if let suffix = entry.suffix {
                contact.nameSuffix = suffix
            }
            
            var phoneNumbers = [CNLabeledValue]()
            if let mobile = entry.mobile {
                let mobileLabeledValue = CNLabeledValue(label: CNLabelPhoneNumberMobile, value: CNPhoneNumber(stringValue: mobile))
                phoneNumbers.append(mobileLabeledValue)
            }
            if let phone = entry.phone {
                let phoneLabeledValue = CNLabeledValue(label: CNLabelPhoneNumberMain, value: CNPhoneNumber(stringValue: phone))
                phoneNumbers.append(phoneLabeledValue)
            }
            contact.phoneNumbers = phoneNumbers
            
            var emails = [CNLabeledValue]()
            if let email = entry.email {
                let emailLabeledValue = CNLabeledValue(label: CNLabelOther, value: email)
                emails.append(emailLabeledValue)
            }
            contact.emailAddresses = emails

            let address = CNMutablePostalAddress()
            
            if let street = entry.street {
                address.street =   street
            }
            
            if let city = entry.city {
                address.city = city
            }
            
            if let state = entry.state {
                address.state = state
            }
            
            if let postalCode = entry.postalCode {
                address.postalCode = postalCode
            }
            
            if let country = entry.country {
                address.country = country
            }
            
            contact.postalAddresses = [CNLabeledValue(label: CNLabelOther,
                value: address)]

            if let image = imageView.image {
                contact.imageData = UIImagePNGRepresentation(image)
            }
            
            let ucvc = CNContactViewController(forUnknownContact: contact)
            ucvc.delegate = self
            ucvc.allowsEditing = true
            ucvc.allowsActions = true
            ucvc.alternateName = name()
            ucvc.contactStore = CNContactStore() //needed?
            
            self.navigationController?.pushViewController(ucvc, animated: true)
        }
        
    }
    
    @IBAction func alertSignIn(sender: AnyObject) {
        let alertController = UIAlertController(title: nil, message: NSLocalizedString("More contact information may be displayed if you are signed in.", comment: "Message in directory suggesting to sign in for more information"), preferredStyle: .Alert)

        let OKAction = UIAlertAction(title: "OK", style: .Default) { (action) in
            
        }
        alertController.addAction(OKAction)
        
        self.presentViewController(alertController, animated: true, completion: nil)
    }
}