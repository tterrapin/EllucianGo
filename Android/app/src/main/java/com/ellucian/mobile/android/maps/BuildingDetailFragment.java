/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.maps;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.client.maps.BuildingsBuilder;
import com.ellucian.mobile.android.client.maps.BuildingsResponse;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.provider.EllucianContract.MapsBuildings;
import com.ellucian.mobile.android.provider.EllucianContract.MapsCampuses;
import com.ellucian.mobile.android.util.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class BuildingDetailFragment extends EllucianDefaultDetailFragment {
	private static final String TAG = BuildingDetailFragment.class.getSimpleName();
	
	public static final String ARG_ADDRESS = "com.address";
	public static final String ARG_DESCRIPTION = "description";
	public static final String ARG_EMAIL = "email";
	public static final String ARG_IMAGE_URL = "imageUrl";
	public static final String ARG_LATITUDE = "latitude";
	public static final String ARG_LONGITUDE = "longitude";
	public static final String ARG_BUILDING_ID = "buildingId";
	public static final String ARG_CAMPUS_ID = "campusId";
	public static final String ARG_CAMPUS_NAME = "campusName";
	public static final String ARG_NAME = "name";
	public static final String ARG_PHONE = "phone";
	public static final String ARG_TYPE = "type";
	public static final String ARG_ADDITIONAL_SERVICES = "additionalServices";
	public static final String ARG_SHOW_NAME = "showName";
	public static final String ARG_SHOW_BUILDING_INFO = "showBuildingInfo";
	
	private Activity activity;
	private View rootView;
	private AQuery aQuery;
	
	private String name;
	private String address;
	private String description;
	private String additionalServices;
	private String imageUrl;
	private Double latitude = null;
	private Double longitude = null;
	private String buildingId;
	private String campusId;
	private String campusName;
	private String type;

	private String buildingUrl;
	@SuppressWarnings("unused")
	private boolean showDirections = false;
	@SuppressWarnings("unused")
	private boolean showMap = false;


	public BuildingDetailFragment() {
		// void constructor
	}


	@Override 
	public void onAttach(Context context) {
		Log.d(TAG, "onAttach");
		super.onAttach(context);
		activity = getActivity();
	}


	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
		sendView("Building Detail", getEllucianActivity().moduleName);
	}


	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView");
		if (aQuery != null) {
			aQuery.ajaxCancel();
		}
		super.onDestroyView();
	}


	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		aQuery = null;
		super.onDestroy();
	}


	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container,
							 Bundle bundle) {
		Log.d(TAG, "onCreateView");
		if (container == null) {
			return null;
		}
		rootView = inflater.inflate(R.layout.fragment_building_detail, container, false);
		return rootView;
	}


	@Override
	public void onActivityCreated(Bundle bundle) {
		super.onActivityCreated(bundle);
		Log.d(TAG, "onActivityCreated");
		
		if (aQuery == null) {
			aQuery = new AQuery(rootView);
		}
		
		Bundle args = getArguments();
		name = null;
		address = null;
		latitude = null;
		longitude = null;
		buildingId = null;
		campusId = null;
		type = null;
		showDirections = false;
		showMap = false;
		if (args != null) {
			setFields(args);
			if ( latitude == null || longitude == null || (latitude == 0.0 && longitude == 0.0)) {
				Log.d(TAG, "latitude or longitude is missing, setting views from buildingId");
				if (!TextUtils.isEmpty(buildingId)) {
					buildingUrl = Utils.getStringFromPreferences(getActivity(), Utils.CONFIGURATION, Utils.MAP_BUILDINGS_URL, null);
					setViewsFromBuildingId();
				} else {
					Log.d(TAG, "buildingId is also missing showing data from args only");
					setViews();
				}
			} else {
				setViews();
			}
		} else {
			Log.d(TAG, "args null, no setFields, no setViews");
		}
	}


	/*
	 * set the fragment field values from the arguments received
	 */
	private void setFields(Bundle arguments) {
		Log.d(TAG, "setFields");
		if (arguments != null) {
			name = arguments.getString(ARG_NAME);
			address = arguments.getString(ARG_ADDRESS);
			description = arguments.getString(ARG_DESCRIPTION);
			imageUrl = arguments.getString(ARG_IMAGE_URL);
			if (arguments.containsKey(ARG_LATITUDE)) {
				latitude = arguments.getDouble(ARG_LATITUDE, 0);
			}
			if (arguments.containsKey(ARG_LONGITUDE)) {
				longitude = arguments.getDouble(ARG_LONGITUDE, 0);
			}
			buildingId = arguments.getString(ARG_BUILDING_ID);
			campusId = arguments.getString(ARG_CAMPUS_ID);
			campusName = arguments.getString(ARG_CAMPUS_NAME);
			type = arguments.getString(ARG_TYPE);
			additionalServices = arguments.getString(ARG_ADDITIONAL_SERVICES);
		} else {
			Log.d(TAG, "Bundle null, no fields set." );
		}
	}


	/*
	 * set the fragment fields from a building
	 */
	private void setFieldsFromClientBuilding(com.ellucian.mobile.android.client.maps.Building clientBuilding) {
		Log.d(TAG, "setFieldsFromClientBuilding");
		if (clientBuilding != null) {
			if (clientBuilding.address != null) {
				address = clientBuilding.address.replace("\\n", "\n");
			}
			description = clientBuilding.longDescription;
			imageUrl = clientBuilding.imageUrl;
			longitude = (double) clientBuilding.longitude;
			latitude = (double) clientBuilding.latitude;
			buildingId = clientBuilding.id;
			campusId = clientBuilding.campusId;
			
			// Don't overwrite type if present
			if (TextUtils.isEmpty(type)) {
				String typeString = "";
				for (String type : clientBuilding.type) {			
						if (!TextUtils.isEmpty(typeString)) {
							typeString += ",";
						}					
						typeString += type;
				}
				type = typeString;
			}
			additionalServices = clientBuilding.additionalServices;
		} else {
			Log.d(TAG, "Client Building null, no fields set." );
		}	
	}


	/*
	 * Set the values for the layout views
	 */
	 private void setViews() {
		Log.d(TAG, "setViews");
		if (rootView != null) {
			if (!TextUtils.isEmpty(imageUrl)) {
				// Must use mem and file cache in order for the downsampling to work. 
				// Larger images will not show without it. 
				View v = rootView.findViewById(R.id.image);
				if (v != null) {
					Log.d(TAG, "fetching image");
					aQuery.id(R.id.image).image(imageUrl, true, true, 300, 0).visible();
				}
			}

			View detailsFrame = getActivity().findViewById(R.id.frame_extra);
			boolean dualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

			if ((!TextUtils.isEmpty(name)) && dualPane) {
				setBasicView(R.id.nameLayout, R.id.name, name);
			} 

			if (!TextUtils.isEmpty(campusName)) {
				setBasicView(R.id.campusLayout, R.id.campus, campusName);
			}

			if (!TextUtils.isEmpty(address)) {
				setLinkifiedView(R.id.addressLayout, R.id.address, Linkify.MAP_ADDRESSES, address);
			}

			if (!TextUtils.isEmpty(description)) {
				setLinkifiedView(R.id.descriptionLayout, R.id.description, Linkify.ALL, description);
			}

			if (!TextUtils.isEmpty(additionalServices)) {
				setLinkifiedView(R.id.additionalServicesLayout, R.id.additionalServices, Linkify.ALL, additionalServices);
			}

			if (!TextUtils.isEmpty(type)) {
				setBasicView(R.id.typeLayout, R.id.type, type);
			}

			if (latitude != null && longitude != null && !(latitude == 0.0 && longitude == 0.0) 
					&& Utils.isGoogleMapsInstalled(activity)) {
				showMap = true;
				Button mapButton = (Button) rootView.findViewById(R.id.showMap);
				mapButton.setVisibility(View.VISIBLE);
				mapButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View arg0) {
						startActivity(MapUtils.buildMapPinIntent(getActivity(),
								name, latitude, longitude));
					}
				});

				showDirections = true;
				Button directionsButton = (Button) rootView.findViewById(R.id.showDirections);
				directionsButton.setVisibility(View.VISIBLE);
				directionsButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						BuildingDetailFragment.this.sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, "Get Directions", null, getEllucianActivity().moduleName);
						startActivity(MapUtils.buildDirectionsIntent(latitude,
								longitude));
					}
				});

			} else if (!TextUtils.isEmpty(address)) {
				showMap = false;
				showDirections = true;
				Button directionsButton = (Button) rootView.findViewById(R.id.showDirections);
				directionsButton.setVisibility(View.VISIBLE);
				directionsButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						BuildingDetailFragment.this.sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, "Get Directions", null, getEllucianActivity().moduleName);
						startActivity(MapUtils.buildDirectionsIntent(address));
					}
				});
			} else {
				showMap = false;
				showDirections = false;
			}
		} else {
			Log.d(TAG, "rootView null, no work to do");
		}
	}


	// set a basic view - find the layout, make it visible, set the string
	private void setBasicView(int layoutId, int tvId, String value) {
		View v = rootView.findViewById(layoutId);
		if (v != null) {
			v.setVisibility(View.VISIBLE);
			TextView tv = ((TextView) rootView.findViewById(tvId));
			if (tv != null) {
				tv.setText(value);
			}
		}
	}


	// set a linkified view - find the layout, make it visible, set the link mask, set the string
	private void setLinkifiedView(int layoutId, int tvId, int linkMask, String value) {
		View v = rootView.findViewById(layoutId);
		if (v != null) {
			v.setVisibility(View.VISIBLE);
			TextView tv = (TextView) rootView.findViewById(tvId);
			if (tv != null) {
				tv.setAutoLinkMask(Utils.getAvailableLinkMasks(activity, linkMask));
				tv.setText(value);
			}
		}
	}


	private void setViewsFromBuildingId() {
		Log.d(TAG, "setViewsFromBuildingId");
		
		if (!TextUtils.isEmpty(buildingId)) {
			Log.d(TAG, "Searching for building with a buildingId of " + buildingId);
		
			String selection = MapsBuildings.BUILDING_BUILDING_ID + " = ?";
			String[] selectionArgs = new String[] { buildingId };
			
			if (!TextUtils.isEmpty(campusId)) {
			 	selection += " AND " + MapsCampuses.CAMPUS_ID + " = ?";
			 	selectionArgs = new String[] { buildingId, campusId };
			}

			Cursor cursor = getActivity()
					           .getContentResolver()
					           .query(MapsBuildings.CONTENT_URI, 
									  null, 
									  selection, 
									  selectionArgs, 
									  null);
			
			if (cursor.moveToFirst()) {
				Log.d(TAG, "Cursor returned a row");
				// Don't overwrite name if present
				if (TextUtils.isEmpty(name)) {
					name = cursor.getString(cursor.getColumnIndex(MapsBuildings.BUILDING_NAME));
				}
				
				// Don't overwrite type if present
				if (TextUtils.isEmpty(type)) {
					type = cursor.getString(cursor.getColumnIndex(MapsBuildings.BUILDING_CATEGORIES));
				}
				address = cursor.getString(cursor.getColumnIndex(MapsBuildings.BUILDING_ADDRESS));
				description = cursor.getString(cursor.getColumnIndex(MapsBuildings.BUILDING_DESCRIPTION));
				additionalServices = cursor.getString(cursor.getColumnIndex(MapsBuildings.BUILDING_ADDITIONAL_SERVICES));
				latitude = (double) cursor.getFloat(cursor.getColumnIndex(MapsBuildings.BUILDING_LATITUDE));
				longitude = (double) cursor.getFloat(cursor.getColumnIndex(MapsBuildings.BUILDING_LONGITUDE));
				buildingId = cursor.getString(cursor.getColumnIndex(MapsBuildings.BUILDING_BUILDING_ID));
				campusId = cursor.getString(cursor.getColumnIndex(MapsCampuses.CAMPUS_ID));
				campusName = cursor.getString(cursor.getColumnIndex(MapsCampuses.CAMPUS_NAME));
				imageUrl = cursor.getString(cursor.getColumnIndex(MapsBuildings.BUILDING_IMAGE_URL));
				cursor.close();
				setViews();
				refresh();				
			} else {
				cursor.close();
				Log.d(TAG, "No building found in the database");
				if (!TextUtils.isEmpty(buildingUrl) && !TextUtils.isEmpty(buildingId)) {
					Log.d(TAG, "Requesting building info from server");
					try {
						String modifiedUrl = buildingUrl + "/" + URLEncoder.encode(buildingId, "UTF-8");
						RetrieveBuildingInfoTask dailyTask = new RetrieveBuildingInfoTask(getActivity());
						dailyTask.execute(modifiedUrl);
					} catch (UnsupportedEncodingException e) {
					}
				} else {
					Log.d(TAG, "Cannot perform request missing buildingUrl.");
					Log.d(TAG, "buildingUrl: " + buildingUrl);
				}
			}
		} else {
			Log.d(TAG, "Missing buildingId");
		}
	}


	private void refresh() {
		rootView.invalidate();
	}


	private class RetrieveBuildingInfoTask extends AsyncTask<String, Void, BuildingsResponse> {
		final Activity activity;
		
		public RetrieveBuildingInfoTask(Activity activity) {
			this.activity = activity;
		}

		@Override
		protected BuildingsResponse doInBackground(String... params) {
			String requestUrl = params[0];
			MobileClient client = new MobileClient(activity);
			BuildingsResponse buildingsResponse = client.getBuildings(requestUrl);
			return buildingsResponse;
			
		}
		
		@Override
		protected void onPostExecute(BuildingsResponse buildingsResponse) {
			if (buildingsResponse != null) {
				insertIntoDatabase(buildingsResponse);
				if (buildingsResponse.buildings != null && buildingsResponse.buildings.length > 0) {
					com.ellucian.mobile.android.client.maps.Building clientBuilding = buildingsResponse.buildings[0];
					setFieldsFromClientBuilding(clientBuilding);
				} else {
					Log.d(TAG, "buildingResponse is null or length of 0");
				}
			} else {
				Log.d(TAG, "AsyncTask returned null response");
			}
			setViews();
			refresh();
		}
		
		void insertIntoDatabase(BuildingsResponse response) {
			BuildingsBuilder builder = new BuildingsBuilder(activity, null);
			Log.d(TAG, "Building content provider operations");
			ArrayList<ContentProviderOperation> operations = builder.buildOperations(response);
			Log.d(TAG, "Created " + operations.size() + " operations");
			try {
				activity.getContentResolver().applyBatch(
						EllucianContract.CONTENT_AUTHORITY, operations);
			} catch (RemoteException e) {
				Log.e(TAG, "RemoteException applying batch" + e.getLocalizedMessage());
			} catch (OperationApplicationException e) {
				Log.e(TAG, "OperationApplicationException applying batch:" + e.getLocalizedMessage());
			}
		}
	}

}
