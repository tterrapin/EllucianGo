/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.services;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.ellucian.mobile.android.client.notifications.Notification;
import com.ellucian.mobile.android.provider.EllucianContract.Notifications;
import com.ellucian.mobile.android.util.Extra;

public class NotificationsUpdateDatabaseService extends IntentService {
	private static final String TAG = NotificationsUpdateDatabaseService.class.getSimpleName();
	public static final String ACTION_DATABASE_UPDATED = "com.ellucian.mobile.android.client.NotificationsUpdateDatabaseReadService.action.database.updated";
	public static final String ACTION_RESET_LIST = "com.ellucian.mobile.android.client.NotificationsUpdateDatabaseReadService.action.reset.list";
	public static final String MODIFICATION_READ = "modificationRead";
	public static final String MODIFICATION_DELETE = "modificationDelete";
	
	
	public NotificationsUpdateDatabaseService() {
		super("NotificationsUpdateDatabaseReadService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "handling intent");
		
		if (!intent.hasExtra(Extra.ID)) {
			Log.e(TAG, "Missing uuid, can not send attempt to update database!");
			return ;
		}
		
		String modType = intent.getStringExtra(Extra.NOTIFICATIONS_MODIFICATION_TYPE);
		
		boolean reset = false;
		if (modType.equals(MODIFICATION_READ)) {
			handleUpdateRead(intent);
		} else if (modType.equals(MODIFICATION_DELETE)) {
			handleDelete(intent);
			// request an adapter selected item reset on a delete
			reset = true;
		}
					
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_DATABASE_UPDATED);
		broadcastIntent.putExtra(ACTION_RESET_LIST, reset);
		lbm.sendBroadcast(broadcastIntent);
		
	}
	
	private void handleUpdateRead(Intent intent) {
		String id = intent.getStringExtra(Extra.ID);
		String statusesString = intent.getStringExtra(Extra.NOTIFICATIONS_STATUSES);
		if (!TextUtils.isEmpty(statusesString)) {
			statusesString += "," + Notification.STATUS_READ;
		} else {
			statusesString = Notification.STATUS_READ;
		}
		Log.d(TAG, "Trying to update read status on notification :" + id);
		
		ContentResolver cr = getContentResolver();
		ContentValues values = new ContentValues();
		
		values.put(Notifications.NOTIFICATIONS_STATUSES, statusesString);
		int rows = cr.update(Notifications.buildNotificationsUri(id), values, null, null);
		Log.d(TAG, "Rows updated:" + rows);	
	}
	
	private void handleDelete(Intent intent) {
		String id = intent.getStringExtra(Extra.ID);
		
		Log.d(TAG, "Trying to delete notification :" + id);
		
		ContentResolver cr = getContentResolver();

		int rows = cr.delete(Notifications.buildNotificationsUri(id), null, null);
		Log.d(TAG, "Rows updated:" + rows);	
	}

}
