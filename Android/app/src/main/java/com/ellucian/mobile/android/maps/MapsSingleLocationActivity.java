/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.maps;

import android.app.Dialog;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.maps.LayersDialogFragment.LayersDialogFragmentListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsSingleLocationActivity extends EllucianActivity
		implements LayersDialogFragmentListener, LocationSource,
		LocationListener {

	private GoogleMap map;
	private OnLocationChangedListener mapLocationListener = null;
	private LocationManager locMgr = null;
	private Criteria crit = new Criteria();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (readyToGo()) {
			setContentView(R.layout.activity_maps_single_location);
			setUpMapIfNeeded();
			Intent intent = getIntent();
			final String title = intent.getStringExtra("title");
			LatLng location = new LatLng(intent.getDoubleExtra("latitude", 0),
					intent.getDoubleExtra("longitude", 0));
			setTitle(title);
			
			map.setInfoWindowAdapter(new CustomInfoWindowAdapter(getLayoutInflater()));
			Marker marker = map.addMarker(new MarkerOptions().position(location).title(title));
			
			locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
			crit.setAccuracy(Criteria.ACCURACY_COARSE);

			map.setMyLocationEnabled(true);
			map.getUiSettings().setMyLocationButtonEnabled(true);

			CameraUpdate cu = CameraUpdateFactory.newLatLng(location);
			map.moveCamera(cu);
			marker.showInfoWindow();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (locMgr != null) {
			try {
				locMgr.requestLocationUpdates(0L, 0.0f, crit, this, null);
			} catch (IllegalArgumentException e) {
				//java.lang.IllegalArgumentException: no providers found for criteria
			}
		}
		if (map != null) {
			map.setLocationSource(this);
			map.setIndoorEnabled(true);
		}

	}

	@Override
	protected void onPause() {
		if (map != null) {
			map.setLocationSource(null);
			map.setIndoorEnabled(false);
		}
		if (locMgr != null)
			locMgr.removeUpdates(this);

		super.onPause();
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		this.mapLocationListener = listener;
	}

	@Override
	public void deactivate() {
		this.mapLocationListener = null;
	}

	@Override
	public void onLocationChanged(Location location) {
		if (mapLocationListener != null) {
			mapLocationListener.onLocationChanged(location);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// unused
	}

	@Override
	public void onProviderEnabled(String provider) {
		// unused
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// unused
	}

	private void setUpMapIfNeeded() {
		if (map == null) {
			map = ((MapFragment) getFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			if (map != null) {
				// map.setOnInfoWindowClickListener(this);
			}
		}
	}

	protected boolean readyToGo() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		if (status == ConnectionResult.SUCCESS) {
			return (true);
		} else if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
			// http://stackoverflow.com/questions/13932474/googleplayservicesutil-geterrordialog-is-null
			// https://code.google.com/p/gmaps-api-issues/issues/detail?id=4720&q=store&colspec=ID%20Type%20Status%20Introduced%20Fixed%20Summary%20Stars%20ApiType%20Internal
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,
					0);
			if (dialog != null) {
				dialog.show();
			} else {
				Toast.makeText(this,
						getString(R.string.maps_feature_not_supported),
						Toast.LENGTH_LONG).show();
				finish();
			}
		} else {
			Toast.makeText(this,
					getString(R.string.maps_feature_not_supported),
					Toast.LENGTH_LONG).show();
			finish();
		}

		return (false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_maps_single_location, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.maps_layers:
			sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, "Change map view", null, moduleName);
			showLayersDialog();
			return true;
		case R.id.maps_legal:
			startActivity(new Intent(this, LegalNoticesActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void showLayersDialog() {
		int layer = 0;
		switch (map.getMapType()) {
		case GoogleMap.MAP_TYPE_NORMAL:
			layer = MAP_TYPE_NORMAL;
			break;
		case GoogleMap.MAP_TYPE_SATELLITE:
			layer = MAP_TYPE_SATELLITE;
			break;
		case GoogleMap.MAP_TYPE_TERRAIN:
			layer = MAP_TYPE_TERRAIN;
			break;
		case GoogleMap.MAP_TYPE_HYBRID:
			layer = MAP_TYPE_HYBRID;
			break;

		}
		LayersDialogFragment layersDialog = LayersDialogFragment
				.newInstance(layer);
		layersDialog.setLayersDialogFragmentListener(this);
		layersDialog.show(getFragmentManager(), null);

	}

	@Override
	public void setLayer(int layer) {
		switch (layer) {
		case MAP_TYPE_NORMAL:
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			break;
		case MAP_TYPE_SATELLITE:
			map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			break;
		case MAP_TYPE_TERRAIN:
			map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			break;
		case MAP_TYPE_HYBRID:
			map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			break;
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		sendView("Map of campus", moduleName);
	}

}