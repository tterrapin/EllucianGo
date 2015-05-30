/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.services;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.ModuleType;
import com.ellucian.mobile.android.about.AboutActivity;
import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.client.configuration.ConfigurationBuilder;
import com.ellucian.mobile.android.provider.EllucianContract;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

public class ConfigurationUpdateService extends IntentService {

	private boolean checkVersion = true;
	public static String latestVersionToCauseAlert = null;

	public static final String PARAM_UPGRADE_AVAILABLE = "upgradeAvailable";
	public static final String REFRESH = "refresh";
	public static final String ACTION_SUCCESS = "com.ellucian.mobile.android.client.services.ConfigurationUpdateService.action.success";
	public static final String ACTION_SEND_TO_SELECTION = "com.ellucian.mobile.android.client.services.ConfigurationUpdateService.action.reselect";
	public static final String ACTION_OUTDATED = "com.ellucian.mobile.android.client.services.ConfigurationUpdateService.action.outdated";
	public static final String ACTION_UNABLE_TO_DOWNLOAD = "com.ellucian.mobile.android.client.services.ConfigurationUpdateService.action.unableToDownload";
	private static final String TAG = ConfigurationUpdateService.class
			.getSimpleName();
	private boolean imagesDone;
	private ImageLoaderReceiver imageReceiver;
	private boolean refresh;

