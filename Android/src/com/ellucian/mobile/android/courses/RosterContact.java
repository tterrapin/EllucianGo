package com.ellucian.mobile.android.courses;

import android.os.Parcel;
import android.os.Parcelable;

public class RosterContact implements Parcelable {
	public static final Parcelable.Creator<RosterContact> CREATOR = new Parcelable.Creator<RosterContact>() {
		public RosterContact createFromParcel(Parcel source) {
			return new RosterContact(source);
		}

		public RosterContact[] newArray(int size) {
			return new RosterContact[size];
		}
	};
	private String domain;
	private String name;
	private String username;

	public RosterContact() {
	}

	public RosterContact(Parcel in) {
		name = in.readString();
		domain = in.readString();
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

	public void setName(String formattedName) {
		this.name = formattedName;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(domain);
		dest.writeString(username);
	}
}
