/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.ilp;


public class AnnouncementItemHolder extends IlpItemHolder {
	private static final String TYPE_ANNOUNCEMENT = "typeAnnouncement";
	
	public AnnouncementItemHolder(String sectionId, String sectionName, String title, String date, String displayDate,
                                  String content, String url) {
		super(TYPE_ANNOUNCEMENT, sectionId, sectionName, title, date, displayDate, content, null, url);
	}
		
	@Override
	public String getDefaultText() {
		return title;
	}

}
