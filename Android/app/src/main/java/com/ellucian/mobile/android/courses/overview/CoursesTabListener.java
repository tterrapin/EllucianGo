/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.overview;

import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;


public class CoursesTabListener<T extends Fragment> implements ActionBar.TabListener {
	private Fragment mFragment;
    private final Activity mActivity;
    private final String mTag;
    private final Class<T> mClass;
	private FragmentManager fragmentManager;
	private int fragmentContainerResId;

    /** Constructor used each time a new tab is created.
      * @param activity  The host Activity, used to instantiate the fragment
      * @param tag  The identifier tag for the fragment
      * @param clz  The fragment's Class, used to instantiate the fragment
      */
    public CoursesTabListener(Activity activity, String tag, Class<T> clz, FragmentManager fragmentManager, int fragmentContainerResId) {
        mActivity = activity;
        mTag = tag;
        mClass = clz;
        this.fragmentManager = fragmentManager;
        this.fragmentContainerResId = fragmentContainerResId;
    }

    /* The following are each of the ActionBar.TabListener callbacks */

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
    	mFragment = fragmentManager.findFragmentByTag(mTag);
        // Check if the fragment is already initialized
        if (mFragment == null) {
            // If not, instantiate and add it to the activity
            mFragment = Fragment.instantiate(mActivity, mClass.getName());

            if (fragmentContainerResId == 0) {
            	fragmentContainerResId = android.R.id.content;
            }
            ft.add(fragmentContainerResId, mFragment, mTag);
        } else {
            // If it exists, simply attach it in order to show it
            ft.attach(mFragment);
        }
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        if (mFragment != null) {
            // Detach the fragment, because another one is being attached
            ft.detach(mFragment);
        }
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // User selected the already selected tab. Usually do nothing.
    }

}
