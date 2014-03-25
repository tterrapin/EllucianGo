// Copyright 2014 Ellucian Company L.P and its affiliates.
package com.ellucian.mobile.android.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ellucian.mobile.android.login.LoginDialogFragment;

public class UnauthenticatedUserReceiver extends BroadcastReceiver {

	private Activity activity;

	public UnauthenticatedUserReceiver(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void onReceive(Context context, Intent incomingIntent) {
		LoginDialogFragment loginFragment = new LoginDialogFragment();
		loginFragment.queueIntent(activity.getIntent());
		loginFragment.forcedLogin(true);
		loginFragment.show(activity.getFragmentManager(),
				LoginDialogFragment.LOGIN_DIALOG);
	}
}