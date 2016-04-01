/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.numbers;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.ellucian.mobile.android.adapter.EllucianRecyclerAdapter;
import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.EllucianDefaultRecyclerFragment;
import com.ellucian.mobile.android.util.Extra;

public class NumbersRecyclerFragment extends EllucianDefaultRecyclerFragment {
	private static final String TAG = NumbersRecyclerFragment.class.getSimpleName();

    public NumbersRecyclerFragment() {
		Log.d(TAG, "constructor (null)");
	}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
	public Bundle buildDetailBundle(Object... objects) {
		Log.d(TAG, "buildDetailBundle");
        NumbersItemHolder infoHolder = (NumbersItemHolder) objects[0];

		Bundle bundle = new Bundle();
		bundle.putString(Extra.MODULE_NAME, getEllucianActivity().moduleName);

		bundle.putString("name", infoHolder.name);
		bundle.putString("type", infoHolder.type);
		bundle.putString("address",  infoHolder.address);
		bundle.putString("email", infoHolder.email);
		bundle.putString("phone",  infoHolder.phone);
        bundle.putString("extension", infoHolder.extension);
		bundle.putString("buildingId",  infoHolder.buildingId);
		bundle.putString("campusId", infoHolder.campusId);
		bundle.putDouble("latitude", infoHolder.latitude);
		bundle.putDouble("longitude",  infoHolder.longitude);
		return bundle;
	}

	@Override
	public Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
		Log.d(TAG, "getDetailFragmentClass");
		return NumbersDetailFragment.class;
	}

	@Override
	public Class<? extends EllucianDefaultDetailActivity> getDetailActivityClass() {
		Log.d(TAG, "getDetailActivityClass");
		return NumbersDetailActivity.class;
	}

    @Override
    public void setAdapter(EllucianRecyclerAdapter adapter) {
        super.setAdapter(adapter);
    }

	@Override
	public void onStart() {
		super.onStart();
		sendView("Numbers List", getEllucianActivity().moduleName);
	}

}

