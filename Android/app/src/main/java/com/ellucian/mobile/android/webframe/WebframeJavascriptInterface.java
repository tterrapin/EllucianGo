// Copyright 2014 Ellucian Company L.P and its affiliates.
package com.ellucian.mobile.android.webframe;

import java.util.List;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.ModuleType;
import com.ellucian.mobile.android.adapter.ModuleMenuAdapter;
import com.ellucian.mobile.android.app.DrawerLayoutHelper;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.services.AuthenticateUserIntentService;
import com.ellucian.mobile.android.login.LoginDialogFragment;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

public class WebframeJavascriptInterface {
	private static final String TAG = "WebAppInterface";
	private WebframeActivity webActivity;
	private AuthenticationReceiver authenticationReceiver;
	private QueuedIntentAuthenticationReceiver queuedIntentAuthenticationReceiver;

	public WebframeJavascriptInterface(WebframeActivity c) {
		webActivity = c;
	}

	/*
	 * Useful during debugging
	 */
	@JavascriptInterface
	public void log(String message) {
		Log.d(TAG, message);
	}

	@JavascriptInterface
	public boolean refreshRoles() {

		int RETRY_LIMIT = 50;
		int SLEEP_INTERVAL = 200;

		authenticationReceiver = new AuthenticationReceiver();
		LocalBroadcastManager.getInstance(webActivity).registerReceiver(
				authenticationReceiver,
				new IntentFilter(
						AuthenticateUserIntentService.ACTION_UPDATE_MAIN));
		Intent intent = new Intent(webActivity,
				AuthenticateUserIntentService.class);
		intent.putExtra(Extra.REFRESH, true);
		webActivity.startService(intent);

		int i = 0;
		while (!authenticationReceiver.completed && i < RETRY_LIMIT) {
			try {
				Thread.sleep(SLEEP_INTERVAL);
				i++;
			} catch (InterruptedException e) {
			}
		}
		if (i == RETRY_LIMIT) {
			Log.d(TAG,
					"Login failed while refreshing roles from webview - took too much time");
		}

		final WebView webView = webActivity.getWebView();
		webView.post(new Runnable() {
			@Override
			public void run() {
				EllucianApplication ellucianApplication = (EllucianApplication) webActivity
						.getApplicationContext();
				ellucianApplication.resetModuleMenuAdapter();
			}
		});
		return authenticationReceiver.success;

	}

	public class AuthenticationReceiver extends BroadcastReceiver {

		public boolean success = false;
		public boolean completed = false;

		@Override
		public void onReceive(Context context, Intent incomingIntent) {
			String result = incomingIntent.getStringExtra(Extra.LOGIN_SUCCESS);

			if (!TextUtils.isEmpty(result)
					&& result
							.equals(AuthenticateUserIntentService.ACTION_SUCCESS)) {
				LocalBroadcastManager.getInstance(context).unregisterReceiver(
						authenticationReceiver);
				success = true;
				completed = true;
			} else {
				Log.d(TAG, "Login failed while refreshing roles from webview");
				completed = true;
			}

		}
	};

