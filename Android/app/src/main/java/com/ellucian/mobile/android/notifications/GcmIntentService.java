// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.notifications;

import java.util.HashMap;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.util.Extra;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {
	public static final String TAG = GcmIntentService.class.getSimpleName();
	
	public GcmIntentService() {
		super("GcmIntentService");
	}

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                //sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                //sendNotification("Deleted messages on server: " + extras.toString());
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
            	
            	String message = extras.getString("message");
            	String uuid = extras.getString("uuid");
                postNotificationToDrawer(uuid, message);
                
                Log.d(TAG, "Recieved message: '" + message + "' for uuid: '" + uuid + "'");
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void postNotificationToDrawer(String uuid, String message) {
    	EllucianApplication app = (EllucianApplication)getApplication();
    	DeviceNotifications deviceNotifications = app.getDeviceNotifications();
    	
    	HashMap<String, String> extras = new HashMap<String, String>();
    	extras.put(Extra.NOTIFICATIONS_NOTIFICATION_ID, uuid);
    	
    	Notification notification = deviceNotifications.buildiGcmNotification(message, extras);
    	
    	deviceNotifications.makeNotificationActive(uuid, notification);
    }
}
