/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.assignments;

import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;

public class CourseAssignmentsDetailActivity extends EllucianDefaultDetailActivity {

	@Override
	public Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
		return CourseAssignmentsDetailFragment.class;
	}
	
}
