package com.ellucian.mobile.android.map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;

public class CampusMapsParser {

	public static CampusMapCollection parse(String jString)
			throws JSONException {

		final CampusMapCollection campuses = new CampusMapCollection();
		final JSONObject jObject = new JSONObject(jString);

		final JSONArray feedsObj = jObject.getJSONArray("Campuses");
		for (int i = 0; i < feedsObj.length(); i++) {

			final CampusMap campusMap = new CampusMap();
			campuses.add(campusMap);
			campusMap.setName(feedsObj.getJSONObject(i).getString("Name"));
			final JSONObject nw = feedsObj.getJSONObject(i).getJSONObject(
					"NorthWest");
			campusMap.setNorthwest(new GeoPoint((int) (1000000 * nw
					.getDouble("Latitude")), (int) (1000000 * nw
					.getDouble("Longitude"))));
			final JSONObject se = feedsObj.getJSONObject(i).getJSONObject(
					"SouthEast");
			campusMap.setSoutheast(new GeoPoint((int) (1000000 * se
					.getDouble("Latitude")), (int) (1000000 * se
					.getDouble("Longitude"))));
			final JSONArray items = feedsObj.getJSONObject(i).getJSONArray(
					"Categories");
			for (int j = 0; j < items.length(); j++) {
				final BuildingCategory category = new BuildingCategory();
				campusMap.add(category);

				final JSONObject itemObj = items.getJSONObject(j);
				final String type = itemObj.getString("Type");
				category.setType(type);

				final JSONArray buildingsJson = itemObj
						.getJSONArray("Buildings");
				for (int k = 0; k < buildingsJson.length(); k++) {

					final Building building = new Building();
					category.addBuilding(building);

					final JSONObject buildingObj = buildingsJson
							.getJSONObject(k);

					if (!buildingObj.isNull("Name")) {
						building.setName(buildingObj.getString("Name"));
					}
					if (!buildingObj.isNull("Description")) {
						building.setDescription(buildingObj
								.getString("Description"));
					}
					if (!buildingObj.isNull("Image")) {
						building.setImageUrl(buildingObj.getString("Image"));
					}
					// if (!buildingObj.isNull("Address")) {
					// building.setAddress(buildingObj.getString("Address"));
					// }
					if (!buildingObj.isNull("Label")) {
						building.setLabel(buildingObj.getString("Label"));
					}
					if (!buildingObj.isNull("Latitude")
							&& !buildingObj.isNull("Longitude")) {
						building.setGeoPoint(new GeoPoint(
								(int) (1000000 * buildingObj
										.getDouble("Latitude")),
								(int) (1000000 * buildingObj
										.getDouble("Longitude"))));
					}
					building.setType(type);
				}

			}

		}

		return campuses;

	}
}