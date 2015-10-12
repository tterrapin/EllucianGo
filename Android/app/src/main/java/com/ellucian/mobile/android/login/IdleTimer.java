/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.login;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.MainActivity;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;

public class IdleTimer extends Thread {
	
	private static final String TAG = IdleTimer.class.getName();
	private final Context context;
    private long lastUsed;
    private long period;
    private boolean stop;

    public IdleTimer(Context context, long period) {
    	this.context = context;
        this.period = period;
        stop=false;
    }

    public void run() {
        long idle = 0;
        this.touch();
        do {
            idle = System.currentTimeMillis()-lastUsed;
            try {
                Thread.sleep(15000); //check every 15 seconds
            } catch (InterruptedException e) {
                Log.d(TAG, "Timer interrupted!");
            }
            if(idle > period) {
                idle = 0;
                EllucianApplication application = (EllucianApplication) context.getApplicationContext();

                application.sendEvent(GoogleAnalyticsConstants.CATEGORY_AUTHENTICATION, GoogleAnalyticsConstants.ACTION_TIMEOUT, "Password Timeout", null, null);
                application.removeAppUser();
                
                // Make sure to reset the menu adapter so the navigation drawer will 
				// display correctly for a non-authenticated user
                application.resetModuleMenuAdapter();
                
                Intent mainIntent = new Intent(context, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(mainIntent);
                stop=true;
            }
        }
        while(!stop);
    }

    public synchronized void touch() {
        lastUsed=System.currentTimeMillis();
    }

    public synchronized void forceInterrupt() {
        this.interrupt();
    }

    //soft stopping of thread
    public synchronized void stopTimer() {
        stop=true;
    }

    public synchronized void setPeriod(long period) {
        this.period=period;
    }
}
