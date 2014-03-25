package com.ellucian.mobile.android.events;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ShareActionProvider;
import android.widget.ShareActionProvider.OnShareTargetSelectedListener;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

public class EventsDetailFragment extends EllucianDefaultDetailFragment {
	
	private Activity activity;
	private View rootView;
	
	public EventsDetailFragment() {	
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }
		
		
		rootView = inflater.inflate(R.layout.fragment_events_detail, container, false);
		
		// Setting Accent colors on headers
        View outerHeader = rootView.findViewById(R.id.events_detail_header_layout);
     
        outerHeader.setBackgroundColor(Utils.getAccentColor(activity));
        
        Bundle args = getArguments();

        TextView titleView = (TextView) rootView.findViewById(R.id.events_detail_title);
        titleView.setText(args.getString(Extra.TITLE));
        titleView.setTextColor(Utils.getSubheaderTextColor(activity));
        
        TextView dateView = (TextView) rootView.findViewById(R.id.events_detail_date);
        if (args.containsKey(Extra.DATE)) {        	
        	dateView.setText(args.getString(Extra.DATE));
        	dateView.setTextColor(Utils.getSubheaderTextColor(activity));
        }else {
        	dateView.setVisibility(View.GONE); 
        }
        
        TextView locationView = (TextView) rootView.findViewById(R.id.events_detail_location);
        if (args.containsKey(Extra.LOCATION)) {      	
        	locationView.setText(args.getString(Extra.LOCATION));
        	locationView.setTextColor(Utils.getSubheaderTextColor(activity));
        } else {
        	locationView.setVisibility(View.GONE); 
        }
        
        TextView contactView = (TextView) rootView.findViewById(R.id.events_detail_contact);
        if (args.containsKey(Extra.CONTACT)) {
        	contactView.setAutoLinkMask(Utils.getAvailableLinkMasks(activity, Linkify.ALL));
        	contactView.setText(args.getString(Extra.CONTACT));
        }else {
        	TextView contactLabel = (TextView) rootView.findViewById(R.id.events_detail_contact_label);
        	contactLabel.setVisibility(View.GONE);
        	contactView.setVisibility(View.GONE); 
        }
        
        TextView emailView = (TextView) rootView.findViewById(R.id.events_detail_email);
        if (args.containsKey(Extra.EMAIL)) {   	
        	emailView.setText(args.getString(Extra.EMAIL));
        }else {
        	TextView emailLabel = (TextView) rootView.findViewById(R.id.events_detail_email_label);
        	emailLabel.setVisibility(View.GONE); 
        	emailView.setVisibility(View.GONE); 
        }
        
        TextView contentView = (TextView) rootView.findViewById(R.id.events_detail_content);
        if (args.containsKey(Extra.CONTENT)) {  
        	contentView.setAutoLinkMask(Utils.getAvailableLinkMasks(activity, Linkify.ALL));
	        contentView.setText(args.getString(Extra.CONTENT) + "\n");
	    }else {
        	contentView.setVisibility(View.GONE); 
        }
        
        return rootView;
	}
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.events_detail, menu);
        
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
        if(Utils.isIntentAvailable(activity, shareIntent)) {
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
			sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION,
					GoogleAnalyticsConstants.ACTION_BUTTON_PRESS,
					"Add to Calendar", null, getEllucianActivity().moduleName);
    		sendAddToCalendarIntent();
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
	private void sendAddToCalendarIntent() {
		Bundle args = getArguments();
        long startTime = args.getLong(Extra.START, 0);
        long endTime = args.getLong(Extra.END, 0);
        String title = args.getString(Extra.TITLE);
        String content = args.getString(Extra.CONTENT);
        String location = args.getString(Extra.LOCATION);
        
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
		sendView("Events Detail", getEllucianActivity().moduleName);
	}
}
