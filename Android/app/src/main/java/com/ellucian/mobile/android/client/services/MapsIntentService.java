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
import com.ellucian.mobile.android.client.maps.MapsBuilder;
import com.ellucian.mobile.android.client.maps.MapsResponse;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.util.Extra;

/**
 * Service to load data necessary for campus maps.  This service will attempt to
 * communicate with web services to retrieve maps data, create a batch of
 * operations to update the SQLite database locally, and then execute the
 * query.  The Android support LocalBroadcastManager is used to notify the 
 * application of this change and will return a boolean on action finished to
 * denote whether the update was successful.
 * @author sdk
 */
public class MapsIntentService extends IntentService {
	private static final String PARAM_OUT_DATABASE_UPDATED = "updated";
	private static final String ACTION_FINISHED = "com.ellucian.mobile.android.client.services.MapsIntentService.action.updated";
	public MapsIntentService() {
		super("MapsIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		boolean success = false;
		Log.d("MapsIntentService", "handling intent");
		MobileClient client = new MobileClient(this);
		String url = intent.getStringExtra(Extra.MAPS_CAMPUSES_URL);
		MapsResponse response = client.getMaps(url);
		if (response != null) {
			Log.d("MapsIntentService", "Retrieved response from map client");
			MapsBuilder builder = new MapsBuilder(this, intent.getStringExtra(Extra.MODULE_ID));
			Log.d("MapsIntentService", "Building content provider operations");
			ArrayList<ContentProviderOperation> ops = builder.buildOperations(response);
			Log.d("MapsIntentService", "Created " + ops.size() + " operations");
			
			if(ops.size() > 0) {
				Log.d("MapsIntentService", "Executing batch.");
				try {
					getContentResolver().applyBatch(
							EllucianContract.CONTENT_AUTHORITY, ops);
					success = true;
				} catch (RemoteException e) {
					Log.e("MapsIntentService", "RemoteException applying batch" + e.getLocalizedMessage());
				} catch (OperationApplicationException e) {
					Log.e("MapsIntentService", "OperationApplicationException applying batch:" + e.getLocalizedMessage());
				}
				Log.d("MapsIntentService", "Batch executed.");
				
				LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(ACTION_FINISHED);
				broadcastIntent.putExtra(PARAM_OUT_DATABASE_UPDATED, success);
				bm.sendBroadcast(broadcastIntent);
			}
		} else {
			Log.d("MapsIntentService", "Response Object was null");
		}
		
	}

}
