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
import com.ellucian.mobile.android.util.Utils;

public class OutdatedReceiverActivity extends Activity {
	private static final String TAG = OutdatedReceiverActivity.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AlertDialog.Builder builder = new AlertDialog.Builder(OutdatedReceiverActivity.this)
			.setTitle(R.string.version_upgrade)
			.setMessage(R.string.version_force_upgrade_message)
			.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							Log.v("MainActivity", "User canceled force upgrade");
							finish();
							android.os.Process.killProcess(android.os.Process.myPid());
						}

					})
			.setPositiveButton(R.string.version_upgrade,
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
							intent.setData(Uri.parse("http://play.google.com/store/apps/details?id="+ packageName));
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
						}
					}
				}
			);
		AlertDialog dlg = builder.create();
		Log.d(TAG, "showing alert dialog");
		dlg.show();
	}
	
}
