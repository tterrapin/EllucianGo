// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.client.registration;

import android.os.Parcel;
import android.os.Parcelable;

public class RegisterSection implements Parcelable {
	public String termId;
	public String sectionId;
	private String action;
	public String courseName;
	public String courseSectionNumber;
	public String courseTitle;
	public Message[] messages;
	
	public RegisterSection() { 
	}
	
	public RegisterSection(Parcel in) { 
		readFromParcel(in);
	}
	
	private void readFromParcel(Parcel in) {
		termId = in.readString();
		sectionId = in.readString();
		action = in.readString();
		courseName = in.readString();
		courseSectionNumber = in.readString();
		courseTitle = in.readString();
		in.readParcelableArray(Message.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(termId);
		dest.writeString(sectionId);
		dest.writeString(action);
		dest.writeString(courseName);
		dest.writeString(courseSectionNumber);
		dest.writeString(courseTitle);
		dest.writeParcelableArray(messages, flags);
		
	}
	
	public static final Parcelable.Creator<RegisterSection> CREATOR = new Parcelable.Creator<RegisterSection>() { 
		public RegisterSection createFromParcel(Parcel in) { 
			return new RegisterSection(in); 
		}   
		
		public RegisterSection[] newArray(int size) { 
			return new RegisterSection[size]; 
		}
	};
}
