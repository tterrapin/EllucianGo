package com.ellucian.mobile.android.directory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.DataCache;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.UICustomizer;
import com.ellucian.mobile.android.auth.LoginActivity;
import com.ellucian.mobile.android.auth.LoginUtil;

public class DirectoryActivity extends ListActivity {

	private class ResultsAdapter extends BaseAdapter {
		private final List<ContactProxy> items;
		private final LayoutInflater mInflater;

		public ResultsAdapter(Context context, List<ContactProxy> items) {

			this.items = items;
			mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			return items.size();
		}

		public Object getItem(int position) {
			return items.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			final ContactProxy item = items.get(position);

			convertView = mInflater.inflate(R.layout.directory_list_row, null);

			final TextView preferredName = (TextView) convertView
					.findViewById(R.id.preferredName);

			preferredName.setText(item.getPreferredName());

			return convertView;

		}

	}

	private class SearchDirectoryTask extends
			AsyncTask<String, Void, List<ContactProxy>> {

		private final Context context;
		private final String query;

		public SearchDirectoryTask(Context context, String query) {

			this.context = context;
			this.query = query;
		}

		@Override
		protected List<ContactProxy> doInBackground(String... urls) {
			try {
				refreshInProgress = true;
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

					jsonResults = sb.toString();

					contacts = SearchResultsParser.parse(jsonResults);

					((EllucianApplication) getApplication()).getDataCache()
							.putAuthCache(searchScopeUrls.get(0), jsonResults,
									contacts);
					((EllucianApplication) getApplication()).getDataCache()
							.putAuthCache("search-" + searchScopeUrls.get(0),
									query, query);
					((EllucianApplication) getApplication()).getDataCache()
							.putAuthCache("index-" + searchScopeUrls.get(0),
									"" + spinner.getSelectedItemPosition(),
									"" + spinner.getSelectedItemPosition());
					return contacts;

				} else {
					throw new RuntimeException(response.getStatusLine()
							.toString());
				}

			} catch (final Exception e) {
				Log.e(EllucianApplication.TAG, "Directory search failed = " + e);
			}
			return null;

		}

