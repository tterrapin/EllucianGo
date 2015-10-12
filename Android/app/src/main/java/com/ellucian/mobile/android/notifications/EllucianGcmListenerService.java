/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.notifications;

import android.app.Notification;
import android.os.Bundle;
import android.util.Log;

import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.util.Extra;
import com.google.android.gms.gcm.GcmListenerService;

import java.util.HashMap;

public class EllucianGcmListenerService extends GcmListenerService{
	private static final String TAG = "EllucianGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String uuid = data.getString("uuid");
        postNotificationToDrawer(uuid, message);
        Log.d(TAG, "Received message: '" + message + "' for uuid: '" + uuid + "'");
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void postNotificationToDrawer(String uuid, String message) {
    	EllucianApplication app = (EllucianApplication)getApplication();
    	DeviceNotifications deviceNotifications = app.getDeviceNotifications();
    	
    	HashMap<String, String> extras = new HashMap<>();
    	extras.put(Extra.NOTIFICATIONS_NOTIFICATION_ID, uuid);

    	Notification notification = deviceNotifications.buildGcmNotification(message, extras);
    	
    	deviceNotifications.makeNotificationActive(uuid, notification);
    }
}
