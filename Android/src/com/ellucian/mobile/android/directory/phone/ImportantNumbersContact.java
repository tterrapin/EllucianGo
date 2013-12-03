package com.ellucian.mobile.android.directory.phone;

public class ImportantNumbersContact {

	private String address;

	private String email;

	private String label;

	private Double latitude = null;

	private Double longitude = null;

	private String name;
	private String phone;

	public String getAddress() {
		return address;
	}

	public String getEmail() {
		return email;
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

	public String getName() {
		return name;
	}

	public String getPhone() {
		return phone;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;

	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Override
	public String toString() {
		return name;
	}

}