	@JavascriptInterface
	public void openMenu(String name, String type) {
		type = type.toLowerCase(Locale.US);
		EllucianApplication ellucianApplication = (EllucianApplication) webActivity
				.getApplicationContext();
		final ContentResolver contentResolver = webActivity
				.getContentResolver();

		boolean allowMaps = ModuleMenuAdapter.allowMaps(webActivity);

		if (type.equals(ModuleType.MAPS) && !allowMaps) {
			return;
		}

		Cursor modulesCursor = contentResolver.query(Modules.CONTENT_URI,
				new String[] { BaseColumns._ID, Modules.MODULE_TYPE,
						Modules.MODULE_SUB_TYPE, Modules.MODULE_NAME,
						Modules.MODULES_ICON_URL, Modules.MODULES_ID,
						Modules.MODULE_SECURE, Modules.MODULE_SHOW_FOR_GUEST },
				Modules.MODULE_NAME + " =? AND " + Modules.MODULE_TYPE + " =?",
				new String[] { name, type }, Modules.DEFAULT_SORT);

		String subType = null;
		String moduleId = null;
		String secureString = null;
		boolean found = false;
		if (modulesCursor.moveToFirst()) {
			do {
				subType = modulesCursor.getString(modulesCursor
							.getColumnIndex(Modules.MODULE_SUB_TYPE));
				moduleId = modulesCursor.getString(modulesCursor
							.getColumnIndex(Modules.MODULES_ID));
				secureString = modulesCursor.getString(modulesCursor
							.getColumnIndex(Modules.MODULE_SECURE));

				boolean secure = false;

				List<String> roles = null;
				if (moduleId != null) {
					
					Intent intent = ModuleMenuAdapter.getIntent(webActivity, type,
							subType, name, moduleId);
					
					if(intent != null) {
						roles = ModuleMenuAdapter.getModuleRoles(contentResolver, moduleId);

						if (roles != null
								&& roles.size() > 0
								&& !(roles.size() == 1 && roles.get(0).equals(
										ModuleMenuAdapter.MODULE_ROLE_EVERYONE))) {
							secure = true;
						} else if (type.equals(ModuleType.WEB) && secureString != null) {
							secure = Boolean.parseBoolean(secureString);
						} else if (type.equals(ModuleType.CUSTOM)) {
							secure = Utils.isAuthenticationNeededForSubType(webActivity,
									subType);
						} else {
							secure = Utils.isAuthenticationNeededForType(type);
						}


						if (secure) {
							if (!ellucianApplication.isUserAuthenticated()) {
									LoginDialogFragment loginFragment = new LoginDialogFragment();
									loginFragment.queueIntent(intent, roles);
									loginFragment.show(webActivity.getFragmentManager(),
											LoginDialogFragment.LOGIN_DIALOG);
									found = true;
							} else if (type.equals(ModuleType.WEB)) {
									// do if basic authentication only; web login will be
									// handled by cookies
									String loginType = Utils.getStringFromPreferences(
											webActivity, Utils.SECURITY, Utils.LOGIN_TYPE,
											Utils.NATIVE_LOGIN_TYPE);
									if (loginType.equals(Utils.NATIVE_LOGIN_TYPE)
											&& System.currentTimeMillis() > (ellucianApplication
													.getLastAuthRefresh() + DrawerLayoutHelper.AUTH_REFRESH_TIME)) {
										LocalBroadcastManager lbm = LocalBroadcastManager
												.getInstance(webActivity);
										queuedIntentAuthenticationReceiver = new QueuedIntentAuthenticationReceiver();
										queuedIntentAuthenticationReceiver
												.setQueuedIntent(intent, name, type, roles);
										lbm.registerReceiver(
												queuedIntentAuthenticationReceiver,
												new IntentFilter(
														AuthenticateUserIntentService.ACTION_BACKGROUND_AUTH));

										((EllucianActivity) webActivity)
												.sendEvent(
														GoogleAnalyticsConstants.CATEGORY_AUTHENTICATION,
														GoogleAnalyticsConstants.ACTION_LOGIN,
														"Background re-authenticate", null,
														null);

										Toast signInMessage = Toast.makeText(webActivity,
												R.string.dialog_re_authenticate,
												Toast.LENGTH_LONG);
										signInMessage.setGravity(Gravity.CENTER, 0, 0);
										signInMessage.show();

										Intent loginIntent = new Intent(webActivity,
												AuthenticateUserIntentService.class);
										loginIntent.putExtra(Extra.LOGIN_USERNAME,
												ellucianApplication.getAppUserName());
										loginIntent.putExtra(Extra.LOGIN_PASSWORD,
												ellucianApplication.getAppUserPassword());
										loginIntent.putExtra(Extra.LOGIN_BACKGROUND, true);
										webActivity.startService(loginIntent);
									} else {
										boolean showModule = roles == null;
										if (roles != null) {
										
											List<String> userRoles = ellucianApplication.getAppUserRoles();
											if (roles.contains(ModuleMenuAdapter.MODULE_ROLE_EVERYONE)) {
												showModule = true;
											} else if (userRoles != null) {
												showModule = ModuleMenuAdapter.doesUserHaveAccessForRole(userRoles, roles);
											}
										}
										if(showModule) {
											webActivity.startActivity(intent);
											found = true;
										}
									}
							} else {
								boolean showModule = roles == null;
								if (roles != null) {
								
									List<String> userRoles = ellucianApplication.getAppUserRoles();
									if (roles.contains(ModuleMenuAdapter.MODULE_ROLE_EVERYONE)) {
										showModule = true;
									} else if (userRoles != null) {
										showModule = ModuleMenuAdapter.doesUserHaveAccessForRole(userRoles, roles);
									}
								}
								if(showModule) {
									webActivity.startActivity(intent);
									found = true;
								}
							}
						} else {
							webActivity.startActivity(intent);
							found = true;
						}
					}
				}
			} while (!found && modulesCursor.moveToNext());

		}
		
		modulesCursor.close();
		
		if(!found) {
			Log.d(TAG, "No menu item found for name: '" + name
					+ "' type: '" + type + "'");
			Toast unauthorizedToast = Toast.makeText(webActivity,
					R.string.unauthorized_feature, Toast.LENGTH_LONG);
			unauthorizedToast.setGravity(Gravity.CENTER, 0, 0);
			unauthorizedToast.show();
		}
	}

