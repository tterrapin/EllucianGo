package com.ellucian.mobile.android.configuration;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;

public abstract class AbstractModule {

	private String imageUrl;
	public int[] mobileVersions = new int[] { 1 };

	private String name;
	private final List<Integer> versions = new ArrayList<Integer>();
	private boolean enabled;
	private boolean showForGuest ;
	private final List<String> roles = new ArrayList<String>();
	private int index;

	public void addVersion(String version) {
		versions.add(Integer.parseInt(version));
	}

	public abstract Intent buildIntent(Context context);
	public Intent buildService(Context context) {
		return null;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getName() {
		return name;
	}

	public boolean hasSupportedVersion() {
		return true; //TODO
		/*
		for (final int version : mobileVersions) {
			if (versions.contains(version)) {
				return true;
			}
		}
		return false;
		*/
	}

	public void setImageUrl(String defaultImageUrl) {
		this.imageUrl = defaultImageUrl;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public void addRole(String role) {
		roles.add(role);
	}

	public boolean isShowForGuest() {
		return showForGuest;
	}

	public void setShowForGuest(boolean showForGuest) {
		this.showForGuest = showForGuest;
	}

	public void setIndex(int parseInt) {
		this.index = parseInt;
		
	}

	public int getIndex() {
		return index;
	}

	public List<String> getRoles() {
		return roles;
	}

	
}
