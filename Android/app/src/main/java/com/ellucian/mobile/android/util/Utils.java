/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.util;

import java.util.Arrays;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;

import com.androidquery.AQuery;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.ModuleType;

public class Utils {
	public static final String DEFAULT_MENU_ICON = "defaultMenuIcon";
	public static final String MENU_ICON_URL = "menuIconUrl";
	public static final String SUBHEADER_TEXT_COLOR = "subheaderTextColor";
	public static final String ACCENT_COLOR = "accentColor";
	public static final String HEADER_TEXT_COLOR = "headerTextColor";
	public static final String PRIMARY_COLOR = "primaryColor";
	public static final String HOME_URL_PHONE = "homeUrlPhone";
	public static final String HOME_URL_TABLET = "homeUrlTablet";
	public static final String SCHOOL_LOGO_PHONE= "schoolLogoPhone";
	public static final String SCHOOL_LOGO_TABLET = "schoolLogoTablet";
	public static final String SECURITY = "security";
	public static final String SECURITY_URL = "securityUrl";
	public static final String NOTIFICATION = "notification";
	public static final String NOTIFICATION_PRESENT = "notificationPresent";
	public static final String NOTIFICATION_NOTIFICATIONS_URL = "notificationNotificationsUrl";
	public static final String NOTIFICATION_MOBILE_NOTIFICATIONS_URL = "notificationMobileNotificationsUrl";
	public static final String NOTIFICATION_REGISTRATION_URL = "notificationRegistrationUrl";
	public static final String NOTIFICATION_DELIVERED_URL = "notificationDeliveredUrl";
	public static final String NOTIFICATION_ENABLED = "notificationEnabled";
	public static final String CONFIGURATION = "configuration";
	public static final String CONFIGURATION_NAME = "configurationName";
	public static final String CONFIGURATION_URL = "configurationUrl";
	public static final String CONFIGURATION_LAST_UPDATE = "configurationLastUpdate";
	public static final String APPEARANCE = "appearance";
	public static final String DIALOG = "dialog";
	public static final String USER = "user";
	public static final String USER_ID = "userId";
	public static final String USER_NAME = "userName";
	public static final String USER_PASSWORD = "userPassword";
	public static final String USER_ROLES = "userRoles";
	public static final String USER_MASTER = "userMaster";
	public static final String ID = "id";
	public static final String COURSE_ROSTER_VISIBILITY = "course_roster_visibility";
	public static final String MAP_BUILDINGS_URL = "maBuildingsUrl";
	public static final String MAP_CAMPUSES_URL = "mapCampusesUrl";
	public static final String MAP_PRESENT = "mapPresent";
	public static final String DIRECTORY_PRESENT = "directoryPresent";
	public static final String DIRECTORY_ALL_SEARCH_URL = "directoyAllSearchUrl";
	public static final String DIRECTORY_STUDENT_SEARCH_URL = "directoyStudentSearchUrl";	
	public static final String DIRECTORY_FACULTY_SEARCH_URL = "directoyFacultySearchUrl";
	public static final String GOOGLE_ANALYTICS = "googleAnalytics";
	public static final String GOOGLE_ANALYTICS_TRACKER1 = "tracker1";
	public static final String GOOGLE_ANALYTICS_TRACKER2 = "tracker2";
	public static final String LOGIN_URL = "loginUrl";
	public static final String LOGIN_TYPE = "loginType";
	public static final String BROWSER_LOGIN_TYPE = "browser";
	public static final String NATIVE_LOGIN_TYPE = "native";
	public static final String MENU = "menu";
	public static final String MENU_HEADER_STATE = "menuHeaderState";
    public static final String ILP_URL = "ilpUrl";


	public static boolean isIntentAvailable(Context context, Intent intent) {
		if(intent == null) return false;
	    final PackageManager packageManager = context.getPackageManager();
	    List<ResolveInfo> resolveInfo =
	            packageManager.queryIntentActivities(intent,
	                    PackageManager.MATCH_DEFAULT_ONLY);
	   if (resolveInfo.size() > 0) {
	   		return true;
	   	}
	   return false;
	}
	
	public static int getPrimaryColor(Context context) {
		return getColor(context, PRIMARY_COLOR);
	}
	
	public static int getHeaderTextColor(Context context) {
		return getColor(context, HEADER_TEXT_COLOR);
	}
	public static int getAccentColor(Context context) {
		return getColor(context, ACCENT_COLOR);
	}
	public static int getSubheaderTextColor(Context context) {
		return getColor(context, SUBHEADER_TEXT_COLOR);
	}
	
	public static boolean hasDefaultMenuIcon(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(APPEARANCE, Context.MODE_PRIVATE);
		return preferences.getBoolean(DEFAULT_MENU_ICON, false);
	}
	
