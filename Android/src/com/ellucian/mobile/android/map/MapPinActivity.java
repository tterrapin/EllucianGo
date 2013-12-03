package com.ellucian.mobile.android.map;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ellucian.mobile.android.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MapPinActivity extends MapActivity {
	public class PinItemizedOvelay extends ItemizedOverlay<OverlayItem> {
		private final ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

		public PinItemizedOvelay(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
		}

		public void addOverlay(OverlayItem overlay) {
			mOverlays.add(overlay);
			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return mOverlays.get(i);
		}

		@Override
		protected boolean onTap(int index) {
			final OverlayItem item = mOverlays.get(index);
			final AlertDialog.Builder dialog = new AlertDialog.Builder(
					MapPinActivity.this);
			dialog.setMessage(item.getTitle());
			dialog.setPositiveButton(getResources().getString(R.string.getDirections),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							startActivity(buildDirectionsIntent());
						}
					});
			if(showBuildDetailOption()) {
				dialog.setNegativeButton(getResources().getString(R.string.showDetails),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							startActivity(buildDetailIntent());
						}
					});
			}
			dialog.show();
			return true;
		}

		@Override
		public int size() {
			return mOverlays.size();
		}
	}

	Drawable drawable;
	PinItemizedOvelay itemizedOverlay;
	private double latitude;
	LinearLayout linearLayout;
	private double longitude;
	List<Overlay> mapOverlays;
	MapView mapView;
	private String name;
	private String label;
	private String description;
	private String imageUrl;
	private String type;

	private Intent buildDirectionsIntent() {
		// String lat = "";
		// String lon = "";
		// if (location != null) {
		// lat = "" + location.getLatitude();
		// lon = "" + location.getLongitude();
		// }
		final Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
				Uri.parse("http://maps.google.com/maps?saddr=" +
				// lat + ","+ lon +
						"&daddr=" + latitude + "," + longitude));
		return intent;
	}

	public Intent buildDetailIntent() {
		final Intent intent = new Intent(MapPinActivity.this,
				BuildingDetailActivity.class);
		intent.putExtra("name", name);
		intent.putExtra("label", label);
		intent.putExtra("description", description);
		intent.putExtra("latitude", latitude);
		intent.putExtra("longitude", longitude);
		intent.putExtra("imageUrl", imageUrl);
		intent.putExtra("type", type);
		return intent;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.map_pin);
		Intent intent = getIntent();
		final String title = intent.getStringExtra("title");
		latitude = intent.getDoubleExtra("latitude", 0);
		longitude = intent.getDoubleExtra("longitude", 0);
		setTitle(title);
		// UICustomizer.style(this);
		style(this, getIntent().getIntExtra("primaryColor", 0), getIntent()
				.getIntExtra("secondaryColor", 0),
				getIntent().getIntExtra("accentColor", 0));
		name = intent.getStringExtra("name");
		label = intent.getStringExtra("label");
		description = intent.getStringExtra("description");
		imageUrl = intent.getStringExtra("imageUrl");
		type = intent.getStringExtra("type");
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapOverlays = mapView.getOverlays();
		drawable = this.getResources().getDrawable(R.drawable.pushpin);
		itemizedOverlay = new PinItemizedOvelay(drawable);
		final GeoPoint point = new GeoPoint((int) (latitude * 1000000),
				(int) (longitude * 1000000));
		final OverlayItem overlayitem = new OverlayItem(point, title, "");
		mapView.getController().animateTo(point);
		// mapView.getController().zoomToSpan(itemizedOverlay.getLatSpanE6(),
		// itemizedOverlay.getLonSpanE6());
		mapView.getController().setZoom(17);
		itemizedOverlay.addOverlay(overlayitem);
		mapOverlays.add(itemizedOverlay);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_pin, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_details).setVisible(showBuildDetailOption());
		menu.findItem(R.id.menu_map_satellite).setVisible(
				!mapView.isSatellite());
		menu.findItem(R.id.menu_map).setVisible(mapView.isSatellite());
		return super.onPrepareOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_directions:
			startActivity(buildDirectionsIntent());
			break;
		case R.id.menu_details:
			startActivity(buildDetailIntent());
			break;
		case R.id.menu_map_satellite:
			mapView.setSatellite(true);
			mapView.invalidate();
			return true;
		case R.id.menu_map:
			mapView.setSatellite(false);
			mapView.invalidate();
			return true;
		}
		return false;
	}

	private boolean showBuildDetailOption() {
		return name != null || label != null || description != null
				|| imageUrl != null || type != null;
	}

	private void style(Activity activity, int primaryColor, int secondaryColor,
			int accentColor) {
		final TextView title = (TextView) activity
				.findViewById(R.id.titlebar_title);
		final View separator = activity.findViewById(R.id.titlebar_separator);
		final ProgressBar progressBar = (ProgressBar) activity
				.findViewById(R.id.titlebar_progress_circular);
		final View container = activity.findViewById(R.id.titlebar_container);
		separator.setBackgroundColor(accentColor);
		title.setText(activity.getTitle());
		title.setTextColor(secondaryColor);
		progressBar.setDrawingCacheBackgroundColor(primaryColor);
		progressBar.setBackgroundColor(primaryColor);
		container.setBackgroundColor(primaryColor);
		separator.setBackgroundColor(accentColor);
	}
}