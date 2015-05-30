/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.adapter.ModuleMenuAdapter;
import com.ellucian.mobile.android.app.EllucianActivity;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.services.AuthenticateUserIntentService;
import com.ellucian.mobile.android.login.LoginDialogFragment;
import com.ellucian.mobile.android.login.QueuedIntentHolder;
import com.ellucian.mobile.android.schoolselector.ConfigurationLoadingActivity;
import com.ellucian.mobile.android.schoolselector.SchoolSelectionActivity;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

import java.util.List;

public class MainActivity extends EllucianActivity {

    public static final String SHOW_LOGIN = "showLogin";
	public boolean useDefaultConfiguration; 
	public String defaultConfigurationUrl; 

	private Button signInButton;

	private MainAuthenticationReceiver mainAuthenticationReceiver;
	private BackgroundAuthenticationReceiver backgroundAuthenticationReceiver;

	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Setting fields from the configuration file. See res/xml/configuration_properties.xml
        useDefaultConfiguration = getConfigurationProperties().useDefaultConfiguration;
    	defaultConfigurationUrl = getConfigurationProperties().defaultConfigurationUrl;

    }
	
	@Override
	protected void onPause() {
		super.onPause();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(mainAuthenticationReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(backgroundAuthenticationReceiver);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		this.setTitle(R.string.title_home_page);

        String backgroundUrl = null;
		if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= 
		        Configuration.SCREENLAYOUT_SIZE_LARGE) {
			backgroundUrl = Utils.getStringFromPreferences(this, Utils.APPEARANCE, Utils.HOME_URL_TABLET, "");
		} 
		if (TextUtils.isEmpty(backgroundUrl)) {
			backgroundUrl = Utils.getStringFromPreferences(this, Utils.APPEARANCE, Utils.HOME_URL_PHONE, "");
		}
		
		String logoUrl = Utils.getStringFromPreferences(this, Utils.APPEARANCE, Utils.SCHOOL_LOGO_PHONE, "");
	
		Drawable backgroundImage = null;
		if (!TextUtils.isEmpty(backgroundUrl)) {
			AQuery aq = new AQuery(this);
			Bitmap bit = aq.getCachedImage(backgroundUrl);
			if (bit != null) {
				backgroundImage = new BitmapDrawable(getResources(), bit);			
			}
		}
		
		ImageView background = (ImageView)findViewById(R.id.home_background);
		if (backgroundImage != null) {
			background.setImageDrawable(backgroundImage);
		}
		
		Drawable logoImage = null;
		if (!TextUtils.isEmpty(logoUrl)) {
			AQuery aq = new AQuery(this);
			Bitmap bit = aq.getCachedImage(logoUrl);
			if (bit != null) {
				logoImage = new BitmapDrawable(getResources(), bit);	
			}
		}
		
		ImageView logo = (ImageView)findViewById(R.id.home_logo);
		if (logoImage != null) {
			logo.setImageDrawable(logoImage);
		}
		
		
		signInButton = (Button) findViewById(R.id.home_sign_in_button);
		//signInButton.setBackgroundColor(Utils.getPrimaryColor(this));
		
		 
		if (getEllucianApp().isUserAuthenticated()) {
			signInButton.setText(R.string.main_sign_out);
		}

        if (getIntent().getBooleanExtra(SHOW_LOGIN, false)) {
            getIntent().removeExtra(SHOW_LOGIN);

            if (getIntent().getParcelableExtra(QueuedIntentHolder.QUEUED_INTENT_HOLDER) != null) {
                QueuedIntentHolder qih = getIntent().getExtras().getParcelable(QueuedIntentHolder.QUEUED_INTENT_HOLDER);
                    String moduleId = qih.moduleId;
                    List<String> roles = null;
                    if(moduleId != null) {
                        roles = ModuleMenuAdapter.getModuleRoles(getContentResolver(), moduleId);
                    }

                    LoginDialogFragment loginFragment = new LoginDialogFragment();
                    loginFragment.queueIntent(qih.queuedIntent, roles);
                    loginFragment.show(getFragmentManager(), LoginDialogFragment.LOGIN_DIALOG);
            } else {
                showLoginDialog();
            }
        }

		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);

		mainAuthenticationReceiver = new MainAuthenticationReceiver();
		lbm.registerReceiver(mainAuthenticationReceiver, new IntentFilter(AuthenticateUserIntentService.ACTION_UPDATE_MAIN));
		
		backgroundAuthenticationReceiver = new BackgroundAuthenticationReceiver();
		lbm.registerReceiver(backgroundAuthenticationReceiver, new IntentFilter(AuthenticateUserIntentService.ACTION_BACKGROUND_AUTH));
		
	}
	
	@Override
	public void onStart() {
		super.onStart();
		configure();
		sendView("Show Home Screen", "");
	}

    protected void configure() {

		SharedPreferences preferences = getSharedPreferences(Utils.CONFIGURATION, MODE_PRIVATE);
		String configUrl = preferences.getString(Utils.CONFIGURATION_URL, null);

		if (configUrl == null && !useDefaultConfiguration) {
			showInstitutionSelector();
			finish();
		} else if (configUrl == null && useDefaultConfiguration) {
	        Intent intent = new Intent(this, ConfigurationLoadingActivity.class);
			intent.putExtra(Utils.CONFIGURATION_URL, defaultConfigurationUrl);
			startActivity(intent);
		}
	}

	private void showInstitutionSelector() {
		final Intent intentSetup = new Intent(MainActivity.this,
				SchoolSelectionActivity.class);
		startActivity(intentSetup);
	}

	public void showInstitutionSelector(View view) {
		showInstitutionSelector();
	}
	
		public void onSignInClick(View view) {
		if (getEllucianApp().isUserAuthenticated()) {
			sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_LOGOUT, "Home-Click Sign Out", null, null);
		} else {
			sendEvent(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_LOGIN, "Home-Click Sign In", null, null);
		}
		handleSignIn();
	}
	
	public void handleSignIn() {
		if (getEllucianApp().isUserAuthenticated()) {
			// Sign Out
			
			EllucianApplication ellucianApp = getEllucianApp();
			// This also removes saved users
			ellucianApp.removeAppUser(true);

			Toast signOutMessage = Toast.makeText(this, R.string.dialog_signed_out, Toast.LENGTH_LONG);
			signOutMessage.setGravity(Gravity.CENTER, 0, 0);
			signOutMessage.show();
			signInButton.setText(R.string.main_sign_in);
			
			
			// Reset back stack and menu
			ellucianApp.resetModuleMenuAdapter();
			Intent newIntent = new Intent(this, this.getClass());
			newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(newIntent);
		} else {
			// Sign In
			showLoginDialog();
		}
	}

	
	private void showLoginDialog() {
		LoginDialogFragment loginFragment = new LoginDialogFragment();
		loginFragment.show(getFragmentManager(), LoginDialogFragment.LOGIN_DIALOG);
	    
	}
	
	public class MainAuthenticationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent incomingIntent) {	
			String result = incomingIntent.getStringExtra(Extra.LOGIN_SUCCESS);
			if(!TextUtils.isEmpty(result) && result.equals(AuthenticateUserIntentService.ACTION_SUCCESS)) {
				signInButton.setText(R.string.main_sign_out);
			}
		}		
	}
	
	public class BackgroundAuthenticationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent incomingIntent) {	
			String result = incomingIntent.getStringExtra(Extra.LOGIN_SUCCESS);
			if(!TextUtils.isEmpty(result) && result.equals(AuthenticateUserIntentService.ACTION_SUCCESS)) {
				signInButton.setText(R.string.main_sign_out);
			} else {
				signInButton.setText(R.string.main_sign_in);	
			}	
			signInButton.invalidate();
		}		
	}

	@Override
	protected void onNewIntent (Intent intent) {
		if (getEllucianApp().isUserAuthenticated()) {
			signInButton.setText(R.string.main_sign_out);
		} else {
			signInButton.setText(R.string.main_sign_in);	
		}
	}
}
