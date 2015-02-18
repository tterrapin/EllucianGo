package com.ellucian.mobile.android.registration;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchFilter implements Parcelable {
	public String name;
	public String code;
	
	public SearchFilter() {
	}
	
	public SearchFilter(Parcel in) { 
		readFromParcel(in);
	}
	
	private void readFromParcel(Parcel in) {
		name = in.readString();
		code = in.readString();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(code);
	}
	
	public static final Parcelable.Creator<SearchFilter> CREATOR = new Parcelable.Creator<SearchFilter>() { 
		public SearchFilter createFromParcel(Parcel in) { 
			return new SearchFilter(in); 
		}   
		
		public SearchFilter[] newArray(int size) { 
			return new SearchFilter[size]; 
		}
	};
}
