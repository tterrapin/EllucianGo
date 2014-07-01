package com.ellucian.mobile.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.CookieManager;

import com.ellucian.mobile.android.adapter.ModuleMenuAdapter;
import com.ellucian.mobile.android.client.services.NotificationsIntentService;
import com.ellucian.mobile.android.login.IdleTimer;
import com.ellucian.mobile.android.login.User;
import com.ellucian.mobile.android.notifications.DeviceNotifications;
import com.ellucian.mobile.android.notifications.EllucianNotificationManager;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.util.ConfigurationProperties;
import com.ellucian.mobile.android.util.Encrypt;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.ModuleConfiguration;
import com.ellucian.mobile.android.util.PRNGFixes;
import com.ellucian.mobile.android.util.Utils;
import com.ellucian.mobile.android.util.WebkitCookieManagerProxy;
import com.ellucian.mobile.android.util.XmlParser;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.HitBuilders.AppViewBuilder;
import com.google.android.gms.analytics.HitBuilders.EventBuilder;
import com.google.android.gms.analytics.Logger.LogLevel;
import com.google.android.gms.analytics.Tracker;

public class EllucianApplication extends Application {
	public static final String TAG = EllucianApplication.class.getSimpleName();

	private HashMap<String, Object> liveObjects = new HashMap<String, Object>();
	private User user;
	private IdleTimer idleTimer;
	private long idleTime = 30 * 60 * 1000; // 30 Minutes
	private DeviceNotifications deviceNotifications;
	private long lastNotificationsCheck;
	public static final long DEFAULT_NOTIFICATIONS_REFRESH = 60 * 60 * 1000; // 60 minutes
	private long lastAuthRefresh;
	private ConfigurationProperties configurationProperties;
	private HashMap<String, ModuleConfiguration> moduleConfigMap;
	private EllucianNotificationManager ellucianNotificationManager;
	public static final long MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
	private ModuleMenuAdapter moduleMenuAdapter;

	// Google Analytics trackers
	private Tracker gaTracker1;
	private Tracker gaTracker2;

	@Override
	public void onCreate() {
		super.onCreate();
		
		PRNGFixes.apply();

		GoogleAnalytics.getInstance(this).getLogger()
				.setLogLevel(LogLevel.VERBOSE);

		idleTimer = new IdleTimer(this, idleTime);
		deviceNotifications = new DeviceNotifications(this);
		lastAuthRefresh = 0;

		// Setting up cookie management
		android.webkit.CookieSyncManager.createInstance(this);
		android.webkit.CookieManager.getInstance().setAcceptCookie(true);
		WebkitCookieManagerProxy coreCookieManager = new WebkitCookieManagerProxy(
				null, java.net.CookiePolicy.ACCEPT_ALL);
		java.net.CookieHandler.setDefault(coreCookieManager);

		loadSavedUser();

		// Creating objects with configuration information
		configurationProperties = XmlParser
				.createConfigurationPropertiesFromXml(this);
		moduleConfigMap = XmlParser.createModuleConfigMapFromXml(this, 0);

		ellucianNotificationManager = new EllucianNotificationManager(this);
	}

	public Object getCachedObject(String key) {
		return liveObjects.get(key);
	}

	public void putCachedObject(String key, Object value) {
		liveObjects.put(key, value);
	}

	public void createAppUser(String userId, String username, String password,
			List<String> roles) {
		user = new User();
		user.setId(userId);
		user.setName(username);
		user.setPassword(password);
		user.setRoles(roles);

		lastAuthRefresh = System.currentTimeMillis();

		String logString = "App User created:\n" + "userId: " + userId;
		logString += "\n" + "username:" + username;
		if(roles != null && roles.size() > 0) {
			logString += "\n" + "roles:" + roles.toString();
		}
		Log.d("EllucianApplication.createAppUser", logString);
	}

	public void removeAppUser() {
		user = null;
		getContentResolver().delete(EllucianContract.SECURED_CONTENT_URI, null,
				null);
		Utils.removeSavedUser(this);

		// Removed saved cookies on user logout
		CookieManager.getInstance().removeAllCookie();

		stopIdleTimer();
	}

