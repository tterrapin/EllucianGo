package com.ellucian.mobile.android.courses;

import java.text.DateFormat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.UICustomizer;
import com.ellucian.mobile.android.directory.ProfileActivity;
import com.ellucian.mobile.android.map.MapPinActivity;

public class CourseInformationActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course_information);
		final Intent intent = getIntent();
		final Course course = intent.getParcelableExtra("course");
		final String rosterProfileUrl = intent
				.getStringExtra("rosterProfileUrl");
		setTitle(course.getName() + " " + getResources().getString(R.string.overview));
		UICustomizer.style(this);
		if (course.getSectionTitle() == null) {
			findViewById(R.id.sectionTitle).setVisibility(View.GONE);
		} else {
			((TextView) findViewById(R.id.sectionTitle)).setText(course
					.getSectionTitle());
		}
		final ViewGroup facultyLayout = (ViewGroup) findViewById(R.id.facultyLayout);
		for (final Faculty faculty : course.getFaculty()) {
			final Faculty facultyFinal = faculty;
			final View view = getLayoutInflater().inflate(
					R.layout.course_overview_faculty, facultyLayout, false);
			facultyLayout.addView(view);
			final TextView tv = (TextView) view.findViewById(R.id.facultyName);
			tv.setText(faculty.getName());
			if(facultyFinal.getUsername() != null && facultyFinal.getUsername().length()>0 ) {
			view.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					final Intent intent = new Intent(
							CourseInformationActivity.this,
							ProfileActivity.class);
					intent.putExtra("username", facultyFinal.getUsername());
					intent.putExtra("domain", facultyFinal.getDomain());
					intent.putExtra("profileUrl", rosterProfileUrl);
					intent.putExtra("preferredName", facultyFinal.getName());
					startActivity(intent);
				}
			});
			}
		}
		final ViewGroup sessionLayout = (ViewGroup) findViewById(R.id.sessionLayout);
		for (final Session session : course.getSessions()) {
			final Session sessionFinal = session;
			final View view = getLayoutInflater().inflate(
					R.layout.course_overview_session, sessionLayout, false);
			sessionLayout.addView(view);
			
			DateFormat dateFormat = android.text.format.DateFormat
					.getDateFormat(getApplicationContext());
			TextView tv = (TextView) view.findViewById(R.id.dates);
			tv.setText(dateFormat.format(sessionFinal.getStartDate().getTime()) + " - "
					+ dateFormat.format(sessionFinal.getEndDate().getTime()));
			
			final ViewGroup sessionInformationLayout = (ViewGroup) findViewById(R.id.sessionInformationLayout);
			if ((sessionFinal.getInstructionalMethod() != null && sessionFinal.getInstructionalMethod().length() > 0)
					|| (sessionFinal.getDays() != null && sessionFinal.getDays().length() > 0)
					|| (sessionFinal.getPattern() != null && sessionFinal.getPattern().length() > 0)) {
				tv = (TextView) view.findViewById(R.id.sessionType);
				tv.setText(sessionFinal.getInstructionalMethod());
				tv = (TextView) view.findViewById(R.id.sessionDays);
				tv.setText(sessionFinal.getDays());
				tv = (TextView) view.findViewById(R.id.sessionPattern);
				tv.setText(sessionFinal.getPattern());
			} else {
				sessionInformationLayout.setVisibility(View.GONE);
			}
			tv = (TextView) view.findViewById(R.id.sessionTime);
			if(sessionFinal.getTimeStart() != null && sessionFinal.getTimeEnd() != null) {
			tv.setText(sessionFinal.getTimeStart() + " - "
					+ sessionFinal.getTimeEnd());
			} else {
				tv.setVisibility(View.GONE);
			}
			tv = (TextView) view.findViewById(R.id.sessionLocation);
			if(sessionFinal.getLocation() != null && sessionFinal.getLocation().getBuildingLabel() != null && sessionFinal.getLocation().getBuildingLabel().length() > 0 ) {
				tv.setText(sessionFinal.getLocation().getBuildingLabel());
			} else {
				tv.setVisibility(View.GONE);
			}
			

			
			if (sessionFinal.getLocation() != null
					&& sessionFinal.getLocation().getLatitude() != null
					&& sessionFinal.getLocation().getLongitude() != null) {
				view.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						final Intent intent = new Intent(
								CourseInformationActivity.this,
								MapPinActivity.class);
						intent.putExtra("latitude", sessionFinal.getLocation()
								.getLatitude());
						intent.putExtra("longitude", sessionFinal.getLocation()
								.getLongitude());
						intent.putExtra("title", sessionFinal.getLocation()
								.getBuildingLabel());
						intent.putExtra("primaryColor",
								UICustomizer.primaryColor);
						intent.putExtra("secondaryColor",
								UICustomizer.secondaryColor);
						intent.putExtra("accentColor", UICustomizer.accentColor);
						intent.putExtra("name", sessionFinal.getLocation()
								.getBuildingName());
						intent.putExtra("label", sessionFinal.getLocation()
								.getLabel());
						intent.putExtra("description", sessionFinal
								.getLocation().getDescription());
						intent.putExtra("imageUrl", sessionFinal.getLocation()
								.getImage());
						// intent.putExtra("type",
						// sessionFinal.getLocation().getType()); //TODO
						startActivity(intent);
					}
				});
			}
		}
		if (course.getDescription() == null) {
			findViewById(R.id.descriptionLayout).setVisibility(View.GONE);
		} else {
			((TextView) findViewById(R.id.description)).setText(course
					.getDescription());
		}
	}
}
