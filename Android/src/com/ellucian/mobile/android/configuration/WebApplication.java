package com.ellucian.mobile.android.configuration;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class WebApplication extends AbstractModule {

	private String url;

	@Override
	public Intent buildIntent(Context context) {
		try {
			Uri uri = Uri.parse(url );
			return new Intent( Intent.ACTION_VIEW, uri );
		} catch (Exception e) {
			return null;
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
