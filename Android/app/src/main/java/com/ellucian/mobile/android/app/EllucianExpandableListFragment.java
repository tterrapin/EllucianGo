/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.app;

import com.ellucian.mobile.android.util.ConfigurationProperties;

import android.widget.ExpandableListView;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;

@SuppressWarnings("JavaDoc")
public abstract class EllucianExpandableListFragment extends Fragment
	implements	OnCreateContextMenuListener,
				ExpandableListView.OnChildClickListener,
				ExpandableListView.OnGroupCollapseListener,
				ExpandableListView.OnGroupExpandListener {

	private static final String TAG = EllucianExpandableListFragment.class.getSimpleName();
	static final int INTERNAL_EMPTY_ID = 0x00ff0001;
	
    final private Handler mHandler = new Handler();
    
    final private Runnable mRequestFocus = new Runnable() {
        public void run() {
            mList.focusableViewAvailable(mList);
        }
    };
    
    final private AdapterView.OnItemClickListener mOnClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            onListItemClick((ExpandableListView) parent, v, position, id);
        }
    };
    
    final private OnChildClickListener mOnChildClickListener = new OnChildClickListener() {
    	@Override
    	public boolean onChildClick(ExpandableListView arg0, View arg1, int arg2, int arg3, long arg4) {
        	Log.d(TAG, "onChildClick");
    		return EllucianExpandableListFragment.this.onChildClick(arg0, arg1, arg2, arg3, arg4);
    	}
    };

    private ExpandableListAdapter mAdapter;
    private ExpandableListView mList;
    private View mEmptyView;
    private TextView mStandardEmptyView;
    private View mProgressContainer;
    private View mListContainer;
    private CharSequence mEmptyText;
    boolean mSetEmptyText;
    private boolean mListShown;
    private boolean mFinishedStart = false;
    
    public EllucianExpandableListFragment() {
    	Log.d(TAG, "constructor (null)");
    }

    /**
     * This method will be called when an item in the list is selected.
     * Subclasses MUST override. Subclasses can call
     * getListView().getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param l The ListView where the click happened
     * @param v The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id The row id of the item that was clicked
     */
    private void onListItemClick(ExpandableListView l, View v, int position, long id) {
    	Log.d(TAG, "onListItemClick default no-op");
    }

    /**
     * Provide default implementation to return a simple list view.  Subclasses
     * can override to replace with their own layout.  If doing so, the
     * returned view hierarchy <em>must</em> have a ListView whose id
     * is {@link android.R.id#list android.R.id.list} and can optionally
     * have a sibling view id {@link android.R.id#empty android.R.id.empty}
     * that is to be shown when the list is empty.
     * 
     * <p>If you are overriding this method with your own custom content,
     * consider including the standard layout {@link android.R.layout#list_content}
     * in your layout file, so that you continue to retain all of the standard
     * behavior of ListFragment.  In particular, this is currently the only
     * way to have the built-in indeterminant progress state be shown.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	Log.d(TAG, "onCreateView");
        return inflater.inflate(android.R.layout.list_content,
                container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	Log.d(TAG, "onActivityCreated");
    	super.onActivityCreated(savedInstanceState);
    	ensureList();
    }
    
    /**
     * Attach to list view once the view hierarchy has been created.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        ensureList();
    }

    /**
     * Detach from list view.
     */
    @Override
    public void onDestroyView() {
    	Log.d(TAG, "onDestroyView");
        mHandler.removeCallbacks(mRequestFocus);
        mList = null;
        mListShown = false;
        mEmptyView = mProgressContainer = mListContainer = null;
        mStandardEmptyView = null;
        super.onDestroyView();
    }

    /**
     * Provide the cursor for the list view.
     */
    public void setListAdapter(ExpandableListAdapter adapter) {
    	Log.d(TAG, "setListAdapter");
        boolean hadAdapter = mAdapter != null;
        mAdapter = adapter;
        if (mList != null) {
            mList.setAdapter(adapter);
            if (!mListShown && !hadAdapter) {
                // The list was hidden, and previously didn't have an
                // adapter.  It is now time to show it.
                setListShown(true, getView().getWindowToken() != null);
            }
        }
    }

    /**
     * Set the currently selected list item to the specified
     * position with the adapter's data
     *
     * @param position
     */
    public void setSelection(int position) {
    	Log.d(TAG, "setSelection");
        ensureList();
        mList.setSelection(position);
    }

    /**
     * Get the position of the currently selected list item.
     */
    public int getSelectedItemPosition() {
    	Log.d(TAG, "getSelectedItemPosition");
        ensureList();
        return mList.getSelectedItemPosition();
    }

    /**
     * Get the cursor row ID of the currently selected list item.
     */
    public long getSelectedItemId() {
    	Log.d(TAG, "getSelectedItemId");
        ensureList();
        return mList.getSelectedItemId();
    }

    /**
     * Get the activity's list view widget.
     */
    public ExpandableListView getExpandableListView() {
    	Log.d(TAG, "getExpandableListView");
        ensureList();
        return mList;
    }

    /**
     * The default content for a ListFragment has a TextView that can
     * be shown when the list is empty.  If you would like to have it
     * shown, call this method to supply the text it should use.
     */
    public void setEmptyText(CharSequence text) {
    	Log.d(TAG, "setEmptyText");
        ensureList();
        if (mStandardEmptyView == null) {
            throw new IllegalStateException("Can't be used with a custom content view");
        }
        mStandardEmptyView.setText(text);
        if (mEmptyText == null) {
            mList.setEmptyView(mStandardEmptyView);
        }
        mEmptyText = text;
    }
    
    /**
     * Control whether the list is being displayed.  You can make it not
     * displayed if you are waiting for the initial data to show in it.  During
     * this time an indeterminant progress indicator will be shown instead.
     * 
     * <p>Applications do not normally need to use this themselves.  The default
     * behavior of ListFragment is to start with the list not being shown, only
     * showing it once an adapter is given with {@link #setListAdapter(ListAdapter)}.
     * If the list at that point had not been shown, when it does get shown
     * it will be do without the user ever seeing the hidden state.
     * 
     * @param shown If true, the list view is shown; if false, the progress
     * indicator.  The initial value is true.
     */
    public void setListShown(boolean shown) {
    	Log.d(TAG, "setListShown");
        setListShown(shown, true);
    }
    
    /**
     * Like {@link #setListShown(boolean)}, but no animation is used when
     * transitioning from the previous state.
     */
    public void setListShownNoAnimation(boolean shown) {
    	Log.d(TAG, "setListShownNoAnimation");
        setListShown(shown, false);
    }
    
    /**
     * Control whether the list is being displayed.  You can make it not
     * displayed if you are waiting for the initial data to show in it.  During
     * this time an indeterminant progress indicator will be shown instead.
     * 
     * @param shown If true, the list view is shown; if false, the progress
     * indicator.  The initial value is true.
     * @param animate If true, an animation will be used to transition to the
     * new state.
     */
    private void setListShown(boolean shown, boolean animate) {
    	Log.d(TAG, "setListShown(shown,animate)");
        ensureList();
        if (mListShown == shown) {
            return;
        }
        mListShown = shown;
        if (shown) {
            if (animate) {
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
            } else {
                mListContainer.clearAnimation();
            }
            mListContainer.setVisibility(View.VISIBLE);
        } else {
            if (animate) {
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            } else {
                mListContainer.clearAnimation();
            }
            mListContainer.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onGroupExpand(int arg0) {
    	Log.d(TAG, "onGroupExpand");
    }
    @Override
    public void onGroupCollapse(int arg0) {
    	Log.d(TAG, "onGroupCollapse");
    }
    @Override
    public boolean onChildClick(ExpandableListView arg0, View arg1, int arg2, int arg3, long arg4) {
    	Log.d(TAG, "onChildClick");
    	return false;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	Log.d(TAG, "onCreateContextMenu");
    }
    
    public void onContentChanged() {
    	Log.d(TAG, "onContentChanged");
    	View emptyView = getView().findViewById(android.R.id.empty);
    	mList = (ExpandableListView)getView().findViewById(android.R.id.list);
    	if (mList == null) {
    		throw new RuntimeException(
            "Your content must have a ExpandableListView whose id attribute is " +
            "'android.R.id.list'");
    	}
    	if (emptyView != null) {
    		mList.setEmptyView(emptyView);
    	}
        mList.setOnChildClickListener(this);
        mList.setOnGroupExpandListener(this);
        mList.setOnGroupCollapseListener(this);

        if (mFinishedStart) {
            setListAdapter(mAdapter);
        }
        mFinishedStart = true;
    }
    
    /**
     * Get the ListAdapter associated with this activity's ListView.
     */
    public ExpandableListAdapter getListAdapter() {
    	Log.d(TAG, "getListAdapter");
        return mAdapter;
    }

    private void ensureList() {
    	Log.d(TAG, "ensureList");
        if (mList != null) {
            return;
        }
        View root = getView();
        if (root == null) {
            throw new IllegalStateException("Content view not yet created");
        }
        if (root instanceof ExpandableListView) {
            mList = (ExpandableListView)root;
        } else {
            mStandardEmptyView = (TextView)root.findViewById(
                    android.R.id.empty);
            if (mStandardEmptyView == null) {
                mEmptyView = root.findViewById(android.R.id.empty);
            } else {
                mStandardEmptyView.setVisibility(View.GONE);
            }
            mListContainer = root.findViewById(android.R.id.list);
            View rawListView = root.findViewById(android.R.id.list);
            if (!(rawListView instanceof ExpandableListView)) {
                throw new RuntimeException(
                        "Content has view with id attribute 'android.R.id.list' "
                        + "that is not a ListView class");
            }
            mList = (ExpandableListView)rawListView;
            if (mList == null) {
                throw new RuntimeException(
                        "Your content must have a ListView whose id attribute is " +
                        "'android.R.id.list'");
            }
            if (mEmptyView != null) {
                mList.setEmptyView(mEmptyView);
            } else if (mEmptyText != null) {
                mStandardEmptyView.setText(mEmptyText);
                mList.setEmptyView(mStandardEmptyView);
            }
        }
        mListShown = true;
        mList.setOnItemClickListener(mOnClickListener);
        mList.setOnChildClickListener(mOnChildClickListener);
        if (mAdapter != null) {
        	ExpandableListAdapter adapter = mAdapter;
            mAdapter = null;
            setListAdapter(adapter);      	
        } else {
            // We are starting without an adapter, so assume we won't
            // have our data right away and start with the progress indicator.
            setListShown(false, false);
        }
        mHandler.post(mRequestFocus);
    }

	protected EllucianActivity getEllucianActivity() {
    	Log.d(TAG, "getEllucianActivity");
		return (EllucianActivity)getActivity();
	}
	
	public ConfigurationProperties getConfigurationProperties() {
        return getEllucianActivity().getConfigurationProperties();
    }

    /**
     * Send event to google analytics
     * @param category
     * @param action
     * @param label
     * @param value
     * @param moduleName
     */
    public void sendEvent(String category, String action, String label, Long value, String moduleName) {
    	Log.d(TAG, "sendEvent");
    	 getEllucianActivity().sendEvent(category, action, label, value, moduleName);
    }
    
    /**
     * Send event to google analytics for just tracker 1
     * @param category
     * @param action
     * @param label
     * @param value
     * @param moduleName
     */
    public void sendEventToTracker1(String category, String action, String label, Long value, String moduleName) {
    	Log.d(TAG, "sendEventToTracker1");
    	 getEllucianActivity().sendEventToTracker1(category, action, label, value, moduleName);
    }
    
    /**
     * Send event to google analytics for just tracker 2
     * @param category
     * @param action
     * @param label
     * @param value
     * @param moduleName
     */
    public void sendEventToTracker2(String category, String action, String label, Long value, String moduleName) {
    	Log.d(TAG, "sendEventToTracker2");
    	 getEllucianActivity().sendEventToTracker2(category, action, label, value, moduleName);
    }

    /**
     * Send view to google analytics
     * @param appScreen
     * @param moduleName
     */
    protected void sendView(String appScreen, String moduleName) {
    	Log.d(TAG, "sendView");
    	getEllucianActivity().sendView(appScreen, moduleName);
    }
    
    /**
     * Send view to google analytics for just tracker 1
     * @param appScreen
     * @param moduleName
     */
    public void sendViewToTracker1(String appScreen, String moduleName) {
    	Log.d(TAG, "sendViewToTracker1");
    	getEllucianActivity().sendViewToTracker1(appScreen, moduleName);
    }
    
    /**
     * Send view to google analytics for just tracker 2
     * @param appScreen
     * @param moduleName
     */
    public void sendViewToTracker2(String appScreen, String moduleName) {
    	Log.d(TAG, "sendEventToTracker2");
    	getEllucianActivity().sendViewToTracker2(appScreen, moduleName);
    }

}
