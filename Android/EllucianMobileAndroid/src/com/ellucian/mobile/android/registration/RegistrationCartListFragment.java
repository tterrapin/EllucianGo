// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.registration;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.adapter.CheckableSectionedListAdapter;
import com.ellucian.mobile.android.adapter.SectionedListAdapter;
import com.ellucian.mobile.android.app.EllucianDefaultDetailActivity;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.EllucianDefaultListFragment;
import com.ellucian.mobile.android.client.courses.Instructor;
import com.ellucian.mobile.android.client.courses.MeetingPattern;
import com.ellucian.mobile.android.client.registration.Section;
import com.ellucian.mobile.android.util.CalendarUtils;

public class RegistrationCartListFragment extends EllucianDefaultListFragment {
	public static final String TAG = RegistrationCartListFragment.class.getSimpleName();
	
	View registerButton;
	RegistrationActivity activity;
	
	public RegistrationCartListFragment () {	
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (RegistrationActivity) activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_registration_cart_list, container, false);
		registerButton = rootView.findViewById(R.id.register);
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
	}
	
	@Override
	public void onStart() {
		super.onStart();
		sendView("Registration Cart list", getEllucianActivity().moduleName);
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		if (!((CheckableSectionedListAdapter)getListAdapter()).getCheckedPositions().isEmpty()) {
			showRegisterButton(true);
		} else {
			showRegisterButton(false);
		}
		
	}
	
	
	public void showRegisterButton(boolean show) {
		if (show) {
			if (!registerButton.isShown()) {
				registerButton.setVisibility(View.VISIBLE);
			}
		} else {		
			registerButton.setVisibility(View.GONE);
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

