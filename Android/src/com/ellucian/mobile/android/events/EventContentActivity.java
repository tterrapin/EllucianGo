package com.ellucian.mobile.android.events;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.UICustomizer;
import com.ellucian.mobile.android.Utils;

public class EventContentActivity extends Activity {

	private String title;
	private long startDateLong;
	private long endDateLong;
	private String location;
	private String description;
	private boolean allDay;
	private String startDate;
	private String endDate;
	private String startTime;
	private String endTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.events_detail);

		UICustomizer.style(this);

		final Intent intent = getIntent();
		allDay = intent.getBooleanExtra("allDay", false);
		startDateLong = intent.getLongExtra("startDateLong", 0);
		endDateLong = intent.getLongExtra("endDateLong", 0);
		startDate = intent.getStringExtra("startDate");
		endDate = intent.getStringExtra("endDate");
		title = intent.getStringExtra("title");
		description = intent.getStringExtra("description");
		startTime = intent.getStringExtra("startTime");
		endTime = intent.getStringExtra("endTime");

		if (allDay) {
			startTime = endTime = getResources().getString(R.string.allDay);
			endDateLong = intent.getLongExtra("endDateLong", 0) - 24 * 60 * 60
					* 1000;
			findViewById(R.id.allDayEventLabel).setVisibility(View.VISIBLE);
			(findViewById(R.id.eventStartTime)).setVisibility(View.GONE);
			(findViewById(R.id.eventEndTime)).setVisibility(View.GONE);

		}

		Log.d(EllucianApplication.TAG,
				"start=" + new Date(startDateLong).toString());
		Log.d(EllucianApplication.TAG, "end=" + new Date(endDateLong).toString());

		Log.d(EllucianApplication.TAG, "Event : " + (allDay ? "ALL DAY " : "")
				+ startDate + " (" + startDateLong + ") " + endDate + " ("
				+ endDateLong + ") " + title);

		((TextView) findViewById(R.id.eventTitle)).setText(title);
		((TextView) findViewById(R.id.eventStartDate)).setText(intent
				.getStringExtra("startDate"));
		((TextView) findViewById(R.id.eventStartTime)).setText(startTime);
		((TextView) findViewById(R.id.eventEndDate)).setText(intent
				.getStringExtra("endDate"));
		((TextView) findViewById(R.id.eventEndTime)).setText(endTime);
		TextView locationTV = ((TextView) findViewById(R.id.eventLocation));
		location = intent.getStringExtra("location");
		if (location == null) {
			locationTV.setVisibility(View.GONE);
		} else {
			locationTV.setText(location);
		}

		((TextView) findViewById(R.id.eventDescription)).setText(description);

		Button calendarButton = ((Button) findViewById(R.id.eventCalendarButton));
		Button shareButton = ((Button) findViewById(R.id.eventShareButton));

		final Intent calendarIntent = buildCalendarIntent();

		if (Utils.isIntentAvailable(this, calendarIntent)) {
			calendarButton.setOnClickListener(new View.OnClickListener() {

				public void onClick(View arg0) {

					startActivity(calendarIntent);

				}

			});
		} else {
			calendarButton.setVisibility(View.GONE);
		}
		final Intent emailIntent = buildEmailIntent();

		if (Utils.isIntentAvailable(this, emailIntent)) {
			shareButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {

					startActivity(emailIntent);

				}
			});
		} else {
			shareButton.setVisibility(View.GONE);
		}
	}

	private Intent buildCalendarIntent() {
		final Intent calendarIntent = new Intent(Intent.ACTION_EDIT);
		calendarIntent.setType("vnd.android.cursor.item/event");
		Log.d("beginTime", new java.util.Date(startDateLong).toLocaleString());
		calendarIntent.putExtra("beginTime", startDateLong);
		calendarIntent.putExtra("allDay", allDay);
		calendarIntent.putExtra("endTime", endDateLong);
		Log.d("endTime", new java.util.Date(endDateLong).toLocaleString());
		calendarIntent.putExtra("title", title);
		calendarIntent.putExtra("eventLocation", location);
		calendarIntent.putExtra("description", description);
		return calendarIntent;
	}

	private Intent buildEmailIntent() {
		final Intent emailIntent = new Intent(
				android.content.Intent.ACTION_SEND);

		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);

		emailIntent.setType("plain/text");
		String format = null;
		String text = null;
		if (location != null && description != null) {
			format = getResources().getString(
					R.string.eventEmailTextWithDescriptionAndLocation);
			text = String.format(format, description, location, startDate,
					endDate, startTime != null ? startTime : "",
					endTime != null ? endTime : "");
		} else if (location != null) {
			format = getResources().getString(
					R.string.eventEmailTextWithLocation);
			text = String.format(format, location, startDate,
					endDate, startTime != null ? startTime : "",
					endTime != null ? endTime : "");
		} else if (description != null) {
			format = getResources().getString(
					R.string.eventEmailTextWithDescription);
			text = String.format(format, description, startDate,
					endDate, startTime != null ? startTime : "",
					endTime != null ? endTime : "");
		} else {
			format = getResources().getString(R.string.eventEmailText);
			text = String.format(format, startDate,
					endDate, startTime != null ? startTime : "",
					endTime != null ? endTime : "");
		}
		
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
		return emailIntent;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.events_detail, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.eventsShare).setVisible(
				Utils.isIntentAvailable(this, buildEmailIntent()));

		menu.findItem(R.id.eventsCalendar).setVisible(
				Utils.isIntentAvailable(this, buildCalendarIntent()));

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.eventsShare:
			Intent emailIntent = buildEmailIntent();
			startActivity(emailIntent);
			break;
		case R.id.eventsCalendar:
			Intent calendarIntent = buildCalendarIntent();
			startActivity(calendarIntent);
			break;
		}
		return false;
	}
}
