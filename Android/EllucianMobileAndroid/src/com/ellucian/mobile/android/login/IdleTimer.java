package com.ellucian.mobile.android.login;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.MainActivity;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.util.Utils;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Logger.LogLevel;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public class IdleTimer extends Thread {
	
	private static final String TAG = IdleTimer.class.getName();
	Context context;
    private long lastUsed;
    private long period;
    private boolean stop;
	private GoogleAnalytics gaInstance;
	private Tracker gaTracker1;
	private Tracker gaTracker2;

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
                sendEvent(GoogleAnalyticsConstants.CATEGORY_AUTHENTICATION, GoogleAnalyticsConstants.ACTION_TIMEOUT, "Password Timeout", null, null);
                ((EllucianApplication)context).removeAppUser();
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
    
    /**
     * Create the tracker objects for Google Analytics
     */
	private void configureGoogleAnalytics() {
		gaInstance = GoogleAnalytics.getInstance(context);
        gaInstance.getLogger().setLogLevel(LogLevel.VERBOSE); 
        String trackerId1 = Utils.getStringFromPreferences(context, Utils.GOOGLE_ANALYTICS, Utils.GOOGLE_ANALYTICS_TRACKER1, null);
        String trackerId2 = Utils.getStringFromPreferences(context, Utils.GOOGLE_ANALYTICS, Utils.GOOGLE_ANALYTICS_TRACKER2, null);
        if(trackerId1 != null) {
			gaTracker1 = gaInstance.getTracker(trackerId1);
        }
        if(trackerId2 != null) {
        	gaTracker2 = gaInstance.getTracker(trackerId2);
        }
	}
    
    /**
     * Send event to google analytics
     * @param category
     * @param action
     * @param label
     * @param value
     * @param moduleName
     */
    public void sendEvent(String category, String action, String label, Long value, String moduleName) {
    	configureGoogleAnalytics();
    	sendEventToTracker1(category, action, label, value, moduleName);
    	sendEventToTracker2(category, action, label, value, moduleName);
    }
    
    /**
     * Send event to google analytics
     * @param category
     * @param action
     * @param label
     * @param value
     * @param moduleName
     */
    public void sendEventToTracker1(String category, String action, String label, Long value, String moduleName) {
    	if(gaTracker1 != null) {
    		MapBuilder mb = MapBuilder.createEvent(category, action, label, value);
    		String configurationName = Utils.getStringFromPreferences(context, Utils.CONFIGURATION, Utils.CONFIGURATION_NAME, null);
    		mb.set(Fields.customDimension(1), configurationName);
    		gaTracker1.send(mb.build());
    	}
    }
    
    
    /**
     * Send event to google analytics
     * @param category
     * @param action
     * @param label
     * @param value
     * @param moduleName
     */
    public void sendEventToTracker2(String category, String action, String label, Long value, String moduleName) {
    	if(gaTracker2 != null) {
    		MapBuilder mb = MapBuilder.createEvent(category, action, label, value);
    		String configurationName = Utils.getStringFromPreferences(context, Utils.CONFIGURATION, Utils.CONFIGURATION_NAME, null);
    		mb.set(Fields.customDimension(1), configurationName);
    		gaTracker2.send(mb.build());
    	}
    }
    	

}