	public void loadSavedUser() {
		String userId = Utils.getSavedUserId(this);
		if (userId != null) {
			String username = Utils.getSavedUserName(this);

			String encryptedPassword = Utils.getSavedUserPassword(this);

			String password = null;
			if (encryptedPassword != null) {
				try {
					password = Encrypt.decrypt(Utils.USER_MASTER,
							encryptedPassword);

				} catch (Exception e) {
					Log.e("EllucianApplication.loadSavedUser",
							"Decrypting on password failed, user not created.");
				}
			}
			String roles = Utils.getSavedUserRoles(this);
			List<String> roleList = new ArrayList<String>();
			if (roles != null) {
				roleList = Arrays.asList(roles.split(","));
			}
			createAppUser(userId, username, password, roleList);
		} else {
			Log.d("EllucianApplication.loadSavedUser", "No saved user to load");
			CookieManager.getInstance().removeAllCookie();
		}
	}

	public String getAppUserId() {
		if (user == null) {
			return null;
		}
		return user.getId();
	}

	public String getAppUserName() {
		if (user == null) {
			return null;
		}
		return user.getName();
	}

	public String getAppUserPassword() {
		if (user == null) {
			return null;
		}
		return user.getPassword();
	}

	public List<String> getAppUserRoles() {
		if (user == null) {
			return null;
		}
		return user.getRoles();
	}

	public boolean isUserAuthenticated() {
		if (user != null) {
			return true;
		} else {
			return false;
		}
	}

	public void startIdleTimer() {
		idleTimer = new IdleTimer(this, idleTime);
		idleTimer.start();
	}

	public void stopIdleTimer() {
		idleTimer.stopTimer();
	}

	public void touch() {
		idleTimer.touch();

	}

	public void startNotifications() {
		if (Utils.isNotificationsPresent(this)) {
			Log.d(TAG, "Starting Notifications");
			resetLastNotificationsCheck();
			Intent intent = new Intent(this, NotificationsIntentService.class);
			intent.putExtra(Extra.REQUEST_URL, getNotificationsUrl());
			startService(intent);
		}
	}

	synchronized public EllucianNotificationManager getNotificationManager() {
		return ellucianNotificationManager;
	}

	public void registerWithGcmIfNeeded() {
		ellucianNotificationManager.registerWithGcmIfNeeded();
	}

	public DeviceNotifications getDeviceNotifications() {
		return deviceNotifications;
	}

	public long getLastNotificationsCheck() {
		return lastNotificationsCheck;
	}

	public void resetLastNotificationsCheck() {
		this.lastNotificationsCheck = System.currentTimeMillis();
	}

	public String getNotificationsUrl() {
		return Utils.getStringFromPreferences(this, Utils.NOTIFICATION,
				Utils.NOTIFICATION_NOTIFICATIONS_URL, null);
	}

	public String getMobileNotificationsUrl() {
		return Utils.getStringFromPreferences(this, Utils.NOTIFICATION,
				Utils.NOTIFICATION_MOBILE_NOTIFICATIONS_URL, null);
	}

	public long getLastAuthRefresh() {
		return lastAuthRefresh;
	}

	public ConfigurationProperties getConfigurationProperties() {
		return configurationProperties;
	}

	public void setModuleConfigMap(
			HashMap<String, ModuleConfiguration> moduleConfigMap) {
		this.moduleConfigMap = moduleConfigMap;
	}

	public HashMap<String, ModuleConfiguration> getModuleConfigMap() {
		return moduleConfigMap;
	}

	public ModuleConfiguration findModuleConfig(String configName) {
		return moduleConfigMap.get(configName);
	}

	public List<String> getModuleConfigTypeList() {
		return new ArrayList<String>(moduleConfigMap.keySet());
	}

