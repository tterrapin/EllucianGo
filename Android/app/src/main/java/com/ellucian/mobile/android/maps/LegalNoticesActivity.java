/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.maps;

import android.os.Bundle;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.google.android.gms.common.GoogleApiAvailability;

public class LegalNoticesActivity extends EllucianActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps_legal);

		TextView legal = (TextView) findViewById(R.id.legal);

        legal.setText(GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(this));
	}
}