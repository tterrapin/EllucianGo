/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.maps;

import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
// TODO: Need to clean up image cache from AQ
public class BuildingDetailActivity extends EllucianDefaultDetailActivity {

	private static final String TAG = BuildingDetailActivity.class.getSimpleName();
	private Bundle arguments;
	
	@Override
	public Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
		return BuildingDetailFragment.class;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		if(savedInstanceState == null) {
			final Intent intent = getIntent();
			arguments = intent.getExtras();
			FragmentManager manager = getSupportFragmentManager();
			EllucianDefaultDetailFragment fragment = (EllucianDefaultDetailFragment) manager.findFragmentByTag("BuildingDetailFragment");
			if (fragment == null) {
				fragment = new BuildingDetailFragment();
				fragment.setArguments(arguments);
		        manager.beginTransaction()
	               .add(R.id.detail_container, fragment)
	               .commit();
			} else {
				fragment.setArguments(arguments);
			}
		} else {
			arguments = savedInstanceState;
		}
		String name = arguments.getString(BuildingDetailFragment.ARG_NAME);
		if(!TextUtils.isEmpty(name)) {
			setTitle(name);
		}
	}
	
}
