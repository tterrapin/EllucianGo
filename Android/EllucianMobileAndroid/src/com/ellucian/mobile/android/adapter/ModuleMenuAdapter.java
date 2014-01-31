package com.ellucian.mobile.android.adapter;

import java.util.HashMap;
import java.util.Map.Entry;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.ellucian.mobile.android.grades.GradesActivity;
import com.ellucian.mobile.android.maps.MapsActivity;
import com.ellucian.mobile.android.multimedia.AudioActivity;
import com.ellucian.mobile.android.multimedia.VideoActivity;
import com.ellucian.mobile.android.news.NewsActivity;
import com.ellucian.mobile.android.notifications.NotificationsActivity;
import com.ellucian.mobile.android.numbers.NumberListActivity;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;
import com.ellucian.mobile.android.provider.EllucianContract.ModulesProperties;
import com.ellucian.mobile.android.registration.RegistrationActivity;
import com.ellucian.mobile.android.schoolselector.SchoolSelectionActivity;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.ModuleConfiguration;
import com.ellucian.mobile.android.util.Utils;
import com.ellucian.mobile.android.webframe.WebframeActivity;

public class ModuleMenuAdapter extends CursorAdapter {
	
	public static final String TAG = ModuleMenuAdapter.class.getSimpleName();
	public static final String IMAGE_RESOURCE = "imageResource";
	public static final String EXTERNAL_WEB_BROWSER = "externalWebBrowser";

	public ModuleMenuAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
	}

	@Override
	public int getItemViewType(int position) {
		Cursor cursor = (Cursor) getItem(position);
		String type = cursor.getString(cursor
				.getColumnIndex(Modules.MODULE_TYPE));
		if (type.equals(ModuleType.HEADER)) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		String label = cursor.getString(cursor
				.getColumnIndex(Modules.MODULE_NAME));
		String type = cursor.getString(cursor
				.getColumnIndex(Modules.MODULE_TYPE));
		String subType = cursor.getString(cursor
				.getColumnIndex(Modules.MODULE_SUB_TYPE));
		int moduleIdIndex = cursor.getColumnIndex(Modules.MODULES_ID);
		String moduleId = null;
		if (moduleIdIndex > -1)
			moduleId = cursor.getString(moduleIdIndex);

		Drawable drawable = null;

		if (type.equals(ModuleType.HEADER)) {
			drawable = context.getResources().getDrawable(
					R.drawable.menu_header_endcap);
		} else if (type.equals(ModuleType._SIGN_IN)) {
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
				if (res > 0)
					drawable = context.getResources().getDrawable(res);
			}
		} else {

			Intent intent = ModuleMenuAdapter.getIntent(context, type, subType, label, moduleId);
		
			if (intent != null) {
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
					if (res > 0)
						drawable = context.getResources().getDrawable(res);
				}

			}
			
		}

		holder.textView.setText(label);
		holder.imageView.setImageDrawable(drawable);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		ViewHolder holder = new ViewHolder();
		View v = null;

		String type = cursor.getString(cursor
				.getColumnIndex(Modules.MODULE_TYPE));

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (type.equals(ModuleType.HEADER)) {
			v = inflater.inflate(R.layout.drawer_list_header_item, parent,
					false);
			holder.textView = (TextView) v
					.findViewById(R.id.drawer_list_item_label);
			holder.imageView = (ImageView) v
					.findViewById(R.id.drawer_list_item_image);
		} else {
			v = inflater.inflate(R.layout.drawer_list_item, parent, false);
			holder.textView = (TextView) v
					.findViewById(R.id.drawer_list_item_label);
			holder.imageView = (ImageView) v
					.findViewById(R.id.drawer_list_item_image);
		}

		v.setTag(holder);
		return v;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		Cursor cursor = (Cursor) getItem(position);
		String type = cursor.getString(cursor
				.getColumnIndex(Modules.MODULE_TYPE));

		return !type.equals(ModuleType.HEADER);
	}

	private static class ViewHolder {
		public TextView textView;
		public ImageView imageView;
	}

	private Drawable getIcon(Context context, String iconUrl) {

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

		if (type.equals(ModuleType.AUDIO)) {
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

			String requestUrl = moduleProperties.get("allSearch");
			if (!TextUtils.isEmpty(requestUrl)) {
				intent.putExtra(Extra.REQUEST_URL, requestUrl);
			}
			String facultyUrl = moduleProperties.get("facultySearch");
			if (!TextUtils.isEmpty(facultyUrl)) {
				intent.putExtra(Extra.DIRECTORY_FACULTY_URL, facultyUrl);
			}
			String studentUrl = moduleProperties.get("studentSearch");
			if (!TextUtils.isEmpty(studentUrl)) {
				intent.putExtra(Extra.DIRECTORY_STUDENT_URL, studentUrl);
			}
			// Set in preferences that the directory module is present
			Utils.addBooleanToPreferences(context, Utils.CONFIGURATION,
					Utils.DIRECTORY_PRESENT, true);

			return intent;
		} else if (type.equals(ModuleType.EVENTS)) {
			intent.setClass(context, EventsActivity.class);

			String requestUrl = moduleProperties.get("events");
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
			return intent;
		} else if (type.equals(ModuleType.NUMBERS)) {
			intent.setClass(context, NumberListActivity.class);

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
	
	public static Intent getCustomIntent(Context context, String type, String subType, String moduleName, 
			String moduleId, HashMap<String, String> moduleProperties ) {
		
		if (!TextUtils.isEmpty(subType)) {
			
			EllucianApplication ellucianApp = ((EllucianActivity)context).getEllucianApp();	
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
