// Copyright 2014 Ellucian Company L.P and its affiliates.
package com.ellucian.mobile.android.login;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * https://code.google.com/p/android/issues/detail?id=7189#c6
 * 
 * Fix for a web view shown for web-based logins to always act like a text
 * editor. Otherwise, it will not treat focus correctly and not show the soft
 * keyboard
 * 
 * LoginWebView in the layout file must also be set to be focusable.
 * @author jkh
 * 
 */
public class LoginWebView extends WebView {

	public LoginWebView(Context context) {
		super(context);
	}

	public LoginWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LoginWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onCheckIsTextEditor() {
		return true;
	}

}