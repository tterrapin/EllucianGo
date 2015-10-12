/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ellucian.mobile.android.provider.EllucianContract;

public abstract class EllucianIntentService extends IntentService {
	private static final String PARAM_OUT_DATABASE_UPDATED = "updated";
	private final String actionFinished;
	private final String tag;
	
	
	public EllucianIntentService(String tag) {
		super(tag);
		actionFinished = "com.ellucian.mobile.android.client.services" + tag + ".action.updated";
		this.tag = tag;		
	}
	
	abstract protected void onHandleIntent(Intent arg0);
	

	public void executeBatch(ArrayList<ContentProviderOperation> operations ) {
		Log.d(tag, "Executing batch.");
		boolean success = false;
		
		try {
			getContentResolver().applyBatch(EllucianContract.CONTENT_AUTHORITY, operations);
			success = true;
		} catch (RemoteException e) {
			Log.e(tag, "RemoteException applying batch" + e.getLocalizedMessage());
		} catch (OperationApplicationException e) {
			Log.e(tag, "OperationApplicationException applying batch:" + e.getLocalizedMessage());
		}
		Log.d(tag, "Batch executed.");
		
		LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(actionFinished);
		broadcastIntent.putExtra(PARAM_OUT_DATABASE_UPDATED, success);
		bm.sendBroadcast(broadcastIntent);
	}
	
	
}
