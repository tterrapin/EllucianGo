/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.events;

import java.util.Date;

import android.app.IntentService;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDualPaneActivity;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.client.services.CourseEventsIntentService;
import com.ellucian.mobile.android.provider.EllucianContract.CourseCourses;
import com.ellucian.mobile.android.provider.EllucianContract.CourseEvents;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;

public class CourseEventsActivity extends EllucianDefaultDualPaneActivity {
	protected String courseId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Must set courseId before call to super.onCreate to make sure the variable is ready for the loader
		courseId = getIntent().getStringExtra(Extra.COURSES_COURSE_ID);

		super.onCreate(savedInstanceState);
	}
	
	@Override
	public String getFragmentTag() {
		return "courseEventsListFragment";
	}
	
	@Override
	public Class<? extends EllucianDefaultListFragment> getListFragmentClass() {
		return CourseEventsListFragment.class;
	}
	
	@Override
	public Class<? extends IntentService> getIntentServiceClass() {
		return CourseEventsIntentService.class;
	}
	
	@Override
	public SimpleCursorAdapter getCursorAdapter() {
		return new SimpleCursorAdapter(
				this,
				R.layout.default_content_row, 
				null,
				new String[] {CourseEvents.EVENT_TITLE, CourseEvents.EVENT_DESCRIPTION, CourseEvents.EVENT_START}, 
				new int[] {R.id.row_title, R.id.row_description, R.id.row_date}, 
				0);
	}
	
	@Override
	public ViewBinder getCursorViewBinder() {
		return new CourseEventsViewBinder();
	}
	
	@Override
	public Loader<Cursor> getCursorLoader(int id, Bundle args) {
		return new CursorLoader(this, CourseEvents.CONTENT_URI, null,
				CourseCourses.COURSE_ID + "=?", new String[] {courseId}, CourseEvents.DEFAULT_SORT);
	}
   	
   	private class CourseEventsViewBinder implements SimpleCursorAdapter.ViewBinder {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int index) {
			if(index == cursor.getColumnIndex(CourseEvents.EVENT_START)) {
				String startDateString = cursor.getString(index);
				String endDateString = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_END));
				String allDayString = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_ALL_DAY));
				
				boolean allDay = Boolean.parseBoolean(allDayString);
				
				String output = "";
				if (!TextUtils.isEmpty(startDateString)) {
					Date startDate = CalendarUtils.parseFromUTC(startDateString);
					
					if (allDay) {
						output = getString(R.string.date_all_day_event_format, 
									CalendarUtils.getDefaultDateString(CourseEventsActivity.this, startDate));
					} else if (!TextUtils.isEmpty(endDateString)) {
						Date endDate = CalendarUtils.parseFromUTC(endDateString);
						if (CalendarUtils.getDefaultDateString(CourseEventsActivity.this, startDate)
								.equals(CalendarUtils.getDefaultDateString(CourseEventsActivity.this, endDate))) {
							
							output = getString(R.string.date_time_to_time_format, 
									CalendarUtils.getDefaultDateString(CourseEventsActivity.this, startDate),
									CalendarUtils.getDefaultTimeString(CourseEventsActivity.this, startDate),
									CalendarUtils.getDefaultTimeString(CourseEventsActivity.this, endDate));
						} else {
							output = getString(R.string.date_time_to_date_time_format, 
									CalendarUtils.getDefaultDateString(CourseEventsActivity.this, startDate),
									CalendarUtils.getDefaultTimeString(CourseEventsActivity.this, startDate),
									CalendarUtils.getDefaultDateString(CourseEventsActivity.this, endDate),
									CalendarUtils.getDefaultTimeString(CourseEventsActivity.this, endDate));
						}
					} else {
						output = getString(R.string.date_time_format, 
									CalendarUtils.getDefaultDateString(CourseEventsActivity.this, startDate),
									CalendarUtils.getDefaultTimeString(CourseEventsActivity.this, startDate));
					}
				} else {
					output = getString(R.string.unavailable);
				}
				
				((TextView) view).setText(output);
				return true;
			} else {
				return false;
			}
		}
	}
}
