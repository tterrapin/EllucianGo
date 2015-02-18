package com.ellucian.mobile.android.courses.overview;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

public class CourseOverviewActivity extends EllucianActivity  {
	private static final String TAG = CourseOverviewActivity.class.getSimpleName();
	@SuppressWarnings("unused")
	private String courseId;
	private boolean moreTabPreviouslyAdded;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_course_overview);
	    
	    Bundle incomingExtras = getIntent().getExtras();
		String courseName = incomingExtras.getString(Extra.COURSES_NAME);
		String sectionNumber = incomingExtras.getString(Extra.COURSES_SECTION_NUMBER);
		setTitle(getString(R.string.default_course_section_format,
						courseName, 
						sectionNumber));
	    // Determine if the roster is shown
	    courseId = incomingExtras.getString(Extra.COURSES_COURSE_ID);
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

		
	    // setup action bar for tabs
	    ActionBar actionBar = getActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    //actionBar.setDisplayShowTitleEnabled(false);
	    configureActionBar();

	    Tab detailsTab = actionBar.newTab()
	            .setText(R.string.course_details)
	            .setTabListener(new CoursesTabListener<CourseDetailsFragment>(
	                    this, "detail", CourseDetailsFragment.class, getFragmentManager(),
	                    R.id.courses_course_overview_frame));
	    actionBar.addTab(detailsTab);

	    Tab gradesTab = actionBar.newTab()
	        .setText(R.string.course_grades)
	        .setTabListener(new CoursesTabListener<CourseGradesFragment>(
	                this, "grades", CourseGradesFragment.class, getFragmentManager(),
	                R.id.courses_course_overview_frame));
	    actionBar.addTab(gradesTab);
	    
	    if (showRoster) {
		    Tab rosterTab = actionBar.newTab()
			        .setText(R.string.course_roster)
			        .setTabListener(new CoursesTabListener<CourseRosterFragment>(
			                this, "roster", CourseRosterFragment.class, getFragmentManager(),
			                R.id.courses_course_overview_frame));
			    actionBar.addTab(rosterTab);	    
	    }

	    // Check for which tab was selected on re-create
	    if ( savedInstanceState != null ) {
	    	if (savedInstanceState.getBoolean("moreTabPreviouslyAdded")) {
	    		addMoreTab();
	    	}
	    	getActionBar().setSelectedNavigationItem(savedInstanceState.getInt("tabState")); 
	    }
	    
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
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) { 
		super.onSaveInstanceState(outState);
		outState.putInt("tabState", getActionBar().getSelectedTab().getPosition());
		outState.putBoolean("moreTabPreviouslyAdded", moreTabPreviouslyAdded);
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
    		String query = (String) ((TextView) view).getText();
    		
    		sendDirectoryQueryIntent(DirectoryCategoriesFragment.DIRECTORY_TYPE_STUDENT,  query);
    	} else {
    		noModuleToast(R.string.course_details_no_directory);
    	}
	}
	
	public void sendDirectoryQueryIntent(String queryType,  String query) { 
		if (TextUtils.isEmpty(queryType)) {
			queryType = DirectoryCategoriesFragment.DIRECTORY_TYPE_ALL;
		}
		
    	Log.d(TAG, "Sending query to database for : " + query);
    	Intent intent = new Intent(this, DirectoryListActivity.class);
    	intent.setAction(Intent.ACTION_SEARCH);
    	intent.putExtra(Extra.DIRECTORY_TYPE, queryType);
    	intent.putExtra(Extra.DIRECTORY_QUERY, query);
    	
    	if (queryType.equals(DirectoryCategoriesFragment.DIRECTORY_TYPE_STUDENT)) {
    		intent.putExtra(Extra.DIRECTORY_STUDENT_URL, 
    				Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.DIRECTORY_STUDENT_SEARCH_URL, null));
    	} else if (queryType.equals(DirectoryCategoriesFragment.DIRECTORY_TYPE_FACULTY)) {
    		intent.putExtra(Extra.DIRECTORY_FACULTY_URL, 
    				Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.DIRECTORY_FACULTY_SEARCH_URL, null));
    	} else {
    		intent.putExtra(Extra.REQUEST_URL, 
    				Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.DIRECTORY_ALL_SEARCH_URL, null));
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
	
	protected void addMoreTab() {
		
		if (!moreTabPreviouslyAdded ) {
			moreTabPreviouslyAdded = true;
			Tab moreTab = getActionBar().newTab()
			        .setText(R.string.course_more)
			        .setTabListener(new CoursesTabListener<CourseMoreFragment>(
			                this, "more", CourseMoreFragment.class, getFragmentManager(),
			                R.id.courses_course_overview_frame));
	        	getActionBar().addTab(moreTab);
		}
	}
	
}
