package com.ellucian.mobile.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

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
import com.ellucian.mobile.android.util.Utils;
import com.ellucian.mobile.android.util.XmlParser;

public class EllucianApplication extends Application {
	private HashMap<String, Object> liveObjects = new HashMap<String, Object>();
	private User user;
	private IdleTimer idleTimer;
	private long idleTime = 30 * 60 * 1000; // 30 Minutes
	private boolean notificationsPresent;
	private String notificationsUrl;
	private DeviceNotifications deviceNotifications;
	private long lastNotificationsCheck;
	public static final long DEFAULT_NOTIFICATIONS_REFRESH = 60 * 60 * 1000; // 60 minutes
	private long lastAuthRefresh;
	private ConfigurationProperties configurationProperties;
	private HashMap<String, ModuleConfiguration> moduleConfigMap;
	private EllucianNotificationManager ellucianNotificationManager;
	
	@Override
	public void onCreate() {
		super.onCreate();
	    idleTimer = new IdleTimer(this, idleTime);
	    deviceNotifications = new DeviceNotifications(this);
	    lastAuthRefresh = 0;
	    
	    // Setting up cookie management for WebView
	    CookieSyncManager.createInstance(this);
	    CookieManager.getInstance().setAcceptCookie(true);;

	    loadSavedUser();
	    
	    // Creating objects with configuration information
	    configurationProperties = XmlParser.createConfigurationPropertiesFromXml(this);
	    moduleConfigMap = XmlParser.createModuleConfigMapFromXml(this, 0);
	    
        ellucianNotificationManager = new EllucianNotificationManager(this);
	}
	
	public Object getCachedObject(String key) {
		return liveObjects.get(key);
	}
	
	public void putCachedObject(String key, Object value) {
		liveObjects.put(key, value);
	}
	
	public void createAppUser(String userId, String username, String password, List<String> roles) {
		user = new User();
		user.setId(userId);
		user.setName(username);
		user.setPassword(password);
		user.setRoles(roles);
		
		lastAuthRefresh = System.currentTimeMillis();
		
		String logString = "App User created:\n" + "userId: " + userId;
		logString += "\n" + "username:" + username;
		logString += "\n" + "roles:" + roles.toString();
		Log.d("EllucianApplication.createAppUser", logString);
	}
	
	public void removeAppUser() {
		user = null;
		getContentResolver().delete(EllucianContract.SECURED_CONTENT_URI,
				null, null);
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
			try {
				password = Encrypt.decrypt(Utils.USER_MASTER, encryptedPassword);
				
				String roles = Utils.getSavedUserRoles(this);
				List<String> roleList = null;
				if (roles != null) {
					roleList = Arrays.asList(roles.split(","));
				}
				
				createAppUser(userId, username, password, roleList);
				
			} catch (Exception e) {
				Log.d("EllucianApplication.loadSavedUser", "Decrypting on password failed, user not created.");
				e.printStackTrace();
			}
			
		} else {
			Log.d("EllucianApplication.loadSavedUser", "No saved user to load"); 
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
		if (notificationsPresent) {
			resetLastNotificationsCheck();
			Intent intent = new Intent(this, NotificationsIntentService.class);
			intent.putExtra(Extra.REQUEST_URL, notificationsUrl);
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
	
	public void setNotificationsPresent(boolean value) {
		notificationsPresent = value;
	}
	
	public boolean isNotificationsPresent() {
		return notificationsPresent;
	}
	
	public void setNotificationsUrl(String requestUrl) {
		notificationsUrl = requestUrl;
	}
	
	public long getLastAuthRefresh() {
		return lastAuthRefresh;
	}
	
	public ConfigurationProperties getConfigurationProperties() {
		return configurationProperties;
	}
	
	public void setModuleConfigMap(HashMap<String, ModuleConfiguration> moduleConfigMap) {
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
} 
