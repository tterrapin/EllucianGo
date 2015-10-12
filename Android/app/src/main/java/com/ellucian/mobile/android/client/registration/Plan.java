// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.client.registration;

import android.os.Parcel;
import android.os.Parcelable;

public class Plan implements Parcelable {
	public String planId;
	private String studentId;
	public Term[] terms;
	
	public Plan() {
	}
	
	public Plan(Parcel in) { 
		readFromParcel(in);
	}
	
	private void readFromParcel(Parcel in) { 
		planId = in.readString();
		studentId = in.readString();
		in.readParcelableArray(Term.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(planId);
		dest.writeString(studentId);
		dest.writeParcelableArray(terms, flags);
		
	}
	
	public static final Parcelable.Creator<Plan> CREATOR = new Parcelable.Creator<Plan>() { 
		public Plan createFromParcel(Parcel in) { 
			return new Plan(in); 
		}   
		
		public Plan[] newArray(int size) { 
			return new Plan[size]; 
		}
	}; 
}
