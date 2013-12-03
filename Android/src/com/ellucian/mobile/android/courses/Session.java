package com.ellucian.mobile.android.courses;

import java.util.Calendar;

import android.os.Parcel;
import android.os.Parcelable;

public class Session implements Parcelable {
	public static final Parcelable.Creator<Session> CREATOR = new Parcelable.Creator<Session>() {
		public Session createFromParcel(Parcel in) {
			return new Session(in);
		}

		public Session[] newArray(int size) {
			return new Session[size];
		}
	};
	private String days; // MWF
	private Calendar endDate;
	private String instructionalMethod; // LEC, LAB
	private Location location;
	private String pattern; // Weekly
	private Calendar startDate;
	private String timeEnd;
	private String timeStart;

	public Session() {
	}

	public Session(Parcel in) {
		days = in.readString();
		instructionalMethod = in.readString();
		location = in.readParcelable(Location.class.getClassLoader());
		pattern = in.readString();
		startDate = Calendar.getInstance();
		startDate.setTimeInMillis(in.readLong());
		endDate = Calendar.getInstance();
		endDate.setTimeInMillis(in.readLong());
		timeStart = in.readString();
		timeEnd = in.readString();
	}

	public int describeContents() {
		return 0;
	}

	public String getDays() {
		return days;
	}

	public Calendar getEndDate() {
		return endDate;
	}

	public String getInstructionalMethod() {
		return instructionalMethod;
	}

	public Location getLocation() {
		return location;
	}

	public String getPattern() {
		return pattern;
	}

	public Calendar getStartDate() {
		return startDate;
	}

	public String getTimeEnd() {
		return timeEnd;
	}

	public String getTimeStart() {
		return timeStart;
	}

	public void setDays(String days) {
		this.days = days;
	}

	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}

	public void setInstructionalMethod(String instructionalMethod) {
		this.instructionalMethod = instructionalMethod;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}

	public void setTimeEnd(String timeEnd) {
		this.timeEnd = timeEnd;
	}

	public void setTimeStart(String timeStart) {
		this.timeStart = timeStart;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(days);
		dest.writeString(instructionalMethod);
		dest.writeParcelable(location, flags);
		dest.writeString(pattern);
		dest.writeLong(startDate.getTimeInMillis());
		dest.writeLong(endDate.getTimeInMillis());
		dest.writeString(timeStart);
		dest.writeString(timeEnd);
	}
}
