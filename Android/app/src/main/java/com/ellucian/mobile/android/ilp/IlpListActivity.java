/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.ilp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TabHost;

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
    private AssignmentsRecyclerFragment assignmentsRecyclerFragment;
    private EventsRecyclerFragment eventsRecyclerFragment;
    private AnnouncementsRecyclerFragment announcementsRecyclerFragment;

    private int currentTab;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_dual_pane);

        if (TextUtils.isEmpty(moduleName)) {
            // When coming from Widget, moduleName is not known.
            String title = Utils.getStringFromPreferences(getApplicationContext(), Utils.CONFIGURATION, Utils.ILP_NAME, null);
            setTitle(title);
        } else {
            setTitle(moduleName);
        }

        View detailsFrame = findViewById(R.id.frame_extra);
        boolean dualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

        EllucianApplication app = getEllucianApp();
        if (!app.isUserAuthenticated()) {
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

        Bundle args = getIntent().getExtras();
        FragmentManager manager = getSupportFragmentManager();
        assignmentsRecyclerFragment = (AssignmentsRecyclerFragment) manager.findFragmentByTag("AssignmentsRecyclerFragment");
        if (assignmentsRecyclerFragment == null) {
            assignmentsRecyclerFragment = new AssignmentsRecyclerFragment();
            Bundle bundle = cleanBundle(args, TAB_ASSIGNMENTS);
            assignmentsRecyclerFragment.setArguments(bundle);
        }

        eventsRecyclerFragment = (EventsRecyclerFragment) manager.findFragmentByTag("EventsRecyclerFragment");
        if (eventsRecyclerFragment == null) {
            eventsRecyclerFragment = new EventsRecyclerFragment();
            Bundle bundle = cleanBundle(args, TAB_EVENTS);
            eventsRecyclerFragment.setArguments(bundle);
        }

        announcementsRecyclerFragment = (AnnouncementsRecyclerFragment) manager.findFragmentByTag("AnnouncementsRecyclerFragment");
        if (announcementsRecyclerFragment == null) {
            announcementsRecyclerFragment = new AnnouncementsRecyclerFragment();
            Bundle bundle = cleanBundle(args, TAB_ANNOUNCEMENTS);
            announcementsRecyclerFragment.setArguments(bundle);
        }

        // Setup the 3 tabs in TabLayout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setSelectedTabIndicatorColor(Utils.getColorHelper(this, R.color.tab_indicator_color));

        TabLayout.Tab assignmentsTab = tabLayout.newTab().setText(R.string.ilp_assignments);
        TabLayout.Tab eventsTab = tabLayout.newTab().setText(R.string.ilp_events);
        TabLayout.Tab announcementsTab = tabLayout.newTab().setText(R.string.ilp_announcements);
        tabLayout.addTab(assignmentsTab, TAB_ASSIGNMENTS);
        tabLayout.addTab(eventsTab, TAB_EVENTS);
        tabLayout.addTab(announcementsTab, TAB_ANNOUNCEMENTS);
        tabLayout.setOnTabSelectedListener(new MyTabListener(this, R.id.frame_main));

        // clear requested indexes after use
        args.remove(IlpListActivity.SHOW_DETAIL);
        args.remove(IlpListActivity.SELECTED_INDEX);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("previousSelected")) {
                currentTab = savedInstanceState.getInt("previousSelected");
            }
        }

        if (args.containsKey(TAB_INDEX)) {
            currentTab = args.getInt(TAB_INDEX);
            getIntent().removeExtra(TAB_INDEX);
        }

        tabLayout.getTabAt(currentTab).select();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int mCurrentTab = currentTab;
        outState.putInt("previousSelected", mCurrentTab);

    }

    private Bundle cleanBundle(Bundle args, int tabIndex) {
        Bundle bundle = new Bundle(args);

        // Only want to send this info to the requested tab/fragment
        if (args.containsKey(IlpListActivity.SHOW_DETAIL) && args.getInt(TAB_INDEX) != tabIndex) {
            bundle.remove(IlpListActivity.SHOW_DETAIL);
            bundle.remove(IlpListActivity.SELECTED_INDEX);
        }
        return bundle;
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

    private void clearMainFragment() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        Fragment mainFrame = manager.findFragmentById(R.id.frame_main);

        if (mainFrame != null) {
            ft.detach(mainFrame);
        }
        ft.commitAllowingStateLoss();
    }

    private void clearDetailFragment() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        Fragment extraFrame = manager.findFragmentById(R.id.frame_extra);

        if (extraFrame != null) {
            ft.detach(extraFrame);
        }

        ft.commitAllowingStateLoss();
    }

    private class MyTabListener implements TabLayout.OnTabSelectedListener {
        private final FragmentManager fragmentManager;
        private int fragmentContainerResId;
        private final Context mContext;
        private String mTag;
        private Class<? extends Fragment> mClass;
        private Fragment mFragment;

        public MyTabListener(Context context, int fragmentContainerResId) {
            fragmentManager = getSupportFragmentManager();
            mContext = context;
            this.fragmentContainerResId = fragmentContainerResId;
        }

        /* The following are each of the TabLayout.OnTabSelectedListener callbacks */
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            if (tab.getTag() != null) {
                if (!((Boolean) tab.getTag())) {
                    return;
                }
            }

            determineTab(tab.getPosition());
            clearMainFragment();
            clearDetailFragment();
            FragmentTransaction ft = fragmentManager.beginTransaction();

            // Check if the fragment is in the Fragment Manager
            if (fragmentManager.findFragmentByTag(mTag) == null) {
                if (mFragment == null) {
                    mFragment = Fragment.instantiate(mContext, mClass.getName());
                }

                if (fragmentContainerResId == 0) {
                    fragmentContainerResId = android.R.id.content;
                }

                ft.add(fragmentContainerResId, mFragment, mTag);

            } else {
                // If it exists, simply attach it in order to show it
                ft.attach(mFragment);
            }
            currentTab = tab.getPosition();
            ft.commit();
        }

        private void determineTab(int tabPosition) {
            switch (tabPosition) {
                case TAB_ANNOUNCEMENTS: // Announcements
                    mClass = AnnouncementsRecyclerFragment.class;
                    mTag = "AnnouncementsRecyclerFragment";
                    mFragment = announcementsRecyclerFragment;
                    break;
                case TAB_EVENTS: // Events
                    mClass = EventsRecyclerFragment.class;
                    mTag = "EventsRecyclerFragment";
                    mFragment = eventsRecyclerFragment;
                    break;
                case TAB_ASSIGNMENTS:
                    mClass = AssignmentsRecyclerFragment.class;
                    mTag = "AssignmentsRecyclerFragment";
                    mFragment = assignmentsRecyclerFragment;
                    break;
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            onTabSelected(tab);
        }
    }
	
}