	public ConfigurationUpdateService() {
		super("ConfigurationUpdateService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		EllucianApplication ellucianApp = (EllucianApplication) getApplicationContext();
		// Setting fields from the configuration file. See res/xml/configuration_properties.xml
		checkVersion = ellucianApp.getConfigurationProperties().enableVersionChecking;
		
		LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);

		imageReceiver = new ImageLoaderReceiver();
		IntentFilter imageFilter = new IntentFilter(
				ImageLoaderService.ACTION_FINISHED);
		LocalBroadcastManager.getInstance(this).registerReceiver(imageReceiver,
				imageFilter);

		imagesDone = false;

		String configUrl = intent.getStringExtra(Utils.CONFIGURATION_URL);
		refresh = intent.getBooleanExtra(REFRESH, false);

		MobileClient client = new MobileClient(this);
		String configurationString = client.getConfiguration(configUrl);

		JSONObject jsonConfiguration = null;
		boolean success = false;
		boolean sendToSelection = false;
		boolean outdated = false;
		boolean upgradeAvailable = false;
		try {
			if (configurationString == null) {
				Log.e(TAG, "Configuration not downloaded: " + configUrl);
			} else if (configurationString.equals("401")
					|| configurationString.equals("403")
					|| configurationString.equals("404")) {
				Log.e(TAG, "Return server error: " + configurationString);
				sendToSelection = true;
			} else {
				jsonConfiguration = new JSONObject(configurationString);

				if (checkVersion
						&& jsonConfiguration.has("versions")
						&& jsonConfiguration.getJSONObject("versions").has(
								"android")) {
					ArrayList<String> supportedVersions = new ArrayList<String>();
					JSONArray jsonVersions = jsonConfiguration.getJSONObject(
							"versions").getJSONArray("android");
					for (int i = 0; i < jsonVersions.length(); i++) {
						supportedVersions.add(jsonVersions.getString(i));
					}

					try {
						outdated = true;
						String appVersion = getPackageManager().getPackageInfo(
								getPackageName(), 0).versionName;
						String[] appVersionComponents = appVersion.split("\\.");
						String appVersionWithoutBuildNumber = appVersionComponents[0]
								+ "."
								+ appVersionComponents[1]
								+ "."
								+ appVersionComponents[2];
						String latestSupportedVersion = supportedVersions
								.get(supportedVersions.size() - 1);
						String[] latestSupportedVersionComponents = latestSupportedVersion
								.split("\\.");

						if (latestSupportedVersion
								.equals(appVersionWithoutBuildNumber)) {
							// current
							outdated = false;
						} else if (supportedVersions
								.contains(appVersionWithoutBuildNumber)) {
							// supported
							// only alert the user once
							if (!latestSupportedVersion
									.equals(latestVersionToCauseAlert)) {
								upgradeAvailable = true;
								latestVersionToCauseAlert = latestSupportedVersion;
							}
							outdated = false;
						} else if (appVersionComponents.length > 0
								&& latestSupportedVersionComponents.length > 0
								&& Integer.parseInt(appVersionComponents[0]) > Integer
										.parseInt(latestSupportedVersionComponents[0])) {
							// app newer than what server returns
							outdated = false;
						} else if (appVersionComponents.length > 0
								&& latestSupportedVersionComponents.length > 0
								&& Integer.parseInt(appVersionComponents[0]) == Integer
										.parseInt(latestSupportedVersionComponents[0])) {
							if (appVersionComponents.length > 1
									&& latestSupportedVersionComponents.length > 1
									&& Integer
											.parseInt(appVersionComponents[1]) > Integer
											.parseInt(latestSupportedVersionComponents[1])) {
								// app newer than what server returns
								outdated = false;
							} else if (appVersionComponents.length > 1
									&& latestSupportedVersionComponents.length > 1
									&& Integer
											.parseInt(appVersionComponents[1]) == Integer
											.parseInt(latestSupportedVersionComponents[1])) {
								if (appVersionComponents.length > 2
										&& latestSupportedVersionComponents.length > 2
										&& Integer
												.parseInt(appVersionComponents[2]) > Integer
												.parseInt(latestSupportedVersionComponents[2])) {
									// app newer than what server returns
									outdated = false;
								}
							}
						}
						if (outdated) {
							success = false;
							sendToSelection = true;
						}
					} catch (NameNotFoundException e) {
						Log.e("ConfigurationUpdateService",
								"Unable to get versionName");
					}
				}
				if (!outdated) {
					addConfigurationItemsToPreferences(jsonConfiguration);

					// Starting home screen images download early
					ArrayList<String> homeImagesUrlList = collectHomeImageUrls(jsonConfiguration);
					Intent homeImagesIntent = new Intent(getBaseContext(),
							ImageLoaderService.class);
					homeImagesIntent.putExtra(Extra.IMAGE_URL_LIST,
							homeImagesUrlList);
					homeImagesIntent.putExtra(Extra.SEND_BROADCAST, true);
					startService(homeImagesIntent);

					// Menu and other images
					ArrayList<String> otherImagesUrlList = collectOtherImageUrls(jsonConfiguration);
					Intent otherImagesIntent = new Intent(getBaseContext(),
							ImageLoaderService.class);
					otherImagesIntent.putExtra(Extra.IMAGE_URL_LIST,
							otherImagesUrlList);
					startService(otherImagesIntent);

					if (jsonConfiguration.has(ModuleType.MODULE)) {
						JSONObject jsonModules = jsonConfiguration
								.getJSONObject(ModuleType.MODULE);
						ConfigurationBuilder builder = new ConfigurationBuilder(
								this);
						ArrayList<ContentProviderOperation> ops = builder
								.buildOperations(jsonModules);

						if (ops.size() > 0) {
							this.getContentResolver().applyBatch(
									EllucianContract.CONTENT_AUTHORITY, ops);
						}

						// Pulling specific things from modules
						Iterator<?> moduleIds = jsonModules.keys();
						while (moduleIds.hasNext()) {
							String key = (String) moduleIds.next();
							JSONObject moduleObject = jsonModules
									.getJSONObject(key);
							String type = moduleObject.getString("type");

							// Set if Directory are present
							if (type.equals(ModuleType.DIRECTORY)) {
								Utils.addBooleanToPreferences(this,
										Utils.CONFIGURATION,
										Utils.DIRECTORY_PRESENT, true);
							}
							// Set if Maps are present
							if (type.equals(ModuleType.MAPS)) {
								Utils.addBooleanToPreferences(this,
										Utils.CONFIGURATION, Utils.MAP_PRESENT,
										true);
							}

							// Check to see if notifications are present
							if (type.equals(ModuleType.NOTIFICATIONS)) {								
								Utils.addBooleanToPreferences(this, Utils.CONFIGURATION,
								        Utils.NOTIFICATION_PRESENT, true);

								JSONObject urls = moduleObject.getJSONObject("urls");

								Utils.addStringToPreferences(this, Utils.NOTIFICATION,
								        Utils.NOTIFICATION_NOTIFICATIONS_URL, urls.getString("notifications"));
	
								Utils.addStringToPreferences(this, Utils.NOTIFICATION,
								        Utils.NOTIFICATION_MOBILE_NOTIFICATIONS_URL, urls.getString("mobilenotifications"));

							}

							// Set if the course roster is visible
							if (type.equals(ModuleType.COURSES)) {
								String visible = "none";
								if (moduleObject.has("visible")) {
									visible = moduleObject.getString("visible");
								}
								Utils.addStringToPreferences(this,
										Utils.CONFIGURATION,
										Utils.COURSE_ROSTER_VISIBILITY, visible);
							}

						}
					}
					// Clears the menu adapter so the app knows to recreate it with the new
					// configuration changes
					ellucianApp.resetModuleMenuAdapter();
					
					success = true;
				}
			}
		} catch (JSONException e) {
			Log.e(TAG, "JSONException:", e);
		} catch (NullPointerException e) {
			Log.e(TAG, "NullPointerException:", e);
		} catch (OperationApplicationException e) {
			Log.e(TAG, "OperationApplicationException:", e);
		} catch (RemoteException e) {
			Log.e(TAG, "RemoteException:", e);
		}

		// Make sure the home images are downloaded for display before sending
		// out broadcast to start MainActivity
		while (success && !imagesDone) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (success) {
			long updateTime = System.currentTimeMillis();
			Utils.addLongToPreferences(this, Utils.CONFIGURATION, Utils.CONFIGURATION_LAST_UPDATE,
					updateTime);
			Log.d(TAG, "Configuration update time: " + updateTime);
		}

		if (outdated) {
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(ACTION_OUTDATED);
			bm.sendBroadcast(broadcastIntent);
		} else if (success) {
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(ACTION_SUCCESS);
			broadcastIntent.putExtra(PARAM_UPGRADE_AVAILABLE, upgradeAvailable);
			broadcastIntent.putExtra(REFRESH, refresh);
			bm.sendBroadcast(broadcastIntent);
		} else if (sendToSelection) {
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(ACTION_SEND_TO_SELECTION);
			bm.sendBroadcast(broadcastIntent);
		} else {
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(ACTION_UNABLE_TO_DOWNLOAD);
			bm.sendBroadcast(broadcastIntent);
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				imageReceiver);
	}

