/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.overview;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianListFragment;
import com.ellucian.mobile.android.provider.EllucianContract.Grades;
import com.ellucian.mobile.android.provider.EllucianContract.GradesCourses;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;

import java.util.Date;


public class CourseGradesFragment extends EllucianListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private SimpleCursorAdapter mAdapter;
	private String courseId;
	private String uniqueId;
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_course_grades, container, false);
        
        mAdapter = new SimpleCursorAdapter(getActivity(),
				R.layout.grade_row, null,
				new String[] {Grades.GRADE_NAME, Grades.GRADE_VALUE, Grades.GRADE_UPDATED}, 
				new int[] {R.id.grade_row_label, R.id.grade_row_value, R.id.grade_row_date}, 0);
		mAdapter.setViewBinder(new CourseGradesViewBinder());
		setListAdapter(mAdapter);
		
		Bundle intentExtras = getActivity().getIntent().getExtras();
		courseId = intentExtras.getString(Extra.COURSES_COURSE_ID);
		uniqueId = intentExtras.getString(Extra.COURSES_TERM_ID) + " - " + courseId;
		Log.d("CourseGradesFragment", "courseId : " + courseId);
		Log.d("CourseGradesFragment", "uniqueId : " + uniqueId);
		
		getLoaderManager().initLoader(0, null, this);
        
        return rootView;
    }

    @Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), Grades.CONTENT_URI, null,
				GradesCourses.COURSE_ID + "= ?", new String[] { uniqueId }, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursor) {
		mAdapter.swapCursor(null);

	}
	
	private class CourseGradesViewBinder implements SimpleCursorAdapter.ViewBinder {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int index) {
			if(index == cursor.getColumnIndex(Grades.GRADE_UPDATED)) {
				String dateString = cursor.getString(index);
							
				Date date = CalendarUtils.parseFromUTC(dateString);
				
				// TODO - Set this back when the main format gets fixed
				//Date date = EllucianDatabase.toDate(cursor.getString(index));
				
				String output = CourseGradesFragment.this.getString(R.string.unavailable);
				if(date != null) {	
					output = CalendarUtils.getDefaultDateTimeString(getActivity(), date);
				} 
				
				((TextView) view).setText(output);
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		sendView("Course grades", getEllucianActivity().moduleName);
	}
	
	
    
}
