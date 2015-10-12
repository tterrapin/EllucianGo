// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.client.registration;

import android.os.Parcel;
import android.os.Parcelable;

import com.ellucian.mobile.android.client.ResponseObject;

public class CartResponse implements ResponseObject<CartResponse>, Parcelable {
	public Plan [] plans;
	
	public CartResponse() { 
	}
	
	public CartResponse(Parcel in) { 
		readFromParcel(in);
	}
	
	private void readFromParcel(Parcel in) {   
		in.readParcelableArray(Plan.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelableArray(plans, flags);
		
	}
	
	public static final Parcelable.Creator<CartResponse> CREATOR = new Parcelable.Creator<CartResponse>() { 
		public CartResponse createFromParcel(Parcel in) { 
			return new CartResponse(in); 
		}   
		
		public CartResponse[] newArray(int size) { 
			return new CartResponse[size]; 
		}
	};
}
