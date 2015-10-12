/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.news;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.ShareActionProvider.OnShareTargetSelectedListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;
import com.ellucian.mobile.android.view.SquareImageView;

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
		
        Bundle args = getArguments();

        if (args.containsKey(Extra.LOGO)) {
        	String logo = args.getString(Extra.LOGO);
            if (!TextUtils.isEmpty(logo)) {
                Log.d(TAG, "Downloading logo: " + logo);
                SquareImageView iView = (SquareImageView) rootView.findViewById(R.id.news_detail_logo);
//                new DownloadImageTask(iView).execute(logo);
                AQuery aq = new AQuery(activity);
                aq.id(iView).image(logo);
                iView.setVisibility(View.VISIBLE);
            }
        }
        
        TextView titleView = (TextView) rootView.findViewById(R.id.news_detail_title);
        titleView.setText(args.getString(Extra.TITLE));

        if (args.containsKey(Extra.DATE)) {
        	TextView dateView = (TextView) rootView.findViewById(R.id.news_detail_date);
        	dateView.setText(args.getString(Extra.DATE));
        }
        if (args.containsKey(Extra.CONTENT) && args.getString(Extra.CONTENT) != null) {
            String content = args.getString(Extra.CONTENT).replace("\n", "<br/>");
            // Replace TextView with WebView.
//	        TextView contentView = (TextView) rootView.findViewById(R.id.news_detail_content);
//	        URLImageParser parser = new URLImageParser(contentView, activity);
//	        Spanned formattedHtml = Html.fromHtml(content, parser, null);
//	        contentView.setText(formattedHtml, TextView.BufferType.SPANNABLE);
//	        contentView.setMovementMethod(LinkMovementMethod.getInstance());

            WebView webContentView = (WebView) rootView.findViewById((R.id.news_detail_web_content));
            // Use CSS to set webView's body to have no padding/margins and images not to exceed view width.
            webContentView.loadDataWithBaseURL(null, "<style>html,body{margin:0px;padding:0px;} img{display: inline;height: auto;max-width: 100%;}</style>" + content, "text/html", "UTF-8", null);
            webContentView.setBackgroundColor(Color.TRANSPARENT);
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
        		(ShareActionProvider) MenuItemCompat.getActionProvider(sharedMenuItem);
        
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
        } else {
            // hide "open in browser" menu options if there is no link
            MenuItem openInBrowser = menu.findItem(R.id.browser);
            openInBrowser.setVisible(false);
            openInBrowser.setEnabled(false);
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
        switch (item.getItemId()) {
            case R.id.share:
                return super.onOptionsItemSelected(item);
            case R.id.browser:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getArguments().getString(Extra.LINK)));
                startActivity(browserIntent);
                return true;
        }
        return false;
    }
	
	@Override
	public void onStart() {
		super.onStart();
		sendView("News detail", getEllucianActivity().moduleName);
	}
	
}
