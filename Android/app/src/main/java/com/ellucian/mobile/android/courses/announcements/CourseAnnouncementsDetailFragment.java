/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.announcements;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.ilp.IlpDetailFragment;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.webframe.WebframeActivity;

public class CourseAnnouncementsDetailFragment extends IlpDetailFragment {
	private static final String TAG = CourseAnnouncementsDetailFragment.class.getSimpleName();
	
	public CourseAnnouncementsDetailFragment() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.courses_view_target, menu);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int itemId = item.getItemId();
    	
    	if (itemId == R.id.view_target) {
    		String url = getArguments().getString(Extra.LINK);
    		Log.d(TAG, "View url: " + url);
    		if (!TextUtils.isEmpty(url)) {
	    		
    			sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_FOLLOW_WEB, "Open activity in web frame", null, getEllucianActivity().moduleName);
    			
    			Intent intent = new Intent(getActivity(), WebframeActivity.class);
    			intent.putExtra(Extra.REQUEST_URL, url);
	    		
	    		startActivity(intent);
	    		return true;
    		}
    	}
    	return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		sendView("Course activity detail", getEllucianActivity().moduleName);
	}
	
}
