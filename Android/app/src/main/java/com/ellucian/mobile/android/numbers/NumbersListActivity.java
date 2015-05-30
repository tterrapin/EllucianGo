/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.numbers;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.EllucianDefaultExpandableListFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.services.NumbersIntentService;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;
import com.ellucian.mobile.android.provider.EllucianContract.Numbers;
import com.ellucian.mobile.android.provider.EllucianContract.NumbersCategories;
import com.ellucian.mobile.android.util.Extra;

public class NumbersListActivity extends EllucianActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = NumbersListActivity.class.getSimpleName();
	// category loader is positioned at -1 so that loaders for the
	// individual categories can be automatically allocated at 0+
    private static final int CATEGORY_LOADER = -1;
	private String query;
	private SimpleCursorTreeAdapter adapter;
	private EllucianDefaultExpandableListFragment mainFragment;
	private boolean resetListPosition;
	private boolean clearList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_default_dual_pane);
		this.setTitle(moduleName);
		adapter = getCursorAdapter();
    	FragmentManager manager = getFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		mainFragment =  (EllucianDefaultExpandableListFragment) manager.findFragmentByTag("NumbersListFragment");
		
		if (mainFragment == null) {
			mainFragment = EllucianDefaultExpandableListFragment.newInstance(this, NumbersListFragment.class.getName(), null);

			mainFragment.setListAdapter(adapter);
			transaction.add(R.id.frame_main, mainFragment, "NumbersListFragment");
		} else {
			mainFragment.setListAdapter(adapter);
			transaction.attach(mainFragment);
		}

		SimpleCursorTreeAdapter.ViewBinder viewBinder = new NumbersViewBinder();
		if (viewBinder != null) {
			mainFragment.setViewBinder(viewBinder);
		}
		transaction.commit();
		
		if (savedInstanceState != null) {
			query = savedInstanceState.getString("query");
		}
		
		handleIntent(getIntent());

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d(TAG, "onNewIntent");
		// This case is when a user is already in a module that has singleTop set and 
    	// they select the same module type in the navigation menu
    	if ( !Intent.ACTION_SEARCH.equals(intent.getAction())  && 
    			!moduleId.equals(intent.getStringExtra(Extra.MODULE_ID)) ) {
    		Intent restartingIntent = new Intent(this, NumbersListActivity.class);
    		restartingIntent.putExtras(intent.getExtras());
    		restartingIntent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
    		startActivity(restartingIntent);
    		this.finish();
    	}
		handleIntent(intent);
	}
	
	private void handleIntent(Intent intent) {
		resetListPosition = false;
		clearList = false;
		
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            query = intent.getStringExtra(SearchManager.QUERY);
            
            clearList = true;
            mainFragment.clearCurrentDetailFragment();
        }
        
        getLoaderManager().restartLoader(CATEGORY_LOADER, null, this);

        Intent serviceIntent = new Intent(this, NumbersIntentService.class);
        serviceIntent.putExtra(Extra.MODULE_ID, moduleId);
        serviceIntent.putExtra(Extra.REQUEST_URL, requestUrl);
        startService(serviceIntent); 
        
	}
	 
	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart");
		super.onRestart();
		getLoaderManager().restartLoader(CATEGORY_LOADER, null, this);
		
		Intent serviceIntent = new Intent(this, NumbersIntentService.class);
        serviceIntent.putExtra(Extra.MODULE_ID, moduleId);
        serviceIntent.putExtra(Extra.REQUEST_URL, requestUrl);
        startService(serviceIntent); 
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	if (!TextUtils.isEmpty(query)) {
    		outState.putString("query", query);
    	}
    }

	public NumbersExpandableListAdapter getCursorAdapter() {
		Log.d(TAG, "getCursorAdapter");
		return new NumbersExpandableListAdapter(
				this,
				R.layout.expandable_list_group_item,
				new String[] { NumbersCategories.NUMBERS_CATEGORY_NAME},
				new int[] { R.id.text },
				R.layout.expandable_list_child_item,
				new String[] { Numbers.NUMBERS_NAME },
				new int[] { R.id.text }
			);
	}

	public void doQuery(String queryString) {
		Log.d(TAG, "doQuery");
		if (!TextUtils.isEmpty(queryString)) {
			query = queryString;		
		} else {
			query = null;
		}
		getLoaderManager().restartLoader(CATEGORY_LOADER, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader, id: "+Integer.toString(id));

		String selection = null;
		String[] selectionArgs = null;

		switch (id) {
		case CATEGORY_LOADER:
			Log.d(TAG, "onCreateLoader for CategoryLoader");
			selection = Modules.MODULES_ID + " = ?";
			selectionArgs = new String[] { moduleId };

			return new CursorLoader(this, NumbersCategories.CONTENT_URI, null, selection, selectionArgs, NumbersCategories.DEFAULT_SORT);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.d(TAG, "onLoadFinished");
		int id = loader.getId();
		switch (id) {
		case CATEGORY_LOADER:
			Log.d(TAG, "onLoadFinished for CategoryLoader returning " + data.getCount());
			adapter.setGroupCursor(data);

			View emptyView = mainFragment.getRootView().findViewById(android.R.id.empty);
			mainFragment.getExpandableListView().setEmptyView(emptyView);
			createNotifyHandler(mainFragment);
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d(TAG, "onLoaderReset");
		int id = loader.getId();
		switch (id) {
		case CATEGORY_LOADER:
			Log.d(TAG, "onLoaderReset for CategoryLoader");
			adapter.setGroupCursor(null);
			break;
		}
	}
	
	protected void createNotifyHandler(final EllucianDefaultExpandableListFragment fragment) {
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable(){

			@Override
			public void run() {

				if (!TextUtils.isEmpty(query)) {
					Log.d(TAG, "query: " + query + ", clearList: " + clearList);
					fragment.setInitialCursorPositionAfterQuery(clearList);
				} else {
					Log.d(TAG, "resetListPosition: " + resetListPosition);
					fragment.setInitialCursorPosition(resetListPosition);
				}
			}
		});
   	}

    private class NumbersExpandableListAdapter extends SimpleCursorTreeAdapter {
    	
		public NumbersExpandableListAdapter(Context context, 
				int groupLayout, String[] groupFrom, int[] groupTo,
				int childLayout, String[] childFrom, int[] childTo) {
			
			super(context, null, 
					groupLayout, groupFrom, groupTo, 
					childLayout, childFrom,	childTo);
		}

		@Override
		protected Cursor getChildrenCursor(Cursor groupCursor) {
			Log.d(TAG, "getChildrenCursor");
			int position = groupCursor.getPosition();
			int _id = groupCursor.getInt(groupCursor.getColumnIndex(NumbersCategories._ID));
			String name = groupCursor.getString(groupCursor.getColumnIndex(NumbersCategories.NUMBERS_CATEGORY_NAME));

			Log.d(TAG, "Get Child Cursor for position " + position + ": name: " + name + " _id: " + _id);

			String selection = null;
			String[] selectionArgs = null;
			
			selection = Modules.MODULES_ID + " = ?"
					+ " AND " + NumbersCategories.NUMBERS_CATEGORY_NAME + " = ?";
			
			if (!TextUtils.isEmpty(query)) {
				Log.d(TAG, "query, " + query);
				selection += " AND " + Numbers.NUMBERS_NAME + " LIKE ?";
				selectionArgs = new String[] { moduleId, name, "%" + query + "%" };
			} else {
				Log.d(TAG, "onCreateLoader, no query");
				selectionArgs = new String[] { moduleId, name };
			}

			Cursor cursor = getContentResolver().query(Numbers.CONTENT_URI, null, selection, selectionArgs, Numbers.DEFAULT_SORT);

			return cursor;
		}
    }
	
	
	private class NumbersViewBinder implements SimpleCursorTreeAdapter.ViewBinder {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int index) {
			//Log.d(TAG, "numbersViewBinder, setViewValue, index: "+Integer.toString(index));
			if (index == cursor.getColumnIndex(Numbers.NUMBERS_NAME)) {
				String title = cursor.getString(index);
				String output = getString(R.string.notifications_no_notifications);
				if (title != null) {
					output = title;
				}
				((TextView) view).setText(output);
				return true;
			} else {
				return false;
			}
		}
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.important_numbers, menu);
    	
    	SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    	SearchView searchView = (SearchView) menu.findItem(R.id.numbers_action_search).getActionView();
    	searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    	
    	searchView.setOnCloseListener(new OnCloseListener() {
    		@Override
    		public boolean onClose() {
    			Log.d(TAG, "onClose - onCloseListener");
    			getLoaderManager().restartLoader(CATEGORY_LOADER, null, NumbersListActivity.this);
    			query = null;
    			resetListPosition = true;
    			mainFragment.clearCurrentDetailFragment();
    			return false;
    		}
    	});
    	
    	searchView.setOnClickListener(new SearchView.OnClickListener() {
    		@Override
    		public void onClick(View v) {
    			Log.d(TAG, "onClick - onClickListener");
    			NumbersListActivity.this.sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_SEARCH, "Search", null, moduleName);
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
    	case R.id.numbers_action_search:
    		onSearchRequested();
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    

}

