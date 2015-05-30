/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.announcements;

import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;

public class CourseAnnouncementsDetailActivity extends EllucianDefaultDetailActivity {

	@Override
	public Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
		return CourseAnnouncementsDetailFragment.class;
	}

}
