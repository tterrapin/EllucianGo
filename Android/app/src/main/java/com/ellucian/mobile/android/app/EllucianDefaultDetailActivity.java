/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.app;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.ellucian.elluciango.R;

/**
 * This Activity is the container of the detail fragment if show in single pane (portrait) mode.
 * 
 * @author Jared Higley
 *
 */
public class EllucianDefaultDetailActivity extends EllucianActivity {

	/** A subclass can override this method to change the support for different screen sizes and modes */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_default_detail);

        // Only for large screens in landscape
        View detailsFrame = findViewById(R.id.frame_extra);
        boolean mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

        if (mDualPane) {
//		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE &&
//				(getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=
//					        Configuration.SCREENLAYOUT_SIZE_LARGE) {
//
			// If the screen is now in landscape mode, we can show the
			// dialog in-line with the list so we don't need this activity.
			finish();
			return;
		}

		if (savedInstanceState == null) {
			// During initial setup, plug in the details fragment.
			EllucianDefaultDetailFragment details = getDetailFragment(getIntent().getExtras(), -1);

			getSupportFragmentManager().beginTransaction().add(R.id.detail_container, details).commit();
		}
	}
	
	/** Override to return a subclass of EllucianDefaultDetailFragment created by EllucianDefaultDetailFragment.newInstance
     *  See EllucianDefaultDetailFragment.newInstance for more information
     *  If you are only making changes to the class name, override getDetailFragmentClass() instead
     */
	private EllucianDefaultDetailFragment getDetailFragment(Bundle args, int index) {
		
		return EllucianDefaultDetailFragment.newInstance(this, 
				getDetailFragmentClass().getName(), args, index);
				
	}
	
	/** Override to return the class of an EllucianDefaultDetailFragment subclass */
	protected Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
		return EllucianDefaultDetailFragment.class;	
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
