// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.client.registration;

import android.os.Parcel;
import android.os.Parcelable;

public class Term implements Parcelable {
	public String termId;
	public String name;
	public String startDate;
	public String endDate;
	public Section[] plannedCourses;
	
	public Term() {
	}
	
	public Term(Parcel in) { 
		readFromParcel(in);
	}
	
	private void readFromParcel(Parcel in) { 
		termId = in.readString();
		name = in.readString();
		startDate = in.readString();
		endDate = in.readString();
		in.readParcelableArray(Section.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(termId);
		dest.writeString(name);
		dest.writeString(startDate);
		dest.writeString(endDate);
		dest.writeParcelableArray(plannedCourses, flags);
		
	}
	
	public static final Parcelable.Creator<Term> CREATOR = new Parcelable.Creator<Term>() { 
		public Term createFromParcel(Parcel in) { 
			return new Term(in); 
		}   
		
		public Term[] newArray(int size) { 
			return new Term[size]; 
		}
	}; 
}


