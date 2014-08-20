// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.registration;

import android.app.Activity;
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
import com.ellucian.mobile.android.util.Utils;

public class RegistrationSearchResultsListFragment extends EllucianDefaultListFragment {
	
	protected RegistrationActivity activity;
	protected Button addToCartButton;
	
	public RegistrationSearchResultsListFragment () {	
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (RegistrationActivity) activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_registration_search_results_list, container, false);
		addToCartButton = (Button) rootView.findViewById(R.id.add_to_cart);
		TextView emptyView = (TextView) rootView.findViewById(android.R.id.empty);
		emptyView.setText(R.string.no_results_found);
		
		addToCartButton.setBackgroundColor(Utils.getPrimaryColor(activity));
		addToCartButton.setTextColor(Utils.getHeaderTextColor(activity));
		
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
		CheckableSectionedListAdapter adapter = (CheckableSectionedListAdapter) getListAdapter();
		if (adapter != null && !adapter.getCheckedPositions().isEmpty()) {
			showAddToCartButton(true, adapter.getCheckedPositions().size());
		} else {
			showAddToCartButton(false, 0);
		}
		
	}
		
	public void showAddToCartButton(boolean show, int numberShown) {
		
		if (show) {
			if (numberShown > 0) {
				addToCartButton.setText(getString(R.string.label_with_count_format, 
											getString(R.string.registration_add_to_cart),
											numberShown));
			}
			if (!addToCartButton.isShown()) {
				addToCartButton.setVisibility(View.VISIBLE);
			}
		} else {		
			addToCartButton.setVisibility(View.GONE);
		}	
		
	}
	
	protected void setAddToCartButtonEnabled(boolean enabled) {
		addToCartButton.setEnabled(enabled);
	}

	@Override
	public Bundle buildDetailBundle(Object... objects) {
		Bundle bundle = new Bundle();
		
		bundle.putString(Extra.MODULE_NAME, getEllucianActivity().moduleName);

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
