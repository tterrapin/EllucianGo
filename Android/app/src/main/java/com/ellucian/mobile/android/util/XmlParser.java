/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.util;

import java.io.IOException;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.Log;

import com.ellucian.elluciango.R;

public class XmlParser {
	
	private static final String TAG = XmlParser.class.getSimpleName();
	
	private static final String CONFIGURATION_LIST_URL = "configuration-list-url";
	private static final String USE_DEFAULT_CONFIGURATION = "use-default-configuration";
	private static final String DEFAULT_CONFIGURATION_URL = "default-configuration-url";
	private static final String ENABLE_VERSION_CHECKING = "enable-version-checking";
	private static final String ALLOW_SWITCH_SCHOOL = "allow-switch-school";
	private static final String GCM_SENDER_ID = "gcm-sender-id";

	private static final String MODULE_CONFIGURATIONS = "module-configurations";
	private static final String PACKAGE = "package";
	private static final String ACTIVITY = "activity";
	private static final String INTENT_EXTRA = "intent-extra";
	private static final String INTENT_FLAG = "intent-flag";
	private static final String SECURE = "secure";
	
	
	
	public static ConfigurationProperties createConfigurationPropertiesFromXml(Context context) {
		
		ConfigurationProperties configPropertiesObject = new ConfigurationProperties();

		XmlResourceParser xmlParser = context.getResources().getXml(R.xml.configuration_properties);
		
		try {
			int eventType = xmlParser.getEventType();
			String currentTagName = null;

			while (eventType != XmlPullParser.END_DOCUMENT) {

				if (eventType == XmlPullParser.START_DOCUMENT) {
					Log.d(TAG, "Start document");
				} else if (eventType == XmlPullParser.START_TAG) {
								
					currentTagName = xmlParser.getName();
					Log.d(TAG, "Start tag " + currentTagName);
					
				} else if (eventType == XmlPullParser.END_TAG) {
					Log.d(TAG, "End tag " + xmlParser.getName());
				} else if (eventType == XmlPullParser.TEXT) {
					
					String currentText = xmlParser.getText();
					Log.d(TAG, "currentText: " + currentText);
					if (!TextUtils.isEmpty(currentText)) {
						
						if (currentTagName.equals(CONFIGURATION_LIST_URL)) {
							Log.d(TAG, "Setting " + CONFIGURATION_LIST_URL + ": " + currentText);
							configPropertiesObject.configurationListUrl = currentText;
							
						} else if (currentTagName.equals(USE_DEFAULT_CONFIGURATION)) {
							Log.d(TAG, "Setting " + USE_DEFAULT_CONFIGURATION + ": " + currentText);
							configPropertiesObject.useDefaultConfiguration = Boolean.parseBoolean(currentText);
							
						} else if (currentTagName.equals(DEFAULT_CONFIGURATION_URL)) {
							Log.d(TAG, "Setting " + DEFAULT_CONFIGURATION_URL + ": " + currentText);
							configPropertiesObject.defaultConfigurationUrl = currentText;
							
						} else if (currentTagName.equals(ENABLE_VERSION_CHECKING)) {
							Log.d(TAG, "Setting " + ENABLE_VERSION_CHECKING + ": " + currentText);
							configPropertiesObject.enableVersionChecking = Boolean.parseBoolean(currentText);
							
						} else if (currentTagName.equals(ALLOW_SWITCH_SCHOOL)) {
							Log.d(TAG, "Setting " + ALLOW_SWITCH_SCHOOL + ": " + currentText);
							configPropertiesObject.allowSwitchSchool = Boolean.parseBoolean(currentText);
							
						} else if (currentTagName.equals(GCM_SENDER_ID)) {
							Log.d(TAG, "Setting " + GCM_SENDER_ID + ": " + currentText);
							configPropertiesObject.gcmSenderId = currentText;
						}					
					}
				}
				
				eventType = xmlParser.next();
			}
			Log.d(TAG, "End document");
		} catch (XmlPullParserException e) {
			Log.e(TAG, "XmlPullParserException", e);
			return null;
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
			return null;
		}

		return configPropertiesObject;
		
	}
	
