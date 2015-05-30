/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.events;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.CategoryDialogFragment;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.services.EventsIntentService;
import com.ellucian.mobile.android.provider.EllucianContract.Events;
import com.ellucian.mobile.android.provider.EllucianContract.EventsCategories;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;
import com.ellucian.mobile.android.provider.EllucianDatabase;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

public class EventsActivity extends EllucianActivity implements LoaderManager.LoaderCallbacks<Cursor>,
	CategoryDialogFragment.CategoryDialogListener {
	
	private SimpleCursorAdapter adapter;
	private EllucianDefaultListFragment mainFragment;
	private DialogFragment dialogFragment;
	private String[] allCategories;
	private String[] filteredCategories;
	private String query;
	private boolean resetListPosition;
	private static String[] eventsColumns = new String[] {
		Events._ID,
		Events.EVENTS_TITLE, 
		Events.EVENTS_START, 
		Events.EVENTS_LOCATION, 
		Events.EVENTS_CATEGORIES,
		Events.EVENTS_DESCRIPTION,
		Events.EVENTS_CONTACT,
		Events.EVENTS_EMAIL,
		Events.EVENTS_END,
		Events.EVENTS_ALL_DAY
	};
	
	// TODO - fix this when events gets fixed on the mobile server
	private final SimpleDateFormat eventsFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	setContentView(R.layout.activity_default_dual_pane);
        
        this.setTitle(moduleName);
    	
    	adapter = new SimpleCursorAdapter(
				this,
				R.layout.events_row, 
				null, 
				new String [] { Events.EVENTS_TITLE, Events.EVENTS_START, Events.EVENTS_LOCATION, Events.EVENTS_CATEGORIES }, 
				new int[] { R.id.events_title, R.id.events_start_time, R.id.events_location, R.id.events_category}, 
				0);
    	
    	
    	FragmentManager manager = getFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		mainFragment =  (EllucianDefaultListFragment) manager.findFragmentByTag("EventsListFragment");
		
		if (mainFragment == null) {
			mainFragment = EllucianDefaultListFragment.newInstance(this, EventsListFragment.class.getName(), null);
			
			mainFragment.setListAdapter(adapter);
			transaction.add(R.id.frame_main, mainFragment, "EventsListFragment");
		} else {
			mainFragment.setListAdapter(adapter);
			transaction.attach(mainFragment);
		}
		
		ViewBinder viewBinder = new EventsViewBinder();
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
    		Intent restartingIntent = new Intent(this, EventsActivity.class);
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
        
        getLoaderManager().restartLoader(0, arguments, this);
        getLoaderManager().restartLoader(1, null, this);
        
        Intent serviceIntent = new Intent(this, EventsIntentService.class);
        serviceIntent.putExtra(Extra.MODULE_ID, moduleId);
        serviceIntent.putExtra(Extra.REQUEST_URL, requestUrl);
        startService(serviceIntent); 
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
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
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	if (!TextUtils.isEmpty(query)) {
    		outState.putString("query", query);
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.events, menu);
        
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                 (SearchView) menu.findItem(R.id.events_action_search).getActionView();
        searchView.setSearchableInfo(
                 searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnCloseListener(new OnCloseListener() {

			@Override
			public boolean onClose() {
	        	getLoaderManager().restartLoader(0, null, EventsActivity.this);
	        	query = null;
	        	resetListPosition = true;
				return false;
			}
           
        });
        searchView.setOnSearchClickListener(new SearchView.OnClickListener() {

			@Override
			public void onClick(View v) {
				EventsActivity.this.sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_SEARCH, "Search", null, moduleName);
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
    	case R.id.events_menu_filter:
    		sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_LIST_SELECT, "Select filter", null, moduleName);
    		dialogFragment = new CategoryDialogFragment();
    	    dialogFragment.show(getFragmentManager(), moduleId + "_" + CATEGORY_DIALOG);
    		return true;
    	case R.id.events_action_search:
    		onSearchRequested();
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
    
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = null;
		String selection = null;
		String[] selectionArgs = null;
			
		switch (id) {
		case 0:	
			Uri uri = Events.CONTENT_URI;
			if(args != null && args.containsKey("query")) {
				uri = Events.buildSearchUri(args.getString("query"));
			}
			Log.d("EventsActivity.onCreateLoader", "Creating loader for: " + id + " with URI: " + uri);
			// Because of the JOIN on tables you have to specify the right table column.
			selection = EllucianDatabase.Tables.EVENTS + "." + Modules.MODULES_ID + " = ?";

			if (filteredCategories != null && filteredCategories.length > 0) {				
				selectionArgs = new String[filteredCategories.length + 1];
				selectionArgs[0] = moduleId;
				System.arraycopy(filteredCategories, 0, selectionArgs, 1, filteredCategories.length);
			} else {
				selectionArgs = new String[] {moduleId};
			}
			
			for (int i = 1; i < selectionArgs.length; i++) {
				selection += " AND " + EventsCategories.EVENTS_CATEGORY_NAME + " != ?"; 
			}

			return new CursorLoader(this, uri, eventsColumns, selection, selectionArgs, Events.DEFAULT_SORT);
		case 1:
			Log.d("EventsActivity.onCreateLoader", "Creating loader for: " + id + " with URI: " + EventsCategories.CONTENT_URI);
			selection = Modules.MODULES_ID + " = ?";
			projection = new String[] {EventsCategories.EVENTS_CATEGORY_NAME };
			selectionArgs = new String[] {moduleId};
			return new CursorLoader(this, EventsCategories.CONTENT_URI, projection, selection, selectionArgs, EventsCategories.DEFAULT_SORT);
		default:
			return null;
		}

	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		int id = loader.getId();
		switch (id) {
		case 0:
			Log.d("EventsActivity.onLoadFinished", "Finished loading cursor.  Swapping cursor in adapter containing " + data.getCount());
			adapter.swapCursor(data);
			createNotifyHandler(mainFragment);
			break;
		case 1:
			Log.d("EventsActivity.onLoadFinished", "Finished loading cursor.  Updating categories");
			
			ArrayList<String> categoriesList = new ArrayList<String>();
			while (data.moveToNext()) {
	        	int columnIndex = data.getColumnIndex(EventsCategories.EVENTS_CATEGORY_NAME);
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
			Log.d("EventsActivity.onLoaderReset", "Resetting loader");
			adapter.swapCursor(null);
			break;
		}
	}
	
	/** Make sure to call this method in the LoaderManager.LoaderCallback.onLoadFinished method if  
   	 *  it is overridden in the subclass 
   	 */ 	
	protected void createNotifyHandler(final EllucianDefaultListFragment fragment) {
   		Handler handler = new Handler(Looper.getMainLooper());
   		handler.post(new Runnable(){

			@Override
			public void run() {
				fragment.setInitialCursorPosition(resetListPosition);
				
			}
   			
   		});
   	}
	
	public String getDefaultDateFormatedString(Date date) {
		String dateString;
		DateFormat dateFormatter = android.text.format.DateFormat.getDateFormat(this.getBaseContext());
		dateString = dateFormatter.format(date);
		return dateString;
	}
	
	public String getDefaultTimeFormatedString(Date date) {
		String timeString;
		DateFormat timeFormatter = android.text.format.DateFormat.getTimeFormat(this.getBaseContext());
		timeString = timeFormatter.format(date);
		return timeString;
	}
	
	public String getEventDateFormatedString(Date start, Date end, boolean allDay) {
		String output = "";

		if (allDay) {
			output = getString(R.string.date_all_day_event_format,
						getDefaultDateFormatedString(start));
		} else if (end != null) {
			output = getString(R.string.date_time_to_time_format,
						getDefaultDateFormatedString(start),
						getDefaultTimeFormatedString(start),
						getDefaultTimeFormatedString(end));
		} else {
			output = getString(R.string.date_time_format,
						getDefaultDateFormatedString(start),
						getDefaultTimeFormatedString(start));
		}
		return output;
	}
	
	// TODO - fix this when events gets fixed on the mobile server
	public String fromEventDate(Date date) {
		String formattedDate = null;
		if (date != null) {
			formattedDate = eventsFormat.format(date);
		}
		return formattedDate;
	}
	
	// TODO - fix this when events gets fixed on the mobile server
	public Date toEventDate(String formattedDate) {
		Date date = null;
		if (formattedDate != null) {
			try {
				date = eventsFormat.parse(formattedDate);
			} catch (ParseException e) {
				Log.e("EllucianDatabase.toDate",
						"Unable to convert " + formattedDate + " to a date.\n"
								+ e.getLocalizedMessage());
			}
		}
		return date;
	}
	
	private class EventsViewBinder implements SimpleCursorAdapter.ViewBinder {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int index) {

			if(index == cursor.getColumnIndex(Events.EVENTS_START)) {
				int allDayColumn = cursor.getColumnIndex(Events.EVENTS_ALL_DAY);
				int allDayFlag = cursor.getInt(allDayColumn);
				
				// TODO - fix this when events gets fixed on the mobile server
				String startDateString = cursor.getString(index);
				int endColumn = cursor.getColumnIndex(Events.EVENTS_END);
				String endDateString = cursor.getString(endColumn);

				Date startDate = toEventDate(startDateString);
				Date endDate = toEventDate(endDateString);
				
				String output = "";
				
				if (allDayFlag == 0) {
					output = getEventDateFormatedString(startDate, endDate, false);
				} else {
					output = getEventDateFormatedString(startDate, null, true);			
				}
				
				((TextView) view).setText(output);
				return true;
			} else if (index == cursor.getColumnIndex(Events.EVENTS_CATEGORIES)) {
				// There is a bug in android that cuts off the side of a italic text in TextView.
				// Adding a space to end of string is a quick fix.
				String category = cursor.getString(index);
				category += " ";
				((TextView) view).setText(category);
				return true;
			} else {
				return false;
			}
			
		}
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
		getLoaderManager().restartLoader(0, null, this);
	}
}
