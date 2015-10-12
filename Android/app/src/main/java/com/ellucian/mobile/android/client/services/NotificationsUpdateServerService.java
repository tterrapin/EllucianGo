/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.services;

import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.client.notifications.Notification;
import com.ellucian.mobile.android.util.Extra;
import com.google.gson.Gson;

public class NotificationsUpdateServerService extends IntentService {
	private static final String TAG = NotificationsUpdateServerService.class.getSimpleName();
	public static final String MODIFICATION_READ = "modificationRead";
	public static final String MODIFICATION_DELETE = "modificationDelete";

	public NotificationsUpdateServerService() {
		super("NotificationsMarkReadService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "handling intent");
				
		if (!intent.hasExtra(Extra.ID)) {
			Log.e(TAG, "Missing uuid, can not send request to update server!");
			return ;
		}
		
		String modType = intent.getStringExtra(Extra.NOTIFICATIONS_MODIFICATION_TYPE);
				
		JSONObject response = null;
		if (modType.equals(MODIFICATION_READ)) {
			response = handleUpdateServerRead(intent);
		} else if (modType.equals(MODIFICATION_DELETE)) {
			response = handleUpdateServerDelete(intent);
		}

		if (response == null) {
			Log.e(TAG, "Response is null, mark read not successful.");
		}
		
	}
	
	private JSONObject handleUpdateServerRead(Intent intent) {
		String id = intent.getStringExtra(Extra.ID);
		MobileClient client = new MobileClient(this);
		Gson gson = new Gson();
		
		MarkReadData markReadData = new MarkReadData(id);
		String markReadDataString = gson.toJson(markReadData);
		
		String url = client.addUserToUrl(intent.getStringExtra(Extra.REQUEST_URL));
		url += "/" + id;
		JSONObject response = client.postNotificationMarkedRead(url, markReadDataString);
		return response;
	}
	
	public class MarkReadData {
		public final String uuid;
		public final String[] statuses;
		
		public MarkReadData(String value) {
			uuid = value;
			statuses = new String[] { Notification.STATUS_READ };
		}
	}
	
	private JSONObject handleUpdateServerDelete(Intent intent) {
		String id = intent.getStringExtra(Extra.ID);
		MobileClient client = new MobileClient(this);

		String url = client.addUserToUrl(intent.getStringExtra(Extra.REQUEST_URL));
		url += "/" + id;
		JSONObject response = client.deleteNotification(url);
		return response;
	}

}
