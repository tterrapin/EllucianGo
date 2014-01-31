package com.ellucian.mobile.android.notifications;

import android.app.IntentService;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.MainActivity;

import com.ellucian.mobile.android.app.EllucianDefaultDualPaneActivity;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.client.services.NotificationsIntentService;

import com.ellucian.mobile.android.provider.EllucianContract.Notifications;

public class NotificationsActivity extends EllucianDefaultDualPaneActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	
	@Override
	public String getFragmentTag() {
		// sets the tag of the main / left split fragment
		// referenced in superclass onCreate
		return "notificationsListFragment";
	}
	
	@Override
	public SimpleCursorAdapter getCursorAdapter() {
		// creates an adapter 
		// referenced in superclass onCreate
		return new SimpleCursorAdapter(
				this,
				R.layout.default_single_line_row,
				null,
				new String[] {Notifications.NOTIFICATIONS_TITLE},
				new int[] {R.id.title},
				0);
	}
	
	@Override
	public Class<? extends EllucianDefaultListFragment> getListFragmentClass() {
		// sets the implementation class of the list fragment
		// referenced in superclass getListFragment
		return NotificationsListFragment.class;
	}
	
	@Override
	public ViewBinder getCursorViewBinder() {
		// returns a ViewBinder
		// referenced in superclass onCreate
		return new NotificationsViewBinder();
	}
	
	@Override
	public Class<? extends IntentService> getIntentServiceClass() {
		// sets the intent service
		// referenced in superclass onCreate
		return NotificationsIntentService.class;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// now we can call super.onCreate, with all methods / attributes
		// defined above
		super.onCreate(savedInstanceState);

		if(!getEllucianApp().isUserAuthenticated()) {
        	Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
		}
	}
	
	@Override
	public Loader<Cursor> getCursorLoader(int id, Bundle args) {
		// superclass onCreateLoader invokes this to create proper
		// loader
		return new CursorLoader(this, Notifications.CONTENT_URI, null,
				null, null,
				Notifications.DEFAULT_SORT);
	}
	
	private class NotificationsViewBinder implements SimpleCursorAdapter.ViewBinder {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int index) {
			if (index == cursor.getColumnIndex(Notifications.NOTIFICATIONS_TITLE)) {
				String title = cursor.getString(index);
				String output = getString(R.string.notifications_no_notifications);
				if (title != null) {
					output = title;
				}
				((TextView) view).setText(output);
				return true;
			} else {
				return false;
			}
		}
	}
    
    @Override
	public void onStart() {
		super.onStart();
		EllucianApplication ellucianApp = getEllucianApp();
		sendView("Notifications List", moduleName);
		if (ellucianApp.isUserAuthenticated()) {
			
			if (System.currentTimeMillis() > ellucianApp.getLastNotificationsCheck() + (60 * 1000)) {
				Log.d("MainActivity.onStart", "startingNotifications");
				ellucianApp.startNotifications();
				
			}
		}
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

}