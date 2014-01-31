package com.ellucian.mobile.android.directory;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.OnQueryListener;

public class DirectoryListActivity  extends EllucianActivity {
	
	public String directoryType;
	public String allUrl;
	public String studentUrl;
	public String facultyUrl;
	
	private DirectoryListFragment mainFragment;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_dual_pane);
        
        Intent incomingIntent = getIntent();
        
        allUrl = incomingIntent.getStringExtra(Extra.REQUEST_URL);
        studentUrl = incomingIntent.getStringExtra(Extra.DIRECTORY_STUDENT_URL);
        facultyUrl = incomingIntent.getStringExtra(Extra.DIRECTORY_FACULTY_URL);
        
        directoryType = incomingIntent.getStringExtra(Extra.DIRECTORY_TYPE);
		
        if (directoryType != null) {
			if (directoryType.equals(DirectoryCategoriesFragment.DIRECTORY_TYPE_STUDENT)) {
				setTitle(R.string.title_activity_directory_list_student);
			} else if (directoryType.equals(DirectoryCategoriesFragment.DIRECTORY_TYPE_FACULTY)) {
				setTitle(R.string.title_activity_directory_list_faculty);
			}
		}
        
        FragmentManager manager = getFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		mainFragment =  (DirectoryListFragment) manager.findFragmentByTag("directoryListFragment");
		
		if (mainFragment == null) {
			mainFragment = new DirectoryListFragment();
			
			transaction.add(R.id.frame_main, mainFragment, "directoryListFragment");
		} else {
			transaction.attach(mainFragment);
		}
		transaction.commit();
		
        handleIntent(incomingIntent);
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
    	handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
        	final String query;
        	if (intent.hasExtra(Extra.DIRECTORY_QUERY)) {
        		query = intent.getStringExtra(Extra.DIRECTORY_QUERY);
        	} else {
        		query = intent.getStringExtra(SearchManager.QUERY);
        	}

        	// the fragment isn't always attached when we reach this point. So if
        	// not attached, defer this action for a tad.
            final OnQueryListener onQueryfragment = (OnQueryListener) mainFragment;
            	if (!mainFragment.isAdded()) {
            		new Handler(getMainLooper()).post(new Runnable() {
            			@Override
            			public void run() {
            				onQueryfragment.doQuery(query);
            			}
            		});
            	} else {
            		onQueryfragment.doQuery(query);
            }
        }
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.directory_list, menu);
        
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.directory_list_action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        
        searchView.setOnCloseListener(new OnCloseListener() {

			@Override
			public boolean onClose() {
	            mainFragment.clearList();
				return false;
			}
			
        });
         
        return true;
    }
	     
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.directory_list_action_search:
    		onSearchRequested();
    		item.collapseActionView();
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
}
