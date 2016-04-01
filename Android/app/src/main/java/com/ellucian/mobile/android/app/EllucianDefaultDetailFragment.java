/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.app;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

/**
 * This class can be extended for custom work like add action items to the actionbar/menu
 * The child class needs to follow the constructor rule below.
 **
 */
public class EllucianDefaultDetailFragment extends EllucianFragment {
	private static final String TAG = EllucianDefaultDetailFragment.class.getSimpleName();
	
	private View rootView;	
	
	/** A subclass of EllucianDefaultDetailFragment must have an empty constructor */
	public EllucianDefaultDetailFragment() {
	}
	
	/** Variable "fname" should be the class name of an EllucianDefaultDetailFragment subclass
	 *  example -  SubclassFragmentOfDefaultDetailFragment.class.getName()
	 */
	public static EllucianDefaultDetailFragment newInstance(Context context, String fname, Bundle args, int index) {
		EllucianDefaultDetailFragment fragment;
		
		if (TextUtils.isEmpty(fname)) {
			fname = EllucianDefaultDetailFragment.class.getName();
		}
		Log.d(TAG, "Creating new instance for class: " + fname);
		
		if (args == null) {
			args = new Bundle();
		}
		
		args.putInt("index", index);
		
		fragment = (EllucianDefaultDetailFragment) Fragment.instantiate(context, fname, args);

        return fragment;
    }
    
	/** Returns the index that was sent on time of fragment creation */
	public int getShownIndex() {
		if (getArguments() != null) {
			return getArguments().getInt("index", 0);
		} else {
			return 0;
		}
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }
		
		rootView = inflater.inflate(R.layout.fragment_default_detail, container, false);
		
		View headerLayout = rootView.findViewById(R.id.header_layout);
		TextView titleView = (TextView) rootView.findViewById(R.id.title);
		TextView dateLabelView = (TextView) rootView.findViewById(R.id.date_label);
		TextView dateView = (TextView) rootView.findViewById(R.id.date);
		TextView contentView = (TextView) rootView.findViewById(R.id.content);
		TextView locationView = (TextView) rootView.findViewById(R.id.location);
		
		Activity activity = getActivity();
		
		String title = null;
		String dateLabel = null;
		String date = null;
		String content = null;
		String location = null;

		Bundle args = getArguments();
		if (args != null) {
			title = args.getString(Extra.TITLE);
			dateLabel = args.getString(Extra.DATE_LABEL);
			date = args.getString(Extra.DATE);
			content = args.getString(Extra.CONTENT);
			location = args.getString(Extra.LOCATION);
		}

		if (!TextUtils.isEmpty(title)) { 
			titleView.setText(title); 
		}
		if (!TextUtils.isEmpty(dateLabel)) { 
			dateLabelView.setText(dateLabel); 
		} else {
			dateLabelView.setVisibility(View.GONE);
		}
		if (!TextUtils.isEmpty(date)) { 
			dateView.setText(date); 
		}
		if (!TextUtils.isEmpty(content)) {
			contentView.setAutoLinkMask(Utils.getAvailableLinkMasks(activity, Linkify.ALL));
			contentView.setText(content); 	
		}
		if (!TextUtils.isEmpty(location)) {
			locationView.setText(location); 
		} else {
			locationView.setVisibility(View.GONE);
		}
		return rootView;
	}

}
