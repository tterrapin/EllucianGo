package com.ellucian.mobile.android.courses;

import android.os.Parcel;
import android.os.Parcelable;

public class Faculty implements Parcelable {
	public static final Parcelable.Creator<Faculty> CREATOR = new Parcelable.Creator<Faculty>() {
		public Faculty createFromParcel(Parcel in) {
			return new Faculty(in);
		}

		public Faculty[] newArray(int size) {
			return new Faculty[size];
		}
	};
	private String domain;
	private String name;
	private String username;

	public Faculty() {
	}

	public Faculty(Parcel in) {
		domain = in.readString();
		name = in.readString();
		username = in.readString();
	}

	public int describeContents() {
		return 0;
	}

	public String getDomain() {
		return domain;
	}

	public String getName() {
		return name;
	}

	public String getUsername() {
		return username;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(domain);
		dest.writeString(name);
		dest.writeString(username);
	}
}
