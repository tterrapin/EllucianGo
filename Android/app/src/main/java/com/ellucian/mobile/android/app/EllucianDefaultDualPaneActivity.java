/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.app;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.app.IntentService;
import android.support.v4.app.LoaderManager;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.ellucian.elluciango.R;

/**
 * This class and the other EllucianDefault classes are intended to be used a dual pane list/detail layouts.
 * It is also intended to be used with SimpleCursorAdapter lists.
 * This can support different layouts for both portrait and landscape mode depending on how you set your
 * layouts and in which resource folders they are placed.
 * 
 * This class does not need to be used in order to use the other EllucianDefault classes, creating a separate
 * class that manages the adapter/loader/binder will work.
 * 
 * @author Jared Higley
 *
 */

public abstract class EllucianDefaultDualPaneActivity extends EllucianActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	private SimpleCursorAdapter adapter;
	private EllucianDefaultListFragment mainFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_default_dual_pane);
		
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		mainFragment =  (EllucianDefaultListFragment) manager.findFragmentByTag(getFragmentTag());

		adapter = getCursorAdapter();
		
		if (mainFragment == null) {
			mainFragment = getListFragment();
			
			mainFragment.setListAdapter(adapter);
			transaction.add(R.id.frame_main, mainFragment, getFragmentTag());
		} else {
			mainFragment.setListAdapter(adapter);
			transaction.attach(mainFragment);
		}
		
		ViewBinder viewBinder = getCursorViewBinder();
		if (viewBinder != null) {
			mainFragment.setViewBinder(viewBinder);
		}
		
		transaction.commit();
		getSupportLoaderManager().restartLoader(0, null, this);
		
		Intent intent = new Intent(this, getIntentServiceClass());
		Bundle bundle = getIntentServiceExtras();
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		
		startService(intent);
	}
		
	@Override
   	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
   		return getCursorLoader(id, args);
   	}

   	@Override
   	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
   		adapter.swapCursor(cursor);
   		createNotifyHandler(mainFragment);
   	}

   	@Override
   	public void onLoaderReset(Loader<Cursor> cursor) {
   		adapter.swapCursor(null);
   	}
   	
   	protected abstract String getFragmentTag();
	
	protected abstract SimpleCursorAdapter getCursorAdapter();
	
	protected abstract Class<? extends IntentService> getIntentServiceClass();
	
	
	/** Any variables used in the Loader creation will need to be set before the call to super.onCreate() 
	 *  in the onCreate() method of the subclass. This insures the statement is correct before the loader
	 *  is initialized
	 */
	protected abstract Loader<Cursor> getCursorLoader(int id, Bundle args);
	
	
	/** Override for custom extras for the intent service */
	private Bundle getIntentServiceExtras() {
		return getIntent().getExtras();
	}
	
	/** Override to return a subclass of EllucianDefaultListFragment created by EllucianDefaultListFragment.newInstance
     *  See EllucianDefaultListFragment.newInstance for more information
     *  If you are only making changes to the class name, override getListFragmentClass() instead
     */
	private EllucianDefaultListFragment getListFragment() {
		
		return EllucianDefaultListFragment.newInstance(this, 
				getListFragmentClass().getName(), null);
	}
	
	/** Override to return the class of an EllucianDefaultListFragment subclass */
	protected Class<? extends EllucianDefaultListFragment> getListFragmentClass() {
		return EllucianDefaultListFragment.class;
	}
	
	/** Override to return a custom ViewBinder */
	protected ViewBinder getCursorViewBinder() {
		return null;
	}
   	
   	/** Make sure to call this method in the LoaderManager.LoaderCallback.onLoadFinished method if  
   	 *  it is overridden in the subclass 
   	 */
	private void createNotifyHandler(final EllucianDefaultListFragment fragment) {
   		Handler handler = new Handler(Looper.getMainLooper());
   		handler.post(new Runnable(){

			@Override
			public void run() {
				fragment.setInitialCursorPosition(false);
				
			}
   			
   		});
   	}
}
