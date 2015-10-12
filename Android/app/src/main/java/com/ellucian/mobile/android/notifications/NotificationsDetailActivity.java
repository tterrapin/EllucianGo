/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.notifications;

import android.os.Bundle;
import android.text.TextUtils;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.util.Extra;

public class NotificationsDetailActivity extends EllucianDefaultDetailActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String moduleName = getIntent().getStringExtra(Extra.MODULE_NAME);
		if(TextUtils.isEmpty(moduleName)) {
			String formattedTitle = String.format(getString(R.string.detail_page_title_format), getString(R.string.title_activity_notifications));
        	setTitle(formattedTitle);
		} else {
			String formattedTitle = String.format(getString(R.string.detail_page_title_format), moduleName);
        	setTitle(formattedTitle);
		}
	}
	

	@Override
	public Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
		return NotificationsDetailFragment.class;
	}
	
	void deleteNotification() {
		setResult(NotificationsActivity.RESULT_DELETE);
		finish();
	}

}
