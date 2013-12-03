package com.ellucian.mobile.android.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.DataCache;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.UICustomizer;

public class BuildingExpandableListActivity extends ExpandableListActivity {
	private SimpleExpandableListAdapter adapter;
	private CampusMap campusMap;

	private CampusMapCollection mapCollection;
	private String selectedCampus;
	private String url;

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {

		final Intent intent = new Intent(BuildingExpandableListActivity.this,
				BuildingDetailActivity.class);
		@SuppressWarnings("unchecked")
		final HashMap<String, String> groupMap = (HashMap<String, String>) adapter
				.getGroup(groupPosition);

		@SuppressWarnings("unchecked")
		final HashMap<String, String> mapv = (HashMap<String, String>) adapter
				.getChild(groupPosition, childPosition);

		if (mapv.containsKey("name") && mapv.get("name") != null) {
			intent.putExtra("name", mapv.get("name").toString());
		}
		if (mapv.containsKey("label") && mapv.get("label") != null) {
			intent.putExtra("label", mapv.get("label").toString());
		}
		if (mapv.containsKey("description") && mapv.get("description") != null) {
			intent.putExtra("description", mapv.get("description").toString());
		}
		if (mapv.containsKey("latitude") && mapv.get("latitude") != null) {
			intent.putExtra("latitude",
					Double.parseDouble(mapv.get("latitude").toString()));
		}
		if (mapv.containsKey("longitude") && mapv.get("longitude") != null) {
			intent.putExtra("longitude",
					Double.parseDouble(mapv.get("longitude").toString()));
		}
		if (mapv.containsKey("imageUrl") && mapv.get("imageUrl") != null) {
			intent.putExtra("imageUrl", mapv.get("imageUrl").toString());
		}
		if (groupMap.containsKey("type") && groupMap.get("type") != null) {
			intent.putExtra("type", groupMap.get("type").toString());
		}
		startActivity(intent);

		return super.onChildClick(parent, v, groupPosition, childPosition, id);

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.building_expandable_list);

		final Intent intent = getIntent();

		url = intent.getStringExtra("url");
		selectedCampus = intent.getStringExtra("campus");

		this.setTitle(String.format( getResources().getString(R.string.buildingListTitle), selectedCampus));
		UICustomizer.style(this);

		final DataCache cache = ((EllucianApplication) getApplication())
				.getDataCache();

		mapCollection = (CampusMapCollection) cache.getCacheObject(url);
		campusMap = mapCollection.getCampus(selectedCampus);

		setList();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.maps_building_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_search:
			onSearchRequested();
			return true;

		}
		return false;
	}

	@Override
	public boolean onSearchRequested() {
		final Bundle appData = new Bundle();
		appData.putString("campus", selectedCampus);
		appData.putString("url", url);
		startSearch(null, false, appData, false);
		return true;
	}

	private void setList() {
		final List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
		final List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
		for (final BuildingCategory category : campusMap.getCategories()) {
			final Map<String, String> curGroupMap = new HashMap<String, String>();
			groupData.add(curGroupMap);
			curGroupMap.put("type", category.getType());

			final List<Map<String, String>> children = new ArrayList<Map<String, String>>();
			for (final Building building : category.getBuildings()) {

				final Map<String, String> curChildMap = new HashMap<String, String>();
				children.add(curChildMap);

				curChildMap.put("name", building.getName());
				curChildMap.put("label", building.getLabel());
				curChildMap.put("description", building.getDescription());
				curChildMap.put("latitude", ""
						+ building.getGeoPoint().getLatitudeE6() / 1000000d);
				curChildMap.put("longitude", ""
						+ building.getGeoPoint().getLongitudeE6() / 1000000d);
				curChildMap.put("imageUrl", building.getImageUrl());

			}
			childData.add(children);
		}

		// Set up our adapter
		adapter = new SimpleExpandableListAdapter(this, groupData,
				android.R.layout.simple_expandable_list_item_1,
				new String[] { "type" }, new int[] { android.R.id.text1, },
				childData, android.R.layout.simple_expandable_list_item_2,
				new String[] { "name" }, new int[] { android.R.id.text1, });

		setListAdapter(adapter);
	}

}
