package com.ellucian.mobile.android.notifications;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ShareActionProvider;
import android.widget.ShareActionProvider.OnShareTargetSelectedListener;
import android.widget.TextView;
import android.widget.Toast;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

public class NotificationsDetailFragment extends EllucianDefaultDetailFragment {

	private View rootView;
	
	public NotificationsDetailFragment() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_notifications_detail, container, false);


		Bundle args = getArguments();
		// bundle should contain:
		// TITLE, CONTENT, LINK_LABEL, LINK, DATE attributes
		// which matches to 
		// title, details, hyperlink, linkLabel, notificationDate
		
		String title = null;
		String details = null;
		String linkLabel = null;
		String hyperlink = null;
		String notificationDate = null;
		
		if (args != null) {
			title = args.getString(Extra.TITLE);
			details = args.getString(Extra.CONTENT);
			linkLabel = args.getString(Extra.LINK_LABEL);
			hyperlink = args.getString(Extra.LINK);
			notificationDate = args.getString(Extra.DATE);
			
			int subheaderTextColor = Utils.getSubheaderTextColor(getActivity());
			
	        View outerHeader = (View) rootView.findViewById(R.id.header_layout);
	        outerHeader.setBackgroundColor(Utils.getAccentColor(getActivity()));

			if (!TextUtils.isEmpty(title)) {
				TextView titleView = (TextView) rootView.findViewById(R.id.title);
				titleView.setTextColor(subheaderTextColor);
				titleView.setText(title);
			}
			if (!TextUtils.isEmpty(notificationDate)) {
				TextView dateView = (TextView) rootView.findViewById(R.id.notificationDate);
				dateView.setTextColor(subheaderTextColor);
				dateView.setText(notificationDate);
				TextView dateLabelView = (TextView) rootView.findViewById(R.id.notificationDateLabel);
				dateLabelView.setTextColor(subheaderTextColor);			
			}
			if (!TextUtils.isEmpty(details)) {
				TextView contentView = (TextView) rootView.findViewById(R.id.details);
				contentView.setAutoLinkMask(Utils.getAvailableLinkMasks(getActivity(), Linkify.ALL));
				contentView.setText(details);
			}
			Button button = (Button) rootView.findViewById(R.id.actionButton);
			if (!TextUtils.isEmpty(hyperlink)) {
				if (hyperlink.startsWith("HTTPS")) {
					hyperlink = "https" + hyperlink.substring(5);
				} else {
					hyperlink = "http" + hyperlink.substring(4);
				}
				final Intent websiteIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse(hyperlink));
				boolean intentAvailable = Utils.isIntentAvailable(getActivity(),  websiteIntent);
				if (intentAvailable) {
					button.setText(linkLabel);
					button.setOnClickListener(new View.OnClickListener() {
						public void onClick(View arg0) {
							sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_FOLLOW_WEB, "Open notification in web frame", null, getEllucianActivity().moduleName);
							startActivity(websiteIntent);
						}
					});
				} else {
					button.setVisibility(View.GONE);
				}
			} else {
				button.setVisibility(View.GONE);
			}
		}

		return rootView;
	}
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.notifications_detail, menu);
        
        Bundle args = getArguments();
        String title = args.getString(Extra.TITLE);
        String content = args.getString(Extra.CONTENT);
       
        MenuItem sharedMenuItem = menu.findItem(R.id.share);
        
        MenuItem deleteMenuItem = menu.findItem(R.id.notifications_delete);
    	if (getArguments().getInt(Extra.NOTIFICATIONS_STICKY) == 1) {
    		deleteMenuItem.setVisible(false);
    	} else {
    		deleteMenuItem.setVisible(true);
    	}
        
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
        Intent shareIntent = getDefaultShareIntent(title, content);
 
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
    	case R.id.notifications_delete:
			sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION,
					GoogleAnalyticsConstants.ACTION_BUTTON_PRESS,
					"Deleting notification", null, getEllucianActivity().moduleName);
			DeleteConfirmDialogFragment deleteConfirmDialogFragment = new DeleteConfirmDialogFragment();
			deleteConfirmDialogFragment.detailFragment = this;
			deleteConfirmDialogFragment.show(getFragmentManager(), "RegisterConfirmDialogFragment");
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }

    protected void triggerDeleteNotification() {
    	if (getArguments().getInt(Extra.NOTIFICATIONS_STICKY) == 1) {
    		Toast emptyMessage = Toast.makeText(getActivity(), R.string.notifications_unable_to_delete_message, Toast.LENGTH_LONG);
			emptyMessage.setGravity(Gravity.CENTER, 0, 0);
			emptyMessage.show();
    	} else {
    		Activity activity = getActivity();
	    	if (activity instanceof NotificationsActivity) {
	    		((NotificationsActivity)activity).deleteNotification();
	    	} else if (getActivity() instanceof NotificationsDetailActivity) {
	    		((NotificationsDetailActivity)activity).deleteNotification();
	    	}
    	}
    }

	@Override
	public void onStart() {
		super.onStart();
	}
}
