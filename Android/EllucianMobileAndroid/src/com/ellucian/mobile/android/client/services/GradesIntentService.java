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
import com.ellucian.mobile.android.client.grades.GradesBuilder;
import com.ellucian.mobile.android.client.grades.GradesResponse;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.util.Extra;

public class GradesIntentService extends IntentService {
	public static final String PARAM_OUT_DATABASE_UPDATED = "updated";
	public static final String ACTION_FINISHED = "com.ellucian.mobile.android.client.services.GradesIntentService.action.updated";
	
	public GradesIntentService() {
		super("GradesIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		boolean success = false;
		Log.d("GradesIntentService", "handling intent");
		MobileClient client = new MobileClient(this);
		String url = client.addUserToUrl(intent.getStringExtra(Extra.REQUEST_URL));
		GradesResponse response = client.getGrades(url);
		if (response != null) {
			Log.d("GradesIntentService", "Retrieved response from grades client");
			GradesBuilder builder = new GradesBuilder(this);
			Log.d("GradesIntentService", "Building content provider operations");
			ArrayList<ContentProviderOperation> ops = builder.buildOperations(response);
			Log.d("GradesIntentService", "Created " + ops.size() + " operations");
			
			if(ops.size() > 0) {
				Log.d("GradesIntentService", "Executing batch.");
				try {
					getContentResolver().applyBatch(
							EllucianContract.CONTENT_AUTHORITY, ops);
					success = true;
				} catch (RemoteException e) {
					Log.e("GradesIntentService", "RemoteException applying batch" + e.getLocalizedMessage());
				} catch (OperationApplicationException e) {
					Log.e("GradesIntentService", "OperationApplicationException applying batch:" + e.getLocalizedMessage());
				}
				Log.d("GradesIntentService", "Batch executed.");
				
				LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(ACTION_FINISHED);
				broadcastIntent.putExtra(PARAM_OUT_DATABASE_UPDATED, success);
				bm.sendBroadcast(broadcastIntent);
			}
		} else {
			Log.d("GradesIntentService", "Response Object was null");
		}

	}

}
