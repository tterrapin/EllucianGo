/*
 * Copyright 2016 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.directory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.client.directory.Entry;

import java.util.ArrayList;

/**
 * This activity is only used by the Courses Module when more than 1 directory match
 *  is found. A list is display of all the search matches.
 */
public class DirectoryListActivity extends EllucianActivity {

    public static final String DIRECTORY_MULTIPLE_RESULTS = "directoryMultipleResults";

    private ArrayList<Entry> entries;

    private static final String DIRECTORY_LIST_FRAGMENT = "directoryListFragment";

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory_search);
        // This activity should navigate up to whatever created it.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent incomingIntent = getIntent();
        if (incomingIntent.getParcelableArrayListExtra(DIRECTORY_MULTIPLE_RESULTS) != null) {
            entries = incomingIntent.getParcelableArrayListExtra(DIRECTORY_MULTIPLE_RESULTS);
        }

        FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
        DirectoryRecyclerFragment mainFragment = (DirectoryRecyclerFragment) manager.findFragmentByTag(DIRECTORY_LIST_FRAGMENT);

		if (mainFragment == null) {
			mainFragment = new DirectoryRecyclerFragment();
            DirectoryRecyclerAdapter adapter = new DirectoryRecyclerAdapter(this, entries);
            mainFragment.setAdapter(adapter);

			transaction.add(R.id.frame_main, mainFragment, DIRECTORY_LIST_FRAGMENT);
		} else {
			transaction.attach(mainFragment);
		}
		transaction.commit();
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
