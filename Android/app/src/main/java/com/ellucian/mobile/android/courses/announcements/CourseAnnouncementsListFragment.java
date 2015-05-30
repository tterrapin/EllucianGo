/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.announcements;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.ilp.IlpDetailFragment;
import com.ellucian.mobile.android.provider.EllucianContract.CourseAnnouncements;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;

import java.util.Date;

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
		String section = cursor.getString(cursor.getColumnIndex(CourseAnnouncements.ANNOUNCEMENT_SECTION_NAME));
		
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
		bundle.putString(Extra.HEADER_SECTION_NAME, section);
		// We use the IlpDetailFragment to display the detail view.
		bundle.putString(IlpDetailFragment.DETAIL_TYPE, IlpDetailFragment.DETAIL_TYPE_ANNOUNCEMENTS);
		return bundle;
	}
	
	@Override
	public Class<? extends IlpDetailFragment> getDetailFragmentClass() {
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
