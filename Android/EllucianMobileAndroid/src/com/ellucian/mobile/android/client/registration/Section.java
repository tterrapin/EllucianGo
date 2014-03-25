// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.client.registration;

import android.os.Parcel;
import android.os.Parcelable;

import com.ellucian.mobile.android.client.courses.Instructor;
import com.ellucian.mobile.android.client.courses.MeetingPattern;

public class Section implements Parcelable {
	public static final String GRADING_TYPE_AUDIT = "Audit";
	public static final String GRADING_TYPE_GRADED = "Graded";
	public static final String GRADING_TYPE_PASS_FAIL = "PassFail";
	
	
	public String termId;
	public String sectionId;
	public String courseId;
	public String sectionTitle;
	public String courseName;
	public String courseDescription;
	public String courseSectionNumber;
	public float credits;
	public float ceus;
	public String status;
	public String gradingType;
	public Instructor[] instructors;
	public MeetingPattern[] meetingPatterns;
	public String classification;
	public int minimumCredits;
	public int maximumCredits;
	public float variableCreditIncrement;
	public boolean allowPassNoPass;
	public boolean allowAudit;
	public boolean onlyPassNoPass;
	
	
	public Section() {
	}
	
	public Section(Parcel in) { 
		readFromParcel(in);
	}
	
	private void readFromParcel(Parcel in) {
		termId = in.readString();
		sectionId = in.readString();
		courseId = in.readString();
		sectionTitle = in.readString();
		courseName = in.readString();
		courseDescription = in.readString();
		courseSectionNumber = in.readString();
		credits = in.readFloat();
		ceus = in.readFloat();
		status = in.readString();
		gradingType = in.readString();
		instructors = in.createTypedArray(Instructor.CREATOR);
		meetingPatterns = in.createTypedArray(MeetingPattern.CREATOR);
		classification = in.readString();
		minimumCredits = in.readInt();
		maximumCredits = in.readInt();
		variableCreditIncrement = in.readFloat();
		allowPassNoPass = in.readInt() == 1 ? true : false;
		allowAudit = in.readInt() == 1 ? true : false;
		onlyPassNoPass = in.readInt() == 1 ? true : false;
		
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(termId);
		dest.writeString(sectionId);
		dest.writeString(courseId);
		dest.writeString(sectionTitle);
		dest.writeString(courseName);
		dest.writeString(courseDescription);
		dest.writeString(courseSectionNumber);
		dest.writeFloat(credits);
		dest.writeFloat(ceus);
		dest.writeString(status);
		dest.writeString(gradingType);
		dest.writeTypedArray(instructors, flags);
		dest.writeTypedArray(meetingPatterns, flags);
		dest.writeString(classification);
		dest.writeInt(minimumCredits);
		dest.writeInt(maximumCredits);
		dest.writeFloat(variableCreditIncrement);
		dest.writeInt(allowPassNoPass ? 1 : 0);
		dest.writeInt(allowAudit ? 1 : 0);
		dest.writeInt(onlyPassNoPass ? 1 : 0);
		
	}
	
	public static final Parcelable.Creator<Section> CREATOR = new Parcelable.Creator<Section>() { 
		public Section createFromParcel(Parcel in) { 
			return new Section(in); 
		}   
		
		public Section[] newArray(int size) { 
			return new Section[size]; 
		}
	}; 
}

