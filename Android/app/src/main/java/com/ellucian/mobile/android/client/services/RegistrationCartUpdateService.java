/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ellucian.mobile.android.adapter.ModuleMenuAdapter;
import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.util.Extra;

public class RegistrationCartUpdateService extends IntentService {
	private static final String TAG = RegistrationCartUpdateService.class.getSimpleName();
	public static final String ACTION_UPDATE_FINISHED = "com.ellucian.mobile.android.client.RegistrationCartUpdateService.action.update.finished";
	public static final String UPDATE_RESULT = "updateResult";
	public static final String SECTIONS_TO_UPDATE = "sectionsToUpdate";
	
	public RegistrationCartUpdateService() {
		super("RegistrationCartUpdateService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "handling intent");
			
		String sectionsInJson = intent.getStringExtra(SECTIONS_TO_UPDATE);
		
		String requestUrl = intent.getStringExtra(Extra.REQUEST_URL);
		boolean planningTool = intent.getBooleanExtra(ModuleMenuAdapter.PLANNING_TOOL, false);
		
		MobileClient client = new MobileClient(this);
		requestUrl = client.addUserToUrl(requestUrl);
		requestUrl += "/update-cart?planningTool=" + planningTool;
		
		String response = client.putUpdateServerCart(requestUrl, sectionsInJson);
		
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_UPDATE_FINISHED);
		broadcastIntent.putExtra(UPDATE_RESULT, response);
		lbm.sendBroadcast(broadcastIntent);
		
	}


}
