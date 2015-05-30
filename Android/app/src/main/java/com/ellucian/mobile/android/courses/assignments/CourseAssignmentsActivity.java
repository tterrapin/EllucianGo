/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.assignments;

import java.util.Date;

import android.app.IntentService;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDualPaneActivity;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.client.services.CourseAssignmentsIntentService;
import com.ellucian.mobile.android.provider.EllucianContract.CourseAssignments;
import com.ellucian.mobile.android.provider.EllucianContract.CourseCourses;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;

public class CourseAssignmentsActivity extends EllucianDefaultDualPaneActivity {
	protected String courseId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Must set courseId before call to super.onCreate to make sure the variable is ready for the loader
		courseId = getIntent().getStringExtra(Extra.COURSES_COURSE_ID);
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public String getFragmentTag() {
		return "courseAssignmentsListFragment";
	}
	
	@Override
	public Class<?extends EllucianDefaultListFragment> getListFragmentClass() {
		return CourseAssignmentsListFragment.class;
	}
	
	@Override
	public Class<? extends IntentService> getIntentServiceClass() {
		return CourseAssignmentsIntentService.class;
	}
	
	@Override
	public SimpleCursorAdapter getCursorAdapter() {
		return new SimpleCursorAdapter(
				this,
				R.layout.default_content_row, 
				null,
				new String[] {CourseAssignments.ASSIGNMENT_NAME, CourseAssignments.ASSIGNMENT_DESCRIPTION, CourseAssignments.ASSIGNMENT_DUE}, 
				new int[] {R.id.row_title, R.id.row_description, R.id.row_date}, 
				0);
	}
	
	@Override
	public ViewBinder getCursorViewBinder() {
		return new CourseAssignmentsViewBinder();
	}
	
	@Override
	public Loader<Cursor> getCursorLoader(int id, Bundle args) {
		return new CursorLoader(this, CourseAssignments.CONTENT_URI, null,
   				CourseCourses.COURSE_ID + "=?", new String[] {courseId}, CourseAssignments.DEFAULT_SORT);
	}
	
	
   	private class CourseAssignmentsViewBinder implements SimpleCursorAdapter.ViewBinder {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int index) {
			if(index == cursor.getColumnIndex(CourseAssignments.ASSIGNMENT_DUE)) {
				String dateString = cursor.getString(index);
				Date date = CalendarUtils.parseFromUTC(dateString);
				
				String output = getString(R.string.course_assignments_none_assigned);
				if(date != null) {	
					output = CalendarUtils.getDefaultDateTimeString(CourseAssignmentsActivity.this, date);
				} 
				((TextView) view).setText(output);
				
				View parent = (View) view.getParent();
				TextView dateLableView = (TextView) parent.findViewById(R.id.row_date_label);
				dateLableView.setText(getString(R.string.row_due_label));
				return true;
			} else {
				return false;
			}
		}
	}


}
