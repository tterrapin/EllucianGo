package com.ellucian.mobile.android.directory.phone;

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
import org.json.JSONException;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.DataCache;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.UICustomizer;

public class ImportantNumbersUncategorizedActivity extends ListActivity {

	private class UpdateDirectoryTask extends
			AsyncTask<String, Void, List<ImportantNumbersCategory>> {

		private boolean updateList = false;

		public UpdateDirectoryTask() {

		}

		@Override
		protected List<ImportantNumbersCategory> doInBackground(String... urls) {
			try {
				refreshInProgress = true;
				final HttpClient client = new DefaultHttpClient();
				final HttpGet request = new HttpGet();
				request.setURI(new URI(urls[0]));

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

					final String tempJsonImportantNumbers = sb.toString();
					if (!tempJsonImportantNumbers.equals(jsonImportantNumbers)) {
						updateList = true;
					}
					jsonImportantNumbers = tempJsonImportantNumbers;

					categories = ImportantNumbersParser
							.parse(jsonImportantNumbers);

					((EllucianApplication) getApplication())
							.getDataCache()
							.putCache(urls[0], jsonImportantNumbers, categories);

					return categories;

				} else {
					throw new RuntimeException(response.getStatusLine()
							.toString());
				}

			} catch (final Exception e) {
				Log.e(EllucianApplication.TAG,
						"Important Numbers update failed = " + e);
			}
			return null;

		}

		@Override
		protected void onPostExecute(List<ImportantNumbersCategory> numbers) {
			if (updateList) {
				setList();
			}
			refreshInProgress = false;
			UICustomizer.setProgressBarVisible(
					ImportantNumbersUncategorizedActivity.this, false);

		}
	}

	private String activityTitle;

	private ListAdapter adapter;

	private List<ImportantNumbersCategory> categories;

	private String jsonImportantNumbers;

	private boolean refreshInProgress;

	private AsyncTask<String, Void, List<ImportantNumbersCategory>> task;

	private String url;

	@SuppressWarnings("unchecked")
	private void handleIntent(Intent intent) {

		categories = new ArrayList<ImportantNumbersCategory>();

		final Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);

		// http://code.google.com/p/android/issues/detail?id=15579
		if (appData != null) {
			url = appData.getString("url");
			activityTitle =	(getResources().getString(R.string.searchResults) + " - "
					+ intent.getStringExtra(SearchManager.QUERY));
		} else {
			url = intent.getStringExtra("url");
			activityTitle = intent.getStringExtra("title");
		}

		setTitle(activityTitle);
		UICustomizer.style(this);

		final DataCache cache = ((EllucianApplication) getApplication())
				.getDataCache();

		final boolean current = cache.isCurrent(this, url);

		final String cachedContent = cache.getCache(this, url);

		if (cachedContent != null) {
			try {
				jsonImportantNumbers = cachedContent;
				final Object o = cache.getCacheObject(url);
				if (o != null && o instanceof List) {
					categories = (List<ImportantNumbersCategory>) o;
				} else {
					categories = ImportantNumbersParser
							.parse(jsonImportantNumbers);
					cache.putCacheObject(url, categories);
				}
				setList();
			} catch (final JSONException e) {
				Log.e(EllucianApplication.TAG,
						"Can't parse json in important numbers");
			}
		}

		if (Intent.ACTION_SEARCH.equals(intent.getAction())
				&& intent.getExtras().containsKey(SearchManager.QUERY)) {
			final String query = intent.getStringExtra(SearchManager.QUERY);

			setList(query);

		} else {
			if (!current) {
				update();
			}
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.important_numbers_uncategorized_list);
		handleIntent(getIntent());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.important_numbers, menu);
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (task != null && refreshInProgress) {
			task.cancel(true);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final Intent intent = new Intent(
				ImportantNumbersUncategorizedActivity.this,
				ImportantNumbersDetailActivity.class);

		final ImportantNumbersContact contact = (ImportantNumbersContact) adapter
				.getItem(position);

		intent.putExtra("name", contact.getName().toString());
			if (contact.getEmail() != null) {
			intent.putExtra("email", contact.getEmail().toString());
		}
		if (contact.getPhone() != null) {
			intent.putExtra("phone", contact.getPhone().toString());
		}
		if (contact.getLabel() != null) {
			intent.putExtra("label", contact.getLabel().toString());
		}
		if (contact.getLatitude() != null) {
			intent.putExtra("latitude", contact.getLatitude());
		}
		if (contact.getLongitude() != null) {
			intent.putExtra("longitude", contact.getLongitude());
		}

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
		case R.id.menu_refresh:
			update();
			break;
		}
		return false;
	}

	@Override
	public boolean onSearchRequested() {
		final Bundle appData = new Bundle();
		appData.putString("url", url);
		appData.putString("title", activityTitle);
		startSearch(null, false, appData, false);
		return true;
	}

	private void setList() {
		setList(null);
	}

	private void setList(String searchQuery) {
		final ImportantNumbersCategory category = categories.get(0);

		final List<ImportantNumbersContact> contacts = new ArrayList<ImportantNumbersContact>();
		for (final ImportantNumbersContact contact : category.getContacts()) {
			if (searchQuery == null) {
				contacts.add(contact);
			} else if (searchQuery != null
					&& contact.getName().toLowerCase()
							.contains(searchQuery.toLowerCase())) {
				contacts.add(contact);
			}
		}
		adapter = new ArrayAdapter<ImportantNumbersContact>(
				ImportantNumbersUncategorizedActivity.this,
				android.R.layout.simple_list_item_1, contacts);

		setListAdapter(adapter);
	}

	private void update() {
		UICustomizer.setProgressBarVisible(
				ImportantNumbersUncategorizedActivity.this, true);
		new UpdateDirectoryTask().execute(url);

	}
}
