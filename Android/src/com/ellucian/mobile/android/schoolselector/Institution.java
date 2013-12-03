package com.ellucian.mobile.android.schoolselector;

import java.util.ArrayList;

public class Institution implements Comparable<Institution> {
	private String configUrl;
	private String displayName;
	private String fullName;
	private ArrayList<String> keywords = new ArrayList<String>();

	private String uniqueId;

	public void addKeyword(String keyword) {
		this.keywords.add(keyword);

	}

	public int compareTo(Institution another) {
		return this.getFullName().compareTo(another.getFullName());
	}

	public String getConfigUrl() {
		return configUrl;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getFullName() {
		return fullName;
	}

	public ArrayList<String> getKeywords() {
		return keywords;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setConfigUrl(String configUrl) {
		this.configUrl = configUrl;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setKeywords(ArrayList<String> keywords) {
		this.keywords = keywords;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	@Override
	public String toString() {
		return fullName;
	}
}
