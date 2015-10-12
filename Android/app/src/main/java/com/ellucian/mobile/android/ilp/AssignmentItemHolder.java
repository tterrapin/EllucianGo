/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.ilp;


public class AssignmentItemHolder extends IlpItemHolder {
	public static final String TYPE_ASSIGNMENT = "typeAssignment";
	
	public AssignmentItemHolder(String sectionId, String sectionName, String title, String date, String displayDate,
                                String content, String url) {
		super(TYPE_ASSIGNMENT, sectionId, sectionName, title, date, displayDate, content, null, url);
	}
		
	@Override
	public String getDefaultText() {
		return title;
	}

}