	private ArrayList<String> collectHomeImageUrls(JSONObject jsonConfiguration) {
		ArrayList<String> imageUrlList = new ArrayList<String>();

		// Collecting home background image and school logo
		try {
			JSONObject layout = jsonConfiguration.getJSONObject("layout");
			if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= 
			        Configuration.SCREENLAYOUT_SIZE_LARGE && layout.has(Utils.HOME_URL_TABLET)
			        		&& !TextUtils.isEmpty(layout.getString(Utils.HOME_URL_TABLET))) {
				// Large screens use larger image if present			
				imageUrlList.add(layout.getString(Utils.HOME_URL_TABLET));
			} else {
				if (layout.has(Utils.HOME_URL_PHONE)
						&& !TextUtils.isEmpty(layout
								.getString(Utils.HOME_URL_PHONE))) {
					imageUrlList.add(layout.getString(Utils.HOME_URL_PHONE));
				}
			}
			
			if (layout.has(Utils.SCHOOL_LOGO_PHONE)
					&& !TextUtils.isEmpty(layout
							.getString(Utils.SCHOOL_LOGO_PHONE))) {
				imageUrlList.add(layout.getString(Utils.SCHOOL_LOGO_PHONE));
			}
			if (layout.has(Utils.SCHOOL_LOGO_TABLET)
					&& !TextUtils.isEmpty(layout
							.getString(Utils.SCHOOL_LOGO_TABLET))) {
				imageUrlList.add(layout.getString(Utils.SCHOOL_LOGO_TABLET));
			}
		} catch (JSONException e) {
			Log.e(TAG + ".collectImageUrls", "JSONException:", e);
		}

		String menuIconUrl = Utils.getStringFromPreferences(this,
				Utils.APPEARANCE, Utils.MENU_ICON_URL, null);
		if (!TextUtils.isEmpty(menuIconUrl)) {
			imageUrlList.add(menuIconUrl);
		}

