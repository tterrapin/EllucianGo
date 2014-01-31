package com.ellucian.mobile.android.notifications;

import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;

public class NotificationsDetailActivity extends EllucianDefaultDetailActivity {

	@Override
	public Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
		return NotificationsDetailFragment.class;
	}

}
