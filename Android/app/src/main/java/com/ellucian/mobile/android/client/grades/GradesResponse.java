/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.grades;

import com.ellucian.mobile.android.client.ResponseObject;

public class GradesResponse  implements ResponseObject<GradesResponse>{
	public Student student;
	public Term[] terms;

}
