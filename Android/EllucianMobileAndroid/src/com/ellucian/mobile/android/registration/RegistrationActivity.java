// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.registration;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.adapter.CheckableCursorAdapter;
import com.ellucian.mobile.android.adapter.CheckableCursorAdapter.OnCheckBoxClickedListener;
import com.ellucian.mobile.android.adapter.CheckableSectionedListAdapter;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.client.courses.Instructor;
import com.ellucian.mobile.android.client.courses.MeetingPattern;
import com.ellucian.mobile.android.client.registration.CartResponse;
import com.ellucian.mobile.android.client.registration.OpenTerm;
import com.ellucian.mobile.android.client.registration.Plan;
import com.ellucian.mobile.android.client.registration.RegisterSection;
import com.ellucian.mobile.android.client.registration.RegistrationResponse;
import com.ellucian.mobile.android.client.registration.SearchResponse;
import com.ellucian.mobile.android.client.registration.Section;
import com.ellucian.mobile.android.client.registration.Term;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.google.gson.Gson;

public class RegistrationActivity extends EllucianActivity {
	private static final String TAG = RegistrationActivity.class.getSimpleName();
	
	private final int CART_TAB_INDEX = 0;
	private final int SEARCH_TAB_INDEX = 1;
	
	public static final String PLAN_ID = "planId";
	public static final String TERM_ID = "termId";
	public static final String SECTION_ID = "sectionId";
	public static final String SECTION = "section";
	
	private CheckableSectionedListAdapter cartAdapter;
	private RegistrationCartListFragment cartFragment;
	private RegistrationSearchFragment searchFragment;
	private RegistrationResultsFragment registerResultsFragment;
	private RegistrationSearchResultsListFragment searchResultsFragment;
	private RegisterConfirmDialogFragment registerConfirmDialogFragment;
	private AddToCartConfirmDialogFragment addToCartConfirmDialogFragment;
	private CartResponse currentCart;
	private RetrieveCartListTask cartListTask;
	private int previousSelected; 
	
	protected SearchSectionTask searchTask;
	protected SearchResponse currentResults;
	protected CheckableSectionedListAdapter resultsAdapter;
	
	protected OpenTerm[] openTerms;
	
