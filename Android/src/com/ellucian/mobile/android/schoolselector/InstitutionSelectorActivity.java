package com.ellucian.mobile.android.schoolselector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.ellucian.mobile.android.DataCache;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.UICustomizer;
import com.ellucian.mobile.android.adapter.FilteredAdapter;
import com.ellucian.mobile.android.auth.LoginUtil;
import com.ellucian.mobile.android.configuration.DashboardImagesDownloaderActivity;
import com.ellucian.mobile.android.home.HomeActivity;

public class InstitutionSelectorActivity extends ListActivity {
	public class DownloadTask extends AsyncTask<Void, Void, Institutions> {

		@Override
		protected Institutions doInBackground(Void... urls) {

			try {

				final HttpClient client = new DefaultHttpClient();
				final HttpGet request = new HttpGet();
				request.setURI(new URI(CONFIGURATION_URL));
				final HttpResponse response = client.execute(request);

				final int status = response.getStatusLine().getStatusCode();
				if (status == HttpStatus.SC_OK) {
					final BufferedReader in = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent()));
					final StringBuffer sb = new StringBuffer();
					String line = "";
					final String NL = System.getProperty("line.separator");
					while ((line = in.readLine()) != null) {
						sb.append(line + NL);
					}
					in.close();
					institutions = sb.toString();

					return parseConfiguration(institutions);
				} else {
					throw new RuntimeException(response.getStatusLine()
							.toString());
				}

			} catch (final Exception e) {
				Log.e(EllucianApplication.TAG, "XML Pasing Excpetion = " + e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Institutions schools) {

			super.onPostExecute(schools);
			if (schools != null) {
				final FilteredAdapter<Institution> adapter = getAdapter(
						context, android.R.layout.simple_list_item_1,
						schools.getInstitutions());
				setListAdapter(adapter);
			} else {
				setListAdapter(null);
			}
			setProgressBarIndeterminateVisibility(false);
			pd.dismiss();
		}

	}

	private static final String CONFIGURATION_URL = //"http://datateldev.blob.core.windows.net/mobileappconfig/configurations.xml"; // "http://cdn.datatel.com/mobileappconfig/universities.xml";
			"http://msdev.sghedu.com/mobilecloud-daily/rest/institution"; //TODO move to correct server
	private static final String INSTITUTIONS = "institutionsXml";

	private InstitutionSelectorActivity context;

	private String institutions;

	private ProgressDialog pd;

	private String query;

	private void cleanup() {
		UICustomizer.reset();
		((EllucianApplication) this.getApplication()).getImageLoader()
				.clearCache();
		final DataCache dataCache = ((EllucianApplication) this.getApplication())
				.getDataCache();
		if (dataCache != null) {
			((EllucianApplication) this.getApplication()).getDataCache()
					.clearCache();
		}
		
		LoginUtil.logout(getApplication());
		LoginUtil.clearUsername(getApplication());

	}

	private void handleIntent(Intent intent) {

		final Object data = getLastNonConfigurationInstance();
		final Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
		final String xmlExtra = intent.getStringExtra(INSTITUTIONS);

		// http://code.google.com/p/android/issues/detail?id=15579
		if (data != null) {
			institutions = data.toString();
		} else if (appData != null) {
			institutions = appData.getString(INSTITUTIONS);
		} else if (xmlExtra != null) {
			institutions = intent.getStringExtra(INSTITUTIONS);
		}

		String activityTitle = getResources().getString(R.string.schoolSelection);
		if (Intent.ACTION_SEARCH.equals(intent.getAction())
				&& intent.getExtras().containsKey(SearchManager.QUERY)) {
			activityTitle += " - " + intent.getStringExtra(SearchManager.QUERY);
		}
		setTitle(activityTitle);
		
		style();
		
		if (institutions != null) {
			final Institutions schools = parseConfiguration(institutions);
			final FilteredAdapter<Institution> adapter = getAdapter(
					this, android.R.layout.simple_list_item_1,
					schools.getInstitutions());
			setListAdapter(adapter);

		} else {
			pd = ProgressDialog.show(this, getString(R.string.downloading),
					getString(R.string.downloading), true);

			context = this;
			query = null;
			new DownloadTask().execute();
		}

		if (Intent.ACTION_SEARCH.equals(intent.getAction())
				&& intent.getExtras().containsKey(SearchManager.QUERY)) {
			query = intent.getStringExtra(SearchManager.QUERY);
			if (query.startsWith("http")) {
				cleanup();
				final Intent downloadIntent = new Intent(
						InstitutionSelectorActivity.this,
						DashboardImagesDownloaderActivity.class);
				final SharedPreferences preferences = getSharedPreferences(
						HomeActivity.CONFIGURATION, MODE_PRIVATE);
				final SharedPreferences.Editor editor = preferences.edit();
				editor.putString("configUrl", query);
				editor.remove("configXml");
				editor.putString("displayName", "");
				editor.putString("fullName", "");
				editor.putString("uniqueId", "");
				editor.commit();

				startActivity(downloadIntent);
			} else {
				((FilteredAdapter<?>) getListAdapter()).getFilter().filter(
						query);
			}
		}

	}

