/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.multimedia;

import android.support.v4.app.Fragment;
import android.os.Bundle;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;

public class AudioActivity extends EllucianActivity {
	
	private Fragment fragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_audio);
		
		setTitle(moduleName);
	}
	
	void setFragment(Fragment fragment) {
		this.fragment = fragment;
	}
	
}
