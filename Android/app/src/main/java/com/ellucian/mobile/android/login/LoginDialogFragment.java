/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.MainActivity;
import com.ellucian.mobile.android.adapter.ModuleMenuAdapter;
import com.ellucian.mobile.android.app.EllucianDialogFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.client.services.AuthenticateUserIntentService;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class LoginDialogFragment extends EllucianDialogFragment {
	public static final String LOGIN_DIALOG = "login_dialog";
	private AlertDialog loginDialog;
    private Intent queuedIntent;
	private List<String> roles;

	private MainAuthenticationReceiver mainAuthenticationReceiver;
	private boolean forcedLogin;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String loginType = Utils.getStringFromPreferences(getActivity(), Utils.SECURITY, Utils.LOGIN_TYPE, Utils.NATIVE_LOGIN_TYPE);
		if(loginType.equals(Utils.NATIVE_LOGIN_TYPE)) {
			createBasicAuthenticationLoginDialog();
		} else {
			createWebAuthenticationLoginDialog();
		}

		return loginDialog;
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void createWebAuthenticationLoginDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.fragment_login_web_dialog, null);
		builder.setView(dialogView);

        builder.setNegativeButton(android.R.string.cancel, null);
        loginDialog = builder.create();

        loginDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                Button negative = loginDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                negative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.setEnabled(false);
                        doCancel();
                    }
                });

            }
        });

		final WebView webView = (WebView) dialogView.findViewById(R.id.login_webview);
	
		webView.setWebChromeClient(new WebChromeClient());
		webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
                Utils.hideProgressIndicator(dialogView);
                webView.setVisibility(View.VISIBLE);
				String title = view.getTitle();
				if ("Authentication Success".equals(title)) {
					sendEvent(GoogleAnalyticsConstants.CATEGORY_AUTHENTICATION, GoogleAnalyticsConstants.ACTION_LOGIN, "Authentication using web login", null, null);
					loginUser(null, null, false);
				}
			}
		});

		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setUseWideViewPort(true);
		webSettings.setSupportZoom(true);
		webSettings.setBuiltInZoomControls(true);
		
		//Enable HTML 5 local storage
		String databasePath = webView.getContext().getDir("databases", 
                Context.MODE_PRIVATE).getPath(); 
		webSettings.setDatabaseEnabled(true);
        Utils.setDatabasePath(webSettings, databasePath);
		webSettings.setDomStorageEnabled(true);

		String loginUrl = Utils.getStringFromPreferences(getActivity(), Utils.SECURITY, Utils.LOGIN_URL, "");		
		webView.loadUrl(loginUrl);
		
        Utils.showProgressIndicator(dialogView);
	}

	private void createBasicAuthenticationLoginDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		final ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.fragment_login_dialog, null);
        builder.setView(dialogView);

        builder.setNegativeButton(android.R.string.cancel, null);
		builder.setPositiveButton(R.string.dialog_sign_in, null);

        loginDialog = builder.create();

        loginDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                Button positive = loginDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negative = loginDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                negative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.setEnabled(false);
                        doCancel();
                    }
                });

                positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.setEnabled(false);
                        Dialog loginDialog = getDialog();
                        EditText usernameView = (EditText) loginDialog.findViewById(R.id.login_dialog_username);
                        String username = usernameView.getText().toString();
                        EditText passwordView = (EditText) loginDialog.findViewById(R.id.login_dialog_password);
                        String password = passwordView.getText().toString();
                        CheckBox staySignedIn = (CheckBox) loginDialog.findViewById(R.id.login_dialog_checkbox);

                        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                            Toast emptyMessage = Toast.makeText(LoginDialogFragment.this.getActivity(), R.string.dialog_sign_in_empty, Toast.LENGTH_LONG);
                            emptyMessage.setGravity(Gravity.CENTER, 0, 0);
                            emptyMessage.show();
                            view.setEnabled(true);
                        } else {
                            boolean staySignedInChecked = staySignedIn.isChecked();
                            if (staySignedInChecked) {
                                sendEvent(GoogleAnalyticsConstants.CATEGORY_AUTHENTICATION, GoogleAnalyticsConstants.ACTION_LOGIN, "Authentication with save credential", null, null);
                            } else {
                                sendEvent(GoogleAnalyticsConstants.CATEGORY_AUTHENTICATION, GoogleAnalyticsConstants.ACTION_LOGIN, "Authentication without save credential", null, null);
                            }
                            loginDialog.findViewById(R.id.progress_spinner).setVisibility(View.VISIBLE);
                            loginUser(username, password, staySignedInChecked);
                        }

                    }
                });
            }
        });

	}

	@Override
	public void onStart() {
		super.onStart();
		sendView("Sign In Page", null);
	}
	
	private void doCancel() {
		sendEvent(GoogleAnalyticsConstants.CATEGORY_AUTHENTICATION, GoogleAnalyticsConstants.ACTION_CANCEL, "Click Cancel", null, null);
		loginDialog.cancel();
		// Make sure queue is empty in case of another login attempt
		clearQueuedIntent();
		getEllucianActivity().getEllucianApp().removeAppUser();
		if(forcedLogin) {
			goHome();
		}
	}

	private void goHome() {
		Activity activity = getActivity();
		Intent mainIntent = new Intent(activity, MainActivity.class);
		mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		activity.startActivity(mainIntent);
		activity.finish();
	}
	
	
	public void queueIntent(Intent intent, List<String> roles) {
		queuedIntent = intent;	
		this.roles = roles;
	}
	
	private void clearQueuedIntent() {
		queuedIntent = null;
	}
	
	private void startQueuedIntent() {
		if (queuedIntent != null) {
			Intent startedIntent = queuedIntent;
			queuedIntent = null;
			
			boolean authorized = false;
			if(roles != null) {
				Application application = getActivity().getApplication();
				
				List<String> userRoles = new ArrayList<String>();
				if(application instanceof EllucianApplication) {
					EllucianApplication ea = (EllucianApplication)application;
					userRoles = ea.getAppUserRoles();
				} else {
					userRoles = new ArrayList<String>();
					userRoles.add(ModuleMenuAdapter.MODULE_ROLE_EVERYONE);
				}
				
				
				for(String role : roles) {
					if(userRoles.contains(role)) {
						authorized = true;
					}
					if(role.equals(ModuleMenuAdapter.MODULE_ROLE_EVERYONE)) {
						authorized = true;
					}
				}
				if(roles.size() == 0) { //3.0 upgrade compatibility
					authorized = true;
				}
			} else {
				authorized = false;
			}
			
			if(authorized) {
				startActivity(startedIntent);
				if(forcedLogin) {
					getActivity().finish();
				}
			} else {
				getActivity().runOnUiThread(new Runnable() {
				    public void run() {
						Toast unauthorizedToast = Toast.makeText(LoginDialogFragment.this.getActivity(), R.string.unauthorized_feature, Toast.LENGTH_LONG);
						unauthorizedToast.setGravity(Gravity.CENTER, 0, 0);
						unauthorizedToast.show();
				    }
				});
				goHome();
			}
		}
	}
	
	private void loginUser(String username, String password, boolean staySignedInChecked) {
		Intent intent = new Intent(LoginDialogFragment.this.getActivity(), AuthenticateUserIntentService.class);
		intent.putExtra(Extra.LOGIN_USERNAME, username);
		intent.putExtra(Extra.LOGIN_PASSWORD, password);
		intent.putExtra(Extra.LOGIN_SAVE_USER, staySignedInChecked);
        intent.putExtra(Extra.SEND_UNAUTH_BROADCAST, false);
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
            // Progress spinner only occurs on Native (Basic) Auth login dialog.
            View progressSpinner = loginDialog.findViewById(R.id.progress_spinner);
            if (progressSpinner != null) {
                progressSpinner.setVisibility(View.GONE);
            }

            Toast signInMessage = Toast.makeText(LoginDialogFragment.this.getActivity(), R.string.dialog_sign_in_failed, Toast.LENGTH_LONG);
			signInMessage.setGravity(Gravity.CENTER, 0, 0);
			AlertDialog loginDialog = (AlertDialog) getDialog();
			CheckBox staySignedIn = (CheckBox) loginDialog.findViewById(R.id.login_dialog_checkbox);
			
			
			String result = incomingIntent.getStringExtra(Extra.LOGIN_SUCCESS);
			
			if(!TextUtils.isEmpty(result) && result.equals(AuthenticateUserIntentService.ACTION_SUCCESS)) {
				signInMessage.setText(R.string.dialog_signed_in);
				closeLoginDialog();
				EllucianApplication ellucianApp = LoginDialogFragment.this.getEllucianActivity().getEllucianApp();
				String loginType = Utils.getStringFromPreferences(getActivity(), Utils.SECURITY, Utils.LOGIN_TYPE, Utils.NATIVE_LOGIN_TYPE);
				if(loginType.equals(Utils.NATIVE_LOGIN_TYPE)) {
					if(!staySignedIn.isChecked()) {
						ellucianApp.startIdleTimer();
					}
				}
				signInMessage.show();
				//signInButton.setText(R.string.main_sign_out);

				ellucianApp.startNotifications();

				// Checks to see if the dialog was opened by a request for a auth-necessary activity
				startQueuedIntent();

			} else {
				signInMessage.show();
                loginDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
			}
			
		}		
	}
	
	private void closeLoginDialog() {
		getDialog().dismiss();
	}

	/**
	 * If true, call finish after a successful login.
	 * 
	 * This will be used when the user is prompted because of an unauthorized or session timeout, and need to login again.
	 * By finishing the activity, and with the same activity queued, it will restart the activity without the item on the stack.
	 * @param b boolean
	 */
	public void forcedLogin(boolean b) {
		this.forcedLogin = b;
		this.setCancelable(false);
	}
	
	@Override
	public void onDestroyView() {
		// Trick to keep dialog open on rotate
		if (getDialog() != null && getRetainInstance())
			getDialog().setDismissMessage(null);
		super.onDestroyView();
	}
	
}
