/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.MainActivity;
import com.ellucian.mobile.android.util.Utils;

public class ConfigurationUpdateReceiverActivity extends Activity {
	private static final String TAG = ConfigurationUpdateReceiverActivity.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		boolean negativeButtonAction = extras.getBoolean("negativeButtonAction");
			
		Log.d(TAG, "upgrade is available");
		AlertDialog.Builder builder = new AlertDialog.Builder(ConfigurationUpdateReceiverActivity.this);

		builder.setTitle(R.string.version_outdated);
		builder.setMessage(R.string.version_outdated_message);

		if (negativeButtonAction) {
			Log.d(TAG, "negativeButtonAction flag = true");
			builder.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// Logging out any user
					((EllucianApplication) getApplication())
					.removeAppUser();
					loadMainActivity();

				}

			});

		} else {
			Log.d(TAG, "no negativeButtonAction set");
			builder.setNegativeButton(android.R.string.cancel, null);
		}

		builder.setPositiveButton(R.string.version_upgrade,
				new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				String packageName = getApplicationContext().getPackageName();
				Intent marketIntent = new Intent(Intent.ACTION_VIEW);
				marketIntent.setData(Uri.parse("market://details?id="+ packageName));
				if (Utils.isIntentAvailable(getApplicationContext(),marketIntent)) {

					marketIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					marketIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(marketIntent);
					finish();
				} else {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri
							.parse("http://play.google.com/store/apps/details?id="
									+ packageName));
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
			}
		});
		AlertDialog dlg = builder.create();
		Log.d(TAG, "showing alert dialog");
		dlg.show();

		 
	}
	
	private void loadMainActivity() {
		Log.d(TAG, "Starting MainActivity");
		Intent mainIntent = new Intent(this, MainActivity.class);
		mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ConfigurationUpdateReceiverActivity.this.startActivity(mainIntent);
	}


}
