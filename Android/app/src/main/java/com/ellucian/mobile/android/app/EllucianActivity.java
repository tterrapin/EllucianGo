/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.client.services.AuthenticateUserIntentService;
import com.ellucian.mobile.android.client.services.ConfigurationUpdateService;
import com.ellucian.mobile.android.util.ConfigurationProperties;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

public abstract class EllucianActivity extends Activity implements DrawerLayoutActivity {
	private static final String TAG = EllucianActivity.class.getSimpleName();

	private static final long MILLISECONDS_PER_DAY = 24*60*60*1000;
	public String moduleId;
	public String moduleName;
	public String requestUrl;
	private DrawerLayoutHelper drawerLayoutHelper;
	private MainAuthenticationReceiver mainAuthenticationReceiver;
	private ConfigurationUpdateReceiver configReceiver;
	private SendToSelectionReceiver resetReceiver;
	private OutdatedReceiver outdatedReceiver;
	private UnauthenticatedUserReceiver unauthenticatedUserReceiver;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		String tag = getClass().getName();
       
        Intent incomingIntent = getIntent();
        if (incomingIntent.hasExtra(Extra.MODULE_ID)) {
        	moduleId = incomingIntent.getStringExtra(Extra.MODULE_ID);
        	Log.d(tag, "Activity moduleId set to: " + moduleId);
        } 
               
        if (incomingIntent.hasExtra(Extra.MODULE_NAME)) {
        	moduleName = incomingIntent.getStringExtra(Extra.MODULE_NAME);
        	Log.d(tag, "Activity moduleId set to: " + moduleId);
        } 
        
        if (incomingIntent.hasExtra(Extra.REQUEST_URL))	{
        	requestUrl = incomingIntent.getStringExtra(Extra.REQUEST_URL);
        	Log.d(tag, "Activity requestUrl set to: " + requestUrl);
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
    	configureNavigationDrawer();
	}
	
    @Override
	public void setContentView(View view, LayoutParams params) {
		super.setContentView(view, params);
		configureActionBar();
    	configureNavigationDrawer();
	}

	public void setContentView(int layoutResId) {
    	super.setContentView(layoutResId);
    	configureActionBar();
    	configureNavigationDrawer();
    }

	public void configureNavigationDrawer() {
		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    	ListView drawerList = (ListView) findViewById(R.id.left_drawer);
    	if(drawerLayout != null && drawerList != null) {
    		drawerLayoutHelper = new DrawerLayoutHelper(this, getEllucianApp().getModuleMenuAdapter());
    	}
	}
    
	/*
	 * 'standard' method that reads preferences for these values
	 * valid after a configuration has been loaded
	 */
	public void configureActionBar() {
	    int primaryColor = Utils.getPrimaryColor(this);
	    int headerTextColor = Utils.getHeaderTextColor(this);
	    
	    configureActionBarDirect(primaryColor, headerTextColor);
	}
    
	/*
	 * a 'direct' method that bypasses the preferences (used by 
	 * school selection, which can be called before any config
	 * has ever been loaded on the device
	 */
    public void configureActionBarDirect(int primaryColor, int headerTextColor) {
    	ActionBar bar = getActionBar();
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
    	invalidateOptionsMenu();    	
    }

    public EllucianApplication getEllucianApp() {
        return (EllucianApplication )this.getApplication();
    }
    
    public ConfigurationProperties getConfigurationProperties() {
        return getEllucianApp().getConfigurationProperties();
    }
    
    public DrawerLayoutHelper getDrawerLayoutHelper() {
    	return drawerLayoutHelper;
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        getEllucianApp().touch();
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
    	getEllucianApp().sendEvent(category, action, label, value, moduleName);
    }
    
    /**
     * Send event to google analytics for just tracker 1
     * @param category
     * @param action
     * @param label
     * @param value
     * @param moduleName
     */
    public void sendEventToTracker1(String category, String action, String label, Long value, String moduleName) {
    	getEllucianApp().sendEventToTracker1(category, action, label, value, moduleName);
    }
    
    /**
     * Send event to google analytics for just tracker 2
     * @param category
     * @param action
     * @param label
     * @param value
     * @param moduleName
     */
    public void sendEventToTracker2(String category, String action, String label, Long value, String moduleName) {
    	getEllucianApp().sendEventToTracker2(category, action, label, value, moduleName);
    }
    
    
    /**
     * Send view to google analytics
     * @param appScreen
     */
    public void sendView(String appScreen, String moduleName) {
    	getEllucianApp().sendView(appScreen, moduleName);
    }
    
    /**
     * Send view to google analytics for just tracker 1
     * @param appScreen
     */
    public void sendViewToTracker1(String appScreen, String moduleName) {
    	getEllucianApp().sendViewToTracker1(appScreen, moduleName);
    }
    
    /**
     * Send view to google analytics for just tracker 2
     * @param appScreen
     */
    public void sendViewToTracker2(String appScreen, String moduleName) {
    	getEllucianApp().sendViewToTracker2(appScreen, moduleName);
    }
    
	/**
	 * Send timing to google analytics
	 * @param category
	 * @param value
	 * @param name
	 * @param label
	 * @param moduleName
	 */
	public void sendUserTiming(String category, long value, String name, String label, String moduleName) {
		getEllucianApp().sendUserTiming(category, value, name, label, moduleName);
	}

