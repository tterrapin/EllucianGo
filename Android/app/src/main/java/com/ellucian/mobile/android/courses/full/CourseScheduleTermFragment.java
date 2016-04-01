/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.full;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianListFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.courses.overview.CourseOverviewActivity;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.provider.EllucianContract.CourseCourses;
import com.ellucian.mobile.android.provider.EllucianContract.CourseTerms;
import com.ellucian.mobile.android.util.Extra;

public class CourseScheduleTermFragment extends EllucianListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private SimpleCursorAdapter mAdapter;
	private String termId;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_courses_in_term,
				container, false);

		mAdapter = new SimpleCursorAdapter(getActivity(),
				R.layout.row_courses_full_schedule, null, new String[] {
						EllucianContract.CourseCourses.COURSE_NAME,
						EllucianContract.CourseCourses.COURSE_SECTION_NUMBER,
						EllucianContract.CourseCourses.COURSE_TITLE },
				new int[] { R.id.row_courses_full_schedule_name,
						R.id.row_courses_full_schedule_section_number,
						R.id.row_courses_full_schedule_title }, 0);
		//mAdapter.setViewBinder(new CoursesTermViewBinder());
		setListAdapter(mAdapter);
		
		termId = getArguments().getString("termId");
		
		getLoaderManager().initLoader(0, null, this);
		
		return rootView;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		
		sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_COURSES, GoogleAnalyticsConstants.ACTION_BUTTON_PRESS, "Click Course", null, getEllucianActivity().moduleName);
		
		Cursor cursor = (Cursor) l.getItemAtPosition(position);
		String courseId = cursor.getString(cursor.getColumnIndex(CourseCourses.COURSE_ID));
		String termId = cursor.getString(cursor.getColumnIndex(CourseTerms.TERM_ID));
		int instructorInt = cursor.getInt(cursor.getColumnIndex(CourseCourses.COURSE_IS_INSTRUCTOR));
		String courseName = cursor.getString(cursor.getColumnIndex(CourseCourses.COURSE_NAME));
		String sectionNumber = cursor.getString(cursor.getColumnIndex(CourseCourses.COURSE_SECTION_NUMBER));
		
		boolean isInstructor = false;
		if (instructorInt == 1) {
			isInstructor = true;
		}
		
		Intent intent = new Intent(getActivity(), CourseOverviewActivity.class);
		intent.putExtras(getActivity().getIntent().getExtras());
		intent.putExtra(Extra.COURSES_COURSE_ID, courseId);
		intent.putExtra(Extra.COURSES_TERM_ID, termId);
		intent.putExtra(Extra.COURSES_IS_INSTRUCTOR, isInstructor);
		intent.putExtra(Extra.COURSES_NAME, courseName);
		intent.putExtra(Extra.COURSES_SECTION_NUMBER, sectionNumber);
		startActivity(intent);
		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), CourseCourses.CONTENT_URI, null,
				CourseTerms.TERM_ID + "= ?", new String[] { termId },
                CourseCourses.COURSE_NAME + " ASC, " +
                CourseCourses.COURSE_SECTION_NUMBER + " ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursor) {
		mAdapter.swapCursor(null);

	}

	@Override
	public void onStart() {
		super.onStart();
		sendView("Schedule (full schedule)", getEllucianActivity().moduleName);
	}
	
	
}
