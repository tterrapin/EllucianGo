// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.client.registration;

import android.os.Parcel;
import android.os.Parcelable;

public class RegistrationResponse implements Parcelable {
	public Message[] messages;
	public RegisterSection[] successes;
	public RegisterSection[] failures;
	
	public RegistrationResponse() { 
	}
	
	public RegistrationResponse(Parcel in) { 
		readFromParcel(in);
	}
	
	private void readFromParcel(Parcel in) {   
		in.readParcelableArray(Message.class.getClassLoader());
		in.readParcelableArray(RegisterSection.class.getClassLoader());
		in.readParcelableArray(RegisterSection.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelableArray(messages, flags);
		dest.writeParcelableArray(successes, flags);
		dest.writeParcelableArray(failures, flags);
		
	}
	
	public static final Parcelable.Creator<RegistrationResponse> CREATOR = new Parcelable.Creator<RegistrationResponse>() { 
		public RegistrationResponse createFromParcel(Parcel in) { 
			return new RegistrationResponse(in); 
		}   
		
		public RegistrationResponse[] newArray(int size) { 
			return new RegistrationResponse[size]; 
		}
	};
}
