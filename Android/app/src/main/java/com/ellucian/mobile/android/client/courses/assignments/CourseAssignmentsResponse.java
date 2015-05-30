/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.courses.assignments;

import com.ellucian.mobile.android.client.ResponseObject;

public class CourseAssignmentsResponse implements ResponseObject<CourseAssignmentsResponse> {
	public String person;
	public Assignment[] assignments;
	
}
