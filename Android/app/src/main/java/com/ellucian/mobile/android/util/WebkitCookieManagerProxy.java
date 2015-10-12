// Copyright 2014 Ellucian Company L.P and its affiliates.
package com.ellucian.mobile.android.util;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WebkitCookieManagerProxy extends CookieManager 
{
    private final android.webkit.CookieManager webkitCookieManager;

    public WebkitCookieManagerProxy()
    {
        this(null, null);
    }

    public WebkitCookieManagerProxy(CookieStore store, CookiePolicy cookiePolicy)
    {
        super(null, cookiePolicy);

        this.webkitCookieManager = android.webkit.CookieManager.getInstance();
    }

    @Override
    public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException 
    {
        // make sure our args are valid
        if ((uri == null) || (responseHeaders == null)) return;

        // save our url once
        String url = uri.toString();

		List<String> cookieList = responseHeaders.get("Set-Cookie");
	    if (cookieList != null) {
	        for (String cookieString : cookieList) {
 
	        	String urlToUse = url;
	        	/*
	        	if (cookieString.contains("Domain")) {
					String[] domainSplit = cookieString.split("Domain=");
					String domainHalf = domainSplit[1]; 
					String[] valueSplit = domainHalf.split(";");
					urlToUse = valueSplit[0];
					
				} 
	        	*/
                this.webkitCookieManager.setCookie(urlToUse, cookieString);
            }
        }
    }

    @Override
    public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException 
    {
        // make sure our args are valid
        if ((uri == null) || (requestHeaders == null)) throw new IllegalArgumentException("Argument is null");

        // save our url once
        String url = uri.toString();

        // prepare our response
        Map<String, List<String>> res = new java.util.HashMap<String, List<String>>();

        // get the cookie
        String cookie = this.webkitCookieManager.getCookie(url);

        // return it
        if (cookie != null) res.put("Cookie", Arrays.asList(cookie));
        return res;
    }

    @Override
    public CookieStore getCookieStore() 
    {
        // we don't want anyone to work with this cookie store directly
        throw new UnsupportedOperationException();
    }
}