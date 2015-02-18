package com.ellucian.mobile.android.client.directory;

import java.io.Serializable;

public class Entry implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String type;
	public String id;
	public String displayName;
	public String firstName;
	public String middleName;
	public String lastName;
	public String line1;
	public String line2;
	public String line3;
	public String city;
	public String state;
	public String zip;
	public String country;
	public String number;
	public String email;
	
	
	// Other stuff not in initial json
	public String prefix;
	public String suffix;
	public String title; 
	public String room; //
	public String office;  //
	public String department; //
	public String phone;
	public String mobile; //
	public String street;
	public String postOfficeBox; //
	public String postalCode;
	
	

}
