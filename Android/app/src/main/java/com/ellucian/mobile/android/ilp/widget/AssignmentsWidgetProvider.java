// Copyright 2014-2015 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.ilp.widget;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.ilp.IlpCardActivity;
import com.ellucian.mobile.android.provider.EllucianContract.CourseAssignments;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;

public class AssignmentsWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "AppWidgetProvider";
	private static HandlerThread sWorkerThread;
	private static Handler sWorkerQueue;
	private static DataProviderObserver sDataObserver;
    public static final String ACTION_UPDATE_WIDGET_HEADER = "com.ellucian.mobile.AssignmentWidgetProvider.action.update.widget.header";
    public static final String EXTRA_NO_AUTH = "WIDGET_USER_NOT_AUTHENTICATED";
    public static final String EXTRA_NO_ILP = "NO_ILP_FOR_WIDGET_CONFIG";
    public static final String EXTRA_MESSAGE = "APP_WIDGET_MESSAGE";

	public AssignmentsWidgetProvider() {
		// Start the worker thread to update Content Provider.
		sWorkerThread = new HandlerThread("AssignmentsWidgetProvider worker");
		sWorkerThread.start();
		sWorkerQueue = new Handler(sWorkerThread.getLooper());
	}

    private void updateRemoteView(Context context, AppWidgetManager appWidgetManager,
                                  int[] appWidgetIds, String message) {

        for (int i = 0; i < appWidgetIds.length; i++) {
//            Log.v(TAG, ": " + i);
            Intent svcIntent = new Intent(context, AssignmentsWidgetService.class);

            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    appWidgetIds[i]);
            svcIntent.setData(Uri.parse(svcIntent
                    .toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews widget = new RemoteViews(context.getPackageName(),
                    R.layout.assignments_widget);

            widget.setRemoteAdapter(R.id.assignment_widget_list, svcIntent);

            // ClickIntent for Login Message
            Intent loginIntent = new Intent(context,
                    IlpCardActivity.class);
            PendingIntent loginPI = PendingIntent.getActivity(context, 0,
                    loginIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setOnClickPendingIntent(R.id.assignment_widget_login_message,
                    loginPI);

            // ClickIntent for ListView items
            Intent clickIntent = new Intent(context,
                    IlpCardActivity.class);
            PendingIntent clickPI = PendingIntent.getActivity(context, 0,
                    clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setPendingIntentTemplate(R.id.assignment_widget_list,
                    clickPI);

            if (message == null) {
                widget.setViewVisibility(R.id.assignment_widget_login_message, View.GONE);
                widget.setViewVisibility(R.id.assignment_widget_ilp_message, View.GONE);
                widget.setViewVisibility(R.id.assignment_widget_list, View.VISIBLE);
                widget.setEmptyView(R.id.assignment_widget_list, R.id.assignment_zero_due);
                widget.setTextViewText(R.id.assignment_zero_due, context.getResources().getString(R.string.widget_no_assignments));
            } else if (message.equals(AssignmentsWidgetProvider.EXTRA_NO_AUTH)) {
                widget.setViewVisibility(R.id.assignment_widget_login_message, View.VISIBLE);
                widget.setViewVisibility(R.id.assignment_widget_ilp_message, View.GONE);
                widget.setViewVisibility(R.id.assignment_widget_list, View.VISIBLE);
                widget.setViewVisibility(R.id.assignment_zero_due, View.GONE);
                widget.setTextViewText(R.id.assignment_zero_due, null);
            } else if (message.equals(AssignmentsWidgetProvider.EXTRA_NO_ILP)) {
                widget.setViewVisibility(R.id.assignment_widget_login_message, View.GONE);
                widget.setViewVisibility(R.id.assignment_widget_ilp_message, View.VISIBLE);
                widget.setViewVisibility(R.id.assignment_widget_list, View.GONE);
                widget.setViewVisibility(R.id.assignment_zero_due, View.GONE);
                widget.setTextViewText(R.id.assignment_zero_due, null);
            }

            appWidgetManager.updateAppWidget(appWidgetIds[i], widget);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds[i],
                    R.id.assignment_widget_list);
        }
    }

    private void updateHeader(Context context, Intent intent) {
        String message = null;
        if (intent.hasExtra(EXTRA_MESSAGE)) {
            message = intent.getStringExtra(EXTRA_MESSAGE);
            intent.removeExtra(EXTRA_MESSAGE);
        }

        Log.d(TAG, "updateHeader - Intent message: " + message);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName widgetComponent = new ComponentName(context, AssignmentsWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);

        updateRemoteView(context, appWidgetManager, appWidgetIds, message);
    }

	@Override
	public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive - Intent Action: " + intent.getAction());

        if (intent.getAction().equals(ACTION_UPDATE_WIDGET_HEADER)) {
            updateHeader(context, intent);
        }

        super.onReceive(context, intent);

        // Register for external updates to the data to trigger an update of the
		// widget. When using
		// content providers, the data is often updated via a background
		// service, or in response to
		// user interaction in the main app. To ensure that the widget always
		// reflects the current
		// state of the data, we must listen for changes and update ourselves
		// accordingly.

        final ContentResolver r = context.getContentResolver();
		if (sDataObserver == null) {
			final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
			final ComponentName cn = new ComponentName(context,
					AssignmentsWidgetProvider.class);
			sDataObserver = new DataProviderObserver(mgr, cn, sWorkerQueue);
            // Listen for changes to:
            // - Modules - perhaps the user switched schools to a config with
            //          ILP, or their school added ILP
            // - CourseAssignments
			r.registerContentObserver(CourseAssignments.CONTENT_URI, true, sDataObserver);
            r.registerContentObserver(Modules.CONTENT_URI, true, sDataObserver);
			Log.d(TAG,
                    "--------------------onReceive. sDataObserver = "
                            + sDataObserver);
		}
	}

    @Override
    public void onEnabled(Context context) {
        Log.d (TAG, ".onEnabled() - go setup the alarmManager");
        EllucianApplication ellucianApp = (EllucianApplication)context.getApplicationContext();
        ellucianApp.sendEvent(GoogleAnalyticsConstants.CATEGORY_WIDGET, GoogleAnalyticsConstants.ACTION_INSTALL, "Assignments", null, "AssignmentsWidgetProvider" );

        super.onEnabled(context);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        // InexactRepeating will not wake device
        am.setInexactRepeating(AlarmManager.RTC, 0, ellucianApp.DEFAULT_ASSIGNMENTS_REFRESH, pi);
    }

    @Override
    public void onDisabled(Context context) {
        // Run when the last AppWidget instance for this provider is deleted.
        // Cancel the scheduled refresh alarm
        EllucianApplication ellucianApp = (EllucianApplication)context.getApplicationContext();
        ellucianApp.sendEvent(GoogleAnalyticsConstants.CATEGORY_WIDGET, GoogleAnalyticsConstants.ACTION_UNINSTALL, "Assignments", null, "AssignmentsWidgetProvider" );

        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);

        super.onEnabled(context);
    }

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
        Log.d(TAG, "--------------------onUpdate BEGIN");

        updateRemoteView(context, appWidgetManager, appWidgetIds, null);

		super.onUpdate(context, appWidgetManager, appWidgetIds);
        }

	@SuppressLint("NewApi")
	class DataProviderObserver extends ContentObserver {
		private AppWidgetManager mAppWidgetManager;
		private ComponentName mComponentName;

		DataProviderObserver(AppWidgetManager mgr, ComponentName cn, Handler h) {
			super(h);
			mAppWidgetManager = mgr;
			mComponentName = cn;
		}

		@Override
		public void onChange(boolean selfChange) {
			onChange(selfChange, null);
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {

			// The data has changed, so notify the widget that the collection
			// view needs to be updated.
			// In response, the factory's onDataSetChanged() will be called
			// which will requery the cursor for the new data.

			Log.d(TAG,"--------------------Data Changed");

            mAppWidgetManager.notifyAppWidgetViewDataChanged(
					mAppWidgetManager.getAppWidgetIds(mComponentName),
					R.id.assignment_widget_list);
		}
	}
}