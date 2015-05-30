/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.maps;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Campus {
	public String id;
	public String name;
	public float northWestLatitude;
	public float northWestLongitude;
	public float southEastLatitude;
	public float southEastLongitude;
	public Building[] buildings;
	
	
	public  LatLng calculateCenterPoint() {
		double lat = ((northWestLatitude + southEastLatitude) / 2); 
		double lon = ((northWestLongitude + southEastLongitude) / 2);
		Log.d("MapsBuilder", "calculateCenterPoint lat: " + lat + " lon: " + lon);
		return new LatLng(lat, lon);
	}
}
