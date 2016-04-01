/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.events;

import android.app.Activity;
import android.content.Context;
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
import com.ellucian.mobile.android.provider.EllucianContract.CourseEvents;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;

import java.util.Date;

public class CourseEventsListFragment extends EllucianDefaultListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = CourseEventsListFragment.class.getSimpleName();
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
                new String[] {CourseEvents.EVENT_TITLE, CourseEvents.EVENT_DESCRIPTION, CourseEvents.EVENT_START},
                new int[] {R.id.row_title, R.id.row_description, R.id.row_date},
                0);
        mAdapter.setViewBinder(new CourseEventsViewBinder());
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
		
		String title = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_TITLE));
		String startDateString = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_START));
		String endDateString = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_END));
		String content = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_DESCRIPTION));
		String location = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_LOCATION));
		String allDayString = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_ALL_DAY));
		String section = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_SECTION_NAME));
		
		boolean allDay = Boolean.parseBoolean(allDayString);
		
		String output = "";
		if (!TextUtils.isEmpty(startDateString)) {
			Date startDate = CalendarUtils.parseFromUTC(startDateString);
			bundle.putLong(Extra.START, startDate.getTime());
			
			Activity activity = getActivity();
			
			if (allDay) {
				output = getString(R.string.date_all_day_event_format, 
							CalendarUtils.getDefaultDateString(activity, startDate));
				bundle.putLong(Extra.END, -1);
			} else if (!TextUtils.isEmpty(endDateString)) {
				Date endDate = CalendarUtils.parseFromUTC(endDateString);

				if (CalendarUtils.getDefaultDateString(activity, startDate)
						.equals(CalendarUtils.getDefaultDateString(activity, endDate))) {
					
					output = getString(R.string.date_time_to_time_format, 
							CalendarUtils.getDefaultDateString(activity, startDate),
							CalendarUtils.getDefaultTimeString(activity, startDate),
							CalendarUtils.getDefaultTimeString(activity, endDate));
				} else {
					output = getString(R.string.date_time_to_date_time_format, 
							CalendarUtils.getDefaultDateString(activity, startDate),
							CalendarUtils.getDefaultTimeString(activity, startDate),
							CalendarUtils.getDefaultDateString(activity, endDate),
							CalendarUtils.getDefaultTimeString(activity, endDate));
				}
				
				
				bundle.putLong(Extra.END, endDate.getTime());
			} else {
				output = getString(R.string.date_time_format, 
							CalendarUtils.getDefaultDateString(activity, startDate),
							CalendarUtils.getDefaultTimeString(activity, startDate));
				bundle.putLong(Extra.END, -1);
			}
		} else {
			output = getString(R.string.unavailable);
		}
		
		bundle.putString(Extra.TITLE, title);
		bundle.putString(Extra.DATE, output);
		bundle.putString(Extra.CONTENT, content);
		bundle.putString(Extra.LOCATION, location);
		bundle.putString(Extra.HEADER_SECTION_NAME, section);
		// We use the IlpDetailFragment to display the detail view.
		bundle.putString(IlpDetailFragment.DETAIL_TYPE, IlpDetailFragment.DETAIL_TYPE_EVENTS);
		return bundle;
	}
	
	@Override
	public Class<? extends IlpDetailFragment> getDetailFragmentClass() {
		return CourseEventsDetailFragment.class;	
	}
	
	@Override
	public Class<? extends EllucianDefaultDetailActivity> getDetailActivityClass() {
		return CourseEventsDetailActivity.class;	
	}

	@Override
	public void onStart() {
		super.onStart();
		sendView("Course events list", getEllucianActivity().moduleName);
	}

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), CourseEvents.CONTENT_URI, null,
                EllucianContract.CourseCourses.COURSE_ID + "=?", new String[] {courseId}, CourseEvents.DEFAULT_SORT);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursor) {
        mAdapter.swapCursor(null);
    }

    private class CourseEventsViewBinder implements SimpleCursorAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int index) {
            if(index == cursor.getColumnIndex(CourseEvents.EVENT_START)) {
                String startDateString = cursor.getString(index);
                String endDateString = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_END));
                String allDayString = cursor.getString(cursor.getColumnIndex(CourseEvents.EVENT_ALL_DAY));

                boolean allDay = Boolean.parseBoolean(allDayString);

                Context context = getContext();
                String output = "";
                if (!TextUtils.isEmpty(startDateString)) {
                    Date startDate = CalendarUtils.parseFromUTC(startDateString);

                    if (allDay) {
                        output = getString(R.string.date_all_day_event_format,
                                CalendarUtils.getDefaultDateString(context, startDate));
                    } else if (!TextUtils.isEmpty(endDateString)) {
                        Date endDate = CalendarUtils.parseFromUTC(endDateString);
                        if (CalendarUtils.getDefaultDateString(context, startDate)
                                .equals(CalendarUtils.getDefaultDateString(context, endDate))) {

                            output = getString(R.string.date_time_to_time_format,
                                    CalendarUtils.getDefaultDateString(context, startDate),
                                    CalendarUtils.getDefaultTimeString(context, startDate),
                                    CalendarUtils.getDefaultTimeString(context, endDate));
                        } else {
                            output = getString(R.string.date_time_to_date_time_format,
                                    CalendarUtils.getDefaultDateString(context, startDate),
                                    CalendarUtils.getDefaultTimeString(context, startDate),
                                    CalendarUtils.getDefaultDateString(context, endDate),
                                    CalendarUtils.getDefaultTimeString(context, endDate));
                        }
                    } else {
                        output = getString(R.string.date_time_format,
                                CalendarUtils.getDefaultDateString(context, startDate),
                                CalendarUtils.getDefaultTimeString(context, startDate));
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
