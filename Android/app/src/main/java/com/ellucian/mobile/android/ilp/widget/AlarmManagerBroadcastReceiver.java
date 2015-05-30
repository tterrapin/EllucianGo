// Copyright 2014-2015 Ellucian Company L.P and its affiliates.
package com.ellucian.mobile.android.ilp.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.client.services.UpdateAssignmentIntentService;

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = AlarmManagerBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent incomingIntent) {
        Log.d(TAG, "onReceive() -- begin");
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        // Acquire the lock
        wl.acquire();

        EllucianApplication application = (EllucianApplication) context.getApplicationContext();
        Intent assignmentIntent = new Intent(context, UpdateAssignmentIntentService.class);
        application.startService(assignmentIntent);

        // Release the lock
        wl.release();
    }


}
