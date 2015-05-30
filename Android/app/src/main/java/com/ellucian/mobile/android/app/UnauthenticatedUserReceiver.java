// Copyright 2014 Ellucian Company L.P and its affiliates.
package com.ellucian.mobile.android.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.adapter.ModuleMenuAdapter;
import com.ellucian.mobile.android.login.LoginDialogFragment;

import java.util.List;

public class UnauthenticatedUserReceiver extends BroadcastReceiver {

	private Activity activity;
	private String moduleId;

	public UnauthenticatedUserReceiver(Activity activity, String moduleId) {
		this.activity = activity;
		this.moduleId = moduleId;
	}

	@Override
	public void onReceive(Context context, Intent incomingIntent) {
        EllucianApplication ellucianApplication = (EllucianApplication) activity.getApplicationContext();
        ellucianApplication.removeAppUser();

        LoginDialogFragment loginFragment = new LoginDialogFragment();
		List<String> roles = null;
		if(moduleId != null) {
			roles = ModuleMenuAdapter.getModuleRoles(context.getContentResolver(), moduleId);
		}
		loginFragment.queueIntent(activity.getIntent(), roles);
		loginFragment.forcedLogin(true);
		loginFragment.show(activity.getFragmentManager(),
				LoginDialogFragment.LOGIN_DIALOG);
	}
}