/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.notifications;

import android.os.Bundle;
import android.text.TextUtils;

import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.util.Utils;

public class NotificationsDetailActivity extends EllucianDefaultDetailActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        if (TextUtils.isEmpty(moduleName)) {
            // When tapping on a device Notification, moduleName is not known.
            String title = Utils.getStringFromPreferences(getApplicationContext(), Utils.CONFIGURATION, Utils.NOTIFICATION_MODULE_NAME, null);
            setTitle(title);
        } else {
            setTitle(moduleName);
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
