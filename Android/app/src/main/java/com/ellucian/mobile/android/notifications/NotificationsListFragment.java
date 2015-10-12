/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.notifications;

import java.util.Date;

import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.ellucian.elluciango.R;

import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.client.notifications.Notification;
import com.ellucian.mobile.android.client.services.NotificationsUpdateDatabaseService;
import com.ellucian.mobile.android.client.services.NotificationsUpdateServerService;
import com.ellucian.mobile.android.provider.EllucianContract.Notifications;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;

public class NotificationsListFragment extends EllucianDefaultListFragment {
	private static final String TAG = NotificationsListFragment.class.getSimpleName();
	
	public NotificationsListFragment() {
	}
	
	// Overriding to check for and update READ status	
	@Override
	public void showDetails(int index) {
        mCurCheckPosition = index;

        String statusesString = detailBundle.getString(Extra.NOTIFICATIONS_STATUSES);
        boolean read = false;
		if (!TextUtils.isEmpty(statusesString)) {
			String[] statuses = statusesString.split(",");
			for (String status : statuses) {
				if (status.equals(Notification.STATUS_READ)) {
					read = true;
				}
			}
		}
        if (!read) {
        	updateNotificationToRead();
		}

        if (mDualPane) {
            //We can display everything in-place with fragments
            
            EllucianDefaultDetailFragment details = (EllucianDefaultDetailFragment)
                    getFragmentManager().findFragmentById(R.id.frame_extra);
        
            details = getDetailFragment(detailBundle, index);

            // Execute a transaction, replacing any existing fragment
            // with this one inside the frame.
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.frame_extra, details);

            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
            
        } else {
            // Otherwise we need to launch a new activity to display
            // the dialog fragment with selected text.
        	
            Intent intent = new Intent();
            intent.setClass(getActivity(), getDetailActivityClass());
            intent.putExtras(detailBundle); 
            intent.putExtra("index", index);
            intent = addExtras(intent);
            // startActivityForResult for NotificationsDetailActivity to handle delete requests
            getActivity().startActivityForResult(intent, NotificationsActivity.NOTIFICATIONS_DETAIL_REQUEST_CODE);
            
        }
    }
	 
	/**
	 * Starts services to update READ status in both the database and the server.
	 */
	private void updateNotificationToRead() {
		Log.d(TAG, "updating notification to read status.");
		
		Intent updateDatabaseIntent = new Intent(getActivity(), NotificationsUpdateDatabaseService.class);
		updateDatabaseIntent.putExtra(Extra.NOTIFICATIONS_MODIFICATION_TYPE, NotificationsUpdateDatabaseService.MODIFICATION_READ);
		updateDatabaseIntent.putExtra(Extra.ID, detailBundle.getString(Extra.ID));
		updateDatabaseIntent.putExtra(Extra.NOTIFICATIONS_STATUSES, detailBundle.getString(Extra.NOTIFICATIONS_STATUSES));
        getActivity().startService(updateDatabaseIntent); 
        
        Intent updateServerIntent = new Intent(getActivity(), NotificationsUpdateServerService.class);
        updateServerIntent.putExtra(Extra.NOTIFICATIONS_MODIFICATION_TYPE, NotificationsUpdateServerService.MODIFICATION_READ);
        updateServerIntent.putExtra(Extra.ID, detailBundle.getString(Extra.ID));
        updateServerIntent.putExtra(Extra.REQUEST_URL, 
        		getEllucianActivity().getEllucianApp().getMobileNotificationsUrl());
        getActivity().startService(updateServerIntent);
        
	}


	@Override
	public Bundle buildDetailBundle(Cursor cursor) {
		Bundle bundle = new Bundle();
		
		bundle.putString(Extra.MODULE_NAME, getEllucianActivity().moduleName);
		
		String id = cursor.getString(cursor.getColumnIndex(Notifications.NOTIFICATIONS_ID));
		String title = cursor.getString(cursor.getColumnIndex(Notifications.NOTIFICATIONS_TITLE));
		String details = cursor.getString(cursor.getColumnIndex(Notifications.NOTIFICATIONS_DETAILS));
		String linkLabel = cursor.getString(cursor.getColumnIndex(Notifications.NOTIFICATIONS_LINK_LABEL));
		String link = cursor.getString(cursor.getColumnIndex(Notifications.NOTIFICATIONS_HYPERLINK));
		String date = cursor.getString(cursor.getColumnIndex(Notifications.NOTIFICATIONS_DATE));
		int sticky = cursor.getInt(cursor.getColumnIndex(Notifications.NOTIFICATIONS_STICKY));
		String statusesString = cursor.getString(cursor.getColumnIndex(Notifications.NOTIFICATIONS_STATUSES));

		
		bundle.putString(Extra.NOTIFICATIONS_STATUSES, statusesString);
		bundle.putString(Extra.ID, id);
		bundle.putString(Extra.TITLE, title);
		if (details != null) {
			bundle.putString(Extra.CONTENT, details);
		}
		if (linkLabel != null) {
			bundle.putString(Extra.LINK_LABEL, linkLabel);
		}
		if (link != null) {
			bundle.putString(Extra.LINK, link);
		}
		
		if(date != null) {
			Date convDate = CalendarUtils.parseFromUTC(date);
			final java.text.DateFormat dateFormat = android.text.format.DateFormat
					.getDateFormat(getActivity().getApplicationContext());
			String dateString = dateFormat.format(convDate);
		
			bundle.putString(Extra.DATE, dateString);					
		}
		
		bundle.putInt(Extra.NOTIFICATIONS_STICKY, sticky);
		return bundle;
	}
	
	@Override
	public Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
		return NotificationsDetailFragment.class;
	}
	
	@Override
	public Class<? extends EllucianDefaultDetailActivity> getDetailActivityClass() {
		return NotificationsDetailActivity.class;
	}
}
