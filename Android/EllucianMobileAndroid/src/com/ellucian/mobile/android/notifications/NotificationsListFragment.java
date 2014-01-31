package com.ellucian.mobile.android.notifications;

import java.util.Date;

import android.database.Cursor;
import android.os.Bundle;

import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.notifications.NotificationsDetailActivity;

import com.ellucian.mobile.android.provider.EllucianContract.Notifications;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;

public class NotificationsListFragment extends EllucianDefaultListFragment {

	public NotificationsListFragment() {
	}

	@Override
	public Bundle buildDetailBundle(Cursor cursor) {
		Bundle bundle = new Bundle();
		
		String title = cursor.getString(cursor.getColumnIndex(Notifications.NOTIFICATIONS_TITLE));
		String details = cursor.getString(cursor.getColumnIndex(Notifications.NOTIFICATIONS_DETAILS));
		String linkLabel = cursor.getString(cursor.getColumnIndex(Notifications.NOTIFICATIONS_LINK_LABEL));
		String link = cursor.getString(cursor.getColumnIndex(Notifications.NOTIFICATIONS_HYPERLINK));
		String date = cursor.getString(cursor.getColumnIndex(Notifications.NOTIFICATIONS_DATE));
		
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
