/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.courses.announcements;

import com.ellucian.mobile.android.client.ResponseObject;

public class CourseAnnouncementsResponse implements ResponseObject<CourseAnnouncementsResponse> {
	public String person;
	public Item[] items;
	
}
