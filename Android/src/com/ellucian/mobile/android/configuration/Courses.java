package com.ellucian.mobile.android.configuration;

import android.content.Context;
import android.content.Intent;

import com.ellucian.mobile.android.courses.CoursesActivity;

public class Courses extends AbstractModule {

	private String coursesUrl;
	private String assignmentsUrl;
	private String rosterUrl;
	private String rosterProfileUrl;
	private String announcementsUrl;
	private String eventsUrl;

	@Override
	public Intent buildIntent(Context context) {
		Intent intent = new Intent(context, CoursesActivity.class);
		intent.putExtra("coursesUrl", coursesUrl);
		intent.putExtra("assignmentsUrl", assignmentsUrl);
		intent.putExtra("rosterUrl", rosterUrl);
		intent.putExtra("rosterProfileUrl", rosterProfileUrl);
		intent.putExtra("announcementsUrl", announcementsUrl);
		intent.putExtra("eventsUrl", eventsUrl);
		intent.putExtra("title", getName());
		return intent;
	}

	public void setCoursesUrl(String coursesUrl) {
		this.coursesUrl = coursesUrl;
	}

	public void setAssignmentsUrl(String assignmentsUrl) {
		this.assignmentsUrl = assignmentsUrl;
	}

	public void setRosterUrl(String rosterUrl) {
		this.rosterUrl = rosterUrl;
	}

	public void setRosterProfileUrl(String rosterProfileUrl) {
		this.rosterProfileUrl = rosterProfileUrl;
	}

	public void setAnnouncementsUrl(String announcementsUrl) {
		this.announcementsUrl = announcementsUrl;
	}

	public void setEventsUrl(String eventsUrl) {
		this.eventsUrl = eventsUrl;
	}




}
