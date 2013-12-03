package com.ellucian.mobile.android.map;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.DataCache;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.UICustomizer;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MapsActivity extends MapActivity {

	public class EllucianItemizedOverlay extends ItemizedOverlay<PinOverlay> {

		private final ArrayList<PinOverlay> mOverlays = new ArrayList<PinOverlay>();

		public EllucianItemizedOverlay(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
		}

		public void addOverlay(PinOverlay overlay) {
			mOverlays.add(overlay);
			populate();
		}

		@Override
		protected PinOverlay createItem(int i) {
			return mOverlays.get(i);
		}

		@Override
		protected boolean onTap(int index) {
			final PinOverlay item = mOverlays.get(index);
			final AlertDialog.Builder dialog = new AlertDialog.Builder(
					MapsActivity.this);
			dialog.setMessage(item.getTitle())
					.setNegativeButton(
							getResources().getString(R.string.getDirections),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

									startActivity(buildDirectionsIntent(item
											.getPoint()));
								}
							})
					.setPositiveButton(
							getResources().getString(R.string.showDetails),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

									startActivity(buildDetailIntent(
											item.getBuilding(), item.getType()));
								}
							});

			dialog.show();
			return true;
		}

		@Override
		public int size() {
			return mOverlays.size();
		}

	}

	public class PinOverlay extends OverlayItem {

		private final Building building;

		private final String type;

		public PinOverlay(GeoPoint point, String title, String snippet,
				Building building, String type) {
			super(point, title, snippet);
			this.building = building;
			this.type = type;
		}

		public Building getBuilding() {
			return building;
		}

		public String getType() {
			return type;

		}
	}
