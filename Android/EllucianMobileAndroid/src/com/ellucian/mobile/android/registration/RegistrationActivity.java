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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.adapter.CheckableCursorAdapter;
import com.ellucian.mobile.android.adapter.CheckableCursorAdapter.OnCheckBoxClickedListener;
import com.ellucian.mobile.android.adapter.CheckableSectionedListAdapter;
import com.ellucian.mobile.android.adapter.SectionedListAdapter;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.client.courses.Instructor;
import com.ellucian.mobile.android.client.courses.MeetingPattern;
import com.ellucian.mobile.android.client.registration.CartResponse;
import com.ellucian.mobile.android.client.registration.EligibilityResponse;
import com.ellucian.mobile.android.client.registration.Message;
import com.ellucian.mobile.android.client.registration.OpenTerm;
import com.ellucian.mobile.android.client.registration.Plan;
import com.ellucian.mobile.android.client.registration.RegisterSection;
import com.ellucian.mobile.android.client.registration.RegistrationResponse;
import com.ellucian.mobile.android.client.registration.SearchResponse;
import com.ellucian.mobile.android.client.registration.Section;
import com.ellucian.mobile.android.client.registration.Term;
import com.ellucian.mobile.android.client.services.RegisterService;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.Extra;
import com.google.gson.Gson;

public class RegistrationActivity extends EllucianActivity {
	private static final String TAG = RegistrationActivity.class.getSimpleName();
	
	private final int CART_TAB_INDEX = 0;
	private final int SEARCH_TAB_INDEX = 1;
	private final int REGISTERED_TAB_INDEX = 2;
	
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
	private RegistrationRegisteredListFragment registeredFragment;
	protected CartResponse currentCart;
	private RetrieveCartListTask cartListTask;
	private int previousSelected; 
	protected boolean eligibilityChecked;
	protected boolean planPresent;
	private CheckEligibilityTask eligibilityTask;
	private RegisterReceiver registerReceivcer;
	
	protected SearchSectionTask searchTask;
	protected SearchResponse currentResults;
	protected CheckableSectionedListAdapter resultsAdapter;
	protected SectionedListAdapter registeredAdapter;
	
	
	protected OpenTerm[] openTerms;
	
	protected SimpleDateFormat defaultTimeParserFormat;
	protected SimpleDateFormat altTimeParserFormat;
	protected DateFormat timeFormatter;

	protected Gson gson;
	
