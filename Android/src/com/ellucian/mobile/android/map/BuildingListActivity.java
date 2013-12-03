package com.ellucian.mobile.android.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.DataCache;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.UICustomizer;

public class BuildingListActivity extends ListActivity {

	private ListAdapter adapter;
	private CampusMapCollection mapCollection;

	private String url;

	private void handleIntent(Intent intent) {

		setContentView(R.layout.building_list);

		final Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);

		if (appData != null) {
			url = appData.getString("url");
			setTitle(getResources().getString(R.string.searchResults) + " - "
					+ intent.getStringExtra(SearchManager.QUERY));
			
		} else {
			url = intent.getStringExtra("url");
			setTitle(getResources().getString(R.string.searchResults));
		}

		UICustomizer.style(this);

		final DataCache cache = ((EllucianApplication) getApplication())
				.getDataCache();

		mapCollection = (CampusMapCollection) cache.getCacheObject(url);
		if (Intent.ACTION_SEARCH.equals(intent.getAction())
				&& intent.getExtras().containsKey(SearchManager.QUERY)) {
			final String query = intent.getStringExtra(SearchManager.QUERY);
			setList(query);
		} else {
			setList();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.building_list);
		handleIntent(getIntent());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.maps_building_list, menu);
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final Intent intent = new Intent(BuildingListActivity.this,
				BuildingDetailActivity.class);

		final Building building = (Building) adapter.getItem(position);

		if (building.getName() != null) {
			intent.putExtra("name", building.getName());
		}
		if (building.getLabel() != null) {
			intent.putExtra("label", building.getLabel());
		}
		if (building.getDescription() != null) {
			intent.putExtra("description", building.getDescription());
		}
		if (building.getImageUrl() != null) {
			intent.putExtra("imageUrl", building.getImageUrl());
		}
		if (building.getType() != null) {
			intent.putExtra("type", building.getType());
		}
		if (building.getGeoPoint() != null) {
			intent.putExtra("latitude",
					building.getGeoPoint().getLatitudeE6() / 1E6D);
			intent.putExtra("longitude", building.getGeoPoint()
					.getLongitudeE6() / 1E6D);
		}

		startActivity(intent);

	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
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
		appData.putString("url", url);
		startSearch(null, false, appData, false);
		return true;
	}

	private void setList() {
		setList(null);
	}

	private void setList(String searchQuery) {

		final List<Building> matches = new ArrayList<Building>();
		for (final CampusMap map : mapCollection.getMaps()) {
			for (final BuildingCategory category : map.getCategories()) {
				for (final Building building : category.getBuildings()) {
					if (searchQuery != null
							&& building.getName().toLowerCase()
									.contains(searchQuery.toLowerCase())) {
						matches.add(building);
					}
				}
			}
		}

		Collections.sort(matches, new Comparator<Building>() {

			public int compare(Building arg0, Building arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}

		});
		adapter = new ArrayAdapter<Building>(BuildingListActivity.this,
				android.R.layout.simple_list_item_1, matches);

		setListAdapter(adapter);
	}
}
