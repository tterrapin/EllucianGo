package com.ellucian.mobile.android.login;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.app.EllucianDialogFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.services.AuthenticateUserIntentService;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;



public class LoginDialogFragment extends EllucianDialogFragment {
	public static final String LOGIN_DIALOG = "login_dialog";
	public AlertDialog loginDialog;
	private Intent queuedIntent;
	
	private MainAuthenticationReceiver mainAuthenticationReceiver;
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		final ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.fragment_login_dialog, null);
		
		TextView title = (TextView) dialogView.findViewById(R.id.login_dialog_title);
		title.setTextColor(Utils.getPrimaryColor(getActivity()));
		
		builder.setView(dialogView);
		
		loginDialog = builder.create();
		
		Button okButton = (Button)dialogView.findViewById(R.id.login_button_ok);
		okButton.setOnClickListener(
                 new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						arg0.setEnabled(false);
						Dialog loginDialog = getDialog();
						EditText usernameView =  (EditText) ((Dialog) loginDialog).findViewById(R.id.login_dialog_username);
						String username = usernameView.getText().toString();
						EditText passwordView =  (EditText) ((Dialog) loginDialog).findViewById(R.id.login_dialog_password);
						String password = passwordView.getText().toString();
						CheckBox staySignedIn = (CheckBox) ((Dialog) loginDialog).findViewById(R.id.login_dialog_checkbox);
						
						if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
							Toast emptyMessage = Toast.makeText(LoginDialogFragment.this.getActivity(), R.string.dialog_sign_in_empty, Toast.LENGTH_LONG);
							emptyMessage.setGravity(Gravity.CENTER, 0, 0);
							emptyMessage.show();
						} else {
							boolean staySignedInChecked = staySignedIn.isChecked();
							if(staySignedInChecked) {
								sendEvent(GoogleAnalyticsConstants.CATEGORY_AUTHENTICATION, GoogleAnalyticsConstants.ACTION_LOGIN, "Authentication with save credential", null, null);
							} else {
								sendEvent(GoogleAnalyticsConstants.CATEGORY_AUTHENTICATION, GoogleAnalyticsConstants.ACTION_LOGIN, "Authentication without save credential", null, null);
							}
							loginUser(username, password, staySignedInChecked);
						}
						
					}
                     
                 }
         );
		Button cancelButton = (Button)dialogView.findViewById(R.id.login_button_cancel);
		cancelButton.setOnClickListener(
                new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						sendEvent(GoogleAnalyticsConstants.CATEGORY_AUTHENTICATION, GoogleAnalyticsConstants.ACTION_CANCEL, "Click Cancel", null, null);
						view.setEnabled(false);
						loginDialog.cancel();
						// Make sure queue is empty in case of another login attempt
						clearQueuedIntent();
					}
                }
        );
		

		
		return loginDialog;
	}

	@Override
	public void onStart() {
		super.onStart();
		sendView("Sign In Page", null);
	}
	
	
	public void queueIntent(Intent intent) {
		queuedIntent = intent;		
	}
	
	private void clearQueuedIntent() {
		queuedIntent = null;
	}
	
	private void startQueuedIntent() {
		if (queuedIntent != null) {
			Intent startedIntent = queuedIntent;
			queuedIntent = null;
			startActivity(startedIntent);
		}
	}
	
	public void loginUser(String username, String password, boolean staySignedInChecked) {
		Intent intent = new Intent(LoginDialogFragment.this.getActivity(), AuthenticateUserIntentService.class);
		intent.putExtra(Extra.LOGIN_USERNAME, username);
		intent.putExtra(Extra.LOGIN_PASSWORD, password);
		intent.putExtra(Extra.LOGIN_SAVE_USER, staySignedInChecked);
		LoginDialogFragment.this.getActivity().startService(intent);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mainAuthenticationReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		mainAuthenticationReceiver = new MainAuthenticationReceiver();
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mainAuthenticationReceiver, new IntentFilter(AuthenticateUserIntentService.ACTION_UPDATE_MAIN));
		
	
	}
	
	public class MainAuthenticationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent incomingIntent) {
			Toast signInMessage = Toast.makeText(LoginDialogFragment.this.getActivity(), R.string.dialog_sign_in_failed, Toast.LENGTH_LONG);
			signInMessage.setGravity(Gravity.CENTER, 0, 0);
			Dialog loginDialog = getDialog();
			View loginButton = loginDialog.findViewById(R.id.login_button_ok);
			CheckBox staySignedIn = (CheckBox) ((Dialog) loginDialog).findViewById(R.id.login_dialog_checkbox);
			
			
			String result = incomingIntent.getStringExtra(Extra.LOGIN_SUCCESS);
			
			if(!TextUtils.isEmpty(result) && result.equals(AuthenticateUserIntentService.ACTION_SUCCESS)) {
				signInMessage.setText(R.string.dialog_signed_in);
				closeLoginDialog();
				EllucianApplication ellucianApp = LoginDialogFragment.this.getEllucianActivity().getEllucianApp();
				
				if(!staySignedIn.isChecked()) {
					
					ellucianApp.startIdleTimer();
				}
				signInMessage.show();
				//signInButton.setText(R.string.main_sign_out);
				
				ellucianApp.startNotifications();
				
				// Checks to see if the dialog was opened by a request for a auth-neccessary activity
				startQueuedIntent();
				
			} else {
				signInMessage.show();
				if(loginButton != null) loginButton.setEnabled(true);
			}
			
		}		
	}
	
	private void closeLoginDialog() {
		getDialog().dismiss();
	}
	
}
