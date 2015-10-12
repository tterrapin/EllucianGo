/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.ellucian.mobile.android.adapter.ModuleMenuAdapter;
import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.util.Extra;

public class RegisterService extends IntentService {
	private static final String TAG = RegisterService.class.getSimpleName();
	public static final String ACTION_REGISTER_FINISHED = "com.ellucian.mobile.android.client.RegisterService.action.register.finished";
	public static final String ACTION_DROP_FINISHED = "com.ellucian.mobile.android.client.RegisterService.action.drop.finished";
	public static final String REGISTRATION_RESULT = "registrationResult";
	public static final String PLAN_TO_REGISTER = "planToRegister";
	public static final String REGISTER_TYPE = "registerType";
	public static final String TYPE_REGISTER = "typeRegister";
	public static final String TYPE_DROP = "typeDrop";
	
	public RegisterService() {
		super("RegisterService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "handling intent");
			
		String planInJson = intent.getStringExtra(PLAN_TO_REGISTER);
		String registerType = intent.getStringExtra(REGISTER_TYPE);
		
		String requestUrl = intent.getStringExtra(Extra.REQUEST_URL);
		boolean planningTool = intent.getBooleanExtra(ModuleMenuAdapter.PLANNING_TOOL, false);
		MobileClient client = new MobileClient(this);
		requestUrl = client.addUserToUrl(requestUrl);
		requestUrl += "/register-sections?planningTool=" + planningTool;
		
		String response = client.putCoursesToRegister(requestUrl, planInJson);
		
		String action = ACTION_REGISTER_FINISHED;
		if (!TextUtils.isEmpty(registerType) && registerType.equals(TYPE_DROP)) {
			action = ACTION_DROP_FINISHED;
		}
		
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(action);
		broadcastIntent.putExtra(REGISTRATION_RESULT, response);
		lbm.sendBroadcast(broadcastIntent);
		
	}


}