	public boolean isServiceRunning(Class<? extends Service> clazz) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (clazz.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public synchronized Tracker getTracker1() {
		if (gaTracker1 == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			String trackerId1 = Utils.getStringFromPreferences(this,
					Utils.GOOGLE_ANALYTICS, Utils.GOOGLE_ANALYTICS_TRACKER1,
					null);
			if (trackerId1 != null)
				gaTracker1 = analytics.newTracker(trackerId1);
		}
		return gaTracker1;
	}

	public synchronized Tracker getTracker2() {
		if (gaTracker2 == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			String trackerId2 = Utils.getStringFromPreferences(this,
					Utils.GOOGLE_ANALYTICS, Utils.GOOGLE_ANALYTICS_TRACKER2,
					null);
			if (trackerId2 != null)
				gaTracker2 = analytics.newTracker(trackerId2);
		}
		return gaTracker2;
	}

	/**
	 * Send event to google analytics
	 * 
	 * @param category
	 * @param action
	 * @param label
	 * @param value
	 * @param moduleName
	 */
	public void sendEvent(String category, String action, String label,
			Long value, String moduleName) {
		sendEventToTracker1(category, action, label, value,
				moduleName);
		sendEventToTracker2(category, action, label, value,
				moduleName);
	}

	/**
	 * Send event to google analytics for just tracker 1
	 * 
	 */
	public void sendEventToTracker1(String category, String action,
			String label, Long value, String moduleName) {
		sendEventToTracker(getTracker1(), category, action, label, value,
				moduleName);
	}

	/**
	 * Send event to google analytics for just tracker 2
	 * 
	 */
	public void sendEventToTracker2(String category, String action,
			String label, Long value, String moduleName) {
		sendEventToTracker(getTracker2(), category, action, label, value,
				moduleName);
	}

	/**
	 * Send event to google analytics
	 * 
	 * @param category
	 * @param action
	 * @param label
	 * @param value
	 * @param moduleName
	 */
	public void sendEventToTracker(Tracker tracker, String categoryId,
			String actionId, String labelId, Long value, String moduleName) {
		if (tracker != null) {
			String configurationName = Utils.getStringFromPreferences(this,
					Utils.CONFIGURATION, Utils.CONFIGURATION_NAME, null);
			EventBuilder eventBuilder = new HitBuilders.EventBuilder();
			eventBuilder.setCategory(categoryId);
			eventBuilder.setAction(actionId);
			eventBuilder.setLabel(labelId);
			if(value != null) {
				eventBuilder.setValue(value);
			}
			eventBuilder.setCustomDimension(1, configurationName);
			if (moduleName != null)
				eventBuilder.setCustomDimension(2, moduleName);

			tracker.send(eventBuilder.build());
		}

	}

	/**
	 * Send view to google analytics
	 * 
	 * @param appScreen
	 * @param moduleName
	 */
	public void sendView(String appScreen, String moduleName) {
		sendViewToTracker1(appScreen, moduleName);
		sendViewToTracker2(appScreen, moduleName);
	}

	/**
	 * Send view to google analytics for just tracker 1
	 * 
	 * @param appScreen
	 */
	public void sendViewToTracker1(String appScreen, String moduleName) {
		sendViewToTracker(getTracker1(), appScreen, moduleName);
	}

	/**
	 * Send view to google analytics for just tracker 2
	 * 
	 * @param appScreen
	 */
	public void sendViewToTracker2(String appScreen, String moduleName) {
		sendViewToTracker(getTracker2(), appScreen, moduleName);
	}

	/**
	 * Send view to google analytics
	 * 
	 * @param tracker
	 * @param appScreen
	 * @param moduleName
	 */
	public void sendViewToTracker(Tracker tracker, String appScreen,
			String moduleName) {
		if (tracker != null) {
			String configurationName = Utils.getStringFromPreferences(this,
					Utils.CONFIGURATION, Utils.CONFIGURATION_NAME, null);
			tracker.setScreenName(appScreen);
			AppViewBuilder appViewBuilder = new HitBuilders.AppViewBuilder();
			appViewBuilder.setCustomDimension(1, configurationName);
			if (moduleName != null)
				appViewBuilder.setCustomDimension(2, moduleName);
			tracker.send(appViewBuilder.build());
		}
	}
	
	/**
	 * Application will only manage one menu adapter at a time.
	 * Typically ModuleMenuAdapter.buildInstance() will only be called once on app creation
	 * and when a new configuration is requested.
	 */
	public ModuleMenuAdapter getModuleMenuAdapter() {
		if (moduleMenuAdapter == null) {
			moduleMenuAdapter = ModuleMenuAdapter.buildInstance(this);
		}		
    	return moduleMenuAdapter;
    }
	
	public void resetModuleMenuAdapter() {
		moduleMenuAdapter = null;
	}
	

}
