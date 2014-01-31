package com.ellucian.mobile.android.multimedia;

import android.app.Fragment;
import android.os.Bundle;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;

public class AudioActivity extends EllucianActivity {
	
	Fragment fragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_audio);
		
		setTitle(moduleName);
	}
	
	protected void setFragment(Fragment fragment) {
		this.fragment = fragment;
	}
	
}
