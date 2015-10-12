// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.registration;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDialogFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.util.Utils;

public class RefineSearchDialogFragment extends EllucianDialogFragment {
	public static final String TAG = RefineSearchDialogFragment.class.getSimpleName();
	
	private RegistrationActivity activity;
	private OnDoneFilteringListener listener;
	private View rootView;
	private ListView locationList;
	private ListAdapter locationAdapter;
	protected TextView headerView;
	private ListView levelList;
	private ListAdapter levelAdapter;
	private Button okButton;
	private Button cancelButton;
	private boolean okButtonEnabled;
	private ArrayList<String> selectedLocations;
	private ArrayList<String> selectedLevels;
	
	public interface OnDoneFilteringListener {
		void onDoneFiltering(ArrayList<String> locationFilters, ArrayList<String> levelFilters);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (RegistrationActivity) activity;
		// This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
        	listener = (OnDoneFilteringListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		okButtonEnabled = true;
		
		ArrayList<String> locationNames = getArguments().getStringArrayList("locationNames");
		ArrayList<String> levelNames = getArguments().getStringArrayList("levelNames");
		selectedLocations = getArguments().getStringArrayList("selectedLocations");
		selectedLevels = getArguments().getStringArrayList("selectedLevels");
		
		
		if (!locationNames.isEmpty()) {

			locationAdapter = new ArrayAdapter<String>(activity, 
					android.R.layout.simple_list_item_multiple_choice, 
					android.R.id.text1,
					locationNames.toArray(new String[locationNames.size()]));
		}
		
		
		if (!levelNames.isEmpty()) {
			
			levelAdapter = new ArrayAdapter<String>(activity, 
					android.R.layout.simple_list_item_multiple_choice, 
					android.R.id.text1,
					levelNames.toArray(new String[levelNames.size()]));
		}

	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.registration_refine_search_dialog_layout, container, false);		
		okButton = (Button) rootView.findViewById(R.id.ok_button);
		cancelButton = (Button) rootView.findViewById(R.id.cancel_button);	

		TextView emptyView = (TextView) inflater.inflate(R.layout.default_list_empty_view_layout, container, false);
		
		locationList = (ListView) rootView.findViewById(R.id.location_list);
		locationList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		locationList.setAdapter(locationAdapter);
		locationList.setEmptyView(emptyView);
		
		locationList.setOnItemClickListener(new onFilterListItemClickedListener());

		levelList = (ListView) rootView.findViewById(R.id.level_list);
		levelList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		levelList.setAdapter(levelAdapter);
		levelList.setEmptyView(emptyView);
		
		levelList.setOnItemClickListener(new onFilterListItemClickedListener());
		
		TextView locationHeader = (TextView) rootView.findViewById(R.id.location_header);
		locationHeader.setBackgroundColor(Utils.getAccentColor(activity));
		locationHeader.setTextColor(Utils.getSubheaderTextColor(activity));
		
		TextView levelHeader = (TextView) rootView.findViewById(R.id.level_header);
		levelHeader.setBackgroundColor(Utils.getAccentColor(activity));
		levelHeader.setTextColor(Utils.getSubheaderTextColor(activity));

		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION,
						GoogleAnalyticsConstants.ACTION_BUTTON_PRESS,
						"Done refining search", null, getEllucianActivity().moduleName);

				ArrayList<String> selectedLocations = new ArrayList<String>();
				for (int i = 0; i < locationList.getCount(); i++) {
					if (locationList.isItemChecked(i)) {
						String locationName = (String) locationList.getItemAtPosition(i);
						selectedLocations.add(locationName);
					} 
				}

				ArrayList<String> selectedLevels = new ArrayList<String>();
				for (int i = 0; i < levelList.getCount(); i++) {
					if (levelList.isItemChecked(i)) {
						String levelName = (String) levelList.getItemAtPosition(i);
						selectedLevels.add(levelName);
					} 
				}

				listener.onDoneFiltering(selectedLocations, selectedLevels);
				
				RefineSearchDialogFragment.this.dismiss();
			}
		});

		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				RefineSearchDialogFragment.this.dismiss();
			}
		});
		
		return rootView;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		// removes the default title view
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return dialog;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (selectedLocations != null) {
			for (int i = 0; i < locationList.getCount(); i++) {
				String location = (String) locationList.getItemAtPosition(i);
				boolean preSelected = selectedLocations.contains(location);
				locationList.setItemChecked(i, preSelected);
			}
		} else {
			for (int i = 0; i < locationList.getCount(); i++) {
				locationList.setItemChecked(i, true);
			}
		}
		
		if (selectedLevels != null) {
			for (int i = 0; i < levelList.getCount(); i++) {
				String location = (String) levelList.getItemAtPosition(i);
				boolean preSelected = selectedLevels.contains(location);
				levelList.setItemChecked(i, preSelected);
			}
		} else {
			for (int i = 0; i < levelList.getCount(); i++) {
				levelList.setItemChecked(i, true);
			}
		}		
		
		okButton.setEnabled(okButtonEnabled);

	}


	@Override
	public void onStart() {
		super.onStart();
		sendView("Registration Refine Search", getEllucianActivity().moduleName);
	}
	
	@Override
	public void onDestroyView() {
		// Trick to keep dialog open on rotate
		if (getDialog() != null && getRetainInstance())
			getDialog().setDismissMessage(null);
		super.onDestroyView();
	}
	
	private class onFilterListItemClickedListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
			if ((locationList.getCount() == 0 || locationList.getCheckedItemCount() > 0) && 
					(levelList.getCount() == 0 || levelList.getCheckedItemCount() > 0)) {
				okButtonEnabled = true;
			} else {
				okButtonEnabled = false;
			}
			okButton.setEnabled(okButtonEnabled);
		}
		
	}
}
