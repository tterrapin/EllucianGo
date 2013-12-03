package com.ellucian.mobile.android.auth;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.configuration.Configuration;

public class LoginActivity extends Activity {
	private class TestLoginTask extends AsyncTask<String, Void, Boolean> {

		private final String password;
		private final String username;
		private String[] roles;

		public TestLoginTask(String username, String password) {
			this.username = username;
			this.password = password;
		}

		@Override
		protected Boolean doInBackground(String... urls) {
			try {

				final HttpClient client = new DefaultHttpClient();
				final HttpGet request = new HttpGet();
				request.setURI(new URI(urls[0]));

				request.addHeader(BasicScheme.authenticate(
						new UsernamePasswordCredentials(username, password),
						"UTF-8", false));
				final HttpResponse response = client.execute(request);

				final int status = response.getStatusLine().getStatusCode();
				
				//TODO get roles
				return status == HttpStatus.SC_OK;
				
				
			} catch (final Exception e) {
				Log.e(EllucianApplication.TAG,
						"Login test failed" + e.getLocalizedMessage());
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean r) {
			if (r == Boolean.TRUE) {
				LoginUtil.storeCredentials(LoginActivity.this, username,
						password, roles);
				setResult(RESULT_OK);
				finish();
			} else {
				final TextView username = (TextView) findViewById(R.id.loginUsername);
				final TextView password = (TextView) findViewById(R.id.loginPassword);
				final Button loginButton = (Button) findViewById(R.id.loginButton);
				final TextView loginMessage = (TextView) findViewById(R.id.loginMessage);

				username.setEnabled(true);
				password.setEnabled(true);
				loginButton.setEnabled(true);
				loginMessage.setText(R.string.loginFailed);
			}
		}
	}

	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		final TextView username = (TextView) findViewById(R.id.loginUsername);
		final TextView password = (TextView) findViewById(R.id.loginPassword);
		final Button loginButton = (Button) findViewById(R.id.loginButton);
		final Button cancelButton = (Button) findViewById(R.id.cancelButton);
		final TextView loginMessage = (TextView) findViewById(R.id.loginMessage);

		username.setText(LoginUtil.getUsername(this));

		loginButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String usernameString = username.getText().toString();
				String passwordString = password.getText().toString();

				if (usernameString.length() > 0 && passwordString.length() > 0) {
					username.setEnabled(false);
					password.setEnabled(false);
					loginButton.setEnabled(false);

					loginMessage.setText(R.string.loginProgress);

					final Configuration configuration = ((EllucianApplication) getApplication())
							.getConfiguration();
					final String login = configuration.getAuthenticationUrl();
					new TestLoginTask(usernameString, passwordString)
							.execute(login);
				} else if (usernameString.length() == 0) {
					loginMessage.setText(R.string.enterUsername);
				} else if (passwordString.length() == 0) {
					loginMessage.setText(R.string.enterPassword);
				}
			}
		});

		cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

	}

}
