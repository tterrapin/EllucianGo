package com.ellucian.mobile.android.courses;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.UICustomizer;

public class CoursesTermActivity extends ListActivity {
	private class CoursesAdapter extends BaseAdapter {
		private final List<Course> courses;
		private final LayoutInflater mInflater;

		public CoursesAdapter(Context context, List<Course> items) {
			this.courses = items;
			mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			return courses.size();
		}

		public Object getItem(int position) {
			return courses.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final Course course = courses.get(position);
			convertView = mInflater.inflate(
					android.R.layout.simple_list_item_2, null);
			final TextView name = (TextView) convertView
					.findViewById(android.R.id.text1);
			final TextView title = (TextView) convertView
					.findViewById(android.R.id.text2);
			name.setText(course.getName());
			title.setText(course.getSectionTitle());
			return convertView;
		}
	}

	private String announcementsUrl;
	private String assignmentsUrl;
	private String coursesUrl;
	private String eventsUrl;
	private String rosterProfileUrl;
	private String rosterUrl;

	// private TermsList terms;
	// private int position;
	// private String url;
	// private String title;
	// private String jsonCourses;
	// @SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.courses_list);
		final Intent intent = getIntent();
		final Term term = intent.getParcelableExtra("term");
		coursesUrl = getIntent().getStringExtra("coursesUrl");
		assignmentsUrl = getIntent().getStringExtra("assignmentsUrl");
		rosterUrl = getIntent().getStringExtra("rosterUrl");
		rosterProfileUrl = getIntent().getStringExtra("rosterProfileUrl");
		announcementsUrl = getIntent().getStringExtra("announcementsUrl");
		eventsUrl = getIntent().getStringExtra("eventsUrl");
		// position = intent.getIntExtra("position", 0);
		// title = intent.getStringExtra("title");
		// url = intent.getStringExtra("url");
		this.setTitle(term.getName());
		UICustomizer.style(this);
		setListAdapter(new CoursesAdapter(this, term.getCourses()));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final Course course = (Course) l.getItemAtPosition(position);
		final Intent intent = new Intent(CoursesTermActivity.this,
				CourseOverviewActivity.class);
		intent.putExtra("course", course);
		intent.putExtra("coursesUrl", coursesUrl);
		intent.putExtra("assignmentsUrl", assignmentsUrl);
		intent.putExtra("rosterUrl", rosterUrl);
		intent.putExtra("rosterProfileUrl", rosterProfileUrl);
		intent.putExtra("announcementsUrl", announcementsUrl);
		intent.putExtra("eventsUrl", eventsUrl);
		startActivity(intent);
	}
}
