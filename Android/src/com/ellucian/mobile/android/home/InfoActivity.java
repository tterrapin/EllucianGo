package com.ellucian.mobile.android.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.Utils;
import com.ellucian.mobile.android.configuration.Configuration;

public class InfoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_LEFT_ICON);

		setContentView(R.layout.info);

		final Configuration configuration = ((EllucianApplication) getApplication())
				.getConfiguration();

		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.ic_dialog_info);

		boolean hideHelpLabel = true;

		final TextView version = (TextView) findViewById(R.id.version);
		try {
			final PackageInfo packageInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			version.setText(packageInfo.versionName);
		} catch (final PackageManager.NameNotFoundException e) {
			version.setVisibility(View.INVISIBLE);
			final TextView versionLabel = (TextView) findViewById(R.id.versionLabel);
			versionLabel.setVisibility(View.INVISIBLE);
		}
		final TextView schoolName = (TextView) findViewById(R.id.schoolName);

		final SharedPreferences preferences = getSharedPreferences(
				HomeActivity.CONFIGURATION, Context.MODE_PRIVATE);
		final String displayName = preferences.getString("displayName", "");
		schoolName.setText(displayName);

		final TextView schoolAddress = (TextView) findViewById(R.id.schoolAddress);
		final String address = configuration.getAddress();
		if (address != null && address.length() > 0) {
			schoolAddress.setText(configuration.getAddress());
		} else {
			schoolAddress.setVisibility(View.GONE);
		}

		final Button websiteButton = (Button) findViewById(R.id.websiteButton);
		websiteButton.setText(configuration.getHelpdeskWebsiteLabel());
		final String website = configuration.getHelpdeskWebsite();
		if (website != null && website.length() > 0) {
			final Uri data = Uri.parse(website);
			final Intent websiteIntent = new Intent(
					Intent.ACTION_VIEW, data);
			if (Utils.isIntentAvailable(this, websiteIntent)) {
				hideHelpLabel = false;
				websiteButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						
						startActivity(websiteIntent);
					}
				});
			} else {
				websiteButton.setVisibility(View.GONE);
			}
		} else {
			websiteButton.setVisibility(View.GONE);
		}

		final Button emailButton = (Button) findViewById(R.id.emailButton);
		emailButton.setText(configuration.getHelpdeskEmailLabel());
		final String email = configuration.getHelpdeskEmail();
		if (email != null && email.length() > 0) {
			final Uri data = Uri.parse("mailto:" + email);
			final Intent emailIntent = new Intent(
					android.content.Intent.ACTION_SENDTO, data);
			emailIntent.setDataAndType(data, null);
			if (Utils.isIntentAvailable(this, emailIntent)) {
				hideHelpLabel = false;
				emailButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						
						startActivity(Intent.createChooser(emailIntent,
								"Email:"));
					}
				});
			} else {
				emailButton.setVisibility(View.GONE);
			}
		} else {
			emailButton.setVisibility(View.GONE);
		}

		final Button callButton = (Button) findViewById(R.id.callButton);
		callButton.setText(configuration.getHelpdeskPhoneLabel());
		final String call = configuration.getHelpdeskPhone();
		if (call != null && call.length() > 0) {
			final Uri data = Uri.parse("tel:" + call);
			final Intent intent = new Intent(Intent.ACTION_CALL);
			intent.setData(data);
			if (Utils.isIntentAvailable(this, intent)) {
				hideHelpLabel = false;
				callButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						
						startActivity(intent);
					}
				});
			} else {
				callButton.setVisibility(View.GONE);
			}
		} else {
			callButton.setVisibility(View.GONE);
		}
		
		final Button tellFriendButton = (Button) findViewById(R.id.tellFriendButton);
		//String uriText = getResources().getString(R.string.tellFriendMailto);
	    Uri uri = Uri.parse("mailto:"); //Uri.parse(uriText);
		final Intent emailIntent = new Intent(
				android.content.Intent.ACTION_SENDTO, uri);
		emailIntent.setDataAndType(uri, null);
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.tellFriendSubject));
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.tellFriendText));
		if (Utils.isIntentAvailable(this, emailIntent)) {
			tellFriendButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					startActivity(Intent.createChooser(emailIntent,
							"Email:"));
				}
			});
		} else {
			tellFriendButton.setVisibility(View.GONE);
		}


		if (hideHelpLabel) {
			findViewById(R.id.helpLabel).setVisibility(View.GONE);
		}

	}
}
