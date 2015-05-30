// Copyright 2014 Ellucian Company L.P and its affiliates.
package com.ellucian.mobile.android.client.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.ilp.widget.AssignmentsWidgetProvider;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

import java.util.Date;

public class UpdateAssignmentIntentService extends IntentService {

    private final String TAG = UpdateAssignmentIntentService.class.getSimpleName();

    public UpdateAssignmentIntentService() { super("UpdateAssignmentIntentService"); }

    @Override
    protected void onHandleIntent(Intent incomingIntent) {
        Log.d(TAG, "update the assignments " + incomingIntent.getAction());

        EllucianApplication app = (EllucianApplication) this.getApplicationContext();

        String ilpUrl = Utils.getStringFromPreferences(getApplicationContext(), Utils.CONFIGURATION, Utils.ILP_URL, null);

        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(app.getApplicationContext());
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(AssignmentsWidgetProvider.ACTION_UPDATE_WIDGET_HEADER);
        if (!TextUtils.isEmpty(ilpUrl)) {
            if (app.isUserAuthenticated()) {
                Log.d(TAG, "Fetch updated Course Assignments");
                Intent assignmentsIntent = new Intent(app, CourseAssignmentsIntentService.class);
                assignmentsIntent.putExtra(Extra.SEND_BROADCAST, false);
                assignmentsIntent.putExtra(Extra.COURSES_ILP_URL, ilpUrl);
                app.startService(assignmentsIntent);

                Date lastUpdated = new Date();
                String updatedTime = CalendarUtils.getDefaultTimeString(app, lastUpdated);
                Log.d(TAG, "last updated time:" + updatedTime);
                app.sendBroadcast(broadcastIntent);
            } else {
                Log.d(TAG, "User is not authenticated. Don't run the task!!");
                broadcastIntent.putExtra(AssignmentsWidgetProvider.EXTRA_MESSAGE,
                        AssignmentsWidgetProvider.EXTRA_NO_AUTH);
                app.sendBroadcast(broadcastIntent);
            }
        } else {
            Log.d(TAG, "ILP is not configured for this App");
            broadcastIntent.putExtra(AssignmentsWidgetProvider.EXTRA_MESSAGE, AssignmentsWidgetProvider.EXTRA_NO_ILP);
            app.sendBroadcast(broadcastIntent);
        }

    }
}
