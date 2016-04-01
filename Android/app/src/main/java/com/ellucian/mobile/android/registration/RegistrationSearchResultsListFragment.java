// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.registration;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
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

public class RegistrationSearchResultsListFragment extends EllucianDefaultListFragment {
	
	private RegistrationActivity activity;
	private Button addToCartButton;
	private boolean newSearch;
	
	public RegistrationSearchResultsListFragment () {	
	}
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		this.activity = (RegistrationActivity) getActivity();
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
		
		if (newSearch) {
			mCurCheckPosition = 1;
			detailBundle = null;
			final ListView listView = getListView();
			Handler handler = new Handler();
	   		handler.post(new Runnable(){

				@Override
				public void run() {
					
					int positionToClick = 0;
					if (mDualPane) {
						positionToClick = mCurCheckPosition;	
					}
					// Reset and auto-select first on list if not empty
					// On non-dual-pane layouts we force click the header instead which
					// will scroll to the correct place and clear the selected
					if (!getListAdapter().isEmpty()) {
						listView.performItemClick(null, positionToClick,
								listView.getAdapter().getItemId(positionToClick));
		    		}
					// After force click scroll list to top to show header
					listView.smoothScrollToPosition(0);
					listView.setSelection(0);
							
				}
	   			
	   		});
	   		newSearch = false;
		}
		
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
		bundle.putString(RegistrationDetailFragment.REQUESTING_LIST_FRAGMENT,
                this.getClass().getSimpleName());
        bundle.putString(RegistrationDetailFragment.REGISTRATION_MODULE_ID,
                ((EllucianActivity)getActivity()).moduleId);

        return bundle;
	}
	
	public void setNewSearch(boolean value) {
		newSearch = value;
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
