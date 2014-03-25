// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.registration;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.client.courses.Instructor;
import com.ellucian.mobile.android.client.courses.MeetingPattern;
import com.ellucian.mobile.android.client.registration.Section;
import com.ellucian.mobile.android.util.CalendarUtils;

public class RegistrationDetailFragment extends EllucianDefaultDetailFragment {
	private static final String TAG = RegistrationDetailFragment.class.getSimpleName();
	
	private View rootView;
	
	private SimpleDateFormat dataFormat;
	private DateFormat timeFormatter;
	private Activity activity;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_registration_detail, container, false);	
		
		dataFormat = new SimpleDateFormat("HH:mm'Z'", Locale.US);
		dataFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		timeFormatter = android.text.format.DateFormat.getTimeFormat(activity);
		
		Bundle args = getArguments();
		
		Section section = args.getParcelable(RegistrationActivity.SECTION);
		
		TextView courseTitleView = (TextView)rootView.findViewById(R.id.course_title);
		if (section != null) {
			
			if (!TextUtils.isEmpty(section.courseName)) {
				String title = section.courseName;
				if (!TextUtils.isEmpty(section.courseSectionNumber)) {
					title += "-" + section.courseSectionNumber;
				}	
				courseTitleView.setText(title);
			}
			
			TextView sectionTitleView = (TextView)rootView.findViewById(R.id.section_title);
			if (!TextUtils.isEmpty(section.sectionTitle)) {	
				sectionTitleView.setText(section.sectionTitle);
			}
			
			TextView sectionIdView = (TextView)rootView.findViewById(R.id.section_id);
			if (!TextUtils.isEmpty(section.sectionId)) {		
				sectionIdView.setText(getString(R.string.registration_section_id) + ": " + section.sectionId);
			}
			
			TextView creditsView = (TextView)rootView.findViewById(R.id.credits);		
			creditsView.setText(getString(R.string.label_credits) + ": " + section.credits);
			
			
			LinearLayout meetingLayout = (LinearLayout) rootView.findViewById(R.id.meeting_layout);
			if (section.meetingPatterns != null && section.meetingPatterns.length > 0) {
				
				for (MeetingPattern pattern : section.meetingPatterns) {
					
					String meetingsString = "";

					if (pattern.daysOfWeek != null && pattern.daysOfWeek.length != 0) {
						
						for (int dayNumber : pattern.daysOfWeek) {

							if (!TextUtils.isEmpty(meetingsString)) {
								meetingsString += ", ";
							}
							// Adding 1 to number to make the Calendar constants 
							meetingsString += CalendarUtils.getDayShortName(dayNumber);
						}
						meetingsString += ": ";
					}
					
					Date startTimeDate = null;
					Date endTimeDate = null;
					String displayStartTime = "";
					String displayEndTime = "";
					
					try {
						startTimeDate = dataFormat.parse(pattern.startTime);
						endTimeDate = dataFormat.parse(pattern.endTime);
						
						if (startTimeDate != null) {
							displayStartTime = timeFormatter.format(startTimeDate);
						}	
						if (endTimeDate != null) {
							displayEndTime = timeFormatter.format(endTimeDate);
						}
					} catch (ParseException e) {
						Log.e(TAG, "ParseException: ", e);
					}
					
					
					if (!TextUtils.isEmpty(displayStartTime)) {
						meetingsString += displayStartTime;
						if (!TextUtils.isEmpty(displayEndTime)) {
							meetingsString += " - " + displayEndTime;
						}
					}
					
					if (!TextUtils.isEmpty(meetingsString)) {
						View rowLayout = activity.getLayoutInflater().inflate(R.layout.registration_meeting_row, meetingLayout, false);
						TextView meetingView = (TextView) rowLayout.findViewById(R.id.meeting);
						meetingView.setText(meetingsString);
						
						if (!TextUtils.isEmpty(pattern.instructionalMethodCode)) {
							TextView typeView = (TextView) rowLayout.findViewById(R.id.type);
							typeView.setText(pattern.instructionalMethodCode);
						} 
						
						meetingLayout.addView(rowLayout);				
					}	
				}
			} else {
				meetingLayout.setVisibility(View.GONE);
			}
			
			LinearLayout facultyLayout = (LinearLayout) rootView.findViewById(R.id.faculty_layout);
			if (section.instructors != null && section.instructors.length > 0) {
				
				for (Instructor instructor : section.instructors) {
					View rowLayout = activity.getLayoutInflater().inflate(R.layout.registration_faculty_row, facultyLayout, false);
					TextView instructorView = (TextView) rowLayout.findViewById(R.id.instructor_name);
					String shortName = instructor.lastName + ", " + instructor.firstName.charAt(0);
					instructorView.setText(shortName);

					facultyLayout.addView(rowLayout);
				}
				
			} else {
				facultyLayout.setVisibility(View.GONE);
			}
			
			
			LinearLayout descriptionLayout = (LinearLayout) rootView.findViewById(R.id.description_layout);
			if (!TextUtils.isEmpty(section.courseDescription)) {
				TextView descriptionView = (TextView)descriptionLayout.findViewById(R.id.description);
				descriptionView.setText(section.courseDescription);
			} else {
				descriptionLayout.setVisibility(View.GONE);
			}
		}
		
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		sendView("Registration Section Detail", getEllucianActivity().moduleName);
	}
	
}


