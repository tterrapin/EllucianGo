/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.events;

import java.util.Date;

import android.database.Cursor;
import android.os.Bundle;

import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.provider.EllucianContract.Events;
import com.ellucian.mobile.android.util.Extra;

public class EventsListFragment extends EllucianDefaultListFragment {
	
	public EventsListFragment() {		
	}
	
	@Override
	public Bundle buildDetailBundle(Cursor cursor) {
		EventsActivity activity = (EventsActivity)getActivity();
		
		Bundle bundle = new Bundle();
		
		bundle.putString(Extra.MODULE_NAME, getEllucianActivity().moduleName);
		
		String title = cursor.getString(cursor.getColumnIndex(Events.EVENTS_TITLE));
		String content = cursor.getString(cursor.getColumnIndex(Events.EVENTS_DESCRIPTION));
		String location = cursor.getString(cursor.getColumnIndex(Events.EVENTS_LOCATION));
		String contact = cursor.getString(cursor.getColumnIndex(Events.EVENTS_CONTACT));
		String email = cursor.getString(cursor.getColumnIndex(Events.EVENTS_EMAIL));
		String start = cursor.getString(cursor.getColumnIndex(Events.EVENTS_START));
		String end = cursor.getString(cursor.getColumnIndex(Events.EVENTS_END));
		int allDay = cursor.getInt(cursor.getColumnIndex(Events.EVENTS_ALL_DAY));
		
		bundle.putString(Extra.TITLE, title);
		if (content != null) {
			bundle.putString(Extra.CONTENT, content);
		}
		if (location != null) {
			bundle.putString(Extra.LOCATION, location);
		}
		if (contact != null) {
			bundle.putString(Extra.CONTACT, contact);
		}
		if (email != null) {
			bundle.putString(Extra.EMAIL, email);
		}
		if (start != null) {
			Date startDate = activity.toEventDate(start);
			Date endDate = end != null ? activity.toEventDate(end) : null;
			String dateString;
			if( allDay == 0) {		
				dateString = activity.getEventDateFormattedString(startDate, endDate, false);
			} else {
				dateString = activity.getEventDateFormattedString(startDate, null, true);
			}
			bundle.putString(Extra.DATE, dateString);
			
			// Also add long times for start and end for use with native calendar
			bundle.putLong(Extra.START, startDate.getTime());
			if (allDay == 1 || endDate == null) {
				long endLong = -1;
				bundle.putLong(Extra.END, endLong);
			} else {
				bundle.putLong(Extra.END, endDate.getTime());
			}
		}
	
		
		return bundle;
	}
	
	@Override
	public Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
		return EventsDetailFragment.class;
	}
	
	@Override
	public Class<? extends EllucianDefaultDetailActivity> getDetailActivityClass() {
		return EventsDetailActivity.class;	
	}

	@Override
	public void onStart() {
		super.onStart();
		sendView("Events List", getEllucianActivity().moduleName);
	}

}
