/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.events;

import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;

public class CourseEventsDetailActivity extends EllucianDefaultDetailActivity {

	@Override
	public Class<? extends  EllucianDefaultDetailFragment> getDetailFragmentClass() {
		return CourseEventsDetailFragment.class;	
	}
	
}
