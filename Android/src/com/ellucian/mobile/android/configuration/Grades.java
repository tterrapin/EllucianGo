package com.ellucian.mobile.android.configuration;

import android.content.Context;
import android.content.Intent;

import com.ellucian.mobile.android.grades.GradesActivity;
import com.ellucian.mobile.android.grades.GradesService;

public class Grades extends AbstractModule {

	private String url;

	@Override
	public Intent buildIntent(Context context) {
		final Intent intent = new Intent(context, GradesActivity.class);
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
	
	@Override
	public Intent buildService(Context context) {
		final Intent intent = new Intent(context, GradesService.class);
		intent.putExtra("url", this.url);
		intent.putExtra("title", getName());
		intent.setAction(url);  
		return intent;
	}
}
