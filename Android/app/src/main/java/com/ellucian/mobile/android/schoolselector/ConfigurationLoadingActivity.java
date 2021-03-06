/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.schoolselector;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;

import com.androidquery.AQuery;
import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.client.services.ConfigurationUpdateService;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

import java.util.List;

public class ConfigurationLoadingActivity extends EllucianActivity {
	private static final String TAG = ConfigurationLoadingActivity.class.getSimpleName();
    private final Activity activity = this;

    private UnableToDownloadReceiver unableToDownloadReceiver;
	private String configurationUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_configuration_loading);
        Utils.showProgressIndicator(activity);

        // set the colors directly, not going through preferences
		int primaryColor = Utils.getColorHelper(this, R.color.ellucian_primary_color);
		int headerTextColor = Utils.getColorHelper(this, R.color.ellucian_header_text_color);
		configureActionBarDirect(primaryColor, headerTextColor);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(false);
        bar.setLogo(R.drawable.default_home_logo);

        configurationUrl = getIntent().getStringExtra(
				Utils.CONFIGURATION_URL);
		Uri data = getIntent().getData();
		if (data != null) {
            // See what intent-filter was used. Either a scheme (ie. ellucianmobile://)
            // or a host (mobile.elluciancloud.com) was matched.
            int intStart;
            if (data.getScheme().equals(getResources().getString(R.string.app_intent_filter_scheme))) {
                Log.d(TAG, "Launch config from custom scheme");
                intStart = 0;
            } else {
                // the intent-filter was https://
                Log.d(TAG, "Launch config from matched pathPrefix");
                intStart = 2;
			}
            List<String> segments = data.getPathSegments();
            configurationUrl = TextUtils.join("/",
                    segments.subList(intStart, segments.size()));
            configurationUrl = configurationUrl.replaceFirst("/", "://");
            if(data.getQueryParameter("passcode") != null) {
                configurationUrl += "?passcode="
                        + data.getQueryParameter("passcode");
            }
        }

	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
		unableToDownloadReceiver = new UnableToDownloadReceiver();
		lbm.registerReceiver(unableToDownloadReceiver, new IntentFilter(
				ConfigurationUpdateService.ACTION_UNABLE_TO_DOWNLOAD));
		
		// delete before retrieving the new configuration
		this.getContentResolver().delete(EllucianContract.BASE_CONTENT_URI,
				null, null);

		getSharedPreferences(
				Utils.GOOGLE_ANALYTICS, Context.MODE_PRIVATE).edit().clear().commit();
		
		final SharedPreferences preferences = getSharedPreferences(
				Utils.CONFIGURATION, MODE_PRIVATE);
		final SharedPreferences.Editor editor = preferences.edit();
		// Clear all Configuration Preferences
		editor.clear().commit();
		
		editor.putString(Utils.CONFIGURATION_URL, configurationUrl);
		editor.putString(Utils.CONFIGURATION_NAME,
				getIntent().getStringExtra(Utils.CONFIGURATION_NAME));
		editor.putString(Utils.ID, getIntent().getStringExtra(Utils.ID));
		editor.commit();
		
		Log.d("ConfigurationLoadingActivity",
				"Loading configuration using url: " + configurationUrl);
		Intent intent = new Intent(this, ConfigurationUpdateService.class);
		intent.putExtra(Extra.CONFIG_URL, configurationUrl);

		startService(intent);

	}
	
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(unableToDownloadReceiver);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	public class UnableToDownloadReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent incomingIntent) {
			Log.d(TAG, "onReceive, UnableToDownloadReceiver");

            Utils.hideProgressIndicator(activity);

            Log.d("ConfigurationLoadingActivity.ConfigurationUpdateReceiver",
					"Configuration update failed");
			AQuery aq = new AQuery(ConfigurationLoadingActivity.this);
			aq.id(R.id.configuration_loading_message).text(
					R.string.configuration_loading_failed);
			Utils.removeValuesFromPreferences(ConfigurationLoadingActivity.this, 
					Utils.CONFIGURATION, Utils.CONFIGURATION_URL);
		}

	}

}