		return imageUrlList;
	}

	private ArrayList<String> collectOtherImageUrls(JSONObject jsonConfiguration) {
		ArrayList<String> imageUrlList = new ArrayList<String>();

		// Collecting menu icon images
		try {
			if (jsonConfiguration.has(ModuleType.MODULE)) {
				JSONObject jsonModules = jsonConfiguration
						.getJSONObject(ModuleType.MODULE);
				Iterator<?> iter = jsonModules.keys();
				while (iter.hasNext()) {
					String key = (String) iter.next();
					JSONObject value = jsonModules.getJSONObject(key);

					if (value.has("icon")) {
						imageUrlList.add(value.getString("icon"));
					}
				}
			}
		} catch (JSONException e) {
			Log.e(TAG + ".collectImageUrls", "JSONException:", e);
		}

		String aboutIconUrl = Utils.getStringFromPreferences(this,
				Utils.APPEARANCE, AboutActivity.PREFERENCES_ICON, null);
		if (!TextUtils.isEmpty(aboutIconUrl)) {
			imageUrlList.add(aboutIconUrl);
		}

		return imageUrlList;
	}

	private void addConfigurationItemsToPreferences(JSONObject jsonConfiguration)
			throws JSONException {

		SharedPreferences preferences = this.getSharedPreferences(
				Utils.APPEARANCE, MODE_PRIVATE);
		preferences.edit().clear().commit();
		SharedPreferences.Editor editor = preferences.edit();

		/** Adding Layout Info **/
		JSONObject layout = jsonConfiguration.getJSONObject("layout");

		// Setting information from the configuration. Using defaults if they
		// are not present.
		if (layout.has(Utils.PRIMARY_COLOR)
				&& !TextUtils.isEmpty(layout.getString(Utils.PRIMARY_COLOR))) {
			editor.putString(Utils.PRIMARY_COLOR,
					"#" + layout.getString(Utils.PRIMARY_COLOR));
		} else {
			editor.putString(Utils.PRIMARY_COLOR, "#331640");
		}
		if (layout.has(Utils.HEADER_TEXT_COLOR)
				&& !TextUtils
						.isEmpty(layout.getString(Utils.HEADER_TEXT_COLOR))) {
			editor.putString(Utils.HEADER_TEXT_COLOR,
					"#" + layout.getString(Utils.HEADER_TEXT_COLOR));
		} else {
			editor.putString(Utils.HEADER_TEXT_COLOR, "#FFFFFF");
		}
		if (layout.has(Utils.ACCENT_COLOR)
				&& !TextUtils.isEmpty(layout.getString(Utils.ACCENT_COLOR))) {
			editor.putString(Utils.ACCENT_COLOR,
					"#" + layout.getString(Utils.ACCENT_COLOR));
		} else {
			editor.putString(Utils.ACCENT_COLOR, "#E8E1CD");
		}
		if (layout.has(Utils.SUBHEADER_TEXT_COLOR)
				&& !TextUtils.isEmpty(layout
						.getString(Utils.SUBHEADER_TEXT_COLOR))) {
			editor.putString(Utils.SUBHEADER_TEXT_COLOR,
					"#" + layout.getString(Utils.SUBHEADER_TEXT_COLOR));
		} else {
			editor.putString(Utils.SUBHEADER_TEXT_COLOR, "#736357");
		}
		if (layout.has(Utils.DEFAULT_MENU_ICON)
				&& !TextUtils
						.isEmpty(layout.getString(Utils.DEFAULT_MENU_ICON))) {
			editor.putBoolean(Utils.DEFAULT_MENU_ICON,
					layout.getBoolean(Utils.DEFAULT_MENU_ICON));
		} else {
			editor.putBoolean(Utils.DEFAULT_MENU_ICON, true);
		}
		if (layout.has(Utils.MENU_ICON_URL)
				&& !TextUtils.isEmpty(layout.getString(Utils.MENU_ICON_URL))) {
			editor.putString(Utils.MENU_ICON_URL,
					layout.getString(Utils.MENU_ICON_URL));
		} else {
			editor.putString(Utils.MENU_ICON_URL, null);
		}
		if (layout.has(Utils.HOME_URL_PHONE)
				&& !TextUtils.isEmpty(layout.getString(Utils.HOME_URL_PHONE))) {
			editor.putString(Utils.HOME_URL_PHONE,
					layout.getString(Utils.HOME_URL_PHONE));
		}
		if (layout.has(Utils.HOME_URL_TABLET)
				&& !TextUtils.isEmpty(layout.getString(Utils.HOME_URL_TABLET))) {
			editor.putString(Utils.HOME_URL_TABLET,
					layout.getString(Utils.HOME_URL_TABLET));
		} 
		if (layout.has(Utils.SCHOOL_LOGO_PHONE)
				&& !TextUtils
						.isEmpty(layout.getString(Utils.SCHOOL_LOGO_PHONE))) {
			editor.putString(Utils.SCHOOL_LOGO_PHONE,
					layout.getString(Utils.SCHOOL_LOGO_PHONE));
		}
		if (layout.has(Utils.SCHOOL_LOGO_TABLET)
				&& !TextUtils.isEmpty(layout
						.getString(Utils.SCHOOL_LOGO_TABLET))) {
			editor.putString(Utils.SCHOOL_LOGO_TABLET,
					layout.getString(Utils.SCHOOL_LOGO_TABLET));
		} else {
			editor.putString(Utils.SCHOOL_LOGO_TABLET,
					"http://cloud.ellucian.com/logo.png"); // need defaults
		}

		/** Adding About Info **/
		JSONObject about = jsonConfiguration.getJSONObject("about");
		if (about.has("contact")) {
			editor.putString(AboutActivity.PREFERENCES_CONTACT,
					about.getString("contact"));
		}
		if (about.has("icon")) {
			editor.putString(AboutActivity.PREFERENCES_ICON,
					about.getString("icon"));
		}
		if (about.has("logoUrlPhone")) {
			editor.putString(AboutActivity.PREFERENCES_LOGO_URL_PHONE,
					about.getString("logoUrlPhone"));
		} else {
			// TODO - default here
		}
		if (about.has("logoUrlTablet")) {
			editor.putString(AboutActivity.PREFERENCES_LOGO_URL_TABLET,
					about.getString("logoUrlTablet"));
		} else {
			// TODO - default here
		}
		if (about.has("phone")) {
			JSONObject phone = about.getJSONObject("phone");
			if (phone.has("display")) {
				editor.putString(AboutActivity.PREFERENCES_PHONE_DISPLAY,
						phone.getString("display"));
			}
			if (phone.has("number")) {
				editor.putString(AboutActivity.PREFERENCES_PHONE_NUMBER,
						phone.getString("number"));
			}
		}
		if (about.has("email")) {
			JSONObject email = about.getJSONObject("email");
			if (email.has("display")) {
				editor.putString(AboutActivity.PREFERENCES_EMAIL_DISPLAY,
						email.getString("display"));
			}
			if (email.has("address")) {
				editor.putString(AboutActivity.PREFERENCES_EMAIL_ADDRESS,
						email.getString("address"));
			}
		}
		if (about.has("website")) {

			JSONObject website = about.getJSONObject("website");
			if (website.has("display")) {
				editor.putString(AboutActivity.PREFERENCES_WEBSITE_DISPLAY,
						website.getString("display"));
			}
			if (website.has("url")) {
				editor.putString(AboutActivity.PREFERENCES_WEBSITE_URL,
						website.getString("url"));
			}
		}
		if (about.has("privacy")) {
			JSONObject privacy = about.getJSONObject("privacy");
			if (privacy.has("display")) {
				editor.putString(AboutActivity.PREFERENCES_PRIVACY_DISPLAY,
						privacy.getString("display"));
			}
			if (privacy.has("url")) {
				editor.putString(AboutActivity.PREFERENCES_PRIVACY_URL,
						privacy.getString("url"));
			}
		}

		JSONObject version = about.getJSONObject("version");
		if (version.has("url")) {
			editor.putString(AboutActivity.PREFERENCES_VERSION_URL,
					version.getString("url"));
		}
		editor.commit();

		/** Adding Security Info **/
		JSONObject security = jsonConfiguration.getJSONObject("security");
		if (security.has("url")) {
			Utils.addStringToPreferences(this, Utils.SECURITY,
					Utils.SECURITY_URL, security.getString("url"));
		}
		if (security.has("cas")) {
			JSONObject cas = security.getJSONObject("cas");
			String loginType = null;
			if (cas.has("loginType")) {
				loginType = cas.getString("loginType");
			}
			Utils.addStringToPreferences(this, Utils.SECURITY,
					Utils.LOGIN_TYPE, loginType);
			String loginUrl = null;
			if (cas.has("loginUrl")) {
				loginUrl = cas.getString("loginUrl");
			}
			Utils.addStringToPreferences(this, Utils.SECURITY,
					Utils.LOGIN_URL, loginUrl);
			
		} else if (security.has("web")) {
			JSONObject web = security.getJSONObject("web");

			Utils.addStringToPreferences(this, Utils.SECURITY,
					Utils.LOGIN_TYPE, Utils.BROWSER_LOGIN_TYPE);
			String loginUrl = null;
			if (web.has("loginUrl")) {
				loginUrl = web.getString("loginUrl");
			}
			Utils.addStringToPreferences(this, Utils.SECURITY,
					Utils.LOGIN_URL, loginUrl);
			
		} else {
			Utils.addStringToPreferences(this, Utils.SECURITY,
					Utils.LOGIN_TYPE, "native");
		}

		/** Adding Notification Info **/
		JSONObject notification = null;
		try {
		    notification = jsonConfiguration.getJSONObject("notification");
		    if (notification != null && notification.has("urls")) {
			    JSONObject urls = notification.getJSONObject("urls");
			    if (urls != null) {
				    Utils.addStringToPreferences(this, Utils.NOTIFICATION,
					        Utils.NOTIFICATION_REGISTRATION_URL, urls.getString("registration"));
				    Utils.addStringToPreferences(this, Utils.NOTIFICATION,
					        Utils.NOTIFICATION_DELIVERED_URL, urls.getString("delivered"));
			    }
		    }
		} catch(JSONException e) {
            Log.e(TAG, "exception processing NOTIFICATION URLs " + e, e);
			// ignore this for nddow
		}
		// remove enabled attribute if it exists to ensure we check
		Utils.removeValuesFromPreferences(this, Utils.NOTIFICATION,
				Utils.NOTIFICATION_ENABLED);

		/** Adding Map urls */

		if (jsonConfiguration.has("map")) {
			JSONObject map = jsonConfiguration.getJSONObject("map");
			if (map.has("campuses")) {
				Utils.addStringToPreferences(this, Utils.CONFIGURATION,
						Utils.MAP_CAMPUSES_URL, map.getString("campuses"));
			}
			if (map.has("buildings")) {
				Utils.addStringToPreferences(this, Utils.CONFIGURATION,
						Utils.MAP_BUILDINGS_URL, map.getString("buildings"));
			}
		}

		/** Adding Directory urls */

		if (jsonConfiguration.has("directory")) {
			JSONObject directory = jsonConfiguration.getJSONObject("directory");
			if (directory.has("allSearch")) {
				Utils.addStringToPreferences(this, Utils.CONFIGURATION,
						Utils.DIRECTORY_ALL_SEARCH_URL,
						directory.getString("allSearch"));
			}
			if (directory.has("facultySearch")) {
				Utils.addStringToPreferences(this, Utils.CONFIGURATION,
						Utils.DIRECTORY_FACULTY_SEARCH_URL,
						directory.getString("facultySearch"));
			}
			if (directory.has("studentSearch")) {
				Utils.addStringToPreferences(this, Utils.CONFIGURATION,
						Utils.DIRECTORY_STUDENT_SEARCH_URL,
						directory.getString("studentSearch"));
			}
		}

		/** Adding google analytics */
		if (jsonConfiguration.has("analytics")) {
			JSONObject analytics = jsonConfiguration.getJSONObject("analytics");
			String trackerId1 = analytics.has("ellucian") ? analytics.getString("ellucian") : null;
			String trackerId2 = analytics.has("client") ? analytics.getString("client") : null;
			Utils.addStringToPreferences(this, Utils.GOOGLE_ANALYTICS, Utils.GOOGLE_ANALYTICS_TRACKER1, trackerId1);
			Utils.addStringToPreferences(this, Utils.GOOGLE_ANALYTICS, Utils.GOOGLE_ANALYTICS_TRACKER2, trackerId2);

		}
	}

	private class ImageLoaderReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent incomingIntent) {
			imagesDone = true;

		}

	}

}
