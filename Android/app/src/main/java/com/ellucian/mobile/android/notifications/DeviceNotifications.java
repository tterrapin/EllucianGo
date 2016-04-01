/*
 * Copyright 2016 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeviceNotifications {
	private static final String TAG = DeviceNotifications.class.getName();
	private static final int BASE_NOTIFICATION_ID = 0;

	private final Context context;
	private final NotificationManager manager;
	private final int notificationIcon = R.drawable.ic_notifications;
    private final Bitmap largeNotificationIcon;
	
	public DeviceNotifications(Context context) {
		this.context = context;
		manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        largeNotificationIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
	}
	
	public Notification.Builder getBuilder() {
		return new Notification.Builder(context);
	}
	
	public NotificationManager getManager() {
		return manager;	
	}
	
	@SuppressWarnings("unused")
	public List<android.app.Notification> buildNotificationListFromClientArray(
			com.ellucian.mobile.android.client.notifications.Notification[] clientNotificationsArray) {
		Log.d(TAG, "Building device notification list");
        List<android.app.Notification> notificationList = new ArrayList<>();
		
		// TODO - figure this out
		Intent mainIntent = new Intent(context, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the back stack
		//stackBuilder.addParentStack(ResultActivity.class);
		// Adds the Intent to the top of the stack
		stackBuilder.addNextIntent(mainIntent);
		// Gets a PendingIntent containing the entire back stack
		PendingIntent pendingIntent =
		        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		for (com.ellucian.mobile.android.client.notifications.Notification clientNotification : clientNotificationsArray) {
			notificationList.add(
				builder.setSmallIcon(notificationIcon)
                        .setLargeIcon(largeNotificationIcon)
                        .setContentTitle(context.getResources().getText(R.string.notifications_device_title))
                        .setContentText(context.getResources().getText(R.string.notifications_device_content))
                        .setTicker(context.getResources().getText(R.string.notifications_device_content))
                        .setContentIntent(pendingIntent)
                        .build()
			);
		}
		
		return notificationList;		
	}
	
	public android.app.Notification buildNotification(int numberOfNotifications) {
		android.app.Notification notification;
		
		Intent intent = new Intent(context, NotificationsActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the back stack
		stackBuilder.addParentStack(NotificationsActivity.class);
		// Adds the Intent to the top of the stack
		stackBuilder.addNextIntent(intent);
		// Gets a PendingIntent containing the entire back stack
		PendingIntent pendingIntent =
		        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        notification = builder.setSmallIcon(notificationIcon)
                .setLargeIcon(largeNotificationIcon)
                .setContentTitle(context.getResources().getText(R.string.notifications_device_title))
                .setContentText(context.getResources().getText(R.string.notifications_device_content))
                .setTicker(context.getResources().getText(R.string.notifications_device_content))
                .setNumber(numberOfNotifications)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .build();
			
		
		return notification;
		
	}
	
	public android.app.Notification buildGcmNotification(String message, Map<String, String> extras) {
		android.app.Notification notification;
		
		// Build a PendingIntent to fire when the user clicks the notification in the drawer
		Intent intent = new Intent(context, NotificationsActivity.class);
		
		// add the extras
		if (extras.size() > 0) {
			for (String key : extras.keySet()){
				intent.putExtra(key,  extras.get(key));
			}
		}
		
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the back stack
		stackBuilder.addParentStack(NotificationsActivity.class);
		// Adds the Intent to the top of the stack
		stackBuilder.addNextIntent(intent);
		// Gets a PendingIntent containing the entire back stack
		PendingIntent pendingIntent =
		        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        notification = builder.setSmallIcon(notificationIcon)
                .setLargeIcon(largeNotificationIcon)
                .setContentTitle(context.getResources().getText(R.string.notifications_device_title))
                .setContentText(message)
                .setTicker(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .build();
        return notification;
	}
	
	public void makeNotificationActive(android.app.Notification notification) {
		manager.notify(BASE_NOTIFICATION_ID, notification);
	}
	
	public void makeNotificationActive(String tag, android.app.Notification notification) {
		manager.notify(tag, BASE_NOTIFICATION_ID, notification);
	}

    @SuppressWarnings("unused")
	public void makeNotificationListActive(List<android.app.Notification> notificationList) {
		for (android.app.Notification notification : notificationList) {
			makeNotificationActive(notification);
		}
	}
}
