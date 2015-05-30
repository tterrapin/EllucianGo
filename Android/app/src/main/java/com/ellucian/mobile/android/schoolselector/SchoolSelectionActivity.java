/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.schoolselector;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;

public class SchoolSelectionActivity extends EllucianActivity implements OnQueryTextListener {
	private SchoolSelectionFragment fragment = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// activity_school_selection layout invokes SchoolSelectionFragment
		setContentView(R.layout.activity_school_selection);
		fragment = (SchoolSelectionFragment) getFragmentManager().findFragmentById(R.id.school_list_fragment);

		// set the colors directly, not going through preferences
		int primaryColor = getResources().getColor(R.color.ellucian_primary_color);
		int headerTextColor = getResources().getColor(R.color.ellucian_header_text_color);
		configureActionBarDirect(primaryColor, headerTextColor);
		getActionBar().setIcon(getResources().getDrawable(R.drawable.default_home_icon));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.school_selection, menu);
		
		SearchView searchView = (SearchView) menu.findItem(R.id.school_selection_action_search).getActionView();
		searchView.setQueryHint(getString(R.string.menu_search_school_hint));
		searchView.setOnQueryTextListener(this);
		searchView.setSubmitButtonEnabled(false);
		searchView.setOnSearchClickListener( new SearchView.OnClickListener() {
			@Override
			public void onClick(View v) {
                sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_SEARCH, "Search", null, null);
			}
		});
		
		searchView.setOnCloseListener(new OnCloseListener() {
			@Override
			public boolean onClose() {
				return false;
			}
		});
		return true;
	}

	@Override
	public boolean onQueryTextChange(String query) {
		// on a text change, have the fragment re-query
		this.fragment.doQuery(query);
		return true;
	}

 	@Override
	public boolean onQueryTextSubmit(String query) {
 		// we query on each character addition/removal, 
 		// no need to respond to submit
		return false;
	}
	
}
