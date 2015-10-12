// Copyright 2014 Ellucian Company L.P and its affiliates.
package com.ellucian.mobile.android.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.adapter.ModuleMenuAdapter;
import com.ellucian.mobile.android.login.LoginDialogFragment;

import java.util.List;

public class UnauthenticatedUserReceiver extends BroadcastReceiver {

	private final AppCompatActivity activity;
	private final String moduleId;

	public UnauthenticatedUserReceiver(AppCompatActivity activity, String moduleId) {
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
		loginFragment.show(activity.getSupportFragmentManager(),
				LoginDialogFragment.LOGIN_DIALOG);
	}
}