package com.ellucian.mobile.android.grades;

import java.util.Date;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.UICustomizer;

public class GradesListActivity extends ListActivity {
	private class GradesAdapter extends BaseAdapter {
		private final List<Grade> items;
		private final LayoutInflater mInflater;

		public GradesAdapter(Context context, List<Grade> items) {
			this.items = items;
			mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			return items.size();
		}

		public Object getItem(int position) {
			return items.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final Grade item = items.get(position);
			convertView = mInflater.inflate(R.layout.grades_list_row, null);
			final TextView gradeValue = (TextView) convertView
					.findViewById(R.id.gradeValue);
			gradeValue.setText(item.getGrade());
			final TextView gradeLabel = (TextView) convertView
					.findViewById(R.id.gradeLabel);
			gradeLabel.setText(item.getLabel());
			if (item.getUpdatedDate() != null) {
				final TextView gradeLastUpdated = (TextView) convertView
						.findViewById(R.id.gradeLastUpdated);
				convertView.findViewById(R.id.gradeLastUpdatedLabel)
						.setVisibility(View.VISIBLE);
				gradeLastUpdated.setVisibility(View.VISIBLE);
				final Date date = item.getUpdatedDate().getTime();
				final java.text.DateFormat dateFormat = android.text.format.DateFormat
						.getDateFormat(getApplicationContext());
				gradeLastUpdated.setText(dateFormat.format(date));
			} else {
				convertView.findViewById(R.id.gradeLastUpdated).setVisibility(
						View.INVISIBLE);
				convertView.findViewById(R.id.gradeLastUpdatedLabel)
						.setVisibility(View.INVISIBLE);
			}
			return convertView;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grades_list);
		final Intent intent = getIntent();
		final Course course = intent.getParcelableExtra("course");
		
		setTitle(course.getName() + " " + getResources().getString(R.string.grades));
		UICustomizer.style(this);
		if (course.getSectionTitle() == null) {
			findViewById(R.id.sectionTitle).setVisibility(View.GONE);
		} else {
			((TextView) findViewById(R.id.sectionTitle)).setText(course
					.getSectionTitle());
		}
		
		final ListAdapter adapter = new GradesAdapter(this, course.getGrades());
		setListAdapter(adapter);
	}
}
