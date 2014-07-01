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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.courses.Instructor;
import com.ellucian.mobile.android.client.courses.MeetingPattern;
import com.ellucian.mobile.android.client.registration.Section;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Utils;

public class RegistrationDetailFragment extends EllucianDefaultDetailFragment {
	private static final String TAG = RegistrationDetailFragment.class.getSimpleName();
	public static final String REQUESTING_LIST_FRAGMENT = "requestingListFragment";
	
	private View rootView;
	private Activity activity;
	
	protected SimpleDateFormat defaultTimeParserFormat;
	protected SimpleDateFormat altTimeParserFormat;
	protected DateFormat timeFormatter;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		defaultTimeParserFormat = new SimpleDateFormat("HH:mm'Z'", Locale.US);
		defaultTimeParserFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		altTimeParserFormat = new SimpleDateFormat("HH:mm", Locale.US);
		timeFormatter = android.text.format.DateFormat.getTimeFormat(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_registration_detail, container, false);	
		
		Bundle args = getArguments();
		
		Section section = args.getParcelable(RegistrationActivity.SECTION);
		
		LinearLayout headerLayout = (LinearLayout) rootView.findViewById(R.id.header_layout);
		headerLayout.setBackgroundColor(Utils.getAccentColor(activity));
		
		int subheaderTextColor = Utils.getSubheaderTextColor(getActivity());
		
