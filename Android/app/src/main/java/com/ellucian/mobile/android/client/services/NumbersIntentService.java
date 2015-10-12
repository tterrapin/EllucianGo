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
import com.ellucian.mobile.android.client.numbers.NumbersBuilder;
import com.ellucian.mobile.android.client.numbers.NumbersResponse;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.util.Extra;

public class NumbersIntentService extends IntentService {
	private static final String PARAM_OUT_DATABASE_UPDATED = "updated";
	private static final String ACTION_FINISHED = "com.ellucian.mobile.android.client.services.NumbersIntentService.action.updated";
	
	public NumbersIntentService() {
		super("NumbersIntentService");
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		boolean success = false;
		
		Log.d("NumbersIntentService", "handling intent");
		MobileClient client = new MobileClient(this);
		NumbersResponse response = client.getNumbers(intent.getStringExtra(Extra.REQUEST_URL)); 
		if (response != null) {
			NumbersBuilder builder = new NumbersBuilder(this, intent.getStringExtra(Extra.MODULE_ID));
			ArrayList<ContentProviderOperation> ops = builder.buildOperations(response);
			Log.d("NumbersIntentService", "Created " + ops.size() + " operations");
			
			if(ops.size() > 0) {
				Log.d("NumbersIntentService", "Executing batch.");
				try {
					getContentResolver().applyBatch(
							EllucianContract.CONTENT_AUTHORITY, ops);
					success = true;
				} catch (RemoteException e) {
					Log.e("NumbersIntentService", "RemoteException applying batch" + e.getLocalizedMessage());
				} catch (OperationApplicationException e) {
					Log.e("NumbersIntentService", "OperationApplicationException applying batch:" + e.getLocalizedMessage());
				}
				Log.d("NumbersIntentService", "Batch executed.");
				
				LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(ACTION_FINISHED);
				broadcastIntent.putExtra(PARAM_OUT_DATABASE_UPDATED, success);
				bm.sendBroadcast(broadcastIntent);
			}
		} else {
			Log.d("NumbersIntentService", "Response Object was null");
		}
	}

}
