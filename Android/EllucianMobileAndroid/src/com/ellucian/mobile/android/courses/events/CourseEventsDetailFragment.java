package com.ellucian.mobile.android.courses.events;


import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ShareActionProvider;
import android.widget.ShareActionProvider.OnShareTargetSelectedListener;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

public class CourseEventsDetailFragment extends EllucianDefaultDetailFragment {
	
	public CourseEventsDetailFragment() {		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_courses_events, menu);
        
        Bundle args = getArguments();
        String title = args.getString(Extra.TITLE);
        String content = args.getString(Extra.CONTENT);
        String date = args.getString(Extra.DATE);
        String location = args.getString(Extra.LOCATION);
        
        // Adding date and location to the body of the email
        String text = "";
        if (!TextUtils.isEmpty(date)) {
        	text += getString(R.string.row_date_label) + " " + date + "\n\n";
        }
        if (!TextUtils.isEmpty(location)) {
        	text += getString(R.string.row_location_label) + " " + location + "\n\n";
        }
        text += content;
        
        MenuItem sharedMenuItem = menu.findItem(R.id.share);
        
        /** Getting the actionprovider associated with the menu item whose id is share */
        ShareActionProvider shareActionProvider = 
        		(ShareActionProvider) sharedMenuItem.getActionProvider();
        shareActionProvider.setOnShareTargetSelectedListener(new OnShareTargetSelectedListener() {
			
			@Override
			public boolean onShareTargetSelected(ShareActionProvider source,
					Intent intent) {
				String label = "Tap Share Icon - " + intent.getComponent().flattenToShortString();
				sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, label, null, getEllucianActivity().moduleName);
				return false;
			}
		});
               
        /** Getting the target intent */
        Intent shareIntent = getDefaultShareIntent(title, text);
 
        /** Setting a share intent */
        if(Utils.isIntentAvailable(getActivity(), shareIntent)) {
            shareActionProvider.setShareIntent(shareIntent);
        } else {
        	sharedMenuItem.setVisible(false).setEnabled(false);
        }
 
    }
    
    private Intent getDefaultShareIntent(String subject, String text){
    	 
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        return shareIntent;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.event_detail_add_to_calendar:
    		sendAddToCalendarIntent();
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
	private void sendAddToCalendarIntent() {
        Bundle args = getArguments();
        String title = args.getString(Extra.TITLE);
        String content = args.getString(Extra.CONTENT);
        String location = args.getString(Extra.LOCATION);
        
        long startTime = args.getLong(Extra.START, 0);
        long endTime = args.getLong(Extra.END, 0);
        
        // Creating intent for native Calendar App
        Intent intent = new Intent(Intent.ACTION_INSERT)
	        .setData(Events.CONTENT_URI)
	        .putExtra(Events.TITLE, title)
	        .putExtra(Events.DESCRIPTION, content)
	        .putExtra(Events.EVENT_LOCATION, location)
	        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime);
        	if (endTime == -1) {
        		intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
        	} else {
        		intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime);
        	}
        	
        startActivity(intent);
    }
    
    @Override
	public void onStart() {
		super.onStart();
		sendView("Course events detail", getEllucianActivity().moduleName);
	}
}
