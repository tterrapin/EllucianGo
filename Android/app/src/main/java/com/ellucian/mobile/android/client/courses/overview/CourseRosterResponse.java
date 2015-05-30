/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.courses.overview;

import com.ellucian.mobile.android.client.ResponseObject;

public class CourseRosterResponse implements ResponseObject<CourseRosterResponse> {
	public String sectionId;
	public RosterStudent[] activeStudents;
}
