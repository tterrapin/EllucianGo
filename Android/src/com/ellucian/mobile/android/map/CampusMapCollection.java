package com.ellucian.mobile.android.map;

import java.util.ArrayList;
import java.util.List;

public class CampusMapCollection {

	private final List<CampusMap> maps = new ArrayList<CampusMap>();

	public void add(CampusMap campusMap) {
		maps.add(campusMap);

	}

	public CampusMap getCampus(String name) {
		for (final CampusMap m : maps) {
			if (m.getName().equals(name)) {
				return m;
			}
		}
		return null;
	}

	public CharSequence[] getCampusNames() {
		final String[] names = new String[maps.size()];
		for (int i = 0; i < maps.size(); i++) {
			names[i] = maps.get(i).getName();
		}
		return names;
	}

	public List<CampusMap> getMaps() {
		return maps;
	}

	public CampusMap getSingleMap() {
		return maps.size() >= 1 ? maps.get(0) : null;
	}

	public boolean hasOnlyOneMap() {
		return maps.size() == 1;
	}
}
