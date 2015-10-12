/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.about;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.util.Utils;

public class AboutActivity extends EllucianActivity {
	
	public static final String PREFERENCES_CONTACT = "aboutContact";
	public static final String PREFERENCES_ICON = "aboutIcon";
	public static final String PREFERENCES_LOGO_URL_PHONE = "aboutLogoUrlPhone";
	public static final String PREFERENCES_LOGO_URL_TABLET = "aboutLogoUrlPhone";
	public static final String PREFERENCES_PHONE_NUMBER = "aboutPhoneNumber";
	public static final String PREFERENCES_PHONE_DISPLAY = "aboutPhoneDisplay";
	public static final String PREFERENCES_EMAIL_ADDRESS = "aboutEmailAddress";
	public static final String PREFERENCES_EMAIL_DISPLAY = "aboutEmailDisplay";
	public static final String PREFERENCES_WEBSITE_URL = "aboutWebsiteUrl";
	public static final String PREFERENCES_WEBSITE_DISPLAY = "aboutWebsiteDisplay";
	public static final String PREFERENCES_PRIVACY_URL = "aboutPrivacyUrl";
	public static final String PREFERENCES_PRIVACY_DISPLAY = "aboutPrivacyDisplay";
	public static final String PREFERENCES_VERSION_URL = "aboutVersionUrl";
	
	private String clickedPhoneNumber;
	private String clickedEmailAddress;
	private String clickedWebsiteUrl;
	private String clickedPrivacyUrl;
	
