package com.ellucian.mobile.android.notifications;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.ellucian.mobile.android.MainActivity;
import com.ellucian.elluciango.R;

public class DeviceNotifications {
	private static final String TAG = DeviceNotifications.class.getName();
	private static final int ID = 0;

	private Context context;
	private NotificationCompat.Builder builder;
	private NotificationManager manager;
	private List<android.app.Notification> notificationList;
	private int notificationIcon = R.drawable.ic_notifications;
	//private int idCounter = 0;
	
	public DeviceNotifications(Context context) {
		this.context = context;
		builder = new NotificationCompat.Builder(context);
		manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	public NotificationCompat.Builder getBuilder() {
		return builder;	
	}
	
	public NotificationManager getManager() {
		return manager;	
	}
	
	public List<android.app.Notification> buildNotificationListFromClientArray(
			com.ellucian.mobile.android.client.notifications.Notification[] clientNotificationsArray) {
		Log.d(TAG, "Building device notification list");
		notificationList = new ArrayList<android.app.Notification>();
		
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
		
		for (com.ellucian.mobile.android.client.notifications.Notification clientNotification : clientNotificationsArray) {
			notificationList.add(
				builder.setSmallIcon(notificationIcon)
					   .setContentTitle(context.getResources().getText(R.string.notifications_device_title))
					   .setContentText(context.getResources().getText(R.string.notifications_device_content))
					   .setContentIntent(pendingIntent)
					   .build()
			);
		}
		
		return notificationList;		
	}
	
	public android.app.Notification buildNotification(int numberOfNotifications) {
		android.app.Notification notification = null;
		
		Intent intent = new Intent(context, NotificationsActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the back stack
		stackBuilder.addParentStack(NotificationsActivity.class);
		// Adds the Intent to the top of the stack
		stackBuilder.addNextIntent(intent);
		// Gets a PendingIntent containing the entire back stack
		PendingIntent pendingIntent =
		        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		
		
		notification = builder.setSmallIcon(notificationIcon)
					   .setContentTitle(context.getResources().getText(R.string.notifications_device_title))
					   .setContentText(context.getResources().getText(R.string.notifications_device_content))
					   .setNumber(numberOfNotifications)
					   .setContentIntent(pendingIntent)
					   .setAutoCancel(true)
					   .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE)
					   .build();
			
		
		return notification;
		
	}
	
	public void makeNotificationActive(android.app.Notification notification) {
		manager.notify(ID, notification);
	}
	
	public void makeNotificationListActive(List<android.app.Notification> notificationList) {
		for (android.app.Notification notification : notificationList) {
			makeNotificationActive(notification);
		}
	}
}
