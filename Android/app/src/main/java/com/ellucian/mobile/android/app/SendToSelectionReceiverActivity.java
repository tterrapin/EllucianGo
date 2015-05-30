/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.schoolselector.SchoolSelectionActivity;
import com.ellucian.mobile.android.util.Utils;

public class SendToSelectionReceiverActivity extends Activity {
	private static final String TAG = SendToSelectionReceiverActivity.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		
		super.onCreate(savedInstanceState);
		AlertDialog.Builder builder = 
				new AlertDialog.Builder(SendToSelectionReceiverActivity.this)
					.setMessage(R.string.configuration_loading_missing_config)
					.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0,
							int arg1) {
						loadSchoolSelectionActivity();
					}
					
					private void loadSchoolSelectionActivity() {
						Log.d(TAG, "launch SchoolSelectionActivity intent");
						Context context = SendToSelectionReceiverActivity.this;
						Log.d("MainActivity", "Starting SchoolSelectionActivity");
						Intent selectionIntent = new Intent(context, SchoolSelectionActivity.class);
						selectionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				        selectionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				        Utils.removeValuesFromPreferences(context, Utils.CONFIGURATION, Utils.CONFIGURATION_URL);
				        
				        context.startActivity(selectionIntent);
					}
				}
		);
		AlertDialog dlg = builder.create();
		Log.d(TAG, "showing alert dialog");
		dlg.show();
		
	}

}
