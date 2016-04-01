/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.MainActivity;
import com.ellucian.mobile.android.ModuleType;
import com.ellucian.mobile.android.about.AboutActivity;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.courses.daily.CoursesDailyScheduleActivity;
import com.ellucian.mobile.android.directory.DirectoryActivity;
import com.ellucian.mobile.android.events.EventsActivity;
import com.ellucian.mobile.android.finances.FinancesActivity;
import com.ellucian.mobile.android.grades.GradesActivity;
import com.ellucian.mobile.android.ilp.IlpCardActivity;
import com.ellucian.mobile.android.maps.MapsActivity;
import com.ellucian.mobile.android.multimedia.AudioActivity;
import com.ellucian.mobile.android.multimedia.VideoActivity;
import com.ellucian.mobile.android.news.NewsActivity;
import com.ellucian.mobile.android.notifications.NotificationsActivity;
import com.ellucian.mobile.android.numbers.NumbersListActivity;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;
import com.ellucian.mobile.android.provider.EllucianContract.ModulesProperties;
import com.ellucian.mobile.android.provider.EllucianContract.ModulesRoles;
import com.ellucian.mobile.android.registration.RegistrationActivity;
import com.ellucian.mobile.android.schoolselector.SchoolSelectionActivity;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.ModuleConfiguration;
import com.ellucian.mobile.android.util.Utils;
import com.ellucian.mobile.android.webframe.WebframeActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class ModuleMenuAdapter extends CursorTreeAdapter {
	
	private static final String TAG = ModuleMenuAdapter.class.getSimpleName();
	private static final String IMAGE_RESOURCE = "imageResource";
	public static final String EXTERNAL_WEB_BROWSER = "externalWebBrowser";
	public static final String PLANNING_TOOL = "planningTool";
	private static final String HEADER_COLLAPSIBLE = "headerCollapsible";
	public static final String MODULE_ROLE_EVERYONE = "Everyone";
    public static final String DIRECTORY_MODULE_VERSION = "directoryModuleVersion";
    public static final String DIRECTORY_CATEGORY = "directoryCategory";
    public static final String APP_LAUNCHER_URL = "appLauncherUrl";
    public static final String APP_STORE_URL = "appStore`Url";
	
	private ArrayList<Cursor> childCursorList = new ArrayList<Cursor>();
	private final LayoutInflater inflater;

		
	
	private ModuleMenuAdapter(Context context, Cursor groupCursor, ArrayList<Cursor> childCursorList) {
		super(groupCursor, context);
		this.childCursorList = childCursorList;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
		
	/**
	 * Never call this method directly, please access this method through
	 * EllucianApplication.getModuleMenuAdapter() instead. 
	 */
	public static ModuleMenuAdapter buildInstance(Context context) {
		
		EllucianApplication ellucianApplication = (EllucianApplication) context.getApplicationContext();
		final ContentResolver contentResolver = context.getContentResolver();

		boolean allowMaps = Utils.allowMaps(context);
		String modulesSelection = "";
		List<String> modulesSelectionArgs = new ArrayList<String>();
		List<String> customTypes = ellucianApplication.getModuleConfigTypeList();
		for (int i = 0; i < ModuleType.ALL.length; i++) {
			String type = ModuleType.ALL[i];

			if (type.equals(ModuleType.MAPS) && !allowMaps) {   
				continue;
			}

			if (type.equals(ModuleType.CUSTOM)) {
				for (int n = 0; n < customTypes.size(); n++) {
					String customType = customTypes.get(n);

					modulesSelection += " OR ";
					modulesSelection += "( " + Modules.MODULE_TYPE + " = ?" + " AND " + Modules.MODULE_SUB_TYPE + " = ? )";

					modulesSelectionArgs.add(type);
					modulesSelectionArgs.add(customType);			
				}
			} else {
				if (modulesSelection.length() > 0) {
					modulesSelection += " OR ";
				}
				modulesSelection += Modules.MODULE_TYPE + " = ?";
				modulesSelectionArgs.add(type);
			}	
		}

		// Pull all the legal modules currently set in the Modules table of the database
		Cursor modulesCursor = contentResolver.query(Modules.CONTENT_URI,
				new String[] { BaseColumns._ID, Modules.MODULE_TYPE, Modules.MODULE_SUB_TYPE,
				Modules.MODULE_NAME, Modules.MODULES_ICON_URL,
				Modules.MODULES_ID, Modules.MODULE_SECURE, Modules.MODULE_SHOW_FOR_GUEST }, modulesSelection,
				modulesSelectionArgs.toArray(new String[modulesSelectionArgs.size()]),
				Modules.DEFAULT_SORT);


		int totalRows = modulesCursor.getCount();

		MatrixCursor headersCursor = new MatrixCursor(new String[] {
				BaseColumns._ID, Modules.MODULE_TYPE, Modules.MODULE_SUB_TYPE, Modules.MODULE_NAME,
				Modules.MODULES_ICON_URL, ModuleMenuAdapter.IMAGE_RESOURCE,
				Modules.MODULE_SECURE, ModuleMenuAdapter.HEADER_COLLAPSIBLE });

		ArrayList<Cursor> childCursorList = new ArrayList<Cursor>();

		// if no modules skip section and just add the base actions
		if (totalRows > 0) {

			int typeIndex = modulesCursor.getColumnIndex(Modules.MODULE_TYPE);
			int subTypeIndex = modulesCursor.getColumnIndex(Modules.MODULE_SUB_TYPE);
			int nameIndex = modulesCursor.getColumnIndex(Modules.MODULE_NAME);
			int moduleIdIndex = modulesCursor.getColumnIndex(Modules.MODULES_ID);
			int iconUrlIndex = modulesCursor.getColumnIndex(Modules.MODULES_ICON_URL);
			int secureIndex = modulesCursor.getColumnIndex(Modules.MODULE_SECURE);		
			int showGuestIndex = modulesCursor.getColumnIndex(Modules.MODULE_SHOW_FOR_GUEST);	
			
			// holds the current child modules
			MatrixCursor currentChildCursor = null;
			
			String currentHeaderName = "";
			
			/*
			//Get notification count
			String notificationsCount = null;
			if (ellucianApplication.isUserAuthenticated()) {
				Cursor notificationsCursor = contentResolver.query(Notifications.CONTENT_URI, 
						   new String[] { Notifications.NOTIFICATIONS_ID},
						   Notifications.NOTIFICATIONS_STATUSES + " not like ? or " + Notifications.NOTIFICATIONS_STATUSES + " is null",
						   new String[]{"%" + Notification.STATUS_READ + "%"},
						   Notifications.DEFAULT_SORT);
				int count = notificationsCursor.getCount();
				if(count > 0) {
					notificationsCount = "" + count;
				}
				notificationsCursor.close();
			}
			*/

			// Handle the first row, if no header set at order "1" create default header
			modulesCursor.moveToFirst();
			String type = modulesCursor.getString(typeIndex);
			if (!type.equals(ModuleType.HEADER)) {
				currentHeaderName = context.getString(R.string.menu_header_applications);

				currentChildCursor = new MatrixCursor(new String[] {
						BaseColumns._ID, Modules.MODULE_TYPE, Modules.MODULE_SUB_TYPE, Modules.MODULE_NAME,
						Modules.MODULES_ICON_URL, Modules.MODULES_ID,
						Modules.MODULE_SECURE, Modules.MODULE_LOCK, Modules.MODULE_RIGHT_TEXT });
							
				// Add the first child to the cursor
				String subType = modulesCursor.getString(subTypeIndex);
				String name = modulesCursor.getString(nameIndex);
				String iconUrl = modulesCursor.getString(iconUrlIndex);
				String moduleId = modulesCursor.getString(moduleIdIndex);
				String secure = modulesCursor.getString(secureIndex);
				int showGuestInt = modulesCursor.getInt(showGuestIndex);
				boolean showGuest = showGuestInt == 1 ? true : false;
				List<String> moduleRoles = getModuleRoles(ellucianApplication.getContentResolver(), moduleId);
				boolean lock = ellucianApplication.isUserAuthenticated() ? false : showLock(context, type, subType, secure, moduleRoles, moduleId);
				String rightText = null;
//				if(type.equals(ModuleType.NOTIFICATIONS)) {
//					rightText = notificationsCount;
//				}
				
				if (doesModuleShowForUser(ellucianApplication, moduleId, showGuest)) {
					currentChildCursor.addRow(new Object[] { "1", type, subType,
							name, iconUrl, moduleId, secure, lock, rightText });
				}

			} else {
				currentHeaderName = modulesCursor.getString(nameIndex);		

				currentChildCursor = new MatrixCursor(new String[] {
						BaseColumns._ID, Modules.MODULE_TYPE, Modules.MODULE_SUB_TYPE, Modules.MODULE_NAME,
						Modules.MODULES_ICON_URL, Modules.MODULES_ID,
						Modules.MODULE_SECURE, Modules.MODULE_LOCK, Modules.MODULE_RIGHT_TEXT });

			}


			// These variables will be used to keep the correct cursor BaseColumns._ID
			int currentHeaderRows = headersCursor.getCount();
			int currentChildRows = currentChildCursor.getCount();

			// After the first row is set go through each module and either put it in the 
			// headers cursor or the corresponding child cursor
			while (modulesCursor.moveToNext()) {
				type = modulesCursor.getString(typeIndex);
				if (type.equals(ModuleType.HEADER)) {

					// Only had the last header and child cursor if the child is not empty
					if (currentChildCursor != null && currentChildCursor.getCount() > 0) {
						
						headersCursor.addRow(new Object[] { "" + currentHeaderRows++, ModuleType.HEADER, null,
								currentHeaderName, null, R.drawable.menu_header_endcap, false, 1 });
						
						childCursorList.add(currentChildCursor);
						currentChildCursor = new MatrixCursor(new String[] {
								BaseColumns._ID, Modules.MODULE_TYPE, Modules.MODULE_SUB_TYPE, Modules.MODULE_NAME,
								Modules.MODULES_ICON_URL, Modules.MODULES_ID,
								Modules.MODULE_SECURE, Modules.MODULE_LOCK, Modules.MODULE_RIGHT_TEXT });
						currentChildRows = 0;
					}
					
					// reset name for next header
					currentHeaderName = modulesCursor.getString(nameIndex);			

				} else {
					// Non-header modules will be transferred from the module cursor to a sub child cursor
					// to be used by the adapter
					String subType = modulesCursor.getString(subTypeIndex);
					String name = modulesCursor.getString(nameIndex);
					String iconUrl = modulesCursor.getString(iconUrlIndex);
					String moduleId = modulesCursor.getString(moduleIdIndex);
					String secure = modulesCursor.getString(secureIndex);
					int showGuestInt = modulesCursor.getInt(showGuestIndex);
					boolean showGuest = showGuestInt == 1 ? true : false;
					List<String> moduleRoles = getModuleRoles(ellucianApplication.getContentResolver(), moduleId);
					boolean lock = ellucianApplication.isUserAuthenticated() ? false : showLock(context, type, subType, secure, moduleRoles, moduleId);
					String rightText = null;
//					if(type.equals(ModuleType.NOTIFICATIONS)) {
//						rightText = notificationsCount;
//					}

					if (doesModuleShowForUser(ellucianApplication, moduleId, showGuest)) {
						currentChildCursor.addRow(new Object[] { "" + currentChildRows++, type, subType,
								name, iconUrl, moduleId, secure, lock, rightText });
					}

				}
			}

			// Add last header and child if the child is not empty
			if (currentChildCursor != null && currentChildCursor.getCount() > 0) {
				
				headersCursor.addRow(new Object[] { "" + currentHeaderRows++, ModuleType.HEADER, null,
						currentHeaderName, null, R.drawable.menu_header_endcap, false, 1 });
				childCursorList.add(currentChildCursor);
			}

		}
		
		modulesCursor.close();

		// Add Actions header
		headersCursor.addRow(new Object[] { "" + totalRows++, ModuleType.HEADER, null,
				context.getString(R.string.menu_header_actions), null,
				R.drawable.menu_header_endcap, false, 0 });

		// Add actions child cursor to the childCursorList
		MatrixCursor actionsCursor = new MatrixCursor(new String[] { BaseColumns._ID,
				Modules.MODULE_TYPE, Modules.MODULE_SUB_TYPE, Modules.MODULE_NAME,
				Modules.MODULES_ICON_URL, ModuleMenuAdapter.IMAGE_RESOURCE,
				Modules.MODULE_SECURE, Modules.MODULE_LOCK, Modules.MODULE_RIGHT_TEXT });
		actionsCursor.addRow(new Object[] { "" + totalRows++, ModuleType._HOME, null,
				context.getString(R.string.menu_home), null,
				R.drawable.menu_home, false, false, null });

		String aboutIconUrl = Utils.getStringFromPreferences(context,
				Utils.APPEARANCE, AboutActivity.PREFERENCES_ICON, null);
		actionsCursor.addRow(new Object[] { "" + totalRows++, ModuleType._ABOUT, null,
				context.getString(R.string.menu_about), aboutIconUrl, null,
				false, false, null });
		if (((EllucianApplication)context.getApplicationContext()).getConfigurationProperties().allowSwitchSchool) {
			actionsCursor.addRow(new Object[] { "" + totalRows++, ModuleType._SWITCH_SCHOOLS, null,
					context.getString(R.string.menu_switch_school), null,
					R.drawable.menu_switch_schools, false, false, null });
		}
		actionsCursor.addRow(new Object[] { "" + totalRows++, ModuleType._SIGN_IN, null,
				context.getString(R.string.menu_sign_in), null,
				R.drawable.menu_sign_in, false, false, null });

		childCursorList.add(actionsCursor);

		return new ModuleMenuAdapter(context, headersCursor, childCursorList);
	}

	public static boolean doesModuleShowForUser(EllucianApplication ellucianApp, String moduleId, boolean showGuest) {
		boolean showModule = false;
		
		List<String> moduleRoles = getModuleRoles(ellucianApp.getContentResolver(), moduleId);
		if(moduleRoles.size() == 0) { //3.0 upgrade compatibility
			return true;
		}
		if (ellucianApp.isUserAuthenticated()) {
			List<String> userRoles = ellucianApp.getAppUserRoles();

			if (moduleRoles != null) {
				if (moduleRoles.contains(MODULE_ROLE_EVERYONE)) {
					showModule = true;
				} else if (userRoles != null) {
					showModule = doesUserHaveAccessForRole(userRoles, moduleRoles);
				}
			}
		} else if (showGuest) {
			showModule = true;
		}
		return showModule;
	}
	
	public static List<String> getModuleRoles(ContentResolver resolver, String moduleId) {
		List<String> roles = new ArrayList<String>();
		Cursor moduleRolesCursor = resolver.query(ModulesRoles.CONTENT_URI,
				new String[] {ModulesRoles.MODULE_ROLES_NAME }, 
				Modules.MODULES_ID + " = ?",
				new String[] {moduleId},
				ModulesRoles.DEFAULT_SORT);
		if (moduleRolesCursor != null) {
			while (moduleRolesCursor.moveToNext()) {
				roles.add(moduleRolesCursor.getString(
						moduleRolesCursor.getColumnIndex(ModulesRoles.MODULE_ROLES_NAME)));
			}
		}
		moduleRolesCursor.close();
		return roles;
	}
	
	public static boolean doesUserHaveAccessForRole(List<String> userRoles, List<String> moduleRoles) {
		if (userRoles == null || moduleRoles == null) {
			return false;
		}
		for (String userRole : userRoles) {
			if (moduleRoles.contains(userRole)) {
				return true;
			}
		}
		return false;
		
	}
	
	public static boolean showLock(Context context, String type, String subType, String secureString,
									List<String> moduleRoles, String moduleId) {

		if (moduleRoles.size() > 0) {
			if (moduleRoles.size() == 1
					&& moduleRoles.get(0).equals(MODULE_ROLE_EVERYONE)) {
				// fallthrough
			} else {
				return true;
			}
		}

		if (type.equals(ModuleType.WEB) && secureString != null) {
			return Boolean.parseBoolean(secureString);
		} else if (type.equals(ModuleType.CUSTOM)) {
			return Utils.isAuthenticationNeededForSubType(context, subType);
        } else if (type.equals(ModuleType.DIRECTORY)) {
            return Utils.isAuthenticationNeededForDirectory(context.getContentResolver(), moduleId);
        } else {
			return Utils.isAuthenticationNeededForType(type);
		}
	}

	@Override
	protected Cursor getChildrenCursor(Cursor groupCursor) {
		int groupPosition = groupCursor.getPosition();
		return childCursorList.get(groupPosition);	
	}

    @Override
	public void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
		HeaderViewHolder holder = (HeaderViewHolder) view.getTag();

		String label = cursor.getString(cursor
				.getColumnIndex(Modules.MODULE_NAME));

        Drawable endcapDrawable = Utils.getDrawableHelper(context, R.drawable.menu_header_endcap);

		holder.textView.setText(label);
		holder.iconView.setImageDrawable(endcapDrawable);
		
		int columnIndex = cursor.getColumnIndex(HEADER_COLLAPSIBLE);
		
		boolean collapsible = cursor.getInt(columnIndex) == 1 ?
				true : false;
		
		if (collapsible ) {
			Drawable indicatorDrawable;
			
			if (isExpanded) {
                indicatorDrawable = Utils.getDrawableHelper(context, R.drawable.menu_arrow_up);
			} else {
                indicatorDrawable = Utils.getDrawableHelper(context, R.drawable.menu_arrow_down);
			}
			
			holder.indicatorView.setVisibility(View.VISIBLE);
			holder.indicatorView.setImageDrawable(indicatorDrawable);
		} else {
			holder.indicatorView.setVisibility(View.GONE);
		}
		
	}

    @Override
	public void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
		RowViewHolder holder = (RowViewHolder) view.getTag();
		
		String label = cursor.getString(cursor
				.getColumnIndex(Modules.MODULE_NAME));
		String type = cursor.getString(cursor
				.getColumnIndex(Modules.MODULE_TYPE));
		String rightLabelText = cursor.getString(cursor.getColumnIndex(Modules.MODULE_RIGHT_TEXT));
		
		String lockString = cursor.getString(cursor
				.getColumnIndex(Modules.MODULE_LOCK));

		if ( lockString != null && Boolean.parseBoolean(lockString)) {
			holder.lockView.setVisibility(View.VISIBLE);
            holder.lockView.setImageDrawable(Utils.getDrawableHelper(context, R.drawable.menu_lock_icon));
		} else {
			holder.lockView.setVisibility(View.GONE);
		}
		
		if(TextUtils.isEmpty(rightLabelText)) {
			holder.rightTextView.setVisibility(View.GONE);
		} else {
			holder.rightTextView.setVisibility(View.VISIBLE);
			holder.rightTextView.setText(rightLabelText);
		}

		Drawable drawable = null;

		if (type.equals(ModuleType._SIGN_IN)) {
			EllucianApplication ellucianApp = (EllucianApplication) context
					.getApplicationContext();

			label = ellucianApp.isUserAuthenticated() ? context
					.getString(R.string.menu_sign_out) : context
					.getString(R.string.menu_sign_in);

			view.setVisibility(View.VISIBLE);
			int iconUrlIndex = cursor.getColumnIndex(Modules.MODULES_ICON_URL);

			if (iconUrlIndex > -1) {
				String iconUrl = cursor.getString(iconUrlIndex);
				if (!TextUtils.isEmpty(iconUrl))
					drawable = getIcon(context, iconUrl);
			}

			int drawableIndex = cursor.getColumnIndex(IMAGE_RESOURCE);
			if (drawableIndex > -1) {
				int res = cursor.getInt(drawableIndex);
				if (res > 0) {
                    drawable = Utils.getDrawableHelper(context, res);
                }
			}
		} else {
			
			boolean typeExists = false;
			for (String moduleType : ModuleType.ALL_WITH_INTERNAL) {
				if (type.equals(moduleType)) {
					typeExists = true;
					break;
				}
			}
		
			if (typeExists) {
				view.setVisibility(View.VISIBLE);
				int iconUrlIndex = cursor
						.getColumnIndex(Modules.MODULES_ICON_URL);

				if (iconUrlIndex > -1) {
					String iconUrl = cursor.getString(iconUrlIndex);
					if (!TextUtils.isEmpty(iconUrl))
						drawable = getIcon(context, iconUrl);
				}

				int drawableIndex = cursor.getColumnIndex(IMAGE_RESOURCE);
				if (drawableIndex > -1) {
					int res = cursor.getInt(drawableIndex);
					if (res > 0) {
                        drawable = Utils.getDrawableHelper(context, res);
                    }
				}

			}
			
		}

		holder.textView.setText(label);
		holder.iconView.setImageDrawable(drawable);
	}
	
	@Override
	public View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
		HeaderViewHolder holder = new HeaderViewHolder();
		View v = null;

		v = inflater.inflate(R.layout.drawer_list_header_item, parent,
					false);
		holder.textView = (TextView) v
					.findViewById(R.id.drawer_list_item_label);
		holder.iconView = (ImageView) v
					.findViewById(R.id.drawer_list_item_image);
		holder.indicatorView = (ImageView) v
				.findViewById(R.id.drawer_list_indicator);

		v.setTag(holder);
		return v;
	}

	@Override
	public View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
		RowViewHolder holder = new RowViewHolder();
		View v = null;

		v = inflater.inflate(R.layout.drawer_list_item, parent, false);
		holder.textView = (TextView) v
				.findViewById(R.id.drawer_list_item_label);
		holder.iconView = (ImageView) v
				.findViewById(R.id.drawer_list_item_image);
		holder.rightTextView = (TextView) v.findViewById(R.id.drawer_list_item_right_text);
		holder.lockView = (ImageView) v.findViewById(R.id.drawer_list_item_lock_image);
		v.setTag(holder);
		return v;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}
		
	private static class HeaderViewHolder {
		public ImageView iconView;
		public TextView textView;
		public ImageView indicatorView;
		
	}
	
	private static class RowViewHolder {
		public ImageView iconView;
		public TextView textView;
		public TextView rightTextView;
		public ImageView lockView;
	}

	public static Drawable getIcon(Context context, String iconUrl) {

		if (!TextUtils.isEmpty(iconUrl)) {
			AQuery aq = new AQuery(context);
			Bitmap bit = aq.getCachedImage(iconUrl);
			if (bit != null) {
				Drawable draw = new BitmapDrawable(context.getResources(), bit);
				return draw;
			}
		}
		return null;
	}

	public static Intent getIntent(Context context, String type, String subType,
			String moduleName, String moduleId) {

		HashMap<String, String> moduleProperties = new HashMap<String, String>();
		if (moduleId != null) {

			Cursor propertiesCursor = context.getContentResolver().query(
					ModulesProperties.CONTENT_URI, null, Modules.MODULES_ID + "=?",
					new String[] { moduleId }, null);
			if (propertiesCursor.moveToFirst()) {
				do {
					String key = propertiesCursor.getString(propertiesCursor
							.getColumnIndex(ModulesProperties.MODULE_PROPERTIES_NAME));
					String value = propertiesCursor.getString(propertiesCursor
							.getColumnIndex(ModulesProperties.MODULE_PROPERTIES_VALUE));
					if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
						moduleProperties.put(key, value);
					}
				} while (propertiesCursor.moveToNext());
			}
			propertiesCursor.close();
		}
		Intent intent = new Intent();

		if (!TextUtils.isEmpty(moduleId)) {
			intent.putExtra(Extra.MODULE_ID, moduleId);
		}
		if (!TextUtils.isEmpty(moduleName)) {
			intent.putExtra(Extra.MODULE_NAME, moduleName);
		}

        if (type.equals(ModuleType.APP_LAUNCHER)) {
            String appLauncherUrl = moduleProperties.get(APP_LAUNCHER_URL);
            String appStoreUrl = moduleProperties.get(APP_STORE_URL);
            Log.d(TAG, "appLauncherUrl:" + appLauncherUrl);
            Log.d(TAG, "appStoreUrl:" + appStoreUrl);

            if (TextUtils.isEmpty(appLauncherUrl)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.app_launcher_unsupported_text)
                        .setTitle(R.string.app_launcher_unsupported_title);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return null;
            } else {
                Intent launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(appLauncherUrl));
                launchIntent.putExtra(Extra.APP_LAUNCHER_STORE_URL, appStoreUrl);
                return launchIntent;
            }
        } else if (type.equals(ModuleType.AUDIO)) {
			intent.setClass(context, AudioActivity.class);

			String audioUrl = moduleProperties.get("audio");
			if (!TextUtils.isEmpty(audioUrl)) {
				intent.putExtra(Extra.AUDIO_URL, audioUrl);
			}
			String imageUrl = moduleProperties.get("image");
			if (!TextUtils.isEmpty(imageUrl)) {
				intent.putExtra(Extra.IMAGE_URL, imageUrl);
			}
			String text = moduleProperties.get("description");
			if (!TextUtils.isEmpty(text)) {
				intent.putExtra(Extra.CONTENT, text);
			}

			return intent;
		} else if (type.equals(ModuleType.COURSES)) {
			intent.setClass(context, CoursesDailyScheduleActivity.class);
			String requestUrl = moduleProperties.get("daily");
			if (!TextUtils.isEmpty(requestUrl)) {
				intent.putExtra(Extra.REQUEST_URL, requestUrl);
			}
			String fullUrl = moduleProperties.get("full");
			if (!TextUtils.isEmpty(fullUrl)) {
				intent.putExtra(Extra.COURSES_FULL_URL, fullUrl);
			}
			String overviewUrl = moduleProperties.get("overview");
			if (!TextUtils.isEmpty(overviewUrl)) {
				intent.putExtra(Extra.COURSES_DETAILS_URL, overviewUrl);
			}
			String rosterUrl = moduleProperties.get("roster");
			if (!TextUtils.isEmpty(rosterUrl)) {
				intent.putExtra(Extra.COURSES_ROSTER_URL, rosterUrl);
			}
			String gradesUrl = moduleProperties.get("grades");
			if (!TextUtils.isEmpty(gradesUrl)) {
				intent.putExtra(Extra.COURSES_GRADES_URL, gradesUrl);
			}
			String ilpUrl = moduleProperties.get("ilp");
			if (!TextUtils.isEmpty(ilpUrl)) {
				intent.putExtra(Extra.COURSES_ILP_URL, ilpUrl);
			}

			return intent;
		} else if (type.equals(ModuleType.DIRECTORY)) {
			intent.setClass(context, DirectoryActivity.class);

			// Set in preferences that the directory module is present
			Utils.addBooleanToPreferences(context, Utils.CONFIGURATION,
					Utils.DIRECTORY_PRESENT, true);

            String directoryModuleVersion = moduleProperties.get(ModuleMenuAdapter.DIRECTORY_MODULE_VERSION);
            if (!TextUtils.isEmpty(directoryModuleVersion)) {
                intent.putExtra(Extra.DIRECTORY_MODULE_VERSION, directoryModuleVersion);
            }
			return intent;
		} else if (type.equals(ModuleType.EVENTS)) {
            intent.setClass(context, EventsActivity.class);

            String requestUrl = moduleProperties.get("events");
            if (!TextUtils.isEmpty(requestUrl)) {
                intent.putExtra(Extra.REQUEST_URL, requestUrl);
            }

            return intent;
        } else if (type.equals(ModuleType.STUDENT_FINANCIALS)) {
            intent.setClass(context, FinancesActivity.class);

            String requestUrl = moduleProperties.get("financials");
            String externalLinkUrl = moduleProperties.get("externalLinkUrl");
            String externalLinkLabel = moduleProperties.get("externalLinkLabel");
            String externalBrowser = moduleProperties.get("external");
            if (!TextUtils.isEmpty(externalLinkLabel) && !TextUtils.isEmpty(externalLinkUrl)) {
                intent.putExtra(Extra.LINK_LABEL, externalLinkLabel);
                intent.putExtra(Extra.LINK, externalLinkUrl);
                if (!TextUtils.isEmpty(externalBrowser) && externalBrowser.equals("true")) {
                    intent.putExtra(Extra.LINK_EXTERNAL_BROWSER, true);
                }
            }
            if (!TextUtils.isEmpty(requestUrl)) {
                intent.putExtra(Extra.REQUEST_URL, requestUrl);
            }

            return intent;
		} else if (type.equals(ModuleType.FEED)) {
			intent.setClass(context, NewsActivity.class);

			String requestUrl = moduleProperties.get("feed");
			if (!TextUtils.isEmpty(requestUrl)) {
				intent.putExtra(Extra.REQUEST_URL, requestUrl);
			}

			return intent;
		} else if (type.equals(ModuleType.GRADES)) {
            intent.setClass(context, GradesActivity.class);

            String requestUrl = moduleProperties.get("grades");
            if (!TextUtils.isEmpty(requestUrl)) {
                intent.putExtra(Extra.REQUEST_URL, requestUrl);
            }

            return intent;
        } else if (type.equals(ModuleType.ILP)) {
            intent.setClass(context, IlpCardActivity.class);

            String ilpUrl = moduleProperties.get("ilp");
            if (!TextUtils.isEmpty(ilpUrl)) {
                intent.putExtra(Extra.COURSES_ILP_URL, ilpUrl);
            }

            return intent;
        } else if (type.equals(ModuleType.MAPS)) {
			intent.setClass(context, MapsActivity.class);
			String campusesUrl = moduleProperties.get("campuses");
			if (!TextUtils.isEmpty(campusesUrl)) {
				intent.putExtra(Extra.MAPS_CAMPUSES_URL, campusesUrl);
			}
			String buildingsUrl = moduleProperties.get("buildings");
			if (!TextUtils.isEmpty(buildingsUrl)) {
				intent.putExtra(Extra.MAPS_BUILDINGS_URL, buildingsUrl);
			}

			// Set in preferences that the map module is present
			Utils.addBooleanToPreferences(context, Utils.CONFIGURATION,
					Utils.MAP_PRESENT, true);

			return intent;
		} else if (type.equals(ModuleType.NOTIFICATIONS)) {
			intent.setClass(context, NotificationsActivity.class);
			
			String requestUrl = moduleProperties.get("notifications");
			if (!TextUtils.isEmpty(requestUrl)) {
				intent.putExtra(Extra.REQUEST_URL, requestUrl);
			}
			
			String mobileNotificationsUrl = moduleProperties.get("mobilenotifications");
			if (!TextUtils.isEmpty(mobileNotificationsUrl)) {
				intent.putExtra(Extra.NOTIFICATIONS_MOBILE_URL, mobileNotificationsUrl);
			}
			return intent;
		} else if (type.equals(ModuleType.NUMBERS)) {
			intent.setClass(context, NumbersListActivity.class);

			String requestUrl = moduleProperties.get("numbers");
			if (!TextUtils.isEmpty(requestUrl)) {
				intent.putExtra(Extra.REQUEST_URL, requestUrl);
			}

			return intent;
		} else if (type.equals(ModuleType.REGISTRATION)) {
			intent.setClass(context, RegistrationActivity.class);
			
			String requestUrl = moduleProperties.get("registration");
			if (!TextUtils.isEmpty(requestUrl)) {
				intent.putExtra(Extra.REQUEST_URL, requestUrl);
			}
			
			if (moduleProperties.containsKey(PLANNING_TOOL) && moduleProperties.get(PLANNING_TOOL).equals("true")) {
				intent.putExtra(PLANNING_TOOL, true);
			}
			
			return intent;
		} else if (type.equals(ModuleType.VIDEO)) {
			intent.setClass(context, VideoActivity.class);

			String videoUrl = moduleProperties.get("video");
			if (!TextUtils.isEmpty(videoUrl)) {
				intent.putExtra(Extra.VIDEO_URL, videoUrl);
			}
			String text = moduleProperties.get("description");
			if (!TextUtils.isEmpty(text)) {
				intent.putExtra(Extra.CONTENT, text);
			}

			return intent;
		} else if (type.equals(ModuleType.WEB)) {
			String url = moduleProperties.get("url");
			// If external flag is present send to external browser
			if (moduleProperties.containsKey(EXTERNAL_WEB_BROWSER) && moduleProperties.get(EXTERNAL_WEB_BROWSER).equals("true")) {
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
			} else {
				intent.setClass(context, WebframeActivity.class);
				if (!TextUtils.isEmpty(url)) {
					intent.putExtra(Extra.REQUEST_URL, url);
				}
			}
			return intent;			
		} else if (type.equals(ModuleType.CUSTOM)) { 			
			return getCustomIntent(context, type, subType, moduleName, moduleId, moduleProperties);
			
		} else if (type.equals(ModuleType._HOME)) {
			intent.setClass(context, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			return intent;
		} else if (type.equals(ModuleType._ABOUT)) {
			intent.setClass(context, AboutActivity.class);
			return intent;
		} else if (type.equals(ModuleType._SWITCH_SCHOOLS)) {
			intent.setClass(context, SchoolSelectionActivity.class);
			return intent;
		} else if (type.equals(ModuleType._SIGN_IN)) {
			return null;
		} else {
			return null;
		}

	}
	
	private static Intent getCustomIntent(Context context, String type, String subType, String moduleName,
										  String moduleId, HashMap<String, String> moduleProperties) {
		
		if (!TextUtils.isEmpty(subType)) {
			
			EllucianApplication ellucianApp = (EllucianApplication) context.getApplicationContext();	
			// Find the module configuration for the custom-type set in the Cloud Application
			ModuleConfiguration moduleConfig = ellucianApp.findModuleConfig(subType);
			
			if (moduleConfig != null) {
				Log.d(TAG, "Module config found: " + moduleConfig.configType);
				if (moduleConfig.isValid()) {			
					Log.d(TAG, "Module config is valid, building intent");

					String fullClassPath = moduleConfig.packageName + "." + moduleConfig.activityName;
					
					// Parsing a class from the class path to create intent
					Class<? extends EllucianActivity> activityClass = null;
					try {
						activityClass = Class.forName(fullClassPath).asSubclass(EllucianActivity.class);
					} catch (ClassNotFoundException e) {
						Log.e(TAG, "ClassNotFoundException: ", e);
						return null;
					}
					
					Intent intent = new Intent(context, activityClass);
					
					// Sending default variables to the activity, these are set in the EllucianActivity
					if (!TextUtils.isEmpty(moduleId)) {
						intent.putExtra(Extra.MODULE_ID, moduleId);
					}
					if (!TextUtils.isEmpty(moduleName)) {
						intent.putExtra(Extra.MODULE_NAME, moduleName);
					}
					
					// Adding values from the module configuration xml
					if (moduleConfig != null) {
						for (Entry<String, String> entry : moduleConfig.intentExtras.entrySet()) {
							Log.d(TAG, "Adding extra to intent; key: " + entry.getKey() + ", value: " + entry.getValue());
						    intent.putExtra(entry.getKey(), entry.getValue());
						}
					}
					
					// Adding values from the cloud in the form of intent extras 
					// any value with the same name in both xml and from the cloud will be overridden by the cloud values
					if (moduleProperties != null) {
						for (Entry<String, String> entry : moduleProperties.entrySet()) {
							Log.d(TAG, "Adding extra to intent; key: " + entry.getKey() + ", value: " + entry.getValue());
							intent.putExtra(entry.getKey(), entry.getValue());						
						}
					}
					
					// Add flags if any
					for (Integer flag : moduleConfig.intentFlags) {
						intent.addFlags(flag);
					}
					
					return intent;
				} else {
					Log.e(TAG, "Module config is not valid");
				}	
			} else {
				Log.e(TAG, "Module config not found");
			}	
		} else {
			Log.e(TAG, "Custom type not found");
		}
			
		return null;
		
	}
}
