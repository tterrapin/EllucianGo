package com.ellucian.mobile.android.app;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.ActionBar;
import android.app.ExpandableListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;
import com.google.analytics.tracking.android.ExceptionReporter;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.google.analytics.tracking.android.Logger.LogLevel;

public abstract class EllucianExpandableListActivity extends ExpandableListActivity {
	public String moduleId;
	public String moduleName;
	public String requestUrl;
	private GoogleAnalytics gaInstance;
	private Tracker gaTracker1;
	private Tracker gaTracker2;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        configureGoogleAnalytics();
       
        Intent incomingIntent = getIntent();
        if (incomingIntent.hasExtra(Extra.MODULE_ID)) {
        	moduleId = incomingIntent.getStringExtra(Extra.MODULE_ID);
        	Log.d("Activity", "Activity moduleId set to: " + moduleId);
        } 
               
        if (incomingIntent.hasExtra(Extra.MODULE_NAME)) {
        	moduleName = incomingIntent.getStringExtra(Extra.MODULE_NAME);
        	Log.d("Activity", "Activity moduleId set to: " + moduleId);
        } 
        
        if (incomingIntent.hasExtra(Extra.REQUEST_URL))	{
        	requestUrl = incomingIntent.getStringExtra(Extra.REQUEST_URL);
        	Log.d("Activity", "Activity requestUrl set to: " + requestUrl);
        } else {
        	requestUrl = "";
        }
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		getActionBar();
		setProgressBarIndeterminateVisibility(false);
        
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(view);
		configureActionBar();
	}
	
    @Override
	public void setContentView(View view, LayoutParams params) {
		super.setContentView(view, params);
		configureActionBar();
	}

	public void setContentView(int layoutResId) {
    	super.setContentView(layoutResId);
    	configureActionBar();
    }
    
    public void configureActionBar() {
    	ActionBar bar = getActionBar();
	    
	    int primaryColor = Utils.getPrimaryColor(this);
	    int headerTextColor = Utils.getHeaderTextColor(this);
    	bar.setBackgroundDrawable(new ColorDrawable(primaryColor));
    	bar.setSplitBackgroundDrawable(new ColorDrawable(primaryColor));
    	bar.setStackedBackgroundDrawable(new ColorDrawable(primaryColor));
    	int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
    	TextView title = (TextView)findViewById(titleId);
    	if(title != null) {
    		title.setTextColor(headerTextColor);
    	}
    	if(Utils.hasDefaultMenuIcon(this)) {
    		bar.setIcon(getResources().getDrawable(R.drawable.default_home_icon));
    	} else {
    		Drawable menuIcon = Utils.getMenuIcon(this);
    		if (menuIcon != null) {
    			bar.setIcon(menuIcon);
    		} else {
    			bar.setIcon(getResources().getDrawable(R.drawable.default_home_icon));
    		}
    	}
    }
	
    public EllucianApplication getEllucianApp() {
        return (EllucianApplication )this.getApplication();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        getEllucianApp().touch();
    }

    /**
     * Create the tracker objects for Google Analytics
     */
	private void configureGoogleAnalytics() {
		gaInstance = GoogleAnalytics.getInstance(this);
        gaInstance.getLogger().setLogLevel(LogLevel.VERBOSE); 
        String trackerId1 = Utils.getStringFromPreferences(this, Utils.GOOGLE_ANALYTICS, Utils.GOOGLE_ANALYTICS_TRACKER1, null);
        String trackerId2 = Utils.getStringFromPreferences(this, Utils.GOOGLE_ANALYTICS, Utils.GOOGLE_ANALYTICS_TRACKER2, null);
        if(trackerId1 != null) {
			gaTracker1 = gaInstance.getTracker(trackerId1);
			UncaughtExceptionHandler handler = new ExceptionReporter(
					gaTracker1, GAServiceManager.getInstance(),
					Thread.getDefaultUncaughtExceptionHandler(), this);
			Thread.setDefaultUncaughtExceptionHandler(handler);
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
    		String configurationName = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.CONFIGURATION_NAME, null);
    		mb.set(Fields.customDimension(1), configurationName);
    		if(moduleName != null) mb.set(Fields.customDimension(2), moduleName);
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
    		String configurationName = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.CONFIGURATION_NAME, null);
    		mb.set(Fields.customDimension(1), configurationName);
    		if(moduleName != null) mb.set(Fields.customDimension(2), moduleName);
    		gaTracker2.send(mb.build());
    	}
    }
    
    /**
     * Send view to google analytics
     * @param appScreen
     */
    public void sendView(String appScreen, String moduleName) {
    	sendViewToTracker1(appScreen, moduleName);
    	sendViewToTracker2(appScreen, moduleName);
    }
    
    /**
     * Send view to google analytics for just tracker 1
     * @param appScreen
     */
    public void sendViewToTracker1(String appScreen, String moduleName) {
    	if(gaTracker1 != null) {
    		MapBuilder mb = MapBuilder.createAppView().set(Fields.SCREEN_NAME, appScreen);
    		String configurationName = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.CONFIGURATION_NAME, null);
    		mb.set(Fields.customDimension(1), configurationName);
    		if(moduleName != null) mb.set(Fields.customDimension(2), moduleName);
			gaTracker1.send(mb.build());
    	}
    }
    
    /**
     * Send view to google analytics for just tracker 2
     * @param appScreen
     */
    public void sendViewToTracker2(String appScreen, String moduleName) {
    	if(gaTracker2 != null) {
    		MapBuilder mb = MapBuilder.createAppView().set(Fields.SCREEN_NAME, appScreen);
    		String configurationName = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.CONFIGURATION_NAME, null);
    		mb.set(Fields.customDimension(1), configurationName);
    		if(moduleName != null) mb.set(Fields.customDimension(2), moduleName);
			gaTracker2.send(mb.build());
    	}
    }
}

