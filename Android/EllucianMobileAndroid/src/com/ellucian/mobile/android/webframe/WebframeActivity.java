package com.ellucian.mobile.android.webframe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.widget.ShareActionProvider;
import android.widget.ShareActionProvider.OnShareTargetSelectedListener;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

public class WebframeActivity extends EllucianActivity {
	private static final String TAG = WebframeActivity.class.getSimpleName();
	private WebView webView;
	private SecurityDialogFragment securityDialogFragment;
	private SslErrorHandler handler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webframe);
		
		if (!TextUtils.isEmpty(moduleName)) {
			this.setTitle(moduleName);	
		}
		
	}
	
	protected void setWebView(WebView webView) {
		this.webView = webView;
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
	
	// This is to ensure no problems with multiple Webframe modules
	@Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null)
            webView.destroy();
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
	 
}