	protected SimpleDateFormat dataFormat;
	protected DateFormat timeFormatter;	
	protected Gson gson;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_default_dual_pane);
		dataFormat = new SimpleDateFormat("HH:mm'Z'", Locale.US);
		dataFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		timeFormatter = android.text.format.DateFormat.getTimeFormat(this);
		gson = new Gson();
		
		FragmentManager manager = getFragmentManager();
		cartFragment =  (RegistrationCartListFragment) manager.findFragmentByTag("RegistrationCartListFragment");
		
		if (cartFragment == null) {
			cartFragment = (RegistrationCartListFragment) EllucianDefaultListFragment.newInstance(this, RegistrationCartListFragment.class.getName(), null);
		
		}
		
		searchFragment =  (RegistrationSearchFragment) manager.findFragmentByTag("RegistrationSearchFragment");
		
		if (searchFragment == null) {
			searchFragment = (RegistrationSearchFragment) Fragment.instantiate(this, RegistrationSearchFragment.class.getName());
		
		}
		
		
		cartAdapter = new CheckableSectionedListAdapter(RegistrationActivity.this);
		resultsAdapter = new CheckableSectionedListAdapter(RegistrationActivity.this);
		
		boolean registerResultsAdded = false;
		boolean searchResultsAdded = false;
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey("currentCart")) {
				Log.d(TAG, "Found saved cart, restoring.");
				currentCart = savedInstanceState.getParcelable("currentCart");
			}
			if (savedInstanceState.containsKey("currentResults")) {
				Log.d(TAG, "Found saved currentResults, restoring.");
				currentResults = savedInstanceState.getParcelable("currentResults");
			}
			if (savedInstanceState.containsKey("previousSelected")) {
				previousSelected = savedInstanceState.getInt("previousSelected");
			}
			if (savedInstanceState.containsKey("registerResultsAdded")) {
				registerResultsAdded = savedInstanceState.getBoolean("registerResultsAdded");
			}
			if (savedInstanceState.containsKey("searchResultsAdded")) {
				searchResultsAdded = savedInstanceState.getBoolean("searchResultsAdded");
			}
		}

		if (currentCart != null) {
			Log.d(TAG, "Cart is current, building adapter.");

			fillCartAdapter(currentCart, savedInstanceState);
			if (cartAdapter == null) {
				Log.e("TAG", "cartAdapter is null");
			}

		}
		
		if (currentResults != null) {
			Log.d(TAG, "Results is current, building adapter.");

			fillSearchResultsAdapter(currentResults, savedInstanceState);
			if (resultsAdapter == null) {
				Log.e("TAG", "resultsAdapter is null");
			}

		}
		
		cartFragment.setListAdapter(cartAdapter);
		

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		Tab cartTab = actionBar.newTab()
				.setText(R.string.registration_tab_cart)
				.setTabListener(new RegistrationTabListener<RegistrationCartListFragment>(
						this, "RegistrationCartListFragment", cartFragment,
						R.id.frame_main));
 
		Tab searchTab = actionBar.newTab()
				.setText(R.string.registration_tab_search)
				.setTabListener(new RegistrationTabListener<RegistrationSearchFragment>(
						this, "RegistrationSearchFragment", searchFragment,

						R.id.frame_main));

		actionBar.addTab(cartTab, false);
		actionBar.addTab(searchTab, false);
	

		clearMainFragment();
		if (previousSelected == SEARCH_TAB_INDEX) {
			cartTab.setTag(true);
			searchTab.setTag(true);
			if (searchResultsAdded) {
				FragmentTransaction ft = manager.beginTransaction();
				clearMainFragment();
				
				searchResultsFragment = (RegistrationSearchResultsListFragment) manager.findFragmentByTag("RegistrationSearchResultsListFragment");
				searchResultsFragment.setListAdapter(resultsAdapter);
				ft.attach(searchResultsFragment);
				ft.commit();
			} else {
				getActionBar().setSelectedNavigationItem(SEARCH_TAB_INDEX);
			}
			
		} else {
			if (currentCart != null) {
				cartTab.setTag(true);
				searchTab.setTag(true);
				if (registerResultsAdded) {
					FragmentTransaction ft = manager.beginTransaction();
					clearMainFragment();
					
					registerResultsFragment = (RegistrationResultsFragment) manager.findFragmentByTag("RegistrationResultsFragment");
					
					ft.attach(registerResultsFragment);
					ft.commit();
				} else {
					getActionBar().setSelectedNavigationItem(CART_TAB_INDEX);
				}
					
			} else {
				cartTab.setTag(false);
				searchTab.setTag(false);

				Log.d(TAG, "No cart found, retrieving.");
				setProgressBarIndeterminateVisibility(true);
				cartListTask = new RetrieveCartListTask();
				cartListTask.execute(requestUrl);

			}
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();	
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if (currentCart != null) {
			outState.putParcelable("currentCart", currentCart);
			
			if (cartAdapter != null) {
				ArrayList<String> headerNames = new ArrayList<String>();
				for (int i = 0; i < cartAdapter.headers.getCount(); i++) {
					String headerName = cartAdapter.headers.getItem(i);
					headerNames.add(headerName);
					CheckableCursorAdapter cursorAdapter = (CheckableCursorAdapter) cartAdapter.sections.get(i);					
					outState.putBooleanArray(headerName, cursorAdapter.getCheckedStatesAsBooleanArray());

				}
			}			
		}
		
		if (currentResults != null) {
			outState.putParcelable("currentResults", currentResults);
			
			if (resultsAdapter != null) {
				ArrayList<String> headerNames = new ArrayList<String>();
				for (int i = 0; i < resultsAdapter.headers.getCount(); i++) {
					String headerName = resultsAdapter.headers.getItem(i);
					headerNames.add(headerName);
					CheckableCursorAdapter cursorAdapter = (CheckableCursorAdapter) resultsAdapter.sections.get(i);					
					outState.putBooleanArray(headerName, cursorAdapter.getCheckedStatesAsBooleanArray());					
				}
			}			
		}
		
		int currentTab =  getActionBar().getSelectedNavigationIndex();
		if (registerResultsFragment != null) {
			if (registerResultsFragment.isAdded()) {
				outState.putBoolean("registerResultsAdded", true);
				currentTab = CART_TAB_INDEX;
			}
			
		}
		if (searchResultsFragment != null) {
			if (searchResultsFragment.isAdded()) {
				outState.putBoolean("searchResultsAdded", true);
				currentTab = SEARCH_TAB_INDEX;
			} 
		}
		
		outState.putInt("previousSelected", currentTab);	
	}
	
	@Override
	protected void onDestroy() {
		if (searchTask != null && searchTask.getStatus() != AsyncTask.Status.FINISHED) { 
			Log.e(TAG, "Cancelling search task");
			if (searchTask.cancel(true)) {
				Log.e(TAG, "Cancelled");
			} else {
				Log.e(TAG, "failed to cancel");
			}
		} 
		if (cartListTask != null && cartListTask.getStatus() != AsyncTask.Status.FINISHED) { 
			Log.e(TAG, "Cancelling cart task");
			if (cartListTask.cancel(true)) {
				Log.e(TAG, "Cancelled");
			} else {
				Log.e(TAG, "failed to cancel");
			}
		}
		super.onDestroy();
	}
	
	protected void clearMainFragment() {
		FragmentManager manager = getFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();
		Fragment mainFrame = manager.findFragmentById(R.id.frame_main);

		if (mainFrame != null) {
			ft.detach(mainFrame);
		}
		ft.commit();
	}
	
	protected void clearDetailFragment() {
		FragmentManager manager = getFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();
		Fragment extraFrame = manager.findFragmentById(R.id.frame_extra);
		
		if (extraFrame != null) {
			ft.detach(extraFrame);
		}

		ft.commit();
	}
	
	private void enableTabs() {
		ActionBar actionBar = getActionBar();
		for (int i = 0; i < actionBar.getTabCount(); i++) {
			actionBar.getTabAt(i).setTag(true);
		}
	}
	
	private void fillCartAdapter(CartResponse response, Bundle savedInstanceState) {

		for (Plan plan : response.plans) {
			for (Term term : plan.terms) {
				
				if (term.plannedCourses.length > 0) {
				
					MatrixCursor sectionedCursor = new MatrixCursor(new String[] {
							BaseColumns._ID, SECTION_ID, TERM_ID, PLAN_ID
					});
					
					int row = 1;
					for (Section course : term.plannedCourses) {
						
						if (!TextUtils.isEmpty(course.classification) && !course.classification.equals("registered")) {
							Log.d(TAG, course.courseName + " is not registered, showing in cart");
							sectionedCursor.addRow(new Object[] { "" + row++, 
									course.sectionId, term.termId, plan.planId		
							});
						} else {
							Log.d(TAG, course.courseName + " is already registered");
						}
						

					}
					
					CheckableCursorAdapter cursorAdapter = new CheckableCursorAdapter(
							this, 
							R.layout.registration_list_checkbox_row, 
							sectionedCursor, 
							new String[] {SECTION_ID}, 
							new int[] {R.id.registration_row_layout},
							0,
							R.id.checkbox);
					
					cursorAdapter.setViewBinder(new RegistrationViewBinder());
					cursorAdapter.registerOnCheckBoxClickedListener(new RegisterCheckBoxClickedListener());
					if (savedInstanceState != null) {
						boolean[] checkedStatesBooleanArray = savedInstanceState.getBooleanArray(term.termId);
						if (checkedStatesBooleanArray != null) {
							cursorAdapter.setCheckedStates(checkedStatesBooleanArray);
						}
					}
					if (sectionedCursor.getCount() > 0) {
						cartAdapter.addSection(term.termId, cursorAdapter);
					}
				}
			}
		}

	}
	
	Section findSectionInCart(String termId, String sectionId) {
		if (currentCart != null) {
			for (Plan plan : currentCart.plans) {
				for (Term term : plan.terms) {
					if (term.termId.equals(termId)) {
						for (Section course : term.plannedCourses) {
							if (course.sectionId.equals(sectionId)) {
								return course;
							}
						}
					}
				}
			}
			
			Log.e(TAG, "cannot find course in currentCart");
			return null;
		} else {
			Log.e(TAG, "currentCart is null, cannot find course");
			return null;
		}
	}
	
	
	
	private List<PlanToRegister> getPlansToRegister() {
		HashMap<String, List<SectionRegistration>> selectionMap = new HashMap<String, List<SectionRegistration>>();
		CheckableSectionedListAdapter adapter = (CheckableSectionedListAdapter) cartFragment.getListAdapter();
		
		List<SectionRegistration> currentSelectionList = null;
		for (int checkedPositon : adapter.getCheckedPositions()) {
			Cursor cursor = (Cursor) adapter.getItem(checkedPositon);

			String sectionId = cursor.getString(cursor.getColumnIndex(SECTION_ID));
			String termId = cursor.getString(cursor.getColumnIndex(TERM_ID));
			String planId = cursor.getString(cursor.getColumnIndex(PLAN_ID));
			
			if (selectionMap.containsKey(planId)) {
				currentSelectionList = selectionMap.get(planId);
			} else {
				currentSelectionList = new ArrayList<SectionRegistration>();
				selectionMap.put(planId, currentSelectionList);
			}

			Section section = findSectionInCart(termId, sectionId);
			
			SectionRegistration sectionRegistration = new SectionRegistration();
			sectionRegistration.termId = termId;
			sectionRegistration.sectionId = sectionId;
			sectionRegistration.action = "Add";
			if (section.credits != 0) {
				sectionRegistration.credits = (int) section.credits;
			} else {
				sectionRegistration.credits = (int) section.minimumCredits;
			}

			currentSelectionList.add(sectionRegistration);
		}
		
		List<PlanToRegister> plansToRegister = new ArrayList<PlanToRegister>();
		for (String planId : selectionMap.keySet()) {
			PlanToRegister newPlan = new PlanToRegister();
			newPlan.planId = planId;
			ArrayList<SectionRegistration> listToConvert = (ArrayList<SectionRegistration>) selectionMap.get(planId);
			newPlan.sectionRegistrations = listToConvert.toArray(new SectionRegistration[listToConvert.size()]);
			plansToRegister.add(newPlan);
		}
		
		return plansToRegister;
		
	}
	
	public void onRegisterClicked(View view) {
		registerConfirmDialogFragment = new RegisterConfirmDialogFragment();
		registerConfirmDialogFragment.show(getFragmentManager(), "RegisterConfirmDialogFragment");
	}
	
	void onRegisterConfirmOkClicked() {
		List<PlanToRegister> plansToRegister = getPlansToRegister();
		if (plansToRegister != null && !plansToRegister.isEmpty()) {

			PlanToRegister plan = plansToRegister.get(0);
			String planInJson = gson.toJson(plan);
			Log.d(TAG, "Registering : " + planInJson);
			
			RegisterTask registerTask = new RegisterTask();
			registerTask.execute(requestUrl, planInJson);
			setProgressBarIndeterminateVisibility(true);
		} else {
			Log.e(TAG, "List of plans is either null or empty");
		}
	}
	
	private void updateRegistered(RegisterSection[] registeredSections) {
		for (RegisterSection section : registeredSections) {
			Section course = findSectionInCart(section.termId, section.sectionId);
			Log.d(TAG, "Updating " + course.courseName + course.courseSectionNumber + " to registered");
			course.classification = "registered";
		}
		
		if (registeredSections.length > 0) {
			cartAdapter = new CheckableSectionedListAdapter(RegistrationActivity.this);
			cartFragment.setListAdapter(cartAdapter);
			fillCartAdapter(currentCart, null);
		}
	}
	public void onAddToCartClicked(View view) {
		addToCartConfirmDialogFragment = new AddToCartConfirmDialogFragment();
		addToCartConfirmDialogFragment.show(getFragmentManager(), "AddToCartConfirmDialogFragment");
	}
	
	void onAddToCartConfirmOkClicked() {
		CheckableSectionedListAdapter adapter = (CheckableSectionedListAdapter) searchResultsFragment.getListAdapter();
		for (int checkedPositon : adapter.getCheckedPositions()) {
			Cursor cursor = (Cursor) adapter.getItem(checkedPositon);

			String sectionId = cursor.getString(cursor.getColumnIndex(SECTION_ID));
			String termId = cursor.getString(cursor.getColumnIndex(TERM_ID));

			Section section = findSectionInResults(termId, sectionId);
			addSectionToCart(section);
		}
		if (currentCart != null && adapter.getCheckedPositions().size() > 0) {
			cartAdapter = new CheckableSectionedListAdapter(RegistrationActivity.this);
			cartFragment.setListAdapter(cartAdapter);
			fillCartAdapter(currentCart, null);
			getActionBar().setSelectedNavigationItem(CART_TAB_INDEX);
		}
		
	}
	
	private void addSectionToCart(Section section) {
		if (currentCart != null) {
			for (Plan plan : currentCart.plans) {
				for (Term term : plan.terms) {
					if (term.termId.equals(section.termId)) {
						for (Section cartSection : term.plannedCourses) {
							if (cartSection.sectionId.equals(section.sectionId)) {
								Log.e(TAG, "Can not add section to cart, section already exists in cart.");
								return;
							}
						}
						section.classification = "planned";
						Log.d(TAG, "adding section: " + section.courseName + "-" + section.courseSectionNumber);
						term.plannedCourses = Arrays.copyOf(term.plannedCourses, term.plannedCourses.length + 1);
						term.plannedCourses[term.plannedCourses.length - 1] = section;
						return;
					}
					
				}
				for (OpenTerm openTerm : openTerms) {
					if (openTerm.id.equals(section.termId)) {
						Term newPlanTerm = new Term();
						newPlanTerm.termId = openTerm.id;
						newPlanTerm.name = openTerm.name;
						newPlanTerm.startDate = openTerm.startDate;
						newPlanTerm.endDate = openTerm.endDate;
						newPlanTerm.plannedCourses = new Section[1];
						section.classification = "planned";
						newPlanTerm.plannedCourses[0] = section;
						plan.terms = Arrays.copyOf(plan.terms, plan.terms.length + 1);
						plan.terms[plan.terms.length - 1] = newPlanTerm;
						return;
					}
				}
			}
			
			
		} else {
			Log.e(TAG, "currentCart is null");
		}
	}
	
	protected void startSectionSearch(String termId, String pattern) {
		searchTask = new SearchSectionTask();
		searchTask.execute(requestUrl, termId, pattern);
		setProgressBarIndeterminateVisibility(true);
	}

	private void fillSearchResultsAdapter(SearchResponse response, Bundle savedInstanceState) {

		if (response.sections.length > 0) {
		
			MatrixCursor sectionedCursor = new MatrixCursor(new String[] {
					BaseColumns._ID, RegistrationActivity.SECTION_ID, RegistrationActivity.TERM_ID
			});
			
			int row = 1;
			for (Section section : response.sections) {
				Log.d(TAG, "Creating row for : " + section.sectionId + "/" + section.termId);
				sectionedCursor.addRow(new Object[] { "" + row++, 
						section.sectionId, section.termId});
			}
			
			CheckableCursorAdapter checkableAdapter = new CheckableCursorAdapter(
					this, 
					R.layout.registration_list_checkbox_row, 
					sectionedCursor, 
					new String[] {RegistrationActivity.SECTION_ID}, 
					new int[] {R.id.registration_row_layout},
					0,
					R.id.checkbox);
			
			checkableAdapter.setViewBinder(new RegistrationSearchViewBinder());
			checkableAdapter.registerOnCheckBoxClickedListener(new SearchCheckBoxClickedListener());
			
			if (savedInstanceState != null) {
				boolean[] checkedStatesBooleanArray = savedInstanceState.getBooleanArray("Results");
				if (checkedStatesBooleanArray != null) {
					checkableAdapter.setCheckedStates(checkedStatesBooleanArray);
					for (boolean set : checkedStatesBooleanArray) {
					}
				}
			}
			if (sectionedCursor.getCount() > 0) {
				resultsAdapter.addSection("Results", checkableAdapter);
			}
		}
	}
	
	protected Section findSectionInResults(String termId, String sectionId) {
		if (currentResults != null) {

			for (Section section : currentResults.sections) {
				if (section.sectionId.equals(sectionId) && section.termId.equals(termId)) {
					return section;
				}
			}

			Log.e(TAG, "cannot find section in searchResponse");
			return null;
		} else {
			Log.e(TAG, "searchResponse is null, cannot find section");
			return null;
		}
	}
	
	@SuppressWarnings("unused")
	private class PlanToRegister {
		public String planId;
		public SectionRegistration[] sectionRegistrations;
	}
	
	@SuppressWarnings("unused")
	private class SectionRegistration {
		public String termId;
		public String sectionId;
		public String action;
		public int credits;
	}
	
	private class RetrieveCartListTask extends AsyncTask<String, Void, CartResponse> {

		@Override
		protected CartResponse doInBackground(String... params) {		
			String requestUrl = params[0];
			
			MobileClient client = new MobileClient(getApplication());
			requestUrl = client.addUserToUrl(requestUrl);
			requestUrl += "/plans";

			CartResponse response = client.getCartList(requestUrl);		
			return response;
		}
		
		@Override
		protected void onPostExecute(CartResponse result) {
			currentCart = result;
			
			enableTabs();
			
			if (result != null) {
				if (result.plans != null || result.plans.length > 0) {

					fillCartAdapter(result, null);
					
					if (cartAdapter.getCount() > 0) {
						
						getActionBar().setSelectedNavigationItem(CART_TAB_INDEX);
						setProgressBarIndeterminateVisibility(false);
						return;
					} else {
						Log.e(TAG, "Adapter is empty");
					}
				} else {
					Log.e(TAG, "No plans returned");
				}
			} else {
				Log.e(TAG, "Response is null");
			}

			getActionBar().setSelectedNavigationItem(SEARCH_TAB_INDEX);
			setProgressBarIndeterminateVisibility(false);
		}
	}
	
	private class RegisterTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String requestUrl = params[0];
			String planInJson = params[1];
			
			MobileClient client = new MobileClient(getApplication());
			requestUrl = client.addUserToUrl(requestUrl);
			requestUrl += "/register-sections";
			
			String response = client.putCoursesToRegister(requestUrl, planInJson);
			
			return response;
		}
		
		
		@Override
		protected void onPostExecute(String result) {
			RegistrationResponse registrationResponse = null;
			Log.d(TAG, "RegisterTask result: " + result);
			if (!TextUtils.isEmpty(result)) {
				registrationResponse = gson.fromJson(result, RegistrationResponse.class);
			} else {
				Log.e(TAG, "result is empty or null");
			}
			FragmentManager manager = getFragmentManager();
			FragmentTransaction ft = manager.beginTransaction();
			Fragment mainFrame = manager.findFragmentById(R.id.frame_main);

			if (mainFrame != null) {
				ft.detach(mainFrame);
			}
				
			registerResultsFragment = (RegistrationResultsFragment) Fragment.instantiate(RegistrationActivity.this, RegistrationResultsFragment.class.getName());

			Bundle args = new Bundle();
			args.putParcelable("RegistrationResponse", registrationResponse);
			registerResultsFragment.setArguments(args);
			
			ft.add(R.id.frame_main, registerResultsFragment, "RegistrationResultsFragment");

			ft.commit();
			
			if (registrationResponse.successes != null && registrationResponse.successes.length > 0) {
				updateRegistered(registrationResponse.successes);
			}
			((CheckableSectionedListAdapter)cartAdapter).clearCheckedPositions();
			clearDetailFragment();
			setProgressBarIndeterminateVisibility(false);
			
		}
	}
	
	private class SearchSectionTask extends AsyncTask<String, Void, SearchResponse> {

		@Override
		protected SearchResponse doInBackground(String... params) {
			String requestUrl = params[0];
			String termId = params[1];
			String pattern = params[2];
			String encodedTermId = "";
			String encodedPattern = "";
			try {
				encodedTermId = URLEncoder.encode(termId, "UTF-8");
				encodedPattern = URLEncoder.encode(pattern, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "UnsupportedEncodingException:", e);
			}
			
			MobileClient client = new MobileClient(getApplication());
			requestUrl = client.addUserToUrl(requestUrl);
			requestUrl += "/search-courses?pattern=" + encodedPattern + "&term=" + encodedTermId;
			
			SearchResponse jsonResponse = client.findSections(requestUrl);
			
			return jsonResponse; 
		}
		
		@Override
		protected void onPostExecute(SearchResponse result) {
			
			if (this != null) {
				currentResults = result;
				
				resultsAdapter = new CheckableSectionedListAdapter(RegistrationActivity.this);
				FragmentManager manager = getFragmentManager();
				FragmentTransaction ft = manager.beginTransaction();
				searchResultsFragment = (RegistrationSearchResultsListFragment) 
						manager.findFragmentByTag("RegistrationSearchResultsListFragment");
				
				clearMainFragment();
				
				if (searchResultsFragment == null) {
					searchResultsFragment = (RegistrationSearchResultsListFragment) RegistrationSearchResultsListFragment.newInstance(
							RegistrationActivity.this, RegistrationSearchResultsListFragment.class.getName(), null);
					searchResultsFragment.setListAdapter(resultsAdapter);
					ft.add(R.id.frame_main, searchResultsFragment, "RegistrationSearchResultsListFragment");	
				} else {
					
					searchResultsFragment.setListAdapter(resultsAdapter);
					ft.attach(searchResultsFragment);
				}
				ft.commit();
				
				if (currentResults != null) {
	
					if (currentResults.sections != null && currentResults.sections.length > 0) {						
						fillSearchResultsAdapter(currentResults, null);
					} else {
						Log.e(TAG, "Sections array null or emtpy");
						resultsAdapter = null;
					}			
				} else {
					Log.e(TAG, "Search response is null");
					resultsAdapter = null;
				}	
				setProgressBarIndeterminateVisibility(false);
			}	
		}
	}

	private class RegistrationTabListener<T extends Fragment> implements ActionBar.TabListener {
		private Fragment mFragment;
	    private final Activity mActivity;
	    private final String mTag;
	    private final Class<? extends Fragment> mClass;
		private FragmentManager fragmentManager;
		private int fragmentContainerResId;

	    /** Constructor used each time a new tab is created.
	      * @param activity  The host Activity, used to instantiate the fragment
	      * @param tag  The identifier tag for the fragment
	      * @param clz  The fragment's Class, used to instantiate the fragment
	      */
	    public RegistrationTabListener(Activity activity, String tag, Class<T> clz, int fragmentContainerResId) {
	        mActivity = activity;
	        mTag = tag;
	        mClass = clz;
	        fragmentManager = activity.getFragmentManager();
	        this.fragmentContainerResId = fragmentContainerResId;
	    }
	    
	    public RegistrationTabListener(Activity activity, String tag, Fragment fragment,  int fragmentContainerResId) {
	    	mFragment = fragment;
	    	mActivity = activity;
	    	mTag = tag;
	        mClass = fragment.getClass();
	        fragmentManager = activity.getFragmentManager();
	        this.fragmentContainerResId = fragmentContainerResId;
	    }

	    /* The following are each of the ActionBar.TabListener callbacks */
	    
	    public void onTabSelected(Tab tab, FragmentTransaction ft) {
	    	if (tab.getTag() != null) {
	    		if ((Boolean)tab.getTag() == false) {
	    			return;
	    		}
	    	}	    	
	    	clearMainFragment();
	    	clearDetailFragment();
	    	
	    	// Check if the fragment is in the Fragment Manager
	    	if (fragmentManager.findFragmentByTag(mTag) == null || mFragment == null) {
	            // If not, check to see if it has been instantiated
	    		if (mFragment == null) {
	    			mFragment = Fragment.instantiate(mActivity, mClass.getName());
	    		}
	    		
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
	    	/*
	        if (fragmentManager.findFragmentByTag(mTag) != null && mFragment != null) {
	            // Detach the fragment, because another one is being attached
	            ft.detach(mFragment);
	        }
	    	 */
	    	
	    	clearDetailFragment();

	    	if ( mFragment != null) { //fragmentManager.findFragmentByTag(mTag) != null &&
	    		// Detach the fragment, because another one is being attached
	    		ft.detach(mFragment);
	    	}
	    	Fragment fragment = fragmentManager.findFragmentByTag(mTag);
	    	if (fragment != null) {
	    		ft.detach(mFragment);
	    	}
	    	
	    }

	    public void onTabReselected(Tab tab, FragmentTransaction ft) {
	    	onTabSelected(tab, ft);
	    }

	}
	
	private class RegistrationViewBinder implements SimpleCursorAdapter.ViewBinder {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int index) {
			if(index == cursor.getColumnIndex(SECTION_ID)) {
				String sectionId = cursor.getString(index);
				String termId = cursor.getString(cursor.getColumnIndex(TERM_ID));
				
				Section section = findSectionInCart(termId, sectionId);
				
				setRowView(section, view, cursor, index);

				return true;
			}
			return false;
		}
	}
	
	protected class RegistrationSearchViewBinder implements SimpleCursorAdapter.ViewBinder {
		@Override
		public boolean setViewValue(View view, Cursor cursor, int index) {
			if(index == cursor.getColumnIndex(RegistrationActivity.SECTION_ID)) {
				String sectionId = cursor.getString(index);
				String termId = cursor.getString(cursor.getColumnIndex(RegistrationActivity.TERM_ID));
				
				Section section = findSectionInResults(termId, sectionId);
				
				
				setRowView(section, view, cursor, index);
				return true;
			}
			return false;
		}
	}
	
	private void setRowView(Section section, View view, Cursor cursor, int index) {
		// Reset views to visible for next view
		view.findViewById(R.id.instructor_credits_separator).setVisibility(View.VISIBLE);
		view.findViewById(R.id.meetings_type_separator).setVisibility(View.VISIBLE);
		
					
		if (!TextUtils.isEmpty(section.courseName)) {
			String titleString = section.courseName;
			if (!TextUtils.isEmpty(section.courseSectionNumber)) {
				titleString += "-" + section.courseSectionNumber;
			}
			TextView courseNameView = (TextView) view.findViewById(R.id.course_name);
			courseNameView.setText(titleString);
		}
		if (!TextUtils.isEmpty(section.sectionTitle)) {
			TextView sectionTitleView = (TextView) view.findViewById(R.id.section_title);
			sectionTitleView.setText(section.sectionTitle);
		}
		
		TextView instructorView = (TextView) view.findViewById(R.id.instructor);
		if (section.instructors != null && section.instructors.length != 0) {
			Instructor firstInstructor = section.instructors[0];
			instructorView.setText(firstInstructor.lastName);
		} else {
			view.findViewById(R.id.instructor_credits_separator).setVisibility(View.GONE);
		}
		
		TextView creditsView = (TextView) view.findViewById(R.id.credits);
		if (section.credits != 0) {
			creditsView.setText("" + section.credits + " " + getString(R.string.registration_credits));
		} else if (section.minimumCredits != 0){
			creditsView.setText("" + (float)section.minimumCredits  + " " + getString(R.string.registration_credits));
		} else {
			view.findViewById(R.id.instructor_credits_separator).setVisibility(View.GONE);
		}

		TextView meetingsView = (TextView) view.findViewById(R.id.meetings);
		TextView typeView = (TextView) view.findViewById(R.id.type);
		if (section.meetingPatterns != null && section.meetingPatterns.length != 0) {
			MeetingPattern pattern = section.meetingPatterns[0];
			
			String meetingsString = "";

			if (pattern.daysOfWeek != null && pattern.daysOfWeek.length != 0) {
				
				for (int dayNumber : pattern.daysOfWeek) {

					if (!TextUtils.isEmpty(meetingsString)) {
						meetingsString += ", ";
					}
					// Adding 1 to number to make the Calendar constants 
					meetingsString += CalendarUtils.getDayShortName(dayNumber);
				}
				meetingsString += ": ";
			}
			
			Date startTimeDate = null;
			Date endTimeDate = null;
			String displayStartTime = "";
			String displayEndTime = "";
			
			try {
				startTimeDate = dataFormat.parse(pattern.startTime);
				endTimeDate = dataFormat.parse(pattern.endTime);
				
				if (startTimeDate != null) {
					displayStartTime = timeFormatter.format(startTimeDate);
				}	
				if (endTimeDate != null) {
					displayEndTime = timeFormatter.format(endTimeDate);
				}
			} catch (ParseException e) {
				Log.e(TAG, "ParseException: ", e);
			}

			if (!TextUtils.isEmpty(displayStartTime)) {
				meetingsString += displayStartTime;
				if (!TextUtils.isEmpty(displayEndTime)) {
					meetingsString += " - " + displayEndTime;
				}
			}

			if (!TextUtils.isEmpty(meetingsString)) {
				meetingsView.setText(meetingsString);
			} else {
				view.findViewById(R.id.meetings_type_separator).setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(pattern.instructionalMethodCode)) {
				typeView.setText(pattern.instructionalMethodCode);
			} else {
				view.findViewById(R.id.meetings_type_separator).setVisibility(View.GONE);
			}
		} else {
			view.findViewById(R.id.meetings_type_separator).setVisibility(View.GONE);
		}
	}
	
	private class RegisterCheckBoxClickedListener implements OnCheckBoxClickedListener {
				
		@Override
		public void onCheckBoxClicked(boolean isChecked, int position) {

			if (isChecked || !cartAdapter.getCheckedPositions().isEmpty()) {
				cartFragment.showRegisterButton(true);
			} else {
				cartFragment.showRegisterButton(false);
			}
		}
	}
	
	private class SearchCheckBoxClickedListener implements OnCheckBoxClickedListener {
		
		@Override
		public void onCheckBoxClicked(boolean isChecked, int position) {

			if (isChecked || !resultsAdapter.getCheckedPositions().isEmpty()) {
				searchResultsFragment.showAddToCartButton(true);
			} else {
				searchResultsFragment.showAddToCartButton(false);
			}
			
		}
	}
	
}
