package com.ellucian.mobile.android.app;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
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
import com.ellucian.mobile.android.MainActivity;
import com.ellucian.mobile.android.client.services.AuthenticateUserIntentService;
import com.ellucian.mobile.android.client.services.ConfigurationUpdateService;
import com.ellucian.mobile.android.util.ConfigurationProperties;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;
import com.google.analytics.tracking.android.ExceptionReporter;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Logger.LogLevel;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public abstract class EllucianActivity extends Activity {
	private static final String TAG = EllucianActivity.class.getName();
	private static final long MILLISECONDS_PER_DAY = 24*60*60*1000;
	public String moduleId;
	public String moduleName;
	public String requestUrl;
	private GoogleAnalytics gaInstance;
	private Tracker gaTracker1;
	private Tracker gaTracker2;
	private DrawerLayoutHelper drawerLayoutHelper;
	private MainAuthenticationReceiver mainAuthenticationReceiver;
	private ConfigurationUpdateReceiver configReceiver;
	private SendToSelectionReceiver resetReceiver;
	private OutdatedReceiver outdatedReceiver;
	private boolean noReset;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
        configureGoogleAnalytics();
       
        Intent incomingIntent = getIntent();
        if (incomingIntent.hasExtra(Extra.MODULE_ID)) {
        	moduleId = incomingIntent.getStringExtra(Extra.MODULE_ID);
        	Log.d(TAG, "Activity moduleId set to: " + moduleId);
        } 
               
        if (incomingIntent.hasExtra(Extra.MODULE_NAME)) {
        	moduleName = incomingIntent.getStringExtra(Extra.MODULE_NAME);
        	Log.d(TAG, "Activity moduleId set to: " + moduleId);
        } 
        
        if (incomingIntent.hasExtra(Extra.REQUEST_URL))	{
        	requestUrl = incomingIntent.getStringExtra(Extra.REQUEST_URL);
        	Log.d(TAG, "Activity requestUrl set to: " + requestUrl);
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

	private void configureNavigationDrawer() {
		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    	ListView drawerList = (ListView) findViewById(R.id.left_drawer);
    	if(drawerLayout != null && drawerList != null) {
    		drawerLayoutHelper = new DrawerLayoutHelper(this);
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
     * Send event to google analytics for just tracker 1
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
     * Send event to google analytics for just tracker 2
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
    	sendViewToTracker1(appScreen, "TEST TEST TEST");
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

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		SharedPreferences preferences = getSharedPreferences(Utils.CONFIGURATION, MODE_PRIVATE);
		String configUrl = preferences.getString(Utils.CONFIGURATION_URL, null);
		
		long lastUpdate = preferences.getLong(Utils.CONFIGURATION_LAST_UPDATE, 0);
		Log.d(TAG, "last update time: " + lastUpdate);
		
		if(lastUpdate != 0 && (lastUpdate + MILLISECONDS_PER_DAY) < System.currentTimeMillis()) {
			Log.d(TAG, "24 hours past since last update, updating configuration");
			noReset = true;
			Intent intent = new Intent(this, ConfigurationUpdateService.class);
			intent.putExtra(Extra.CONFIG_URL, configUrl);			
			startService(intent);
		}
		
		//notifications
		if (getEllucianApp().isUserAuthenticated()) {
			if (System.currentTimeMillis() > getEllucianApp().getLastNotificationsCheck() + EllucianApplication.DEFAULT_NOTIFICATIONS_REFRESH) {
				Log.d("MainActivity.onStart", "startingNotifications");
				getEllucianApp().startNotifications();
			}
		}
		
		// call registerWithGcmIfNeeded often - it checks criteria to see if it needs to register or re-register
		getEllucianApp().registerWithGcmIfNeeded();

		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
		
		configReceiver = new ConfigurationUpdateReceiver();
		lbm.registerReceiver(configReceiver, new IntentFilter(ConfigurationUpdateService.ACTION_SUCCESS));
		
		resetReceiver = new SendToSelectionReceiver();
		lbm.registerReceiver(resetReceiver, new IntentFilter(ConfigurationUpdateService.ACTION_SEND_TO_SELECTION));
		
		outdatedReceiver = new OutdatedReceiver();
		lbm.registerReceiver(outdatedReceiver, new IntentFilter(ConfigurationUpdateService.ACTION_OUTDATED));
		
		mainAuthenticationReceiver = new MainAuthenticationReceiver();
		lbm.registerReceiver(mainAuthenticationReceiver, new IntentFilter(AuthenticateUserIntentService.ACTION_UPDATE_MAIN));	
	}
	
	public class MainAuthenticationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent incomingIntent) {	
			if(drawerLayoutHelper != null) {
				drawerLayoutHelper.invalidateItems();
			}
		}		
	}
	
	public class ConfigurationUpdateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(final Context context, Intent incomingIntent) {
			Log.d(TAG, "onReceive, ConfigurationUpdateReceiver");
			setProgressBarIndeterminateVisibility(Boolean.FALSE);

			boolean upgradeAvailable = incomingIntent.getBooleanExtra(
					ConfigurationUpdateService.PARAM_UPGRADE_AVAILABLE, false);
			
			if (upgradeAvailable) {

				// launch an Activity to allow the use of an AlertDialog
				Intent i = new Intent(context, ConfigurationUpdateReceiverActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("upgradeAvailable",  upgradeAvailable);
				i.putExtra("negativeButtonAction", false);
				context.startActivity(i);
			} else {

				if (!noReset) {
					Log.d(TAG, "logoutUser flag is set");
					// Logging out any user
					((EllucianApplication) getApplication()).removeAppUser();
					loadMainActivity();
				} 
			}
		}
	}
	
	private void loadMainActivity() {
		Log.d(TAG, "Starting MainActivity");
		Intent mainIntent = new Intent(this, MainActivity.class);
		mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(mainIntent);
	}
	
	public class SendToSelectionReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(final Context context, Intent incomingIntent) {
			Log.d(TAG, "onReceive, SendToSelectionReceiver");

			setProgressBarIndeterminateVisibility(Boolean.FALSE);
			Utils.removeValuesFromPreferences(context, Utils.CONFIGURATION, Utils.CONFIGURATION_URL);

			// launch an Activity to allow the use of an AlertDialog
			Intent i = new Intent(context, SendToSelectionReceiverActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
	}

	public class OutdatedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(final Context context, Intent incomingIntent) {
			Log.d(TAG, "onReceive, OutdatedReceiver");

			setProgressBarIndeterminateVisibility(Boolean.FALSE);
			Utils.removeValuesFromPreferences(context, Utils.CONFIGURATION, Utils.CONFIGURATION_URL);
			
			// launch an Activity to allow the use of an AlertDialog
			Intent i = new Intent(context, OutdatedReceiverActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
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


