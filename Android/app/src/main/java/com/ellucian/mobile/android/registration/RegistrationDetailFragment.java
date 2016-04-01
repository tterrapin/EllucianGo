// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.registration;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.courses.Instructor;
import com.ellucian.mobile.android.client.courses.MeetingPattern;
import com.ellucian.mobile.android.client.registration.Section;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;
import com.ellucian.mobile.android.provider.EllucianContract.RegistrationLocations;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RegistrationDetailFragment extends EllucianDefaultDetailFragment {
	private static final String TAG = RegistrationDetailFragment.class.getSimpleName();
	public static final String REQUESTING_LIST_FRAGMENT = "requestingListFragment";
    public static final String REGISTRATION_MODULE_ID = "registrationModuleId";

    private Activity activity;
	
	private SimpleDateFormat defaultTimeParserFormat;
	private SimpleDateFormat altTimeParserFormat;
	private DateFormat timeFormatter;
    private String moduleId;
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		this.activity = getActivity();
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
        View rootView = inflater.inflate(R.layout.fragment_registration_detail, container, false);
		
		Bundle args = getArguments();
		
		Section section = args.getParcelable(RegistrationActivity.SECTION);

        if (args.containsKey(REGISTRATION_MODULE_ID)) {
            moduleId = args.getString(REGISTRATION_MODULE_ID);
        }
		
		LinearLayout headerLayout = (LinearLayout) rootView.findViewById(R.id.header_layout);
		headerLayout.setBackgroundColor(Utils.getAccentColor(activity));
		
		int subheaderTextColor = Utils.getSubheaderTextColor(getActivity());
		
		TextView courseTitleView = (TextView) rootView.findViewById(R.id.course_title);
		if (section != null) {
			
			if (!TextUtils.isEmpty(section.courseName)) {
				String title = section.courseName;
				if (!TextUtils.isEmpty(section.courseSectionNumber)) {
					title = getString(R.string.default_course_section_format,
									section.courseName,
									section.courseSectionNumber);
				}
				courseTitleView.setTextColor(subheaderTextColor);
				courseTitleView.setText(title);				
			}
			
			TextView sectionTitleView = (TextView) rootView.findViewById(R.id.section_title);
			if (!TextUtils.isEmpty(section.sectionTitle)) {
				sectionTitleView.setTextColor(subheaderTextColor);
				sectionTitleView.setText(section.sectionTitle);
			}
			
			TextView datesView = (TextView) rootView.findViewById(R.id.dates);
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
		    	datesView.setText(getString(R.string.date_to_date_format, 
		    							startDate, 
		    							endDate));
			} else {
				datesView.setVisibility(View.GONE);
			}
			
			TextView sectionIdView = (TextView) rootView.findViewById(R.id.section_id);
			if (!TextUtils.isEmpty(section.sectionId)) {		
				sectionIdView.setText(getString(R.string.label_string_content_format,
										getString(R.string.registration_section_id),
										section.sectionId));
			}
			
			TextView creditsView = (TextView) rootView.findViewById(R.id.credits);
			String creditsText;
			if (section.selectedCredits != -1) {
				creditsText = getString(R.string.label_float_content_format, 
									getString(R.string.label_credits),
									section.selectedCredits);

			} else if (section.credits != 0) {
				creditsText = getString(R.string.label_float_content_format, 
								getString(R.string.label_credits),
								section.credits);				
			} else if (!TextUtils.isEmpty(section.variableCreditOperator) && section.variableCreditOperator.equals(Section.VARIABLE_OPERATOR_OR)
					|| section.minimumCredits != 0) {

				if (section.maximumCredits != 0) {
					creditsText = getString(R.string.registration_label_credits_min_max_format, 
									getString(R.string.label_credits),
									section.minimumCredits,
									section.maximumCredits);
				} else {
					creditsText = getString(R.string.label_float_content_format, 
									getString(R.string.label_credits),
									section.minimumCredits);
				}

			} else if (section.ceus != 0){
				creditsText = getString(R.string.registration_label_credits_with_ceus_format, 
								getString(R.string.label_credits),
								section.ceus,
								getString(R.string.registration_ceus));
			} else {
				// Only want to display zero in last possible case to avoid not showing the correct alternative
				creditsText = getString(R.string.label_float_content_format, 
								getString(R.string.label_credits),
								0f);
			}
			creditsView.setText(creditsText);
			
			TextView gradingTypeView = (TextView) rootView.findViewById(R.id.grading_type);
			
			if (!TextUtils.isEmpty(section.gradingType) && section.gradingType.equals(Section.GRADING_TYPE_AUDIT)) {
				gradingTypeView.setText(getString(R.string.label_string_content_format, 
											getString(R.string.registration_grading),
											getString(R.string.registration_audit)));				
			} else if (!TextUtils.isEmpty(section.gradingType) && section.gradingType.equals(Section.GRADING_TYPE_PASS_FAIL)) {
				gradingTypeView.setText(getString(R.string.label_string_content_format, 
											getString(R.string.registration_grading),
											getString(R.string.registration_pass_fail)));
			} else {
				gradingTypeView.setText(getString(R.string.label_string_content_format, 
						getString(R.string.registration_grading),
						getString(R.string.registration_graded)));
			}
			
			// Only show academic levels info on the details selected from the search results list
			TextView academicLevelsView = (TextView) rootView.findViewById(R.id.academic_levels);
			if (args.containsKey(REQUESTING_LIST_FRAGMENT) && args.getString(REQUESTING_LIST_FRAGMENT)
					.equals(RegistrationSearchResultsListFragment.class.getSimpleName())) {
				
				if (section.academicLevels != null && section.academicLevels.length > 0) {
					String academicLevelsText = TextUtils.join(",", section.academicLevels);
					
					academicLevelsView.setText(getString(R.string.label_string_content_format,
											getString(R.string.registration_academic_levels),
											academicLevelsText));
				} else {
					academicLevelsView.setVisibility(View.GONE);
				}
				
			} else {
				academicLevelsView.setVisibility(View.GONE);
			}

            // Available/Capacity images & text - only shows on Search Results Tab
            if (args.containsKey(REQUESTING_LIST_FRAGMENT) && args.getString(REQUESTING_LIST_FRAGMENT)
                    .equals(RegistrationSearchResultsListFragment.class.getSimpleName())) {
                LinearLayout seatsDetailsView = (LinearLayout) rootView.findViewById(R.id.seats_details);
                if (section.available != null && section.capacity != null) {
                    String capacityText = getString(R.string.remaining_capacity,
                            section.available,
                            section.capacity);
                    Drawable meterImage = null;
                    meterImage = getMeterImage(section, getContext());

                    seatsDetailsView.setVisibility(View.VISIBLE);
                    View seatsAvailView = rootView.findViewById(R.id.seats_available_box);
                    seatsAvailView.setVisibility(View.VISIBLE);
                    if (meterImage != null) {
                        Utils.enableMirroredDrawable(meterImage);
                        ((ImageView) seatsAvailView.findViewById(R.id.seats_available_image)).setImageDrawable(meterImage);
                    }
                    ((TextView) seatsAvailView.findViewById(R.id.seats_available_text)).setText(capacityText);
                }
            }
			
			LinearLayout meetingLayout = (LinearLayout) rootView.findViewById(R.id.meeting_layout);
			if (section.meetingPatterns != null && section.meetingPatterns.length > 0) {
				
				int meetingCount = 0;
				for (MeetingPattern pattern : section.meetingPatterns) {
					
					String meetingsDaysString = "";

					if (pattern.daysOfWeek != null && pattern.daysOfWeek.length != 0) {
						
						for (int dayNumber : pattern.daysOfWeek) {

							if (!TextUtils.isEmpty(meetingsDaysString)) {
								meetingsDaysString += ", ";
							}
							// Adding 1 to number to make the Calendar constants 
							meetingsDaysString += CalendarUtils.getDayShortName(dayNumber);
						}
						meetingsDaysString += ": ";
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
					
					String meetingTimesString = "";
					if (!TextUtils.isEmpty(displayStartTime)) {
						
						if (!TextUtils.isEmpty(displayEndTime)) {
							meetingTimesString = getString(R.string.time_to_time_format,
														displayStartTime,
														displayEndTime);
						} else {
							meetingTimesString = displayStartTime;
						}
						
					}
					
					if (!TextUtils.isEmpty(meetingsDaysString)) {
						View rowLayout = activity.getLayoutInflater().inflate(R.layout.registration_meeting_row, meetingLayout, false);
						TextView daysView = (TextView) rowLayout.findViewById(R.id.meeting_days);
						daysView.setText(meetingsDaysString);
						
						if (!TextUtils.isEmpty(meetingTimesString)) {
							TextView timesView = (TextView) rowLayout.findViewById(R.id.meeting_times);
							timesView.setText(meetingTimesString);
						}
						
						if (!TextUtils.isEmpty(pattern.instructionalMethodCode)) {
							TextView typeView = (TextView) rowLayout.findViewById(R.id.type);
							typeView.setText(pattern.instructionalMethodCode);
						} 
						
						TextView buildingRoomView = (TextView) rowLayout.findViewById(R.id.building_room);
						String buildingRoomString = "";
						if (!TextUtils.isEmpty(pattern.building)) {
							if (!TextUtils.isEmpty(pattern.room)) {								
								buildingRoomString = getString(R.string.default_building_and_room_format,
														pattern.building,
														pattern.room);
							} else {
								buildingRoomString = pattern.building;
							}
						} 
												
						if (!TextUtils.isEmpty(buildingRoomString)) {
							buildingRoomView.setText(buildingRoomString);
							// Show underline of text
							//locationView.setPaintFlags(locationView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
						} else {
							buildingRoomView.setVisibility(View.GONE);
						}

                        TextView campusLocationView = (TextView) rowLayout.findViewById(R.id.campus_location);
							if (!TextUtils.isEmpty(pattern.campusId)) {
								String selection = Modules.MODULES_ID + " = ? AND " + RegistrationLocations.REGISTRATION_LOCATIONS_CODE + " = ?";
								Cursor cursor = activity.getContentResolver().query(
										RegistrationLocations.CONTENT_URI, 
										new String[] { RegistrationLocations.REGISTRATION_LOCATIONS_NAME }, 
										selection, 
										new String[] { moduleId, pattern.campusId },
										null);
								
								if (cursor.moveToFirst() && 
										!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(RegistrationLocations.REGISTRATION_LOCATIONS_NAME)))) {
									campusLocationView.setText(cursor.getString(cursor.getColumnIndex(RegistrationLocations.REGISTRATION_LOCATIONS_NAME)));
								} else {
                                    campusLocationView.setText(pattern.campusId);
								}
							} else {
								campusLocationView.setVisibility(View.GONE);
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
				args.getString(REQUESTING_LIST_FRAGMENT).equals(RegistrationCartListFragment.class.getSimpleName())) {
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

    void triggerRemoveItemFromCart() {
    	Activity activity = getActivity();
    	if (activity instanceof RegistrationActivity) {
    		((RegistrationActivity)activity).removeItemFromCart();
    	} else if (getActivity() instanceof RegistrationDetailActivity) {
    		((RegistrationDetailActivity)activity).removeItemFromCart();
    	}

    }

    public static Drawable getMeterImage(Section section, Context context) {
        Drawable meterImage = null;
        if (section.capacity >= 0) {
            float available = (float) section.available / (float) section.capacity;
            float delta = 1.0f / 6.0f;
            if (section.available <= 0) {
                meterImage = Utils.getDrawableHelper(context, R.drawable.seats_full_6);
            } else if (available <= delta) {
                meterImage = Utils.getDrawableHelper(context, R.drawable.seats_full_5);
            } else if (available <= delta * 2) {
                meterImage = Utils.getDrawableHelper(context, R.drawable.seats_full_4);
            } else if (available <= delta * 3) {
                meterImage = Utils.getDrawableHelper(context, R.drawable.seats_full_3);
            } else if (available <= delta * 4) {
                meterImage = Utils.getDrawableHelper(context, R.drawable.seats_full_2);
            } else if (available <= delta * 5) {
                meterImage = Utils.getDrawableHelper(context, R.drawable.seats_full_1);
            } else {
                meterImage = Utils.getDrawableHelper(context, R.drawable.seats_full_0);
            }
        }
        return meterImage;
    }

}


