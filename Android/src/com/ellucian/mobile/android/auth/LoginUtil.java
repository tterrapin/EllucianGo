package com.ellucian.mobile.android.auth;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.DataCache;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.configuration.AbstractModule;
import com.ellucian.mobile.android.configuration.Configuration;
import com.ellucian.mobile.android.configuration.Grades;
import com.ellucian.mobile.android.configuration.Notifications;

public class LoginUtil {

	public static final String AUTH = "auth";
	public static final String PASSWORD = "password";
	public static final String USERNAME = "username";
	public static final String ROLE = "role";

	public static String getPassword(Context context) {
		final SharedPreferences preferences = context.getSharedPreferences(
				AUTH, Context.MODE_PRIVATE);
		final String password = preferences.getString(PASSWORD, null);
		if (password != null) {
			return new String(Base64.decode(password, Base64.DEFAULT));
		}
		return null;
	}

	public static String getUsername(Context context) {
		final SharedPreferences preferences = context.getSharedPreferences(
				AUTH, Context.MODE_PRIVATE);
		return preferences.getString(USERNAME, null);
	}
	
	public static String[] getRoles(Context context) {
		final SharedPreferences preferences = context.getSharedPreferences(
				AUTH, Context.MODE_PRIVATE);
		String roles = preferences.getString(ROLE, null);
		if(roles != null) {
			return roles.split(",");
		} else {
			return null;
		}
	}


	public static boolean isLoggedIn(Context application) {
		final SharedPreferences preferences = application.getSharedPreferences(
				AUTH, Context.MODE_PRIVATE);

		return preferences.contains(USERNAME) && preferences.contains(PASSWORD);
	}

	public static void logout(Application application) {
		DataCache dataCache = ((EllucianApplication) application).getDataCache();
		if(dataCache != null) {
			dataCache	.clearAuthenticatedCache();
		}
		final SharedPreferences preferences = application.getSharedPreferences(
				AUTH, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = preferences.edit();
		editor.remove(PASSWORD);
		editor.commit();
		
		final Configuration configuration =  ((EllucianApplication) application)
				.getConfiguration();
		if(configuration != null) {
			// ((EllucianApplication) application).clearAlarms();
			for(AbstractModule m : configuration.getModules()) {
				if(m instanceof Notifications) {
					String url = ((Notifications)m).getUrl();
					NotificationManager mNotificationManager = (NotificationManager) application.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
					mNotificationManager.cancel(R.string.notifications + url.hashCode());
				} else	if(m instanceof Grades) {
					String url = ((Grades)m).getUrl();
					NotificationManager mNotificationManager = (NotificationManager) application.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
					mNotificationManager.cancel(R.string.grades + url.hashCode());
				}
			}
		}

	}

	public static void storeCredentials(Context context, String username,
			String password, String[] roles) {
		final SharedPreferences preferences = context.getSharedPreferences(
				AUTH, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putString(USERNAME, username);
		editor.putString(PASSWORD,
				Base64.encodeToString(password.getBytes(), Base64.DEFAULT));
		editor.putString(ROLE, android.text.TextUtils.join(",", roles));
		editor.commit();
		
		((EllucianApplication)context.getApplicationContext()).startBackgroundUpdateServices(true);
	}

	public static void clearUsername(Application application) {
		final SharedPreferences preferences = application.getSharedPreferences(
				AUTH, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
		
		
	}

	public static boolean allowLogin(Application application) {
		final Configuration configuration =  ((EllucianApplication) application)
				.getConfiguration();
		return configuration.getAuthenticationUrl() != null;
	}
}
