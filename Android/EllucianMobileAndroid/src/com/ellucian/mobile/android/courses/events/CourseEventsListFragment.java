package com.ellucian.mobile.android.courses.events;

import java.util.Date;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.provider.EllucianContract.CourseEvents;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;

public class CourseEventsListFragment extends EllucianDefaultListFragment {
	
	public CourseEventsListFragment() {
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
		
		return bundle;
	}
	
	@Override
	public Class<? extends  EllucianDefaultDetailFragment> getDetailFragmentClass() {
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
}
