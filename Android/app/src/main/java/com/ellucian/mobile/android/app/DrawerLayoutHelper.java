/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.ModuleType;
import com.ellucian.mobile.android.adapter.ModuleMenuAdapter;
import com.ellucian.mobile.android.client.services.AuthenticateUserIntentService;
import com.ellucian.mobile.android.login.LoginDialogFragment;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;
import com.ellucian.mobile.android.provider.EllucianContract.Notifications;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DrawerLayoutHelper {
	
	public static final String TAG = DrawerLayoutHelper.class.getSimpleName();
	public static final long AUTH_REFRESH_TIME = 30 * 60 * 1000; // 30 Minutes

	
	private Activity activity;
	public DrawerLayout drawerLayout;
	public final ExpandableListView drawerList;
	public ActionBarDrawerToggle drawerToggle;
	
	private BackgroundAuthenticationReceiver backgroundAuthenticationReceiver;
	private DrawerLayoutHelper.DrawerListener listener;
	private static NotificationsContentObserver contentObserver;
	
	public static interface DrawerListener {
		abstract void onDrawerOpened();
		abstract void onDrawerClosed();
	}

	public DrawerLayoutHelper(final Activity activity, ModuleMenuAdapter menuAdapter) {
		this.activity = activity;
		
		EllucianApplication ellucianApplicaton = (EllucianApplication) activity.getApplicationContext();
		contentObserver = new NotificationsContentObserver(new Handler(Looper.getMainLooper()), ellucianApplicaton);
		final ContentResolver contentResolver = activity.getContentResolver();
		contentResolver.registerContentObserver (Notifications.CONTENT_URI, true, contentObserver);

		drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
		drawerList = (ExpandableListView) activity.findViewById(R.id.left_drawer);

		if (drawerLayout != null && drawerList != null) {

			drawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
					GravityCompat.START);
			drawerList.setAdapter(menuAdapter);
			
			drawerList.setOnChildClickListener(new MenuChildClickListener());
			
			drawerList.setOnGroupClickListener(new MenuGroupClickListener());
			
			// Only expand groups that are not on the collapsed list
			String headersString = Utils.getStringFromPreferences(activity, Utils.MENU, Utils.MENU_HEADER_STATE, "");
			if (!TextUtils.isEmpty(headersString)) {
				String[] headerArray = headersString.split(",");
				ArrayList<String> headerList = new ArrayList<String> (Arrays.asList(headerArray));
				for (int i = 0; i < drawerList.getExpandableListAdapter().getGroupCount(); i++) {
					View groupView = drawerList.getExpandableListAdapter().getGroupView(i, false, null, drawerList);
					TextView labelView = (TextView) groupView.findViewById(R.id.drawer_list_item_label);
					String headerLabel = labelView.getText().toString();

					if (!headerList.contains(headerLabel)) {
						drawerList.expandGroup(i);
					}
				}					

			} else {
				for (int i = 0; i < drawerList.getExpandableListAdapter().getGroupCount(); i++) {
					drawerList.expandGroup(i);
				}
			}

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
						Application application = activity.getApplication();
						if(application instanceof EllucianApplication) {
							EllucianApplication ellucianApplication = (EllucianApplication)application;
							ellucianApplication.sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION,
								GoogleAnalyticsConstants.ACTION_BUTTON_PRESS,
								"Menu Tray Opened (Android)", null, null);
						}
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

	public void invalidateItems() {
		((ModuleMenuAdapter) drawerList.getExpandableListAdapter()).notifyDataSetChanged();
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

	
	public class MenuGroupClickListener implements OnGroupClickListener {
	
		@Override
		public boolean onGroupClick(ExpandableListView parent, View v,
				int groupPosition, long id) {
				TextView labelView = (TextView) v.findViewById(R.id.drawer_list_item_label);
				String label = labelView.getText().toString();
				
				// Only handle non Actions group clicks
				if (!label.equals(activity.getString(R.string.menu_header_actions))) {
					if (parent.isGroupExpanded(groupPosition)) {
						addMenuHeaderToCollapsedList(label);
						parent.collapseGroup(groupPosition);
					} else {
						removeMenuHeaderToCollapsedList(label);
						parent.expandGroup(groupPosition);
					}
				}

			return true;
		}
		
		private void addMenuHeaderToCollapsedList(String headerLabel) {
			
			if (!isMenuHeaderInCollapsedList(headerLabel)) {
				String headersString = Utils.getStringFromPreferences(activity, Utils.MENU, Utils.MENU_HEADER_STATE, "");
				if (!TextUtils.isEmpty(headersString)) {
					headersString += "," + headerLabel;					
				} else {
					headersString = headerLabel;
				}
				Log.d(TAG, "Udapted collapsed headers string: " + headersString);
				Utils.addStringToPreferences(activity, Utils.MENU, Utils.MENU_HEADER_STATE, headersString);	
			} 
				
		}
		
		private boolean removeMenuHeaderToCollapsedList(String headerLabel) {
			
			String headersString = Utils.getStringFromPreferences(activity, Utils.MENU, Utils.MENU_HEADER_STATE, "");
			if (!TextUtils.isEmpty(headersString)) {
				String[] headerArray = headersString.split(",");
				ArrayList<String> headerList = new ArrayList<String> (Arrays.asList(headerArray));
				int index = headerList.indexOf(headerLabel);
				if (index != -1) {
					headerList.remove(index);
					String newHeadersString = TextUtils.join(",", headerList);
					Log.d(TAG, "Udapted collapsed headers string: " + newHeadersString);
					Utils.addStringToPreferences(activity, Utils.MENU, Utils.MENU_HEADER_STATE, newHeadersString);
					return true;
				}
			}		
			return false;
			
		}
		
		private boolean isMenuHeaderInCollapsedList(String headerLabel) {
			String headersString = Utils.getStringFromPreferences(activity, Utils.MENU, Utils.MENU_HEADER_STATE, "");
			if (!TextUtils.isEmpty(headersString)) {
				String[] headerArray = headersString.split(",");
				ArrayList<String> headerList = new ArrayList<String> (Arrays.asList(headerArray));
				if (headerList.contains(headerLabel)) {
					return true;
				}			
			}		
			return false;
		}
	}
	
	public class MenuChildClickListener implements OnChildClickListener {
		
		@Override
		public boolean onChildClick(ExpandableListView parent, View view, int groupPosition,
				int childPosition, long id) {
			
			long packedPosition = ExpandableListView.getPackedPositionForChild(groupPosition, childPosition);
			int flatPosition = parent.getFlatListPosition(packedPosition);
			
			drawerLayout.closeDrawer(drawerList);

			Cursor modulesCursor = (Cursor) parent.getItemAtPosition(flatPosition);

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
			
			EllucianApplication ellucianApp = (EllucianApplication) activity
					.getApplication();

			boolean secure = false;
			
			List<String> roles = null;
			if(moduleId != null) {
				 roles = ModuleMenuAdapter.getModuleRoles(activity.getContentResolver(), moduleId);
			}
			if (roles != null && roles.size() > 0 && !(roles.size() == 1 && roles.get(0).equals(ModuleMenuAdapter.MODULE_ROLE_EVERYONE))) {
				secure = true;
			} else if (type.equals(ModuleType.WEB) && secureString != null) {
				secure = Boolean.parseBoolean(secureString);
			} else if (type.equals(ModuleType.CUSTOM)) {
				secure = Utils.isAuthenticationNeededForSubType(activity, subType);
			} else {
				secure = Utils.isAuthenticationNeededForType(type);
			}

			if (secure) {
				
				Intent intent = ModuleMenuAdapter.getIntent(activity, type, subType,
						label, moduleId);

				if (!ellucianApp.isUserAuthenticated()) {

					
					LoginDialogFragment loginFragment = new LoginDialogFragment();
					loginFragment.queueIntent(intent, roles);
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

				if (ellucianApp.isUserAuthenticated()) {
					ellucianApp.sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION,
								GoogleAnalyticsConstants.ACTION_MENU_SELECTION,
								"Menu-Click Sign Out", null, null);
				} else {
					ellucianApp.sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION,
							GoogleAnalyticsConstants.ACTION_MENU_SELECTION,
							"Menu-Click Sign In", null, null);
				}

				if (ellucianApp.isUserAuthenticated()) {
					// Sign Out

					// This also removes saved users
					ellucianApp.removeAppUser(true);

					Toast signOutMessage = Toast.makeText(activity,
							R.string.dialog_signed_out, Toast.LENGTH_LONG);
					signOutMessage.setGravity(Gravity.CENTER, 0, 0);
					signOutMessage.show();

					Intent intent = ModuleMenuAdapter.getIntent(activity,
							ModuleType._HOME, null,
							activity.getString(R.string.menu_home), null);
					if (intent != null) {
						// Make sure to reset the menu adapter so the navigation drawer will 
						// display correctly for a non-authenticated user
						ellucianApp.resetModuleMenuAdapter();
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
			return true;
		}																	
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
	
	private class NotificationsContentObserver extends ContentObserver {

		private EllucianApplication application;

		public NotificationsContentObserver(Handler handler,
				EllucianApplication application) {
			super(handler);
			this.application = application;
		}

		@Override
		public void onChange(boolean selfChange) {
			onChange(selfChange, null);
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange);
			activity.getContentResolver().unregisterContentObserver(this);
			application.resetModuleMenuAdapter();
		}
	}
}
