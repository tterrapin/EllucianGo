package com.ellucian.mobile.android.client.courses;

import android.os.Parcel;
import android.os.Parcelable;

public class MeetingPattern implements Parcelable {
	public String instructionalMethodCode;
	public String startDate;
	public String endDate;
	public String startTime;
	public String endTime;
	public int[] daysOfWeek;
	public String room;
	public String frequency;
	public String building;
	public String buildingId;
	public String campusId;
	
	public MeetingPattern() {
	}
	
	public MeetingPattern(Parcel in) { 
		readFromParcel(in);
	}
	
	private void readFromParcel(Parcel in) {
		instructionalMethodCode = in.readString();
		startDate = in.readString();
		endDate = in.readString();
		startTime = in.readString();
		endTime = in.readString();	
		daysOfWeek = in.createIntArray();
		room = in.readString();
		frequency = in.readString();
		building = in.readString();
		buildingId = in.readString();
		campusId = in.readString();
		

	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(instructionalMethodCode);
		dest.writeString(startDate);
		dest.writeString(endDate);
		dest.writeString(startTime);
		dest.writeString(endTime);
		dest.writeIntArray(daysOfWeek);
		dest.writeString(room);
		dest.writeString(frequency);
		dest.writeString(building);
		dest.writeString(buildingId);
		dest.writeString(campusId);
		
	}
	
	public static final Parcelable.Creator<MeetingPattern> CREATOR = new Parcelable.Creator<MeetingPattern>() { 
		public MeetingPattern createFromParcel(Parcel in) { 
			return new MeetingPattern(in); 
		}   
		
		public MeetingPattern[] newArray(int size) { 
			return new MeetingPattern[size]; 
		}
	}; 
}