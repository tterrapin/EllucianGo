/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.numbers;

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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.client.maps.BuildingsBuilder;
import com.ellucian.mobile.android.client.maps.BuildingsResponse;
import com.ellucian.mobile.android.maps.MapUtils;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.provider.EllucianContract.MapsBuildings;
import com.ellucian.mobile.android.provider.EllucianContract.MapsCampuses;
import com.ellucian.mobile.android.util.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class NumbersDetailFragment extends EllucianDefaultDetailFragment {
	private static final String TAG = NumbersDetailFragment.class.getSimpleName();

    private Context context;
	private View rootView;
	private String name;
	private String phone;
    private String extension;
	private String address;
	private String email;
	private Double latitude = null;
	private Double longitude = null;
	private String buildingId;
	private String buildingName;
	private String campusId;
	private String type;
	private String buildingUrl;

	public NumbersDetailFragment() {
	}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
							 ViewGroup container, 
							 Bundle bundle) {
		if (container == null) {
			return null;
		}
		
		Log.d(TAG, "onCreateView");
		rootView = inflater.inflate(R.layout.fragment_number_detail,  container, false);
		Bundle args = getArguments();
		name = null;
		phone = null;
        extension = null;
		address = null;
		email = null;
		latitude = null;
		longitude = null;
		buildingId = null;
		buildingName = null;
		campusId = null;
		type = null;
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
		}
		return rootView;
	}
	
	private void setFields(Bundle args) {
		Log.d(TAG, "setFields");
		if (args != null) {
			name = args.getString("name");
			address = args.getString("address");
			email = args.getString("email");
			phone = args.getString("phone");
            extension = args.getString("extension");
			if (args.containsKey("latitude")) {
				latitude = args.getDouble("latitude", 0);
			}
			if (args.containsKey("longitude")) {
				longitude = args.getDouble("longitude", 0);
			}
			buildingId = args.getString("buildingId");
			campusId = args.getString("campusId");
			type = args.getString("type");
		} else {
			Log.d(TAG, "Bundle null, no fields set." );
		}
	}

	private void setFieldsFromClientBuilding(com.ellucian.mobile.android.client.maps.Building clientBuilding) {
		Log.d(TAG, "setFieldsFromClientBuilding");
		if (clientBuilding != null) {
			
			buildingName = clientBuilding.name;
			
			if (clientBuilding.address != null) {
				address = clientBuilding.address.replace("\\n", "\n");
			}
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
		} else {
			Log.d(TAG, "Client Building null, no fields set." );
		}	
	}

	private void setViews() {
        final int primaryColor = Utils.getPrimaryColor(getContext());
		Log.d(TAG, "setViews");
		
		if (!TextUtils.isEmpty(name)) {
			((TextView) rootView.findViewById(R.id.name)).setText(name);
		} 
		
		if (!TextUtils.isEmpty(type)) {
			rootView.findViewById(R.id.type).setVisibility(View.VISIBLE);
			((TextView) rootView.findViewById(R.id.type)).setText(type);
		}

		if (phone != null) {
			rootView.findViewById(R.id.phoneLayout).setVisibility(View.VISIBLE);
            ((ImageView)rootView.findViewById(R.id.numbers_phone_image)).setColorFilter(primaryColor);
			TextView phoneView = (TextView) rootView.findViewById(R.id.phone);
			phoneView.setAutoLinkMask(Utils.getAvailableLinkMasks(context, Linkify.PHONE_NUMBERS));
            if (extension != null) {
                phoneView.setText(getString(R.string.default_phone_with_extension_format,phone,extension));
            } else {
                phoneView.setText(phone);
            }
			phoneView.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View viewIn) {
	                NumbersDetailFragment.this.sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, "Call Phone Number", null, getEllucianActivity().moduleName);
	            }
	        });
		}

		if (!TextUtils.isEmpty(email)) {
			rootView.findViewById(R.id.emailLayout).setVisibility(View.VISIBLE);
            ((ImageView)rootView.findViewById(R.id.numbers_email_image)).setColorFilter(primaryColor);
			TextView emailView = (TextView) rootView.findViewById(R.id.email);
			emailView.setAutoLinkMask(Utils.getAvailableLinkMasks(context, Linkify.EMAIL_ADDRESSES));
			emailView.setText(email);
		}

		String fullAddressString = "";
		if (!TextUtils.isEmpty(buildingName)) {
			fullAddressString += buildingName + "\n";
		}
		if (!TextUtils.isEmpty(address)) {
			fullAddressString += address;	
		}
		if (!TextUtils.isEmpty(fullAddressString)) {
			rootView.findViewById(R.id.addressLayout).setVisibility(View.VISIBLE);
            ((ImageView)rootView.findViewById(R.id.numbers_location_image)).setColorFilter(primaryColor);
            TextView addressView = (TextView) rootView.findViewById(R.id.address);
			addressView.setAutoLinkMask(Utils.getAvailableLinkMasks(context, Linkify.MAP_ADDRESSES));
			addressView.setText(fullAddressString);
		}
		
		if (latitude != null && longitude != null && !(latitude == 0.0 && longitude == 0.0) 
				&& Utils.isGoogleMapsInstalled(context)) {
			ImageView mapButton = (ImageView) rootView.findViewById(R.id.numbers_location_image);
			mapButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0) {
					startActivity(MapUtils.buildMapPinIntent(getActivity(),
							name, latitude, longitude));
				}
			});

			View directionsRow = rootView.findViewById(R.id.directionsLayout);
            directionsRow.setVisibility(View.VISIBLE);
            ((ImageView)rootView.findViewById(R.id.numbers_directions_image)).setColorFilter(primaryColor);
            directionsRow.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    NumbersDetailFragment.this.sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, "Get Directions", null, getEllucianActivity().moduleName);
                    startActivity(MapUtils.buildDirectionsIntent(latitude,
                            longitude));
                }
            });

		} else if (!TextUtils.isEmpty(address)) {
            View directionsRow = rootView.findViewById(R.id.directionsLayout);
            ((ImageView)rootView.findViewById(R.id.numbers_directions_image)).setColorFilter(primaryColor);
            directionsRow.setVisibility(View.VISIBLE);
            directionsRow.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					NumbersDetailFragment.this.sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, "Get Directions", null, getEllucianActivity().moduleName);
					startActivity(MapUtils.buildDirectionsIntent(address));
				}
			});
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

			Cursor cursor = getActivity().getContentResolver().query(MapsBuildings.CONTENT_URI, 
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
				buildingName = cursor.getString(cursor.getColumnIndex(MapsBuildings.BUILDING_NAME));
				
				// Don't overwrite type if present
				if (TextUtils.isEmpty(type)) {
					type = cursor.getString(cursor.getColumnIndex(MapsBuildings.BUILDING_CATEGORIES));
				}
				address = cursor.getString(cursor.getColumnIndex(MapsBuildings.BUILDING_ADDRESS));
				latitude = (double) cursor.getFloat(cursor.getColumnIndex(MapsBuildings.BUILDING_LATITUDE));
				longitude = (double) cursor.getFloat(cursor.getColumnIndex(MapsBuildings.BUILDING_LONGITUDE));
				buildingId = cursor.getString(cursor.getColumnIndex(MapsBuildings.BUILDING_BUILDING_ID));
				campusId = cursor.getString(cursor.getColumnIndex(MapsCampuses.CAMPUS_ID));
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
		Log.d(TAG, "refresh");
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

	@Override
	public void onStart() {
		super.onStart();
		this.sendView("Important Number Detail", getEllucianActivity().moduleName);
	}

}
