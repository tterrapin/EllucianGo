/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.assignments;


import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.ilp.IlpDetailFragment;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;
import com.ellucian.mobile.android.webframe.WebframeActivity;

public class CourseAssignmentsDetailFragment extends IlpDetailFragment {
	private static final String TAG = CourseAssignmentsDetailFragment.class.getSimpleName();
	private boolean calendarAvailable;

	public CourseAssignmentsDetailFragment() {	
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		Intent calIntent = new Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI);
		calendarAvailable = Utils.isIntentAvailable(getActivity(), calIntent);
	}
		
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.assignments_detail, menu);
		if (!calendarAvailable) {
			MenuItem remindMeItem = menu.findItem(R.id.assignment_detail_remind_me);
			remindMeItem.setVisible(false);
			remindMeItem.setEnabled(false);
		}
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int itemId = item.getItemId();
    	
    	if (itemId == R.id.view_target) {
    		String url = getArguments().getString(Extra.LINK);
    		Log.d(TAG, "View url: " + url);
    		if (!TextUtils.isEmpty(url)) {
 
    			sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_FOLLOW_WEB, "Open assignment in web frame", null, getEllucianActivity().moduleName);
    			
    			Intent intent = new Intent(getActivity(), WebframeActivity.class);
    			intent.putExtra(Extra.REQUEST_URL, url);
	    		
	    		startActivity(intent);
	    		return true;
    		}
    	}
		if (itemId == R.id.assignment_detail_remind_me) {
			sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION,
					GoogleAnalyticsConstants.ACTION_BUTTON_PRESS,
					"Remind Me", null, getEllucianActivity().moduleName);
			sendAddToCalendarIntent();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		sendView("Course assignments detail", getEllucianActivity().moduleName);
	}
	
}
