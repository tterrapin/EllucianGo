/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.news;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.CategoryDialogFragment;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.services.NewsIntentService;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;
import com.ellucian.mobile.android.provider.EllucianContract.News;
import com.ellucian.mobile.android.provider.EllucianContract.NewsCategories;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

import java.util.ArrayList;
import java.util.Date;

public class NewsActivity extends EllucianActivity implements LoaderManager.LoaderCallbacks<Cursor>,
	CategoryDialogFragment.CategoryDialogListener {

    private final Activity activity = this;
    private SimpleCursorAdapter adapter;
	private EllucianDefaultListFragment mainFragment;
	private DialogFragment dialogFragment;
	private String[] allCategories;
	private String[] filteredCategories;
	private String query;
	private boolean resetListPosition;
    private NewsIntentServiceReceiver newsIntentServiceReceiver;
    private boolean showSpinner = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_default_dual_pane);
        
        this.setTitle(moduleName);
        
        adapter = new SimpleCursorAdapter(
        				this,
        				R.layout.news_row, 
        				null, 
        				new String [] { News.NEWS_TITLE, News.NEWS_POST_DATE, News.NEWS_FEED_NAME, News.NEWS_LOGO, News.NEWS_LIST_DESCRIPTION },
        				new int[] { R.id.news_title, R.id.news_date_and_description, R.id.news_category, R.id.news_logo, R.id.news_summary},
        				0);
        
    	FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		mainFragment =  (EllucianDefaultListFragment) manager.findFragmentByTag("NewsListFragment");

        registerNewsServiceReceiver();
		if (mainFragment == null) {
            mainFragment = EllucianDefaultListFragment.newInstance(this, NewsListFragment.class.getName(), null);
			
			mainFragment.setListAdapter(adapter);
			transaction.add(R.id.frame_main, mainFragment, "NewsListFragment");
		} else {
			mainFragment.setListAdapter(adapter);
			transaction.attach(mainFragment);
		}
		
		ViewBinder viewBinder = new NewsViewBinder();
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
    	// This case is when a user is already in a module that has singleTop set and 
    	// they select the same module type in the navigation menu
    	if ( !Intent.ACTION_SEARCH.equals(intent.getAction())  && 
    			!moduleId.equals(intent.getStringExtra(Extra.MODULE_ID)) ) {
    		Intent restartingIntent = new Intent(this, NewsActivity.class);
    		restartingIntent.putExtras(intent.getExtras());
    		restartingIntent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
    		startActivity(restartingIntent);
    		this.finish();
    	}
    	handleIntent(intent);
    }
    
    private void handleIntent(Intent intent) {
    	resetListPosition = false;
    	
        String categoriesString = Utils.getStringFromPreferences(this, CATEGORY_DIALOG, moduleId + "_" + FILTERED_CATEGORIES, "");
        if (!TextUtils.isEmpty(categoriesString)) {
        	filteredCategories = categoriesString.split(",");
        } else {
        	filteredCategories = null;
        }
        
        Bundle arguments = null;
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            query = intent.getStringExtra(SearchManager.QUERY);
            arguments = new Bundle();
            arguments.putString("query", query);
            
            resetListPosition = true;
        } else if (!TextUtils.isEmpty(query)){
        	arguments = new Bundle();
            arguments.putString("query", query);
        }
        
        getSupportLoaderManager().restartLoader(0, arguments, this);
        getSupportLoaderManager().restartLoader(1, null, this);
        
        Intent serviceIntent = new Intent(this, NewsIntentService.class);
        serviceIntent.putExtra(Extra.MODULE_ID, moduleId);
        serviceIntent.putExtra(Extra.REQUEST_URL, requestUrl);
        startService(serviceIntent);
        if (showSpinner) {
            Utils.showProgressIndicator(this);
        }
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
        unregisterNewsServiceReceiver();
    	if (filteredCategories != null) {
	    	StringBuilder categoriesString = new StringBuilder();
	        for (int i = 0; i < filteredCategories.length; i++) {
	        	if (!TextUtils.isEmpty(categoriesString)) {
	        		categoriesString.append(",");
	        	}
	        	categoriesString.append(filteredCategories[i]);
	        }
	        Utils.addStringToPreferences(this, CATEGORY_DIALOG, moduleId + "_" + FILTERED_CATEGORIES, categoriesString.toString());
    	} else {
	        Utils.removeValuesFromPreferences(this, CATEGORY_DIALOG, moduleId + "_" + FILTERED_CATEGORIES);    	
	    }
    	
    	if (dialogFragment != null) {
    		dialogFragment.dismiss();
    		dialogFragment = null;
    	}
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerNewsServiceReceiver();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	if (!TextUtils.isEmpty(query)) {
    		outState.putString("query", query);
    	}
    }
	
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.news, menu);
        
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                 (SearchView) menu.findItem(R.id.news_action_search).getActionView();
        searchView.setSearchableInfo(
                 searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnCloseListener(new OnCloseListener() {

			@Override
			public boolean onClose() {
	        	getSupportLoaderManager().restartLoader(0, null, NewsActivity.this);
	        	query = null;
	        	resetListPosition = true;
				return false;
			}
           
        });
        searchView.setOnSearchClickListener(new SearchView.OnClickListener() {

            @Override
            public void onClick(View v) {
                NewsActivity.this.sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_SEARCH, "Search", null, moduleName);
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
    	case R.id.news_menu_filter:
    		sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_LIST_SELECT, "Select filter", null, moduleName);
    		dialogFragment = new CategoryDialogFragment();
    		dialogFragment.show(getSupportFragmentManager(), moduleId + "_" + CATEGORY_DIALOG);
    		return true;
    	case R.id.news_action_search:
    		onSearchRequested();
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
    
    @Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {		
		String[] projection;
		String selection = Modules.MODULES_ID + " = ?";
		String[] selectionArgs;
			
		switch (id) {
		case 0:	
			Uri uri = News.CONTENT_URI;
			if(args != null && args.containsKey("query")) {
				uri = News.buildSearchUri(args.getString("query"));
			}
			Log.d("NewsActivity.onCreateLoader", "Creating loader for: " + id + " with URI: " + uri);

			if (filteredCategories != null && filteredCategories.length > 0) {				
				selectionArgs = new String[filteredCategories.length + 1];
				selectionArgs[0] = moduleId;
				System.arraycopy(filteredCategories, 0, selectionArgs, 1, filteredCategories.length);
			} else {
				selectionArgs = new String[] {moduleId};
			}
			
			for (int i = 1; i < selectionArgs.length; i++) {
				selection += " AND " + News.NEWS_FEED_NAME + " != ?"; 
			}

			return new CursorLoader(this, uri, null, selection, selectionArgs, News.DEFAULT_SORT);
		case 1:
			Log.d("NewsActivity.onCreateLoader", "Creating loader for: " + id + " with URI: " + NewsCategories.CONTENT_URI);
			projection = new String[] {NewsCategories.NEWS_CATEGORY_NAME };
			selectionArgs = new String[] {moduleId};
			return new CursorLoader(this, NewsCategories.CONTENT_URI, projection, selection, selectionArgs, NewsCategories.DEFAULT_SORT);
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		int id = loader.getId();
		switch (id) {
		case 0:
			Log.d("NewsActivity.onLoadFinished", "Finished loading cursor.  Swapping cursor in adapter containing " + data.getCount());
			adapter.swapCursor(data);
			createNotifyHandler(mainFragment);
            if (data.getCount() == 0) {
                showSpinner = true;
            } else {
                showSpinner = false;
                Utils.hideProgressIndicator(this);
            }
			break;
		case 1:
			Log.d("NewsActivity.onLoadFinished", "Finished loading cursor.  Updating categories");
			
			ArrayList<String> categoriesList = new ArrayList<>();
			while (data.moveToNext()) {
	        	int columnIndex = data.getColumnIndex(NewsCategories.NEWS_CATEGORY_NAME);
	        	categoriesList.add(data.getString(columnIndex));
	        }
			allCategories = categoriesList.toArray(new String[categoriesList.size()]);
			break;
		}

		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = loader.getId();
		switch (id) {
		case 0:
			Log.d("NewsActivity.onLoaderReset", "Resetting loader");
			adapter.swapCursor(null);
			break;
		}
	}
	
	/** Make sure to call this method in the LoaderManager.LoaderCallback.onLoadFinished method if  
   	 *  it is overridden in the subclass 
   	 */
	private void createNotifyHandler(final EllucianDefaultListFragment fragment) {
   		Handler handler = new Handler(Looper.getMainLooper());
   		handler.post(new Runnable(){

			@Override
			public void run() {
				fragment.setInitialCursorPosition(resetListPosition);			
			}
   			
   		});
   	}

	@Override
	public String[] getAllCategories() {		
		return allCategories;
	}

	@Override
	public String[] getFilteredCategories() {
		return filteredCategories;
	}

	@Override
	public void updateFilteredCategories(String[] filteredCategories) {
		this.filteredCategories = filteredCategories;
		resetListPosition = true;
		getSupportLoaderManager().restartLoader(0, null, this);
	}

	
	
	private class NewsViewBinder implements SimpleCursorAdapter.ViewBinder {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int index) {
			if(index == cursor.getColumnIndex(News.NEWS_POST_DATE)) {
				String dateString = cursor.getString(index);
				
				Date date = CalendarUtils.parseFromUTC(dateString);
				
				// TODO - Set this back when the main format gets fixed
				//Date date = EllucianDatabase.toDate(cursor.getString(index));
				
				String output = getString(R.string.unavailable);
				if(date != null) {	
                    output = CalendarUtils.getMonthDateString(NewsActivity.this, date);
				} 
				
				((TextView) view).setText(output);
				return true;
			} else if (index == cursor.getColumnIndex(News.NEWS_FEED_NAME)) {
				String category = cursor.getString(index);
				((TextView) view).setText(category);
				return true;
			} else if (index == cursor.getColumnIndex(News.NEWS_LOGO)) {
				String imageUrl = cursor.getString(cursor.getColumnIndex(News.NEWS_LOGO));
				if (!TextUtils.isEmpty(imageUrl)) {
                    AQuery aq = new AQuery(NewsActivity.this);
					aq.id(view).image(imageUrl);
                    view.setVisibility(View.VISIBLE);
                } else {
                    view.setVisibility(View.GONE);
                }
				return true;
			} else {
				return false;
			}
		}
	}

    private void registerNewsServiceReceiver() {
        if(newsIntentServiceReceiver == null) {
            Log.d("NewsActivity.RegisterNewsServiceReceiver", "Registering new service receiver");
            newsIntentServiceReceiver = new NewsIntentServiceReceiver();
            IntentFilter filter = new IntentFilter(NewsIntentService.ACTION_FINISHED);
            LocalBroadcastManager.getInstance(this).registerReceiver(newsIntentServiceReceiver, filter);
        }
    }

    private void unregisterNewsServiceReceiver() {
        if(newsIntentServiceReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(newsIntentServiceReceiver);
        }
    }

    /**
     * Broadcast receiver which receives notification when the NewsIntentService
     * is finished performing an update of the data from the web services to the
     * local database.  Upon successful completion, this receiver will reload
     * the news data from the local database.
     *
     */
    private class NewsIntentServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean updated = intent.getBooleanExtra(NewsIntentService.PARAM_OUT_DATABASE_UPDATED, false);
            Log.d("NewsIntentServiceReceiver", "onReceive: database updated = " + updated);
            if (updated) {
                Log.d("NewsIntentServiceReceiver.onReceive", "All news retrieved and database updated");
                Utils.hideProgressIndicator(activity);
            }
        }

    }

	
}
