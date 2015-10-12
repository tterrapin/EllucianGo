// Copyright 2014 Ellucian Company L.P and its affiliates.
package com.ellucian.mobile.android.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ellucian.mobile.android.EllucianApplication;

public class MainAuthenticationReceiver extends BroadcastReceiver {
	
	private final DrawerLayoutActivity activity;

	public MainAuthenticationReceiver(DrawerLayoutActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onReceive(Context context, Intent incomingIntent) {
		EllucianApplication ellucianApp = (EllucianApplication) context.getApplicationContext();
		ellucianApp.resetModuleMenuAdapter();
		activity.configureNavigationDrawer();
	}		
}