/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.directory;

import android.view.MenuItem;

import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;


public class DirectoryDetailActivity extends EllucianDefaultDetailActivity {

    @Override
    public Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
        return DirectoryDetailFragment.class;
    }

    /** Override to check if the user selects the UP/home menu option.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, finish this Detail activity and return to Master activity.
            finish();
            return true;
        }
        return false;
    }

}
