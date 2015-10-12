/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.app;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.SimpleCursorTreeAdapter.ViewBinder;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.util.Extra;

public abstract class EllucianDefaultExpandableListFragment extends EllucianExpandableListFragment {
	private static final String TAG = EllucianDefaultExpandableListFragment.class.getSimpleName();

	private View rootView;
	private OnItemClickListener listener;
	private ViewBinder viewBinder;

	private boolean mDualPane;
	private int mCurCheckPosition = 0;
	private int mCurGroupPosition = 0;
	private Bundle detailBundle;
	
	private boolean initialLoad;

	public EllucianDefaultExpandableListFragment() {
	}
	
	public static EllucianDefaultExpandableListFragment newInstance(Context context, String fname, Bundle args) {
		Log.d(TAG, "newInstance (1)");
		EllucianDefaultExpandableListFragment fragment = newInstance(context, fname, args, 0);
		return fragment;
	}
	
	/** Variable "fname" should be the class name of an EllucianDefaultListFragment subclass
	 *  example -  SubclassFragmentOfDefaultListFragment.class.getName()
	 */
	private static EllucianDefaultExpandableListFragment newInstance(Context context, String fname, Bundle args, int emptyTextResId) {
		Log.d(TAG, "newInstance (2)");
		EllucianDefaultExpandableListFragment fragment;

		if (TextUtils.isEmpty(fname)) {
			fname = EllucianDefaultDetailFragment.class.getName();
		}
		Log.d(TAG, "Creating new instance for class: " + fname);

		if (args == null) {
			args = new Bundle();
		}

		args.putInt("emptyTextResId",  emptyTextResId);

		fragment = (EllucianDefaultExpandableListFragment) EllucianExpandableListFragment.instantiate(context,  fname, args);

		return fragment;

	}

	public View getRootView() {
		Log.d(TAG, "getRootView");
		return rootView;
	}
	
