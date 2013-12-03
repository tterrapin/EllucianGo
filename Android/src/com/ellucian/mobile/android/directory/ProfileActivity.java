package com.ellucian.mobile.android.directory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.ImageLoader;
import com.ellucian.mobile.android.UICustomizer;
import com.ellucian.mobile.android.auth.LoginUtil;

public class ProfileActivity extends Activity {

	private String username;
	private String domain;
	private String profileUrl;
	private Button addContactButton;
	private Profile profile;

	private void addContact(Profile profile) {

		String name = profile.getPreferredName();

		ContentValues values = new ContentValues();
		values.put(Data.DISPLAY_NAME, name);
		Uri rawContactUri = getContentResolver().insert(
				RawContacts.CONTENT_URI, values);
		long rawContactId = ContentUris.parseId(rawContactUri);
		// long contactId = Util.getContactId(c, rawContactId);
		System.out.println("rawContactId = " + rawContactId);
		// System.out.println("contactId = " + contactId);

		values.clear();

		values.put(Data.MIMETYPE, Data.CONTENT_TYPE);
		values.put(
				ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
				name);
		values.put(Data.RAW_CONTACT_ID, rawContactId);
		getContentResolver().insert(Data.CONTENT_URI, values);

		values.clear();
		values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
		values.put(
				ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
				name);
		values.put(Data.RAW_CONTACT_ID, rawContactId);
		getContentResolver().insert(Data.CONTENT_URI, values);

		if (profile.getMobilePhone() != null) {
			values.put(Phone.NUMBER, profile.getMobilePhone());
			values.put(Phone.TYPE, Phone.TYPE_MOBILE);
			values.put(Phone.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
			values.put(Data.RAW_CONTACT_ID, rawContactId);
			getContentResolver().insert(Data.CONTENT_URI, values);

			values.clear();
		}

		if (profile.getWorkPhone() != null) {
			values.put(Phone.NUMBER, profile.getWorkPhone());
			values.put(Phone.TYPE, Phone.TYPE_WORK);
			values.put(Phone.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
			values.put(Data.RAW_CONTACT_ID, rawContactId);
			getContentResolver().insert(Data.CONTENT_URI, values);

			values.clear();
		}
		if (profile.getEmail() != null) {
			values.put(Email.ADDRESS, profile.getEmail());
			values.put(Email.TYPE, Email.TYPE_OTHER);
			values.put(Email.MIMETYPE, Email.CONTENT_ITEM_TYPE);
			values.put(Data.RAW_CONTACT_ID, rawContactId);
			getContentResolver().insert(Data.CONTENT_URI, values);

			values.clear();
		}
		Toast.makeText(ProfileActivity.this, getResources().getString(R.string.contactAdded), Toast.LENGTH_SHORT).show();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.directory_detail);

		final Intent intent = getIntent();

		username = intent.getStringExtra("username");
		domain = intent.getStringExtra("domain");
		profileUrl = intent.getStringExtra("profileUrl");

		setTitle(intent.getStringExtra("preferredName"));
		UICustomizer.style(this);

		UICustomizer.setProgressBarVisible(ProfileActivity.this, true);
		String url = buildSearchUrl();
		new UpdateProfileTask(this).execute(url);
	}

	private String buildSearchUrl() {
		String url = profileUrl + "&user=" + username;
		if (domain != null) {
			url += "&domain=" + domain;
		}
		return url;
	}

	private void setData(final Profile profile) {

		if (profile.isOptOut()) {
			Toast.makeText(ProfileActivity.this, getResources().getString(R.string.userUnlisted),
					Toast.LENGTH_LONG).show();
			finish();
			return;
		} else {
			findViewById(R.id.profileLayout).setVisibility(View.VISIBLE);
			
			final ImageView imageView = (ImageView) findViewById(R.id.image);
			String imageUrl = profile.getImageUrl();
			if (imageUrl == null) {
				imageView.setVisibility(View.GONE);

			} else {
				imageView.setVisibility(View.VISIBLE);
				final Bitmap cachedImage = ((EllucianApplication) getApplication())
						.getImageLoader().loadImage(imageUrl,
								new ImageLoader.ImageLoadedListener() {

									public void imageLoaded(Bitmap imageBitmap) {
										imageView.setImageBitmap(imageBitmap);

									}
								});
				if (cachedImage != null) {
					imageView.setImageBitmap(cachedImage);
				}
			}

			setTitle(profile.getPreferredName());
			
			if (profile.getTitle() == null) {
				findViewById(R.id.title).setVisibility(View.GONE);
			} else {
				TextView title = ((TextView) findViewById(R.id.title));
				title.setText(profile.getTitle());
				title.setVisibility(View.VISIBLE);
			}

			if (profile.getDepartment() == null) {
				findViewById(R.id.department).setVisibility(View.GONE);
			} else {
				TextView department = ((TextView) findViewById(R.id.department));
				department.setText(profile.getDepartment());
				department.setVisibility(View.VISIBLE);
			}

			if (profile.getWorkPhone() == null) {
				findViewById(R.id.workLayout).setVisibility(View.GONE);
			} else {
				TextView workPhone = ((TextView) findViewById(R.id.work));
				workPhone.setText(profile.getWorkPhone());
				workPhone.setVisibility(View.VISIBLE);
			}
			if (profile.getMobilePhone() == null) {
				findViewById(R.id.mobileLayout).setVisibility(View.GONE);
			} else {
				TextView mobilePhone = ((TextView) findViewById(R.id.mobile));
				mobilePhone.setText(profile.getMobilePhone());
				mobilePhone.setVisibility(View.VISIBLE);
			}
			if (profile.getEmail() == null) {
				findViewById(R.id.emailLayout).setVisibility(View.GONE);
			} else {
				TextView email = ((TextView) findViewById(R.id.email));
				email.setText(profile.getEmail());
				email.setVisibility(View.VISIBLE);
			}
			if (profile.getOffice() == null) {
				findViewById(R.id.officeLayout).setVisibility(View.GONE);
			} else {
				TextView office = ((TextView) findViewById(R.id.office));
				office.setText(profile.getOffice());
				office.setVisibility(View.VISIBLE);
			}

			addContactButton = (Button) findViewById(R.id.addContactButton);
			addContactButton.setEnabled(true);

			addContactButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0) {
					addContact(profile);
				}
			});
		}
	}

	private class UpdateProfileTask extends AsyncTask<String, Void, Profile> {
		private final Context context;


		public UpdateProfileTask(Context context) {

			this.context = context;
		}

		@Override
		protected Profile doInBackground(String... urls) {
			try {

				final HttpClient client = new DefaultHttpClient();
				final HttpGet request = new HttpGet();
				request.setURI(new URI(urls[0]));

				final String username = LoginUtil.getUsername(context);
				final String password = LoginUtil.getPassword(context);
				request.addHeader(BasicScheme.authenticate(
						new UsernamePasswordCredentials(username, password),
						"UTF-8", false));

				final HttpResponse response = client.execute(request);

				final int status = response.getStatusLine().getStatusCode();
				if (status == HttpStatus.SC_OK) {
					final BufferedReader in = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent(), "UTF-8"));
					final StringBuffer sb = new StringBuffer();
					String line = "";
					final String NL = System.getProperty("line.separator");
					while ((line = in.readLine()) != null) {
						sb.append(line + NL);
					}
					in.close();

					return ProfileParser.parse(sb.toString());

				} else {
					throw new RuntimeException(response.getStatusLine()
							.toString());
				}

			} catch (final Exception e) {
				Log.e(EllucianApplication.TAG,
						"Directory profile update failed = " + e);
			}
			return null;

		}

		@Override
		protected void onPostExecute(Profile profile) {
			if (profile != null) {
				setData(profile);
				ProfileActivity.this.profile = profile;
			} else {
				Toast.makeText(ProfileActivity.this, getResources().getString(R.string.userUnlisted),
						Toast.LENGTH_LONG).show();
			}

			UICustomizer.setProgressBarVisible(ProfileActivity.this, false);

		}
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.profile, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add_contact:
			if(profile != null) {
				addContact(profile);
			}
			return true;
		}
		return false;
	}
	

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_add_contact).setEnabled(profile != null);
		return super.onPrepareOptionsMenu(menu);
	}
}
