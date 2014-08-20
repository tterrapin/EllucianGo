package com.ellucian.mobile.android.client.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ellucian.mobile.android.adapter.ModuleMenuAdapter;
import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.util.Extra;

public class RegisterService extends IntentService {
	public static final String TAG = RegisterService.class.getSimpleName();
	public static final String ACTION_REGISTER_FINISHED = "com.ellucian.mobile.android.client.RegisterService.action.register.finished";
	public static final String REGISTRATION_RESULT = "registrationResult";
	public static final String PLAN_TO_REGISTER = "planToRegister";
	
	public RegisterService() {
		super("RegisterService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "handling intent");
			
		String planInJson = intent.getStringExtra(PLAN_TO_REGISTER);
		
		String requestUrl = intent.getStringExtra(Extra.REQUEST_URL);
		boolean planningTool = intent.getBooleanExtra(ModuleMenuAdapter.PLANNING_TOOL, false);
		MobileClient client = new MobileClient(this);
		requestUrl = client.addUserToUrl(requestUrl);
		requestUrl += "/register-sections?planningTool=" + planningTool;
		
		String response = client.putCoursesToRegister(requestUrl, planInJson);
		
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_REGISTER_FINISHED);
		broadcastIntent.putExtra(REGISTRATION_RESULT, response);
		lbm.sendBroadcast(broadcastIntent);
		
	}


}
