/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.ilp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.MainActivity;
import com.ellucian.mobile.android.ModuleType;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.ilp.widget.AssignmentsWidgetService;
import com.ellucian.mobile.android.login.QueuedIntentHolder;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

public class IlpCardActivity extends EllucianActivity {
    private static final String TAG = IlpCardActivity.class.getSimpleName();
	private IlpCardFragment fragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_default_frame);

        if (TextUtils.isEmpty(moduleName)) {
            // When coming from Widget, moduleName is not known.
            String title = Utils.getStringFromPreferences(getApplicationContext(), Utils.CONFIGURATION, Utils.ILP_NAME, null);
            setTitle(title);
        } else {
            setTitle(moduleName);
        }

        if (getIntent().getBooleanExtra(AssignmentsWidgetService.LAUNCHED_FROM_APPWIDGET, false)) {
            // Click event on AppWidget started this activity
            sendEvent(GoogleAnalyticsConstants.CATEGORY_WIDGET, GoogleAnalyticsConstants.ACTION_LIST_SELECT, "Assignments", null, "AssignmentsWidgetProvider" );
            getIntent().removeExtra(AssignmentsWidgetService.LAUNCHED_FROM_APPWIDGET);
        }

        EllucianApplication app = getEllucianApp();
        if(!app.isUserAuthenticated()) {
            Log.e(TAG, "User not authenticated, sending to home.");
            // Pass the incoming Intent as an extra, so after
            // login user is directed back here.
            Intent queuedIntent = getIntent();
            if (moduleId == null) {
                Cursor cursor = getContentResolver().query(EllucianContract.Modules.CONTENT_URI,
                        new String[] {EllucianContract.Modules.MODULES_ID},
                        EllucianContract.Modules.MODULE_TYPE + "= ?",
                        new String[]{ModuleType.ILP},
                        null);
                if (cursor.moveToFirst()) {
                    moduleId = cursor.getString(cursor.getColumnIndex(EllucianContract.Modules.MODULES_ID));
                }
                cursor.close();
            }

            QueuedIntentHolder queuedIntentHolder = new QueuedIntentHolder(moduleId, queuedIntent);

            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mainIntent.putExtra(MainActivity.SHOW_LOGIN, true);
            mainIntent.putExtra(QueuedIntentHolder.QUEUED_INTENT_HOLDER, queuedIntentHolder);
            startActivity(mainIntent);
            finish();
            return;
        } else if (getIntent().getBooleanExtra(IlpListActivity.SHOW_DETAIL, false)) {
            Intent intent = new Intent();
            intent.setClass(this, IlpListActivity.class);
            intent.putExtras(getIntent().getExtras());
            // make sure to clear the request to show the detail after past on
            getIntent().removeExtra(IlpListActivity.SHOW_DETAIL);
            startActivity(intent);
        }

		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		fragment =  (IlpCardFragment) manager.findFragmentByTag("IlpCardFragment");

		if (fragment == null) {
			fragment = new IlpCardFragment();
			Bundle args = new Bundle();
			args.putString(Extra.COURSES_ILP_URL, getIntent().getStringExtra(Extra.COURSES_ILP_URL));
			fragment.setArguments(args);
			transaction.add(R.id.frame, fragment, "IlpCardFragment");
		} else {
			transaction.attach(fragment);
		}

		transaction.commit();

	}
	
}
