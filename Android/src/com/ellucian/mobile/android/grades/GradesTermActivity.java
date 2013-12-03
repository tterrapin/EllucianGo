package com.ellucian.mobile.android.grades;

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

public class GradesTermActivity extends ListActivity {
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
					R.layout.grades_course_list_row, null);
			final TextView grade = (TextView) convertView
					.findViewById(R.id.grade);
			final TextView name = (TextView) convertView
					.findViewById(R.id.name);
			final TextView title = (TextView) convertView
					.findViewById(R.id.title);
			if(course.getFinalGrade() != null) {
				grade.setText(course.getFinalGrade().getGrade());
			}
			name.setText(course.getName());
			title.setText(course.getSectionTitle());
			return convertView;
		}
	}

	private String url;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grades_courses_list);
		final Intent intent = getIntent();
		final Term term = intent.getParcelableExtra("term");
		url = getIntent().getStringExtra("url");

		this.setTitle(term.getName());
		UICustomizer.style(this);
		setListAdapter(new CoursesAdapter(this, term.getCourses()));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final Course course = (Course) l.getItemAtPosition(position);
		final Intent intent = new Intent(GradesTermActivity.this,
				GradesListActivity.class);
		intent.putExtra("course", course);
		intent.putExtra("url", url);
		startActivity(intent);
	}
}
