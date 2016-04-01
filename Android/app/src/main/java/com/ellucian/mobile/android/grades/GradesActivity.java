/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.grades;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.EllucianListFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.services.GradesIntentService;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.provider.EllucianContract.GradeTerms;
import com.ellucian.mobile.android.provider.EllucianContract.Grades;
import com.ellucian.mobile.android.provider.EllucianContract.GradesCourses;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Main activity for displaying a student's grades.  UI uses a view pager to allow the user to swipe through
 * the terms the student took courses to see their grades
 * @author sdk
 *
 */
public class GradesActivity extends EllucianActivity {

	private final Activity activity = this;
    private TermsPagerAdapter termsPageAdapter;
	private ViewPager viewPager;
	private static final String VIEWPAGER_TERM = "viewpagerTerm";
	private GradesIntentServiceReceiver gradesServiceReceiver;
	private OnPageChangeListener pageChangeListener;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("GradesActivity", "onCreate()");
        setContentView(R.layout.activity_grades);

		setTitle(moduleName);

        // Create the adapter that will return a fragment for each term
        termsPageAdapter = new TermsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
		viewPager = (ViewPager) findViewById(R.id.pager);

        viewPager.setAdapter(termsPageAdapter);
		pageChangeListener = new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				GradesActivity.this.sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_SLIDE_ACTION, "Swipe Terms", null, moduleName);
			}
		};
		viewPager.addOnPageChangeListener(pageChangeListener);

		if(savedInstanceState == null) {
			Log.d("GradesActivity", "No saved instance state.  Grabbing data.");
            Utils.showProgressIndicator(this);
            viewPager.setCurrentItem(viewPager.getChildCount() - 1);

			registerGradesServiceReceiver();

			Intent intent = new Intent(this, GradesIntentService.class);
			intent.putExtra(Extra.MODULE_ID, moduleId);
			intent.putExtra(Extra.REQUEST_URL, requestUrl);
			startService(intent);
		} else {
			// Load terms from the database asynchronously
			String viewpagerItem = savedInstanceState.getString(VIEWPAGER_TERM);
			LoadTermsFromDatabaseTask loadTerms = new LoadTermsFromDatabaseTask();
            if (viewpagerItem != null) {
                loadTerms.setCurrentTermId(viewpagerItem);
				Log.d("GradesActivity.onCreate", "Setting current term to in LoadTermsFromDatabaseTask to: " + viewpagerItem);
			}
			loadTerms.execute();
		}
    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_grades, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_grades_term_picker:
				List<TermModel> terms = termsPageAdapter.getTerms();
				List<String> termNames = new ArrayList<>();
				for(TermModel model : terms) {
					termNames.add(model.getName());
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(this)
						.setItems(termNames.toArray(new String[termNames.size()]), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								viewPager.setCurrentItem(which);
								sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_LIST_SELECT, "Select Term", null, moduleName);
							}
						});
				Dialog dialog = builder.create();
				dialog.show();
				sendView("Term List", moduleName);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * On saving, get the term id currently displayed by the view pager.  This is used to select the term if the 
	 * activity is recreated from saved state
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		int currentItem = viewPager.getCurrentItem();
		List<TermModel> terms = termsPageAdapter.getTerms();
		if(terms.size() >= currentItem + 1) {
			String termId = terms.get(currentItem).getId();
			outState.putString(VIEWPAGER_TERM, termId);
		}
	}

	private void registerGradesServiceReceiver() {
		if(gradesServiceReceiver == null) {
			Log.d("GradesActivity.RegisterGradesServiceReceiver", "Registering new service receiver");
			gradesServiceReceiver = new GradesIntentServiceReceiver();
			IntentFilter filter = new IntentFilter(GradesIntentService.ACTION_FINISHED);
			LocalBroadcastManager.getInstance(this).registerReceiver(gradesServiceReceiver, filter);
		}
	}

	private void unregisterGradesServiceReceiver() {
		if(gradesServiceReceiver != null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(gradesServiceReceiver);
		}
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * a term of courses
	 */
	public class TermsPagerAdapter extends FragmentPagerAdapter {
		/** Store the list of terms that are supported by the page */
		List<TermModel> terms = new ArrayList<>();

		public TermsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		/**
		 * For each term return the grades fragment for the term
		 */
		@Override
		public Fragment getItem(int i) {
			Fragment fragment = new GradeSectionFragment();
			Bundle args = new Bundle();
			// Pass the term id to the Grades fragment
			args.putString("term", terms.get(i).getId());
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return terms.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return terms.get(position).getName();
		}

		/**
		 * Gets the terms that are serviced by the adapter
		 * @return terms model
		 */
		public List<TermModel> getTerms() {
			return this.terms;
		}

		/**
		 * Sets the terms that are serviced by the adapter
		 * @param terms model
		 */
		public void setTerms(List<TermModel> terms) {
			if(terms != null) {
				this.terms = terms;
			} else {
				this.terms = new ArrayList<>();
			}
		}
	}

	/**
	 * Section fragment for the grades for each term.
	 * @author sdk
	 *
	 */
	@SuppressWarnings("JavaDoc")
	public static class GradeSectionFragment extends EllucianListFragment {
		private GradesListAdapter adapter;
		private List<CourseModel> courses = new ArrayList<>();
		private LoadCoursesFromDatabaseTask task;

		/**
		 * When creating the fragment load the async task the queries the local
		 * database for grades
		 */
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			Log.d("GradeSectionFragment.onActivityCreated", "Loading courses from the database");
			task = new LoadCoursesFromDatabaseTask();
			task.execute();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fragment_grades, container,
					false);
		}

		/**
		 * When destroying the fragment, cancel the background task retrieving courses from the database
		 * and close any cursors 
		 */
		@Override
		public void onDestroy() {
			super.onDestroy();
			Log.d("GradeSectionFragment", "onDestroy");
			if(task != null) {
				Log.d("GradeSectionFragment", "onDestroy cancelling LoadCoursesFromDatabase task");
				task.cancel(false);
			}
			for(CourseModel course : getCourses()) {
				if(course.getGrades() != null) {
					Log.d("GradesSectionFragment.onDestroy", "Closing grades cursors");
					course.getGrades().close();
				}
			}
		}

		/**
		 * Gets the data model for the fragment
		 * @return
		 */
		public List<CourseModel> getCourses() {
			return this.courses;
		}

		/**
		 * Sets the data model for the fragment
		 * @param courses
		 */
		public void setCourses(List<CourseModel> courses) {
			if(courses != null) {
				this.courses = courses;
			} else {
				this.courses = new ArrayList<>();
			}
		}

		/**
		 * Used during display of the view to modify the date and time output for the view
		 * @author sdk
		 */
		private class GradeViewBinder implements SimpleCursorAdapter.ViewBinder {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int index) {
				if(index == cursor.getColumnIndex(Grades.GRADE_UPDATED)) {
					String dateString = cursor.getString(index);

					Date date = CalendarUtils.parseFromUTC(dateString);

					// TODO - Set this back when the main format gets fixed
					//Date date = EllucianDatabase.toDate(cursor.getString(index));

					String output = getString(R.string.unavailable);
					if(date != null) {
						output = CalendarUtils.getDefaultDateTimeString(getActivity(), date);
					}

					((TextView) view).setText(output);
					return true;
				} else {
					return false;
				}

			}
		}

		/**
		 * Async task to load course data from the local SQLite database
		 * @author sdk
		 */
		private class LoadCoursesFromDatabaseTask extends AsyncTask<Void, Void, List<CourseModel>> {

			/** Save off the content resolver in case the GradeSectionFragment is unloaded while the async 
			 * task is executing
			 */
			final ContentResolver resolver = GradeSectionFragment.this.getActivity().getContentResolver();

			/**
			 * Creates the CourseModel from data in the database.  This is accomplished in two steps.
			 * The first step is to get the courses for a specific term and store that in a model and then
			 * retrieve a cursor for each course's grades.  The down-side to this implementation is it requires
			 * a separate cursor per course rather than get all the courses and grades in a join.
			 */
			@Override
			protected List<CourseModel> doInBackground(Void... params) {
				Log.d("LoadCoursesFromDatabaseTask", "doInBackground");
				List<CourseModel> courses = new ArrayList<>();
				Cursor cursor = resolver
						.query(GradeTerms.buildCoursesUri(getArguments().getString("term")), null, null,
								null, GradesCourses.DEFAULT_SORT);
				if (cursor.moveToFirst()) {
					do {
						String title = cursor.getString(cursor
								.getColumnIndex(GradesCourses.COURSE_TITLE));
						String courseName = cursor.getString(cursor
								.getColumnIndex(GradesCourses.COURSE_DESCRIPTION));
						String courseId = cursor.getString(cursor
								.getColumnIndex(GradesCourses.COURSE_ID));
						String sectionNumber = cursor.getString(cursor
								.getColumnIndex(GradesCourses.COURSE_SECTION));
						if(courseName != null && courseId != null) { //sql statement may return a term with no courses
							String label;

                            label = getString(R.string.default_course_section_format,
										courseName,
										sectionNumber);

							courses.add(new CourseModel(courseId, label, title, null));
						}
					} while (cursor.moveToNext());
				}
				cursor.close();

				// Add Sections
				for (CourseModel course : courses) {
					if(isCancelled()) {
						Log.d("LoadCoursesFromDatabaseTask", "Cancel received in doInBackground.  Breaking.");
						break;
					}
					Cursor gradesCursor = resolver
							.query(Grades.CONTENT_URI, null, GradesCourses.COURSE_ID+"=?",
									new String[] {course.getId()}, Grades.DEFAULT_SORT);
					Log.d("LoadCoursesFromDatabaseTask", "Querying for grades: " + Grades.buildGradeUri(course.getId()));
					if(gradesCursor.moveToFirst()) {
						do {
							Log.d("LoadCoursesFromDatabaseTask", "Grade: "
									+ gradesCursor.getString(gradesCursor.getColumnIndex(Grades.GRADE_NAME)) + " "
									+ gradesCursor.getString(gradesCursor.getColumnIndex(Grades.GRADE_VALUE)) + " "
									+ gradesCursor.getString(gradesCursor.getColumnIndex(Grades.GRADE_UPDATED)));
						} while (gradesCursor.moveToNext());
					} else {
						Log.d("LoadCoursesFromDatabaseTask", "Grades cursors empty.");
					}
					gradesCursor.moveToFirst();

					course.setGrades(gradesCursor);
				}
				return courses;
			}

			/**
			 * Once the grades have been retrieved update the fragment
			 */
			@Override
			protected void onPostExecute(List<CourseModel> result) {
				super.onPostExecute(result);
				Log.d("LoadCoursesFromDatabaseTask", "onPostExecute");
				final Activity activity = GradeSectionFragment.this.getActivity();
				setCourses(result);
				// checks if the async task has been cancelled before processing the model
				if(activity != null && !isCancelled()) {
					adapter = new GradesListAdapter();
					if(result.size() == 0) {
						setListAdapter(null);
					} else {
						for(CourseModel course : result) {
							Adapter gradesAdapter;
							if(course.getGrades().getCount() > 0) {
								SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(GradeSectionFragment.this.getActivity(),
										R.layout.grade_row, course.getGrades(),
										new String[] {Grades.GRADE_NAME, Grades.GRADE_VALUE, Grades.GRADE_UPDATED},
										new int[] {R.id.grade_row_label, R.id.grade_row_value, R.id.grade_row_date}, 0);

								cursorAdapter.setViewBinder(new GradeViewBinder());
								gradesAdapter = cursorAdapter;
							} else {
								ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(GradeSectionFragment.this.getActivity(),
										R.layout.grade_no_grade_row, R.id.grade_row_label);
								emptyAdapter.add(getResources().getString(R.string.grades_no_grades));
								gradesAdapter = emptyAdapter;

							}
                            GradesSectionHeader[] gradesSectionHeaders = {new GradesSectionHeader(course.getLabel(), course.getTitle())};
                            GradesSectionHeaderAdapter headerAdapter =
                                    new GradesSectionHeaderAdapter(activity, gradesSectionHeaders);

                            adapter.addSection(headerAdapter, gradesAdapter);
						}
						setListAdapter(adapter);
					}
				}
			}

			/**
			 * If the task is cancelled attempt to close any cursors that have been created so far
			 */
			@Override
			protected void onCancelled(List<CourseModel> result) {
				Log.d("LoadCoursesFromDatabaseTask", "onPostExecute");
				if(result == null) {
					Log.d("LoadCoursesFromDatabaseTask", "onPostExecute result is null");
					return;
				}
				for(CourseModel course : result) {
					if(course.getGrades() != null) {
						Log.d("LoadCoursesFromDatabaseTask", "Closing grades cursor.");
						course.getGrades().close();
						course.setGrades(null);
					}
				}
			}
		}
	}

	/**
	 * Loads the terms from the SQLite database.  This may be called for a brand new activity or resuming 
	 * an activity.  If resuming, the name of the current term can be passed in to set the right term to 
	 * display once the terms are read.
	 * @author sdk
	 */
	@SuppressWarnings("JavaDoc")
	private class LoadTermsFromDatabaseTask extends AsyncTask<Void, Void, List<TermModel>> {
		private String currentTermId;

		/**
		 * Queries the database for all terms and puts them into a model
		 */
		@Override
		protected List<TermModel> doInBackground(Void... params) {
			Cursor cursor = GradesActivity.this.getContentResolver().query(
					EllucianContract.GradeTerms.CONTENT_URI, null, null, null,
					EllucianContract.GradeTerms.DEFAULT_SORT);
			List<TermModel> terms = new ArrayList<>();
			if (cursor.moveToFirst()) {
				do {
					String name = cursor.getString(cursor
							.getColumnIndex(GradeTerms.TERM_NAME));
					String id = cursor.getString(cursor
							.getColumnIndex(GradeTerms.TERM_ID));
					terms.add(new TermModel(id, name));
				} while (cursor.moveToNext());
			}
			cursor.close();
			return terms;
		}

		/**
		 * Updates the terms in the page adapter and can set the current term to display based on getCurrentTermId()
		 */
		@Override
		protected void onPostExecute(List<TermModel> result) {
			Log.d("LoadTermsFromDatabaseTask.onPostExecute", "Loaded " + result.size() + " from database and refreshing UI");
			super.onPostExecute(result);
			termsPageAdapter.setTerms(result);
			termsPageAdapter.notifyDataSetChanged();
			if(getCurrentTermId() != null) {
				for(int i = 0; i < result.size(); i++) {
					if(getCurrentTermId().equals(result.get(i).getId())) {
						Log.d("LoadTermsFromDatabase.onPostExecute", "Updating viewPager to item " + i);
						viewPager.setCurrentItem(i);
					}
				}
			}

			viewPager.invalidate();
			// Setup the Sliding Tabs
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setVisibility(View.VISIBLE);
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabLayout.setSelectedTabIndicatorColor(Utils.getColorHelper(GradesActivity.this, R.color.tab_indicator_color));

        }

		/**
		 * Gets the current term to display
		 * @return
		 */
		public String getCurrentTermId() {
			return currentTermId;
		}

		/**
		 * Sets the current term to display
		 * @param currentTermId
		 */
		public void setCurrentTermId(String currentTermId) {
			this.currentTermId = currentTermId;
		}
	}

	/**
	 * Broadcast receiver which receives notification when the GradesIntentService
	 * is finished performing an update of the data from the web services to the 
	 * local database.  Upon successful completion, this receiver will reload 
	 * the grades data from the local database.
	 * @author sdk
	 *
	 */
	private class GradesIntentServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			boolean updated = intent.getBooleanExtra(GradesIntentService.PARAM_OUT_DATABASE_UPDATED, false);
			Log.d("GradesIntentServiceReceiver", "onReceive: database updated = " + updated);
			if(updated) {
				Log.d("GradesIntentServiceReceiver.onReceive", "All grades retrieved and database updated");
                Utils.hideProgressIndicator(activity);
                new LoadTermsFromDatabaseTask().execute();
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		registerGradesServiceReceiver();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterGradesServiceReceiver();
	}

	@Override
	protected void onStart() {
		super.onStart();
		sendView("Grades list", moduleName);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		viewPager.removeOnPageChangeListener(pageChangeListener);
	}
}
