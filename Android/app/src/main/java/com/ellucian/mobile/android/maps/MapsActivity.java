/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.maps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Dialog;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends EllucianActivity implements
		LayersDialogFragmentListener, OnInfoWindowClickListener,
		OnNavigationListener, LocationSource, LocationListener,
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final String STATE_NAV = "nav";

	private GoogleMap map;
	private OnLocationChangedListener mapLocationListener = null;
	private LocationManager locMgr = null;
	private Criteria crit = new Criteria();
	private SimpleCursorAdapter campusAdapter;
	private HashMap<Marker, Building> markers = new HashMap<Marker, Building>();
	private boolean campusesLoadedFirstTime;
	private Location lastKnownLocation;

    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	handleIntent(getIntent());
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
    	handleIntent(intent);
    }
    
    protected void handleIntent(Intent intent) {
		
		if (readyToGo()) {
			setContentView(R.layout.activity_maps);
			setUpMapIfNeeded();
			
			locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
			crit.setAccuracy(Criteria.ACCURACY_COARSE);

			map.setMyLocationEnabled(true);
			map.getUiSettings().setMyLocationButtonEnabled(true);
			map.setInfoWindowAdapter(new CustomInfoWindowAdapter(getLayoutInflater()));
			
			
			String provider = locMgr.getBestProvider(crit, true);
			if(provider != null) {
				sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, "Geolocate user", null, moduleName);
				lastKnownLocation = locMgr.getLastKnownLocation(locMgr
						.getBestProvider(crit, true));
				if (lastKnownLocation != null) {
					LatLng latlng = new LatLng(lastKnownLocation.getLatitude(),
							lastKnownLocation.getLongitude());
					CameraUpdate cu = CameraUpdateFactory.newLatLng(latlng);
					map.animateCamera(cu);
				}
			}
			
			setProgressBarIndeterminateVisibility(true); 
			Intent serviceIntent = new Intent(this, MapsIntentService.class);
			serviceIntent.putExtra(Extra.MODULE_ID, moduleId);
			serviceIntent.putExtra(Extra.MAPS_CAMPUSES_URL, getIntent().getStringExtra(Extra.MAPS_CAMPUSES_URL));
			startService(serviceIntent);

			campusAdapter = new SimpleCursorAdapter(getActionBar().getThemedContext(),
					android.R.layout.simple_spinner_item, null,
					new String[] { MapsCampuses.CAMPUS_NAME },
					new int[] { android.R.id.text1 }, 0);

			ActionBar bar = getActionBar();

			campusAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			bar.setListNavigationCallbacks(campusAdapter, this);

			getLoaderManager().initLoader(0, null, this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(locMgr != null) {
			try {
				locMgr.requestLocationUpdates(0L, 0.0f, crit, this, null);
			} catch (IllegalArgumentException e) {
				//java.lang.IllegalArgumentException: no providers found for criteria
			}
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
		if(locMgr != null) locMgr.removeUpdates(this);
		
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
			map = ((MapFragment) getFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			if (map != null) {
				map.setOnInfoWindowClickListener(this);
				setUpMap();
			}
		}
	}

	protected boolean readyToGo() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		if (status == ConnectionResult.SUCCESS) {
			return (true);
		} else if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
			// http://stackoverflow.com/questions/13932474/googleplayservicesutil-geterrordialog-is-null
			//https://code.google.com/p/gmaps-api-issues/issues/detail?id=4720&q=store&colspec=ID%20Type%20Status%20Introduced%20Fixed%20Summary%20Stars%20ApiType%20Internal
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, 
				      0);
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

	private void setUpMap() {

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
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_BUTTON_PRESS, "Tap campus selector", null, moduleName);
		sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, "Select campus", null, moduleName);
		moveToCampus(itemPosition);
		return true;
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
			setProgressBarIndeterminateVisibility(false); 
			campusAdapter.swapCursor(data);
			if(data.getCount() > 0) {
				if(campusesLoadedFirstTime == false && lastKnownLocation != null) {
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
					getActionBar().setSelectedNavigationItem(selectedCampus);
				}
			}
			break;
		case 1:
			Set<Marker> keys = new HashSet<Marker>(markers.keySet());
			for (Marker marker : keys) {
				marker.remove();
				markers.remove(marker);
			}
			if (data.moveToFirst()) {
				Marker lastMarker = null;
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
					lastMarker = marker;
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

		savedInstanceState.putInt(STATE_NAV, getActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		getActionBar().setSelectedNavigationItem(
				savedInstanceState.getInt(STATE_NAV));
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