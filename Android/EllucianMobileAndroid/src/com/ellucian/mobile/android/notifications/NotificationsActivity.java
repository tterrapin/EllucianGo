package com.ellucian.mobile.android.notifications;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.MainActivity;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.client.notifications.Notification;
import com.ellucian.mobile.android.client.services.NotificationsUpdateDatabaseService;
import com.ellucian.mobile.android.client.services.NotificationsUpdateServerService;
import com.ellucian.mobile.android.provider.EllucianContract.Notifications;
import com.ellucian.mobile.android.util.Extra;

public class NotificationsActivity extends EllucianActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	public static final String TAG = NotificationsActivity.class.getSimpleName();
	public static final int NOTIFICATIONS_DETAIL_REQUEST_CODE = 8888;
	public static final int RESULT_DELETE = 9999;
	private EllucianDefaultListFragment mainFragment;
	private SimpleCursorAdapter adapter;
	private NotificationsDatabaseUpdatedReceiver databaseUpdatedReceiver;
	private boolean resetPosition;
	private boolean initialLoaderCompleted;
	private boolean deleteAfterLoad;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_default_dual_pane);
		
		if(!TextUtils.isEmpty(moduleName)) {
			setTitle(moduleName);
		}
		
		EllucianApplication app = getEllucianApp();
		if(!app.isUserAuthenticated()) {
			Log.e(TAG, "User not authenticated, sending to home.");
        	Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
		}
		
		FragmentManager manager = getFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		mainFragment =  (EllucianDefaultListFragment) manager.findFragmentByTag("notificationsListFragment");

		adapter = new SimpleCursorAdapter(
				this,
				R.layout.notifications_row,
				null,
				new String[] {Notifications.NOTIFICATIONS_STATUSES, Notifications.NOTIFICATIONS_STICKY},
				new int[] {R.id.info_container, R.id.sticky_marker},
				0);
		
		if (mainFragment == null) {
			mainFragment = EllucianDefaultListFragment.newInstance(this, 
					NotificationsListFragment.class.getName(), null);
			
			mainFragment.setListAdapter(adapter);
			transaction.add(R.id.frame_main, mainFragment, "notificationsListFragment");
		} else {
			mainFragment.setListAdapter(adapter);
			transaction.attach(mainFragment);
		}
		
		ViewBinder viewBinder = new NotificationsViewBinder();
		if (viewBinder != null) {
			mainFragment.setViewBinder(viewBinder);
		}
		
		transaction.commit();
		
		getLoaderManager().restartLoader(0, null, this);
				
		// Only want to query server on first load, it might conflict with current database state
		if (savedInstanceState == null  || !savedInstanceState.containsKey("loaded")) {
			Log.d(TAG, "startingNotifications");
			getEllucianApp().startNotifications();
		} 
		
	}
	
	@Override
	public void onStart() {
		super.onStart();
		sendView("Notifications List", moduleName);
	}
    
    @Override
	protected void onResume() {
		super.onResume();
		databaseUpdatedReceiver = new NotificationsDatabaseUpdatedReceiver();
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
		lbm.registerReceiver(databaseUpdatedReceiver, new IntentFilter(NotificationsUpdateDatabaseService.ACTION_DATABASE_UPDATED));
	}
    
    @Override
    protected void onPause() {
    	super.onPause();
    	LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
    	lbm.unregisterReceiver(databaseUpdatedReceiver);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	outState.putBoolean("loaded", true);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
    
    @Override
   	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
   		return new CursorLoader(this, Notifications.CONTENT_URI, null,
				null, null,
				Notifications.DEFAULT_SORT);
   	}

   	@Override
   	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
   		adapter.swapCursor(cursor);
   		initialLoaderCompleted = true;
   		createNotifyHandler(mainFragment);
   	}

   	@Override
   	public void onLoaderReset(Loader<Cursor> cursor) {
   		adapter.swapCursor(null);
   	}
   	
	
	protected void createNotifyHandler(final EllucianDefaultListFragment fragment) {
   		Handler handler = new Handler(Looper.getMainLooper());
   		handler.post(new Runnable(){

			@Override
			public void run() {
				
				// If requestedNotificationId not empty then this came from an external notification request
				Intent incomingIntent = getIntent();
				String requestedNotificationId = incomingIntent.getStringExtra(Extra.NOTIFICATIONS_NOTIFICATION_ID);
				Log.d(TAG, "requestedNotificationId: " + requestedNotificationId);	
				
				if (!TextUtils.isEmpty(requestedNotificationId)) {
					Log.d(TAG, "requestedNotificationId found, looking in the adapter");
					int position = findNotificationInAdapter(requestedNotificationId);
					Log.d(TAG, "found at position: " + position);
					
					boolean forceClickOnSinglePane = position != -1 ? true : false;
					fragment.setInitialCursorPosition(position, forceClickOnSinglePane);
					// reset back after found
					if (position != -1) {
						incomingIntent.removeExtra(Extra.NOTIFICATIONS_NOTIFICATION_ID);
					}
				} else {
					fragment.setInitialCursorPosition(resetPosition);
					resetPosition = false;
				}
				
				if (deleteAfterLoad) {
					deleteAfterLoad = false;
					deleteNotification();
				}
			}  			
   		});
   	}
	
	private int findNotificationInAdapter(String idToFind) {
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			Cursor cursor = (Cursor) adapter.getItem(i);
			String idInCursor = cursor.getString(cursor.getColumnIndex(Notifications.NOTIFICATIONS_ID));
			if (idInCursor.equals(idToFind)) {
				return i;
			}
		}
		return -1;
	}

    // If in single-pane mode this will be called from the NotificationDetailActivity to signal a delete
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == NOTIFICATIONS_DETAIL_REQUEST_CODE && resultCode == RESULT_DELETE) {
    		// Check if the loader is completed updating the adapter, if so delete
    		// if not set flag to delete after loader is complete
    		if (initialLoaderCompleted) {
    			deleteAfterLoad = false;
    			deleteNotification();   			
    		} else {
    			deleteAfterLoad = true;
    		}
    		
    	}
    }
    
    protected void deleteNotification() {

    	int position = mainFragment.getCurrentPosition();
    	Cursor cursor  = (Cursor)mainFragment.getListView().getItemAtPosition(position);
    	
    	String id = cursor.getString(cursor.getColumnIndex(Notifications.NOTIFICATIONS_ID));
    	
    	Intent updateDatabaseIntent = new Intent(this, NotificationsUpdateDatabaseService.class);
		updateDatabaseIntent.putExtra(Extra.NOTIFICATIONS_MODIFICATION_TYPE, NotificationsUpdateDatabaseService.MODIFICATION_DELETE);
		updateDatabaseIntent.putExtra(Extra.ID, id);
        startService(updateDatabaseIntent); 
        
        Intent updateServerIntent = new Intent(this, NotificationsUpdateServerService.class);
        updateServerIntent.putExtra(Extra.NOTIFICATIONS_MODIFICATION_TYPE, NotificationsUpdateServerService.MODIFICATION_DELETE);
        updateServerIntent.putExtra(Extra.ID, id);
        updateServerIntent.putExtra(Extra.REQUEST_URL, getEllucianApp().getMobileNotificationsUrl());
        startService(updateServerIntent);
    	
    	setProgressBarIndeterminateVisibility(true);
    }    	

    private class NotificationsDatabaseUpdatedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			setProgressBarIndeterminateVisibility(false);
			// Only reset the list on delete 	
			resetPosition = intent.getBooleanExtra(NotificationsUpdateDatabaseService.ACTION_RESET_LIST, false);
			getLoaderManager().restartLoader(0, null, NotificationsActivity.this);			
		}    	

    }
    
    private class NotificationsViewBinder implements SimpleCursorAdapter.ViewBinder {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int index) {
			// checks to see if READ status is set if not sets text to bold
			if (index == cursor.getColumnIndex(Notifications.NOTIFICATIONS_STATUSES)) {
				String statusesString = cursor.getString(index);
				boolean read = false;
				if (!TextUtils.isEmpty(statusesString)) {
					String[] statuses = statusesString.split(",");
					for (String status : statuses) {
						if (status.equals(Notification.STATUS_READ)) {
							read = true;
						}
					}
				} 

				TextView titleView = (TextView) view.findViewById(R.id.title);	
				String title = cursor.getString(cursor.getColumnIndex(Notifications.NOTIFICATIONS_TITLE));
				if (!read) {
					titleView.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
				} else {
					titleView.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
				}
				titleView.setText(title);
				
				TextView descriptionView = (TextView) view.findViewById(R.id.description);	
				String description = cursor.getString(cursor.getColumnIndex(Notifications.NOTIFICATIONS_DETAILS));
				if (!read) {
					descriptionView.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
				} else {
					titleView.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
				}
				descriptionView.setText(description);
				
				return true;
			} else if (index == cursor.getColumnIndex(Notifications.NOTIFICATIONS_STICKY)) {	
				int sticky = cursor.getInt(index);
				if (sticky == 1) {
					view.setVisibility(View.VISIBLE);
				} else {
					view.setVisibility(View.GONE);
				}
				return true;
			} else {
				return false;
			}
		}
	}

}
