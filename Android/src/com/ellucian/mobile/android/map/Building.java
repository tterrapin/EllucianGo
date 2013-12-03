package com.ellucian.mobile.android.map;

import com.google.android.maps.GeoPoint;

public class Building {

	private String address;

	private String description;

	private GeoPoint geoPoint;

	private String imageUrl;
	private String label;
	private String name;

	private String type;

	public String getAddress() {
		return address;
	}

	public String getDescription() {
		return description;
	}

	public GeoPoint getGeoPoint() {
		return geoPoint;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getLabel() {
		return label;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setGeoPoint(GeoPoint geoPoint) {
		this.geoPoint = geoPoint;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return name;
	}

}
