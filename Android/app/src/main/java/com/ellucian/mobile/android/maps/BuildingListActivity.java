/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.maps;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SimpleCursorAdapter;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.provider.EllucianContract.MapsBuildings;
import com.ellucian.mobile.android.provider.EllucianContract.MapsCampuses;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;
import com.ellucian.mobile.android.provider.EllucianDatabase.Tables;
import com.ellucian.mobile.android.util.Extra;

public class BuildingListActivity extends EllucianActivity 
	implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final String TAG = BuildingListActivity.class.getSimpleName();
	private String query;
	private SimpleCursorAdapter adapter;
	private EllucianDefaultListFragment mainFragment;
	@SuppressWarnings("unused")
	private boolean resetListPosition;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.activity_default_dual_pane);
		this.setTitle(moduleName);
		adapter = getCursorAdapter();
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		mainFragment = (EllucianDefaultListFragment) manager.findFragmentByTag("BuildingListFragment");
		if (mainFragment == null) {
			mainFragment = EllucianDefaultListFragment.newInstance(this,  BuildingListFragment.class.getName(),  null);
			mainFragment.setListAdapter(adapter);
			transaction.add(R.id.frame_main,  mainFragment, "BuildingListFragment");
		} else {
			mainFragment.setListAdapter(adapter);
			transaction.attach(mainFragment);
		}
		transaction.commit();
		if (savedInstanceState != null) {
			query = savedInstanceState.getString("query");
		}
		handleIntent(getIntent());
	}


	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}


	private void handleIntent(Intent intent) {
		Log.d(TAG, "handleIntent");
		
		resetListPosition = false;
		Bundle arguments = null;
		
		if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
			query = intent.getStringExtra(SearchManager.QUERY);
			arguments = new Bundle();
			arguments.putString("query", query);
			resetListPosition = true;
		} else if (!TextUtils.isEmpty(query)) {
			arguments = new Bundle();
			arguments.putString("query", query);
		}
		getSupportLoaderManager().restartLoader(0, arguments, this);
	}


	@Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	if (!TextUtils.isEmpty(query)) {
    		outState.putString("query", query);
    	}
    }


	private SimpleCursorAdapter getCursorAdapter() {
        return new SimpleCursorAdapter(
				this,
				R.layout.row_building_list, 
				null, 
				new String [] { MapsBuildings.BUILDING_NAME , MapsCampuses.CAMPUS_NAME }, 
				new int[] { R.id.rowBuildingBuilding, R.id.rowBuildingCampus }, 
				0);
    }
   

	public void doQuery(String queryString) {
    	if (!TextUtils.isEmpty(queryString)) {
    		query = queryString;
    	} else {
    		query = null;
    	}
    	getSupportLoaderManager().restartLoader(0,  null,  this);
    }


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {	
		Log.d("BuildingListFragment.onCreateLoader", "Creating loader for: " + id + " with URI: " + MapsBuildings.CONTENT_URI);
		
		String selection = Tables.MAPS_BUILDINGS + "." + Modules.MODULES_ID + " = ? ";
		String[] selectionArgs = new String[] {
				this.moduleId };

		if (query != null) {
			selection += " AND " + MapsBuildings.BUILDING_NAME + " LIKE ?";
			selectionArgs = new String[] { this.moduleId,
					 "%" + query + "%" };
		}
		return new CursorLoader(this, MapsBuildings.CONTENT_URI,
				null, selection, selectionArgs, MapsBuildings.DEFAULT_SORT);
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.d("BuildingListFragment.onLoadFinished", "Finished loading cursor.  Swapping cursor in adapter containing " + data.getCount());
		adapter.swapCursor(data);		
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d("BuildingListFragment.onLoaderReset", "Resetting loader");
		adapter.swapCursor(null);		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_maps_building_list, menu);

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(
				R.id.maps_action_search).getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		searchView.setOnCloseListener(new OnCloseListener() {

			@Override
			public boolean onClose() {
				query = null;
				resetListPosition = true;
				getSupportLoaderManager().restartLoader(0,  null,  BuildingListActivity.this);
				return false;
			}

		});
        searchView.setOnSearchClickListener(new SearchView.OnClickListener() {

			@Override
			public void onClick(View v) {
				BuildingListActivity.this.sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_SEARCH, "Search", null, moduleName);
			}
        	
        });
        
        if (!TextUtils.isEmpty(query)) {
        	searchView.setQuery(query, false);
        }
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.maps_action_search:
			onSearchRequested();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	

	@Override
	public void startActivity(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			intent.putExtra(Extra.MODULE_ID, moduleId);
		}
		super.startActivity(intent);
	}

}
