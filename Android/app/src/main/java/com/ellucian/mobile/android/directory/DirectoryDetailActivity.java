/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.directory;

import android.content.res.Configuration;
import android.os.Bundle;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;


public class DirectoryDetailActivity extends EllucianActivity {
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_detail);
        
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && 
				(getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= 
					        Configuration.SCREENLAYOUT_SIZE_LARGE) {
            // If the screen is now in landscape mode, we can show the
            // dialog in-line with the list so we don't need this activity.
            finish();
            return;
        }
        
        if (savedInstanceState == null) {
            // During initial setup, plug in the details fragment.

        	DirectoryDetailFragment details = DirectoryDetailFragment.newInstance(getIntent().getExtras());
            getFragmentManager().beginTransaction().add(R.id.detail_container, details).commit();
        }
    }

	
}
