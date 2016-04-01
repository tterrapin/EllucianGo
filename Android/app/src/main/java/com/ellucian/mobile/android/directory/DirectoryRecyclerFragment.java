/*
 * Copyright 2016 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.directory;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.adapter.EllucianRecyclerAdapter;
import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.EllucianDefaultRecyclerFragment;
import com.ellucian.mobile.android.client.directory.Entry;

public class DirectoryRecyclerFragment extends EllucianDefaultRecyclerFragment {
    private static final String TAG = DirectoryRecyclerFragment.class.getSimpleName();


    public DirectoryRecyclerFragment() {
        Log.d(TAG, "constructor (null)");
    }

    public EllucianRecyclerAdapter getAdapter() {
        return adapter;
    }

    // need to override it scope to public
    @Override
    public void setAdapter(EllucianRecyclerAdapter adapter) {
        super.setAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    protected Bundle buildDetailBundle(Object... objects) {
        Entry infoHolder = (Entry) objects[0];
        return infoHolder.buildBundle();
    }

    // Override to clear the detail bundle.
    @Override
    public void clearCurrentDetailFragment() {
        EllucianDefaultDetailFragment details = (EllucianDefaultDetailFragment)
                getFragmentManager().findFragmentById(R.id.frame_extra);
        if (details != null) {
            detailBundle = null;
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.remove(details);
            ft.commit();
        }
    }

    @Override
    public Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
        return DirectoryDetailFragment.class;
    }

    @Override
    public Class<? extends EllucianDefaultDetailActivity> getDetailActivityClass() {
        return DirectoryDetailActivity.class;
    }

    @Override
	public void onStart() {
		super.onStart();
		sendView("Directory page", getEllucianActivity().moduleName);
	}

}
