/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.full;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.services.CoursesFullScheduleIntentService;
import com.ellucian.mobile.android.courses.CoursesTabListener;
import com.ellucian.mobile.android.provider.EllucianContract.CourseTerms;
import com.ellucian.mobile.android.util.Utils;

import java.util.ArrayList;

public class CoursesFullScheduleActivity extends EllucianActivity {

	private static final String TAG = CoursesFullScheduleActivity.class.getSimpleName();
    private Activity activity = this;
    private ViewPager viewPager;
	private OnPageChangeListener pageChangeListener;
    private FullScheduleReceiver fullScheduleReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_courses_full_schedule);

        // Setup 2 tabs for Full and Detail view
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setSelectedTabIndicatorColor(Utils.getColorHelper(this, R.color.tab_indicator_color));

        TabLayout.Tab dailyView =  tabLayout.newTab().setText(R.string.courses_menu_daily_schedule);
        TabLayout.Tab fullView =  tabLayout.newTab().setText(R.string.courses_menu_full_schedule);
        tabLayout.addTab(dailyView, CoursesTabListener.DAILY_VIEW_TAB_INDEX, false);
        tabLayout.addTab(fullView, CoursesTabListener.FULL_VIEW_TAB_INDEX, true);
        tabLayout.setOnTabSelectedListener(new CoursesTabListener(this, getIntent()));

        viewPager = (ViewPager) findViewById(R.id.courses_full_schedule_pager);

		TabAdapter tabAdapter = new TabAdapter(getSupportFragmentManager());
		viewPager.setAdapter(tabAdapter);
		viewPager.setOffscreenPageLimit(20);
		pageChangeListener = new OnPageChangeListener(){

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				CoursesFullScheduleActivity.this.sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_SLIDE_ACTION, "Swipe Terms", null, moduleName);
			}
		};
		viewPager.addOnPageChangeListener(pageChangeListener);

        Utils.showProgressIndicator(this);
        registerFullScheduleReceiver();

        Intent outgoingIntent = new Intent(this, CoursesFullScheduleIntentService.class);
		// Pass Extras on to the Service
        outgoingIntent.putExtras(getIntent().getExtras());

		startService(outgoingIntent);

	}

    @Override
    protected void onResume() {
        super.onResume();
        registerFullScheduleReceiver();
    }

    private class TabAdapter extends FragmentPagerAdapter implements
            LoaderManager.LoaderCallbacks<Cursor> {
		ArrayList<String> lists = new ArrayList<>();
		ArrayList<Fragment> fragments = new ArrayList<>();

		public TabAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);

			CoursesFullScheduleActivity.this.getSupportLoaderManager().initLoader(0, null, this);

		}

		@Override
		public Fragment getItem(int arg0) {
			return fragments.get(arg0);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return lists.get(position);
		}

		@Override
		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
			return new CursorLoader(CoursesFullScheduleActivity.this,
					CourseTerms.CONTENT_URI, null, null, null, CourseTerms.DEFAULT_SORT);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
			if (c.moveToFirst()) {
				lists = new ArrayList<>();
				fragments = new ArrayList<>();
				do {
					String title = c
							.getString(c
									.getColumnIndex(CourseTerms.TERM_NAME));
					lists.add(title);
					CourseScheduleTermFragment fragment = new CourseScheduleTermFragment();
					Bundle bundle = new Bundle();
					bundle.putString(
							"termId",
							c.getString(c
									.getColumnIndex(CourseTerms.TERM_ID)));
					fragment.setArguments(bundle);
					fragments.add(fragment);
				} while (c.moveToNext());
			}
			notifyDataSetChanged();
			viewPager.invalidate();
            // Setup the Sliding Tabs
            TabLayout tabLayout = (TabLayout) findViewById(R.id.termTabs);
            tabLayout.setVisibility(View.VISIBLE);
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabLayout.setSelectedTabIndicatorColor(Utils.getColorHelper(CoursesFullScheduleActivity.this, R.color.transparent));

        }

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {

		}
	}

    @Override
    protected void onPause() {
        super.onPause();
        unregisterFullScheduleReceiver();
    }

    @Override
	protected void onDestroy() {
		super.onDestroy();
		viewPager.removeOnPageChangeListener(pageChangeListener);
	}

    private void registerFullScheduleReceiver() {
        if (fullScheduleReceiver == null) {
            Log.d(TAG, "Registering new service receiver");
            fullScheduleReceiver = new FullScheduleReceiver();
            IntentFilter filter = new IntentFilter(CoursesFullScheduleIntentService.ACTION_FINISHED);
            LocalBroadcastManager.getInstance(this).registerReceiver(fullScheduleReceiver, filter);
        }
    }

    private void unregisterFullScheduleReceiver() {
        if (fullScheduleReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(fullScheduleReceiver);
        }
    }

    private class FullScheduleReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean updated = intent.getBooleanExtra(CoursesFullScheduleIntentService.PARAM_OUT_DATABASE_UPDATED, false);
            Log.d("FullScheduleReceiver", "onReceive: database updated = " + updated);
            if(updated) {
                Log.d("FullScheduleReceiver.onReceive", "Courses retrieved and database updated");
                Utils.hideProgressIndicator(activity);
            }
        }

    }

}
