/*
 * Copyright 2016 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.directory;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.client.directory.DirectoryResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class RetrieveDirectoryInfoTask extends AsyncTask<String, Void, DirectoryResponse> {
    private static final String TAG = RetrieveDirectoryInfoTask.class.getSimpleName();

    final Activity activity;

    public RetrieveDirectoryInfoTask(Activity activity) {
        this.activity = activity;
    }

    // params (requestUrl, query [, directories])
    @Override
    protected DirectoryResponse doInBackground(String... params) {
        DirectoryResponse response = null;

        String requestUrl = params[0];
        String query = params[1];
        String directories = null;
        if (params.length > 2) {
            directories = params[2];
        } else {
            Log.d(TAG, "No directories parameter passed");
        }

        if (!TextUtils.isEmpty(requestUrl) && !TextUtils.isEmpty(query)) {
            String modifiedUrl = requestUrl;

            String encodedQuery = null;
            String encodedDirectories = null;
            try {
                encodedQuery = URLEncoder.encode(query, "UTF-8");
                if (!TextUtils.isEmpty(directories)) {
                    encodedDirectories = URLEncoder.encode(directories, "UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(encodedQuery)) {
                modifiedUrl += "?searchString=" + encodedQuery;
                if (!TextUtils.isEmpty(encodedDirectories)) {
                    modifiedUrl += "&directories=" + encodedDirectories;
                }
            }

            Log.d(TAG, "Directory search url, with params:" + modifiedUrl);

            MobileClient client = new MobileClient(activity);
            response = client.searchDirectory(modifiedUrl);

        } else {
            Log.d(TAG, "requestUrl or query is missing, no request sent.");
            Log.d(TAG, "requestUrl: " + requestUrl);
            Log.d(TAG, "query: " + query);
        }
        return response;
    }

}
