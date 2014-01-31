// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.registration;

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
import android.widget.Toast;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianFragment;
import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.client.registration.OpenTerm;
import com.ellucian.mobile.android.client.registration.TermsResponse;

public class RegistrationSearchFragment extends EllucianFragment  {
	private static final String TAG = RegistrationSearchFragment.class.getSimpleName();
	
	protected RegistrationActivity activity;
	protected View rootView;
	protected Spinner termsSpinner;
	protected EditText searchField;
	protected Button searchButton;
	protected RetrieveSearchableTermsTask termsTask;
	protected ArrayAdapter<String> termsAdapter;
	protected String[] termNames;
	protected OpenTerm[] terms;
	protected int selectedSpinnerItem = -1;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_registration_search, container, false);
		termsSpinner = (Spinner) rootView.findViewById(R.id.terms_spinner);
		searchField = (EditText) rootView.findViewById(R.id.search_field);
		searchButton = (Button) rootView.findViewById(R.id.search_button);
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
				if (!TextUtils.isEmpty(selectedTermId) && !TextUtils.isEmpty(pattern)) {
					
					activity.startSectionSearch(selectedTermId, pattern);
				} else {
					Toast fillInMessage = Toast.makeText(getActivity(), R.string.registration_fill_in_message, Toast.LENGTH_SHORT);
					fillInMessage.setGravity(Gravity.CENTER, 0, 0);
					fillInMessage.show();
				}	
			}
			
		});
		
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
			getActivity().setProgressBarIndeterminateVisibility(true);
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
			
			MobileClient client = new MobileClient(getActivity().getApplication());
			requestUrl = client.addUserToUrl(requestUrl);
			requestUrl += "/terms";
			
			client.getOpenTerms(requestUrl);
			
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
					Log.e(TAG, "Terms array null or emtpy");
				}
				
			} else {
				Log.e(TAG, "Terms response is null");
			}
			
			getActivity().setProgressBarIndeterminateVisibility(false);
		}
	}

}
