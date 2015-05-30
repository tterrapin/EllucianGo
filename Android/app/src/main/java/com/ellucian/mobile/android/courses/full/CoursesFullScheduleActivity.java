/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.full;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.services.CoursesFullScheduleIntentService;
import com.ellucian.mobile.android.courses.daily.CoursesDailyScheduleActivity;
import com.ellucian.mobile.android.provider.EllucianContract.CourseTerms;
import com.ellucian.mobile.android.view.PagerTitleStrip;

public class CoursesFullScheduleActivity extends EllucianActivity {

	private TabAdapter tabAdapter;
	private ViewPager viewPager;
	private PagerTitleStrip titleStrip;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_courses_full_schedule);
		
		viewPager = (ViewPager) findViewById(R.id.courses_full_schedule_pager);
		titleStrip = (PagerTitleStrip) findViewById(R.id.courses_full_schedule_pager_title_strip);

		tabAdapter = new TabAdapter(getFragmentManager());
		viewPager.setAdapter(tabAdapter);
		viewPager.setOffscreenPageLimit(20);
		viewPager.setOnPageChangeListener(new OnPageChangeListener(){

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
		});
		
		Intent outgoingIntent = new Intent(this, CoursesFullScheduleIntentService.class);
		// Pass Extras on to the Service
		outgoingIntent.putExtras(getIntent().getExtras());
		
		startService(outgoingIntent);
		
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.courses_full_schedule, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int itemId = item.getItemId();
    	
    	if (itemId == R.id.courses_daily_menu_daily_schedule) {
    		Intent intent = new Intent(this, CoursesDailyScheduleActivity.class);
    		// Pass Extras on to next Activity
    		intent.putExtras(getIntent().getExtras());
    		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		startActivity(intent);
    		return true;
    	}
    	return super.onOptionsItemSelected(item);
	}

	private class TabAdapter extends FragmentPagerAdapter implements
			ActionBar.TabListener, LoaderManager.LoaderCallbacks<Cursor> {
		ArrayList<String> lists = new ArrayList<String>();
		ArrayList<Fragment> fragments = new ArrayList<Fragment>();

		public TabAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);

			CoursesFullScheduleActivity.this.getLoaderManager().initLoader(0, null, this);

		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			viewPager.setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {

		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {

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
			CursorLoader cursorLoader = new CursorLoader(CoursesFullScheduleActivity.this,
					CourseTerms.CONTENT_URI, null, null, null, CourseTerms.DEFAULT_SORT);

			return cursorLoader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
			if (c.moveToFirst()) {
				lists = new ArrayList<String>();
				fragments = new ArrayList<Fragment>();
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
			titleStrip.requestLayout();
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {

		}
	}

}
