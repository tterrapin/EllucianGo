package com.ellucian.mobile.android.courses;

import android.os.Parcel;
import android.os.Parcelable;

public class Location implements Parcelable {
	public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
		public Location createFromParcel(Parcel in) {
			return new Location(in);
		}

		public Location[] newArray(int size) {
			return new Location[size];
		}
	};
	private String buildingLabel;
	private String buildingName;
	private String description;
	private String image;
	private String label;
	private Double latitude;
	private Double longitude;
	private String roomId;

	public Location() {
	}

	public Location(Parcel in) {
		buildingLabel = in.readString();
		String tempLatitude = in.readString();
		if(tempLatitude != null) latitude = Double.parseDouble(tempLatitude);
		String tempLongitude = in.readString();
		if(tempLongitude != null) longitude = Double.parseDouble(tempLongitude);
		buildingName = in.readString();
		description = in.readString();
		image = in.readString();
		label = in.readString();
		roomId = in.readString();
	}

	public int describeContents() {
		return 0;
	}

	public String getBuildingLabel() {
		return buildingLabel;
	}

	public String getBuildingName() {
		return buildingName;
	}

	public String getDescription() {
		return description;
	}

	public String getImage() {
		return image;
	}

	public String getLabel() {
		return label;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setBuildingLabel(String buildingLabel) {
		this.buildingLabel = buildingLabel;
	}

	public void setBuildingName(String buildingName) {
		this.buildingName = buildingName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(buildingLabel);
		dest.writeString(latitude != null ? latitude.toString() : null);
		dest.writeString(longitude != null ? longitude.toString() : null);
		dest.writeString(buildingName);
		dest.writeString(description);
		dest.writeString(image);
		dest.writeString(label);
		dest.writeString(roomId);
	}
}
