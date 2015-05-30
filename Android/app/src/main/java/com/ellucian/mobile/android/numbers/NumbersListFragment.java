/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.numbers;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.ellucian.mobile.android.app.EllucianDefaultExpandableListFragment;
import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.provider.EllucianContract.Numbers;
import com.ellucian.mobile.android.provider.EllucianContract.NumbersCategories;
import com.ellucian.mobile.android.util.Extra;

public class NumbersListFragment extends EllucianDefaultExpandableListFragment {
	private static final String TAG = NumbersListFragment.class.getSimpleName();

	public NumbersListFragment() {
		Log.d(TAG, "constructor (null)");
	}
	
	@Override
	public Bundle buildDetailBundle(Cursor cursor) {
		Log.d(TAG, "buildDetailBundle");
		
		Bundle bundle = new Bundle();
		bundle.putString(Extra.MODULE_NAME, getEllucianActivity().moduleName);
		
		String name = cursor.getString(cursor.getColumnIndex(Numbers.NUMBERS_NAME));
		String type = cursor.getString(cursor.getColumnIndex(NumbersCategories.NUMBERS_CATEGORY_NAME));
		String address = cursor.getString(cursor.getColumnIndex(Numbers.NUMBERS_ADDRESS));
		String email = cursor.getString(cursor.getColumnIndex(Numbers.NUMBERS_EMAIL));
		String phone = cursor.getString(cursor.getColumnIndex(Numbers.NUMBERS_PHONE));
        String extension = cursor.getString(cursor.getColumnIndex(Numbers.NUMBERS_EXTENSION));
        String buildingId = cursor.getString(cursor.getColumnIndex(Numbers.NUMBERS_BUILDING_ID));
		String campusId = cursor.getString(cursor.getColumnIndex(Numbers.NUMBERS_CAMPUS_ID));
		double latitude = (double) cursor.getFloat(cursor.getColumnIndex(Numbers.NUMBERS_LATITUDE));
		double longitude = (double) cursor.getFloat(cursor.getColumnIndex(Numbers.NUMBERS_LONGITUDE));
		bundle.putString("name",  name);
		bundle.putString("type", type);
		bundle.putString("address",  address);
		bundle.putString("email", email);
		bundle.putString("phone",  phone);
        bundle.putString("extension", extension);
		bundle.putString("buildingId",  buildingId);
		bundle.putString("campusId", campusId);
		bundle.putDouble("latitude", latitude);
		bundle.putDouble("longitude",  longitude);
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
	public void onStart() {
		super.onStart();
		sendView("Numbers List", getEllucianActivity().moduleName);
	}

}

