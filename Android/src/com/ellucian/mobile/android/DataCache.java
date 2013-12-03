package com.ellucian.mobile.android;

import java.util.Calendar;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * This "cache" implementation manages storing, retrieving, and clearing textual
 * responses from a web service for activities to use.
 * 
 * Often the stale data will be shown while new data is fetched. The stale data
 * can be shown when there are problems retrieving data from the web service.
 * Also, the stale information can be shown if recently downloaded to cut down
 * on network traffic.
 * 
 * @author Jason Hocker
 * 
 */
public class DataCache {
	
	private static final String NOTIFICATION_CACHE = "notificationCache";
	private static final String AUTHENTICATED_CACHE = "authCache";
	private static final String AUTHENTICATED_CACHE_DATE = "authDateCache";

	private static final String CACHE = "cache";
	private static final String CACHE_DATE = "cacheDate";

	private final SharedPreferences authDatePreferences;

	private final SharedPreferences authPreferences;

	private final SharedPreferences notificationPreferences;

	private final SharedPreferences datePreferences;

	private final long longExpirationTime;
	private final SharedPreferences preferences;
	private final long shortExpirationTime;
	private HashMap<String, Object> liveObjects = new HashMap<String, Object>();

	public DataCache(Context context, long shortExpirationTime,
			long longExpirationTime) {
		preferences = context.getSharedPreferences(CACHE, Context.MODE_PRIVATE);
		authPreferences = context.getSharedPreferences(AUTHENTICATED_CACHE,
				Context.MODE_PRIVATE);
		datePreferences = context.getSharedPreferences(CACHE_DATE,
				Context.MODE_PRIVATE);
		authDatePreferences = context.getSharedPreferences(
				AUTHENTICATED_CACHE_DATE, Context.MODE_PRIVATE);
		notificationPreferences = context.getSharedPreferences(NOTIFICATION_CACHE,
				Context.MODE_PRIVATE);
		this.shortExpirationTime = shortExpirationTime;
		this.longExpirationTime = longExpirationTime;
	}

	public void clearAuthenticatedCache() {
		Editor editor = authPreferences.edit();
		editor.clear();
		editor.commit();
		editor = authDatePreferences.edit();
		editor.clear();
		editor.commit();
		editor = notificationPreferences.edit();
		editor.clear();
		editor.commit();

	}

	public void clearCache() {
		clearAuthenticatedCache();

		Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
		editor = datePreferences.edit();
		editor.clear();
		editor.commit();
	}

	public Object getCacheObject(String url) {
		return liveObjects.get(url);
	}
	
	public String getCache(Context context, String url) {

		if (preferences.contains(url)) {
			final String content = preferences.getString(url, null);
			return content;
		} else if (authPreferences.contains(url)) {
			final String content = authPreferences.getString(url, null);
			return content;
		} else {
			return null;
		}
	}

	public boolean isCurrent(Context context, String url) {
		return isCurrent(context, url, shortExpirationTime);
	}

	public boolean isCurrentLongInterval(Context context, String url) {
		return isCurrent(context, url, longExpirationTime);
	}

	private boolean isCurrent(Context context, String url, long interval) {
		final Calendar calendar = Calendar.getInstance();
		if (preferences.contains(url)) {
			final long time = datePreferences.getLong(url, 0);
			if (time < calendar.getTimeInMillis() - interval) {
				return false;
			}
			final long backgroundTime = datePreferences.getLong("background-" + url, time);
			if(backgroundTime > time) {
				return false;
			}
			return true;
		} else if (authPreferences.contains(url)) {
			final long time = authDatePreferences.getLong(url, 0);
			if (time < calendar.getTimeInMillis() - interval) {
				return false;
			}
			final long backgroundTime = authDatePreferences.getLong("background-" + url, time);
			if(backgroundTime > time) {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}


	public void putAuthCache(String url, String content, Object objects) {
		if(objects != null) liveObjects .put(url, objects);
		Editor editor = authPreferences.edit();
		editor.putString(url, content);
		editor.commit();
		editor = authDatePreferences.edit();
		editor.putLong(url, Calendar.getInstance().getTimeInMillis());
		editor.commit();

	}

	public void putCache(String url, String content, Object objects) {
		if(objects != null) liveObjects .put(url, objects);
		Editor editor = preferences.edit();
		editor.putString(url, content);
		editor.commit();
		editor = datePreferences.edit();
		editor.putLong(url, Calendar.getInstance().getTimeInMillis());
		editor.commit();
	}

	public void putCacheObject(String url, Object objects) {
		liveObjects .put(url, objects);
	}
	
	public boolean hasNotifications(Context context, String url) {

		if (notificationPreferences.contains(url)) {
			return notificationPreferences.getInt(url, 0) > 0;
		} 
		return false;
	}
	
	
	public void recordNotificationCount(String url, int count) {
		Editor editor = notificationPreferences.edit();
		editor.putInt(url, count);
		editor.commit();
	}

}
