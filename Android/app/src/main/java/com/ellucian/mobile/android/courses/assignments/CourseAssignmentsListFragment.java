/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.assignments;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.ilp.IlpDetailFragment;
import com.ellucian.mobile.android.provider.EllucianContract.CourseAssignments;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;

import java.util.Date;

public class CourseAssignmentsListFragment extends EllucianDefaultListFragment {
	
	public CourseAssignmentsListFragment() {		
	}
	
	@Override
	public Bundle buildDetailBundle(Cursor cursor) {
		Bundle bundle = new Bundle();
		
		String title = cursor.getString(cursor.getColumnIndex(CourseAssignments.ASSIGNMENT_NAME));
		String dateString = cursor.getString(cursor.getColumnIndex(CourseAssignments.ASSIGNMENT_DUE));
		String content = cursor.getString(cursor.getColumnIndex(CourseAssignments.ASSIGNMENT_DESCRIPTION));
		String url = cursor.getString(cursor.getColumnIndex(CourseAssignments.ASSIGNMENT_URL));
		String section = cursor.getString(cursor.getColumnIndex(CourseAssignments.ASSIGNMENT_SECTION_NAME));
		
		if (!TextUtils.isEmpty(dateString)) {
			Date date = CalendarUtils.parseFromUTC(dateString);
			dateString = CalendarUtils.getDefaultDateTimeString(getActivity(), date);
			bundle.putLong(Extra.START, date.getTime());
		} else {
			dateString = getString(R.string.course_assignments_none_assigned);
		}
		
		String dateLabel = getString(R.string.course_assignments_due);
		
		bundle.putString(Extra.TITLE, title);
		bundle.putString(Extra.DATE, dateString);
		bundle.putString(Extra.CONTENT, content);
		bundle.putString(Extra.LINK, url);
		bundle.putString(Extra.DATE_LABEL, dateLabel);
		bundle.putString(Extra.HEADER_SECTION_NAME, section);
		// We use the IlpDetailFragment to display the detail view.
		bundle.putString(IlpDetailFragment.DETAIL_TYPE, IlpDetailFragment.DETAIL_TYPE_ASSIGNMENTS);
		return bundle;
	}
	
	@Override
	public Class<? extends IlpDetailFragment> getDetailFragmentClass() {
		return CourseAssignmentsDetailFragment.class;
	}
	
	@Override
	public Class<? extends EllucianDefaultDetailActivity> getDetailActivityClass() {
		return CourseAssignmentsDetailActivity.class;	
	}

	@Override
	public void onStart() {
		super.onStart();
		sendView("Course assignments list", getEllucianActivity().moduleName);
	}
	
	
}
