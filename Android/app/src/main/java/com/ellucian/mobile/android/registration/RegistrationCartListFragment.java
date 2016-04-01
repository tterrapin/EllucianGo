// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.registration;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
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
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.client.registration.Section;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

public class RegistrationCartListFragment extends EllucianDefaultListFragment {
	public static final String TAG = RegistrationCartListFragment.class.getSimpleName();
	
	private RegistrationActivity activity;
	private Button registerButton;
	private View eligibilityErrorView;
	private boolean showEligibilityError;
	private String errorMessages;
	
	public RegistrationCartListFragment () {	
	}
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		this.activity = (RegistrationActivity) getActivity();
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
		eligibilityErrorView = rootView.findViewById(R.id.eligibility_error_message_view);
		
		registerButton.setBackgroundColor(Utils.getPrimaryColor(activity));
		registerButton.setTextColor(Utils.getHeaderTextColor(activity));
		
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
	public void showDetails(int index) {
		mCurCheckPosition = index;

		if (mDualPane) {
			//We can display everything in-place with fragments

			EllucianDefaultDetailFragment details = (EllucianDefaultDetailFragment)
					getFragmentManager().findFragmentById(R.id.frame_extra);
			if (detailBundle == null) {
				detailBundle = new Bundle();
			}
			detailBundle.putString(RegistrationDetailFragment.REQUESTING_LIST_FRAGMENT, "RegistrationCartListFragment");
			details = getDetailFragment(detailBundle, index);

			// Execute a transaction, replacing any existing fragment
			// with this one inside the frame.
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.frame_extra, details);

			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			ft.commit();

		} else {
			// Otherwise we need to launch a new activity to display
			// the dialog fragment with selected text.

			Intent intent = new Intent();
			intent.setClass(getActivity(), getDetailActivityClass());
			intent.putExtras(detailBundle); 
			intent.putExtra("index", index);
			intent = addExtras(intent);
			// startActivityForResult for RegistrationDetailActivity to handle remove requests
			intent.putExtra(RegistrationDetailFragment.REQUESTING_LIST_FRAGMENT, "RegistrationCartListFragment");
			getActivity().startActivityForResult(intent, RegistrationActivity.REGISTRATION_DETAIL_REQUEST_CODE);

		}
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
				registerButton.setText(getString(R.string.label_with_count_format, 
											getString(R.string.registration_register),
											numberShown));
			}
			if (!registerButton.isShown()) {				
				registerButton.setVisibility(View.VISIBLE);
			}
		} else {		
			registerButton.setVisibility(View.GONE);
		}	
		
	}
	
	protected void setRegisterButtonEnabled(boolean enabled) {
		registerButton.setEnabled(enabled);
	}
	
	private void showEligibilityErrorView(boolean show) {
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
		
		bundle.putString(Extra.MODULE_NAME, getEllucianActivity().moduleName);

		Section section = (Section)objects[0];
		bundle.putParcelable(RegistrationActivity.SECTION, section);
        bundle.putString(RegistrationDetailFragment.REGISTRATION_MODULE_ID,
                ((EllucianActivity)getActivity()).moduleId);

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