	private void style() {
		final TextView title = (TextView) 
				findViewById(R.id.titlebar_title);

		title.setText(getTitle());

		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.institution_list);

		handleIntent(getIntent());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.institutions, menu);

		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, final int position,
			long id) {
		super.onListItemClick(l, v, position, id);

		final Institution institution = (Institution) this.getListAdapter()
				.getItem(position);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String text = String.format(
				getResources().getString(R.string.schoolSelectionConfirmation),
				institution.getFullName());
		builder.setMessage(text)
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								setSchool(institution);
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();

	}

	private void setSchool(final Institution institution) {
		cleanup();

		final SharedPreferences preferences = getSharedPreferences(
				HomeActivity.CONFIGURATION, MODE_PRIVATE);
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putString("configUrl", institution.getConfigUrl());
		editor.putString("displayName", institution.getDisplayName());
		editor.putString("fullName", institution.getFullName());
		editor.putString("uniqueId", institution.getUniqueId());
		editor.remove("configXml");
		editor.commit();

		final Intent intent = new Intent(InstitutionSelectorActivity.this,
				DashboardImagesDownloaderActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_search:
			onSearchRequested();
			return true;
		case R.id.menu_clear:
			final Intent intent = new Intent(InstitutionSelectorActivity.this,
					InstitutionSelectorActivity.class);
			intent.setAction(Intent.ACTION_VIEW);
			intent.putExtra(INSTITUTIONS, institutions);
			startActivity(intent);
			query = null;
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_clear).setEnabled(query != null);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {

		return institutions;
	}

	@Override
	public boolean onSearchRequested() {
		final Bundle appData = new Bundle();
		appData.putString(INSTITUTIONS, institutions);
		startSearch(null, false, appData, false);
		return true;
	}

	//old xml way
//	private Institutions parseConfiguration(String institutionsXml) {
//		final InstitutionsHandler xmlHandler = new InstitutionsHandler();
//		final SAXParserFactory spf = SAXParserFactory.newInstance();
//		SAXParser sp;
//		try {
//			sp = spf.newSAXParser();
//
//			final XMLReader xr = sp.getXMLReader();
//			xr.setContentHandler(xmlHandler);
//			xr.parse(new InputSource(new ByteArrayInputStream(institutionsXml
//					.getBytes())));
//
//			return xmlHandler.getInstitutions();
//		} catch (final Exception e) {
//			Log.e(EllucianApplication.TAG, e.getLocalizedMessage());
//		}
//		return null;
//	}
	
	//2.0 json
	private Institutions parseConfiguration(String institutionsJson) {
		JSONArray jsonArray;
		try {
			JSONObject jsonInstitutions = new JSONObject(institutionsJson);
			jsonArray = jsonInstitutions.getJSONArray("institutions");

			Institutions institutions = new Institutions();

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				Institution institution = new Institution();
				institution.setConfigUrl(jsonObject.getString("configurationUrl"));
				institution.setDisplayName(jsonObject.getString("displayName"));
				institution
						.setFullName(jsonObject.getString("institutionName"));

				JSONArray jsonKeywords = jsonObject.getJSONArray("keywords");
				ArrayList<String> list = new ArrayList<String>();
				for (int j = 0; j < jsonKeywords.length(); j++) {
					list.add(jsonKeywords.getString(j));
				}

				institution.setKeywords(list);
				institution.setUniqueId(jsonObject.getString("accountCode"));
				institutions.add(institution);
				
			}
			return institutions;
		} catch (JSONException e) {
			Log.e(EllucianApplication.TAG, e.getLocalizedMessage());
		}

		return null;
	}
		
	private FilteredAdapter<Institution> getAdapter(Context context,
			int textViewResourceId, List<Institution> objects) {
		return new FilteredAdapter<Institution>(context, textViewResourceId,
				objects) {

			@Override
			public CharSequence getText(Institution obj) {
				return obj.getFullName();
			}

			private boolean containsQueryString(String queryString,
					String valueText) {
				valueText = valueText.toLowerCase();
				if (valueText.contains(queryString)) {
					return true;
				} else {
					final String[] words = valueText.split(" ");
					final int wordCount = words.length;

					for (int k = 0; k < wordCount; k++) {
						if (words[k].contains(queryString)) {
							return true;
						}
					}
				}
				return false;
			}

			@Override
			public boolean matchesFilter(Institution institution,
					String queryString, int position) {
				boolean match = false;
				if (containsQueryString(queryString, institution.getFullName())) {
					return true;
				} else if (containsQueryString(queryString,
						institution.getDisplayName())) {
					return true;
				} else {
					for (int k = 0; k < institution.getKeywords().size(); k++) {
						if (containsQueryString(queryString, institution
								.getKeywords().get(k))) {
							return true;
						}
					}
				}
				return match;
			}
		};
	}
}