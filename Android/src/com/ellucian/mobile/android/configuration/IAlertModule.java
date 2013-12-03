package com.ellucian.mobile.android.configuration;

public interface IAlertModule {

	public String getAlertImageUrl();
	
	public void setAlertImageUrl(String url);
	
	public final static String ACTION = "com.ellucian.mobile.android.AlertBroadcastAction";
}
