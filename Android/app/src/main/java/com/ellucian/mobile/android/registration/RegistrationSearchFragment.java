// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.registration;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.EllucianFragment;
import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.client.registration.OpenTerm;
import com.ellucian.mobile.android.client.registration.TermsResponse;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;
import com.ellucian.mobile.android.provider.EllucianContract.RegistrationLevels;
import com.ellucian.mobile.android.provider.EllucianContract.RegistrationLocations;
import com.ellucian.mobile.android.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class RegistrationSearchFragment extends EllucianFragment  {
	private static final String TAG = RegistrationSearchFragment.class.getSimpleName();
	
	private RegistrationActivity activity;
	private View rootView;
	private Spinner termsSpinner;
	private EditText searchField;
	private Button searchButton;
	private TextView refineSearchView;
	private RetrieveSearchableTermsTask termsTask;
	private ArrayAdapter<String> termsAdapter;
	private String[] termNames;
	private OpenTerm[] terms;
	private int selectedSpinnerItem = -1;
	private List<SearchFilter> locationFilters = new ArrayList<SearchFilter>();
	private List<SearchFilter> levelFilters = new ArrayList<SearchFilter>();
	ArrayList<String> selectedLocations;
	ArrayList<String> selectedLevels;
	

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		this.activity = (RegistrationActivity) getActivity();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null) {
			locationFilters = savedInstanceState.getParcelableArrayList("locationFilters");
			levelFilters = savedInstanceState.getParcelableArrayList("levelFilters");
			selectedLocations = savedInstanceState.getStringArrayList("selectedLocations");
			selectedLevels = savedInstanceState.getStringArrayList("selectedLevels");			
		} else {
			
			String moduleId = ((EllucianActivity)activity).moduleId;
			String selection = Modules.MODULES_ID + " = ?";
			
			Cursor locationCursor = activity.getContentResolver().query(
												RegistrationLocations.CONTENT_URI, 
												null, 
												selection, 
												new String[] { moduleId }, 
												RegistrationLocations.DEFAULT_SORT);
			

			if (locationCursor.moveToFirst()) {

				do {
					String locationName = locationCursor.getString(
							locationCursor.getColumnIndex(RegistrationLocations.REGISTRATION_LOCATIONS_NAME));
					String locationCode = locationCursor.getString(
							locationCursor.getColumnIndex(RegistrationLocations.REGISTRATION_LOCATIONS_CODE));
					
					if (!TextUtils.isEmpty(locationName) && !TextUtils.isEmpty(locationName)) {
						SearchFilter locationFilter = new SearchFilter();
						locationFilter.name = locationName;
						locationFilter.code = locationCode;
						locationFilters.add(locationFilter);
					}
				} while (locationCursor.moveToNext());
			}
			
			locationCursor.close();
			
			Cursor levelCursor = activity.getContentResolver().query(
					RegistrationLevels.CONTENT_URI, 
											null, 
											selection, 
											new String[] { moduleId },
											RegistrationLevels.DEFAULT_SORT);
		
			if (levelCursor.moveToFirst()) {
				do {
					String levelName = levelCursor.getString(
							levelCursor.getColumnIndex(RegistrationLevels.REGISTRATION_LEVELS_NAME));
					String levelCode = levelCursor.getString(
							levelCursor.getColumnIndex(RegistrationLevels.REGISTRATION_LEVELS_CODE));
		
					if (!TextUtils.isEmpty(levelName) && !TextUtils.isEmpty(levelName)) {
						SearchFilter levelFilter = new SearchFilter();
						levelFilter.name = levelName;
						levelFilter.code = levelCode;
						levelFilters.add(levelFilter);
					}
				} while (levelCursor.moveToNext());
			}

			levelCursor.close();
		}

	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_registration_search, container, false);
		termsSpinner = (Spinner) rootView.findViewById(R.id.terms_spinner);
		searchField = (EditText) rootView.findViewById(R.id.search_field);
		searchButton = (Button) rootView.findViewById(R.id.search_button);
		refineSearchView = (TextView) rootView.findViewById(R.id.refine_search_link);
		return rootView; 
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity = (RegistrationActivity) getActivity();
				
		searchButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int selectedTermPosition = termsSpinner.getSelectedItemPosition();
				Log.d(TAG, "selectedTermPosition: " + selectedTermPosition);
				String selectedTermId = null;
				if (terms != null) {
					selectedTermId = terms[selectedTermPosition].id;
				}
				String pattern = searchField.getText().toString();
				if (TextUtils.isEmpty(selectedTermId) && TextUtils.isEmpty(pattern)) {
					Toast fillInMessage = Toast.makeText(getActivity(), R.string.registration_fill_in_message, Toast.LENGTH_SHORT);
					fillInMessage.setGravity(Gravity.CENTER, 0, 0);
					fillInMessage.show();
				} else {
					List<String> locationCodes = getCodeList(locationFilters, selectedLocations);
					List<String> levelCodes = getCodeList(levelFilters, selectedLevels);
					
					activity.startSectionSearch(selectedTermId, pattern, locationCodes, levelCodes);
				}
			}
			
		});
		
		if (!locationFilters.isEmpty() || !levelFilters.isEmpty()) {
			refineSearchView.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					activity.openRefineSearch();				
				}
			});
		} else {
			refineSearchView.setVisibility(View.GONE);
		}
		
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey("terms")) {
				terms = (OpenTerm[]) savedInstanceState.getParcelableArray("terms");
			}
			
			if (savedInstanceState.containsKey("termNames")) {
				termNames = savedInstanceState.getStringArray("termNames");
			}
			
			if (savedInstanceState.containsKey("selectedSpinnerItem")) {
				selectedSpinnerItem = savedInstanceState.getInt("selectedSpinnerItem");
			}
			
		}

		if (termNames == null || termNames.length == 0) {
			termsTask = new RetrieveSearchableTermsTask();
			termsTask.execute(getEllucianActivity().requestUrl);
            Utils.showProgressIndicator(getActivity());
            searchButton.setEnabled(false);
		} else {
			setTermsSpinnerAdapter(selectedSpinnerItem);
		}
			
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if (terms != null) {
			outState.putParcelableArray("terms", terms);
			outState.putStringArray("termNames", termNames);
			
			if (termsSpinner != null) {
				outState.putInt("selectedSpinnerItem", termsSpinner.getSelectedItemPosition());
			}
		}
		
		outState.putParcelableArrayList("locationFilters", (ArrayList<SearchFilter>)locationFilters);
		outState.putParcelableArrayList("levelFilters", (ArrayList<SearchFilter>)levelFilters);
		outState.putStringArrayList("selectedLocations", (ArrayList<String>)selectedLocations);
		outState.putStringArrayList("selectedLevels", (ArrayList<String>)selectedLevels);
		
	}
	
	@Override
	public void onDestroy() {
		if (termsTask != null && termsTask.getStatus() != AsyncTask.Status.FINISHED) { 
			Log.e(TAG, "Cancelling termsTask");
			if (termsTask.cancel(true)) {
				Log.e(TAG, "Cancelled");
			} else {
				Log.e(TAG, "failed to cancel");
			}
		}
		super.onDestroy();
	}

	private void setTermsSpinnerAdapter(int selectedItem) {
		termsAdapter = new ArrayAdapter<String>(RegistrationSearchFragment.this.getActivity(), 
				android.R.layout.simple_dropdown_item_1line, termNames );
		
		termsSpinner.setAdapter(termsAdapter);
		if (selectedItem > 0 && selectedItem < termsSpinner.getCount()) {
			termsSpinner.setSelection(selectedItem, true);
		}
		
	}
	
	private class RetrieveSearchableTermsTask extends AsyncTask<String, Void, TermsResponse> {

		@Override
		protected TermsResponse doInBackground(String... params) {
			String requestUrl = params[0];
			
			MobileClient client = new MobileClient(getActivity());
			requestUrl = client.addUserToUrl(requestUrl);
			requestUrl += "/terms";
			
			return client.getOpenTerms(requestUrl);
		}
		
		@Override
		protected void onPostExecute(TermsResponse termsResponse) {
			
			if (termsResponse != null) {
				terms = termsResponse.terms;
				if (terms != null && terms.length > 0) {
					
					termNames = new String[terms.length];
					for (int i = 0; i < terms.length; i++) {
						termNames[i] = terms[i].name;
					}
					
					setTermsSpinnerAdapter(-1);
					if (activity != null) {
						activity.openTerms = terms;
					}
					searchButton.setEnabled(true);
					
				} else {
					Log.e(TAG, "Terms array null or empty");
				}
				
			} else {
				Log.e(TAG, "Terms response is null");
			}

            Utils.hideProgressIndicator(getActivity());
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		sendView("Registration Search", getEllucianActivity().moduleName);
	}
	
	void setSearchFilters(ArrayList<String> selectedLocations, ArrayList<String> selectedLevels) {
		this.selectedLocations = selectedLocations;
		this.selectedLevels = selectedLevels;
	}
	
	ArrayList<String> getLocationNames() {
		if (locationFilters == null) {
			return null;
		}
		ArrayList<String> locationNames = new ArrayList<String>();		
		for (SearchFilter filter : locationFilters) {
			locationNames.add(filter.name);
		}
		return locationNames;
	}
	
	ArrayList<String> getLevelNames() {
		if (levelFilters == null) {
			return null;
		}
		ArrayList<String> levelNames = new ArrayList<String>();
		for (SearchFilter filter : levelFilters) {
			levelNames.add(filter.name);
		}
		return levelNames;
	}
	
	private ArrayList<String> getCodeList(List<SearchFilter> filterList, List<String> nameList) {
		ArrayList<String> codeList = new ArrayList<String>();
		if (nameList != null) {
			for (SearchFilter searchFilter: filterList) {
				if (nameList.contains(searchFilter.name)) {
					codeList.add(searchFilter.code);
				}
			}
		} else {
			for (SearchFilter searchFilter: filterList) {
				codeList.add(searchFilter.code);
			}
		}
		return codeList;
	}
}
