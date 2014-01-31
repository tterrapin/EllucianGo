package com.ellucian.mobile.android.news;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.ShareActionProvider.OnShareTargetSelectedListener;

import com.androidquery.AQuery;
import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.URLImageParser;
import com.ellucian.mobile.android.util.Utils;
import com.ellucian.mobile.android.webframe.WebframeActivity;

public class NewsDetailFragment extends EllucianDefaultDetailFragment {
	private static final String TAG = NewsDetailFragment.class.getSimpleName();
	
	private Activity activity;
	private View rootView;
	
	public NewsDetailFragment() {	
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
		
		
		rootView = inflater.inflate(R.layout.fragment_news_detail, container, false);
		
		View outerHeader = rootView.findViewById(R.id.news_detail_header_layout);
        outerHeader.setBackgroundColor(Utils.getAccentColor(activity));
        
        Bundle args = getArguments();
  
        if (args.containsKey(Extra.LOGO)) {
        	String logo = args.getString(Extra.LOGO);
        	Log.d(TAG, "Downloading logo: " + logo);
			AQuery aq = new AQuery(activity);
			aq.id(R.id.news_detail_logo).image(logo);
        } else {
        	ImageView iView = (ImageView) rootView.findViewById(R.id.news_detail_logo);
        	iView.setVisibility(View.GONE);
        }
        
        TextView titleView = (TextView) rootView.findViewById(R.id.news_detail_title);
        titleView.setText(args.getString(Extra.TITLE));
        titleView.setTextColor(Utils.getSubheaderTextColor(activity));
        
        if (args.containsKey(Extra.DATE)) {
        	TextView dateView = (TextView) rootView.findViewById(R.id.news_detail_date);
        	dateView.setText(args.getString(Extra.DATE));
        }
        if (args.containsKey(Extra.CONTENT) && args.getString(Extra.CONTENT) != null) { 
	        TextView contentView = (TextView) rootView.findViewById(R.id.news_detail_content);
	        URLImageParser parser = new URLImageParser(contentView, activity);
	        Spanned formatedHtml = Html.fromHtml(args.getString(Extra.CONTENT), parser, null);
	        contentView.setText(formatedHtml, TextView.BufferType.SPANNABLE );
	        contentView.setMovementMethod(LinkMovementMethod.getInstance());
        }
        if (args.containsKey(Extra.LINK)) {
        	TextView linkView = (TextView) rootView.findViewById(R.id.news_detail_link);
        	linkView.setAutoLinkMask(Utils.getAvailableLinkMasks(
        			activity, new Integer[] { Linkify.EMAIL_ADDRESSES, Linkify.WEB_URLS }));
        	linkView.setText(args.getString(Extra.LINK));
        }
        
        return rootView;
	}
	
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.news_detail, menu);
        
        Bundle args = getArguments();
        String title = args.getString(Extra.TITLE);
//        String content = intent.getStringExtra(Extra.CONTENT);
//        String strippedContent = intent.getStringExtra(Extra.LIST_DESCRIPTION);
        String link = args.getString(Extra.LINK);
        
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
        String text = title;
        if(!TextUtils.isEmpty(link)) {
        	text += " - " + link;
        }
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
        shareIntent.putExtra(Intent.EXTRA_TEXT, text); //Html.fromHtml(text)
        return shareIntent;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	return super.onOptionsItemSelected(item);
    }
	
	@Override
	public void onStart() {
		super.onStart();
		sendView("Course assignments detail", getEllucianActivity().moduleName);
	}
	
}
