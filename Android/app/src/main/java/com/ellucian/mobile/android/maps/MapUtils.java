/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.maps;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.ellucian.mobile.android.util.Utils;
import com.google.android.gms.maps.model.LatLng;

public class MapUtils {
	private static final String TAG = MapUtils.class.getSimpleName();
	
	private MapUtils() {
	}
	
//	/**
//	 * Converts degrees to micro-degrees.  Uses Objects rather than primitives to be able
//	 * to return null if there is no data present
//	 * @param degrees 
//	 * @return degrees * 1000000
//	 */
//	public static Integer convertToE6(Float degrees) {
//		Integer e6 = null;
//		if(degrees != null) {
//			e6 =  Math.round(degrees * 1000000);
//		}
//		return e6;
//	}
//	
//	public static float convertToDecimal(int microDegrees) {
//		return (float) (microDegrees / 1000000d);
//	}
	
	public static Intent buildBuildingDetailIntent(Context context,
			String name,
			String type,
			String address,
			String description,
			String phone,
			String email,
			String imageUrl,
			Double latitude,
			Double longitude,
			String buildingId,
			String campusId,
			String campusName,
			String additionalServices,
			boolean showName, boolean showBuildingInfo
			) { 
	
		Log.v(TAG, "buildBuildingDetailIntent: Name: " + name + " type: " + type + " address: " + address
				+ " description: " + description + " imageUrl: " + imageUrl 
				+ " email: " + email + " phone: " + phone
				+ " lat: " + latitude + " long: " + longitude
				+ " buildingId: " + buildingId + " campusId: " + campusId + " campusName: " + campusName
				+ " additionalServices: " +  additionalServices
				);
		// Use the new BuildingInfoDetailActivity to launch this intent, since the prior
		// BuildingDetailActivity is not compatible after having been migrated to the dual 
		// pane view for the BuildingList menu option. 
		//final Intent intent = new Intent(context, BuildingDetailActivity.class);
		final Intent intent = new Intent(context, BuildingInfoDetailActivity.class);
		
		Bundle b = buildBuildingDetailBundle(name, type, address, description, 
				phone, email, imageUrl, latitude, longitude, buildingId, campusId, campusName, additionalServices, showName, showBuildingInfo);
		intent.putExtras(b);
		return intent;
	}
	
	public static Bundle buildBuildingDetailBundle(
			String name,
			String type,
			String address,
			String description,
			String phone,
			String email,
			String imageUrl,
			Double latitude,
			Double longitude,
			String buildingId,
			String campusId,
			String campusName,
			String additionalServices,
			boolean showName, boolean showBuildingInfo
			) {
		
		Log.d(TAG, "buildBuildingDetailBundle");
		
		Bundle b = new Bundle();
		if(!TextUtils.isEmpty(name)) {
			b.putString(BuildingDetailFragment.ARG_NAME, name);
		}
		if(!TextUtils.isEmpty(type)) {
			b.putString(BuildingDetailFragment.ARG_TYPE, type);
		}
		if(!TextUtils.isEmpty(address)) {
			b.putString(BuildingDetailFragment.ARG_ADDRESS, address);
		}
		if(!TextUtils.isEmpty(description)) {
			b.putString(BuildingDetailFragment.ARG_DESCRIPTION, description);
		}
		if(!TextUtils.isEmpty(imageUrl)) {
			b.putString(BuildingDetailFragment.ARG_IMAGE_URL, imageUrl);
		}
		if(!TextUtils.isEmpty(email)) {
			b.putString(BuildingDetailFragment.ARG_EMAIL, email);
		}
		if(!TextUtils.isEmpty(phone)) {
			b.putString(BuildingDetailFragment.ARG_PHONE, phone);
		}
		if(latitude != null) {
			b.putDouble(BuildingDetailFragment.ARG_LATITUDE, latitude);
		}
		if(longitude != null) {
			b.putDouble(BuildingDetailFragment.ARG_LONGITUDE, longitude);
		}
		if(!TextUtils.isEmpty(buildingId)) {
			b.putString(BuildingDetailFragment.ARG_BUILDING_ID, buildingId);
		}
		if(!TextUtils.isEmpty(campusId)) {
			b.putString(BuildingDetailFragment.ARG_CAMPUS_ID, campusId);
		}
		if(!TextUtils.isEmpty(campusName)) {
			b.putString(BuildingDetailFragment.ARG_CAMPUS_NAME, campusName);
		}
		if(!TextUtils.isEmpty(additionalServices)) {
			b.putString(BuildingDetailFragment.ARG_ADDITIONAL_SERVICES, additionalServices);
		}
		b.putBoolean(BuildingDetailFragment.ARG_SHOW_NAME, showName);
		b.putBoolean(BuildingDetailFragment.ARG_SHOW_BUILDING_INFO, showBuildingInfo);

		return b;
		
	}
	private static Intent buildDirectionsIntent(LatLng geoPoint) {
		final Intent intent = new Intent(android.content.Intent.ACTION_VIEW,

		Uri.parse("http://maps.google.com/maps?saddr=" + "&daddr="
				+ geoPoint.latitude + ","
				+ geoPoint.longitude));
		return intent;
	}
	
	public static Intent buildDirectionsIntent(double latitude, double longitude) {
		LatLng point = new LatLng(latitude, longitude);
		return buildDirectionsIntent(point);
	}
	
	public static Intent buildDirectionsIntent(String address) {
		final Intent intent = new Intent(android.content.Intent.ACTION_VIEW,

		Uri.parse("http://maps.google.com/maps?saddr=" + "&daddr="
				+ address));
		return intent;
	}
	
	public static Intent buildMapPinIntent(Context context, String name, double latitude, double longitude) {
		final Intent pinIntent = new Intent(context, MapsSingleLocationActivity.class);
		Log.v(TAG, "Creating intent for MapPinActivity: " + name + " latitude: " + latitude + " longitude: " + longitude);
		pinIntent.putExtra("latitude", latitude);
		pinIntent.putExtra("longitude", longitude);
		pinIntent.putExtra("title", name);
		return pinIntent;
	}
	
	public static boolean isMapsIntentAvailable(Context context) {
		final Intent mapIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://maps.google.com/maps"));
		return Utils.isIntentAvailable(context, mapIntent);
	}
	
	
}
