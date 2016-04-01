/*
 * Copyright 2016 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.directory;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.adapter.ModuleMenuAdapter;
import com.ellucian.mobile.android.app.CategoryDialogFragment;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.directory.DirectoryResponse;
import com.ellucian.mobile.android.client.directory.DirectoryType;
import com.ellucian.mobile.android.client.directory.Entry;
import com.ellucian.mobile.android.client.services.AuthenticateUserIntentService;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class DirectoryActivity extends EllucianActivity implements SearchView.OnQueryTextListener,
        CategoryDialogFragment.CategoryDialogListener {
    private static final String TAG = DirectoryActivity.class.getSimpleName();

    public static final String DIRECTORY_TYPE_STUDENT = "student";
    public static final String DIRECTORY_TYPE_FACULTY = "faculty";
    private static final String DIRECTORY_LIST_FRAGMENT = "directoryListFragment";
    private static String SIGN_IN_MESSAGE;

    private Activity activity = this;

    private EllucianApplication application;
    private BackgroundAuthenticationReceiver backgroundAuthenticationReceiver;
    private SearchDirectoryInfoTask directoryTask;
    private DirectoryRecyclerFragment mainFragment;

    private boolean justAuthenticated = false;
    private boolean userAuthenticated;
    private String mQueryString;
    private Handler mHandler;
    private static final int DELAY_SEARCH = 500;

    private String[] filteredCategories;
    private ArrayList<DirectoryType> allCategories = new ArrayList<>();
    private CategoryDialogFragment dialogFragment;

    public boolean isLegacy = true;  // denotes a directory module defined prior to 4.5
    public String allUrl;
    public String studentUrl;
    public String facultyUrl;
    public String baseUrl;

    private TextView searchMessage;
    private TextView signInMessage;
    private TextView noSearchResults;
    private SearchView searchView;
    private boolean directory45response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory);
        SIGN_IN_MESSAGE = getString(R.string.directory_auth_required_tail, getString(R.string.main_sign_in));
        String codeBaseVersion = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.MOBILESERVER_CODEBASE_VERSION, null);
        if (!TextUtils.isEmpty(codeBaseVersion)) {
            directory45response = true;
            Log.d(TAG, String.format("A %s Mobile Server Directory was found. Use baseUrl.", codeBaseVersion));
        } else {
            directory45response = false;
        }

        String directoryModuleVersion = getIntent().getExtras().getString(Extra.DIRECTORY_MODULE_VERSION);
        if (!TextUtils.isEmpty(directoryModuleVersion)) {
            Log.d(TAG, "This is a " + directoryModuleVersion + "  directory module");
            isLegacy = false;
        }

        // these textViews will be toggled base on user search input
        searchMessage = (TextView) findViewById(R.id.directory_search_message);
        noSearchResults = (TextView) findViewById(R.id.directory_search_no_results);
        signInMessage = (TextView) findViewById(R.id.directory_search_sign_in);

        setTitle(moduleName);
        application = (EllucianApplication) getApplication();
        userAuthenticated = application.isUserAuthenticated();
        if (!userAuthenticated) {
            registerAuthenticationReceiver();
        }

        if (savedInstanceState != null) {
            mQueryString = savedInstanceState.getString("searchQueryString");
            justAuthenticated = savedInstanceState.getBoolean("justAuthenticated", false);
            filteredCategories = savedInstanceState.getStringArray("filteredCategories");
        }

        FragmentManager manager = getSupportFragmentManager();
        final FragmentTransaction transaction = manager.beginTransaction();
        mainFragment = (DirectoryRecyclerFragment) manager.findFragmentByTag(DIRECTORY_LIST_FRAGMENT);

        if (mainFragment == null) {
            mainFragment = new DirectoryRecyclerFragment();

            transaction.add(R.id.frame_main, mainFragment, DIRECTORY_LIST_FRAGMENT);
        } else {
            transaction.attach(mainFragment);
        }
        transaction.commit();

        setupSearchAndFilter();

        // Only display filter option if there are > 1 Categories
        View rootView = activity.findViewById(android.R.id.content);
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        if (allCategories.size() > 1) {
            fab.setVisibility(View.VISIBLE);
            fab.setBackgroundTintList(ColorStateList.valueOf(Utils.getPrimaryColor(this)));
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_LIST_SELECT,
                            "Select filter", null, moduleName);
                    dialogFragment = new CategoryDialogFragment();

                    Bundle arguments  = new Bundle();
                    arguments.putString(CategoryDialogFragment.DIALOG_TITLE, getString(R.string.directory_groups));
                    dialogFragment.setArguments(arguments);
                    dialogFragment.setCallingFragment(mainFragment);
                    dialogFragment.show(getSupportFragmentManager(), moduleId + "_" + CATEGORY_DIALOG);
                }
            });

        }

        setupActivity();

    }

    private void setupSearchAndFilter() {
        if (isLegacy) {
            // This is a pre-4.5 mobile server. Must fall-back to use older style URLs for Fac and Stu directories.
            facultyUrl = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.DIRECTORY_FACULTY_SEARCH_URL, null);
            studentUrl = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.DIRECTORY_STUDENT_SEARCH_URL, null);
            allUrl = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.DIRECTORY_ALL_SEARCH_URL, null);
            Log.d(TAG, "This is a pre-4.5 directory module. Use legacy URLs.");
        } else {
            baseUrl = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.DIRECTORY_BASE_SEARCH_URL, null);
            Log.d(TAG, "This is a 4.5+ directory module. Use baseUrl.");
        }
        parseCategories();
    }

    private void parseCategories() {
        String selection =  EllucianContract.Modules.MODULES_ID + " = ? AND "
                + EllucianContract.ModulesProperties.MODULE_PROPERTIES_NAME + " = ?";
        String[] selectionArgs = new String[] { moduleId, ModuleMenuAdapter.DIRECTORY_CATEGORY };

        Cursor cursor = activity.getContentResolver().query(EllucianContract.ModulesProperties.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                EllucianContract.ModulesProperties.MODULE_PROPERTIES_VALUE + " ASC");

        ArrayList<String> directories = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                String directoryKey = cursor.getString(cursor.getColumnIndex(EllucianContract.ModulesProperties.MODULE_PROPERTIES_VALUE));
                Log.d(TAG, "Enabled directory: " + directoryKey);
                directories.add(directoryKey);
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (directories.size() > 0) {
            if (isLegacy) {
                allCategories = getLegacyDirectoryNames(directories);
            } else {
                allCategories = getDirectoryNames(directories);
            }
        }

    }

    public String[] getAllCategories() {
        ArrayList<String> categories = new ArrayList<>();
        for(DirectoryType directoryType: allCategories) {
            categories.add(directoryType.displayName);
        }
        Collections.sort(categories);

        return categories.toArray(new String[categories.size()]);
    }

    public String[] getFilteredCategories() {
        return filteredCategories;
    }

    public void updateFilteredCategories(String[] filteredCategories) {
        String oldCats = Arrays.toString(this.filteredCategories);
        String newCats = Arrays.toString(filteredCategories);

        if (!TextUtils.equals(oldCats, newCats)) {
            Log.d(TAG, "User changed selection - requery");
            this.filteredCategories = filteredCategories;
            doQuery(mQueryString);
        }
    }

    public ArrayList<String> getSearchDirectories() {
        ArrayList<String> searchDirectories = new ArrayList<>();

        for (DirectoryType directoryType : allCategories) {
            if (filteredCategories == null ||
                    filteredCategories.length == 0 ||
                    !Arrays.asList(filteredCategories).contains(directoryType.displayName)) {
                searchDirectories.add(directoryType.internalName);
            }
        }
        return searchDirectories;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mHandler == null) {
            mHandler = new Handler();
        }
        getMenuInflater().inflate(R.menu.search, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint(getString(R.string.searchable_directory_hint));
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(false);

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Log.d(TAG, "onClose - onCloseListener");
                clearList();
                return false;
            }
        });

        searchView.setOnClickListener(new SearchView.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick - onClickListener");
                sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION,
                        GoogleAnalyticsConstants.ACTION_SEARCH, "Search", null, moduleName);
            }
        });

        if (!TextUtils.isEmpty(mQueryString)) {
            searchView.setQuery(mQueryString, false);
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        // On a text change, wait "DELAY_SEARCH" milliseconds before submitting
        // query against server in case user edits their search query.
        if (!TextUtils.equals(mQueryString, query)) {
            mQueryString = query;
            mHandler.removeCallbacksAndMessages(null);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onQueryTextChange: " + mQueryString);
                    doQuery(mQueryString);
                }
            }, DELAY_SEARCH);
        }

        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // we query on each character addition/removal, so don't perform
        // another query if the strings match.
        if (!TextUtils.equals(mQueryString, query)) {
            mQueryString = query;
            Log.d(TAG, "query: " + mQueryString);
            doQuery(mQueryString);
        }

        searchView.clearFocus();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!application.isUserAuthenticated()) {
            registerAuthenticationReceiver();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterAuthenticationReceiver();

        if (filteredCategories != null) {
            StringBuilder categoriesString = new StringBuilder();
            for (int i = 0; i < filteredCategories.length; i++) {
                if (!TextUtils.isEmpty(categoriesString)) {
                    categoriesString.append(",");
                }
                categoriesString.append(filteredCategories[i]);
            }
            Utils.addStringToPreferences(this, CATEGORY_DIALOG, moduleId + "_" + FILTERED_CATEGORIES, categoriesString.toString());
        } else {
            Utils.removeValuesFromPreferences(this, CATEGORY_DIALOG, moduleId + "_" + FILTERED_CATEGORIES);
        }

        if (dialogFragment != null) {
            dialogFragment.dismiss();
            dialogFragment = null;
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!TextUtils.isEmpty(mQueryString)) {
            outState.putString("searchQueryString", mQueryString);
        }
        if (justAuthenticated) {
            outState.putBoolean("justAuthenticated", true);
        }
        outState.putStringArray("filteredCategories",filteredCategories);
    }

    public void doQuery(String query) {
        // Clear details and reset position before query
        mainFragment.clearCurrentDetailFragment();
        clearList();

        if (!TextUtils.isEmpty(query)) {
            // If there is a current running query, cancel it and then create a new one.
            if (directoryTask != null) {
                directoryTask.cancel(true);
            }
            String requestUrl ="";

            ArrayList<String> searchDirectories = getSearchDirectories();
            Log.d(TAG, "Directories to search:" + searchDirectories);

            directoryTask = new SearchDirectoryInfoTask(this);
            if (isLegacy) {
                if (searchDirectories.contains(DirectoryActivity.DIRECTORY_TYPE_STUDENT) &&
                        searchDirectories.contains(DirectoryActivity.DIRECTORY_TYPE_FACULTY)) {
                    requestUrl = allUrl;
                } else if (searchDirectories.contains(DirectoryActivity.DIRECTORY_TYPE_STUDENT)) {
                    requestUrl = studentUrl;
                } else if (searchDirectories.contains(DirectoryActivity.DIRECTORY_TYPE_FACULTY)) {
                    requestUrl = facultyUrl;
                }
                if (!TextUtils.isEmpty(requestUrl)) {
                    directoryTask.execute(requestUrl, query);
                }
            } else {
                requestUrl = baseUrl;
                String directoryList = TextUtils.join(",", searchDirectories);
                // Do not perform search if no directories are passed
                if (!TextUtils.isEmpty(directoryList)) {
                    directoryTask.execute(requestUrl, query, directoryList);
                }
            }

        }
    }

    // clear list with initialize text
    private void setupActivity() {
        if (mainFragment.getAdapter() == null) {
            // The adapter is empty, so we should initialize text view.
            initializeTextViews();
        } else {
            // There are already search results. We don't want to clear them, but
            // if the user just signed in, then we should run the query again.
            if (justAuthenticated) {
                justAuthenticated = false;
                // in case any directories had " - Sign In" appended to their filtered name, remove it
                for (int i=0;i<filteredCategories.length;i++) {
                    filteredCategories[i] = filteredCategories[i].replace(SIGN_IN_MESSAGE,"");
                }

                doQuery(mQueryString);
            }
        }
    }

    private void initializeTextViews() {
        if (TextUtils.isEmpty(mQueryString)) {
            noSearchResults.setVisibility(View.GONE);
            searchMessage.setVisibility(View.VISIBLE);
            if (!userAuthenticated) {
                signInMessage.setVisibility(View.VISIBLE);
            }
        } else {
            searchMessage.setVisibility(View.GONE);
            signInMessage.setVisibility(View.GONE);
            noSearchResults.setVisibility(View.GONE);
        }
    }

    private void clearList() {
        initializeTextViews();
        mainFragment.setAdapter(null);
        mainFragment.clearCurrentDetailFragment();
    }

    private class SearchDirectoryInfoTask extends RetrieveDirectoryInfoTask {

        SearchDirectoryInfoTask(Activity activity) {
            super(activity);
        }

        @Override
        protected void onPostExecute(DirectoryResponse response) {

            DirectoryRecyclerAdapter mAdapter;


            if (response != null) {
                Entry[] entries = response.entries;
                if (entries == null) {
                    Log.d(TAG, "entries from response was null.");
                } else {
                    if (!directory45response) {
                        for (Entry entry : entries) {
                            entry.type = translateLegacyDirectoryType(entry.type);
                        }
                    }
                }
                mAdapter = new DirectoryRecyclerAdapter(activity, entries);
                mainFragment.setAdapter(mAdapter);
            } else {
                Log.d(TAG, "response is null.");
                noSearchResults.setVisibility(View.VISIBLE);
            }

        }
    }

    /**
     * For 4.5 directories and above, get the values for displayName and authOnly from the DB.
     */
    private ArrayList<DirectoryType> getDirectoryNames(ArrayList<String> directories) {
        ArrayList<DirectoryType> enabledDirectories = new ArrayList<>();

        String directoriesString = directoryStringBuilder(directories);
        String selection = EllucianContract.Directories.DIRECTORY_KEY + " IN (" + directoriesString + ")";
        Cursor directoryNameCursor = activity.getContentResolver().query(
                EllucianContract.Directories.CONTENT_URI,
                new String[]{EllucianContract.Directories.DIRECTORY_INTERNAL_NAME, EllucianContract.Directories.DIRECTORY_DISPLAY_NAME, EllucianContract.Directories.DIRECTORY_AUTHENTICATED_ONLY},
                selection,
                null,
                EllucianContract.Directories.DEFAULT_SORT);

        if (directoryNameCursor.moveToFirst()) {
            do {
                String internalName = directoryNameCursor.getString(
                        directoryNameCursor.getColumnIndex(EllucianContract.Directories.DIRECTORY_INTERNAL_NAME));
                String displayName = directoryNameCursor.getString(
                        directoryNameCursor.getColumnIndex(EllucianContract.Directories.DIRECTORY_DISPLAY_NAME));
                String authOnlyString = directoryNameCursor.getString(
                        directoryNameCursor.getColumnIndex(EllucianContract.Directories.DIRECTORY_AUTHENTICATED_ONLY));

                boolean authOnly = false;
                if (TextUtils.equals(authOnlyString,"true")) {
                    authOnly = true;
                }

                // When not authenticated, append "- Sign In" to directories requiring authentication.
                if (!userAuthenticated && authOnly) {
                    displayName = getString(R.string.directory_auth_required, displayName, SIGN_IN_MESSAGE);
                }

                Log.d(TAG, "DIRECTORY: " + internalName + ":" + displayName + ":" + authOnly );
                enabledDirectories.add(new DirectoryType(displayName, internalName, authOnly));

            } while (directoryNameCursor.moveToNext());
        }

        directoryNameCursor.close();
        return enabledDirectories;
    }

    private ArrayList<DirectoryType> getLegacyDirectoryNames(ArrayList<String> directories) {
        ArrayList<DirectoryType> enabledDirectories = new ArrayList<>();

        for (String directory : directories) {
            String directoryName = translateLegacyDirectoryType(directory);
            if (directoryName != null) {
                enabledDirectories.add(new DirectoryType(directoryName, directory, true));
            }
        }
        return enabledDirectories;
    }

    /**
     * A pre-4.5 directory module can only be faculty or student. We need to get the translated string.
     * @param dirType the directory type
     * @return translated value of legacy student or faculty
     */
    private String translateLegacyDirectoryType(String dirType) {
        if (TextUtils.equals(dirType, DirectoryActivity.DIRECTORY_TYPE_FACULTY)) {
            return getString(R.string.directory_type_faculty);
        } else if (TextUtils.equals(dirType, DirectoryActivity.DIRECTORY_TYPE_STUDENT)) {
            return getString(R.string.directory_type_student);
        }
        return null;
    }

    private String directoryStringBuilder(ArrayList<String> directories) {
        StringBuilder sb = null;
        for (int i=0; i<directories.size(); i++) {
            if (i==0) {
                sb = new StringBuilder("\"");
            } else {
                sb.append(", \"");
            }

            sb.append(directories.get(i) + "\"");
        }
        return sb.toString();
    }

    private void registerAuthenticationReceiver() {
        if (backgroundAuthenticationReceiver == null) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
            backgroundAuthenticationReceiver = new BackgroundAuthenticationReceiver();
            lbm.registerReceiver(backgroundAuthenticationReceiver, new IntentFilter(AuthenticateUserIntentService.ACTION_UPDATE_MAIN));
        }
    }

    private void unregisterAuthenticationReceiver() {
        if(backgroundAuthenticationReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(backgroundAuthenticationReceiver);
        }
    }

    public class BackgroundAuthenticationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent incomingIntent) {
            String result = incomingIntent.getStringExtra(Extra.LOGIN_SUCCESS);

            if (!TextUtils.isEmpty(result)
                    && result.equals(AuthenticateUserIntentService.ACTION_SUCCESS)) {
                LocalBroadcastManager.getInstance(activity).unregisterReceiver(
                        backgroundAuthenticationReceiver);
                justAuthenticated = true;
                activity.recreate();
            }
        }

    }

}
