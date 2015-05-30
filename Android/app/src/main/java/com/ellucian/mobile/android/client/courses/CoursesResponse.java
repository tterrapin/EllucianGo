/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.courses;

import com.ellucian.mobile.android.client.ResponseObject;

public class CoursesResponse implements ResponseObject<CoursesResponse>{
	public Person person;
	public Term[] terms;

}
