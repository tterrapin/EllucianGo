package com.ellucian.mobile.android.webframe;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianFragment;
import com.ellucian.mobile.android.util.Extra;

@SuppressLint("SetJavaScriptEnabled")
public class WebframeFragment extends EllucianFragment {
	private static final String TAG = WebframeFragment.class.getSimpleName();
	
	WebframeActivity webActivity;
	View rootView;
	WebView webView;	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		webActivity = (WebframeActivity)activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
		
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	rootView =  inflater.inflate(R.layout.fragment_webframe, container, false);
    	webView = (WebView) rootView.findViewById(R.id.webframe_webview);
		return rootView;	
    }
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		webActivity.setWebView(webView);
			
		webView.setWebChromeClient(new WebChromeClient());
		webView.setWebViewClient(new WebViewClient() {

		    @Override
		    public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
		    	webActivity.handleError(handler);
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
		webSettings.setDatabasePath(databasePath); 
		webSettings.setDomStorageEnabled(true);
		
		String requestUrl = getActivity().getIntent().getStringExtra(Extra.REQUEST_URL);
		Log.d(TAG, "Making request at: " + requestUrl);
		
		webView.loadUrl(requestUrl);
		
	}

	@Override
	public void onStart() {
		super.onStart();
		sendView("Display web frame", getEllucianActivity().moduleName);
	}
	
	private void sendToExternalBrowser(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	    startActivity( intent );
	}

}
