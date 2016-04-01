/*
 * Copyright 2016 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses.overview;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.client.directory.DirectoryResponse;
import com.ellucian.mobile.android.client.directory.Entry;
import com.ellucian.mobile.android.client.services.CourseAnnouncementsIntentService;
import com.ellucian.mobile.android.client.services.CourseAssignmentsIntentService;
import com.ellucian.mobile.android.client.services.CourseDetailsIntentService;
import com.ellucian.mobile.android.client.services.CourseEventsIntentService;
import com.ellucian.mobile.android.client.services.CourseGradesIntentService;
import com.ellucian.mobile.android.client.services.CourseRosterIntentService;
import com.ellucian.mobile.android.courses.announcements.CourseAnnouncementsListFragment;
import com.ellucian.mobile.android.courses.assignments.CourseAssignmentsListFragment;
import com.ellucian.mobile.android.courses.events.CourseEventsListFragment;
import com.ellucian.mobile.android.directory.DirectoryActivity;
import com.ellucian.mobile.android.directory.DirectoryDetailActivity;
import com.ellucian.mobile.android.directory.DirectoryListActivity;
import com.ellucian.mobile.android.maps.MapUtils;
import com.ellucian.mobile.android.provider.EllucianContract.CourseInstructors;
import com.ellucian.mobile.android.provider.EllucianContract.CourseRoster;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

public class CourseOverviewActivity extends EllucianActivity  {
	private static final String TAG = CourseOverviewActivity.class.getSimpleName();
    private Activity activity = this;
    private CourseDetailsReceiver courseDetailsReceiver;
    boolean isILPConfigured;
    boolean showRoster;
    public int tabIncrementer = 0;
    private CourseDetailsFragment courseDetailsFragment;
    private CourseGradesFragment courseGradesFragment;
    private CourseRosterFragment courseRosterFragment;
    private CourseAssignmentsListFragment courseAssignmentsListFragment;
    private CourseEventsListFragment courseEventsListFragment;
    private CourseAnnouncementsListFragment courseAnnouncementsListFragment;

    boolean isLegacy = false;
    private SearchDirectoryInfoTask directoryTask;

    private int currentTab;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_course_overview);

        Bundle incomingExtras = getIntent().getExtras();
		String courseName = incomingExtras.getString(Extra.COURSES_NAME);
		String sectionNumber = incomingExtras.getString(Extra.COURSES_SECTION_NUMBER);
        String courseId = incomingExtras.getString(Extra.COURSES_COURSE_ID);
        String termId = incomingExtras.getString(Extra.COURSES_TERM_ID);
	    // Determine if the roster is shown
	    boolean isInstructor = incomingExtras.getBoolean(Extra.COURSES_IS_INSTRUCTOR);
		String rosterVisibility = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, 
				Utils.COURSE_ROSTER_VISIBILITY, "");

		showRoster = false;
		if (TextUtils.isEmpty(rosterVisibility) || rosterVisibility.equals("none")) {
			showRoster = false;
		} else if (rosterVisibility.equals("both")){
			showRoster = true;
		} else if (rosterVisibility.equals("faculty") && isInstructor){
			showRoster = true;
		}

        isILPConfigured = getIntent().getExtras().containsKey(Extra.COURSES_ILP_URL);

	    setTitle(getString(R.string.default_course_section_format, courseName, sectionNumber));

        FragmentManager manager = getSupportFragmentManager();
        setupFragments(manager, incomingExtras, showRoster);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setSelectedTabIndicatorColor(Utils.getColorHelper(this, R.color.tab_indicator_color));

        // Add tabs
        TabLayout.Tab detailsTab = tabLayout.newTab().setText(R.string.course_details);
        tabLayout.addTab(detailsTab, tabIncrementer);
        TabLayout.Tab gradesTab = tabLayout.newTab().setText(R.string.course_grades);
        tabLayout.addTab(gradesTab, ++tabIncrementer);
        if (showRoster) {
            TabLayout.Tab rosterTab = tabLayout.newTab().setText(R.string.course_roster);
            tabLayout.addTab(rosterTab, ++tabIncrementer);
        }
        if (isILPConfigured) {
            addIlpTabs(tabLayout);
        }

        tabLayout.setOnTabSelectedListener(new MyTabListener(this, R.id.course_overview_frame));

        registerCourseDetailsReceiver();

        if (savedInstanceState != null && savedInstanceState.containsKey("previousSelected")) {
            currentTab = savedInstanceState.getInt("previousSelected");
        } else {
            Utils.showProgressIndicator(this);

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

            // Get a jump start on fetching ILP data
            if (isILPConfigured) {
                Log.d(TAG, "Pre fetch ILP data for term/course: " + termId + "/" + courseId);
                Intent assignmentsIntent = new Intent(this, CourseAssignmentsIntentService.class);
                assignmentsIntent.putExtras(incomingExtras);
                startService(assignmentsIntent);

                Intent announcementsIntent = new Intent(this, CourseAnnouncementsIntentService.class);
                announcementsIntent.putExtras(incomingExtras);
                startService(announcementsIntent);

                Intent eventsIntent = new Intent(this, CourseEventsIntentService.class);
                eventsIntent.putExtras(incomingExtras);
                startService(eventsIntent);
            }
        }

        tabLayout.getTabAt(currentTab).select();
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int mCurrentTab = currentTab;
        outState.putInt("previousSelected", mCurrentTab);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerCourseDetailsReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterCourseDetailsReceiver();
    }

    private void setupFragments(FragmentManager manager, Bundle bundle, Boolean showRoster) {
        courseDetailsFragment = (CourseDetailsFragment) manager.findFragmentByTag("CourseDetailsFragment");
        if (courseDetailsFragment == null) {
            courseDetailsFragment = new CourseDetailsFragment();
            courseDetailsFragment.setArguments(bundle);
        }

        courseGradesFragment = (CourseGradesFragment) manager.findFragmentByTag("CourseGradesFragment");
        if (courseGradesFragment == null) {
            courseGradesFragment = new CourseGradesFragment();
            courseGradesFragment.setArguments(bundle);
        }

        if (showRoster) {
            courseRosterFragment = (CourseRosterFragment) manager.findFragmentByTag("CourseRosterFragment");
            if (courseRosterFragment == null) {
                courseRosterFragment = new CourseRosterFragment();
                courseRosterFragment.setArguments(bundle);
            }
        }

        if (isILPConfigured) {
            courseAssignmentsListFragment = (CourseAssignmentsListFragment) manager.findFragmentByTag("CourseAssignmentsListFragment");
            if (courseAssignmentsListFragment == null) {
                courseAssignmentsListFragment = new CourseAssignmentsListFragment();
                courseAssignmentsListFragment.setArguments(bundle);
            }

            courseEventsListFragment = (CourseEventsListFragment) manager.findFragmentByTag("CourseEventsListFragment");
            if (courseEventsListFragment == null) {
                courseEventsListFragment = new CourseEventsListFragment();
                courseEventsListFragment.setArguments(bundle);
            }

            courseAnnouncementsListFragment = (CourseAnnouncementsListFragment) manager.findFragmentByTag("CourseAnnouncementsListFragment");
            if (courseAnnouncementsListFragment == null) {
                courseAnnouncementsListFragment = new CourseAnnouncementsListFragment();
                courseAnnouncementsListFragment.setArguments(bundle);
            }
        }
    }

    private void addIlpTabs(TabLayout tabLayout) {
        TabLayout.Tab assignmentsTab = tabLayout.newTab().setText(R.string.course_assignments);
        tabLayout.addTab(assignmentsTab, ++tabIncrementer);

        TabLayout.Tab eventsTab = tabLayout.newTab().setText(R.string.course_events);
        tabLayout.addTab(eventsTab, ++tabIncrementer);

        TabLayout.Tab announcementsTab = tabLayout.newTab().setText(R.string.course_announcements);
        tabLayout.addTab(announcementsTab, ++tabIncrementer);
    }

	// This method is trigger in the course_details_faculty_row layout
	public void findFacultyMember(View view) {
    	if (Utils.isDirectoryPresent(this)) {
    		String instructorFormattedName = (String) ((TextView) view).getText();
    		
    		Cursor cursor = getContentResolver().query(CourseInstructors.CONTENT_URI,
                    new String[]{CourseInstructors.INSTRUCTOR_FIRST_NAME, CourseInstructors.INSTRUCTOR_LAST_NAME, CourseInstructors.INSTRUCTOR_ID},
                    CourseInstructors.INSTRUCTOR_FORMATTED_NAME + " = ?",
                    new String[]{instructorFormattedName},
                    null);
    		
    		cursor.moveToFirst();
    		String firstName = cursor.getString(cursor.getColumnIndex(CourseInstructors.INSTRUCTOR_FIRST_NAME));
    		String lastName = cursor.getString(cursor.getColumnIndex(CourseInstructors.INSTRUCTOR_LAST_NAME));
            String personId = cursor.getString(cursor.getColumnIndex(CourseInstructors.INSTRUCTOR_ID));
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

            if (TextUtils.isEmpty(query)) {
                query = instructorFormattedName;
            }

    		sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_LIST_SELECT, "Faculty detail", null, moduleName);
    		buildDirectoryQuery(DirectoryActivity.DIRECTORY_TYPE_FACULTY, query, personId);
    	} else {
    		noModuleToast(R.string.course_details_no_directory);
    	}
    }
	
	// This method is trigger in the course_roster_row layout
	public void findRosterStudent(View view) {
		if (Utils.isDirectoryPresent(this)) {
            String studentFormattedName = (String) ((TextView) view).getText();

            Cursor cursor = getContentResolver().query(CourseRoster.CONTENT_URI,
                    new String[] {CourseRoster.ROSTER_FIRST_NAME, CourseRoster.ROSTER_LAST_NAME, CourseRoster.ROSTER_STUDENT_ID},
                    CourseRoster.ROSTER_FORMATTED_NAME + " = ?",
                    new String[] { studentFormattedName },
                    null);
            cursor.moveToFirst();
            String firstName = cursor.getString(cursor.getColumnIndex(CourseRoster.ROSTER_FIRST_NAME));
            String lastName = cursor.getString(cursor.getColumnIndex(CourseRoster.ROSTER_LAST_NAME));
            String personId = cursor.getString(cursor.getColumnIndex(CourseRoster.ROSTER_STUDENT_ID));
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

            if (TextUtils.isEmpty(query)) {
                query = studentFormattedName;
            }

            buildDirectoryQuery(DirectoryActivity.DIRECTORY_TYPE_STUDENT, query, personId);
    	} else {
    		noModuleToast(R.string.course_details_no_directory);
    	}
	}
	
	private void buildDirectoryQuery(String queryType, String query, String personId) {
    	Log.d(TAG, "Build directory query for: " + query);

        String codeBaseVersion = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.MOBILESERVER_CODEBASE_VERSION, null);
        String directorySearchUrl = "";

        if (TextUtils.isEmpty(codeBaseVersion)) {
            // This is a pre-4.5 mobile server. Must fall-back to use older style URLs for Fac and Stu directories.
            isLegacy = true;
            switch (queryType) {
                case DirectoryActivity.DIRECTORY_TYPE_STUDENT :
                    directorySearchUrl = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.DIRECTORY_STUDENT_SEARCH_URL, null);
                    break;
                case DirectoryActivity.DIRECTORY_TYPE_FACULTY :
                    directorySearchUrl = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.DIRECTORY_FACULTY_SEARCH_URL, null);
                    break;
            }
    	} else {
            directorySearchUrl = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.DIRECTORY_BASE_SEARCH_URL, null);
        }

        if (directoryTask != null) {
            directoryTask.cancel(true);
        }
        directoryTask = new SearchDirectoryInfoTask(this);
        directoryTask.execute(directorySearchUrl, query, personId);
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

    private void registerCourseDetailsReceiver() {
        if (courseDetailsReceiver == null) {
            Log.d(TAG, "Registering new service receiver");
            courseDetailsReceiver = new CourseDetailsReceiver();
            IntentFilter filter = new IntentFilter(CourseDetailsIntentService.ACTION_FINISHED);
            LocalBroadcastManager.getInstance(this).registerReceiver(courseDetailsReceiver, filter);
        }
    }

    private void unregisterCourseDetailsReceiver() {
        if (courseDetailsReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(courseDetailsReceiver);
        }
    }

    private class CourseDetailsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean updated = intent.getBooleanExtra(CourseDetailsIntentService.PARAM_OUT_DATABASE_UPDATED, false);
            Log.d("CourseDetailsReceiver", "onReceive: database updated = " + updated);
            if(updated) {
                Log.d("CourseDetailsReceiver.onReceive", "Course details retrieved and database updated");
                Utils.hideProgressIndicator(activity);
            }
        }

    }

    private void clearMainFragment() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        Fragment mainFrame = manager.findFragmentById(R.id.course_overview_frame);

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
            determineTab(tab.getText().toString());
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

        private void determineTab(String tabPosition) {
            if (TextUtils.equals(tabPosition,getString(R.string.course_details))) {
                mClass = CourseDetailsFragment.class;
                mTag = "CourseDetailsFragment";
                mFragment = courseDetailsFragment;
                return;
            }
            if (TextUtils.equals(tabPosition,getString(R.string.course_grades))) {
                mClass = CourseGradesFragment.class;
                mTag = "CourseGradesFragment";
                mFragment = courseGradesFragment;
                return;
            }
            if (TextUtils.equals(tabPosition,getString(R.string.course_roster))) {
                mClass = CourseRosterFragment.class;
                mTag = "CourseRosterFragment";
                mFragment = courseRosterFragment;
                return;
            }
            if (TextUtils.equals(tabPosition,getString(R.string.course_assignments))) {
                mClass = CourseAssignmentsListFragment.class;
                mTag = "CourseAssignmentsListFragment";
                mFragment = courseAssignmentsListFragment;
                return;
            }
            if (TextUtils.equals(tabPosition,getString(R.string.course_events))) {
                mClass = CourseEventsListFragment.class;
                mTag = "CourseEventsListFragment";
                mFragment = courseEventsListFragment;
                return;
            }
            if (TextUtils.equals(tabPosition,getString(R.string.course_announcements))) {
                mClass = CourseAnnouncementsListFragment.class;
                mTag = "CourseAnnouncementsListFragment";
                mFragment = courseAnnouncementsListFragment;
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

    private class SearchDirectoryInfoTask extends AsyncTask<String, Void, DirectoryResponse> {
        private final String TAG = SearchDirectoryInfoTask.class.getSimpleName();

        final Activity activity;

        SearchDirectoryInfoTask(Activity activity) {
            this.activity = activity;
        }

        // params (requestUrl, query [, personId])
        protected DirectoryResponse doInBackground(String... params) {
            DirectoryResponse response = null;

            String requestUrl = params[0];
            String query = params[1];
            String personId = null;
            if (params.length > 2) {
                personId = params[2];
            } else {
                Log.d(TAG, "No person id parameter passed");
            }

            if (!TextUtils.isEmpty(requestUrl) && !TextUtils.isEmpty(query)) {
                String modifiedUrl = requestUrl;

                String encodedQuery = null;
                String encodedId = null;
                try {
                    encodedQuery = URLEncoder.encode(query, "UTF-8");
                    if (!TextUtils.isEmpty(personId)) {
                        encodedId = URLEncoder.encode(personId, "UTF-8");
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (!TextUtils.isEmpty(encodedQuery)) {
                    modifiedUrl += "?searchString=" + encodedQuery;
                    if (!TextUtils.isEmpty(encodedId)) {
                        modifiedUrl += "&targetId=" + encodedId;
                    }
                }

                Log.d(TAG, "Directory search url, with params:" + modifiedUrl);

                MobileClient client = new MobileClient(activity);
                response = client.searchDirectory(modifiedUrl);

            } else {
                Log.d(TAG, "requestUrl or query is missing, no request sent.");
                Log.d(TAG, "requestUrl: " + requestUrl);
                Log.d(TAG, "query: " + query);
            }
            return response;
        }

        protected void onPostExecute(DirectoryResponse response) {

            if (response != null) {
                ArrayList<Entry> directoryEntries = new ArrayList<>(Arrays.asList(response.entries));
                if (directoryEntries.size() > 0) {

                    if (isLegacy) {
                        for (Entry entry : directoryEntries) {
                            entry.type = translateLegacyDirectoryType(entry.type);
                        }
                    }

                } else {
                    Log.d(TAG, "entries from response was null.");
                    Snackbar.make(findViewById(android.R.id.content), R.string.directory_no_results, Snackbar.LENGTH_LONG).show();
                }

                Log.d(TAG, "entries from response was " + directoryEntries.size());

                if (directoryEntries.size() == 1) {
                    // Exactly 1 match found. Launch straight into detail.
                    Entry entry = directoryEntries.get(0);
                    Bundle bundle = entry.buildBundle();
                    Intent intent = new Intent();
                    intent.setClass(activity, DirectoryDetailActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    // More than 1 match found. Launch list.
                    Intent intent = new Intent();
                    intent.setClass(activity, DirectoryListActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList(DirectoryListActivity.DIRECTORY_MULTIPLE_RESULTS, directoryEntries);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            } else {
                Log.d(TAG, "response is null.");
                Snackbar.make(findViewById(android.R.id.content), R.string.directory_no_results, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * For pre-4.5 directory, we get faculty or student directly from mobile server as the group/type.
     * We need to get the translated string.
     * @param dirType the directory type
     * @return translated value of legacy student or faculty
     */
    private String translateLegacyDirectoryType(String dirType) {
        if (TextUtils.equals(dirType, DirectoryActivity.DIRECTORY_TYPE_FACULTY)) {
            return getString(R.string.directory_type_faculty);
        } else if (TextUtils.equals(dirType, DirectoryActivity.DIRECTORY_TYPE_STUDENT)) {
            return getString(R.string.directory_type_student);
        }
        return null;
    }

}
