package com.ellucian.mobile.android.map;

import java.util.ArrayList;
import java.util.List;

public class BuildingCategory {

	private List<Building> buildings = new ArrayList<Building>();
	private String type;

	public void addBuilding(Building building) {
		getBuildings().add(building);
	}

	public List<Building> getBuildings() {
		return buildings;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void xxxsetBuildings(List<Building> buildings) {
		this.buildings = buildings;
	}

}
