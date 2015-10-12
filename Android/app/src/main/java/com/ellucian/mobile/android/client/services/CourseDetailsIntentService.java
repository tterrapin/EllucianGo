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
import com.ellucian.mobile.android.client.courses.CoursesResponse;
import com.ellucian.mobile.android.client.courses.overview.CourseDetailsBuilder;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.util.Extra;

public class CourseDetailsIntentService extends IntentService {
	private final String TAG = getClass().getSimpleName();
	
	private static final String PARAM_OUT_DATABASE_UPDATED = "updated";
	private static final String ACTION_FINISHED = "com.ellucian.mobile.android.client.services.CourseDetailsIntentService.action.updated";
	public CourseDetailsIntentService() {
		super("CourseDetailsIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		boolean success = false;
		Log.d(TAG, "handling intent");
		MobileClient client = new MobileClient(this);
		client.setDateFormat("yyyy-MM-dd");
		
		String url = client.addUserToUrl(intent.getStringExtra(Extra.COURSES_DETAILS_URL));
		url = client.addTermAndSectionToUrl(url, intent.getStringExtra(Extra.COURSES_TERM_ID), intent.getStringExtra(Extra.COURSES_COURSE_ID));
		CoursesResponse response = client.getCourseDetails(url);
		
		if (response != null) {
			Log.d(TAG, "Retrieved response from courses client");
			CourseDetailsBuilder builder = new CourseDetailsBuilder(this);
			Log.d("CourseDetailsIntentService", "Building content provider operations");
			ArrayList<ContentProviderOperation> ops = builder.buildOperations(response);
			Log.d("CourseDetailsIntentService", "Created " + ops.size() + " operations");
			
			if(ops.size() > 0) {
				Log.d("CourseDetailsIntentService", "Executing batch.");
				try {
					getContentResolver().applyBatch(
							EllucianContract.CONTENT_AUTHORITY, ops);
					success = true;
				} catch (RemoteException e) {
					Log.e("CourseDetailsIntentService", "RemoteException applying batch" + e.getLocalizedMessage());
				} catch (OperationApplicationException e) {
					Log.e("CourseDetailsIntentService", "OperationApplicationException applying batch:" + e.getLocalizedMessage());
				}
				Log.d("CourseDetailsIntentService", "Batch executed.");
				
				LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(ACTION_FINISHED);
				broadcastIntent.putExtra(PARAM_OUT_DATABASE_UPDATED, success);
				bm.sendBroadcast(broadcastIntent);
			}
		} else {
			Log.d("CourseDetailsIntentService", "Response Object was null");
		}
		
	}

}
