// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.registration;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.adapter.CheckableSectionedListAdapter;
import com.ellucian.mobile.android.adapter.SectionedListAdapter;
import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.client.registration.Section;

public class RegistrationCartListFragment extends EllucianDefaultListFragment {
	public static final String TAG = RegistrationCartListFragment.class.getSimpleName();
	
	private RegistrationActivity activity;
	private Button registerButton;
	private View eligibilityErrorView;
	protected boolean showEligibilityError;
	private String errorMessages;
	
	public RegistrationCartListFragment () {	
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (RegistrationActivity) activity;
	}
	
	public void setShowEligibilityError(boolean showError, String message) {
		showEligibilityError = showError;
		this.errorMessages = message;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_registration_cart_list, container, false);
		registerButton = (Button) rootView.findViewById(R.id.register);
		eligibilityErrorView = rootView.findViewById(R.id.eligibility_error_messsage_view);
		Bundle bundle = getArguments();
		if (bundle != null) {
			int emptyTextResId = bundle.getInt("emptyTextResId");
			if (emptyTextResId != 0) {
				TextView emptyView = (TextView) rootView.findViewById(android.R.id.empty);
				emptyView.setText(emptyTextResId);
			}
		}

		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {

				SectionedListAdapter sectionedAdapter = (SectionedListAdapter)parent.getAdapter();
				if (sectionedAdapter.getItemViewType(position) != SectionedListAdapter.TYPE_SECTION_HEADER) {
					Cursor cursor = (Cursor)sectionedAdapter.getItem(position);
					
					String sectionId = cursor.getString(cursor.getColumnIndex(RegistrationActivity.SECTION_ID));
					String termId = cursor.getString(cursor.getColumnIndex(RegistrationActivity.TERM_ID));

					Section section = activity.findSectionInCart(termId, sectionId);

					detailBundle = buildDetailBundle(section);
					
					showDetails(position);
				}
			}		
		});
		
		if (savedInstanceState != null) {
			showEligibilityError = savedInstanceState.getBoolean("showEligibilityError");
			errorMessages = savedInstanceState.getString("errorMessages");
		}

		showEligibilityErrorView(showEligibilityError);
		
	}
	
	@Override
	public void onStart() {
		super.onStart();
		sendView("Registration Cart list", getEllucianActivity().moduleName);
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		CheckableSectionedListAdapter adapter = (CheckableSectionedListAdapter) getListAdapter();
		if (!adapter.getCheckedPositions().isEmpty()) {
			showRegisterButton(true, adapter.getCheckedPositions().size());
		} else {
			showRegisterButton(false, 0);
		}
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("showEligibilityError", showEligibilityError);
		outState.putString("errorMessages", errorMessages);
	}
	
	
	public void showRegisterButton(boolean show, int numberShown) {
		if (show) {
		
			if (numberShown > 0) {
				registerButton.setText(getString(R.string.registration_register) + " (" + numberShown + ")");
			}
			if (!registerButton.isShown()) {				
				registerButton.setVisibility(View.VISIBLE);
			}
		} else {		
			registerButton.setVisibility(View.GONE);
		}	
		
	}
	
	protected void showEligibilityErrorView(boolean show) {
		if (show) {
			if (!TextUtils.isEmpty(errorMessages)) {
				TextView messagesView = (TextView) rootView.findViewById(R.id.messages);
				messagesView.setMovementMethod(ScrollingMovementMethod.getInstance());
				messagesView.setText(errorMessages);
			}
			
			eligibilityErrorView.setVisibility(View.VISIBLE);
		} else {
			eligibilityErrorView.setVisibility(View.GONE);
		}
	}

	@Override
	public Bundle buildDetailBundle(Object... objects) {
		Bundle bundle = new Bundle();

		Section section = (Section)objects[0];
		bundle.putParcelable(RegistrationActivity.SECTION, section);
		
		return bundle;
	}
	
	
	@Override
	public Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
		return RegistrationDetailFragment.class;	
	}
	
	@Override
	public Class<? extends EllucianDefaultDetailActivity> getDetailActivityClass() {
		return RegistrationDetailActivity.class;	
	}
	

}

