/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.notifications;

public class Notification {
	
	public static final String STATUS_STORED = "STORED";
	public static final String STATUS_PUSHED = "PUSHED";
	public static final String STATUS_DELIVERED = "DELIVERED";
	public static final String STATUS_PULLED = "PULLED";
	public static final String STATUS_READ = "READ";
	
	public String id;
	public String title;
	public String description;	
	public String hyperlink;
	public String linkLabel;
	public String noticeDate;
	public String source;
	public String dispatchDate;
	public String mobileHeadline;
	public String expires;
	public boolean push;
	public boolean module;
	public boolean sticky = true;
	public String[] statuses;

}
