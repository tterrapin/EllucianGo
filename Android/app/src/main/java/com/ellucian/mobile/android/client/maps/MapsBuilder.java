/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.maps;

import java.util.ArrayList;
import java.util.HashSet;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.util.Log;

import com.ellucian.mobile.android.client.ContentProviderOperationBuilder;
import com.ellucian.mobile.android.provider.EllucianContract.MapsBuildings;
import com.ellucian.mobile.android.provider.EllucianContract.MapsBuildingsBuildingsCategories;
import com.ellucian.mobile.android.provider.EllucianContract.MapsBuildingsCategories;
import com.ellucian.mobile.android.provider.EllucianContract.MapsCampuses;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;
import com.google.android.gms.maps.model.LatLng;

public class MapsBuilder extends ContentProviderOperationBuilder<MapsResponse> {

	private final String module;

	public MapsBuilder(Context context, String module) {
		super(context);
		this.module = module;
	}

	@Override
	public ArrayList<ContentProviderOperation> buildOperations(MapsResponse model) {
		final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
		final HashSet<String> categories = new HashSet<String>();
		
		// delete current contents in database
		batch.add(ContentProviderOperation.newDelete(MapsCampuses.CONTENT_URI)
				.withSelection(Modules.MODULES_ID+"=?", new String[] {module})
				.build());
		batch.add(ContentProviderOperation.newDelete(MapsBuildings.CONTENT_URI)
				.withSelection(Modules.MODULES_ID+"=?", new String[] {module})
				.build());
		batch.add(ContentProviderOperation.newDelete(MapsBuildingsCategories.CONTENT_URI)
				.withSelection(Modules.MODULES_ID+"=?", new String[] {module})
				.build());
		batch.add(ContentProviderOperation.newDelete(MapsBuildingsBuildingsCategories.CONTENT_URI)
				.withSelection(Modules.MODULES_ID+"=?", new String[] {module})
				.build());
		
		// process each campus
		for (Campus campus : model.campuses) {
			
			Log.d("MapsBuilder", "Processing campus: " + campus.name);
			LatLng center = calculateCenterPoint(campus);
			//GeoPoint span = calculateSpan(campus);
			batch.add(ContentProviderOperation
					.newInsert(MapsCampuses.CONTENT_URI)
					.withValue(MapsCampuses.CAMPUS_ID, campus.id)
					.withValue(MapsCampuses.CAMPUS_NAME, campus.name)
					.withValue(MapsCampuses.CAMPUS_CENTER_LATITUDE, center.latitude)
					.withValue(MapsCampuses.CAMPUS_CENTER_LONGITUDE, center.longitude)
					.withValue(MapsCampuses.CAMPUS_NORTHWEST_LATITUDE, campus.northWestLatitude)
					.withValue(MapsCampuses.CAMPUS_NORTHWEST_LONGITUDE, campus.northWestLongitude)
					.withValue(MapsCampuses.CAMPUS_SOUTHEAST_LATITUDE, campus.southEastLatitude)
					.withValue(MapsCampuses.CAMPUS_SOUTHEAST_LONGITUDE, campus.southEastLongitude)
					.withValue(Modules.MODULES_ID, module)
					.build());
			
			
				for (Building building : campus.buildings) {
					
					String categoriesString = "";
					int length = building.type.length;
					for (int i = 0; i < length; i ++) {
						String currentCategory = building.type[i];
						categoriesString += currentCategory;
						if (i != (length -1)) {
							categoriesString += ", ";
						}
						
						if (!categories.contains(currentCategory)) {
							categories.add(currentCategory);
							batch.add(ContentProviderOperation
									.newInsert(MapsBuildingsCategories.CONTENT_URI)
									.withValue(MapsBuildingsCategories.MAPS_BUILDINGS_CATEGORY_NAME, currentCategory)
									.withValue(Modules.MODULES_ID, module)
									.build());
						}
						
						
					}

					String buildingAddress = building.address;
					if(buildingAddress != null) buildingAddress = buildingAddress.replace("\\n", "\n");
					batch.add(ContentProviderOperation
							.newInsert(MapsBuildings.CONTENT_URI)
							.withValue(MapsCampuses.CAMPUS_ID, campus.id)
							.withValue(MapsCampuses.CAMPUS_NAME, campus.name)
							.withValue(MapsBuildings.BUILDING_BUILDING_ID, building.id)
							.withValue(MapsBuildings.BUILDING_ADDRESS, buildingAddress)
							//.withValue(MapsBuildingsCategories.MAPS_BUILDINGS_CATEGORY_NAME, building.type)						
							.withValue(MapsBuildings.BUILDING_CATEGORIES, categoriesString)
							.withValue(MapsBuildings.BUILDING_DESCRIPTION, building.longDescription)
							.withValue(MapsBuildings.BUILDING_IMAGE_URL, building.imageUrl)
							.withValue(MapsBuildings.BUILDING_LATITUDE, building.latitude)
							.withValue(MapsBuildings.BUILDING_LONGITUDE, building.longitude)
							.withValue(MapsBuildings.BUILDING_NAME, building.name)
							.withValue(Modules.MODULES_ID, module)
							.withValue(MapsBuildings.BUILDING_ADDITIONAL_SERVICES, building.additionalServices)
							.build());
					
					for (String category : building.type) {
						batch.add(ContentProviderOperation
								.newInsert(MapsBuildingsBuildingsCategories.CONTENT_URI)
								.withValue(Modules.MODULES_ID, module)
								.withValue(MapsBuildingsCategories.MAPS_BUILDINGS_CATEGORY_NAME, category)
								.withValue(MapsCampuses.CAMPUS_ID, campus.id)
								.withValue(MapsBuildings.BUILDING_BUILDING_ID, building.id)
								.withValue(MapsBuildings.BUILDING_ADDRESS, buildingAddress)
								.withValue(MapsBuildings.BUILDING_CATEGORIES, categoriesString)
								.withValue(MapsBuildings.BUILDING_DESCRIPTION, building.longDescription)
								.withValue(MapsBuildings.BUILDING_IMAGE_URL, building.imageUrl)
								.withValue(MapsBuildings.BUILDING_LATITUDE, building.latitude)
								.withValue(MapsBuildings.BUILDING_LONGITUDE, building.longitude)
								.withValue(MapsBuildings.BUILDING_NAME, building.name)
								.build());
					}
					
				}
			
		}
		return batch;
	}

	private LatLng calculateCenterPoint(Campus campus) {
		double lat = ((campus.northWestLatitude + campus.southEastLatitude) / 2); 
		double lon = ((campus.northWestLongitude + campus.southEastLongitude) / 2);
		Log.d("MapsBuilder", "calculateCenterPoint lat: " + lat + " lon: " + lon);
		return new LatLng(lat, lon);
	}
}