	public static HashMap<String, ModuleConfiguration> createModuleConfigMapFromXml(Context context, int targetXmlFileId) {
		HashMap<String, ModuleConfiguration> moduleConfigMap = new HashMap<String, ModuleConfiguration>();
		ModuleConfiguration currentConfigObject = null;

		// If targetXmlFileId is set to zero use the default xml file
		if (targetXmlFileId == 0) {
			targetXmlFileId = R.xml.custom_module_configurations;
		}
		XmlResourceParser xmlParser = context.getResources().getXml(targetXmlFileId);
		
		try {
			int eventType = xmlParser.getEventType();
			String currentTagName = null;
			String currentKey = null;
			while (eventType != XmlPullParser.END_DOCUMENT) {

				if (eventType == XmlPullParser.START_DOCUMENT) {
					Log.d(TAG, "Start document");
				} else if (eventType == XmlPullParser.START_TAG) {
					
					
					currentTagName = xmlParser.getName();
					Log.d(TAG, "Start tag " + currentTagName);
					
					if (isNewConfigStartTag(currentTagName)) {
						
						if (currentConfigObject != null) {
							// Add object to list before creating a new object
							// This is only happens if more than one module-configs are present
							moduleConfigMap.put(currentConfigObject.configType, currentConfigObject);
						}
						
						Log.d(TAG, "Creating new config object");
						currentConfigObject = new ModuleConfiguration();
						
						Log.d(TAG, "Setting configType: " + currentTagName);
						currentConfigObject.configType = currentTagName;
					}
					
					if (currentTagName.equals(INTENT_EXTRA)) {
						currentKey = xmlParser.getAttributeValue(null, "key");
					}

				} else if (eventType == XmlPullParser.END_TAG) {
					Log.d(TAG, "End tag " + xmlParser.getName());
				} else if (eventType == XmlPullParser.TEXT) {
					
					String currentText = xmlParser.getText();
					Log.d(TAG, "currentText: " + currentText);
					if (!TextUtils.isEmpty(currentText)) {
						
						if (currentTagName.equals(PACKAGE)) {
							Log.d(TAG, "Setting packName: " + currentText);
							currentConfigObject.packageName = currentText;
							
						} else if (currentTagName.equals(ACTIVITY)) {
							Log.d(TAG, "Setting activityName: " + currentText);
							currentConfigObject.activityName = currentText;
							
						} else if (currentTagName.equals(INTENT_EXTRA)) {
							Log.d(TAG, "Adding intent-extra - key: " + currentKey + ", value: " + currentText);
							if (!TextUtils.isEmpty(currentKey) && !TextUtils.isEmpty(currentText)) {
								Log.d(TAG, "Creating intent extra - key: " + currentKey + ", value: " + currentText);
								currentConfigObject.intentExtras.put(currentKey, currentText);
							}
							
						} else if (currentTagName.equals(INTENT_FLAG)) {
							int flag = 0;
							
							Log.d(TAG, "Adding intent-flag of: " + currentText);
							if (currentText.startsWith("0x")) {
								currentText = currentText.replaceFirst("0x", "");
								flag = Integer.parseInt(currentText, 16);
							} else {
								flag = Integer.parseInt(currentText);
							}
						
							currentConfigObject.intentFlags.add(flag);
						} else if (currentTagName.equals(SECURE)) {
							Log.d(TAG, "Setting secure: " + currentText);
							currentConfigObject.secure = Boolean.parseBoolean(currentText);
						}					
					}
				}
				
				eventType = xmlParser.next();
			}
			Log.d(TAG, "End document");
		} catch (XmlPullParserException e) {
			Log.e(TAG, "XmlPullParserException", e);
			return null;
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
			return null;
		}
		
		if (currentConfigObject != null) {
			// Add the last object created to list
			moduleConfigMap.put(currentConfigObject.configType, currentConfigObject);
		}

		return moduleConfigMap;
		
	}
	
	private static boolean isNewConfigStartTag(String tagName) {
		if (tagName.equals(MODULE_CONFIGURATIONS) || tagName.equals(PACKAGE) || tagName.equals(ACTIVITY) || 
				tagName.equals(INTENT_EXTRA) || tagName.equals(INTENT_FLAG) || tagName.equals(SECURE) ) {
			return false;
		}
		return true;
	}
	

}
