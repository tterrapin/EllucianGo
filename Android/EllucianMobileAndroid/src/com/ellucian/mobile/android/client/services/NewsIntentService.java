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
import com.ellucian.mobile.android.client.news.NewsBuilder;
import com.ellucian.mobile.android.client.news.NewsResponse;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.util.Extra;

public class NewsIntentService extends IntentService {
	public static final String PARAM_OUT_DATABASE_UPDATED = "updated";
	public static final String ACTION_FINISHED = "com.ellucian.mobile.android.client.services.NewsIntentService.action.updated";
	public NewsIntentService() {
		super("NewsIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		boolean success = false;
		Log.d("NewsIntentService", "handling intent");
		MobileClient client = new MobileClient(this);
		NewsResponse response = client.getNews(intent.getStringExtra(Extra.REQUEST_URL));
		if (response != null) {
			Log.d("NewsIntentService", "Retrieved response from News client");
			NewsBuilder builder = new NewsBuilder(this, intent.getStringExtra(Extra.MODULE_ID));
			Log.d("NewsIntentService", "Building content provider operations");
			ArrayList<ContentProviderOperation> ops = builder.buildOperations(response);
			Log.d("NewsIntentService", "Created " + ops.size() + " operations");
			
			if(ops.size() > 0) {
				Log.d("NewsIntentService", "Executing batch.");
				try {
					getContentResolver().applyBatch(
							EllucianContract.CONTENT_AUTHORITY, ops);
					success = true;
				} catch (RemoteException e) {
					Log.e("NewsIntentService", "RemoteException applying batch" + e.getLocalizedMessage());
				} catch (OperationApplicationException e) {
					Log.e("NewsIntentService", "OperationApplicationException applying batch:" + e.getLocalizedMessage());
				}
				Log.d("NewsIntentService", "Batch executed.");
				
				LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(ACTION_FINISHED);
				broadcastIntent.putExtra(PARAM_OUT_DATABASE_UPDATED, success);
				bm.sendBroadcast(broadcastIntent);
			}
		} else {
			Log.d("NewsIntentService", "Response Object was null");
		}
	} 
}
