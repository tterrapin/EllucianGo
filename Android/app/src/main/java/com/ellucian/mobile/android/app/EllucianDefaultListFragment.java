/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.util.Extra;

/**
 * This class can be extended in order to set custom activities and fragments.
 * The child class needs to follow the constructor rule below.
 * Many methods are intended to be overridden.
 * 
 * @author Jared Higley
 *
 */

public class EllucianDefaultListFragment extends EllucianListFragment {
	private static final String TAG = EllucianDefaultListFragment.class.getSimpleName();
	
	protected View rootView;
	private OnItemClickListener listener;
	private ViewBinder viewBinder;
	
	protected boolean mDualPane;
	protected int mCurCheckPosition = 0;
	protected Bundle detailBundle;
	private boolean showDualPaneDetailOnLoad = true;
	
	/** A subclass of EllucianDefaultDetailFragment must have an empty constructor */
	public EllucianDefaultListFragment() {
	}
	
	public static EllucianDefaultListFragment newInstance(Context context, String fname, Bundle args) {
		EllucianDefaultListFragment fragment = newInstance(context, fname, args, 0);
		return fragment;
	}
	
	/** Variable "fname" should be the class name of an EllucianDefaultListFragment subclass
	 *  example -  SubclassFragmentOfDefaultListFragment.class.getName()
	 */
	private static EllucianDefaultListFragment newInstance(Context context, String fname, Bundle args, int emptyTextResId) {
		EllucianDefaultListFragment fragment;
		
		if (TextUtils.isEmpty(fname)) {
			fname = EllucianDefaultListFragment.class.getName();
		}
		Log.d(TAG, "Creating new instance for class: " + fname);
		
		if (args == null) {
			args = new Bundle();
		}
		
		args.putInt("emptyTextResId", emptyTextResId);
		
		fragment = (EllucianDefaultListFragment) Fragment.instantiate(context, fname, args);

        return fragment;
	
    }
	
	public View getRootView() {
		return rootView;
	}
	
	public void setEmptyView(View view) {
		getListView().setEmptyView(view);
	}
	
	public void setOnItemClickListener(OnItemClickListener listener) {
		this.listener = listener;
	}
	
	public void setViewBinder(ViewBinder binder) {
		this.viewBinder = binder;
	}
	
	public int getCurrentPosition() {
		return mCurCheckPosition;
	}
	
	public Bundle getDetailBundle() {
		return detailBundle;
	}
	
	public void setDetailBundle(Bundle bundle) {
		detailBundle = bundle;
	}
	
	public boolean isShowDualPaneDetailOnLoad() {
		return showDualPaneDetailOnLoad;
	}
	
	public void setShowDualPaneDetailOnLoad(boolean value) {
		showDualPaneDetailOnLoad = value;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_default_list, container, false);

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
		
