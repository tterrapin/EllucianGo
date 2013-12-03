package com.ellucian.mobile.android.courses;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.UICustomizer;
import com.ellucian.mobile.android.Utils;

public class AssignmentDetailActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.assignment_detail);
		UICustomizer.style(this);
		final Intent intent = getIntent();
		final Assignment assignment = (Assignment) intent
				.getParcelableExtra("assignment");
		((TextView) findViewById(R.id.assignmentTitle)).setText(assignment
				.getName());
		((TextView) findViewById(R.id.assignmentDescription))
				.setText(assignment.getDescription());
		
		if (assignment.getDueDate() != null) {
			final Date date = assignment.getDueDate().getTime();
			DateFormat dateFormat = android.text.format.DateFormat
					.getDateFormat(getApplicationContext());
			DateFormat timeFormat = android.text.format.DateFormat
					.getTimeFormat(getApplicationContext());

			((TextView) findViewById(R.id.assignmentDueDate))
					.setText(dateFormat.format(date) + " " + timeFormat.format(date));
		}

		final String website = assignment.getUrl();
		final Button button = (Button) findViewById(R.id.goToAssignmentButton);
		final Uri data = Uri.parse(website);
		final Intent websiteIntent = new Intent(Intent.ACTION_VIEW, data);
		if (Utils.isIntentAvailable(this, websiteIntent)) {
			button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					startActivity(websiteIntent);
				}
			});
		} else {
			button.setVisibility(View.GONE);
		}
	}
}
