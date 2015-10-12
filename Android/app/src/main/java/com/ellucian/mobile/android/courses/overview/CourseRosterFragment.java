/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.overview;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianListFragment;
import com.ellucian.mobile.android.provider.EllucianContract.CourseRoster;
import com.ellucian.mobile.android.util.Extra;


public class CourseRosterFragment extends EllucianListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private SimpleCursorAdapter mAdapter;
	private String courseId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_course_roster, container, false);
        
        mAdapter = new SimpleCursorAdapter(getActivity(),
        		R.layout.course_roster_row, null,
				new String[] {CourseRoster.ROSTER_FORMATTED_NAME }, 
				new int[] {R.id.course_roster_row_name}, 0);
		//mAdapter.setViewBinder(new CourseGradesViewBinder());
		setListAdapter(mAdapter);
		
		courseId = getActivity().getIntent().getStringExtra(Extra.COURSES_COURSE_ID);
		Log.d("CourseRosterFragment", "courseId: " + courseId);
		
		getLoaderManager().initLoader(0, null, this);

        return rootView;
    }

    @Override
   	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
   		return new CursorLoader(getActivity(), CourseRoster.CONTENT_URI, null,
   				CourseRoster.ROSTER_COURSE_ID + "= ?", new String[] { courseId }, CourseRoster.DEFAULT_SORT);
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
		sendView("Course roster list", getEllucianActivity().moduleName);
	}
    
    
}
