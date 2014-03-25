package com.ellucian.mobile.android.client.services;

import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.client.notifications.Notification;
import com.ellucian.mobile.android.client.notifications.NotificationsBuilder;
import com.ellucian.mobile.android.client.notifications.NotificationsResponse;
import com.ellucian.mobile.android.notifications.DeviceNotifications;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.provider.EllucianContract.Notifications;
import com.ellucian.mobile.android.util.Extra;

public class NotificationsIntentService extends IntentService {
	public static final String PARAM_OUT_DATABASE_UPDATED = "updated";
	public static final String ACTION_FINISHED = "com.ellucian.mobile.android.client.services.NotificationsIntentService.action.updated";

	public NotificationsIntentService() {
		super("NotificationsIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		boolean success = false;
		Log.d("NotificationsIntentService", "handling intent");
		MobileClient client = new MobileClient(this);
		
		String url = client.addUserToUrl(intent.getStringExtra(Extra.REQUEST_URL));
		NotificationsResponse response = client.getNotifications(url);
		
		if (response != null) {
			
			if (response.notifications != null && response.notifications.length > 0) {
				
				ContentResolver contentResolver = getContentResolver();
	
				Cursor notificationsCursor = contentResolver.query(Notifications.CONTENT_URI, 
																   new String[] { Notifications.NOTIFICATIONS_ID},
																   null, 
																   null, 
																   Notifications.DEFAULT_SORT);
				
				int count = 0;
				if (notificationsCursor != null && notificationsCursor.moveToFirst()) {
					List<String> savedIdList = new ArrayList<String>();
					
					int columnIndex = notificationsCursor.getColumnIndex(Notifications.NOTIFICATIONS_ID);
					do {
						String savedId = notificationsCursor.getString(columnIndex);
						savedIdList.add(savedId);
						Log.d("NotificationsIntentService", "Found notification id in database: " + savedId);
					} while (notificationsCursor.moveToNext());
					
					for (Notification notification : response.notifications) {
						if (!savedIdList.contains(notification.id)) {
							count++;
						}
					}
				} else {
					Log.d("NotificationsIntentService", "No notifications found in the database");
					count = response.notifications.length;
				}
				
				Log.d("NotificationsIntentService", "" + count + " new notifications found");
				if (count > 0) {
					DeviceNotifications deviceNotifications = ((EllucianApplication)getApplication()).getDeviceNotifications();
					deviceNotifications.makeNotificationActive(deviceNotifications.buildNotification(count));
				}
			}
			
			
			
			Log.d("NotificationsIntentService", "Retrieved response from notifications client");
			NotificationsBuilder builder = new NotificationsBuilder(this);
			Log.d("NotificationsIntentService", "Building content provider operations");
			ArrayList<ContentProviderOperation> ops = builder.buildOperations(response);
			Log.d("NotificationsIntentService", "Created " + ops.size() + " operations");
			
			if(ops.size() > 0) {
				Log.d("NotificationsIntentService", "Executing batch.");
				try {
					getContentResolver().applyBatch(
							EllucianContract.CONTENT_AUTHORITY, ops);
					success = true;
				} catch (RemoteException e) {
					Log.e("NotificationsIntentService", "RemoteException applying batch" + e.getLocalizedMessage());
				} catch (OperationApplicationException e) {
					Log.e("NotificationsIntentService", "OperationApplicationException applying batch:" + e.getLocalizedMessage());
				}
				Log.d("NotificationsIntentService", "Batch executed.");
				
				LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(ACTION_FINISHED);
				broadcastIntent.putExtra(PARAM_OUT_DATABASE_UPDATED, success);
				bm.sendBroadcast(broadcastIntent);
			}
		} else {
			Log.d("NotificationsIntentService", "Response Object was null");
		}
		
	}

}