	private boolean isInForeground;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_default_dual_pane);
		
		isInForeground = true;
		
		defaultTimeParserFormat = new SimpleDateFormat("HH:mm'Z'", Locale.US);
		defaultTimeParserFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		altTimeParserFormat = new SimpleDateFormat("HH:mm", Locale.US);
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
		
		registeredFragment =  (RegistrationRegisteredListFragment) manager.findFragmentByTag("RegistrationRegisteredListFragment");
		
		if (registeredFragment == null) {
			registeredFragment = (RegistrationRegisteredListFragment) Fragment.instantiate(this, RegistrationRegisteredListFragment.class.getName());
		}		
		
		cartAdapter = new CheckableSectionedListAdapter(RegistrationActivity.this);
		resultsAdapter = new CheckableSectionedListAdapter(RegistrationActivity.this);
		registeredAdapter = new SectionedListAdapter(this);
				
		boolean registerResultsAdded = false;
		boolean searchResultsAdded = false;
		if (savedInstanceState != null) {
			eligibilityChecked = savedInstanceState.getBoolean("eligibilityChecked");
			
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
			planPresent = true;
			Log.d(TAG, "Cart is current, building adapter.");
			
			fillCartAdapter(currentCart, savedInstanceState);
			fillRegisteredAdapter(currentCart);
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
		registeredFragment.setListAdapter(registeredAdapter);
		

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		Tab cartTab = actionBar.newTab()
				.setText(getCurrentCartText())
				.setTabListener(new RegistrationTabListener<RegistrationCartListFragment>(
						this, "RegistrationCartListFragment", cartFragment,
						R.id.frame_main));
 
		Tab searchTab = actionBar.newTab()
				.setText(R.string.registration_tab_search)
				.setTabListener(new RegistrationTabListener<RegistrationSearchFragment>(
						this, "RegistrationSearchFragment", searchFragment,
						R.id.frame_main));
		
		Tab registeredTab = actionBar.newTab()
				.setText(R.string.registration_tab_registered)
				.setTabListener(new RegistrationTabListener<RegistrationRegisteredListFragment>(
						this, "RegistrationRegisteredListFragment", registeredFragment,
						R.id.frame_main));

		actionBar.addTab(cartTab, false);
		actionBar.addTab(searchTab, false);
		actionBar.addTab(registeredTab, false);
	
		if (!eligibilityChecked) {
			eligibilityTask = new CheckEligibilityTask();
			eligibilityTask.execute(requestUrl);
		}

		clearMainFragment();
		if (previousSelected == SEARCH_TAB_INDEX) {
			cartTab.setTag(true);
			searchTab.setTag(true);
			registeredTab.setTag(true);
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
			
		} else if (previousSelected == REGISTERED_TAB_INDEX) {
			cartTab.setTag(true);
			searchTab.setTag(true);
			registeredTab.setTag(true);
			getActionBar().setSelectedNavigationItem(REGISTERED_TAB_INDEX);	
		} else {
			if (currentCart != null) {
				cartTab.setTag(true);
				searchTab.setTag(true);
				registeredTab.setTag(true);
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
				registeredTab.setTag(false);

				Log.d(TAG, "No cart found, retrieving.");
				setProgressBarIndeterminateVisibility(true);
				cartListTask = new RetrieveCartListTask();
				cartListTask.execute(requestUrl);

			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();	
		isInForeground = true;
		// Check if the RegisterService is still currently running and if so show the progress bar
		if (getEllucianApp().isServiceRunning(RegisterService.class)) {
			setProgressBarIndeterminateVisibility(true);
		}
		registerReceivcer = new RegisterReceiver();
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
		lbm.registerReceiver(registerReceivcer, new IntentFilter(RegisterService.ACTION_REGISTER_FINISHED));
	}
	
	@Override
	protected void onPause() {
		super.onPause();	
		isInForeground = false;
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
		lbm.unregisterReceiver(registerReceivcer);
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
		outState.putBoolean("eligibilityChecked", eligibilityChecked);
	}
	
	@Override
	protected void onDestroy() {
		if (eligibilityTask != null && eligibilityTask.getStatus() != AsyncTask.Status.FINISHED) { 
			Log.e(TAG, "Cancelling eligibility task");
			if (eligibilityTask.cancel(true)) {
				Log.e(TAG, "Cancelled");
			} else {
				Log.e(TAG, "failed to cancel");
			}
		}
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
	
	// Get the updated text for the Cart Tab showing the number of items currently in the cart
	private String getCurrentCartText() {
		if (cartAdapter != null && cartAdapter.getCountWithoutHeaders() > 0) {
			return getString(R.string.registration_tab_cart) + " (" + cartAdapter.getCountWithoutHeaders() +")";
		} else {
			return getString(R.string.registration_tab_cart);
		}
		
	}
	
	protected void clearMainFragment() {
		FragmentManager manager = getFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();
		Fragment mainFrame = manager.findFragmentById(R.id.frame_main);

		if (mainFrame != null) {
			ft.detach(mainFrame);
		}
		ft.commitAllowingStateLoss();
	}
	
	protected void clearDetailFragment() {
		FragmentManager manager = getFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();
		Fragment extraFrame = manager.findFragmentById(R.id.frame_extra);
		
		if (extraFrame != null) {
			ft.detach(extraFrame);
		}

		ft.commitAllowingStateLoss();
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

						// only showing non-registered sections						
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
		
		// After adapter is filled update the number of items in cart showing in tab
		if (getActionBar().getTabCount() > 0) {
			Tab cartTab = getActionBar().getTabAt(CART_TAB_INDEX);
			if (cartTab != null) {
				cartTab.setText(getCurrentCartText());
			} else {
				Log.d(TAG, "cartTab is null");
			}
		} else {
			Log.d(TAG, "Current tab count is 0");
		}
		

	}
	
	private void fillRegisteredAdapter(CartResponse response) {

		for (Plan plan : response.plans) {
			for (Term term : plan.terms) {
				
				if (term.plannedCourses.length > 0) {
				
					MatrixCursor sectionedCursor = new MatrixCursor(new String[] {
							BaseColumns._ID, RegistrationActivity.SECTION_ID, RegistrationActivity.TERM_ID, RegistrationActivity.PLAN_ID
					});
					
					int row = 1;
					for (Section course : term.plannedCourses) {
						
						if (!TextUtils.isEmpty(course.classification) && course.classification.equals("registered")) {
							Log.d(TAG, course.courseName + " is registered, showing in cart");
							sectionedCursor.addRow(new Object[] { "" + row++, 
									course.sectionId, term.termId, plan.planId		
							});
						} else {
							Log.d(TAG, course.courseName + " is not registered");
						}

					}
					
					SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(
							this, 
							R.layout.registration_registered_row, 
							sectionedCursor, 
							new String[] {RegistrationActivity.SECTION_ID}, 
							new int[] {R.id.registration_row_layout},
							0);
					
					cursorAdapter.setViewBinder(new RegistrationViewBinder());


					if (sectionedCursor.getCount() > 0) {
						registeredAdapter.addSection(term.termId, cursorAdapter);
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
			String action = null;
			Float credits = null;
			if (!TextUtils.isEmpty(section.gradingType) && section.gradingType.equals(Section.GRADING_TYPE_AUDIT)) {
				action = Section.GRADING_TYPE_AUDIT;
			} else if (!TextUtils.isEmpty(section.gradingType) && section.gradingType.equals(Section.GRADING_TYPE_PASS_FAIL)) {
				action = Section.GRADING_TYPE_PASS_FAIL;
			} else {
				action = "Add";
				if (section.minimumCredits != 0 && section.maximumCredits != 0) {
					credits = Float.valueOf(section.credits);
				}
			}

			SectionRegistration sectionRegistration = new SectionRegistration();
			sectionRegistration.termId = termId;
			sectionRegistration.sectionId = sectionId;
			sectionRegistration.action = action;
			sectionRegistration.credits = credits;
			
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
		if (planPresent && !getEllucianApp().isServiceRunning(RegisterService.class)) {
			registerConfirmDialogFragment = new RegisterConfirmDialogFragment();
			registerConfirmDialogFragment.show(getFragmentManager(), "RegisterConfirmDialogFragment");
		}
	}
	
	void onRegisterConfirmOkClicked() {
		sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_BUTTON_PRESS, "Register", null, moduleName);
		List<PlanToRegister> plansToRegister = getPlansToRegister();
		if (plansToRegister != null && !plansToRegister.isEmpty()) {

			PlanToRegister plan = plansToRegister.get(0);
			String planInJson = gson.toJson(plan);
			Log.d(TAG, "Registering : " + planInJson);
			
			Intent registerIntent = new Intent(this, RegisterService.class);
			registerIntent.putExtra(Extra.REQUEST_URL, requestUrl);
			registerIntent.putExtra(RegisterService.PLAN_TO_REGISTER, planInJson);
			startService(registerIntent);
			setProgressBarIndeterminateVisibility(true);
		} else {
			Log.e(TAG, "List of plans is either null or empty");
		}
	}
	
	private void updateRegistered(RegisterSection[] registeredSections) {
		int updated = 0;
		for (RegisterSection registeredSection : registeredSections) {			
			Section section = findSectionInCart(registeredSection.termId, registeredSection.sectionId);
			if (section != null) {
				Log.d(TAG, "Updating " + section.courseName + "-" + section.courseSectionNumber + " to registered");
				section.classification = "registered";
				updated ++;
			}		
		}
		
		if (updated > 0) {
			cartAdapter = new CheckableSectionedListAdapter(this);
			registeredAdapter = new SectionedListAdapter(this);
			cartFragment.setListAdapter(cartAdapter);
			registeredFragment.setListAdapter(registeredAdapter);
			fillCartAdapter(currentCart, null);
			fillRegisteredAdapter(currentCart);
		}
	}
	
	void onVariableCreditsConfirmOkClicked(String termId, String sectionId, float credits) {
		Section section = findSectionInResults(termId, sectionId);
		float setCredits = section.credits;
		section.credits = credits;

		if (setCredits != section.credits) {			
			resultsAdapter.notifyDataSetChanged();
		}
	}
	
	void onVariableCreditsConfirmCancelClicked(int position) {
		CheckBox checkBox = resultsAdapter.getCheckBoxAtPosition(position);
		if (checkBox != null) {
			checkBox.performClick();
		}
	}
	
	public void onAddToCartClicked(View view) {
		if (planPresent) {
			addToCartConfirmDialogFragment = new AddToCartConfirmDialogFragment();
			addToCartConfirmDialogFragment.show(getFragmentManager(), "AddToCartConfirmDialogFragment");
		}
	}
	
	void onAddToCartConfirmOkClicked() {
		sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_BUTTON_PRESS, "Add to cart", null, moduleName);
		CheckableSectionedListAdapter adapter = (CheckableSectionedListAdapter) searchResultsFragment.getListAdapter();
		String successMessage = "";
		for (int checkedPositon : adapter.getCheckedPositions()) {
			Cursor cursor = (Cursor) adapter.getItem(checkedPositon);

			String sectionId = cursor.getString(cursor.getColumnIndex(SECTION_ID));
			String termId = cursor.getString(cursor.getColumnIndex(TERM_ID));

			Section section = findSectionInResults(termId, sectionId);
			if (addSectionToCart(section)) {
				successMessage += section.courseName + "-" + section.courseSectionNumber + " " + getString(R.string.registration_added_to_cart_success) + "\n\n";
			} else {
				successMessage += section.courseName + "-" + section.courseSectionNumber +  " " + getString(R.string.registration_added_to_cart_failed) + "\n\n";
			}
		}
		if (currentCart != null && adapter.getCheckedPositions().size() > 0) {
			cartAdapter = new CheckableSectionedListAdapter(this);
			registeredAdapter = new SectionedListAdapter(this);
			cartFragment.setListAdapter(cartAdapter);
			registeredFragment.setListAdapter(registeredAdapter);
			fillCartAdapter(currentCart, null);
			fillRegisteredAdapter(currentCart);
			
			// Clear checked positions and remove button
			adapter.clearCheckedPositions();
			adapter.notifyDataSetChanged();
			searchResultsFragment.showAddToCartButton(false, 0);
			
			// Show success/fail toast
			if (!TextUtils.isEmpty(successMessage)) {
				Toast fillInMessage = Toast.makeText(this, successMessage, Toast.LENGTH_LONG);
				fillInMessage.setGravity(Gravity.CENTER, 0, 0);
				fillInMessage.show();
			}
		}
		
	}
	
	private boolean addSectionToCart(Section section) {
		if (currentCart != null) {
			for (Plan plan : currentCart.plans) {
				for (Term term : plan.terms) {
					if (term.termId.equals(section.termId)) {
						for (Section cartSection : term.plannedCourses) {
							if (cartSection.sectionId.equals(section.sectionId)) {
								Log.e(TAG, "Can not add section to cart, section already exists in cart.");
								return false;
							}
						}
						section.classification = "planned";
						if (section.credits == 0) {
							section.credits = section.minimumCredits;
						}
						Log.d(TAG, "adding section: " + section.courseName + "-" + section.courseSectionNumber);
						term.plannedCourses = Arrays.copyOf(term.plannedCourses, term.plannedCourses.length + 1);
						term.plannedCourses[term.plannedCourses.length - 1] = section;
						return true;
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
						if (section.credits == 0) {
							section.credits = section.minimumCredits;
						}
						newPlanTerm.plannedCourses[0] = section;
						plan.terms = Arrays.copyOf(plan.terms, plan.terms.length + 1);
						plan.terms[plan.terms.length - 1] = newPlanTerm;
						return true;
					}
				}
			}
			
			
		} else {
			Log.e(TAG, "currentCart is null");			
		}
		return false;
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
				}
			}
			if (sectionedCursor.getCount() > 0) {
				resultsAdapter.addSection(getString(R.string.search_results_label), checkableAdapter);
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
		public Float credits;
	}
	
	private class CheckEligibilityTask extends AsyncTask<String, Void, EligibilityResponse> {

		@Override
		protected EligibilityResponse doInBackground(String... params) {
			String requestUrl = params[0];
			
			MobileClient client = new MobileClient(RegistrationActivity.this);
			requestUrl = client.addUserToUrl(requestUrl);
			requestUrl += "/eligibility";

			EligibilityResponse response = client.getEligibility(requestUrl);		
			return response;
		}
		
		@Override
		protected void onPostExecute(EligibilityResponse result) {
			
			if (result != null && result.eligible == true) {
				Log.d(TAG, "Eligibility check: true");				
			} else {			
				Log.d(TAG, "Eligibility check: false");
				String message = "";

				if (result != null && result.messages != null && result.messages.length > 0) {
					for (Message currentMessage : result.messages) {
						if (!TextUtils.isEmpty(currentMessage.message)) {
							if (!TextUtils.isEmpty(message)) {
								message += "\n\n";
							}
							message += currentMessage.message;
						}		
					}		
				}
				
				cartFragment.setShowEligibilityError(true, message);
				
				EligibilityDialogFragment eligibilityDialogFragment = EligibilityDialogFragment.newInstance(message);
				if(isInForeground) {
					eligibilityDialogFragment.show(getFragmentManager(), "EligibilityDialogFragment");				
				}
				
			}
			eligibilityChecked = true;
		}
		
	}
	
	private class RetrieveCartListTask extends AsyncTask<String, Void, CartResponse> {

		@Override
		protected CartResponse doInBackground(String... params) {		
			String requestUrl = params[0];
			
			MobileClient client = new MobileClient(RegistrationActivity.this);
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
					planPresent = true;
					fillCartAdapter(result, null);
					fillRegisteredAdapter(result);
					
					if (cartAdapter.getCount() > 0) {	
						if(isInForeground) {
							getActionBar().setSelectedNavigationItem(CART_TAB_INDEX);	
							setProgressBarIndeterminateVisibility(false);
						}
						return;
					} else {
						Log.e(TAG, "Adapter is empty");
					}
				} else {
					Log.e(TAG, "No plans returned");
					planPresent = false;
				}
			} else {
				Log.e(TAG, "Response is null");
				planPresent = false;
			}

			getActionBar().setSelectedNavigationItem(SEARCH_TAB_INDEX);
			setProgressBarIndeterminateVisibility(false);
			
			if (!planPresent) {
				EligibilityDialogFragment eligibilityDialogFragment = 
						EligibilityDialogFragment.newInstance(getString(R.string.registration_no_plan_eligibility_message));
				if(isInForeground) {
					eligibilityDialogFragment.show(getFragmentManager(), "PlanNotPresentDialogFragment");				
				}
			}
		}
	}
	
	private class RegisterReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			String result = intent.getStringExtra(RegisterService.REGISTRATION_RESULT);
			
			RegistrationResponse registrationResponse = null;
			Log.d(TAG, "RegisterTask result: " + result);
			if (!TextUtils.isEmpty(result)) {
				registrationResponse = gson.fromJson(result, RegistrationResponse.class);
			} else {				
				Log.e(TAG, "result is empty or null");
				return;
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
			
			MobileClient client = new MobileClient(RegistrationActivity.this);
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
	
	protected class RegistrationViewBinder implements SimpleCursorAdapter.ViewBinder {
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
			String shortName = firstInstructor.lastName + ", " + firstInstructor.firstName.charAt(0);
			instructorView.setText(shortName);
		} else {
			view.findViewById(R.id.instructor_credits_separator).setVisibility(View.GONE);
		}
		
		TextView creditsView = (TextView) view.findViewById(R.id.credits);
		if (section.credits != 0) {
			String creditsString = "" + section.credits + " " + getString(R.string.registration_credits);
			if (!TextUtils.isEmpty(section.gradingType) && section.gradingType.equals(Section.GRADING_TYPE_AUDIT)) {
				creditsString += " | " + getString(R.string.registration_audit);
			} else if (!TextUtils.isEmpty(section.gradingType) && section.gradingType.equals(Section.GRADING_TYPE_PASS_FAIL)) {
				creditsString += " | " + getString(R.string.registration_pass_fail_abbrev);
			}
			creditsView.setText(creditsString);
			
		} else if (section.minimumCredits != 0){
			String creditsText = "" + (float)section.minimumCredits;
			if (section.maximumCredits != 0) {
				creditsText += "-" + (float)section.maximumCredits;
			}
			creditsText += " " + getString(R.string.registration_credits);
			creditsView.setText(creditsText);
		} else if (section.ceus != 0){
			creditsView.setText("" + section.ceus + " " + getString(R.string.registration_ceus));
		} else {
			// Only want to display zero in last possible case to avoid not showing the correct alternative
			String creditsString = "0 " + getString(R.string.registration_credits);
			if (!TextUtils.isEmpty(section.gradingType) && section.gradingType.equals(Section.GRADING_TYPE_AUDIT)) {
				creditsString += " | " + getString(R.string.registration_audit);
			} else if (!TextUtils.isEmpty(section.gradingType) && section.gradingType.equals(Section.GRADING_TYPE_PASS_FAIL)) {
				creditsString += " | " + getString(R.string.registration_pass_fail_abbrev);
			}
			creditsView.setText(creditsString);
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
				if (!TextUtils.isEmpty(pattern.sisStartTimeWTz) && pattern.sisStartTimeWTz.contains(" ")) {
					String[] splitTimeAndZone = pattern.sisStartTimeWTz.split(" ");
					String time = splitTimeAndZone[0];
					String timeZone = splitTimeAndZone[1];
					altTimeParserFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
					startTimeDate = altTimeParserFormat.parse(time);
				} else {
					startTimeDate = defaultTimeParserFormat.parse(pattern.startTime);
				}
				
				if (!TextUtils.isEmpty(pattern.sisEndTimeWTz) && pattern.sisEndTimeWTz.contains(" ")) {
					String[] splitTimeAndZone = pattern.sisEndTimeWTz.split(" ");
					String time = splitTimeAndZone[0];
					String timeZone = splitTimeAndZone[1];
					altTimeParserFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
					endTimeDate = altTimeParserFormat.parse(time);
				} else {
					endTimeDate = defaultTimeParserFormat.parse(pattern.endTime);
				}
				
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
		public void onCheckBoxClicked(CheckBox checkBox, boolean isChecked, int position) {

			if (isChecked || !cartAdapter.getCheckedPositions().isEmpty()) {
				cartFragment.showRegisterButton(true, cartAdapter.getCheckedPositions().size());
			} else {
				cartFragment.showRegisterButton(false, 0);
			}
		}
	}
	
	private class SearchCheckBoxClickedListener implements OnCheckBoxClickedListener {
		
		@Override
		public void onCheckBoxClicked(CheckBox checkBox, boolean isChecked, int position) {

			if (isChecked || !resultsAdapter.getCheckedPositions().isEmpty()) {
				
				if (isChecked) {
					// Have to add 1 to position because of the header
					Cursor cursor = (Cursor)resultsAdapter.getItem(position + 1);
					
					String sectionId = cursor.getString(cursor.getColumnIndex(RegistrationActivity.SECTION_ID));
					String termId = cursor.getString(cursor.getColumnIndex(RegistrationActivity.TERM_ID));

					Section section = findSectionInResults(termId, sectionId);
					
					if (section != null && section.minimumCredits != 0 && section.maximumCredits != 0) {

						VariableCreditsConfirmDialogFragment creditsDialogFragment = VariableCreditsConfirmDialogFragment.newInstance(section, position);
						// Stops the back button from closing dialog
						creditsDialogFragment.setCancelable(false);
						creditsDialogFragment.show(getFragmentManager(), "VariableCreditsConfirmDialogFragment");
					}
					
				}
				searchResultsFragment.showAddToCartButton(true, resultsAdapter.getCheckedPositions().size());
			} else {
				searchResultsFragment.showAddToCartButton(false, 0);
			}
			
		}
	}
	
}
