// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.registration;

import android.database.Cursor;
import android.os.Bundle;
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
import com.ellucian.mobile.android.util.Extra;

public class RegistrationSearchResultsListFragment extends EllucianDefaultListFragment {
	private static final String TAG = RegistrationSearchResultsListFragment.class.getSimpleName();
	
	protected RegistrationActivity activity;
	protected Button addToCartButton;
	
	public RegistrationSearchResultsListFragment () {	
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_registration_search_results_list, container, false);
		addToCartButton = (Button) rootView.findViewById(R.id.add_to_cart);
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
		activity = (RegistrationActivity) getActivity();
		
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {

				SectionedListAdapter sectionedAdapter = (SectionedListAdapter)parent.getAdapter();
				if (sectionedAdapter.getItemViewType(position) != SectionedListAdapter.TYPE_SECTION_HEADER) {
					Cursor cursor = (Cursor)sectionedAdapter.getItem(position);
					
					String sectionId = cursor.getString(cursor.getColumnIndex(RegistrationActivity.SECTION_ID));
					String termId = cursor.getString(cursor.getColumnIndex(RegistrationActivity.TERM_ID));

					Section section = activity.findSectionInResults(termId, sectionId);

					detailBundle = buildDetailBundle(section);
					
					showDetails(position);
				}
			}		
		});
	}
	
	@Override
	public void onStart() {
		super.onStart();
		sendView("Registration search results list", getEllucianActivity().moduleName);
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		if (getListAdapter() != null && !((CheckableSectionedListAdapter)getListAdapter()).getCheckedPositions().isEmpty()) {
			showAddToCartButton(true);
		} else {
			showAddToCartButton(false);
		}
		
	}
		
	public void showAddToCartButton(boolean show) {
		
		if (show) {
			if (!addToCartButton.isShown()) {
				addToCartButton.setVisibility(View.VISIBLE);
			}
		} else {		
			addToCartButton.setVisibility(View.GONE);
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
