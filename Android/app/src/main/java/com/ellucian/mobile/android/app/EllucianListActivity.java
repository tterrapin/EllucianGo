package com.ellucian.mobile.android.app;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.client.services.AuthenticateUserIntentService;
import com.ellucian.mobile.android.client.services.ConfigurationUpdateService;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

@Deprecated
public abstract class EllucianListActivity extends ListActivity implements DrawerLayoutActivity {
	public String moduleId;
	public String moduleName;
	public String requestUrl;
	private MainAuthenticationReceiver mainAuthenticationReceiver;
	private ConfigurationUpdateReceiver configReceiver;
	private SendToSelectionReceiver resetReceiver;
	private OutdatedReceiver outdatedReceiver;
	private UnauthenticatedUserReceiver unauthenticatedUserReceiver;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
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
		
		if(lastUpdate != 0 && (lastUpdate + EllucianApplication.MILLISECONDS_PER_DAY) < System.currentTimeMillis()) {
			Log.d(tag, "24 hours past since last update, updating configuration");
			Intent intent = new Intent(this, ConfigurationUpdateService.class);
			intent.putExtra(Extra.CONFIG_URL, configUrl);			
			intent.putExtra(ConfigurationUpdateService.REFRESH, true);	
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
}

