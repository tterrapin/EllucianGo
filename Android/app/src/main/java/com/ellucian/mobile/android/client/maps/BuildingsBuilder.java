/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.maps;

import java.util.ArrayList;
import java.util.HashSet;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.text.TextUtils;

import com.ellucian.mobile.android.client.ContentProviderOperationBuilder;
import com.ellucian.mobile.android.provider.EllucianContract.MapsBuildings;
import com.ellucian.mobile.android.provider.EllucianContract.MapsBuildingsCategories;
import com.ellucian.mobile.android.provider.EllucianContract.MapsCampuses;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;

public class BuildingsBuilder extends ContentProviderOperationBuilder<BuildingsResponse> {
	private static final String FROM_NUMBERS = "from_numbers";
	private final String module;

	public BuildingsBuilder(Context context, String module) {
		super(context);
		// Setting default if module not there
		if (!TextUtils.isEmpty(module)) {
			this.module = module;
		} else {
			this.module = FROM_NUMBERS;
		}		
	}

	@Override
	public ArrayList<ContentProviderOperation> buildOperations(BuildingsResponse model) {
		final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
		final HashSet<String> categories = new HashSet<String>();

		// Not deleting anything from tables because all these are set to replace on conflict in database
		
		
		for (Building building : model.buildings) {
			
			String typeString = "";
			for (String type : building.type) {
				if(!categories.contains(type)) {
					
					if (categories.size() > 0) {
						typeString += ",";
					}
					
					typeString += type;
					categories.add(type);
				
				batch.add(ContentProviderOperation
						.newInsert(MapsBuildingsCategories.CONTENT_URI)
						.withValue(MapsBuildingsCategories.MAPS_BUILDINGS_CATEGORY_NAME, type)
						.withValue(Modules.MODULES_ID, module)
						.build());
				
				}
			}

			batch.add(ContentProviderOperation
					.newInsert(MapsBuildings.CONTENT_URI)
					.withValue(MapsBuildings.BUILDING_BUILDING_ID, building.id)
					.withValue(MapsCampuses.CAMPUS_ID, building.campusId)
					.withValue(MapsBuildings.BUILDING_NAME, building.name)
					.withValue(MapsBuildings.BUILDING_ADDRESS, building.address != null ? building.address.replace("\\n", "\n") : null) 
					.withValue(MapsBuildings.BUILDING_CATEGORIES, typeString)					
					.withValue(MapsBuildings.BUILDING_DESCRIPTION, building.longDescription)
					.withValue(MapsBuildings.BUILDING_IMAGE_URL, building.imageUrl)
					.withValue(MapsBuildings.BUILDING_LATITUDE, building.latitude)
					.withValue(MapsBuildings.BUILDING_LONGITUDE, building.longitude)
					
					.withValue(Modules.MODULES_ID, module)
					.withValue(MapsBuildings.BUILDING_ADDITIONAL_SERVICES, building.additionalServices)
					.build());
		}
			
		
		return batch;
	}

}
