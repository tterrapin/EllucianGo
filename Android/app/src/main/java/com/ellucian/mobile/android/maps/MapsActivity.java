/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.maps;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.services.MapsIntentService;
import com.ellucian.mobile.android.maps.LayersDialogFragment.LayersDialogFragmentListener;
import com.ellucian.mobile.android.provider.EllucianContract.MapsBuildings;
import com.ellucian.mobile.android.provider.EllucianContract.MapsCampuses;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;
import com.ellucian.mobile.android.provider.EllucianDatabase.Tables;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.PermissionUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MapsActivity extends EllucianActivity implements
		LayersDialogFragmentListener, OnInfoWindowClickListener,
		AdapterView.OnItemSelectedListener,
		LocationSource, LocationListener,
		LoaderManager.LoaderCallbacks<Cursor>,
        ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String TAG = "MapsActivity";
	private static final String STATE_NAV = "nav";

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
	private SimpleCursorAdapter campusAdapter;
	private final HashMap<Marker, Building> markers = new HashMap<>();
	private boolean campusesLoadedFirstTime;
	private Location lastKnownLocation;
    private Spinner spinner;

    public void onCreate(Bundle savedInstanceState) {
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
                                ActivityCompat.requestPermissions(MapsActivity.this,
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
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
        if (!hasStoragePermission()) {
            // Storage permission have not been granted
            requestStoragePermissions();
        } else {
            handleIntent();
        }
    }

    private void handleIntent() {
		
		if (readyToGo()) {

            if (!hasLocationPermission()) {
                // Location permissions have not been granted
                requestLocationPermission();
            }

			setContentView(R.layout.activity_maps);
			setUpMapIfNeeded();
			
			locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
			crit.setAccuracy(Criteria.ACCURACY_COARSE);

            // Don't get user's location if they haven't granted it.
            if (hasLocationPermission()) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            }
			map.setInfoWindowAdapter(new CustomInfoWindowAdapter(getLayoutInflater()));
			
			String provider = locMgr.getBestProvider(crit, true);
			if(provider != null) {
				sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, "Geolocate user", null, moduleName);
                try {
                    lastKnownLocation = locMgr.getLastKnownLocation(locMgr
                            .getBestProvider(crit, true));
                } catch (Exception e) {
                    Log.e(TAG, "Location not available. " + e.getMessage());
                }
				if (lastKnownLocation != null) {
					LatLng latlng = new LatLng(lastKnownLocation.getLatitude(),
							lastKnownLocation.getLongitude());
					CameraUpdate cu = CameraUpdateFactory.newLatLng(latlng);
					map.animateCamera(cu);
				}
			}

            Intent serviceIntent = new Intent(this, MapsIntentService.class);
			serviceIntent.putExtra(Extra.MODULE_ID, moduleId);
			serviceIntent.putExtra(Extra.MAPS_CAMPUSES_URL, getIntent().getStringExtra(Extra.MAPS_CAMPUSES_URL));
			startService(serviceIntent);

			ActionBar bar = getSupportActionBar();
			bar.setDisplayShowTitleEnabled(false);
			spinner = (Spinner) findViewById(R.id.toolbar_spinner);

			campusAdapter = new SimpleCursorAdapter(getSupportActionBar().getThemedContext(),
					android.R.layout.simple_spinner_item, null,
					new String[] { MapsCampuses.CAMPUS_NAME },
					new int[] { android.R.id.text1 }, 0);

			campusAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(campusAdapter);  // TODO disable drop down if cursor has 1 item
            spinner.setOnItemSelectedListener(this);

			getLoaderManager().initLoader(0, null, this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
        if (hasLocationPermission()) {
            getLocation();
        }

		if(map != null) {
			map.setLocationSource(this);
			map.setIndoorEnabled(true);
		}
		
	}

	@Override
	protected void onPause() {
		if(map != null) {
			map.setLocationSource(null);
			map.setIndoorEnabled(false);
		}
		if(locMgr != null)
            try {
                locMgr.removeUpdates(this);
            } catch (Exception e) {
                Log.e(TAG, "Location is not available. " + e.getMessage());
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
			lastKnownLocation = location;
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
			if (map != null) {
				map.setOnInfoWindowClickListener(this);
			}
		}
	}

    private void getLocation() {
        if(locMgr != null) {
            try {
                locMgr.requestLocationUpdates(0L, 0.0f, crit, this, null);
            } catch (Exception e) {
                //java.lang.IllegalArgumentException: no providers found for criteria
                Log.e(TAG, "Location is not available" + e.getMessage());
            }
        }
    }

	private boolean readyToGo() {
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

		if (status == ConnectionResult.SUCCESS) {
			return (true);
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(status)) {
			// http://stackoverflow.com/questions/13932474/googleplayservicesutil-geterrordialog-is-null
			//https://code.google.com/p/gmaps-api-issues/issues/detail?id=4720&q=store&colspec=ID%20Type%20Status%20Introduced%20Fixed%20Summary%20Stars%20ApiType%20Internal
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, status, 0);
			if(dialog != null)
	        {
	            dialog.show();                
	        }
	        else
	        {
	        	Toast.makeText(this, getString(R.string.maps_feature_not_supported), Toast.LENGTH_LONG).show();
	        	finish();
	        }
		} else {
			Toast.makeText(this, getString(R.string.maps_feature_not_supported), Toast.LENGTH_LONG).show();
			finish();
		}

		return (false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_maps, menu);

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(
				R.id.maps_action_search).getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
        searchView.setOnSearchClickListener(new SearchView.OnClickListener() {

			@Override
			public void onClick(View v) {
				MapsActivity.this.sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_SEARCH, "Search", null, moduleName);
			}
        	
        });
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
		case R.id.maps_buildings:
			sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_BUTTON_PRESS, "Tap building icon", null, moduleName);
			Intent intent = new Intent(this, BuildingListActivity.class);
			intent.putExtra(Extra.MODULE_ID, moduleId);
			startActivity(intent);
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
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_BUTTON_PRESS, "Tap campus selector", null, moduleName);
		sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, "Select campus", null, moduleName);
		moveToCampus(parent.getSelectedItemPosition());
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	private void moveToCampus(int itemPosition) {
		Cursor cursor = (Cursor) campusAdapter.getItem(itemPosition);
		String campusName = cursor.getString(cursor
				.getColumnIndex(MapsCampuses.CAMPUS_NAME));
		double centerLat = cursor.getDouble(cursor
				.getColumnIndex(MapsCampuses.CAMPUS_CENTER_LATITUDE));
		double centerLng = cursor.getDouble(cursor
				.getColumnIndex(MapsCampuses.CAMPUS_CENTER_LONGITUDE));
		double nwLat = cursor.getDouble(cursor
				.getColumnIndex(MapsCampuses.CAMPUS_NORTHWEST_LATITUDE));
		double nwLng = cursor.getDouble(cursor
				.getColumnIndex(MapsCampuses.CAMPUS_NORTHWEST_LONGITUDE));
		double seLat = cursor.getDouble(cursor
				.getColumnIndex(MapsCampuses.CAMPUS_SOUTHEAST_LATITUDE));
		double seLng = cursor.getDouble(cursor
				.getColumnIndex(MapsCampuses.CAMPUS_SOUTHEAST_LONGITUDE));

		map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(centerLat,
				centerLng)));
		LatLngBounds bounds = new LatLngBounds.Builder()
				.include(new LatLng(nwLat, nwLng))
				.include(new LatLng(seLat, seLng)).build();
		map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));

		Bundle arguments = new Bundle();
		arguments.putString("campusName", campusName);
		getLoaderManager().restartLoader(1, arguments, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case 0:
			return new CursorLoader(this, MapsCampuses.CONTENT_URI, null, Tables.MAPS_CAMPUSES + "." + Modules.MODULES_ID + " = ? ",
					new String[] { this.moduleId }, MapsCampuses.DEFAULT_SORT);
		case 1:
			return new CursorLoader(this, MapsCampuses.buildBuildingsUri(args
					.getString("campusName")), null, null, null,
					MapsBuildings.DEFAULT_SORT);
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		int id = loader.getId();
		switch (id) {
		case 0:
            campusAdapter.swapCursor(data);
			if(data.getCount() > 0) {
				if(!campusesLoadedFirstTime && lastKnownLocation != null) {
					campusesLoadedFirstTime = true;
					
					int selectedCampus = 0;
					float distance = Float.MAX_VALUE;
					for(int i = 0; i < campusAdapter.getCount(); i++) {
						Cursor cursor = (Cursor) campusAdapter.getItem(i);
						double centerLat = cursor.getDouble(cursor
								.getColumnIndex(MapsCampuses.CAMPUS_CENTER_LATITUDE));
						double centerLng = cursor.getDouble(cursor
								.getColumnIndex(MapsCampuses.CAMPUS_CENTER_LONGITUDE));
						Location campusCenter = new Location(lastKnownLocation);
						campusCenter.setLatitude(centerLat);
						campusCenter.setLongitude(centerLng);
						float campusDistance = lastKnownLocation.distanceTo(campusCenter);
						if(campusDistance < distance) {
							selectedCampus = i;
							distance = campusDistance;
						}
					}
                    spinner.setSelection(selectedCampus);
				}
			}
			break;
		case 1:
			Set<Marker> keys = new HashSet<>(markers.keySet());
			for (Marker marker : keys) {
				marker.remove();
				markers.remove(marker);
			}
			if (data.moveToFirst()) {
				do {

					String buildingName = data.getString(data
							.getColumnIndex(MapsBuildings.BUILDING_NAME));
					String campusName = data.getString(data
							.getColumnIndex(MapsCampuses.CAMPUS_NAME));
					String category = data.getString(data
							.getColumnIndex(MapsBuildings.BUILDING_CATEGORIES));//Categories.MAPS_BUILDINGS_CATEGORY_NAMEMAPS_BUILDINGS_CATEGORY_NAME));
					String description = data
							.getString(data
									.getColumnIndex(MapsBuildings.BUILDING_DESCRIPTION));
					String imageUri = data.getString(data
							.getColumnIndex(MapsBuildings.BUILDING_IMAGE_URL));
					String label = data.getString(data
							.getColumnIndex(MapsBuildings.BUILDING_ADDRESS));
					double buildingLat = data.getDouble(data
							.getColumnIndex(MapsBuildings.BUILDING_LATITUDE));
					double buildingLon = data.getDouble(data
							.getColumnIndex(MapsBuildings.BUILDING_LONGITUDE));
					String additionalServices = data
							.getString(data
									.getColumnIndex(MapsBuildings.BUILDING_ADDITIONAL_SERVICES));
					Marker marker = map.addMarker(new MarkerOptions()
							.position(new LatLng(buildingLat, buildingLon))
							.title(buildingName).snippet(category));
					Building building = new Building();
					building.name = buildingName;
					building.campusName = campusName;
					building.type = category;
					building.description = description;
					building.imageUrl = imageUri;
					building.address = label;
					building.latitude = buildingLat;
					building.longitude = buildingLon;
					building.additionalServices = additionalServices;
					markers.put(marker, building);
				} while (data.moveToNext());
			}
			break;
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
        if (spinner != null) {
		savedInstanceState.putInt(STATE_NAV, spinner.getSelectedItemPosition());
	}
    }

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
        if (spinner != null) {
        spinner.setSelection(savedInstanceState.getInt(STATE_NAV));
	}
    }

	@Override
	public void onInfoWindowClick(Marker arg0) {
		sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_BUTTON_PRESS, "Select Map Pin", null, moduleName);
		Building building = markers.get(arg0);
		Intent intent = MapUtils.buildBuildingDetailIntent(this, building.name,
				building.type, building.address, building.description,
				building.phone, building.email, building.imageUrl,
				building.latitude, building.longitude, null, null,  building.campusName, building.additionalServices, building.showName, true);
		startActivity(intent);
	}
	
	@Override
	public void startActivity(Intent intent) {      
	    if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
	        intent.putExtra(Extra.MODULE_ID, moduleId);
	    }
	    super.startActivity(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();
		sendView("Map of campus", moduleName);
	}
}