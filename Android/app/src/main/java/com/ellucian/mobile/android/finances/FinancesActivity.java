/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.finances;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;

public class FinancesActivity extends EllucianActivity {

    public static final String TAG = "FinancesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_finances);

        setTitle(moduleName);

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment financesFragment = manager.findFragmentById(R.id.finances_frame);
        if (financesFragment == null) {
            financesFragment = new FinancesFragment();
            financesFragment.setArguments(getIntent().getExtras());
            transaction.add(R.id.finances_frame, financesFragment);
        } else {
            transaction.attach(financesFragment);
        }
        transaction.commit();

    }
}