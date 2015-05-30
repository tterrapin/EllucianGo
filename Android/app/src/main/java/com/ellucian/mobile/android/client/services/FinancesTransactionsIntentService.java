/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.client.finances.TransactionsResponse;
import com.ellucian.mobile.android.util.Extra;

public class FinancesTransactionsIntentService extends IntentService {

    public static final String TAG = "FinancesTransactions";
    public static final String ACTION_UPDATE_FINISHED = "com.ellucian.mobile.android.client.FinancesTransactionsIntentService.action.update.finished";
    public static final String UPDATE_RESULT = "updateResult";
    public FinancesTransactionsIntentService() {
        super("FinancesTransactionsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean success = false;
        Log.d(TAG, "handling intent");
        MobileClient client = new MobileClient(this);
        String url = client.addUserToUrl(intent.getStringExtra(Extra.REQUEST_URL))+"/transactions";

        TransactionsResponse response = client.getFinanceTransactions(url);
        if (response != null) {
            Log.d(TAG, "Retrieved transaction response from client");
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTION_UPDATE_FINISHED);
            broadcastIntent.putExtra(UPDATE_RESULT, response);
            lbm.sendBroadcast(broadcastIntent);
        } else {
            Log.d(TAG, "Response Object was null");
        }

    }
}
