/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.finances;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.util.Utils;

public class FinancesActivity extends EllucianActivity {

    private static final String TAG = "FinancesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_finances);

        setTitle(moduleName);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment financesFragment = manager.findFragmentById(R.id.finances_frame);
        if (financesFragment == null) {
            Utils.showProgressIndicator(this);
            financesFragment = new FinancesFragment();
            financesFragment.setArguments(getIntent().getExtras());
            transaction.add(R.id.finances_frame, financesFragment);
        } else {
            transaction.attach(financesFragment);
        }
        transaction.commit();

    }
}