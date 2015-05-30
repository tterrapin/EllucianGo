/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.courses.events;

import com.ellucian.mobile.android.client.ResponseObject;

public class CourseEventsResponse implements ResponseObject<CourseEventsResponse> {
	public String person;
	public Event[] events;
	
}
