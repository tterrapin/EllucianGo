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
import com.ellucian.mobile.android.client.courses.overview.CourseGradesBuilder;
import com.ellucian.mobile.android.client.grades.GradesResponse;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.util.Extra;

public class CourseGradesIntentService extends IntentService {
	private static final String PARAM_OUT_DATABASE_UPDATED = "updated";
	private static final String ACTION_FINISHED = "com.ellucian.mobile.android.client.services.CourseGradesIntentService.action.updated";
	
	public CourseGradesIntentService() {
		super("CourseGradesIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		boolean success = false;
		Log.d("CourseGradesIntentService", "handling intent");
		MobileClient client = new MobileClient(this);
		
		String url = client.addUserToUrl(intent.getStringExtra(Extra.COURSES_GRADES_URL));
		url = client.addTermAndSectionToUrl(url, intent.getStringExtra(Extra.COURSES_TERM_ID), intent.getStringExtra(Extra.COURSES_COURSE_ID));
		GradesResponse response = client.getGrades(url);
		
		if (response != null) {
			Log.d("CourseGradesIntentService", "Retrieved response from grades client");
			CourseGradesBuilder builder = new CourseGradesBuilder(this);
			Log.d("CourseGradesIntentService", "Building content provider operations");
			ArrayList<ContentProviderOperation> ops = builder.buildOperations(response);
			Log.d("CourseGradesIntentService", "Created " + ops.size() + " operations");
			
			if(ops.size() > 0) {
				Log.d("CourseGradesIntentService", "Executing batch.");
				try {
					getContentResolver().applyBatch(
							EllucianContract.CONTENT_AUTHORITY, ops);
					success = true;
				} catch (RemoteException e) {
					Log.e("CourseGradesIntentService", "RemoteException applying batch" + e.getLocalizedMessage());
				} catch (OperationApplicationException e) {
					Log.e("CourseGradesIntentService", "OperationApplicationException applying batch:" + e.getLocalizedMessage());
				}
				Log.d("CourseGradesIntentService", "Batch executed.");
				
				LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(ACTION_FINISHED);
				broadcastIntent.putExtra(PARAM_OUT_DATABASE_UPDATED, success);
				bm.sendBroadcast(broadcastIntent);
			}
		} else {
			Log.d("CourseGradesIntentService", "Response Object was null");
		}

	}

}