	public static Drawable getMenuIcon(Context context) {
		String menuIconUrl = Utils.getStringFromPreferences(context, Utils.APPEARANCE, Utils.MENU_ICON_URL, null);
		if (!TextUtils.isEmpty(menuIconUrl)) {
			AQuery aq = new AQuery(context);
			Bitmap bit = aq.getCachedImage(menuIconUrl);
			if (bit != null) {
				Drawable draw = new BitmapDrawable(context.getResources(), bit);
				return draw;
			}
		}
		return null;
	}

	private static int getColor(Context context, String key) {
		SharedPreferences preferences = context.getSharedPreferences(APPEARANCE, Context.MODE_PRIVATE);
		String color = preferences.getString(key, "#000000");
		try {
			int colorValue = Color.parseColor(color);
			return colorValue;
		} catch (IllegalArgumentException e) {
			return Color.TRANSPARENT;
		}
	}
		
	public static String getStringFromPreferences(Context context, String fileName, String key, String defaultString) {		
		SharedPreferences preferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return preferences.getString(key, defaultString);
	}
	
	public static void addStringToPreferences(Context context, String fileName, String key, String value) {
		SharedPreferences preferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        
        editor.putString(key, value);
        editor.commit();
	}
	
	public static Long getLongFromPreferences(Context context, String fileName, String key, Long defaultString) {		
		SharedPreferences preferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return preferences.getLong(key, defaultString);
	}
	
	public static void addLongToPreferences(Context context, String fileName, String key, Long value) {
		SharedPreferences preferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        
        editor.putLong(key, value);
        editor.commit();
	}
	
	public static boolean getBooleanFromPreferences(Context context, String fileName, String key, boolean defaultValue) {		
		SharedPreferences preferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return preferences.getBoolean(key, defaultValue);
	}
	
	public static void addBooleanToPreferences(Context context, String fileName, String key, boolean value) {
		SharedPreferences preferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        
        editor.putBoolean(key, value);
        editor.commit();
	}
	
