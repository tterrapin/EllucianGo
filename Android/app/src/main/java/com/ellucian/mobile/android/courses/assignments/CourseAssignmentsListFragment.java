package com.ellucian.mobile.android.courses.assignments;

import java.util.Date;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.provider.EllucianContract.CourseAssignments;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;

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
		
		if (!TextUtils.isEmpty(dateString)) {
			Date date = CalendarUtils.parseFromUTC(dateString);
			dateString = CalendarUtils.getDefaultDateTimeString(getActivity(), date);
		} else {
			dateString = getString(R.string.course_assignments_none_assigned);
		}
		
		String dateLabel = getString(R.string.course_assignments_due);
		
		bundle.putString(Extra.TITLE, title);
		bundle.putString(Extra.DATE, dateString);
		bundle.putString(Extra.CONTENT, content);
		bundle.putString(Extra.LINK, url);
		bundle.putString(Extra.DATE_LABEL, dateLabel);
		
		return bundle;
	}
	
	@Override
	public Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
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
