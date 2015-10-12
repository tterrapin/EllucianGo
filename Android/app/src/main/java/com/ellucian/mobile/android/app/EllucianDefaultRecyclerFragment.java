/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.app;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.adapter.EllucianRecyclerAdapter;
import com.ellucian.mobile.android.view.SimpleDividerItemDecoration;

/**
 * This class can be used as an alternative to the EllucianDefaultListFragment.
 * It makes use of the RecyclerView instead of a ListView.
 * Unlike EllucianDefaultListFragment it is less dependant on use of loaders but
 * could be extended and modified to do so.
 * The child class needs to follow the constructor rule below.
 * Many methods are intended to be overridden.
 *
 * @author Jared Higley
 *
 */
public class EllucianDefaultRecyclerFragment extends EllucianFragment implements EllucianRecyclerAdapter.OnItemClickListener{
	private static final String TAG = EllucianDefaultRecyclerFragment.class.getSimpleName();
	
	private Activity activity;
	private View rootView;
	protected EllucianRecyclerView recyclerView;
	protected EllucianRecyclerAdapter adapter;
	protected Bundle detailBundle;
	protected boolean dualPane;
	
	/** A subclass of RetainStateRecyclerFragment must have an empty constructor */
	public EllucianDefaultRecyclerFragment() {
	}
	
	public static EllucianDefaultRecyclerFragment newInstance(Context context, String fname, Bundle args) {
		EllucianDefaultRecyclerFragment fragment = newInstance(context, fname, args, 0);
		return fragment;
	}
	
	/** Variable "fname" should be the class name of an RetainStateRecyclerFragment subclass
	 *  example -  SubclassFragmentOfDefaultListFragment.class.getName()
	 */
	private static EllucianDefaultRecyclerFragment newInstance(Context context, String fname, Bundle args, int emptyTextResId) {
		EllucianDefaultRecyclerFragment fragment;
		
		if (TextUtils.isEmpty(fname)) {
			fname = EllucianDefaultRecyclerFragment.class.getName();
		}
		Log.d(TAG, "Creating new instance for class: " + fname);
		
		if (args == null) {
			args = new Bundle();
		}
		
		args.putInt("emptyTextResId", emptyTextResId);
		
		fragment = (EllucianDefaultRecyclerFragment) Fragment.instantiate(context, fname, args);

        return fragment;
	
    }
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        // Unlike the previous EllucianDefaultListFragment this fragment will retain its state on rotation
		setRetainInstance(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// View only gets build the on the first load because state is retained
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_default_recycler, container, false);				

			recyclerView = (EllucianRecyclerView) rootView.findViewById(R.id.recycler_view);

			// use a linear layout manager
			LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity);
			recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.addItemDecoration(new SimpleDividerItemDecoration(activity));

			recyclerView.setAdapter(adapter);
		}

		return rootView;
	}

	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		View detailsFrame = getActivity().findViewById(R.id.frame_extra);
		dualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
		
		
		if (dualPane && detailBundle != null) {
			showDetails(0);
		}
	}

    /**
     * Only supports the use of a subclass of EllucianRecyclerAdapter.
     * View ItemHolderRecyclerAdapter or SectionedItemHolderRecyclerAdapter for an example on how
     * to extend EllucianRecyclerAdapter.
     * All these classes can be found in the com.ellucian.mobile.android.adapter package.
     */
	protected void setAdapter(EllucianRecyclerAdapter adapter) {
		this.adapter = adapter;
		if (adapter != null) {
			adapter.setOnItemClickListener(this);
		}
		
		if (recyclerView != null) {
			recyclerView.setAdapter(this.adapter);
		}
	}
	
	public RecyclerView getRecyclerView() {
		return recyclerView;
	}

	@Override
	public void onItemClicked(View view, int position) {
		Object item = ((EllucianRecyclerAdapter)recyclerView.getAdapter()).getItem(position);
		detailBundle = buildDetailBundle(item);
		showDetails(position);
        recyclerView.setSelectedIndex(position);
	}

    protected void showDetails(int index) {

        if (dualPane) {
        	//We can display everything in-place with fragments
            
            EllucianDefaultDetailFragment details = getDetailFragment(detailBundle, index);

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
            intent.putExtras(activity.getIntent().getExtras());
            intent.putExtras(detailBundle); 
            intent.putExtra("index", index);
            startActivity(intent);
            
        }
    }

    public void clearCurrentDetailFragment() {
        EllucianDefaultDetailFragment details = (EllucianDefaultDetailFragment)
                getFragmentManager().findFragmentById(R.id.frame_extra);
        if (details != null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.remove(details);
            ft.commit();
        }
    }
    
    /** Override to return custom Bundle */
	protected Bundle buildDetailBundle(Object... objects) {
		return null;
	}
		
    /** Override to return a subclass of EllucianDefaultDetailFragment created by EllucianDefaultDetailFragment.newInstance
     *  See EllucianDefaultDetailFragment.newInstance for more information
     *  If you are only making changes to the class name, override getDetailFragmentClass() instead
     */
	private EllucianDefaultDetailFragment getDetailFragment(Bundle args, int index) {
		
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
	public Intent addExtras(Intent intent) {
		return intent;
	}
	

}
