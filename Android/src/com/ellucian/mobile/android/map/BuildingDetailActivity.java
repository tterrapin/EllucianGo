package com.ellucian.mobile.android.map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.ImageLoader;
import com.ellucian.mobile.android.UICustomizer;
import com.ellucian.mobile.android.Utils;

public class BuildingDetailActivity extends Activity {

	private Double latitude = null;
	private Double longitude = null;
	private String name;

	private Intent buildDirectionsIntent() {

		final Intent intent = new Intent(android.content.Intent.ACTION_VIEW,

		Uri.parse("http://maps.google.com/maps?saddr=" + "&daddr=" + latitude
				+ "," + longitude));
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
		setContentView(R.layout.building_detail);

		final Intent intent = getIntent();

		name = intent.getStringExtra("name");
		setTitle(name);
		UICustomizer.style(this);

		final String type = intent.getStringExtra("type");
		if (type == null) {
			findViewById(R.id.typeLayout).setVisibility(View.GONE);
		} else {
			((TextView) findViewById(R.id.buildingType)).setText(type);
		}
		

		final ImageView img = (ImageView) findViewById(R.id.buildingImage);
		final String imageUrl = intent.getStringExtra("imageUrl");
		if (imageUrl == null) {
			img.setVisibility(View.GONE);
		} else {
			final Bitmap cachedImage = ((EllucianApplication) getApplication())
					.getImageLoader().loadImage(imageUrl,
							new ImageLoader.ImageLoadedListener() {
								public void imageLoaded(Bitmap imageBitmap) {
									img.setImageBitmap(imageBitmap);
								}
							});
			if (cachedImage != null) {
				img.setImageBitmap(cachedImage);
			}
		}

		final String address = intent.getStringExtra("label");
		if (address == null) {
			findViewById(R.id.addressLayout).setVisibility(View.GONE);
		} else {
			((TextView) findViewById(R.id.buildingAddress)).setText(address);
		}

		final String description = intent.getStringExtra("description");
		if (description == null) {
			findViewById(R.id.descriptionLayout).setVisibility(View.GONE);
		} else {
			((TextView) findViewById(R.id.buildingDescription))
					.setText(description);
		}
		if (intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
			latitude = intent.getDoubleExtra("latitude", 0);
			longitude = intent.getDoubleExtra("longitude", 0);
		}

		final Button mapButton = (Button) findViewById(R.id.showMap);
		final Button directionsButton = (Button) findViewById(R.id.showDirections);

		final Intent mapIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://maps.google.com/maps"));

		if (Utils.isIntentAvailable(this, mapIntent)) {
			mapButton.setOnClickListener(new View.OnClickListener() {

				public void onClick(View arg0) {

					startActivity(buildMapIntent());

				}

			});
		} else {
			mapButton.setVisibility(View.GONE);
		}

		final Intent directionsIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://maps.google.com/maps"));

		if (latitude != null && longitude != null
				&& Utils.isIntentAvailable(this, directionsIntent)) {
			directionsButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {

					startActivity(buildDirectionsIntent());

				}
			});
		} else {
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
}
