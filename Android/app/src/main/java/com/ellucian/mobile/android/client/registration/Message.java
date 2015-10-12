// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.client.registration;

import android.os.Parcel;
import android.os.Parcelable;

public class Message implements Parcelable{
	public String message;
	private String courseName;
	private String courseSectionNumber;
	
	public Message() { 
	}
	
	public Message(Parcel in) { 
		readFromParcel(in);
	}
	
	private void readFromParcel(Parcel in) {
		message = in.readString();
		courseName = in.readString();
		courseSectionNumber = in.readString();

	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(message);
		dest.writeString(courseName);
		dest.writeString(courseSectionNumber);
		
	}
	
	public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() { 
		public Message createFromParcel(Parcel in) { 
			return new Message(in); 
		}   
		
		public Message[] newArray(int size) { 
			return new Message[size]; 
		}
	};
}