	@JavascriptInterface
	public void reloadWebModule() {
		final WebView webView = webActivity.getWebView();
		webView.post(new Runnable() {
			@Override
			public void run() {
				webView.loadUrl(webActivity.requestUrl);
			}
		});
	}

	private class QueuedIntentAuthenticationReceiver extends BroadcastReceiver {

		private Intent queuedIntent;
		private String name;
		private String type;
		private List<String> roles;

		@Override
		public void onReceive(Context context, Intent incomingIntent) {
			String result = incomingIntent.getStringExtra(Extra.LOGIN_SUCCESS);

			if (!TextUtils.isEmpty(result)
					&& result
							.equals(AuthenticateUserIntentService.ACTION_SUCCESS)) {
				EllucianApplication ellucianApplication = (EllucianApplication) webActivity
						.getApplicationContext();
	
				boolean showModule = roles == null;
				if (roles != null) {
				
					List<String> userRoles = ellucianApplication.getAppUserRoles();
					if (roles.contains(ModuleMenuAdapter.MODULE_ROLE_EVERYONE)) {
						showModule = true;
					} else if (userRoles != null) {
						showModule = ModuleMenuAdapter.doesUserHaveAccessForRole(userRoles, roles);
					}
				}
				if(showModule) {
					webActivity.startActivity(queuedIntent);
				} else {
					Log.d(TAG, "No menu item found for name: '" + name
							+ "' type: '" + type + "'");
					Toast unauthorizedToast = Toast.makeText(webActivity,
							R.string.unauthorized_feature, Toast.LENGTH_LONG);
					unauthorizedToast.setGravity(Gravity.CENTER, 0, 0);
					unauthorizedToast.show();
				}
			} else {

				Toast signInMessage = Toast.makeText(webActivity,
						R.string.dialog_sign_in_failed, Toast.LENGTH_LONG);
				signInMessage.setGravity(Gravity.CENTER, 0, 0);
				signInMessage.show();
			}
			LocalBroadcastManager.getInstance(webActivity).unregisterReceiver(
					queuedIntentAuthenticationReceiver);

		}

		public void setQueuedIntent(Intent intent, String name, String type, List<String> roles) {
			queuedIntent = intent;
			this.name = name;
			this.type = type;
			this.roles = roles;
		}
	}
}