		@Override
		protected void onPostExecute(List<ContactProxy> results) {
			adapter = new ResultsAdapter(context, results);
			setListAdapter(adapter);
			refreshInProgress = false;
			UICustomizer.setProgressBarVisible(DirectoryActivity.this, false);
			if(results != null && results.size() == 0) {
				Toast.makeText(context, R.string.noResultsFound, Toast.LENGTH_SHORT).show();
			}

		}
	}

	private static final int LOGIN_RESULT = RESULT_FIRST_USER;

	private String activityTitle;

	private ListAdapter adapter;

	private List<ContactProxy> contacts;

	private String jsonResults;

	private ArrayList<String> profileUrls;

	private boolean refreshInProgress;

	private ArrayList<String> searchScopeNames;

	private ArrayList<String> searchScopeUrls;

	private int selectedIndex;

	private Spinner spinner;

	private AsyncTask<String, Void, List<ContactProxy>> task;

	private void doLogin() {
		final Intent loginIntent = new Intent(DirectoryActivity.this,
				LoginActivity.class);
		startActivityForResult(loginIntent, LOGIN_RESULT);
	}

	@SuppressWarnings("unchecked")
	private void handleIntent(Intent intent) {

		contacts = new ArrayList<ContactProxy>();

		final Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);

		final boolean loggedIn = LoginUtil.isLoggedIn(getApplicationContext());
		setContentView(R.layout.directory_list);

		final DataCache cache = ((EllucianApplication) getApplication())
				.getDataCache();

		if (appData != null) {
			searchScopeNames = appData.getStringArrayList("searchScopeNames");
			searchScopeUrls = appData.getStringArrayList("searchScopeUrls");
			activityTitle = appData.getString("title");
			selectedIndex = appData.getInt("selectedIndex");
			profileUrls = appData.getStringArrayList("profileUrls");
			setTitle(activityTitle + " - "
					+ intent.getStringExtra(SearchManager.QUERY));
		} else {
			searchScopeNames = intent
					.getStringArrayListExtra("searchScopeNames");
			searchScopeUrls = intent.getStringArrayListExtra("searchScopeUrls");
			activityTitle = intent.getStringExtra("title");
			selectedIndex = intent.getIntExtra("selectedIndex", 0);
			profileUrls = intent.getStringArrayListExtra("profileUrls");

			final String cachedSearchString = cache.getCache(this, "search-"
					+ searchScopeUrls.get(0));

			if (cachedSearchString != null) {
				setTitle(activityTitle + " - " + cachedSearchString);
			} else {
				setTitle(activityTitle);
			}
		}
		

		UICustomizer.style(this);

		spinner = (Spinner) findViewById(R.id.filter);
		final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, searchScopeNames);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(spinnerAdapter);
		spinner.setSelection(selectedIndex);

		final Button button = (Button) findViewById(R.id.searchButton);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				onSearchRequested();

			}
		});
		
		if (!loggedIn) {
			doLogin();
		} else if (Intent.ACTION_SEARCH.equals(intent.getAction())
				&& intent.getExtras().containsKey(SearchManager.QUERY)) {
			final String query = intent.getStringExtra(SearchManager.QUERY);
			final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
					this, DirectorySearchSuggestionProvider.AUTHORITY,
					DirectorySearchSuggestionProvider.MODE);
			suggestions.saveRecentQuery(query, null);
			setList(query);
		} else {

			contacts = new ArrayList<ContactProxy>();

			final String cachedContent = cache.getCache(this,
					searchScopeUrls.get(0));
			final String cachedIndex = cache.getCache(this, "index-"
					+ searchScopeUrls.get(0));

			if (cachedContent != null) {
				try {

					jsonResults = cachedContent;
					final Object o = cache.getCacheObject(searchScopeUrls
							.get(0));
					if (o != null && o instanceof List) {
						contacts = (List<ContactProxy>) o;
					} else {
						contacts = SearchResultsParser.parse(jsonResults);

						cache.putCacheObject(searchScopeUrls.get(0), contacts);
					}
					try {
						spinner.setSelection(Integer.parseInt(cachedIndex));
					} catch (Exception e) {
					}
					adapter = new ResultsAdapter(
							DirectoryActivity.this, contacts);
					setListAdapter(adapter);

				} catch (final JSONException e) {
					Log.e(EllucianApplication.TAG,
							"Can't parse json in directory search");
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_RESULT) {
			if (resultCode == RESULT_OK) {

			} else if (resultCode == RESULT_CANCELED) {
				finish();
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		handleIntent(getIntent());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.directory, menu);
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
		final Intent intent = new Intent(DirectoryActivity.this,
				ProfileActivity.class);

		final ContactProxy contact = (ContactProxy) adapter.getItem(position);

		intent.putExtra("username", contact.getUserName());
		intent.putExtra("domain", contact.getUserDomain());
		intent.putExtra("profileUrl", profileUrls.get(spinner.getSelectedItemPosition()));
		intent.putExtra("preferredName", contact.getPreferredName());
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
		case R.id.menu_logout:
			doLogout();
			break;
		}
		return false;
	}

	@Override
	public boolean onSearchRequested() {
		final Bundle appData = new Bundle();
		appData.putStringArrayList("searchScopeNames", searchScopeNames);
		appData.putStringArrayList("searchScopeUrls", searchScopeUrls);
		appData.putInt("selectedIndex", spinner.getSelectedItemPosition());
		appData.putString("title", activityTitle);
		appData.putStringArrayList("profileUrls", profileUrls);
		startSearch(null, false, appData, false);
		return true;
	}

	private void setList(String searchQuery) {
		try {
			String url = searchScopeUrls.get(selectedIndex) + "&query="
					+ URLEncoder.encode(searchQuery,"UTF-8");
			UICustomizer.setProgressBarVisible(DirectoryActivity.this, true);
			new SearchDirectoryTask(DirectoryActivity.this, searchQuery)
					.execute(url);
		} catch (UnsupportedEncodingException e) {
			Log.e(EllucianApplication.TAG, e.getLocalizedMessage());
		}

		
	}
	
	private void doLogout() {
		LoginUtil.logout(getApplication());
		setResult(RESULT_CANCELED);
		finish();
	}

}
