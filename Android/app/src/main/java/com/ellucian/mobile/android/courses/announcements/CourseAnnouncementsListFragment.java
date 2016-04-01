/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.announcements;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.ilp.IlpDetailFragment;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.provider.EllucianContract.CourseAnnouncements;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;

import java.util.Date;

public class CourseAnnouncementsListFragment extends EllucianDefaultListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = CourseAnnouncementsListFragment.class.getSimpleName();
    private SimpleCursorAdapter mAdapter;
    private String courseId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_course_ilp, container, false);

        mAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.default_content_row,
                null,
                new String[] {CourseAnnouncements.ANNOUNCEMENT_TITLE, CourseAnnouncements.ANNOUNCEMENT_CONTENT, CourseAnnouncements.ANNOUNCEMENT_DATE},
                new int[] {R.id.row_title, R.id.row_description, R.id.row_date},
                0);
        mAdapter.setViewBinder(new CourseAnnouncementsViewBinder());
        setListAdapter(mAdapter);

        Bundle intentExtras = getActivity().getIntent().getExtras();
        courseId = intentExtras.getString(Extra.COURSES_COURSE_ID);
        String termId = intentExtras.getString(Extra.COURSES_TERM_ID);
        Log.d(TAG, "courseId : " + courseId);
        Log.d(TAG, "termId : " + termId);

        getLoaderManager().initLoader(0, null, this);

        Bundle bundle = getArguments();
        if (bundle != null) {
            int emptyTextResId = bundle.getInt("emptyTextResId");
            if (emptyTextResId != 0) {
                TextView emptyView = (TextView) rootView.findViewById(android.R.id.empty);
                emptyView.setText(emptyTextResId);
            }
        }

        return rootView;
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), CourseAnnouncements.CONTENT_URI, null,
                EllucianContract.CourseCourses.COURSE_ID + "=?", new String[] {courseId}, CourseAnnouncements.DEFAULT_SORT);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursor) {
        mAdapter.swapCursor(null);
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
                    output = CalendarUtils.getDefaultDateTimeString(getContext(), date);
                }
                ((TextView) view).setText(output);
                return true;
            } else {
                return false;
            }
        }
    }
}
