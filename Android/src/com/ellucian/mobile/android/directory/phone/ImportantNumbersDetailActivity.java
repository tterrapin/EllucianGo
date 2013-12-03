package com.ellucian.mobile.android.directory.phone;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.UICustomizer;
import com.ellucian.mobile.android.Utils;
import com.ellucian.mobile.android.map.MapPinActivity;

public class ImportantNumbersDetailActivity extends Activity {

	private Double latitude = null;
	private Double longitude = null;
	private String name;
	private boolean showDirections = false;
	private boolean showMap = false;

	private Intent buildDirectionsIntent() {

		final Intent intent = new Intent(android.content.Intent.ACTION_VIEW,

		Uri.parse("http://maps.google.com/maps?saddr=" +

		"&daddr=" + latitude + "," + longitude));
		return intent;
	}

	private Intent buildMapIntent() {
		final Intent mapIntent = new Intent(this, MapPinActivity.class);
		mapIntent.putExtra("latitude", latitude);
		mapIntent.putExtra("longitude", longitude);
		mapIntent.putExtra("title", name);
		mapIntent.putExtra("primaryColor", UICustomizer.primaryColor);
		mapIntent.putExtra("secondaryColor", UICustomizer.secondaryColor);
		mapIntent.putExtra("accentColor", UICustomizer.accentColor);
		return mapIntent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.important_numbers_detail);

		final Intent intent = getIntent();

		name = intent.getStringExtra("name");
		setTitle(name);
		UICustomizer.style(this);

		final String phone = intent.getStringExtra("phone");
		if (!intent.hasExtra("phone") || phone == null) {
			findViewById(R.id.phoneLayout).setVisibility(View.GONE);
		} else {
			((TextView) findViewById(R.id.impNumPhone)).setText(phone);
		}
		final String email = intent.getStringExtra("email");
		if (!intent.hasExtra("email") || email == null) {
			findViewById(R.id.emailLayout).setVisibility(View.GONE);
		} else {
			((TextView) findViewById(R.id.impNumEmail)).setText(email);
		}
		final String address = intent.getStringExtra("label");
		if (!intent.hasExtra("label") || address == null) {
			findViewById(R.id.addressLayout).setVisibility(View.GONE);
		} else {
			((TextView) findViewById(R.id.impNumAddress)).setText(address);
		}
		if (intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
			latitude = intent.getDoubleExtra("latitude", 0);
			longitude = intent.getDoubleExtra("longitude", 0);
		}

		final Button mapButton = (Button) findViewById(R.id.showMap);
		final Button directionsButton = (Button) findViewById(R.id.showDirections);

		final Intent mapIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://maps.google.com/maps"));

		if (latitude != null && longitude != null
				&& Utils.isIntentAvailable(this, mapIntent)) {
			showMap = true;
			mapButton.setOnClickListener(new View.OnClickListener() {

				public void onClick(View arg0) {

					startActivity(buildMapIntent());

				}

			});
		} else {
			showMap = false;
			mapButton.setVisibility(View.GONE);
		}

		final Intent directionsIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://maps.google.com/maps"));

		if (latitude != null && longitude != null
				&& Utils.isIntentAvailable(this, directionsIntent)) {
			showDirections = true;
			directionsButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {

					startActivity(buildDirectionsIntent());

				}
			});
		} else {
			showDirections = false;
			directionsButton.setVisibility(View.GONE);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.important_numbers_detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_map:
			startActivity(buildMapIntent());
			break;
		case R.id.menu_directions:
			startActivity(buildDirectionsIntent());
			break;
		}
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_map).setEnabled(showMap);
		menu.findItem(R.id.menu_directions).setEnabled(showDirections);
		return super.onPrepareOptionsMenu(menu);
	}
}
