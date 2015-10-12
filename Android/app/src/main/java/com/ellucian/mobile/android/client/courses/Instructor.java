/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.courses;

import android.os.Parcel;
import android.os.Parcelable;

public class Instructor implements Parcelable {
	public String firstName;
	public String lastName;
	public String middleInitial;
	public String instructorId;
	public boolean primary;
	public String formattedName;
	
	public Instructor() {
	}
	
	private Instructor(Parcel in) {
		readFromParcel(in);
	}
	
	private void readFromParcel(Parcel in) {
		firstName = in.readString();
		lastName = in.readString();
		middleInitial = in.readString();
		instructorId = in.readString();
		int booleanValue = in.readInt();
		primary = booleanValue == 1 ? true : false;
		formattedName = in.readString();

	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(firstName);
		dest.writeString(lastName);
		dest.writeString(middleInitial);
		dest.writeString(instructorId);
		dest.writeInt(primary ? 1 : 0);
		dest.writeString(formattedName);

		
	}
	
	public static final Parcelable.Creator<Instructor> CREATOR = new Parcelable.Creator<Instructor>() { 
		public Instructor createFromParcel(Parcel in) { 
			return new Instructor(in); 
		}   
		
		public Instructor[] newArray(int size) { 
			return new Instructor[size]; 
		}
	}; 
}