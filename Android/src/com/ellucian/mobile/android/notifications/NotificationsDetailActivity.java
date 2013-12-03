package com.ellucian.mobile.android.notifications;

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

public class NotificationsDetailActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notifications_detail);

		final Intent intent = getIntent();
		setTitle(intent.getStringExtra("description"));
		UICustomizer.style(this);

		final String descriptionDetails = intent
				.getStringExtra("descriptionDetails");

		((TextView) findViewById(R.id.details)).setText(descriptionDetails);
		
		final TextView notificationDate = (TextView) findViewById(R.id.notificationDate);
		notificationDate.setText(intent.getStringExtra("startDate"));

		final String linkLabel = intent.getStringExtra("linkLabel");
		final String hyperlink = intent.getStringExtra("hyperlink");
		final Button button = (Button) findViewById(R.id.actionButton);
		if (intent.hasExtra("linkLabel") && linkLabel != null) {

			button.setText(linkLabel);
		}

		if (intent.hasExtra("hyperlink") && hyperlink != null) {
			final Intent websiteIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(hyperlink));
			boolean intentAvailable = Utils.isIntentAvailable(this,
					websiteIntent);
			if (intentAvailable) {
				button.setOnClickListener(new View.OnClickListener() {

					public void onClick(View arg0) {

						startActivity(websiteIntent);
					}

				});
			} else {
				button.setVisibility(View.GONE);
			}

		} else {
			button.setVisibility(View.GONE);
		}
	}
}
