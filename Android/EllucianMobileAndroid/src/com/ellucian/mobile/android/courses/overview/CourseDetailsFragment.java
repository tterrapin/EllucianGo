package com.ellucian.mobile.android.courses.overview;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.provider.EllucianContract.CourseCourses;
import com.ellucian.mobile.android.provider.EllucianContract.CourseInstructors;
import com.ellucian.mobile.android.provider.EllucianContract.CourseMeetings;
import com.ellucian.mobile.android.provider.EllucianContract.CoursePatterns;
import com.ellucian.mobile.android.provider.EllucianContract.MapsBuildings;
import com.ellucian.mobile.android.provider.EllucianContract.MapsCampuses;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

public class CourseDetailsFragment extends EllucianFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	
	private Activity activity;
	private String courseId;
	private View rootView;
	private LinearLayout coloredLayout;
	private TextView titleTextView;
	private TextView datesTextView;
	private TextView descriptionTextView;
	private int subheaderTextColor;
	
	private final int PATTERNS_LOADER = 1;
	private final int INSTRUCTORS_LOADER = 2;
	private final int COURSE_LOADER = 3;
	private final int MEETINGS_LOADER = 4;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = getActivity();
	
		subheaderTextColor = Utils.getSubheaderTextColor(activity);

		courseId = getActivity().getIntent().getStringExtra(Extra.COURSES_COURSE_ID);

		LoaderManager manager = getLoaderManager();
		manager.initLoader(PATTERNS_LOADER, null, this);
		manager.initLoader(INSTRUCTORS_LOADER, null, this);
		manager.initLoader(COURSE_LOADER, null, this);
		manager.initLoader(MEETINGS_LOADER, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_course_details,
				container, false);
		coloredLayout = (LinearLayout) rootView.findViewById(R.id.course_details_colored_layout);
		titleTextView = (TextView) rootView.findViewById(R.id.course_details_title);
		datesTextView = (TextView) rootView.findViewById(R.id.course_details_dates);
		descriptionTextView = (TextView) rootView.findViewById(R.id.course_details_course_description);
		TextView facultyTitleView = (TextView) rootView.findViewById(R.id.course_details_faculty_title);
		
		titleTextView.setTextColor(subheaderTextColor);
		datesTextView.setTextColor(subheaderTextColor);
		facultyTitleView.setTextColor(subheaderTextColor);
		coloredLayout.setBackgroundColor(Utils.getAccentColor(activity));
		
		return rootView;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle data) {
		CursorLoader loader = null;
		if (id == PATTERNS_LOADER) {
			loader = new CursorLoader(getActivity(), CoursePatterns.CONTENT_URI, null,
					CourseCourses.COURSE_ID + " = ?", new String[] { courseId }, null);
		} else if (id == INSTRUCTORS_LOADER) {
			loader = new CursorLoader(getActivity(), CourseInstructors.CONTENT_URI, null,
					CourseCourses.COURSE_ID + " = ?", new String[] { courseId }, null);
		} else if (id == COURSE_LOADER) {
			Uri courseUrl = CourseCourses.buildCourseUri(courseId);
			loader = new CursorLoader(getActivity(), courseUrl, null, null,
					null, null);
		} else if (id == MEETINGS_LOADER) {
			loader = new CursorLoader(getActivity(), CourseMeetings.CONTENT_URI, null,
					CourseCourses.COURSE_ID + " = ?", new String[] { courseId }, null);
		}
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (getActivity() == null) {
			return;
		}

		if (loader.getId() == PATTERNS_LOADER) {
			onPatternsQueryComplete(cursor);
		} else if (loader.getId() == INSTRUCTORS_LOADER) {
			onInstructorsQueryComplete(cursor);
		} else if (loader.getId() == COURSE_LOADER) {
			onCourseQueryComplete(cursor);
		} else if (loader.getId() == MEETINGS_LOADER) {
			onMeetingsQueryComplete(cursor);
		} else {
			cursor.close();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}
	
	private void onPatternsQueryComplete(Cursor cursor) {
		final LinearLayout meetingLayout = (LinearLayout) rootView.findViewById(R.id.course_details_meetings_layout);
		meetingLayout.removeAllViews();
		final LayoutInflater inflater = getActivity().getLayoutInflater();
		
		final SimpleDateFormat defaultTimeParserFormat = new SimpleDateFormat("HH:mm'Z'", Locale.US); //for dates	
		defaultTimeParserFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		final SimpleDateFormat altTimeParserFormat = new SimpleDateFormat("HH:mm", Locale.US); //for dates	
		final DateFormat timeFormatter = android.text.format.DateFormat.getTimeFormat(getActivity());
		
		if (cursor.moveToFirst()) {
			do {
				final String daysOfWeek = cursor.getString(cursor
						.getColumnIndex(CoursePatterns.PATTERN_DAYS));
				final String startTime = cursor.getString(cursor
						.getColumnIndex(CoursePatterns.PATTERN_START_TIME));
				final String endTime = cursor.getString(cursor
						.getColumnIndex(CoursePatterns.PATTERN_END_TIME));
				final String location = cursor.getString(cursor
						.getColumnIndex(CoursePatterns.PATTERN_LOCATION));
				final String room = cursor.getString(cursor
						.getColumnIndex(CoursePatterns.PATTERN_ROOM));
				final String buildingId = cursor.getString(cursor
						.getColumnIndex(MapsBuildings.BUILDING_BUILDING_ID));
				@SuppressWarnings("unused")
				final String campusId = cursor.getString(cursor
						.getColumnIndex(MapsCampuses.CAMPUS_ID));
				
				// Converting dates to correct format for display
				Date startTimeDate = null;
				Date endTimeDate = null;
				String displayStartTime = "";
				String displayEndTime = "";

				try {
					if (!TextUtils.isEmpty(startTime) && startTime.contains(" ")) {
						String[] splitTimeAndZone = startTime.split(" ");
						String time = splitTimeAndZone[0];
						String timeZone = splitTimeAndZone[1];
						altTimeParserFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
						startTimeDate = altTimeParserFormat.parse(time);
					} else {
						startTimeDate = defaultTimeParserFormat.parse(startTime);
					}
					
					if (!TextUtils.isEmpty(endTime) && endTime.contains(" ")) {
						String[] splitTimeAndZone = endTime.split(" ");
						String time = splitTimeAndZone[0];
						String timeZone = splitTimeAndZone[1];
						altTimeParserFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
						endTimeDate = altTimeParserFormat.parse(time);
					} else {
						endTimeDate = defaultTimeParserFormat.parse(endTime);
					}
										
					
					if (startTimeDate != null) {
						displayStartTime = timeFormatter.format(startTimeDate);
					}	
					if (endTimeDate != null) {
						displayEndTime = timeFormatter.format(endTimeDate);
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				// Getting correct days to show
				String[] days = daysOfWeek.split(",");
				String displayDaysOfWeek = "";
				for (String day : days) {
					int dayNumber = Integer.parseInt(day);
					if (!TextUtils.isEmpty(displayDaysOfWeek)) {
						displayDaysOfWeek += ", ";
					}
					// Adding 1 to number to make the Calendar constants 
					displayDaysOfWeek += CalendarUtils.getDayShortName(dayNumber);
				}
				displayDaysOfWeek += ": ";
				

				final LinearLayout meetingRow = (LinearLayout)inflater.inflate(
						R.layout.course_details_meeting_row, meetingLayout,
						false);
				final TextView daysView = (TextView) meetingRow
						.findViewById(R.id.course_details_meeting_row_days);
				daysView.setTextColor(subheaderTextColor);
				daysView.setText(displayDaysOfWeek);
				final TextView timeView = (TextView) meetingRow
						.findViewById(R.id.course_details_meeting_row_times);
				if (!TextUtils.isEmpty(displayStartTime)) {
					timeView.setTextColor(subheaderTextColor);
					timeView.setText(displayStartTime + " - " + displayEndTime);
				} else {
					timeView.setVisibility(View.GONE);
				}
				final TextView locationView = (TextView) meetingRow
						.findViewById(R.id.course_details_meeting_row_location);
				locationView.setTextColor(subheaderTextColor);
				String locationString = "";
				if (!TextUtils.isEmpty(location)) {
					locationString += location;
				} 
				
				if (!TextUtils.isEmpty(room)) {
					if (!TextUtils.isEmpty(locationString)) {
						locationString += ", ";
					}
					locationString += room;
				}
				if (!TextUtils.isEmpty(locationString)) {
					locationView.setText(locationString);
					// Show underline of text
					locationView.setPaintFlags(locationView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
				} else {
					locationView.setVisibility(View.GONE);
				}
				
				meetingRow.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_LIST_SELECT, "Map Detail", null, getEllucianActivity().moduleName);
						((CourseOverviewActivity) activity).openBuildingDetail(buildingId, location);
					}
				});
				
				/*
				//TODO l10n days of the week and the labels
				if(daysOfWeek.contains("Sunday")) {
					((TextView) meetingRow
					.findViewById(R.id.courses_detail_meeting_pattern_row_day0)).setBackgroundColor(getResources().getColor(R.color.header_color));//TODO
				} if(daysOfWeek.contains("Monday")) {
					((TextView) meetingRow
					.findViewById(R.id.courses_detail_meeting_pattern_row_day1)).setBackgroundColor(getResources().getColor(R.color.header_color));//TODO
				} if(daysOfWeek.contains("Tuesday")) {
					((TextView) meetingRow
					.findViewById(R.id.courses_detail_meeting_pattern_row_day2)).setBackgroundColor(getResources().getColor(R.color.header_color));//TODO)
				} if(daysOfWeek.contains("Wednesday")) {
					((TextView) meetingRow
					.findViewById(R.id.courses_detail_meeting_pattern_row_day3)).setBackgroundColor(getResources().getColor(R.color.header_color));//TODO
				} if(daysOfWeek.contains("Thursday")) {
					((TextView) meetingRow
					.findViewById(R.id.courses_detail_meeting_pattern_row_day4)).setBackgroundColor(getResources().getColor(R.color.header_color));//TODO
				} if(daysOfWeek.contains("Friday")) {
					((TextView) meetingRow
					.findViewById(R.id.courses_detail_meeting_pattern_row_day5)).setBackgroundColor(getResources().getColor(R.color.header_color));//TODO
				} if(daysOfWeek.contains("Saturday")) {
					((TextView) meetingRow
					.findViewById(R.id.courses_detail_meeting_pattern_row_day6)).setBackgroundColor(getResources().getColor(R.color.header_color));//TODO
				}
				*/

				meetingLayout.addView(meetingRow);
			} while (cursor.moveToNext());
		}
	}

	private void onInstructorsQueryComplete(Cursor cursor) {
		
		final LinearLayout facultyLayout = (LinearLayout) rootView
				.findViewById(R.id.course_details_faculty_layout);
		facultyLayout.removeAllViews();
		final LayoutInflater inflater = getActivity().getLayoutInflater();
		
		if (cursor.moveToFirst()) {
			rootView.findViewById(R.id.course_details_faculty_title).setVisibility(View.VISIBLE);
			do {
				final String instructorName = cursor.getString(cursor
						.getColumnIndex(CourseInstructors.INSTRUCTOR_FORMATTED_NAME));

				final View instructorView = inflater.inflate(
						R.layout.course_details_faculty_row, facultyLayout,
						false);
				final TextView instructorNameView = (TextView) instructorView
						.findViewById(R.id.course_details_instructor_name);
				instructorNameView.setText(instructorName);
				instructorNameView.setTextColor(subheaderTextColor);
				// Show underline of text
				instructorNameView.setPaintFlags(instructorNameView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

				facultyLayout.addView(instructorView);
			} while (cursor.moveToNext());
		} else {
			rootView.findViewById(R.id.course_details_faculty_title).setVisibility(View.GONE);
		}
		
	}
	
    private void onCourseQueryComplete(Cursor cursor) {
        if (!cursor.moveToFirst()) {
            return;
        }
        
        titleTextView.setText( cursor.getString(cursor.getColumnIndex(CourseCourses.COURSE_TITLE)));
        descriptionTextView.setText( cursor.getString(cursor.getColumnIndex(CourseCourses.COURSE_DESCRIPTION)));
        
        boolean isILPConfigured = getActivity().getIntent().getExtras().containsKey(Extra.COURSES_ILP_URL);
        String learningProvider = cursor.getString(cursor.getColumnIndex(CourseCourses.COURSE_LEARNING_PROVIDER));
        if(isILPConfigured && learningProvider != null && !learningProvider.equalsIgnoreCase("SHAREPOINT")) {
        	((CourseOverviewActivity)activity).addMoreTab();
        }
    }
    
    private void onMeetingsQueryComplete(Cursor cursor) {
    	if (!cursor.moveToFirst()) {
            return;
        }
    	String startDate = cursor.getString(cursor.getColumnIndex(CourseMeetings.MEETING_START));
    	String endDate = cursor.getString(cursor.getColumnIndex(CourseMeetings.MEETING_END));
    	DateFormat fromDatabase = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
    	try {
			Date start = fromDatabase.parse(startDate);
			Date end = fromDatabase.parse(endDate);
	    	DateFormat localFormat = android.text.format.DateFormat.getDateFormat(getActivity());
	    	startDate = localFormat.format(start);
	    	endDate = localFormat.format(end);
		} catch (ParseException e) {
			Log.e("CourseDetailsFragment", e.getMessage());
		}
    	
    	datesTextView.setText(startDate + " - " + endDate);
    	
    }

	@Override
	public void onStart() {
		super.onStart();
		sendView("Course Overview", getEllucianActivity().moduleName);
	}
    
    
}
