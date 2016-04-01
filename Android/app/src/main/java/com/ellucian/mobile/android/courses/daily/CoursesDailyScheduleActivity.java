/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.daily;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.client.courses.daily.DailyScheduleResponse;
import com.ellucian.mobile.android.client.courses.daily.Day;
import com.ellucian.mobile.android.client.courses.daily.Meeting;
import com.ellucian.mobile.android.courses.CoursesTabListener;
import com.ellucian.mobile.android.courses.overview.CourseOverviewActivity;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;
import com.ellucian.mobile.android.view.BlockView;
import com.ellucian.mobile.android.view.BlocksLayout;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CoursesDailyScheduleActivity extends EllucianActivity {
	private static final String TAG = CoursesDailyScheduleActivity.class.getSimpleName();
	private static final String COURSES_TIME_PICKER = "courses_time_picker";

    private Activity activity = this;
	private BlocksLayout blocksView;
	private View rootView;
	private TextView tvDisplayDate;
	
	private DialogFragment pickerFragment;
	
	private final Calendar calendar = Calendar.getInstance();
	private SimpleDateFormat dateFormat = null;

    private SimpleDateFormat meetingPatternFormat;
    private DailyScheduleResponse cachedSchedule;
    private static final String CACHED_SCHEDULE = "cachedSchedule";
    // create a formatter that always returns a certain date-only format
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private SimpleDateFormat getMeetingPatternDateFormat() {
		if(meetingPatternFormat == null) {
			meetingPatternFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
			meetingPatternFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		}
		return meetingPatternFormat;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_courses_daily_schedule);
		
		rootView = findViewById(R.id.courses_daily_root_view);
		tvDisplayDate = (TextView) findViewById(R.id.courses_daily_date_display);
		blocksView = (BlocksLayout) findViewById(R.id.courses_daily_layout);
		
		if(savedInstanceState != null) {
			int year = savedInstanceState.getInt("year");
			int month = savedInstanceState.getInt("month");
			int day = savedInstanceState.getInt("day");
			if (year != 0) {
				setDate(year, month, day);
			}
            cachedSchedule = (DailyScheduleResponse) savedInstanceState.getSerializable(CACHED_SCHEDULE);
        }

        // Setup 2 tabs for Full and Detail view
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setSelectedTabIndicatorColor(Utils.getColorHelper(this, R.color.tab_indicator_color));

        TabLayout.Tab dailyView =  tabLayout.newTab().setText(R.string.courses_menu_daily_schedule);
        TabLayout.Tab fullView =  tabLayout.newTab().setText(R.string.courses_menu_full_schedule);
        tabLayout.addTab(dailyView, CoursesTabListener.DAILY_VIEW_TAB_INDEX, true);
        tabLayout.addTab(fullView, CoursesTabListener.FULL_VIEW_TAB_INDEX, false);
        tabLayout.setOnTabSelectedListener(new CoursesTabListener(this, getIntent()));

		checkButtonIcons();
		
		refreshSchedule();
		
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private void checkButtonIcons() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			// In RTL mode in order for the buttons to look right after they flip the icons need to be switched
			if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL) {
				ImageView forwardButton = (ImageView) findViewById(R.id.courses_daily_date_forward_button);
				forwardButton.setImageResource(R.drawable.ic_calendar_nav_left);
				ImageView backButton = (ImageView) findViewById(R.id.courses_daily_date_back_button);
				backButton.setImageResource(R.drawable.ic_calendar_nav_right);
			}
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Closes date picker if open when screen changes, keeps from throwing errors.
		if (pickerFragment != null) {
			pickerFragment.dismiss();
		}
		super.onSaveInstanceState(outState);
		outState.putInt("year", calendar.get(Calendar.YEAR));
		outState.putInt("month", calendar.get(Calendar.MONTH));
		outState.putInt("day", calendar.get(Calendar.DAY_OF_MONTH));
        outState.putSerializable(CACHED_SCHEDULE, cachedSchedule);
	}
	
	
	
	public void showDatePicker(View v) {
		pickerFragment = new DatePickerFragment();
		pickerFragment.show(getFragmentManager(),
                COURSES_TIME_PICKER);
	}

	@SuppressLint("ValidFragment")
	public class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {
		

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), 
										this, 
										calendar.get(Calendar.YEAR),
										calendar.get(Calendar.MONTH),
										calendar.get(Calendar.DAY_OF_MONTH));
		}
		
		public void onDateSet(DatePicker view, int year, int month, int day) {
			setDate(year, month, day);
            blocksView.removeAllBlocks();
			refreshSchedule();
		}
		
	}
	
	private void setDate(int year, int month, int day) {
		calendar.set(year, month, day);
	}
	
	public void dayAfter(View view) {
		calendar.add(Calendar.DAY_OF_YEAR, 1);
        blocksView.removeAllBlocks();
		refreshSchedule();
	}
	
	public void dayBefore(View view) {
		calendar.add(Calendar.DAY_OF_YEAR, -1);
        blocksView.removeAllBlocks();
		refreshSchedule();
	}
	
	private void refreshSchedule() {

        Log.d(TAG, "Get courses for " + sdf.format(calendar.getTime()));
        Day[] calDateSchedule = parseCachedSchedule();

        if (calDateSchedule == null) {
            String modifiedRequestUrl = createRequestUrl();

            // Start retrieval of daily schedule
            new RetrieveDailyScheduleTask().execute(modifiedRequestUrl);
        } else {
            displaySchedule(calDateSchedule);
        }
		
		displayDate();
	}
	
	private String createRequestUrl() {
		// ask for the date of interest AND the following week +/- 1 day, which allows us to catch meetings that might
		// be date shifted due to translation to Zulu time.
		Calendar cal = Calendar.getInstance();
		
		cal.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
		cal.add(Calendar.DAY_OF_YEAR, -1);
		String startDateString = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH);
		
		cal.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
		cal.add(Calendar.DAY_OF_YEAR, 7);
		String endDateString = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH);
		
		String modifiedRequestUrl = requestUrl + "/" + getEllucianApp().getAppUserId() + "?start=" + startDateString + "&end=" + endDateString;
		
		return modifiedRequestUrl;
	}
	
	private void displayDate() {
		Date date = calendar.getTime();
		tvDisplayDate.setText(formatDate(date));	
	}

    private boolean inDateRange(Date start, Date end, String targetDateStr) {
        // strip the localized date/time to just the date portion for comparison
        // with the targetDateStr
        String startStr = sdf.format(start);
        String endStr = sdf.format(end);

        Log.d(TAG, "    parsed date, start: " + startStr + ", end: " + endStr);

        return startStr.compareTo(targetDateStr) == 0 || endStr.compareTo(targetDateStr) == 0;
    }

	private void displaySchedule(Day[] calDateSchedule) {
		String targetDateStr = sdf.format(calendar.getTime());

		final DateFormat timeFormatter = android.text.format.DateFormat.getTimeFormat(this);

		// setup some "bounds-checking" dates for use inside the loops
		Calendar xCal = Calendar.getInstance();
		// base this on our calendar, which holds the date of interest
		xCal.setTime(calendar.getTime());
		
		// get a Date at the start of the day
		xCal.set(Calendar.HOUR_OF_DAY, 0);
		xCal.set(Calendar.MINUTE, 0);
		xCal.set(Calendar.SECOND,  0);
		xCal.set(Calendar.MILLISECOND, 0);
		Date startOfDay = xCal.getTime();
		
		// get a Date at the end of the day
		xCal.set(Calendar.HOUR_OF_DAY, 23);
		xCal.set(Calendar.MINUTE, 59);
		xCal.set(Calendar.SECOND,  59);
		xCal.set(Calendar.MILLISECOND, 0);
		Date endOfDay = xCal.getTime();
		
		
		// the request is asking for 1 day before and after the desired date
		// so look at each meeting after parsing to catch any meetings that 
		// might have been date-shifted when set to Zulu time
		for (final Day day : calDateSchedule){

			Log.d(TAG, "day.date: "+day.date);

            // Create a collection to sort today's classes by start time
            ArrayList<Meeting> meetings = new ArrayList<>();
            Collections.addAll(meetings, day.coursesMeetings);
            Collections.sort(meetings);
            Date previousCourseEndTime = null;

            for (int i=0; i<=meetings.size()-1;i++ ){
                final Meeting meeting = meetings.get(i);

				try {
					Date start = getMeetingPatternDateFormat().parse(meeting.start);
					Date end = getMeetingPatternDateFormat().parse(meeting.end);

                    Log.d(TAG, "  meeting start: " + meeting.start + ", end: " + meeting.end);

                    if (inDateRange(start, end, targetDateStr)){
						// some portion of the event overlaps the date of interest
						// BlockView doesn't automatically trim if a block started before, 
						// or ends after today, so we have to check those conditions ourselves.
						
						// if it started before today, but ends today, advance the start time to today
						if (start.compareTo(startOfDay) < 0) {
							start = startOfDay;
							Log.d(TAG, "  advanced start datetime to today");
						}

						// if it started today, but ends tomorrow, reduce the end time to today
						if (end.compareTo(endOfDay) > 0) {
							end = endOfDay;
							Log.d(TAG, "reduced end datetime to today");
						}
						
						String courseLabel = getString(R.string.default_course_section_format,
											meeting.courseName,
											meeting.courseSectionNumber);

                        String title = meeting.sectionTitle;

                        String location = "";
                        if (!TextUtils.isEmpty(meeting.building)) {
							if (!TextUtils.isEmpty(meeting.room)) {
								location = getString(R.string.default_building_and_room_format,
													meeting.building,
													meeting.room);
							} else {
								location = meeting.building;
							}
						}

                        String time = getString(R.string.time_to_time_format,
                                timeFormatter.format(start),
                                timeFormatter.format(end));

                        // Add logic to accommodate for 2 columns of course blocks.
                        // Column 0 is full width.
                        // Column 1 and 2 are for 1/2 width columns on the Left/Right respectively.
                        int column = 0;
                        if (previousCourseEndTime == null) {
                            previousCourseEndTime = end;
                        } else {
                            if (start.compareTo(previousCourseEndTime) <= 0) {
                                try {
                                    // Need to go back and change the column of the previous
                                    // Course Meeting since it can't be full width.
                                    BlockView previousCourseBV = (BlockView) blocksView.getChildAt(i);
                                    previousCourseBV.setColumn(1);
                                } catch (Exception e) {
                                    Log.e("CoursesDailyScheduleActivity", "Bad Block view accessed at i:" + i);
                                    Log.e("CoursesDailyScheduleActivity", e.getMessage());
                                }
                                column = 2;
                            }
                        }

                        BlockView blockView = new BlockView(this, courseLabel, title, location,
                                time, start.getTime(), end.getTime(), column);

						blockView.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_COURSES, GoogleAnalyticsConstants.ACTION_BUTTON_PRESS, "Click Course", null, moduleName);
								Intent intent = new Intent(CoursesDailyScheduleActivity.this, CourseOverviewActivity.class);
								intent.putExtras(CoursesDailyScheduleActivity.this.getIntent().getExtras());
								intent.putExtra(Extra.COURSES_COURSE_ID, meeting.sectionId);
								intent.putExtra(Extra.COURSES_TERM_ID, meeting.termId);
								intent.putExtra(Extra.COURSES_IS_INSTRUCTOR, meeting.isInstructor);
								intent.putExtra(Extra.COURSES_NAME, meeting.courseName);
								intent.putExtra(Extra.COURSES_SECTION_NUMBER, meeting.courseSectionNumber);
								startActivity(intent);
							}
						});

						blocksView.addBlock(blockView);
					}

				} catch (ParseException e) {
					Log.e("CoursesDailyScheduleActivity", e.getMessage());
				}
			}
		}
		rootView.invalidate();		
	}
	
	
	private class RetrieveDailyScheduleTask extends AsyncTask<String, Void, DailyScheduleResponse>{
		
		@Override
		protected DailyScheduleResponse doInBackground(String... params) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.showProgressIndicator(activity);
                }
            });
			String requestUrl = params[0];
			MobileClient client = new MobileClient(CoursesDailyScheduleActivity.this);
			client.setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			DailyScheduleResponse dailySchedule = client.getDailySchedule(requestUrl);
			return dailySchedule;
		}
		
		@Override
		protected void onPostExecute(DailyScheduleResponse dailySchedule) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.hideProgressIndicator(activity);
                }
            });

            if (dailySchedule != null && dailySchedule.coursesDays != null ) {
				Log.d("RetrieveDailyScheduleTask", "Daily schedule retrieved.");
				if (dailySchedule.coursesDays.length > 0) {

                    cachedSchedule = dailySchedule;
                    displaySchedule(parseCachedSchedule());

				} else {
					Log.d("RetrieveDailyScheduleTask", "No meetings scheduled for this day.");
				}
			} else {
				Log.d("RetrieveDailyScheduleTask", "Daily schedule information was not retrieved correctly");
			}
		}
	}

    // Parse the cached schedule data for just the user's selected calendar date (+/- 1 day)
    // and return that as an array.
    private Day[] parseCachedSchedule() {
        String targetDateStr = sdf.format(calendar.getTime());

        if (cachedSchedule != null) {

            // Start at 1. End at length-1 b/c we need 1 day before and after.
            for (int i = 1; i < cachedSchedule.coursesDays.length-1; i++) {
                if (cachedSchedule.coursesDays[i].date.equals(targetDateStr)){
                    Day[] calDateSchedule = new Day[3];
                    calDateSchedule[0] = cachedSchedule.coursesDays[i - 1];
                    calDateSchedule[1] = cachedSchedule.coursesDays[i];
                    calDateSchedule[2] = cachedSchedule.coursesDays[i + 1];
                    return calDateSchedule;
                }
            }
        }
        return null;
    }
	
	private String formatDate(Date date) {
		if(dateFormat == null) {
	
			DateFormat formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
			String localPattern  = ((SimpleDateFormat)formatter).toLocalizedPattern();
			dateFormat = new SimpleDateFormat("E " + localPattern, Locale.getDefault());
		}
		return dateFormat.format(date);
		
	}

	@Override
	protected void onStart() {
		super.onStart();
		sendView("Schedule (daily view)", moduleName);
	}


}
