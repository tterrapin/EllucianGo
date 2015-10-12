// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.client.registration;

import android.os.Parcel;
import android.os.Parcelable;

public class OpenTerm implements Parcelable {
	public String id;
	public String name;
	public String startDate;
	public String endDate;
	
	public OpenTerm() {
	}
	
	private OpenTerm(Parcel in) {
		readFromParcel(in);
	}
	
	private void readFromParcel(Parcel in) { 
		id = in.readString();
		name = in.readString();
		startDate = in.readString();
		endDate = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(startDate);
		dest.writeString(endDate);
		
	}
	
	public static final Parcelable.Creator<OpenTerm> CREATOR = new Parcelable.Creator<OpenTerm>() { 
		public OpenTerm createFromParcel(Parcel in) { 
			return new OpenTerm(in); 
		}   
		
		public OpenTerm[] newArray(int size) { 
			return new OpenTerm[size]; 
		}
	};
}
