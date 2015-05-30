/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.ilp;

public class EventItemHolder extends IlpItemHolder {
	public static final String TYPE_EVENT = "typeEvent";
	public String startDate;
	public String endDate;
	public boolean allDay;
	
	
	public EventItemHolder() {
	}
	
	public EventItemHolder(String sectionId, String sectionName, String title, String displayDate, String content,
                           String location, String startDate, String endDate, boolean allDay) {
		super(TYPE_EVENT, sectionId, sectionName, title, startDate, displayDate, content, location, null);
		this.startDate = startDate;
		this.endDate = endDate;
		this.allDay = allDay;
	}
		
	@Override
	public String getDefaultText() {
		return title;
	}

}
