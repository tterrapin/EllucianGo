/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.courses;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.util.Log;

import com.ellucian.mobile.android.courses.daily.CoursesDailyScheduleActivity;
import com.ellucian.mobile.android.courses.full.CoursesFullScheduleActivity;

public class CoursesTabListener implements TabLayout.OnTabSelectedListener {

    public static final String TAG = "CoursesTabListener";
    public static final int DAILY_VIEW_TAB_INDEX = 0;
    public static final int FULL_VIEW_TAB_INDEX = 1;
    private Context mContext;
    private Intent mIntent;

    public CoursesTabListener(Context context, Intent intent) {
        mContext = context;
        mIntent = intent;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        Log.d(TAG, "Tab " + tab + " selected");

        switch (tab.getPosition()) {
            case FULL_VIEW_TAB_INDEX: // Full Schedule View
                Intent fullViewIntent = new Intent(mContext, CoursesFullScheduleActivity.class);
                // Pass Extras on to next Activity
                fullViewIntent.putExtras(mIntent.getExtras());
                mContext.startActivity(fullViewIntent);
                break;
            case DAILY_VIEW_TAB_INDEX: // Daily Schedule View
                Intent dailyViewIntent = new Intent(mContext, CoursesDailyScheduleActivity.class);
                // Pass Extras on to next Activity
                dailyViewIntent.putExtras(mIntent.getExtras());
                dailyViewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(dailyViewIntent);
                break;
        }

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        Log.d(TAG, "Tab " + tab + " unselected");
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        onTabSelected(tab);
    }

}
