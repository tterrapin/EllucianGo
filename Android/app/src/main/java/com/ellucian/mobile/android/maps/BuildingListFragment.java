/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.maps;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.provider.EllucianContract.MapsBuildings;
import com.ellucian.mobile.android.provider.EllucianContract.MapsCampuses;
import com.ellucian.mobile.android.util.Extra;

public class BuildingListFragment extends EllucianDefaultListFragment {

	private static final String TAG = BuildingListFragment.class.getSimpleName();
	
	public BuildingListFragment() {
		// null constructor
	}
	
	@Override
	public Bundle buildDetailBundle(Cursor data) {
		Log.d(TAG, "buildDetailBundle");
		Bundle bundle = new Bundle();
		bundle.putString(Extra.MODULE_NAME, getEllucianActivity().moduleName);
		String buildingName = data.getString(data
				.getColumnIndex(MapsBuildings.BUILDING_NAME));
		String campusName = data.getString(data
				.getColumnIndex(MapsCampuses.CAMPUS_NAME));
		String category = data
				.getString(data
						.getColumnIndex(MapsBuildings.BUILDING_CATEGORIES));
		String description = data.getString(data
				.getColumnIndex(MapsBuildings.BUILDING_DESCRIPTION));
		String imageUri = data.getString(data
				.getColumnIndex(MapsBuildings.BUILDING_IMAGE_URL));
		String address = data.getString(data
				.getColumnIndex(MapsBuildings.BUILDING_ADDRESS));
		double buildingLat = data.getDouble(data
				.getColumnIndex(MapsBuildings.BUILDING_LATITUDE));
		double buildingLon = data.getDouble(data
				.getColumnIndex(MapsBuildings.BUILDING_LONGITUDE));
		String additionalServices = data.getString(data
				.getColumnIndex(MapsBuildings.BUILDING_ADDITIONAL_SERVICES));

		return MapUtils.buildBuildingDetailBundle(buildingName, category, address, description, 
					null, null, imageUri, buildingLat, buildingLon, null, null, campusName, 
					additionalServices, false, true);
	}
	
	@Override
	public Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
		return BuildingDetailFragment.class;
	}
	
	@Override
	public Class<? extends EllucianDefaultDetailActivity> getDetailActivityClass() {
		return BuildingDetailActivity.class;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		sendView("Building List", getEllucianActivity().moduleName);
	}
		
}