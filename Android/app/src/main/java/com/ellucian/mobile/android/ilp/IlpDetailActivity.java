/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.ilp;

import android.os.Bundle;
import android.text.TextUtils;

import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.util.Utils;

public class IlpDetailActivity extends EllucianDefaultDetailActivity {
	
	@Override
	public Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
		return IlpDetailFragment.class;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        if (TextUtils.isEmpty(moduleName)) {
            // When coming from Widget, moduleName is not known.
            String title = Utils.getStringFromPreferences(getApplicationContext(), Utils.CONFIGURATION, Utils.ILP_NAME, null);
            setTitle(title);
        } else {
            setTitle(moduleName);
        }
	}
	
}