		TextView courseTitleView = (TextView)rootView.findViewById(R.id.course_title);
		if (section != null) {
			
			if (!TextUtils.isEmpty(section.courseName)) {
				String title = section.courseName;
				if (!TextUtils.isEmpty(section.courseSectionNumber)) {
					title += "-" + section.courseSectionNumber;
				}
				courseTitleView.setTextColor(subheaderTextColor);
				courseTitleView.setText(title);				
			}
			
			TextView sectionTitleView = (TextView)rootView.findViewById(R.id.section_title);
			if (!TextUtils.isEmpty(section.sectionTitle)) {
				sectionTitleView.setTextColor(subheaderTextColor);
				sectionTitleView.setText(section.sectionTitle);
			}
			
			TextView datesView = (TextView)rootView.findViewById(R.id.dates);
			if (!TextUtils.isEmpty(section.firstMeetingDate) && !TextUtils.isEmpty(section.lastMeetingDate)) {
				String startDate = "";
				String endDate = "";
				DateFormat fromDatabase = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
		    	try {
					Date start = fromDatabase.parse(section.firstMeetingDate);
					Date end = fromDatabase.parse(section.lastMeetingDate);

					
			    	DateFormat localFormat = android.text.format.DateFormat.getDateFormat(getActivity());
			    	startDate = localFormat.format(start);
			    	endDate = localFormat.format(end);
				} catch (ParseException e) {
					Log.e(TAG, e.getMessage());
				}
		    	
		    	datesView.setTextColor(subheaderTextColor);
		    	datesView.setText(startDate + " - " + endDate);
			} else {
				datesView.setVisibility(View.GONE);
			}
			
			TextView sectionIdView = (TextView)rootView.findViewById(R.id.section_id);
			if (!TextUtils.isEmpty(section.sectionId)) {		
				sectionIdView.setText(getString(R.string.registration_section_id) + ": " + section.sectionId);
			}
			
			TextView creditsView = (TextView)rootView.findViewById(R.id.credits);
			if (section.selectedCredits != -1) {
				creditsView.setText(getString(R.string.label_credits) + ": " + section.selectedCredits);
			} else if (section.credits != 0) {
				creditsView.setText(getString(R.string.label_credits) + ": " + section.credits);				
			} else if (!TextUtils.isEmpty(section.variableCreditOperator) && section.variableCreditOperator.equals(Section.VARIABLE_OPERATOR_OR)
					|| section.minimumCredits != 0) {
				String creditsText = getString(R.string.label_credits) + ": " + section.minimumCredits;
				if (section.maximumCredits != 0) {
					creditsText += "-" + (float)section.maximumCredits;
				}
				creditsText += " " + getString(R.string.registration_credits);
				creditsView.setText(creditsText);
			} else if (section.ceus != 0){
				creditsView.setText(getString(R.string.label_credits) + ": "  + section.ceus + " " 
						+ getString(R.string.registration_ceus));
			} else {
				// Only want to display zero in last possible case to avoid not showing the correct alternative
				creditsView.setText(getString(R.string.label_credits) + ": 0");
			}
			
			TextView gradingTypeView = (TextView) rootView.findViewById(R.id.grading_type);
			
			if (!TextUtils.isEmpty(section.gradingType) && section.gradingType.equals(Section.GRADING_TYPE_GRADED)) {				
				gradingTypeView.setText(getString(R.string.registration_grading) + ": " + getString(R.string.registration_graded));
			
			} else if (!TextUtils.isEmpty(section.gradingType) && section.gradingType.equals(Section.GRADING_TYPE_AUDIT)) {
				gradingTypeView.setText(getString(R.string.registration_grading) + ": " + getString(R.string.registration_audit));
				
			} else if (!TextUtils.isEmpty(section.gradingType) && section.gradingType.equals(Section.GRADING_TYPE_PASS_FAIL)) {
				gradingTypeView.setText(getString(R.string.registration_grading) + ": " + getString(R.string.registration_pass_fail));
			} else {
				gradingTypeView.setVisibility(View.GONE);
			}
						
			LinearLayout meetingLayout = (LinearLayout) rootView.findViewById(R.id.meeting_layout);
			if (section.meetingPatterns != null && section.meetingPatterns.length > 0) {
				
				int meetingCount = 0;
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
						if (!TextUtils.isEmpty(pattern.sisStartTimeWTz) && pattern.sisStartTimeWTz.contains(" ")) {
							String[] splitTimeAndZone = pattern.sisStartTimeWTz.split(" ");
							String time = splitTimeAndZone[0];
							String timeZone = splitTimeAndZone[1];
							altTimeParserFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
							startTimeDate = altTimeParserFormat.parse(time);							
						} else {
							startTimeDate = defaultTimeParserFormat.parse(pattern.startTime);
						}
						
						if (!TextUtils.isEmpty(pattern.sisEndTimeWTz) && pattern.sisEndTimeWTz.contains(" ")) {
							String[] splitTimeAndZone = pattern.sisEndTimeWTz.split(" ");
							String time = splitTimeAndZone[0];
							String timeZone = splitTimeAndZone[1];
							altTimeParserFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
							endTimeDate = altTimeParserFormat.parse(time);
						} else {
							endTimeDate = defaultTimeParserFormat.parse(pattern.endTime);
						}
						
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
						
						TextView locationView = (TextView) rowLayout.findViewById(R.id.building_room);
						String locationString = "";
						if (!TextUtils.isEmpty(pattern.building)) {
							locationString += pattern.building;
						} 
						
						if (!TextUtils.isEmpty(pattern.room)) {
							if (!TextUtils.isEmpty(locationString)) {
								locationString += ", ";
							}
							locationString += pattern.room;
						}
						if (!TextUtils.isEmpty(locationString)) {
							locationView.setText(locationString);
							// Show underline of text
							//locationView.setPaintFlags(locationView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
						} else {
							locationView.setVisibility(View.GONE);
						}
						
						if (meetingCount > 0) {
							View separator = activity.getLayoutInflater().inflate(R.layout.separator, meetingLayout, false);
							meetingLayout.addView(separator);
						}
						meetingLayout.addView(rowLayout);
						meetingCount++;
					}	
				}
			} else {
				meetingLayout.setVisibility(View.GONE);
			}
			
			LinearLayout facultyLayout = (LinearLayout) rootView.findViewById(R.id.faculty_layout);
			if (section.instructors != null && section.instructors.length > 0) {
				
				int instructorCount = 0;
				for (Instructor instructor : section.instructors) {
					View rowLayout = activity.getLayoutInflater().inflate(R.layout.registration_faculty_row, facultyLayout, false);
					TextView instructorView = (TextView) rowLayout.findViewById(R.id.instructor_name);
					String displayName = "";
					if (!TextUtils.isEmpty(instructor.formattedName)) {
						displayName = instructor.formattedName;
					} else {
						displayName = instructor.firstName + instructor.lastName;
					}
					
					instructorView.setText(displayName);
					
					if (instructorCount > 0) {
						View separator = activity.getLayoutInflater().inflate(R.layout.separator, meetingLayout, false);
						facultyLayout.addView(separator);
					}

					facultyLayout.addView(rowLayout);
					instructorCount++;
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
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Bundle args = getArguments();
		if (args.containsKey(REQUESTING_LIST_FRAGMENT) && 
				args.getString(REQUESTING_LIST_FRAGMENT).equals("RegistrationCartListFragment")) {
			inflater.inflate(R.menu.registration_detail, menu);
		}        
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.registration_remove:
			sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION,
					GoogleAnalyticsConstants.ACTION_BUTTON_PRESS,
					"Removing from cart", null, getEllucianActivity().moduleName);
			RemoveFromCartConfirmDialogFragment removeFromCartConfirmDialogFragment = new RemoveFromCartConfirmDialogFragment();
			removeFromCartConfirmDialogFragment.detailFragment = this;
			removeFromCartConfirmDialogFragment.show(getFragmentManager(), "RemoveFromCartConfirmDialogFragment");
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }

    protected void triggerRemoveItemFromCart() {
    	Activity activity = getActivity();
    	if (activity instanceof RegistrationActivity) {
    		((RegistrationActivity)activity).removeItemFromCart();
    	} else if (getActivity() instanceof RegistrationDetailActivity) {
    		((RegistrationDetailActivity)activity).removeItemFromCart();
    	}

    }
	
}


