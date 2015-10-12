/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.services;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.client.courses.overview.CourseRosterBuilder;
import com.ellucian.mobile.android.client.courses.overview.CourseRosterResponse;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.util.Extra;

public class CourseRosterIntentService extends IntentService {
	private static final String PARAM_OUT_DATABASE_UPDATED = "updated";
	private static final String ACTION_FINISHED = "com.ellucian.mobile.android.client.services.CourseRosterIntentService.action.updated";
	
	public CourseRosterIntentService() {
		super("CourseRosterIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		boolean success = false;
		Log.d("CourseRosterIntentService", "handling intent");
		MobileClient client = new MobileClient(this);
		
		String url = client.addUserToUrl(intent.getStringExtra(Extra.COURSES_ROSTER_URL));
		url = client.addTermAndSectionToUrl(url, intent.getStringExtra(Extra.COURSES_TERM_ID), intent.getStringExtra(Extra.COURSES_COURSE_ID));
		CourseRosterResponse response = client.getCourseRoster(url);
		
		if (response != null) {
			Log.d("CourseRosterIntentService", "Retrieved response from roster client");
			CourseRosterBuilder builder = new CourseRosterBuilder(this);
			Log.d("CourseGradesIntentService", "Building content provider operations");
			ArrayList<ContentProviderOperation> ops = builder.buildOperations(response);
			Log.d("CourseRosterIntentService", "Created " + ops.size() + " operations");
			
			if(ops.size() > 0) {
				Log.d("CourseRosterIntentService", "Executing batch.");
				try {
					getContentResolver().applyBatch(
							EllucianContract.CONTENT_AUTHORITY, ops);
					success = true;
				} catch (RemoteException e) {
					Log.e("CourseRosterIntentService", "RemoteException applying batch" + e.getLocalizedMessage());
				} catch (OperationApplicationException e) {
					Log.e("CourseRosterIntentService", "OperationApplicationException applying batch:" + e.getLocalizedMessage());
				}
				Log.d("CourseRosterIntentService", "Batch executed.");
				
				LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(ACTION_FINISHED);
				broadcastIntent.putExtra(PARAM_OUT_DATABASE_UPDATED, success);
				bm.sendBroadcast(broadcastIntent);
			}
		} else {
			Log.d("CourseRosterIntentService", "Response Object was null");
		}

	}

}
