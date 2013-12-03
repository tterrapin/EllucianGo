package com.ellucian.mobile.android.configuration;

import android.content.Context;
import android.content.Intent;

import com.ellucian.mobile.android.map.MapsActivity;

public class Maps extends AbstractModule {

	private String url;

	@Override
	public Intent buildIntent(Context context) {
		final Intent intent = new Intent(context, MapsActivity.class);
		intent.putExtra("url", this.url);
		intent.putExtra("title", getName());
		return intent;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
