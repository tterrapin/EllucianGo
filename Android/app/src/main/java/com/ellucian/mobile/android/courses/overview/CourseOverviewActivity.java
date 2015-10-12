/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.overview;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.services.CourseDetailsIntentService;
import com.ellucian.mobile.android.client.services.CourseGradesIntentService;
import com.ellucian.mobile.android.client.services.CourseRosterIntentService;
import com.ellucian.mobile.android.directory.DirectoryCategoriesFragment;
import com.ellucian.mobile.android.directory.DirectoryListActivity;
import com.ellucian.mobile.android.maps.MapUtils;
import com.ellucian.mobile.android.provider.EllucianContract.CourseInstructors;
import com.ellucian.mobile.android.provider.EllucianContract.CourseRoster;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class CourseOverviewActivity extends EllucianActivity  {
	private static final String TAG = CourseOverviewActivity.class.getSimpleName();
    private boolean moreTabPreviouslyAdded;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_course_overview);
	    
	    Bundle incomingExtras = getIntent().getExtras();
		String courseName = incomingExtras.getString(Extra.COURSES_NAME);
		String sectionNumber = incomingExtras.getString(Extra.COURSES_SECTION_NUMBER);
	    // Determine if the roster is shown
	    boolean isInstructor = incomingExtras.getBoolean(Extra.COURSES_IS_INSTRUCTOR);
		String rosterVisibility = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, 
				Utils.COURSE_ROSTER_VISIBILITY, "");

		boolean showRoster = false;		
		if (TextUtils.isEmpty(rosterVisibility) || rosterVisibility.equals("none")) {
			showRoster = false;
		} else if (rosterVisibility.equals("both")){
			showRoster = true;
		} else if (rosterVisibility.equals("faculty") && isInstructor){
			showRoster = true;
		}

	    setTitle(getString(R.string.default_course_section_format, courseName, sectionNumber));

        ViewPager viewPager = (ViewPager) findViewById(R.id.courses_course_overview_viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager, showRoster);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);

	    // Start services for each tab info
		Intent detailsIntent = new Intent(this, CourseDetailsIntentService.class);
		detailsIntent.putExtras(incomingExtras);
		startService(detailsIntent);
		
		Intent gradesIntent = new Intent(this, CourseGradesIntentService.class);
		gradesIntent.putExtras(incomingExtras);
		startService(gradesIntent);
		
		if (showRoster) {
			Intent rosterIntent = new Intent(this, CourseRosterIntentService.class);
			rosterIntent.putExtras(incomingExtras);
			startService(rosterIntent);
		}

        tabLayout.setupWithViewPager(viewPager);
	}

    private void setupViewPager(ViewPager viewPager, Boolean showRoster) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new CourseDetailsFragment(), getString(R.string.course_details));
        adapter.addFragment(new CourseGradesFragment(), getString(R.string.course_grades));
        if (showRoster) {
            adapter.addFragment(new CourseRosterFragment(), getString(R.string.course_roster));
        }
        boolean isILPConfigured = getIntent().getExtras().containsKey(Extra.COURSES_ILP_URL);
        if (!moreTabPreviouslyAdded && isILPConfigured) {
            moreTabPreviouslyAdded = true;
            adapter.addFragment(new CourseMoreFragment(), getString(R.string.course_more));
            viewPager.setAdapter(adapter);
        }
        viewPager.setAdapter(adapter);
    }

	// This method is trigger in the course_details_faculty_row layout
	public void findFacultyMember(View view) {
    	String facultySearchUrl = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.DIRECTORY_FACULTY_SEARCH_URL, null);
    	if (Utils.isDirectoryPresent(this) && !TextUtils.isEmpty(facultySearchUrl)) {
    		String instructorFormattedName = (String) ((TextView) view).getText();
    		
    		Cursor cursor = getContentResolver().query(CourseInstructors.CONTENT_URI,
    							new String[] { CourseInstructors.INSTRUCTOR_FIRST_NAME, CourseInstructors.INSTRUCTOR_LAST_NAME}, 
    							CourseInstructors.INSTRUCTOR_FORMATTED_NAME + " = ?", 
    							new String[] { instructorFormattedName }, 
    							null);
    		
    		cursor.moveToFirst();
    		String firstName = cursor.getString(cursor.getColumnIndex(CourseInstructors.INSTRUCTOR_FIRST_NAME));
    		String lastName = cursor.getString(cursor.getColumnIndex(CourseInstructors.INSTRUCTOR_LAST_NAME));
    		cursor.close();
    		
    		String query = "";
    		
    		if (!TextUtils.isEmpty(firstName)) {
    			query += firstName;
    		} 
    		
    		if (!TextUtils.isEmpty(lastName)) {
    			if (!TextUtils.isEmpty(query)) {
    				query += " ";
    			}
    			query += lastName;
    		} 
    		sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_LIST_SELECT, "Faculty detail", null, moduleName);
    		sendDirectoryQueryIntent(DirectoryCategoriesFragment.DIRECTORY_TYPE_FACULTY,  query);
    	} else {
    		noModuleToast(R.string.course_details_no_directory);
    	}
    }
	
	// This method is trigger in the course_roster_row layout
	public void findRosterStudent(View view) {
		String studentSearchUrl = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.DIRECTORY_STUDENT_SEARCH_URL, null);
		if (Utils.isDirectoryPresent(this) && !TextUtils.isEmpty(studentSearchUrl)) {
            String studentFormattedName = (String) ((TextView) view).getText();

            Cursor cursor = getContentResolver().query(CourseRoster.CONTENT_URI,
                    new String[] {CourseRoster.ROSTER_FIRST_NAME, CourseRoster.ROSTER_LAST_NAME},
                    CourseRoster.ROSTER_FORMATTED_NAME + " = ?",
                    new String[] { studentFormattedName },
                    null);
            cursor.moveToFirst();
            String firstName = cursor.getString(cursor.getColumnIndex(CourseRoster.ROSTER_FIRST_NAME));
            String lastName = cursor.getString(cursor.getColumnIndex(CourseRoster.ROSTER_LAST_NAME));
            cursor.close();

            String query = "";

            if (!TextUtils.isEmpty(firstName)) {
                query += firstName;
            }

            if (!TextUtils.isEmpty(lastName)) {
                if (!TextUtils.isEmpty(query)) {
                    query += " ";
                }
                query += lastName;
            }
            sendDirectoryQueryIntent(DirectoryCategoriesFragment.DIRECTORY_TYPE_STUDENT, query);
    	} else {
    		noModuleToast(R.string.course_details_no_directory);
    	}
	}
	
	private void sendDirectoryQueryIntent(String queryType, String query) {
		if (TextUtils.isEmpty(queryType)) {
			queryType = DirectoryCategoriesFragment.DIRECTORY_TYPE_ALL;
		}
		
    	Log.d(TAG, "Sending query to database for : " + query);
    	Intent intent = new Intent(this, DirectoryListActivity.class);
    	intent.setAction(Intent.ACTION_SEARCH);
    	intent.putExtra(Extra.DIRECTORY_TYPE, queryType);
    	intent.putExtra(Extra.DIRECTORY_QUERY, query);

        switch (queryType) {
            case DirectoryCategoriesFragment.DIRECTORY_TYPE_STUDENT :
                intent.putExtra(Extra.DIRECTORY_STUDENT_URL,
                        Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.DIRECTORY_STUDENT_SEARCH_URL, null));
                break;
            case DirectoryCategoriesFragment.DIRECTORY_TYPE_FACULTY :
    		    intent.putExtra(Extra.DIRECTORY_FACULTY_URL,
                        Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.DIRECTORY_FACULTY_SEARCH_URL, null));
                break;
    	    default:
    		    intent.putExtra(Extra.REQUEST_URL,
                        Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.DIRECTORY_ALL_SEARCH_URL, null));
                break;
    	}
    	
    	startActivity(intent);
    }
	
	public void openBuildingDetail(String buildingId, String buildingName) {
		if (Utils.isMapPresent(this)) {
			Intent intent = MapUtils.buildBuildingDetailIntent(this, buildingName, null, null, null, null, null, null, null, null, 
					buildingId, null, null, null, false, true);
			startActivity(intent);
		} else {
			noModuleToast(R.string.course_details_no_maps);
		}
	}
	
	private void noModuleToast(int stringResId) {
		Toast emptyMessage = Toast.makeText(this, stringResId, Toast.LENGTH_SHORT);
		emptyMessage.setGravity(Gravity.CENTER, 0, 0);
		emptyMessage.show();
	}

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }
}
