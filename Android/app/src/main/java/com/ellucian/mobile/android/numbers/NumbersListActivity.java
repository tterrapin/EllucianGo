/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.numbers;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnCloseListener;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.adapter.EllucianRecyclerAdapter;
import com.ellucian.mobile.android.adapter.SectionedItemHolderRecyclerAdapter;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.EllucianDefaultRecyclerFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.services.NumbersIntentService;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

import java.util.ArrayList;
import java.util.Collections;

public class NumbersListActivity extends EllucianActivity implements OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

    private final Activity activity = this;
    private static final String TAG = NumbersListActivity.class.getSimpleName();
    // category loader is positioned at -1 so that loaders for the
    // individual categories can be automatically allocated at 0+
    private static final int CATEGORY_LOADER = -1;
    private String query;
    private NumbersRecyclerFragment mainFragment;
    private boolean resetListPosition;
    private boolean clearList;
    private ArrayList<NumbersItemHolder> numbersList;
    private NumbersIntentServiceReceiver numbersIntentServiceReceiver;
    private boolean showSpinner = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_dual_pane);
        this.setTitle(moduleName);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        mainFragment = (NumbersRecyclerFragment) manager.findFragmentByTag("NumbersRecyclerFragment");

        registerNumbersServiceReceiver();
        if (mainFragment == null) {
            mainFragment = new NumbersRecyclerFragment();
            transaction.add(R.id.frame_main, mainFragment, "NumbersRecyclerFragment");
        } else {
            transaction.attach(mainFragment);
        }

        transaction.commit();

        if (savedInstanceState != null) {
            query = savedInstanceState.getString("query");
        }

        handleIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent");
        // This case is when a user is already in a module that has singleTop set and
        // they select the same module type in the navigation menu
        if (!Intent.ACTION_SEARCH.equals(intent.getAction()) &&
                !moduleId.equals(intent.getStringExtra(Extra.MODULE_ID))) {
            Intent restartingIntent = new Intent(this, NumbersListActivity.class);
            restartingIntent.putExtras(intent.getExtras());
            restartingIntent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            startActivity(restartingIntent);
            this.finish();
        }
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        resetListPosition = false;
        clearList = false;

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            query = intent.getStringExtra(SearchManager.QUERY);

            clearList = true;
            mainFragment.clearCurrentDetailFragment();
        }

        getSupportLoaderManager().restartLoader(CATEGORY_LOADER, null, this);

        Intent serviceIntent = new Intent(this, NumbersIntentService.class);
        serviceIntent.putExtra(Extra.MODULE_ID, moduleId);
        serviceIntent.putExtra(Extra.REQUEST_URL, requestUrl);
        startService(serviceIntent);
        if (showSpinner) {
            Utils.showProgressIndicator(this);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterNumbersServiceReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerNumbersServiceReceiver();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!TextUtils.isEmpty(query)) {
            outState.putString("query", query);
        }
    }

    public void doQuery(String queryString) {
        Log.d(TAG, "doQuery");
        if (!TextUtils.isEmpty(queryString)) {
            query = queryString;
        } else {
            query = null;
        }

        SectionedItemHolderRecyclerAdapter mAdapter;
        if (!TextUtils.isEmpty(query) && numbersList != null
                && !numbersList.isEmpty()) {
            final ArrayList<NumbersItemHolder> filteredList = filter(numbersList, query);
            Log.d(TAG, "filtered list count: " + filteredList.size());
            mAdapter = buildAdapters(filteredList);
        } else {
            mAdapter = buildAdapters(numbersList);
        }

        mainFragment.setAdapter(mAdapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader, id: " + Integer.toString(id));

        String selection = null;
        String[] selectionArgs = null;

        Log.d(TAG, "onCreateLoader for CategoryLoader");
        selection = Modules.MODULES_ID + " = ?";
        selectionArgs = new String[]{moduleId};

        return new CursorLoader(this, EllucianContract.Numbers.CONTENT_URI, null, selection,
                selectionArgs, EllucianContract.Numbers.DEFAULT_SORT);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished");
        int id = loader.getId();
        switch (id) {
            case CATEGORY_LOADER:
                Log.d(TAG, "onLoadFinished for CategoryLoader returning " + data.getCount());

                numbersList = buildNumbersList(data);
                SectionedItemHolderRecyclerAdapter adapter = buildAdapters(numbersList);

                mainFragment.setAdapter(adapter);

                createNotifyHandler(mainFragment);

                if (data.getCount() == 0) {
                    showSpinner = true;
                    TextView emptyView = (TextView) mainFragment.getView().findViewById(android.R.id.empty);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    showSpinner = false;
                    Utils.hideProgressIndicator(this);
                }

                if (!TextUtils.isEmpty(query)) {
                        doQuery(query);
                }

                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");
        int id = loader.getId();
        switch (id) {
            case CATEGORY_LOADER:
                Log.d(TAG, "onLoaderReset for CategoryLoader");
                break;
        }
    }

    private void createNotifyHandler(final EllucianDefaultRecyclerFragment fragment) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {

                if (!TextUtils.isEmpty(query)) {
                    Log.d(TAG, "query: " + query + ", clearList: " + clearList);
                } else {
                    Log.d(TAG, "resetListPosition: " + resetListPosition);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.important_numbers, menu);

        final MenuItem item = menu.findItem(R.id.numbers_action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint(getString(R.string.searchable_numbers_hint));
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(false);

        searchView.setOnCloseListener(new OnCloseListener() {
            @Override
            public boolean onClose() {
                Log.d(TAG, "onClose - onCloseListener");
                return false;
            }
        });

        searchView.setOnClickListener(new SearchView.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick - onClickListener");
                sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_SEARCH, "Search", null, moduleName);
            }
        });

        if (!TextUtils.isEmpty(query)) {
            searchView.setQuery(query, false);
        }
        return true;
    }

    private ArrayList<NumbersItemHolder> buildNumbersList(Cursor cursor) {
        ArrayList<NumbersItemHolder> numbersList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex(EllucianContract.Numbers.NUMBERS_NAME));
                String type = cursor.getString(cursor.getColumnIndex(EllucianContract.NumbersCategories.NUMBERS_CATEGORY_NAME));
                String address = cursor.getString(cursor.getColumnIndex(EllucianContract.Numbers.NUMBERS_ADDRESS));
                String email = cursor.getString(cursor.getColumnIndex(EllucianContract.Numbers.NUMBERS_EMAIL));
                String phone = cursor.getString(cursor.getColumnIndex(EllucianContract.Numbers.NUMBERS_PHONE));
                String extension = cursor.getString(cursor.getColumnIndex(EllucianContract.Numbers.NUMBERS_EXTENSION));
                String buildingId = cursor.getString(cursor.getColumnIndex(EllucianContract.Numbers.NUMBERS_BUILDING_ID));
                String campusId = cursor.getString(cursor.getColumnIndex(EllucianContract.Numbers.NUMBERS_CAMPUS_ID));
                double latitude = (double) cursor.getFloat(cursor.getColumnIndex(EllucianContract.Numbers.NUMBERS_LATITUDE));
                double longitude = (double) cursor.getFloat(cursor.getColumnIndex(EllucianContract.Numbers.NUMBERS_LONGITUDE));

                NumbersItemHolder infoHolder = new NumbersItemHolder(name, type, address, email, phone,
                        extension, buildingId, campusId, latitude, longitude);
                numbersList.add(infoHolder);

            } while (cursor.moveToNext());
        }

        return numbersList;
    }

    private SectionedItemHolderRecyclerAdapter buildAdapters(ArrayList<NumbersItemHolder> numbersList) {
        SectionedItemHolderRecyclerAdapter mAdapter = new SectionedItemHolderRecyclerAdapter(this);

        if (numbersList != null && !numbersList.isEmpty()) {
            Collections.sort(numbersList);

            String lastType = "#*!"; // seed value
            ArrayList<NumbersItemHolder> numbersByType = new ArrayList<>();
            for (NumbersItemHolder infoHolder : numbersList) {
                String thisType = infoHolder.type;

                // Reset new section if not empty and type does not match
                if (!numbersByType.isEmpty() && !lastType.equals(thisType)) {
                    NumbersHeaderHolder numbersHeaderHolder = new NumbersHeaderHolder(lastType);
                    mAdapter.addSection(numbersHeaderHolder, numbersByType);
                    numbersByType = new ArrayList<>();
                }

                numbersByType.add(infoHolder);

                lastType = infoHolder.type;
            }

            // Add last section if not empty
            if (!numbersByType.isEmpty()) {
                NumbersHeaderHolder numbersHeaderHolder = new NumbersHeaderHolder(lastType);
                mAdapter.addSection(numbersHeaderHolder, numbersByType);
            }
        }
        return mAdapter;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        // on a text change, filter the list
        Log.d(TAG, "onQueryTextChange: " + query);
        doQuery(query);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // we query on each character addition/removal,
        // no need to respond to submit
        Log.d(TAG, "query: " + query);
        return false;
    }

    private ArrayList<NumbersItemHolder> filter(
            ArrayList<? extends EllucianRecyclerAdapter.ItemInfoHolder> numbersList,
            String query) {
        query = query.toLowerCase();

        final ArrayList<NumbersItemHolder> filteredList = new ArrayList<>();

        for (EllucianRecyclerAdapter.ItemInfoHolder item : numbersList) {
            NumbersItemHolder number = (NumbersItemHolder) item;
            final String name = number.name.toLowerCase();
            if (name.contains(query)) {
                filteredList.add(number);
            }
        }
        return filteredList;
    }


    private void registerNumbersServiceReceiver() {
        if(numbersIntentServiceReceiver == null) {
            Log.d("NumbersListActivity.RegisterNumbersServiceReceiver", "Registering new service receiver");
            numbersIntentServiceReceiver = new NumbersIntentServiceReceiver();
            IntentFilter filter = new IntentFilter(NumbersIntentService.ACTION_FINISHED);
            LocalBroadcastManager.getInstance(this).registerReceiver(numbersIntentServiceReceiver, filter);
        }
    }

    private void unregisterNumbersServiceReceiver() {
        if(numbersIntentServiceReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(numbersIntentServiceReceiver);
        }
    }

    /**
     * Broadcast receiver which receives notification when the NumbersIntentService
     * is finished performing an update of the data from the web services to the
     * local database.  Upon successful completion, this receiver will reload
     * the numbers data from the local database.
     *
     */
    private class NumbersIntentServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean updated = intent.getBooleanExtra(NumbersIntentService.PARAM_OUT_DATABASE_UPDATED, false);
            Log.d("NumbersIntentServiceReceiver", "onReceive: database updated = " + updated);
            if (updated) {
                Log.d("NumbersIntentServiceReceiver.onReceive", "All numbers retrieved and database updated");
                Utils.hideProgressIndicator(activity);
            }
        }

    }

}