/*
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			refreshInProgress = false;
			UICustomizer.setProgressBarVisible(MapsActivity.this, false);

			drawMap();
		}
	};*/
	
	private final Handler handler = new IncomingHandler(this);
	
	static class IncomingHandler extends Handler {
	    private final WeakReference<MapsActivity> mActivity; 

	    IncomingHandler(MapsActivity activity) {
	    	mActivity = new WeakReference<MapsActivity>(activity);
	    }
	    @Override
	    public void handleMessage(Message msg)
	    {
	    	super.handleMessage(msg);
			mActivity.get().refreshInProgress = false;
			UICustomizer.setProgressBarVisible(mActivity.get(), false);

			mActivity.get().drawMap();
	    }
	}

	private EllucianItemizedOverlay itemizedOverlay;
	private String jsonMap;
	// Define a listener that responds to location updates
	LocationListener locationListener = null;
	private LocationManager manager = null;
	private CampusMapCollection map;

	private MapView mapView;

	private MyLocationOverlay myLocationOverlay;

	boolean refreshInProgress;

	private GeoPoint restoreCenter;
	private int restoreZoom;
	private String selectedCampus;
	private boolean showMyLocation;

	private String url;

	private static final int CAMPUS_SELECTION_RESULT = RESULT_FIRST_USER;
	
	private void alertCampusSelection() {

		final Intent intent = new Intent(MapsActivity.this,
				CampusSelectionActivity.class);
		intent.putExtra("campuses", map.getCampusNames());
		startActivityForResult(intent, CAMPUS_SELECTION_RESULT);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMPUS_SELECTION_RESULT) {
			if (resultCode == RESULT_OK) {
				final CharSequence[] items = map.getCampusNames();
				selectedCampus = items[data.getIntExtra("position", 0)].toString();
				
				final DataCache cache = ((EllucianApplication) getApplication())
						.getDataCache();

				cache.putCache("campus" + url, selectedCampus,
						selectedCampus);

				showMyLocation = false;
				drawMap();
			} else if (resultCode == RESULT_CANCELED) {
				if(selectedCampus == null) {
					finish();
				}
			}
		}
	}

	public Intent buildDetailIntent(Building building, String type) {

		final Intent intent = new Intent(MapsActivity.this,
				BuildingDetailActivity.class);

		intent.putExtra("name", building.getName());
		intent.putExtra("label", building.getLabel());
		intent.putExtra("description", building.getDescription());
		intent.putExtra("latitude",
				building.getGeoPoint().getLatitudeE6() / 1000000d);
		intent.putExtra("longitude",
				building.getGeoPoint().getLongitudeE6() / 1000000d);
		intent.putExtra("imageUrl", building.getImageUrl());
		intent.putExtra("type", type);

		return intent;

	}

	private Intent buildDirectionsIntent(GeoPoint geoPoint) {

		final Intent intent = new Intent(android.content.Intent.ACTION_VIEW,

		Uri.parse("http://maps.google.com/maps?saddr=" + "&daddr="
				+ geoPoint.getLatitudeE6() / 1000000d + ","
				+ geoPoint.getLongitudeE6() / 1000000d));
		return intent;
	}

	private void doShowMyLocation() {
		showMyLocation = true;
		restoreCenter = mapView.getMapCenter();
		restoreZoom = mapView.getZoomLevel();
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.runOnFirstFix(new Runnable() {

			public void run() {
				setMyLocation();
			}

		});
		
	
	}

	void drawMap() {

		CampusMap campusMap = null;
		if(map == null || map.getMaps().size() == 0) {
			Toast.makeText(this, getResources().getString(R.string.noLocations), Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		if (map.hasOnlyOneMap()) {
			campusMap = map.getSingleMap();
			selectedCampus = campusMap.getName();
		} else {
			campusMap = map.getCampus(selectedCampus);
		}

		if (campusMap == null) {
			alertCampusSelection();
		} else {

			final TextView title = (TextView) findViewById(R.id.titlebar_title);
			title.setText(campusMap.getName());

			mapView = (MapView) findViewById(R.id.mapview);
			mapView.setBuiltInZoomControls(true);
			mapView.getController().setCenter(campusMap.getCenter());
			mapView.getController().zoomToSpan(campusMap.getLatitudeSpan(),
					campusMap.getLongitudeSpan());

			mapView.getOverlays().clear();

			myLocationOverlay = new MyLocationOverlay(this, mapView);
			mapView.getOverlays().add(myLocationOverlay);

			final List<Overlay> mapOverlays = mapView.getOverlays();
			final Drawable drawable = this.getResources().getDrawable(
					R.drawable.pushpin);
			itemizedOverlay = new EllucianItemizedOverlay(drawable);

			for (final BuildingCategory bc : campusMap.getCategories()) {
				for (final Building b : bc.getBuildings()) {

					final PinOverlay overlayitem = new PinOverlay(
							b.getGeoPoint(), b.getName(), "", b, bc.getType());
					itemizedOverlay.addOverlay(overlayitem);
				}
			}

			mapOverlays.add(itemizedOverlay);
			mapView.postInvalidate();
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_pin);

		final String activityTitle = getIntent().getStringExtra("title");
		setTitle(activityTitle);
		UICustomizer.style(this);		

		url = getIntent().getStringExtra("url");
		final DataCache cache = ((EllucianApplication) getApplication())
				.getDataCache();

		selectedCampus = cache.getCache(this, "campus" + url);

		final boolean current = cache.isCurrentLongInterval(this, url);

		final String cachedContent = cache.getCache(this, url);

		manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				Log.d(EllucianApplication.TAG, "location changed");
			}

			public void onProviderDisabled(String provider) {
				Log.d(EllucianApplication.TAG, provider + " disabled");
			}

			public void onProviderEnabled(String provider) {
				Log.d(EllucianApplication.TAG, provider + " enabled");
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {
				 switch (status) {
				    case LocationProvider.TEMPORARILY_UNAVAILABLE:
				    	Log.d(EllucianApplication.TAG, "Your location is temporarily unavailable");
				        break;
				    case LocationProvider.OUT_OF_SERVICE:
				    	Log.d(EllucianApplication.TAG, "Your location is now unavailable");
				        break;
				    case LocationProvider.AVAILABLE:
				    	Log.d(EllucianApplication.TAG, "Your location is now available");
				    }
			}
		};

		//JKh 11/29/11 http://code.google.com/p/android/issues/detail?id=19857 
		//suggested fix http://code.google.com/p/osmdroid/issues/detail?id=167
		//manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		//manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		for (final String provider : manager.getProviders(true)) {
				if (LocationManager.GPS_PROVIDER.equals(provider)
						|| LocationManager.NETWORK_PROVIDER.equals(provider)) {
					manager.requestLocationUpdates(provider,
							0, 0, locationListener);
				}
			}

		if (cachedContent != null) {
			try {
				jsonMap = cachedContent;
				final Object o = cache.getCacheObject(url);
				if (o != null && o instanceof List) {
					map = (CampusMapCollection) o;
				} else {
					map = CampusMapsParser.parse(jsonMap);
					cache.putCacheObject(url, map);
				}

				if (!current) {
					update();
				} else {
					drawMap();
				}
			} catch (final JSONException e) {
				Log.e(EllucianApplication.TAG, "Can't parse json in events");
			}
		} else {
			update();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.maps, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_my_location:
			doShowMyLocation();

			break;
		case R.id.menu_restore_default_location:
			showMyLocation = false;
			myLocationOverlay.disableMyLocation();
			mapView.getController().setCenter(restoreCenter);
			mapView.getController().setZoom(restoreZoom);
			break;
		case R.id.menu_choose_campus:
			alertCampusSelection();
			break;
		case R.id.menu_search:
			onSearchRequested();
			return true;
		case R.id.menu_building_list:
			final Intent intent = new Intent(MapsActivity.this,
					BuildingExpandableListActivity.class);
			intent.putExtra("campus", selectedCampus);
			intent.putExtra("url", url);
			startActivity(intent);
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

	@Override
	protected void onPause() {
		super.onPause();
		if(myLocationOverlay != null) {
			myLocationOverlay.disableMyLocation();
		}
		manager.removeUpdates(locationListener);

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(map == null) return false;
		menu.findItem(R.id.menu_choose_campus).setVisible(!map.hasOnlyOneMap());
		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			menu.findItem(R.id.menu_my_location).setVisible(true);
			menu.findItem(R.id.menu_my_location).setEnabled(false);
			menu.findItem(R.id.menu_restore_default_location).setVisible(false);

		} else {
			menu.findItem(R.id.menu_my_location).setEnabled(true);
			menu.findItem(R.id.menu_my_location).setVisible(!showMyLocation);
			menu.findItem(R.id.menu_restore_default_location).setVisible(
					showMyLocation);
		}

		menu.findItem(R.id.menu_map_satellite).setVisible(mapView != null && 
				!mapView.isSatellite());
		menu.findItem(R.id.menu_map).setVisible(mapView != null && mapView.isSatellite());
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onResume() {
		super.onResume();

		final Object data = getLastNonConfigurationInstance();

		if (data != null) {
			if ((Boolean) data) {
				doShowMyLocation();
			}

		}
		// if (showMyLocation) {
		// myLocationOverlay.enableMyLocation();
		// }
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return showMyLocation;
	}

	@Override
	public boolean onSearchRequested() {
		final Bundle appData = new Bundle();
		appData.putString("campus", selectedCampus);
		appData.putString("url", url);
		startSearch(null, false, appData, false);
		return true;
	}

	private void update() {
		UICustomizer.setProgressBarVisible(MapsActivity.this, true);
		refreshInProgress = true;

		new Thread() {
			@Override
			public void run() {
				try {
					refreshInProgress = true;
					final HttpClient client = new DefaultHttpClient();
					final HttpGet request = new HttpGet();
					request.setURI(new URI(url));

					final HttpResponse response = client.execute(request);

					final int status = response.getStatusLine().getStatusCode();
					if (status == HttpStatus.SC_OK) {
						final BufferedReader in = new BufferedReader(
								new InputStreamReader(response.getEntity()
										.getContent(), "UTF-8"));
						final StringBuffer sb = new StringBuffer();
						String line = "";
						final String NL = System.getProperty("line.separator");
						while ((line = in.readLine()) != null) {
							sb.append(line + NL);
						}
						in.close();

						jsonMap = sb.toString();
						map = CampusMapsParser.parse(jsonMap);

						((EllucianApplication) getApplication()).getDataCache()
								.putCache(url, jsonMap, map);

					} else {
						throw new RuntimeException(response.getStatusLine()
								.toString());
					}

				} catch (final Exception e) {
					Log.e(EllucianApplication.TAG, "Maps update failed = " + e);
				}
				handler.sendEmptyMessage(0);
			}
		}.start();

	}

	private void setMyLocation() {
		final GeoPoint myLocation = myLocationOverlay.getMyLocation();

		final GeoPoint overlayCenter = itemizedOverlay.getCenter();

		int currentBottomLat = 0;
		int currentTopLat = 0;
		int currentCenterLat = overlayCenter.getLatitudeE6();
		int currentSpanLat = itemizedOverlay.getLatSpanE6();
		final int spanHalfLat = itemizedOverlay.getLatSpanE6() / 2;

		if (overlayCenter.getLatitudeE6() > 0) {
			currentBottomLat = itemizedOverlay.getCenter()
					.getLatitudeE6() - spanHalfLat;
			currentTopLat = itemizedOverlay.getCenter().getLatitudeE6()
					+ spanHalfLat;
			if (myLocation.getLatitudeE6() < currentTopLat
					&& myLocation.getLatitudeE6() > currentBottomLat) {
				// fall through;
			} else if (myLocation.getLatitudeE6() < currentTopLat) {
				currentBottomLat = myLocation.getLatitudeE6();
				currentCenterLat = (currentTopLat + myLocation
						.getLatitudeE6()) / 2;
			} else if (myLocation.getLatitudeE6() > currentBottomLat) {
				currentTopLat = myLocation.getLatitudeE6();
				currentCenterLat = (currentBottomLat + myLocation
						.getLatitudeE6()) / 2;
			}
			currentSpanLat = Math.abs(currentTopLat - currentBottomLat);
		}

		int currentLeftLon = 0;
		int currentRightLon = 0;
		int currentCenterLon = overlayCenter.getLongitudeE6();
		int currentSpanLon = itemizedOverlay.getLonSpanE6();
		final int spanHalfLon = itemizedOverlay.getLonSpanE6() / 2;

		if (overlayCenter.getLongitudeE6() < 0) {
			currentLeftLon = itemizedOverlay.getCenter()
					.getLongitudeE6() - spanHalfLon;
			currentRightLon = itemizedOverlay.getCenter()
					.getLongitudeE6() + spanHalfLon;
			if (myLocation.getLongitudeE6() < currentRightLon
					&& myLocation.getLongitudeE6() > currentLeftLon) {
				// fall through;
			} else if (myLocation.getLongitudeE6() < currentRightLon) {
				currentLeftLon = myLocation.getLongitudeE6();
				currentCenterLon = (currentRightLon + myLocation
						.getLongitudeE6()) / 2;
			} else if (myLocation.getLongitudeE6() > currentLeftLon) {
				currentRightLon = myLocation.getLongitudeE6();
				currentCenterLon = (currentLeftLon + myLocation
						.getLongitudeE6()) / 2;
			}
			currentSpanLon = Math.abs(currentLeftLon - currentRightLon);
		}

		mapView.getController().animateTo(
				new GeoPoint(currentCenterLat, currentCenterLon));
		mapView.getController().zoomToSpan(currentSpanLat,
				currentSpanLon);
	}
}