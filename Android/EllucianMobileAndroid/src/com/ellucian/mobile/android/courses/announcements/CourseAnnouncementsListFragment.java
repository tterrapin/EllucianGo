package com.ellucian.mobile.android.courses.announcements;

import java.util.Date;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.provider.EllucianContract.CourseAnnouncements;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;

public class CourseAnnouncementsListFragment extends EllucianDefaultListFragment {
	
	public CourseAnnouncementsListFragment() {
	}
	
	@Override
	public Bundle buildDetailBundle(Cursor cursor) {
		Bundle bundle = new Bundle();
		
		String title = cursor.getString(cursor.getColumnIndex(CourseAnnouncements.ANNOUNCEMENT_TITLE));
		String dateString = cursor.getString(cursor.getColumnIndex(CourseAnnouncements.ANNOUNCEMENT_DATE));
		String content = cursor.getString(cursor.getColumnIndex(CourseAnnouncements.ANNOUNCEMENT_CONTENT));
		String url = cursor.getString(cursor.getColumnIndex(CourseAnnouncements.ANNOUNCEMENT_URL));
		
		if (!TextUtils.isEmpty(dateString)) {
			Date date = CalendarUtils.parseFromUTC(dateString);
			dateString = CalendarUtils.getDefaultDateTimeString(getActivity(), date);
		} else {
			dateString = getString(R.string.not_applicable);
		}
		
		bundle.putString(Extra.TITLE, title);
		bundle.putString(Extra.DATE, dateString);
		bundle.putString(Extra.CONTENT, content);
		bundle.putString(Extra.LINK, url);
		
		return bundle;
	}
	
	@Override
	public Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
		return CourseAnnouncementsDetailFragment.class;	
	}
	
	@Override
	public Class<? extends EllucianDefaultDetailActivity> getDetailActivityClass() {
		return CourseAnnouncementsDetailActivity.class;	
	}
	
	@Override
	public void onStart() {
		super.onStart();
		sendView("Course activity list", getEllucianActivity().moduleName);
	}
}
