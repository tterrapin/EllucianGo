/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.maps;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;

public class BuildingInfoDetailActivity extends EllucianActivity {

	public static final String TAG = BuildingInfoDetailActivity.class.getSimpleName();
	BuildingDetailFragment detailFragment;
	
	Bundle arguments;
	
	public BuildingInfoDetailActivity() {
	}

	/*
	 * BuildingInfoDetailActivity provides a mechanism for the MapsActivity onInfoWindowClick
	 * to launch the Building Details activity directly, outside the effects of the 
	 * EllucianDefaultDualPane functionality used in the BuildingList menu option.
	 * 
	 */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.activity_map_info_building_detail);
		if (bundle == null) {
			final Intent intent = getIntent();
			arguments = intent.getExtras();
		} else {
			arguments = bundle;
		}
		detailFragment = (BuildingDetailFragment) getFragmentManager().findFragmentById(R.id.frame_extra);
		if (detailFragment == null) {
			detailFragment = new BuildingDetailFragment();
			detailFragment.setArguments(arguments);
			getFragmentManager()
			 .beginTransaction()
			 .add(R.id.frame_extra, detailFragment)
			 .commit();
		}
		String name = arguments.getString(BuildingDetailFragment.ARG_NAME);
		if(!TextUtils.isEmpty(name)) {
			setTitle(name);
		}
	}
}
