package com.ellucian.mobile.android.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.provider.BaseColumns;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.ModuleType;
import com.ellucian.mobile.android.about.AboutActivity;
import com.ellucian.mobile.android.adapter.ModuleMenuAdapter;
import com.ellucian.mobile.android.client.services.AuthenticateUserIntentService;
import com.ellucian.mobile.android.login.LoginDialogFragment;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Logger.LogLevel;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public class DrawerLayoutHelper {

	public DrawerLayout drawerLayout;
	public ListView drawerList;
	public ActionBarDrawerToggle drawerToggle;
	private Activity activity;
	private BackgroundAuthenticationReceiver backgroundAuthenticationReceiver;
	private DrawerLayoutHelper.DrawerListener listener;
	
	public static interface DrawerListener {
		abstract void onDrawerOpened();
		abstract void onDrawerClosed();
	}

	public DrawerLayoutHelper(final Activity activity) {
		this.activity = activity;

		boolean allowMaps = allowMaps(activity);
		String selection = "";
		List<String> selectionArgs = new ArrayList<String>();
		List<String> customTypes = ((EllucianActivity)activity).getEllucianApp().getModuleConfigTypeList();
		for (int i = 0; i < ModuleType.ALL.length; i++) {
			String type = ModuleType.ALL[i];
			
			if (type.equals(ModuleType.MAPS) && !allowMaps) {
				continue;
			}
			
			if (type.equals(ModuleType.CUSTOM)) {
				for (int n = 0; n < customTypes.size(); n++) {
					String customType = customTypes.get(n);

					selection += " OR ";
					selection += "( " + Modules.MODULE_TYPE + " = ?" + " AND " + Modules.MODULE_SUB_TYPE + " = ? )";

					selectionArgs.add(type);
					selectionArgs.add(customType);			
				}
			} else {
				if (selection.length() > 0) {
					selection += " OR ";
				}
				selection += Modules.MODULE_TYPE + " = ?";
				selectionArgs.add(type);
			}	
		}

		final ContentResolver contentResolver = activity.getContentResolver();
		Cursor modulesCursor = contentResolver.query(Modules.CONTENT_URI,
				new String[] { BaseColumns._ID, Modules.MODULE_TYPE, Modules.MODULE_SUB_TYPE,
						Modules.MODULE_NAME, Modules.MODULES_ICON_URL,
						Modules.MODULES_ID, Modules.MODULE_SECURE }, selection,
				selectionArgs.toArray(new String[selectionArgs.size()]),
				Modules.DEFAULT_SORT);

		modulesCursor.moveToFirst();
		int rows = modulesCursor.getCount();

		modulesCursor.moveToFirst();
		int typeIndex = modulesCursor.getColumnIndex(Modules.MODULE_TYPE);

		MatrixCursor firstRow = new MatrixCursor(new String[] {
				BaseColumns._ID, Modules.MODULE_TYPE, Modules.MODULE_SUB_TYPE, Modules.MODULE_NAME,
				Modules.MODULES_ICON_URL, ModuleMenuAdapter.IMAGE_RESOURCE,
				Modules.MODULE_SECURE });

		if (rows > 0) {
			String type = modulesCursor.getString(typeIndex);
			if (!type.equals(ModuleType.HEADER)) {
				firstRow.addRow(new Object[] { "0", ModuleType.HEADER, null,
						activity.getString(R.string.menu_header_applications),
						null, R.drawable.menu_header_endcap, false });
			}
		}

		MatrixCursor actions = new MatrixCursor(new String[] { BaseColumns._ID,
				Modules.MODULE_TYPE, Modules.MODULE_SUB_TYPE, Modules.MODULE_NAME,
				Modules.MODULES_ICON_URL, ModuleMenuAdapter.IMAGE_RESOURCE,
				Modules.MODULE_SECURE });
		actions.addRow(new Object[] { "" + rows++, ModuleType.HEADER, null,
				activity.getString(R.string.menu_header_actions), null,
				R.drawable.menu_header_endcap, false });
		actions.addRow(new Object[] { "" + rows++, ModuleType._HOME, null,
				activity.getString(R.string.menu_home), null,
				R.drawable.menu_home, false });
		String aboutIconUrl = Utils.getStringFromPreferences(activity,
				Utils.APPEARANCE, AboutActivity.PREFERENCES_ICON, null);
		actions.addRow(new Object[] { "" + rows++, ModuleType._ABOUT, null,
				activity.getString(R.string.menu_about), aboutIconUrl, null,
				false });
		if (((EllucianActivity)activity).getConfigurationProperties().allowSwitchSchool) {
			actions.addRow(new Object[] { "" + rows++, ModuleType._SWITCH_SCHOOLS, null,
					activity.getString(R.string.menu_switch_school), null,
					R.drawable.menu_switch_schools, false });
		}
		actions.addRow(new Object[] { "" + rows++, ModuleType._SIGN_IN, null,
				activity.getString(R.string.menu_sign_in), null,
				R.drawable.menu_sign_in, false });

		Cursor[] cursors = { firstRow, modulesCursor, actions };
		final Cursor extendedCursor = new MergeCursor(cursors);

		drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
		drawerList = (ListView) activity.findViewById(R.id.left_drawer);

		if (drawerLayout != null && drawerList != null) {

			drawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
					GravityCompat.START);
			drawerList.setAdapter(new ModuleMenuAdapter(activity,
					extendedCursor, 0));

			drawerList.setOnItemClickListener(new MenuItemClickListener());

			final ActionBar actionBar = activity.getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeButtonEnabled(true);

			drawerToggle = new ActionBarDrawerToggle(activity, drawerLayout,
					R.drawable.ic_drawer, R.string.drawer_open,
					R.string.drawer_close) {
				public void onDrawerClosed(View view) {
					actionBar.setTitle(activity.getTitle());
					activity.invalidateOptionsMenu();
					// If a extra listener has been set, trigger onDrawerClosed for it
					if (listener != null) {
						listener.onDrawerClosed();
					}
				}

				public void onDrawerOpened(View drawerView) {
					actionBar.setTitle(R.string.app_name);
					activity.invalidateOptionsMenu();
					// If a extra listener has been set, trigger onDrawerOpened for it
					if (listener != null) {
						listener.onDrawerOpened();
					}
				}

				@Override
				public void onDrawerSlide(View drawerView, float slideOffset) {
					super.onDrawerSlide(drawerView, slideOffset);
					if (slideOffset == 1f) {
						sendEventGoogleAnalytics(activity,
								GoogleAnalyticsConstants.CATEGORY_UI_ACTION,
								GoogleAnalyticsConstants.ACTION_BUTTON_PRESS,
								"Menu Tray Opened (Android)", null);
					}
				}

				public boolean onOptionsItemSelected(MenuItem item) {
					if (item != null && item.getItemId() == android.R.id.home
							&& isDrawerIndicatorEnabled()) {
						if (drawerLayout.isDrawerVisible(drawerList)) {
							drawerLayout.closeDrawer(drawerList);
						} else {
							drawerLayout.openDrawer(drawerList);
						}
						return true;
					}
					return false;
				}

			};
			drawerLayout.setDrawerListener(drawerToggle);
		}
	}

	private boolean allowMaps(Context context) {
		// check if google play services is present
		try {
			context.getPackageManager().getApplicationInfo(
					"com.google.android.gms", 0);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}

	public void removeMenuItems(Menu menu) {
		// boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
		// for (int i = 0; i < menu.size(); i++) {
		// MenuItem mi = menu.getItem(i);
		// mi.setVisible(!drawerOpen);
		// } // TODO may have issues with children also changing visibility
	}

	public void changeDrawer() {
		if (drawerLayout.isDrawerOpen(drawerList)) {
			drawerLayout.closeDrawer(drawerList);
		} else {
			drawerLayout.openDrawer(drawerList);
		}

	}

	public void onConfigurationChanged(Configuration newConfig) {
		drawerToggle.onConfigurationChanged(newConfig);
	}

	private void sendEventGoogleAnalytics(final Activity activity,
			String category, String action, String label, Long value) {
		GoogleAnalytics gaInstance = GoogleAnalytics.getInstance(activity);
		gaInstance.getLogger().setLogLevel(LogLevel.VERBOSE); 
		String trackerId1 = Utils.getStringFromPreferences(activity,
				Utils.GOOGLE_ANALYTICS, Utils.GOOGLE_ANALYTICS_TRACKER1, null);
		Tracker gaTracker1;
		if (trackerId1 != null) {
			gaTracker1 = gaInstance.getTracker(trackerId1);
			gaTracker1.send(MapBuilder
				    .createEvent(category, action, label, value).build());
		}
	}

	public boolean isAuthenticationNeededForType(String type) {
		List<String> authTypeList = Arrays
				.asList(ModuleType.AUTHENTICATION_NEEDED);
		return authTypeList.contains(type);
	}

	public void invalidateItems() {
		((ModuleMenuAdapter) drawerList.getAdapter()).notifyDataSetChanged();
	}

	public class MenuItemClickListener implements OnItemClickListener {

		private static final long AUTH_REFRESH_TIME = 30 * 60 * 1000; // 30
																		// Minutes

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			drawerList.setItemChecked(position, true);
			drawerLayout.closeDrawer(drawerList);

			Cursor modulesCursor = (Cursor) parent.getItemAtPosition(position);

			String label = modulesCursor.getString(modulesCursor
					.getColumnIndex(Modules.MODULE_NAME));
			String type = modulesCursor.getString(modulesCursor
					.getColumnIndex(Modules.MODULE_TYPE));
			String subType = modulesCursor.getString(modulesCursor
					.getColumnIndex(Modules.MODULE_SUB_TYPE));
			int moduleIdIndex = modulesCursor
					.getColumnIndex(Modules.MODULES_ID);
			String moduleId = null;
			if (moduleIdIndex > -1)
				moduleId = modulesCursor.getString(moduleIdIndex);

			String secureString = modulesCursor.getString(modulesCursor
					.getColumnIndex(Modules.MODULE_SECURE));

			boolean secure = false;
			if (type.equals(ModuleType.WEB) && secureString != null) {
				secure = Boolean.parseBoolean(secureString);
			} else {
				secure = isAuthenticationNeededForType(type);
			}

			if (secure) {
				EllucianApplication ellucianApp = (EllucianApplication) activity
						.getApplication();

				Intent intent = ModuleMenuAdapter.getIntent(activity, type, subType,
						label, moduleId);

				if (!ellucianApp.isUserAuthenticated()) {
					LoginDialogFragment loginFragment = new LoginDialogFragment();
					loginFragment.queueIntent(intent);
					loginFragment.show(activity.getFragmentManager(),
							LoginDialogFragment.LOGIN_DIALOG);
				} else if (type.equals(ModuleType.WEB)) {
					
					//do if basic authentication only; web login will be handled by cookies
					String loginType = Utils.getStringFromPreferences(activity, Utils.SECURITY, Utils.LOGIN_TYPE, Utils.NATIVE_LOGIN_TYPE);
					if (loginType.equals(Utils.NATIVE_LOGIN_TYPE) && System.currentTimeMillis() > (ellucianApp
							.getLastAuthRefresh() + AUTH_REFRESH_TIME)) {
						LocalBroadcastManager lbm = LocalBroadcastManager
								.getInstance(activity);
						backgroundAuthenticationReceiver = new BackgroundAuthenticationReceiver();
						backgroundAuthenticationReceiver
								.setQueuedIntent(intent);
						lbm.registerReceiver(
								backgroundAuthenticationReceiver,
								new IntentFilter(
										AuthenticateUserIntentService.ACTION_BACKGROUND_AUTH));

						((EllucianActivity) activity)
								.sendEvent(
										GoogleAnalyticsConstants.CATEGORY_AUTHENTICATION,
										GoogleAnalyticsConstants.ACTION_LOGIN,
										"Background re-authenticate", null,
										null);

						Toast signInMessage = Toast.makeText(activity,
								R.string.dialog_re_authenticate,
								Toast.LENGTH_LONG);
						signInMessage.setGravity(Gravity.CENTER, 0, 0);
						signInMessage.show();

						Intent loginIntent = new Intent(activity,
								AuthenticateUserIntentService.class);
						loginIntent.putExtra(Extra.LOGIN_USERNAME,
								ellucianApp.getAppUserName());
						loginIntent.putExtra(Extra.LOGIN_PASSWORD,
								ellucianApp.getAppUserPassword());
						loginIntent.putExtra(Extra.LOGIN_BACKGROUND, true);
						activity.startService(loginIntent);
					} else {
						activity.startActivity(intent);
					}
				} else {
					activity.startActivity(intent);
				}
			} else if (type.equals(ModuleType._SIGN_IN)) {

				EllucianApplication ellucianApp = (EllucianApplication) activity
						.getApplication();

				if (ellucianApp.isUserAuthenticated()) {
					sendEventGoogleAnalytics(activity,
							GoogleAnalyticsConstants.CATEGORY_UI_ACTION,
							GoogleAnalyticsConstants.ACTION_MENU_SELECTION,
							"Menu-Click Sign Out", null);
				} else {
					sendEventGoogleAnalytics(activity,
							GoogleAnalyticsConstants.CATEGORY_UI_ACTION,
							GoogleAnalyticsConstants.ACTION_MENU_SELECTION,
							"Menu-Click Sign In", null);
				}

				if (ellucianApp.isUserAuthenticated()) {
					// Sign Out

					// This also removes saved users
					ellucianApp.removeAppUser();

					Toast signOutMessage = Toast.makeText(activity,
							R.string.dialog_signed_out, Toast.LENGTH_LONG);
					signOutMessage.setGravity(Gravity.CENTER, 0, 0);
					signOutMessage.show();

					Intent intent = ModuleMenuAdapter.getIntent(activity,
							ModuleType._HOME, null,
							activity.getString(R.string.menu_home), null);
					if (intent != null) {
						activity.startActivity(intent);
					}

				} else {
					// Sign In
					showLoginDialog();
				}

			} else {

				Intent intent = ModuleMenuAdapter.getIntent(activity, type, subType,
						label, moduleId);
				if (intent != null) {
					activity.startActivity(intent);
				}
			}
		}
	}

	private void showLoginDialog() {
		LoginDialogFragment loginFragment = new LoginDialogFragment();
		loginFragment.show(activity.getFragmentManager(),
				LoginDialogFragment.LOGIN_DIALOG);

	}
	
	public void setDrawerListener(DrawerLayoutHelper.DrawerListener listener) {
		this.listener = listener;
	}
	
	public boolean isDrawerOpen() {
		return drawerLayout.isDrawerOpen(drawerList);
	}
	
	public void openDrawer() {
		drawerLayout.openDrawer(drawerList);
	}
	
	public void closeDrawer() {
		drawerLayout.closeDrawer(drawerList);
	}
	public class BackgroundAuthenticationReceiver extends BroadcastReceiver {

		private Intent queuedIntent;

		@Override
		public void onReceive(Context context, Intent incomingIntent) {
			String result = incomingIntent.getStringExtra(Extra.LOGIN_SUCCESS);

			if (!TextUtils.isEmpty(result)
					&& result
							.equals(AuthenticateUserIntentService.ACTION_SUCCESS)) {
				activity.startActivity(queuedIntent);
			} else {

				Toast signInMessage = Toast.makeText(activity,
						R.string.dialog_sign_in_failed, Toast.LENGTH_LONG);
				signInMessage.setGravity(Gravity.CENTER, 0, 0);
				signInMessage.show();
			}
			LocalBroadcastManager.getInstance(activity).unregisterReceiver(
					backgroundAuthenticationReceiver);

		}

		public void setQueuedIntent(Intent intent) {
			queuedIntent = intent;
		}
	}
}
