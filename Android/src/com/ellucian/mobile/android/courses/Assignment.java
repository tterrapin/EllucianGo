package com.ellucian.mobile.android.courses;

import java.util.Calendar;

import android.os.Parcel;
import android.os.Parcelable;

public class Assignment implements Parcelable {
	public static final Creator<Assignment> CREATOR = new Parcelable.Creator<Assignment>() {
		public Assignment createFromParcel(Parcel in) {
			return new Assignment(in);
		}

		public Assignment[] newArray(int size) {
			return new Assignment[size];
		}
	};
	private String description;
	private Calendar dueDate;
	private String name;
	private String url;

	public Assignment() {
	}

	public Assignment(Parcel in) {
		description = in.readString();
		final long temp = in.readLong();
		if (temp != 0) {
			dueDate = Calendar.getInstance();
			dueDate.setTimeInMillis(temp);
		}
		name = in.readString();
		url = in.readString();
	}

	public int describeContents() {
		return 0;
	}

	public String getDescription() {
		return description;
	}

	public Calendar getDueDate() {
		return dueDate;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDueDate(Calendar dueDate) {
		this.dueDate = dueDate;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(description);
		dest.writeLong(dueDate != null ? dueDate.getTimeInMillis() : 0);
		dest.writeString(name);
		dest.writeString(url);
	}
}