	private class RetrieveServerVersionTask extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... params) {
			MobileClient client = new MobileClient(AboutActivity.this);
			String version = client.getServerVersion(params[0]);
			return version;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (!TextUtils.isEmpty(result)) {
				Log.d("RetrieveServerVersionTask", "Version sent from server: " + result);
		        TextView serverDisplay = (TextView) findViewById(R.id.about_server_version);
		        serverDisplay.setText(result);
			} else {
				Log.d("RetrieveServerVersionTask", "Version information was not retrieved correctly: " + result);
			}
		}
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        
        /** Setting Colors **/
        // comment
        // no longer using these colors for background of layouts
        //View mainLayout = findViewById(R.id.about_main_layout);
        //mainLayout.setBackgroundColor(Utils.getAccentColor(this));
        
        //View scrollLayout = findViewById(R.id.about_scroll_linear_layout);
        //scrollLayout.setBackgroundColor(Utils.getAccentColor(this));
        
        View poweredByEllucianButton = findViewById(R.id.about_powered_by_ellucian_button);
        poweredByEllucianButton.setBackgroundColor(Utils.getPrimaryColor(this));
        
        View ellucianPrivacyButton = findViewById(R.id.about_ellucian_privacy_button);
        ellucianPrivacyButton.setBackgroundColor(Utils.getPrimaryColor(this));       
        
        /** Getting all About Info stored in SharedPreferences */
        SharedPreferences preferences = this.getSharedPreferences(Utils.APPEARANCE, MODE_PRIVATE);
        
        String configLogoUrlPhone = preferences.getString(PREFERENCES_LOGO_URL_PHONE, "");
        if (TextUtils.isEmpty(configLogoUrlPhone)) {
        	// No longer using default image, will show blank
        	//ImageView logo = (ImageView) findViewById(R.id.about_school_logo);
        	//logo.setImageResource(R.drawable.about_logo_ellucian_university_2x);
        } else {
        	AQuery aq = new AQuery(this);
        	aq.id(R.id.about_school_logo).image(configLogoUrlPhone);
        }
        // TODO - Add tablet support later
        //String configLogoUrlTablet = preferences.getString(PREFERENCES_LOGO_URL_TABLET, "");
        
        String configAppVersion = "";
        try {
            configAppVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e("tag", e.getMessage());
        }
        Resources r = getResources();

        String configServerVersionUrl = preferences.getString(PREFERENCES_VERSION_URL, "");
        String configPhoneNumber = preferences.getString(PREFERENCES_PHONE_NUMBER, "");
        String configPhoneLabel = r.getString(R.string.label_phone); 
        String configEmail = preferences.getString(PREFERENCES_EMAIL_ADDRESS, "");
        String configEmailLabel = r.getString(R.string.label_email); 
        String configWebsiteUrl = preferences.getString(PREFERENCES_WEBSITE_URL, "");
        String configWebsiteLabel = r.getString(R.string.label_website); 
        String configPrivacyUrl = preferences.getString(PREFERENCES_PRIVACY_URL, "");
        String configPrivacyLabel = preferences.getString(PREFERENCES_PRIVACY_DISPLAY, r.getString(R.string.about_privacy_label));
        String configExtraInformation = preferences.getString(PREFERENCES_CONTACT, "");
        
        
        TextView appVersion = (TextView) findViewById(R.id.about_app_version);
        if (!TextUtils.isEmpty(configAppVersion)) {
        	appVersion.setText(configAppVersion);   
        } else {
        	appVersion.setText("...");  
        }
            
        TextView serverVersion = (TextView) findViewById(R.id.about_server_version);
        serverVersion.setText("...");
        if (!TextUtils.isEmpty(configServerVersionUrl)) {
        	// Creating a new AsyncTask to get mobile server version to show on screen
            RetrieveServerVersionTask versionTask = new RetrieveServerVersionTask();
            versionTask.execute(configServerVersionUrl);
        }              
        
        if (!TextUtils.isEmpty(configPhoneNumber)) {
        	TextView phoneNumber = (TextView) findViewById(R.id.about_phone_number);
        	phoneNumber.setText(configPhoneNumber);
        	clickedPhoneNumber = configPhoneNumber;
        	TextView phoneLabel = (TextView) findViewById(R.id.about_phone_label);
        	phoneLabel.setText(configPhoneLabel);
        } else {
        	TableRow phoneRow = (TableRow) findViewById(R.id.about_phone_row);
        	phoneRow.setVisibility(View.GONE);
        }
        
        if (!TextUtils.isEmpty(configEmail)) {
        	TextView emailAddress = (TextView) findViewById(R.id.about_email_address);
        	emailAddress.setText(configEmail);
        	clickedEmailAddress = configEmail;
        	TextView emailLabel = (TextView) findViewById(R.id.about_email_label);
        	emailLabel.setText(configEmailLabel);
        } else {        	
        	TableRow emailRow = (TableRow) findViewById(R.id.about_email_row);
        	emailRow.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(configWebsiteUrl)) {
        	TextView websiteUrl = (TextView) findViewById(R.id.about_website_url);
        	websiteUrl.setText(configWebsiteUrl);
        	clickedWebsiteUrl = configWebsiteUrl;
        	TextView websiteLabel = (TextView) findViewById(R.id.about_website_label);
        	websiteLabel.setText(configWebsiteLabel);
        } else {       	
        	TableRow websiteRow = (TableRow) findViewById(R.id.about_website_row);
        	websiteRow.setVisibility(View.GONE);
        }        
        
        if (!TextUtils.isEmpty(configPrivacyUrl)) {
        	clickedPrivacyUrl = configPrivacyUrl;
        	TextView privacyLabel = (TextView) findViewById(R.id.about_privacy_label);
        	privacyLabel.setText(configPrivacyLabel);   	
        } else {
        	TableRow privacyRow = (TableRow) findViewById(R.id.about_privacy_row);
        	privacyRow.setVisibility(View.GONE); 
        }
        
        TextView extraInformation = (TextView) findViewById(R.id.about_extra_information);
        if (!TextUtils.isEmpty(configExtraInformation)) {    		
        	extraInformation.setAutoLinkMask(Utils.getAvailableLinkMasks(this, Linkify.MAP_ADDRESSES)); 
        	extraInformation.setText(configExtraInformation);	
        } else {
        	extraInformation.setVisibility(View.GONE);
        }	
                     
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
  
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    
  
    @SuppressWarnings("unused")
	public void callContact(View view) {
    	sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, "About Phone", null, null);
    	Uri uri = Uri.parse("tel:" + clickedPhoneNumber);
    	Intent intent = new Intent(Intent.ACTION_DIAL, uri);
    	if (Utils.isIntentAvailable(this, intent)) {
    		startActivity(intent);
    	}
    }
    
    @SuppressWarnings("unused")
	public void emailSupport(View view) {
    	sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, "About Email", null, null);
    	Uri uri = Uri.parse("mailto:" + clickedEmailAddress);
    	Intent intent = new Intent(Intent.ACTION_SENDTO);
    	intent.setData(uri);
    	if (Utils.isIntentAvailable(this, intent)) {
    		startActivity(intent);
    	} 
    }
    
    @SuppressWarnings("unused")
	public void goToUniversityWebsite(View view) {
    	sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_FOLLOW_WEB, "About Web", null, null);
    	Uri uri = Uri.parse(clickedWebsiteUrl);
    	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    	startActivity(intent);
    }
    
    @SuppressWarnings("unused")
	public void goToUniversityPrivacy(View view) {
    	sendView("School Privacy", null);
    	Uri uri = Uri.parse(clickedPrivacyUrl);
    	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    	startActivity(intent);
    }
    
    @SuppressWarnings("unused")
	public void goToEllucianHome(View view) {
    	sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, "About Text", null, null);
    	Uri uri = Uri.parse("http://www.ellucian.com");
    	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    	startActivity(intent);
    }
    
    @SuppressWarnings("unused")
	public void goToEllucianPrivacy(View view) {
    	sendView("Ellucian Privacy", null);
    	Uri uri = Uri.parse("http://www.ellucian.com/Privacy");
    	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    	startActivity(intent);
    }

	@Override
	protected void onStart() {
		super.onStart();
		sendView("About Page", null);
	}
    
    
}

