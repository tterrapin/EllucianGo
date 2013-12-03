package com.ellucian.mobile.android.configuration;

import android.content.Context;
import android.content.Intent;

import com.ellucian.mobile.android.events.EventsActivity;

public class Events extends AbstractModule {

	private String authenticatedUrl;

	private String publicUrl;

	@Override
	public Intent buildIntent(Context context) {

		final Intent intent = new Intent(context, EventsActivity.class);
		intent.putExtra("publicUrl", this.publicUrl);
		intent.putExtra("authenticatedUrl", this.authenticatedUrl);
		intent.putExtra("title", getName());
		return intent;
	}

	public void setAuthenticatedUrl(String currentValue) {
		this.authenticatedUrl = currentValue;

	}

	public void setPublicUrl(String currentValue) {
		this.publicUrl = currentValue;

	}
}