	public static void removeValuesFromPreferences(Context context, String fileName, String... keys) {
		SharedPreferences preferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        
        for (String key: keys) {
        	editor.remove(key);
        }
        editor.commit();
	}

	
	public static void saveUserInfo(Context context, String userId, String username, String password, List<String> roleList) {
		
		StringBuilder roleBuilder = new StringBuilder();
		int rolesLength = roleList.size();
		String role;
		for (int i = 0; i < rolesLength; i++) {
			if (i > 0) {
				roleBuilder.append(",");
			}
			role = (String) roleList.get(i);
			roleBuilder.append(role);
			
		}
		
		SharedPreferences preferences = context.getSharedPreferences(Utils.USER, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		
		if (!TextUtils.isEmpty(userId)) {
			editor.putString(Utils.USER_ID, userId);
			Log.d("MainActivity.saveUserInfo", "User saved with id: " + userId);
		}
		if (!TextUtils.isEmpty(username)) {
			editor.putString(Utils.USER_NAME, username);
			Log.d("MainActivity.saveUserInfo", "User saved with username: " + username);
		}
		if (!TextUtils.isEmpty(password)) {
			//Encrypt password before saving
			String encryptedPassword = null;
			try {
				encryptedPassword = Encrypt.encrypt(Utils.USER_MASTER, password);
			} catch (Exception e) {
				Log.d("Utils.saveUserInfo", "Encryption Failed");
				e.printStackTrace();
			}
			
			editor.putString(Utils.USER_PASSWORD, encryptedPassword);
		}
		String rolesString = roleBuilder.toString();
		if (!TextUtils.isEmpty(rolesString)) {
			editor.putString(Utils.USER_ROLES, rolesString);
			Log.d("Utils.saveUserInfo", "User saved with roles: " + rolesString);
		}
		
		editor.commit();
				
	}

	public static String getSavedUserId(Context context) {
		return getStringFromPreferences(context, Utils.USER, Utils.USER_ID, null);
	}
	
	public static String getSavedUserName(Context context) {
		return getStringFromPreferences(context, Utils.USER, Utils.USER_NAME, null);
	}
	
	public static String getSavedUserPassword(Context context) {
		return getStringFromPreferences(context, Utils.USER, Utils.USER_PASSWORD, null);
	}
	
	public static String getSavedUserRoles(Context context) {
		return getStringFromPreferences(context, Utils.USER, Utils.USER_ROLES, null);
	}
	
	public static void removeSavedUser(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(Utils.USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        
        editor.remove(Utils.USER_ID);
        editor.remove(Utils.USER_NAME);
        editor.remove(Utils.USER_PASSWORD);
        editor.remove(Utils.USER_ROLES);
        editor.commit();
	}
	
	public static String getUserId(Application application) {
		if(application instanceof EllucianApplication) {
			EllucianApplication ea = (EllucianApplication)application;
			return ea.getAppUserId();
		}
		return null;
	}
	
	public static String getUserName(Application application) {
		if(application instanceof EllucianApplication) {
			EllucianApplication ea = (EllucianApplication)application;
			return ea.getAppUserName();
		}
		return null;
	}
	
	public static String getUserPassword(Application application) {
		if(application instanceof EllucianApplication) {
			EllucianApplication ea = (EllucianApplication)application;
			return ea.getAppUserPassword();
		}
		return null;
	}
	
	public static boolean isMapPresent(Context context) {
        return getBooleanFromPreferences(context, CONFIGURATION, MAP_PRESENT, false);
	}
	 
	public static boolean isDirectoryPresent(Context context) {
		return getBooleanFromPreferences(context, CONFIGURATION, DIRECTORY_PRESENT, false);
	}
	
	public static boolean isNotificationsPresent(Context context) {
		return getBooleanFromPreferences(context, CONFIGURATION, NOTIFICATION_PRESENT, false);
	}
	
	public static boolean isGoogleMapsInstalled(Context context) {
	    try {
	        @SuppressWarnings("unused")
			ApplicationInfo info = context.getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0 );
	        return true;
	    } catch(PackageManager.NameNotFoundException e) {
	        return false;
	    }
	}
	
	public static boolean isPhoneIntentAvailable (Context context) {
		Uri uri = Uri.parse("tel:222-333-4444");
    	Intent intent = new Intent(Intent.ACTION_DIAL, uri);
		return isIntentAvailable(context, intent);
	}
	
	public static boolean isEmailIntentAvailable (Context context) {
		Uri uri = Uri.parse("mailto:test@test.com");
    	Intent intent = new Intent(Intent.ACTION_SENDTO);
    	intent.setData(uri);
		return isIntentAvailable(context, intent);
	}
	
	public static boolean isWebIntentAvailable(Context context) {
		Intent intent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://www.google.com"));
		return Utils.isIntentAvailable(context, intent);
	}

	public static int getAvailableLinkMasks(Context context, Integer... linkTypes) {
		int mask = 0;
		
		List<Integer> typeList = null;
		if (linkTypes != null) {
			typeList = Arrays.asList(linkTypes);
		
			if (typeList.contains(Linkify.ALL) || typeList.contains(Linkify.MAP_ADDRESSES)) {
				if (isGoogleMapsInstalled(context)) {
					mask = mask | Linkify.MAP_ADDRESSES;
				}
			}
			if (typeList.contains(Linkify.ALL) || typeList.contains(Linkify.EMAIL_ADDRESSES)) {
				if (isEmailIntentAvailable(context)) {
					mask = mask | Linkify.EMAIL_ADDRESSES;
				}
			}
			if (typeList.contains(Linkify.ALL) || typeList.contains(Linkify.PHONE_NUMBERS)) {
				if (isPhoneIntentAvailable(context)) {
					mask = mask | Linkify.PHONE_NUMBERS;
				}
			}
			if (typeList.contains(Linkify.ALL) || typeList.contains(Linkify.WEB_URLS)) {
				if (isWebIntentAvailable(context)) {
					mask = mask | Linkify.WEB_URLS;
				}
			}
		}
		
		return mask;
	}
	
	public static void sendMarketIntent(Activity activity, boolean setFlags) {
		String packageName = activity.getApplicationContext().getPackageName();
		Intent marketIntent = new Intent(Intent.ACTION_VIEW);
		marketIntent.setData(Uri.parse("market://details?id="
						+ packageName));

		if (Utils.isIntentAvailable(activity, marketIntent)) {
			if (setFlags) {
				marketIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				marketIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			}
			activity.startActivity(marketIntent);
			activity.finish();
		} else {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("http://play.google.com/store/apps/details?id="
							+ packageName));
			if (setFlags) {
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			}
			activity.startActivity(intent);
		}
		
	}
	
	public static boolean isAuthenticationNeededForType(String type) {
		List<String> authTypeList = Arrays
				.asList(ModuleType.AUTHENTICATION_NEEDED);
		return authTypeList.contains(type);
	}
	
	public static boolean isAuthenticationNeededForSubType(Context context, String subType) {
		EllucianApplication ellucianApp = (EllucianApplication) context.getApplicationContext();
		ModuleConfiguration moduleConfig = ellucianApp.findModuleConfig(subType);
		
		return moduleConfig != null ? moduleConfig.secure : false;
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static boolean isSystemLayoutDirectionRtl() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return TextUtils.getLayoutDirectionFromLocale(null)
                    == View.LAYOUT_DIRECTION_RTL;
        }
        return false;
    }
}
