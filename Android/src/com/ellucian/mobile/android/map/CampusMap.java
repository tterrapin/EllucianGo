package com.ellucian.mobile.android.map;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;

public class CampusMap {

	private final List<BuildingCategory> categories = new ArrayList<BuildingCategory>();

	private String name;
	private GeoPoint northwest;
	private GeoPoint southeast;

	public void add(BuildingCategory category) {
		getCategories().add(category);

	}

	public List<BuildingCategory> getCategories() {
		return categories;
	}

	public GeoPoint getCenter() {
		return new GeoPoint(
				(northwest.getLatitudeE6() + southeast.getLatitudeE6()) / 2,
				(northwest.getLongitudeE6() + southeast.getLongitudeE6()) / 2);
	}

	public int getLatitudeSpan() {
		return Math.abs(northwest.getLatitudeE6() - southeast.getLatitudeE6());
	}

	public int getLongitudeSpan() {
		return Math
				.abs(northwest.getLongitudeE6() - southeast.getLongitudeE6());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;

	}

	public void setNorthwest(GeoPoint p) {
		northwest = p;
	}

	public void setSoutheast(GeoPoint p) {
		southeast = p;
	}

}
