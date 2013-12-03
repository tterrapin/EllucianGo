package com.ellucian.mobile.android.directory;

public class Profile {

	private String department;
	private String firstName;
	private String imageUrl;
	private String lastName;
	private String mobilePhone;
	private String office;
	private boolean optOut;
	private String preferredName;
	private String title;
	private String workPhone;
	private String email;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDepartment() {
		return department;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getLastName() {
		return lastName;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public String getOffice() {
		return office;
	}

	public String getPreferredName() {
		return preferredName;
	}

	public String getTitle() {
		return title;
	}

	public String getWorkPhone() {
		return workPhone;
	}

	public boolean isOptOut() {
		return optOut;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	public void setOffice(String office) {
		this.office = office;
	}

	public void setOptOut(boolean optOut) {
		this.optOut = optOut;
	}

	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setWorkPhone(String workPhone) {
		this.workPhone = workPhone;
	}
}
