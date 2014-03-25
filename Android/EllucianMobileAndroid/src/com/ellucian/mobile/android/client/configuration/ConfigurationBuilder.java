package com.ellucian.mobile.android.client.configuration;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.ellucian.mobile.android.ModuleType;
import com.ellucian.mobile.android.adapter.ModuleMenuAdapter;
import com.ellucian.mobile.android.client.ContentProviderOperationBuilder;
import com.ellucian.mobile.android.directory.DirectoryCategoriesFragment;
import com.ellucian.mobile.android.provider.EllucianContract.Modules;
import com.ellucian.mobile.android.provider.EllucianContract.ModulesProperties;

public class ConfigurationBuilder extends ContentProviderOperationBuilder<JSONObject>{
	@SuppressWarnings("unused")
	private Context context;
	
	public ConfigurationBuilder(Context context) {
		super(context);
		this.context = context;
	}
	
	@Override
	public ArrayList<ContentProviderOperation> buildOperations(JSONObject mApps) {
		final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
		
		try {
			batch.add(ContentProviderOperation.newDelete(Modules.CONTENT_URI).build());
			batch.add(ContentProviderOperation.newDelete(ModulesProperties.CONTENT_URI).build());
			
			Iterator<?> iter = mApps.keys();
			while (iter.hasNext()) {
				String moduleId = (String) iter.next();
				JSONObject moduleObject = mApps.getJSONObject(moduleId);
				
				String icon = "";
				if (moduleObject.has("icon")) {
					icon = moduleObject.getString("icon");
				}
				 
				int index = moduleObject.getInt("order");
				String name = moduleObject.getString("name");
				
				//boolean guest = value.getBoolean("showGuest");
				String type = moduleObject.getString("type");
				
				String secure = null;
				if (moduleObject.has("secure")) {
					secure = moduleObject.getString("secure");
				}
				
				String subType = null;
				if (moduleObject.has("custom-type")) {
					subType = moduleObject.getString("custom-type");
				}
				
				batch.add(ContentProviderOperation
						.newInsert(Modules.CONTENT_URI)
						.withValue(Modules.MODULES_ID, moduleId)
						.withValue(Modules.MODULES_ICON_URL, icon)
						.withValue(Modules.MODULE_ORDER, index)
						.withValue(Modules.MODULE_NAME,name)
			//			.withValue(MApps.M_APPS_SHOW_FOR_GUEST, guest)
						.withValue(Modules.MODULE_TYPE, type)
						.withValue(Modules.MODULE_SUB_TYPE, subType)
						.withValue(Modules.MODULE_SECURE, secure)
						.build());
				if(moduleObject.has("urls")) {
					JSONObject urlsValues = moduleObject.getJSONObject("urls");
					Iterator<?>  urlsIter = urlsValues.keys();
					while(urlsIter.hasNext()) {
						String urlKey = (String)urlsIter.next();
						String urlValue = urlsValues.getString(urlKey);
						if(!TextUtils.isEmpty(urlKey) && !TextUtils.isEmpty(urlValue)) {
							batch.add(ContentProviderOperation
									.newInsert(ModulesProperties.CONTENT_URI)
									.withValue(Modules.MODULES_ID, moduleId)
									.withValue(ModulesProperties.MODULE_PROPERTIES_NAME, urlKey)
									.withValue(ModulesProperties.MODULE_PROPERTIES_VALUE, urlValue)
									.build());
						}
					}
				}
				
				if (type.equals(ModuleType.DIRECTORY)) {
					if (moduleObject.has("directories")) {
						String studentDirectoryVisible = "false";
						String facultyDirectoryVisible = "false";
						JSONObject directories = moduleObject.getJSONObject("directories");

						if (directories.has("student")) {							
							studentDirectoryVisible = directories.getString("student");
						}
						if (directories.has("faculty")) {
							facultyDirectoryVisible = directories.getString("faculty");
						}

						batch.add(ContentProviderOperation
								.newInsert(ModulesProperties.CONTENT_URI)
								.withValue(Modules.MODULES_ID, moduleId)
								.withValue(ModulesProperties.MODULE_PROPERTIES_NAME, DirectoryCategoriesFragment.DIRECTORY_STUDENT_VISIBLE)
								.withValue(ModulesProperties.MODULE_PROPERTIES_VALUE, studentDirectoryVisible)
								.build());
						batch.add(ContentProviderOperation
								.newInsert(ModulesProperties.CONTENT_URI)
								.withValue(Modules.MODULES_ID, moduleId)
								.withValue(ModulesProperties.MODULE_PROPERTIES_NAME, DirectoryCategoriesFragment.DIRECTORY_FACULTY_VISIBLE)
								.withValue(ModulesProperties.MODULE_PROPERTIES_VALUE, facultyDirectoryVisible)
								.build());

					}
				}
				
				if (type.equals(ModuleType.WEB)) {;
					if (moduleObject.has("external") && moduleObject.getString("external").equals("true")) {
						batch.add(ContentProviderOperation
								.newInsert(ModulesProperties.CONTENT_URI)
								.withValue(Modules.MODULES_ID, moduleId)
								.withValue(ModulesProperties.MODULE_PROPERTIES_NAME, ModuleMenuAdapter.EXTERNAL_WEB_BROWSER)
								.withValue(ModulesProperties.MODULE_PROPERTIES_VALUE, "true")
								.build());
					}	
				}
				
				if (type.equals(ModuleType.CUSTOM)) {
					
					if (moduleObject.has("parameters")) {
						JSONObject parametersObject = moduleObject.getJSONObject("parameters");
						Iterator<?>  parametersIter = parametersObject.keys();
						while(parametersIter.hasNext()) {
							String parameterKey = (String)parametersIter.next();
							String parameterValue = parametersObject.getString(parameterKey);
							if(!TextUtils.isEmpty(parameterKey) && !TextUtils.isEmpty(parameterValue)) {
								batch.add(ContentProviderOperation
										.newInsert(ModulesProperties.CONTENT_URI)
										.withValue(Modules.MODULES_ID, moduleId)
										.withValue(ModulesProperties.MODULE_PROPERTIES_NAME, parameterKey)
										.withValue(ModulesProperties.MODULE_PROPERTIES_VALUE, parameterValue)
										.build());
							}
						}
					}
				}
								
			}
				/*
				 * if(value.has("roles")) { String[] roles =
				 * value.getString("roles").split(","); for(String role : roles) {
				 * module.addRole(role); } }
				 */
			
		
		} catch (JSONException e){
			Log.e("ConfigurationBuilder", "JSONException:", e);
		}
		return batch;
	}
}