		listener = buildOnItemClickListener();
		if (listener != null) {
			getListView().setOnItemClickListener(listener);
		} else {
			getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
					
					Cursor cursor = ((SimpleCursorAdapter)parent.getAdapter()).getCursor();
					cursor.moveToPosition(position);
					
					detailBundle = buildDetailBundle(cursor);
					
					if (detailBundle == null) {
						detailBundle = getDefaultBundle();
					}
					
					showDetails(position);
					
				}
				
				
			});
		}
		
		if (viewBinder != null) {
			((SimpleCursorAdapter)getListView().getAdapter()).setViewBinder(viewBinder);
		}
		
		// Check to see if we have a frame in which to embed the details
        // fragment directly in the containing UI.
		View detailsFrame = getActivity().findViewById(R.id.frame_extra);
		mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
        Log.d(TAG, "mDualPane: " + mDualPane);
        
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
            detailBundle = savedInstanceState.getBundle("detailBundle");
            showDualPaneDetailOnLoad = savedInstanceState.getBoolean("showDualPaneDetailOnLoad", true);
                     
        }

        // CoordinatorLayout works with RecyclerView and any other scrolling views that support
        // NestedScrollView.
        // This is a workaround to get CoordinatorLayout to work with ListView on Android L+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getListView().setNestedScrollingEnabled(true);
        }

        if (detailBundle != null && mDualPane) {
        	// Make sure our UI is in the correct state.
        	if (showDualPaneDetailOnLoad) {
	        	showDetails(mCurCheckPosition);
	            getListView().setSelection(mCurCheckPosition);
        	}
        }       
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curChoice", mCurCheckPosition);
        outState.putBundle("detailBundle", detailBundle);
        outState.putBoolean("showDualPaneDetailOnLoad", showDualPaneDetailOnLoad);
    }
	
	

    /**
     * Helper function to show the details of a selected item, either by
     * displaying a fragment in-place in the current UI, or starting a
     * whole new activity in which it is displayed.
     */
	protected void showDetails(int index) {
        mCurCheckPosition = index;

        if (mDualPane) {
            //We can display everything in-place with fragments
            
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
        	
            Intent intent = new Intent();
            intent.setClass(getActivity(), getDetailActivityClass());
            intent.putExtras(getActivity().getIntent().getExtras());
            intent.putExtras(detailBundle); 
            intent.putExtra("index", index);
            intent = addExtras(intent);
            startActivity(intent);
            
        }
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
    
    public void setInitialCursorPosition(int position, boolean forceClickOnSinglePane) {
    	
    	if (!(position >= 0 && position < getListView().getAdapter().getCount())) {
    		Log.e(TAG, "The position requested is not within bounds of adapter.");
    		position = 0;
    	}

    	getListView().setSelection(position);
    	
    	if (forceClickOnSinglePane || mDualPane) { 
    		
    		View selectedView = getListView().getChildAt(position);

    		if (getListAdapter().isEmpty()) {
    			clearCurrentDetailFragment();
    		} else {
    			getListView().performItemClick(selectedView, position, 0);
    		}
    	} 
    }
    
    private void clearCurrentDetailFragment() {
    	EllucianDefaultDetailFragment details = (EllucianDefaultDetailFragment)
                getFragmentManager().findFragmentById(R.id.frame_extra);
    	if (details != null) {
        	FragmentTransaction ft = getFragmentManager().beginTransaction();
        	ft.remove(details);
        	ft.commit();
    	}
    }
    
    private Bundle getDefaultBundle() {
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
    public Bundle buildDetailBundle(Object... objects) {
    	Cursor cursor = (Cursor) objects[0];
		return buildDetailBundle(cursor);
	}
    
    /** Override to return custom Bundle */
	protected Bundle buildDetailBundle(Cursor cursor) {
		Bundle bundle = null;
		return bundle;
	}
    
    /** Override to return custom OnItemClickListener */
	private AdapterView.OnItemClickListener buildOnItemClickListener() {
		return listener;
	}
    
    /** Override to return a subclass of EllucianDefaultDetailFragment created by EllucianDefaultDetailFragment.newInstance
     *  See EllucianDefaultDetailFragment.newInstance for more information
     *  If you are only making changes to the class name, override getDetailFragmentClass() instead
     */
	protected EllucianDefaultDetailFragment getDetailFragment(Bundle args, int index) {
		
		return EllucianDefaultDetailFragment.newInstance(getActivity(), 
				getDetailFragmentClass().getName(), args, index);
				
	}
	
	/** Override to return the class of an EllucianDefaultDetailFragment subclass */
	protected Class<? extends EllucianDefaultDetailFragment> getDetailFragmentClass() {
		return EllucianDefaultDetailFragment.class;	
	}
	
	/** Override to return the class of an EllucianDefaultDetailActivity subclass */
	protected Class<? extends EllucianDefaultDetailActivity> getDetailActivityClass() {
		return EllucianDefaultDetailActivity.class;	
	}
	
	/** Override to return additional extras */
	protected Intent addExtras(Intent intent) {
		return intent;
	}
	
}
