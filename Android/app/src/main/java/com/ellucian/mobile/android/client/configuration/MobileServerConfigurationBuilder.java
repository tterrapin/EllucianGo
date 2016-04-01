/*
 * Copyright 2016 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.configuration;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.util.Log;

import com.ellucian.mobile.android.client.ContentProviderOperationBuilder;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class MobileServerConfigurationBuilder extends ContentProviderOperationBuilder<JSONObject> {
    private static final String TAG = MobileServerConfigurationBuilder.class.getSimpleName();
    private final Context context;

    public MobileServerConfigurationBuilder(Context context) {
        super(context);
        this.context = context;
    }

    /* (non-Javadoc)
     * @see com.ellucian.mobile.android.client.ContentProviderOperationBuilder#buildOperations(java.lang.Object)
     */
    @Override
    public ArrayList<ContentProviderOperation> buildOperations(JSONObject jsonConfiguration) {
        final ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        try {
            // Add Mobile Server's last updated date and codebase version as user preferences
            if (jsonConfiguration.has("lastUpdated")) {
                Utils.addStringToPreferences(context, Utils.CONFIGURATION, Utils.MOBILESERVER_CONFIG_LAST_UPDATE, jsonConfiguration.getString("lastUpdated"));
            }
            if (jsonConfiguration.has("codebaseVersion")) {
                Utils.addStringToPreferences(context, Utils.CONFIGURATION, Utils.MOBILESERVER_CODEBASE_VERSION, jsonConfiguration.getString("codebaseVersion"));
            }

            // Additional Directory Info to be saved to content provider
            if (jsonConfiguration.has("directories")) {
                JSONObject directories = jsonConfiguration.getJSONObject("directories");

                Iterator<String> directoryKeys = directories.keys();
                // delete all existing directories information
                batch.add(ContentProviderOperation.newDelete(EllucianContract.Directories.CONTENT_URI).build());

                while (directoryKeys.hasNext()) {
                    String directoryKey = directoryKeys.next();
                    JSONObject directoryInfo = directories.getJSONObject(directoryKey);
                    String internalName = null;
                    String displayName = null;
                    String authenticatedOnly = null;
                    if (directoryInfo.has("internalName")) {
                        internalName = directoryInfo.getString("internalName");
                    }
                    if (directoryInfo.has("displayName")) {
                        displayName = directoryInfo.getString("displayName");
                    }
                    if (directoryInfo.has("authenticatedOnly")) {
                        authenticatedOnly = directoryInfo.getString("authenticatedOnly");
                    }
                    batch.add(ContentProviderOperation
                            .newInsert(EllucianContract.Directories.CONTENT_URI)
                            .withValue(EllucianContract.Directories.DIRECTORY_KEY, directoryKey)
                            .withValue(EllucianContract.Directories.DIRECTORY_DISPLAY_NAME, displayName)
                            .withValue(EllucianContract.Directories.DIRECTORY_INTERNAL_NAME, internalName)
                            .withValue(EllucianContract.Directories.DIRECTORY_AUTHENTICATED_ONLY, authenticatedOnly)
                            .build());
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException:", e);
        }

        return batch;
    }


}
