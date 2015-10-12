// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.client.registration;

import android.os.Parcel;
import android.os.Parcelable;

import com.ellucian.mobile.android.client.ResponseObject;

public class SearchResponse implements ResponseObject<SearchResponse>, Parcelable {
	public Section[] sections;
	
	public SearchResponse() { 
	}
	
	public SearchResponse(Parcel in) { 
		readFromParcel(in);
	}
	
	private void readFromParcel(Parcel in) {   
		in.readParcelableArray(Section.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelableArray(sections, flags);
		
	}
	
	public static final Parcelable.Creator<SearchResponse> CREATOR = new Parcelable.Creator<SearchResponse>() { 
		public SearchResponse createFromParcel(Parcel in) { 
			return new SearchResponse(in); 
		}   
		
		public SearchResponse[] newArray(int size) { 
			return new SearchResponse[size]; 
		}
	};
}
