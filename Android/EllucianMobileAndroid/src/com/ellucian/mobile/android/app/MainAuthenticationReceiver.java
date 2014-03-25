// Copyright 2014 Ellucian Company L.P and its affiliates.
package com.ellucian.mobile.android.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MainAuthenticationReceiver extends BroadcastReceiver {
	
	private DrawerLayoutActivity activity;

	public MainAuthenticationReceiver(DrawerLayoutActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onReceive(Context context, Intent incomingIntent) {	
		DrawerLayoutHelper drawerLayoutHelper = activity.getDrawerLayoutHelper();
		if(drawerLayoutHelper != null) {
			drawerLayoutHelper.invalidateItems();
		}
	}		
}