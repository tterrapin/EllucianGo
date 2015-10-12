/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.maps;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.maps.LayersDialogFragment.LayersDialogFragmentListener;
import com.ellucian.mobile.android.util.PermissionUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsSingleLocationActivity extends EllucianActivity
		implements LayersDialogFragmentListener, LocationSource,
		LocationListener, ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String TAG = "MapsSingleLocationActivity";

    // WRITE is required by Google Maps API to store map tiles.
    private static final int STORAGE_REQUEST_ID = 0;
    private static String[] STORAGE_PERMISSIONS =
            {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    // LOCATION is optional - user can deny and still use maps.
    private static final int LOCATION_REQUEST_ID = 1;
    private static String[] LOCATION_PERMISSIONS =
            {Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION};

    private GoogleMap map;
	private OnLocationChangedListener mapLocationListener = null;
	private LocationManager locMgr = null;
	private final Criteria crit = new Criteria();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Check if the Storage permission is already available. READ is implicitly granted if WRITE is.
        if (!hasStoragePermission()) {
            // Storage permission have not been granted
            requestStoragePermissions();
        } else {
            handleIntent();
        }
	}

    private boolean hasLocationPermission() {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasStoragePermission() {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * (Android M+ only)
     * Before user is prompted to grant permission, explain why we need it.
     * If they choose to never be asked again, they will see this message
     * and then be returned to where they came from.
     */
    private void requestStoragePermissions() {
        Log.d(TAG, "Explain and request storage permission.");
        new AlertDialog.Builder(this)
                .setTitle(R.string.maps_storage_alert_title)
                .setMessage(R.string.maps_storage_alert_message)
                .setPositiveButton(R.string.dialog_continue,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                ActivityCompat.requestPermissions(MapsSingleLocationActivity.this,
                                        STORAGE_PERMISSIONS, STORAGE_REQUEST_ID);
                            }
                        }).create().show();
    }

    /**
     * (Android M+ only)
     * Request location permission. No explanation to user is needed.
     */
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, LOCATION_REQUEST_ID);
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == STORAGE_REQUEST_ID) {
            if (!PermissionUtil.verifyPermissions(grantResults)) {
                // Not all required permissions were granted.
                Log.w(TAG, "The Required STORAGE permission was NOT granted.");
                Toast.makeText(this, R.string.maps_storage_not_granted, Toast.LENGTH_LONG).show();
                finish();  // Close activity.
            } else {
                Log.d(TAG, "Storage permissions have been granted.");
                handleIntent();
            }

        } else if (requestCode == LOCATION_REQUEST_ID) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                // All required permissions have been granted.
                getLocation();
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

	@Override
	protected void onResume() {
		super.onResume();
        if (hasLocationPermission()) {
            getLocation();
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
            try {
                locMgr.removeUpdates(this);
            } catch (Exception e) {
                Log.e(TAG, "Location is not available" + e.getMessage());
            }

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
			map = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
		}
	}

    private void handleIntent() {
        if (readyToGo()) {

            if (!hasLocationPermission()) {
                // Location permissions have not been granted
                requestLocationPermission();
            }

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

            // Don't get user's location if they haven't granted it.
            if (hasLocationPermission()) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            }

            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(location, 17);
            map.moveCamera(cu);
            marker.showInfoWindow();
        }
    }

    private void getLocation() {
        if(locMgr != null) {
            try {
                locMgr.requestLocationUpdates(0L, 0.0f, crit, this, null);
            } catch (Exception e) {
                Log.e(TAG, "Location not available. " + e.getMessage());
            }
        }
    }


    private boolean readyToGo() {
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