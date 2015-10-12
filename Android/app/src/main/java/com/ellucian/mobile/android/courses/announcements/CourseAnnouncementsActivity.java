/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.announcements;

import android.app.IntentService;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDualPaneActivity;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.client.services.CourseAnnouncementsIntentService;
import com.ellucian.mobile.android.provider.EllucianContract.CourseAnnouncements;
import com.ellucian.mobile.android.provider.EllucianContract.CourseCourses;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;

import java.util.Date;

public class CourseAnnouncementsActivity extends EllucianDefaultDualPaneActivity {
	private String courseId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Must set courseId before call to super.onCreate to make sure the variable is ready for the loader
		courseId = getIntent().getStringExtra(Extra.COURSES_COURSE_ID);

		super.onCreate(savedInstanceState);
	}
	
	@Override
	public String getFragmentTag() {
		return "courseAnnouncementsListFragment";
	}
	
	@Override
	public Class<? extends EllucianDefaultListFragment> getListFragmentClass() {
		return CourseAnnouncementsListFragment.class;
	}
	
	@Override
	public Class<? extends IntentService> getIntentServiceClass() {
		return CourseAnnouncementsIntentService.class;
	}
	
	@Override
	public SimpleCursorAdapter getCursorAdapter() {
		return new SimpleCursorAdapter(
				this,
				R.layout.default_content_row, 
				null,
				new String[] {CourseAnnouncements.ANNOUNCEMENT_TITLE, CourseAnnouncements.ANNOUNCEMENT_CONTENT, CourseAnnouncements.ANNOUNCEMENT_DATE}, 
				new int[] {R.id.row_title, R.id.row_description, R.id.row_date}, 
				0);
	}
	
	@Override
	public ViewBinder getCursorViewBinder() {
		return new CourseAnnouncementsViewBinder();
	}
	
	@Override
	public Loader<Cursor> getCursorLoader(int id, Bundle args) {
		return new CursorLoader(this, CourseAnnouncements.CONTENT_URI, null,
   				CourseCourses.COURSE_ID + "=?", new String[] {courseId}, CourseAnnouncements.DEFAULT_SORT);
	}
	
 	
   	private class CourseAnnouncementsViewBinder implements SimpleCursorAdapter.ViewBinder {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int index) {
			if(index == cursor.getColumnIndex(CourseAnnouncements.ANNOUNCEMENT_CONTENT)) {
				String content = cursor.getString(index);
				

				if (TextUtils.isEmpty(content)) {	
					view.setVisibility(View.GONE);
				} 

				return true;
			} else if (index == cursor.getColumnIndex(CourseAnnouncements.ANNOUNCEMENT_DATE)) {
				String dateString = cursor.getString(index);
				Date date = CalendarUtils.parseFromUTC(dateString);
				
				String output = getString(R.string.not_applicable);
				if(date != null) {	
					output = CalendarUtils.getDefaultDateTimeString(CourseAnnouncementsActivity.this, date);
				} 
				((TextView) view).setText(output);
				return true;
			} else {
				return false;
			}
		}
	}
	
}
