/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.directory;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianListFragment;
import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.client.directory.DirectoryResponse;
import com.ellucian.mobile.android.client.directory.Entry;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.OnQueryListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class DirectoryListFragment extends EllucianListFragment implements OnQueryListener{
	private static final String TAG = DirectoryListFragment.class.getSimpleName();
	
	private boolean mDualPane;
    private int mCurCheckPosition = -1;
    
    private DirectoryListActivity activity;
    private RetrieveDirectoryInfoTask directoryTask;
    
    private View rootView;
    private ListView listView;
    
    private String[] nameList;
    private Entry[] entries;
    
    
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof DirectoryListActivity)) {
            throw new IllegalStateException("Activity must implement DirectoryListActivity");
        }
        this.activity = (DirectoryListActivity) activity;
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	rootView =  inflater.inflate(R.layout.fragment_default_list, container, false);
		return rootView;	
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView = getListView();
        ((TextView)getListView().getEmptyView()).setText(R.string.directory_search);
        
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mCurCheckPosition = savedInstanceState.getInt("curChoice", -1);
            nameList = (String[]) savedInstanceState.getSerializable("nameList");
            entries = (Entry[]) savedInstanceState.getSerializable("entries");
        }
        
        if (nameList == null) {
        	nameList = new String[0];
        }
        
        

        // Start with an empty array until the search is made     
        getListView().setAdapter(new ArrayAdapter<String>(getActivity(),
        		R.layout.default_single_line_row, R.id.title, nameList));
        
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> list, View v, int position,
					long id) {
				Log.d(TAG, "Selected Item index: " + position);
		        showDetails(position);
				
			}
        	
		});

			
        

        // Check to see if we have a frame in which to embed the details
        // fragment directly in the containing UI.
        View detailsFrame = getActivity().findViewById(R.id.frame_extra);
        mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        
        
        if (mDualPane && entries != null && mCurCheckPosition != -1) {        	
            // Make sure our UI is in the correct state.
            showDetails(mCurCheckPosition);
        }
        
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curChoice", mCurCheckPosition);
        outState.putSerializable("nameList", nameList);
        outState.putSerializable("entries", entries);
    }


    /**
     * Helper function to show the details of a selected item, either by
     * displaying a fragment in-place in the current UI, or starting a
     * whole new activity in which it is displayed.
     */
	private void showDetails(int index) {
        mCurCheckPosition = index;
        getListView().setSelection(mCurCheckPosition);
        
        Bundle bundle = buildBundle(index);
        if (mDualPane) {
            // We can display everything in-place with fragments, so update
            // the list to highlight the selected item and show the data.
        	//getListView().setItemChecked(index, true);

            // Check what fragment is currently shown, replace if needed.
            DirectoryDetailFragment details = (DirectoryDetailFragment)
                    getFragmentManager().findFragmentById(R.id.frame_extra);
            //if (details == null || details.getShownIndex() != index) {
                // Make new fragment to show this selection.
                details = DirectoryDetailFragment.newInstance(index);
                
                details.setArguments(bundle);
                
                // Execute a transaction, replacing any existing fragment
                // with this one inside the frame.
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.frame_extra, details);

                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
           // }

        } else {
            // Otherwise we need to launch a new activity to display
            // the dialog fragment with selected text.
            Intent intent = new Intent();
            intent.setClass(getActivity(), DirectoryDetailActivity.class);
            intent.putExtras(bundle);
            //intent.putExtra("index", index);
            
            startActivity(intent);
        }
    }

	@Override
	public void doQuery(String query) {
		// Clear details and reset position before query
		clearCurrentDetailFragment();
		mCurCheckPosition = -1;
		
		if (!TextUtils.isEmpty(query)) {
			// If there is a current running query, cancel it and then create a new one.
			if (directoryTask != null) {
				directoryTask.cancel(true);
			}
			String requestUrl;
			if (activity.directoryType.equals(DirectoryCategoriesFragment.DIRECTORY_TYPE_STUDENT)) {
				requestUrl = activity.studentUrl;
			} else if (activity.directoryType.equals(DirectoryCategoriesFragment.DIRECTORY_TYPE_FACULTY)) {
				requestUrl = activity.facultyUrl;
			} else {
				// Default to all search
				requestUrl = activity.allUrl;
			}
			
			directoryTask = new RetrieveDirectoryInfoTask(getActivity());
			directoryTask.execute(requestUrl, query);
		} else {
			Log.d(TAG, "query was null or empty, no request sent");
		}	
	}
	
	public void clearList() {
		((TextView)getListView().getEmptyView()).setText(R.string.directory_search);
		
		nameList = new String[0];
	
		getListView().setAdapter(new ArrayAdapter<String>(getActivity(),
				R.layout.default_single_line_row, R.id.title, nameList));
		
		clearCurrentDetailFragment();
	}
	
	public void setInitialCursorPosition(boolean resetPosition) {
    	// Adapter been has changed by either filtering or a search must update and selected to first on list
    	if (resetPosition) {
    		mCurCheckPosition = 0;		
    	}
    	
    	getListView().setSelection(mCurCheckPosition);
    	
    	if (mDualPane) { 
    		
    		View selectedView = getListView().getChildAt(mCurCheckPosition);

    		if (getListAdapter().isEmpty()) {
    			clearCurrentDetailFragment();
    		} else {
    			getListView().performItemClick(selectedView, mCurCheckPosition, 0);
    		}
    	} 
    }
    
    private void clearCurrentDetailFragment() {
    	Fragment details = getFragmentManager().findFragmentById(R.id.frame_extra);
    	if (details != null) {
        	FragmentTransaction ft = getFragmentManager().beginTransaction();
        	ft.remove(details);
        	ft.commit();
    	}
    }
	
	private Bundle buildBundle(int index) {
		Bundle bundle = new Bundle();
		
		Entry entry = entries[index];
		
		// Use displayName from details, if missing use the created name of first and last name.
		if (!TextUtils.isEmpty(entry.displayName)) {
			bundle.putString(Extra.DIRECTORY_DISPLAY_NAME, entry.displayName);
		} else {
			bundle.putString(Extra.DIRECTORY_DISPLAY_NAME, nameList[index]);
		}
		if (!TextUtils.isEmpty(entry.title)) {
			bundle.putString(Extra.DIRECTORY_TITLE, entry.title);
		}
		// TODO- change to entry.phone when fixed on server
		if (!TextUtils.isEmpty(entry.number)) {
			bundle.putString(Extra.DIRECTORY_PHONE, entry.number); 
		} else if (!TextUtils.isEmpty(entry.phone)) {
			bundle.putString(Extra.DIRECTORY_PHONE, entry.phone); 
		}
		if (!TextUtils.isEmpty(entry.mobile)) {
			bundle.putString(Extra.DIRECTORY_MOBILE, entry.mobile);
		}
		if (!TextUtils.isEmpty(entry.email)) {
			bundle.putString(Extra.DIRECTORY_EMAIL, entry.email);
		}
		
		//Build formatted address
		String address = "";
		if (!TextUtils.isEmpty(entry.street)) {
			address += entry.street.replace("\\n", "\n");
		}
		if (!TextUtils.isEmpty(entry.city)) {
			if (!TextUtils.isEmpty(address)) {
				address += "\n";
			}
			address += entry.city;
			if (!TextUtils.isEmpty(entry.state)) {
				address += ", " + entry.state;
			}
			// TODO - Once the json fields have been setting this can be fixed.
			if (!TextUtils.isEmpty(entry.zip)) {
				address += " " + entry.zip;
			} else if (!TextUtils.isEmpty(entry.postalCode)) {
				address += " " + entry.postalCode;
			}
		}
		
		if (!TextUtils.isEmpty(address)) {
			bundle.putString(Extra.DIRECTORY_ADDRESS, address);
		}
		
		if (!TextUtils.isEmpty(entry.department)) {
			bundle.putString(Extra.DIRECTORY_DEPARTMENT, entry.department);
		}
		
		if (!TextUtils.isEmpty(entry.office)) {
			bundle.putString(Extra.DIRECTORY_OFFICE, entry.office);
		}
		
		if (!TextUtils.isEmpty(entry.room)) {
			bundle.putString(Extra.DIRECTORY_ROOM, entry.room);
		}
		
		return bundle;
		
	}
	
	
	private class RetrieveDirectoryInfoTask extends AsyncTask<String, Void, DirectoryResponse> {
		final Activity activity;
		
		RetrieveDirectoryInfoTask(Activity activity) {
			this.activity = activity;
		}
		
		// params (requestUrl, directoryType, query)
		@Override
		protected DirectoryResponse doInBackground(String... params) {
			DirectoryResponse response = null;
			
			String requestUrl = params[0];
			String query = params[1];
			
			if (!TextUtils.isEmpty(requestUrl) && !TextUtils.isEmpty(query)) {	
				String modifiedUrl = requestUrl;

				String encodedQuery = null;
				try {
					encodedQuery = URLEncoder.encode(query, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (!TextUtils.isEmpty(encodedQuery)) {
					modifiedUrl += "?searchString=" + encodedQuery;
				}
				
				MobileClient client = new MobileClient(activity);
				response = client.searchDirectory(modifiedUrl);
				
			} else {
				Log.d(TAG, "requestUrl or query is missing, no request sent.");
				Log.d(TAG, "requestUrl: " + requestUrl);
				Log.d(TAG, "query: " + query);		
			}
			return response;		
		}
		
		@Override
		protected void onPostExecute(DirectoryResponse response) {
			
			if (response != null) {
				entries = response.entries;
				if (entries != null) {
					nameList = new String[entries.length];
					for (int i = 0; i < entries.length; i++) {
						Entry entry = entries[i];
						String displayName = getString(R.string.default_first_last_name_format,
													entry.firstName, 
													entry.lastName);
						nameList[i] = displayName;
					}
				} else {
					Log.d(TAG, "entries from response was null.");
					nameList = new String[0];
				}
				
				if (getActivity() != null && listView != null) {
					listView.setAdapter(new ArrayAdapter<String>(getActivity(),
						R.layout.default_single_line_row, R.id.title, nameList));
				} else {
					Log.e(TAG, "Activity or listView is null, and can not set adapter");
				}
				
			} else {
				Log.d(TAG, "response is null.");
				if (getActivity() != null && listView != null) {
					TextView view = (TextView) listView.getEmptyView();
					view.setText(R.string.directory_no_results);
				} else {
					Log.e(TAG, "Activity or listView is null, and can not set empty view");
				}
			}
		}
	}


	@Override
	public void onStart() {
		super.onStart();
		sendView("Directory page", getEllucianActivity().moduleName);
	}
	
	
	
}
