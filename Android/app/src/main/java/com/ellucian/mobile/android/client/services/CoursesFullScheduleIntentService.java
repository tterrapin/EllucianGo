/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.services;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.res.Configuration;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.client.courses.CoursesResponse;
import com.ellucian.mobile.android.client.courses.full.FullScheduleBuilder;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.util.Extra;

public class CoursesFullScheduleIntentService extends IntentService {
	private static final String PARAM_OUT_DATABASE_UPDATED = "updated";
	private static final String ACTION_FINISHED = "com.ellucian.mobile.android.client.services.CoursesFullScheduleIntentService.action.updated";
	public CoursesFullScheduleIntentService() {
		super("CoursesFullScheduleIntentService");
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
		stopSelf();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		boolean success = false;
		Log.d("CoursesFullScheduleIntentService", "handling intent");
		MobileClient client = new MobileClient(this);
		client.setDateFormat("yyyy-MM-dd");
		
		String url = client.addUserToUrl(intent.getStringExtra(Extra.COURSES_FULL_URL));
		CoursesResponse response = client.getFullSchedule(url);
		
		if (response != null) {
			Log.d("CoursesFullScheduleIntentService", "Retrieved response from courses client");
			FullScheduleBuilder builder = new FullScheduleBuilder(this);
			Log.d("CoursesFullScheduleIntentService", "Building content provider operations");
			ArrayList<ContentProviderOperation> ops = builder.buildOperations(response);
			Log.d("CoursesFullScheduleIntentService", "Created " + ops.size() + " operations");
			
			if(ops.size() > 0) {
				Log.d("CoursesFullScheduleIntentService", "Executing batch.");
				try {
					getContentResolver().applyBatch(
							EllucianContract.CONTENT_AUTHORITY, ops);
					success = true;
				} catch (RemoteException e) {
					Log.e("CoursesFullScheduleIntentService", "RemoteException applying batch" + e.getLocalizedMessage());
				} catch (OperationApplicationException e) {
					Log.e("CoursesFullScheduleIntentService", "OperationApplicationException applying batch:" + e.getLocalizedMessage());
				}
				Log.d("CoursesFullScheduleIntentService", "Batch executed.");
				
				LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(ACTION_FINISHED);
				broadcastIntent.putExtra(PARAM_OUT_DATABASE_UPDATED, success);
				bm.sendBroadcast(broadcastIntent);
			}
		} else {
			Log.d("CoursesFullScheduleIntentService", "Response Object was null");
		}
		
	}

}
