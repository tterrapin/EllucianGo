/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.ilp;

import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.MainActivity;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.util.Utils;

public class IlpListActivity extends EllucianActivity implements TabHost.OnTabChangeListener {
    private static final String TAG = IlpListActivity.class.getSimpleName();
    public static final String SHOW_DETAIL = "ilpShowDetail";
    public static final String SELECTED_INDEX = "ilpSelectedIndex";

    public static final String TAB_INDEX = "tabIndex";
    public static final int TAB_ASSIGNMENTS = 0;
    public static final int TAB_EVENTS = 1;
    public static final int TAB_ANNOUNCEMENTS = 2;

    private FragmentTabHost tabHost;
    private TabInfo[] tabs;

    private class TabInfo {
        public final int index;
        public final Class clazz;
        public final String name;

        public TabInfo(int index, Class clazz, String name) {
            this.index = index;
            this.clazz = clazz;
            this.name = name;
        }
    }

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tabbed_dual_pane_layout);

        setTitle(moduleName);

        View detailsFrame = findViewById(R.id.frame_extra);
        boolean dualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

        EllucianApplication app = getEllucianApp();
        if(!app.isUserAuthenticated()) {
            Log.e(TAG, "User not authenticated, sending to home.");
            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mainIntent.putExtra(MainActivity.SHOW_LOGIN, true);
            startActivity(mainIntent);
            finish();
            return;
        } else if (!dualPane) {
            clearCurrentDetailFragment();
            if (getIntent().getBooleanExtra(SHOW_DETAIL, false)) {
                // make sure to clear the request to show the detail
                getIntent().removeExtra(IlpListActivity.SHOW_DETAIL);
                Intent intent = new Intent();
                intent.setClass(this, IlpDetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                intent.putExtras(getIntent().getExtras());
                startActivity(intent);
                finish();
            }
		}

        tabs = new TabInfo[] {
                new TabInfo(TAB_ASSIGNMENTS, AssignmentsRecyclerFragment.class, getString(R.string.ilp_assignments)),
                new TabInfo(TAB_EVENTS, EventsRecyclerFragment.class, getString(R.string.ilp_events)),
                new TabInfo(TAB_ANNOUNCEMENTS, AnnouncementsRecyclerFragment.class, getString(R.string.ilp_announcements))};


        tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);

        tabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        View tabsLayout = findViewById(R.id.tabs_layout);
        tabsLayout.setBackgroundColor(Utils.getPrimaryColor(this));

        Bundle args = getIntent().getExtras();

        for (int i = 0; i < tabs.length; i++) {
            Bundle bundle = new Bundle(args);

            // Only want to send this info to the requested tab/fragment
            if (args.containsKey(IlpListActivity.SHOW_DETAIL) && args.getInt(TAB_INDEX) != i) {
                bundle.remove(IlpListActivity.SHOW_DETAIL);
                bundle.remove(IlpListActivity.SELECTED_INDEX);
            }

            View tabLayout = getLayoutInflater().inflate(R.layout.ilp_tab_layout, null, false);
            TextView textView = (TextView)tabLayout.findViewById(R.id.title);
            textView.setText(tabs[i].name);


            tabHost.addTab(tabHost.newTabSpec("tab" + tabs[i].index).setIndicator(tabLayout),
                    tabs[i].clazz, bundle);
        }

        // clear requested indexes after use
        args.remove(IlpListActivity.SHOW_DETAIL);
        args.remove(IlpListActivity.SELECTED_INDEX);

        tabHost.setOnTabChangedListener(this);

        if (args.containsKey(TAB_INDEX)) {
            tabHost.setCurrentTab(args.getInt(TAB_INDEX));
            getIntent().removeExtra(TAB_INDEX);
        }


	}

    private void clearCurrentDetailFragment() {
        EllucianDefaultDetailFragment details = (EllucianDefaultDetailFragment)
                getSupportFragmentManager().findFragmentById(R.id.frame_extra);
        if (details != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.remove(details);
            ft.commit();
        }
    }

    @Override
    public void onTabChanged(String tabId) {
        clearCurrentDetailFragment();
    }
	
}