	public void setEmptyView(View view) {
		Log.d(TAG, "setEmptyView");
		getExpandableListView().setEmptyView(view);
	}
	
	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		Log.d(TAG, "setOnItemClickListener");
		this.listener = onItemClickListener;
	}
	
	public void setViewBinder(ViewBinder binder) {
		Log.d(TAG, "setViewBinder");
		this.viewBinder = binder;
	}
	
	public int getCurrentPosition() {
		return mCurCheckPosition;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		rootView = inflater.inflate(R.layout.fragment_default_expandable_list, container, false);
		
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
		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		// Must keep track of activity initial load state so no items are selected on rotation until an item has been selected
		initialLoad = true;
		
		getExpandableListView().setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);
		
		if (listener != null) {
			getExpandableListView().setOnItemClickListener(listener);
		} else {
			getExpandableListView().setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

				@Override
				public boolean onChildClick(ExpandableListView parent, View v,
						int groupPosition, int childPosition, long id) {
					
					SimpleCursorTreeAdapter treeAdapter = (SimpleCursorTreeAdapter)parent.getExpandableListAdapter();
					Cursor childCursor = treeAdapter.getChild(groupPosition, childPosition);
					detailBundle = buildDetailBundle(childCursor);
					if (detailBundle == null) {
						detailBundle = getDefaultBundle();
					}
					
					initialLoad = false;
					List<Boolean> expandedGroups = getExpandedGroups();
					// Set list in a Neutral state before collecting the flat position
					collapseTree(groupPosition);
					// Reset open groups
					expandGroups(expandedGroups);
					int index = parent.getFlatListPosition(
							ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));
					mCurGroupPosition = groupPosition;
					showDetails(index);
					
					return false;
				}
			});
		}
		
		if (viewBinder != null) {
			((SimpleCursorTreeAdapter)getExpandableListView().getExpandableListAdapter()).setViewBinder(viewBinder);
		}
		
		View detailsFrame = getActivity().findViewById(R.id.frame_extra);
		mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
		
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mCurCheckPosition = savedInstanceState.getInt("checkPosition", 0);
            mCurGroupPosition = savedInstanceState.getInt("groupPosition", 0);
            detailBundle = savedInstanceState.getBundle("detailsBundle");
            initialLoad = savedInstanceState.getBoolean("initialLoad");
            
            if (detailBundle != null && mDualPane) {
            	// Make sure showing correct details
	            showDetails(mCurCheckPosition);
            }
        }
        
	}
	
	private void collapseTree() {
		Log.d(TAG, "collapseTree");
		ExpandableListView listView = getExpandableListView();
		//int groupCount = listView.getExpandableListAdapter().getGroupCount();
		
		for (int i = 0; i < listView.getCount(); i++) {
			listView.collapseGroup(i);
		}
	}
	
	private void collapseTree(int excludedPosition) {
		Log.d(TAG, "collapseTree");
		ExpandableListView listView = getExpandableListView();
		int groupCount = listView.getExpandableListAdapter().getGroupCount();

		for (int i = 0; i < groupCount; i++) {
			if (i != excludedPosition) {
				listView.collapseGroup(i);
			}
		}		
	}

	private void expandTree() {
		Log.d(TAG, "expandTree");
		ExpandableListView listView = getExpandableListView();
		for (int i = 0; i < listView.getCount(); i++) {
			listView.expandGroup(i);
		}
	}
	
	private void expandGroups(List<Boolean> expandedGroups) {
		if (expandedGroups != null && !expandedGroups.isEmpty()) {
			ExpandableListView listView = getExpandableListView();
			int groupCount = listView.getExpandableListAdapter().getGroupCount();
			for (int i = 0; i < groupCount; i++) {
				if (expandedGroups.get(i)) {
					listView.expandGroup(i);
				}
			}
		}
	}
	
	private List<Boolean> getExpandedGroups() {
		List<Boolean> expandedGroups = new ArrayList<Boolean>();
		ExpandableListView listView = getExpandableListView();
		int groupCount = listView.getExpandableListAdapter().getGroupCount();

		for (int i = 0; i < groupCount; i++) {
			if (listView.isGroupExpanded(i)) {
				expandedGroups.add(i, true);
			} else {
				expandedGroups.add(i, false);
			}
		}	
		
		return expandedGroups;
	}

	@Override
    public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putInt("checkPosition", mCurCheckPosition);
        outState.putInt("groupPosition", mCurGroupPosition);
        outState.putBundle("detailsBundle", detailBundle);
        outState.putBoolean("initialLoad", initialLoad);
    }
	
    /**
     * Helper function to show the details of a selected item, either by
     * displaying a fragment in-place in the current UI, or starting a
     * whole new activity in which it is displayed.
     */
	private void showDetails(int index) {
		Log.d(TAG, "showDetails");
        mCurCheckPosition = index;
        
        
        if (mDualPane) {
        	Log.d(TAG, "  dual pane view");
            // We can display everything in-place with fragments, so update
            // the list to highlight the selected item and show the data.
        	getExpandableListView().setItemChecked(mCurCheckPosition, true);
            
            EllucianDefaultDetailFragment details = (EllucianDefaultDetailFragment)
                    getFragmentManager().findFragmentById(R.id.frame_extra);
            
        	
            details = getDetailFragment(detailBundle, index);

            // Execute a transaction, replacing any existing fragment
            // with this one inside the frame.
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.frame_extra, details);

            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();

        } else {
            // Otherwise we need to launch a new activity to display
            // the dialog fragment with selected text.
        	
        	Log.d(TAG, "single pane view, new intent");
        	
            Intent intent = new Intent();
            intent.setClass(getActivity(), getDetailActivityClass());
            intent.putExtras(detailBundle); 
            intent.putExtra("index", index);
            intent = addExtras(intent);
            startActivity(intent);
        }
    }
    
    public void setInitialCursorPosition(boolean resetPosition) {
    	// Adapter been updated, must set state and UI correctly   	
    	if (resetPosition) { 
    		getExpandableListView().setItemChecked(mCurCheckPosition, false);
    		collapseTree();
    		initialLoad = true;
    		detailBundle = null;
    		clearCurrentDetailFragment();
    	} else {
    		if (!initialLoad) {
	    		setSelected();
    		}
    	}   	
    }
    
    public void setInitialCursorPositionAfterQuery(boolean clearList) {
    	// Adapter been updated do to a query, must set state and UI correctly  
    	if (clearList) { 		
    		getExpandableListView().setItemChecked(mCurCheckPosition, false);
    		collapseTree();
    		expandTree();
    	} else {
    		if (!initialLoad) {
	        	setSelected();
    		} else {
    			expandTree();
    		}
    	} 	
    }
    
    private void setSelected() {
    	Log.d(TAG, "Group position: " + mCurGroupPosition);
		Log.d(TAG, "Check position: " + mCurCheckPosition);
		if (mDualPane) {
			// Must set list back in the same state when you captured the Checked Position
			collapseTree();
	    	getExpandableListView().performItemClick(null, mCurGroupPosition, 0);
			getExpandableListView().setItemChecked(mCurCheckPosition, true);
		} else {
    		getExpandableListView().expandGroup(mCurGroupPosition);
    	}
    }


    public void clearCurrentDetailFragment() {
    	Fragment details = getFragmentManager().findFragmentById(R.id.frame_extra);
    	if (details != null) {
        	FragmentTransaction ft = getFragmentManager().beginTransaction();
        	ft.remove(details);
        	ft.commit();
    	}
    }
    
    private Bundle getDefaultBundle() {
		Log.d(TAG, "getDefaultBundle");
    	Bundle bundle = new Bundle();
		String title = "TITLE";
		String date = "Today";
		String content = "This is content";
		
		bundle.putString(Extra.TITLE, title);
		bundle.putString(Extra.DATE, date);
		bundle.putString(Extra.CONTENT, content);
		
		return bundle;
    }
    
    /** Override to return custom Bundle */
	protected Bundle buildDetailBundle(Cursor cursor) {
		Log.d(TAG, "buildDetailBundle");
		Bundle bundle = null;
		return bundle;
	}
    
    /** Override to return a subclass of EllucianDefaultDetailFragment created by EllucianDefaultDetailFragment.newInstance
     *  See EllucianDefaultDetailFragment.newInstance for more information
     *  If you are only making changes to the class name, override getDetailFragmentClass() instead
     */
	private EllucianDefaultDetailFragment getDetailFragment(Bundle args, int index) {
		Log.d(TAG, "getDetailFragment");
		
		return EllucianDefaultDetailFragment.newInstance(getActivity(), 
				getDetailFragmentClass().getName(), args, index);
	}
	
	/** Override to return the class of an EllucianDefaultDetailFragment subclass */
	protected Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
		Log.d(TAG, "getDetailFragmentClass");
		return EllucianDefaultDetailFragment.class;	
	}
	
	/** Override to return the class of an EllucianDefaultDetailActivity subclass */
	protected Class<? extends EllucianDefaultDetailActivity> getDetailActivityClass() {
		Log.d(TAG, "getDetailActivityClass");
		return EllucianDefaultDetailActivity.class;	
	}

	/** Override to return additional extras */
	private Intent addExtras(Intent intent) {
		Log.d(TAG, "addExtras");
		return intent;
	}

}
