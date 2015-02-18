package com.ellucian.mobile.android.courses.daily;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.ellucian.mobile.android.courses.full.CoursesFullScheduleActivity;
import com.ellucian.mobile.android.courses.overview.CourseOverviewActivity;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.view.BlockView;
import com.ellucian.mobile.android.view.BlocksLayout;

public class CoursesDailyScheduleActivity extends EllucianActivity {
	private static final String TAG = CoursesDailyScheduleActivity.class.getSimpleName();
	private static final String COURSES_TIME_PICKER = "courses_time_picker";
	
	private BlocksLayout blocksView;
	private View rootView;
	private TextView tvDisplayDate;
	
	private DialogFragment pickerFragment;
	
	private final Calendar calendar = Calendar.getInstance();
	private SimpleDateFormat dateFormat = null;
	private SimpleDateFormat meetingPatternFormat;
	
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
		}
		
		checkButtonIcons();
		
		refreshSchedule();
		
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void checkButtonIcons() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			// In RTL mode in order for the buttons to look right after they flip the icons need to be switched
			if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL) {
				ImageView forwardButton = (ImageView) findViewById(R.id.courses_daily_date_foward_button);
				forwardButton.setImageResource(R.drawable.ic_calendar_nav_left);
				ImageView backButton = (ImageView) findViewById(R.id.courses_daily_date_back_button);
				backButton.setImageResource(R.drawable.ic_calendar_nav_right);
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.courses_daily_schedule, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int itemId = item.getItemId();

    	if (itemId == R.id.courses_daily_menu_full_schedule) {
    		Intent intent = new Intent(this, CoursesFullScheduleActivity.class);
    		// Pass Extras on to next Activity
    		intent.putExtras(getIntent().getExtras());
    		startActivity(intent);
    		return true;
    	}
    	
    	return super.onOptionsItemSelected(item);
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
			refreshSchedule();	
		}
		
	}
	
	private void setDate(int year, int month, int day) {
		calendar.set(year, month, day);
	}
	
	public void dayAfter(View view) {
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		refreshSchedule();
	}
	
	public void dayBefore(View view) {
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		refreshSchedule();
	}
	
	private void refreshSchedule() {
		String modifiedRequestUrl = createRequestUrl();
		
		// Start retrieval of daily schedule
		RetrieveDailyScheduleTask dailyTask = new RetrieveDailyScheduleTask();
		dailyTask.execute(modifiedRequestUrl);
		
		displayDate();
	}
	
	public String createRequestUrl() {
		// ask for the date of interest +/- 1 day, which allows us to catch meetings that might
		// be date shifted due to translation to Zulu time.
		Calendar cal = Calendar.getInstance();
		
		cal.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
		cal.add(Calendar.DAY_OF_YEAR, -1);
		String startDateString = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH);
		
		cal.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
		cal.add(Calendar.DAY_OF_YEAR, 1);
		String endDateString = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH);
		
		String modifiedRequestUrl = requestUrl + "/" + getEllucianApp().getAppUserId() + "?start=" + startDateString + "&end=" + endDateString;
		
		return modifiedRequestUrl;
	}
	
	private void displayDate() {
		Date date = calendar.getTime();
		tvDisplayDate.setText(formatDate(date));	
	}

	
	private void displaySchedule(DailyScheduleResponse dailySchedule) {
		// create a formatter that always returns a certain date-only format
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

		blocksView.removeAllBlocks();

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
		for (final Day day : dailySchedule.coursesDays){

			Log.d(TAG, "day.date: "+day.date);
			
			for (final Meeting meeting : day.coursesMeetings){

				try {
					Date start = getMeetingPatternDateFormat().parse(meeting.start);
					Date end = getMeetingPatternDateFormat().parse(meeting.end);
					
					// strip the localized date/time to just the date portion for comparison
					// with the targetDateStr
					String startStr = sdf.format(start);
					String endStr = sdf.format(end);
					
					Log.d(TAG, "  meeting start: "+meeting.start+", end: "+meeting.end);
					Log.d(TAG, "    parsed date, start: "+startStr+", end: "+endStr);

					if (startStr.compareTo(targetDateStr) == 0 || endStr.compareTo(targetDateStr) == 0) {

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
						
						String title = getString(R.string.default_course_section_title_format,
											meeting.courseName,
											meeting.courseSectionNumber,
											meeting.sectionTitle);
						title += "\n" + getString(R.string.time_to_time_format, 
											timeFormatter.format(start),
											timeFormatter.format(end));
						
						if (!TextUtils.isEmpty(meeting.building)) {
							
							if (!TextUtils.isEmpty(meeting.room)) {
								title += "\n" + getString(R.string.default_building_and_room_format,
													meeting.building, 
													meeting.room);
							} else {
								title += "\n" + meeting.building;
							}
						}
						
						BlockView blockView = new BlockView(this,
								meeting.courseName, title, start.getTime(), end.getTime(), 1);
						
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
			String requestUrl = params[0];
			MobileClient client = new MobileClient(CoursesDailyScheduleActivity.this);
			client.setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			DailyScheduleResponse dailySchedule = client.getDailySchedule(requestUrl);
			return dailySchedule;
		}
		
		@Override
		protected void onPostExecute(DailyScheduleResponse dailySchedule) {
			if (dailySchedule != null && dailySchedule.coursesDays != null ) {
				Log.d("RetrieveDailyScheduleTask", "Daily schedule retrieved.");
				if (dailySchedule.coursesDays.length > 0) {
					
					displaySchedule(dailySchedule);
			        
				} else {
					Log.d("RetrieveDailyScheduleTask", "No meetings scheduled for this day.");
				}
			} else {
				Log.d("RetrieveDailyScheduleTask", "Daily schedule information was not retrieved correctly");
			}
		}
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
