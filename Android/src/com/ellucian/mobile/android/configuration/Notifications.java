package com.ellucian.mobile.android.configuration;

import android.content.Context;
import android.content.Intent;

import com.ellucian.mobile.android.notifications.NotificationsActivity;
import com.ellucian.mobile.android.notifications.NotificationsService;

public class Notifications extends AbstractModule implements IAlertModule {

	private String url;
	private String alertImageUrl;

	@Override
	public Intent buildIntent(Context context) {
		final Intent intent = new Intent(context, NotificationsActivity.class);
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
		final Intent intent = new Intent(context, NotificationsService.class);
		intent.putExtra("url", this.url);
		intent.putExtra("title", getName());
		intent.putExtra("network", true);
		intent.putExtra("markRead", false);
		intent.setAction(url);  
		return intent;
	}

	public String getAlertImageUrl() {
		return alertImageUrl != null ? alertImageUrl : getImageUrl();
	}

	public void setAlertImageUrl(String defaultImageUrl) {
		this.alertImageUrl = defaultImageUrl;
	}
}