	/**
	 * Send timing to google analytics for just tracker 1
	 * @param category
	 * @param value
	 * @param name
	 * @param label
	 * @param moduleName
	 */
	public void sendUserTimingToTracker1(String category, long value, String name, String label, String moduleName) {
		getEllucianApp().sendUserTimingToTracker1(category, value, name, label, moduleName);
	}

	/**
	 * Send timing to google analytics for just tracker 2
	 * @param category
	 * @param value
	 * @param name
	 * @param label
	 * @param moduleName
	 */
	public void sendUserTimingToTracker2(String category, long value, String name, String label, String moduleName) {
		getEllucianApp().sendUserTimingToTracker2(category, value, name, label, moduleName);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		if (drawerLayoutHelper != null) {
			drawerLayoutHelper.removeMenuItems(menu);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (drawerLayoutHelper.drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
        // Handle your other action bar items...
        
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (drawerLayoutHelper != null) {
			drawerLayoutHelper.drawerToggle.syncState();
			invalidateOptionsMenu();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (drawerLayoutHelper != null) {
			drawerLayoutHelper.onConfigurationChanged(newConfig);
			invalidateOptionsMenu();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();

		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
		
		lbm.unregisterReceiver(mainAuthenticationReceiver);
		lbm.unregisterReceiver(configReceiver);
		lbm.unregisterReceiver(resetReceiver);
		lbm.unregisterReceiver(outdatedReceiver);
		lbm.unregisterReceiver(unauthenticatedUserReceiver);

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		String tag = getClass().getName();
		
		SharedPreferences preferences = getSharedPreferences(Utils.CONFIGURATION, MODE_PRIVATE);
		String configUrl = preferences.getString(Utils.CONFIGURATION_URL, null);
		
		long lastUpdate = preferences.getLong(Utils.CONFIGURATION_LAST_UPDATE, 0);
		Log.d(tag, "last update time: " + lastUpdate);
		
		if(lastUpdate != 0 && (lastUpdate + MILLISECONDS_PER_DAY) < System.currentTimeMillis()) {
			Log.d(tag, "24 hours past since last update, updating configuration");
			Intent intent = new Intent(this, ConfigurationUpdateService.class);
			intent.putExtra(Extra.CONFIG_URL, configUrl);			
			intent.putExtra(ConfigurationUpdateService.REFRESH, true);	
			startService(intent);
		}
		
		//notifications
		if (getEllucianApp().isUserAuthenticated()) {
			if (System.currentTimeMillis() > getEllucianApp().getLastNotificationsCheck() + EllucianApplication.DEFAULT_NOTIFICATIONS_REFRESH) {
				Log.d(TAG, "startingNotifications");
				getEllucianApp().startNotifications();
			}
		}
		
		// call registerWithGcmIfNeeded often - it checks criteria to see if it needs to register or re-register
		getEllucianApp().registerWithGcmIfNeeded();

		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
		
		configReceiver = new ConfigurationUpdateReceiver(this);
		lbm.registerReceiver(configReceiver, new IntentFilter(ConfigurationUpdateService.ACTION_SUCCESS));
		
		resetReceiver = new SendToSelectionReceiver(this);
		lbm.registerReceiver(resetReceiver, new IntentFilter(ConfigurationUpdateService.ACTION_SEND_TO_SELECTION));
		
		outdatedReceiver = new OutdatedReceiver(this);
		lbm.registerReceiver(outdatedReceiver, new IntentFilter(ConfigurationUpdateService.ACTION_OUTDATED));
		
		mainAuthenticationReceiver = new MainAuthenticationReceiver(this);
		lbm.registerReceiver(mainAuthenticationReceiver, new IntentFilter(AuthenticateUserIntentService.ACTION_UPDATE_MAIN));
		
		unauthenticatedUserReceiver = new UnauthenticatedUserReceiver(this, moduleId);
		lbm.registerReceiver(unauthenticatedUserReceiver, new IntentFilter(MobileClient.ACTION_UNAUTHENTICATED_USER));
	}

	/**
	 * Called to process touch screen events. At the very least your
	 * implementation must call superDispatchTouchEvent(MotionEvent) to do the
	 * standard touch screen processing. Overriding to capture EditText
	 * objects. If the user touches outside the EditText, dismiss the keyboard
	 * 
	 * @param event	The touch screen event.
	 * @return boolean Return true if this event was consumed.
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {

		View v = getCurrentFocus();
		boolean ret = super.dispatchTouchEvent(event);

		if (v instanceof EditText) {
			View w = getCurrentFocus();
			int scrcoords[] = new int[2];
			w.getLocationOnScreen(scrcoords);
			float x = event.getRawX() + w.getLeft() - scrcoords[0];
			float y = event.getRawY() + w.getTop() - scrcoords[1];

			Log.d("Activity",
					"Touch event " + event.getRawX() + "," + event.getRawY()
							+ " " + x + "," + y + " rect " + w.getLeft() + ","
							+ w.getTop() + "," + w.getRight() + ","
							+ w.getBottom() + " coords " + scrcoords[0] + ","
							+ scrcoords[1]);
			if (event.getAction() == MotionEvent.ACTION_UP
					&& (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w
							.getBottom())) {

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getWindow().getCurrentFocus()
						.getWindowToken(), 0);
			}
		}
		return ret;
	}
}


