package com.ellucian.mobile.android.courses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.UICustomizer;

public class CourseOverviewActivity extends ListActivity {
	private String announcementsUrl;
	private String assignmentsUrl;
	private Course course;
	private String coursesUrl;
	private String eventsUrl;
	private String rosterProfileUrl;
	private String rosterUrl;

	protected void addItem(List<Map<String, Object>> data, String name,
			Intent intent) {
		final Map<String, Object> temp = new HashMap<String, Object>();
		temp.put("title", name);
		temp.put("intent", intent);
		data.add(temp);
	}

	private String buildUrl(String url) {
		return url + "&type=" + course.getType() + "&id="
				+ course.getIdForUrl();
	}

	protected List<Map<String, Object>> getData() {
		final List<Map<String, Object>> myData = new ArrayList<Map<String, Object>>();
		final Intent informationIntent = new Intent(
				CourseOverviewActivity.this, CourseInformationActivity.class);
		informationIntent.putExtra("course", course);
		informationIntent.putExtra("rosterProfileUrl", rosterProfileUrl);
		informationIntent.putExtra("title", course.getName());
		addItem(myData, getResources().getString(R.string.overview),
				informationIntent);
		if (course.allowsAssignments()) {
			final Intent assignmentsIntent = new Intent(
					CourseOverviewActivity.this, AssignmentsActivity.class);
			assignmentsIntent.putExtra("course", course);
			assignmentsIntent.putExtra("assignmentsUrl", assignmentsUrl);
			assignmentsIntent.putExtra("title", course.getName());
			addItem(myData, getResources().getString(R.string.assignments),
					assignmentsIntent);
		}
		final Intent gradesIntent = new Intent(CourseOverviewActivity.this,
				GradesActivity.class);
		gradesIntent.putExtra("course", course);
		gradesIntent.putExtra("coursesUrl", coursesUrl);
		gradesIntent.putExtra("title", course.getName());
		addItem(myData, getResources().getString(R.string.grades), gradesIntent);
		final Intent rosterIntent = new Intent(CourseOverviewActivity.this,
				RosterActivity.class);
		rosterIntent.putExtra("rosterUrl", rosterUrl);
		rosterIntent.putExtra("rosterProfileUrl", rosterProfileUrl);
		rosterIntent.putExtra("title", course.getName());
		rosterIntent.putExtra("courseSectionTitle", course.getSectionTitle());
		addItem(myData, getResources().getString(R.string.roster), rosterIntent);
		if (course.allowsAnnouncements()) {
			final Intent announcementsIntent = new Intent(
				CourseOverviewActivity.this, CourseAnnouncementsActivity.class);
			announcementsIntent.putExtra("authenticatedUrl", this.announcementsUrl);
			announcementsIntent.putExtra("course", course);
			addItem(myData, getResources().getString(R.string.courseAnnouncements),
				announcementsIntent);
		}
		if (course.allowsEvents()) {
			final Intent eventsIntent = new Intent(CourseOverviewActivity.this,
				CourseEventsActivity.class);
			eventsIntent.putExtra("authenticatedUrl", this.eventsUrl);
			eventsIntent.putExtra("title", course.getName());
			eventsIntent.putExtra("course", course);
			addItem(myData, getResources().getString(R.string.events), eventsIntent);
		}
		return myData;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course_overview);
		final Intent intent = getIntent();
		course = intent.getParcelableExtra("course");
		coursesUrl = getIntent().getStringExtra("coursesUrl");
		assignmentsUrl = buildUrl(getIntent().getStringExtra("assignmentsUrl"));
		rosterUrl = getIntent().getStringExtra("rosterUrl") + "&courseId="
				+ course.getCourseId();
		rosterProfileUrl = getIntent().getStringExtra("rosterProfileUrl");
		announcementsUrl = buildUrl(getIntent().getStringExtra(
				"announcementsUrl"));
		eventsUrl = buildUrl(getIntent().getStringExtra("eventsUrl"));
		setTitle(course.getName());
		UICustomizer.style(this);
		((TextView) findViewById(R.id.sectionTitle)).setText(course
				.getSectionTitle());
		setListAdapter(new SimpleAdapter(this, getData(),
				android.R.layout.simple_list_item_1, new String[] { "title" },
				new int[] { android.R.id.text1 }));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		@SuppressWarnings("unchecked")
		final Map<String, Object> map = (Map<String, Object>) l
				.getItemAtPosition(position);
		final Intent intent = (Intent) map.get("intent");
		startActivity(intent);
	}
}
