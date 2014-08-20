package com.ellucian.mobile.android.webframe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ShareActionProvider;
import android.widget.ShareActionProvider.OnShareTargetSelectedListener;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

public class WebframeActivity extends EllucianActivity {

	private WebView webView;
	private SecurityDialogFragment securityDialogFragment;
	private SslErrorHandler handler;
	
	@SuppressLint("SetJavaScriptEnabled")
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webframe);
		
		if (!TextUtils.isEmpty(moduleName)) {
			this.setTitle(moduleName);	
		}
		
		webView = new WebView(this);
		FrameLayout layout = (FrameLayout) findViewById(R.id.web_frame);
		layout.addView(webView);
		
		if (savedInstanceState != null) {
			webView.restoreState(savedInstanceState);
		} else {			
			webView.setWebChromeClient(new WebChromeClient());
			webView.setWebViewClient(new WebViewClient() {
	
			    @Override
			    public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
			    	handleError(handler);
			    }
			    
			    @Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
				    if( url.startsWith("http:") || url.startsWith("https:") ) {
				        return false;
				    }
	
				    // Otherwise allow the OS to handle it
				    sendToExternalBrowser(url);
				    return true;
				}
			});
	
			WebSettings webSettings = webView.getSettings();
			webSettings.setJavaScriptEnabled(true);
			//webSettings.setBuiltInZoomControls(true); //removed because of bug http://code.google.com/p/android/issues/detail?id=15694
			webSettings.setUseWideViewPort(true);
			
			//Enable HTML 5 local storage
			String databasePath = webView.getContext().getDir("databases", 
	                Context.MODE_PRIVATE).getPath(); 
			webSettings.setDatabaseEnabled(true);
			webSettings.setDatabasePath(databasePath); //deprecated, but needed for earlier than API 19
			webSettings.setDomStorageEnabled(true);
			
			Log.d("WebframeActivity", "Making request at: " + requestUrl);

			webView.loadUrl(requestUrl);

		}
		
	}	


	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		webView.saveState(outState);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    // Check if the key event was the Back button and if there's history
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
	    	webView.goBack();
	        return true;
	    }
	    // If it wasn't the Back key or there's no web page history, bubble up to the default
	    // system behavior (probably exit the activity)
	    return super.onKeyDown(keyCode, event);
	}
	
	
	protected void handleError(SslErrorHandler handler) {
		this.handler = handler;
		securityDialogFragment = new SecurityDialogFragment();
		securityDialogFragment.show(getFragmentManager(), SecurityDialogFragment.SECURITY_DIALOG);
	}
	
	protected void onContinueClicked() {
		handler.proceed();
	}
	
	protected void onGoBackClicked() {
		dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
		dispatchKeyEvent(new KeyEvent (KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.webframe, menu);

        Intent intent = getIntent();
        String url = intent.getStringExtra(Extra.REQUEST_URL);
              
        MenuItem sharedMenuItem = menu.findItem(R.id.share);
        
        // Getting the actionprovider associated with the menu item whose id is share
        ShareActionProvider shareActionProvider = 
        		(ShareActionProvider) sharedMenuItem.getActionProvider();
        shareActionProvider.setOnShareTargetSelectedListener(new OnShareTargetSelectedListener() {
			
			@Override
			public boolean onShareTargetSelected(ShareActionProvider source,
					Intent intent) {
				String label = "Tap Share Icon - " + intent.getComponent().flattenToShortString();
				sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, label, null, WebframeActivity.this.moduleName);
				return false;
			}
		});
               
        // Getting the target intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
 
        // Setting a share intent
        if(Utils.isIntentAvailable(this, shareIntent)) {
            shareActionProvider.setShareIntent(shareIntent);
        } else {
        	sharedMenuItem.setVisible(false).setEnabled(false);
        }
        
        
        MenuItem viewMenuItem = menu.findItem(R.id.view_target);
        viewMenuItem.setIcon(R.drawable.ic_location_web_site_white);
        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        viewIntent.setData(Uri.parse(url));
        if (Utils.isIntentAvailable(this, viewIntent)) {
        	viewMenuItem.setIntent(viewIntent);
        } else {
        	viewMenuItem.setVisible(false).setEnabled(false);
        }
        
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
	public void onStart() {
		super.onStart();
		sendView("Display web frame", moduleName);
	}
	
	private void sendToExternalBrowser(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	    startActivity( intent );
	}
	 
}
