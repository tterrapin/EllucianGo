package com.ellucian.mobile.android.configuration;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;

import com.ellucian.mobile.android.directory.DirectoryActivity;

public class Directory extends AbstractModule {

	private String profileUrl;
	private ArrayList<String> searchScopeNames = new ArrayList<String>();
	private ArrayList<String> searchScopeUrls = new ArrayList<String>();
	private ArrayList<String> profileUrls = new ArrayList<String>();

	public String getProfileUrl() {
		return profileUrl;
	}

	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}

	@Override
	public Intent buildIntent(Context context) {
		final Intent intent = new Intent(context, DirectoryActivity.class);
		intent.putExtra("title", getName());
		intent.putStringArrayListExtra("searchScopeNames", searchScopeNames);
		intent.putStringArrayListExtra("searchScopeUrls", searchScopeUrls);
		intent.putStringArrayListExtra("profileUrls", profileUrls);
		return intent;
	}

	public void addSearchScope(String name, String searchUrl, String profileUrl) {
		searchScopeNames.add(name);
		searchScopeUrls.add(searchUrl);
		profileUrls.add(profileUrl);
	}

}
