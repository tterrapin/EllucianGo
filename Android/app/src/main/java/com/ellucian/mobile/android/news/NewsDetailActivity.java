/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.news;

import android.os.Bundle;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.util.Extra;

public class NewsDetailActivity extends EllucianDefaultDetailActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        String formattedTitle = String.format(getString(R.string.detail_page_title_format), getIntent().getStringExtra(Extra.MODULE_NAME));
        this.setTitle(formattedTitle);
	}
	
	
	@Override
	public Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
		return NewsDetailFragment.class;
	}
}
