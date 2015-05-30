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
import com.ellucian.mobile.android.client.events.EventsBuilder;
import com.ellucian.mobile.android.client.events.EventsResponse;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.util.Extra;

public class EventsIntentService extends IntentService {
	public static final String PARAM_OUT_DATABASE_UPDATED = "updated";
	public static final String ACTION_FINISHED = "com.ellucian.mobile.android.client.services.EventsIntentService.action.updated";
	public EventsIntentService() {
		super("EventsIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		boolean success = false;
		Log.d("EventsIntentService", "handling intent");
		MobileClient client = new MobileClient(this);
		EventsResponse response = client.getEvents(intent.getStringExtra(Extra.REQUEST_URL));
		if (response != null) {
			Log.d("EventsIntentService", "Retrieved response from Events client");
			EventsBuilder builder = new EventsBuilder(this, intent.getStringExtra(Extra.MODULE_ID));
			Log.d("EventsIntentService", "Building content provider operations");
			ArrayList<ContentProviderOperation> ops = builder.buildOperations(response);
			Log.d("EventsIntentService", "Created " + ops.size() + " operations");
			
			if(ops.size() > 0) {
				Log.d("EventsIntentService", "Executing batch.");
				try {
					getContentResolver().applyBatch(
							EllucianContract.CONTENT_AUTHORITY, ops);
					success = true;
				} catch (RemoteException e) {
					Log.e("EventsIntentService", "RemoteException applying batch" + e.getLocalizedMessage());
				} catch (OperationApplicationException e) {
					Log.e("EventsIntentService", "OperationApplicationException applying batch:" + e.getLocalizedMessage());
				}
				Log.d("EventsIntentService", "Batch executed.");
				
				LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(ACTION_FINISHED);
				broadcastIntent.putExtra(PARAM_OUT_DATABASE_UPDATED, success);
				bm.sendBroadcast(broadcastIntent);
			}
		} else {
			Log.d("EventsIntentService", "Response Object was null");
		}
	}

}
