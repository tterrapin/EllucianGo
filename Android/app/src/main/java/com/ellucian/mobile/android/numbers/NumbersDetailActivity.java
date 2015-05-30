/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.numbers;

import android.os.Bundle;
import android.util.Log;

import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;

public class NumbersDetailActivity extends EllucianDefaultDetailActivity {
	private static final String TAG = NumbersDetailActivity.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle bundle) {
		Log.d(TAG, "onCreate");
		super.onCreate(bundle);
	}
	
	@Override
	public Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
		Log.d(TAG, "getDetailFragmentClass");
		return NumbersDetailFragment.class;
	}
}

