// Copyright 2014 Ellucian Company L.P and its affiliates.
package com.ellucian.mobile.android.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.MainActivity;
import com.ellucian.mobile.android.client.services.ConfigurationUpdateService;
import com.ellucian.mobile.android.util.Utils;

public class ConfigurationUpdateReceiver extends BroadcastReceiver {
	private static final String TAG = ConfigurationUpdateReceiver.class.getSimpleName();
	private final Activity activity;

	public ConfigurationUpdateReceiver(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void onReceive(final Context context, Intent incomingIntent) {
		String tag = activity.getClass().getName();
		Log.d(tag, "onReceive, ConfigurationUpdateReceiver");
        Utils.hideProgressIndicator(activity);

		boolean upgradeAvailable = incomingIntent.getBooleanExtra(
				ConfigurationUpdateService.PARAM_UPGRADE_AVAILABLE, false);
		boolean refresh = incomingIntent.getBooleanExtra(
				ConfigurationUpdateService.REFRESH, false);
		
		if (upgradeAvailable) {

			// launch an Activity to allow the use of an AlertDialog
			Intent i = new Intent(context, ConfigurationUpdateReceiverActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.putExtra("upgradeAvailable",  upgradeAvailable);
			i.putExtra("negativeButtonAction", false);
			context.startActivity(i);
		} else {
			if (refresh) {
                // A refresh happened. Make sure to redraw the drawer menu to reflect any changes.
                try {
                    EllucianActivity ellucianActivity = (EllucianActivity) activity;
                    ellucianActivity.configureNavigationDrawer();
                }
                catch (ClassCastException e) {
                    // Not an EllucianActivity - don't redraw menus.
                    Log.d(TAG, e.getMessage());
                }
            } else {
				Log.d(tag, "logoutUser flag is set");
				
				EllucianApplication application = (EllucianApplication) activity.getApplicationContext();
				// Logging out any user
				application.removeAppUser();
				
				// Make sure to reset the menu adapter so the navigation drawer will 
				// display correctly for a non-authenticated user
                application.resetModuleMenuAdapter();

				Log.d(tag, "Starting MainActivity");
				Intent mainIntent = new Intent(activity, MainActivity.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				activity.startActivity(mainIntent);

            }
		}
	}
}