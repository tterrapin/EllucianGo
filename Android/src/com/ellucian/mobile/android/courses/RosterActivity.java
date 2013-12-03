package com.ellucian.mobile.android.courses;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import android.app.ExpandableListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.DataCache;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.UICustomizer;
import com.ellucian.mobile.android.auth.LoginUtil;
import com.ellucian.mobile.android.directory.ProfileActivity;

public class RosterActivity extends ExpandableListActivity {
	private class UpdateRosterTask extends AsyncTask<String, Void, Roster> {
		private final Context context;
		private boolean updateList = false;

		public UpdateRosterTask(Context context) {
			this.context = context;
		}

		@Override
		protected Roster doInBackground(String... urls) {
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
					final String tempJsonImportantNumbers = sb.toString();
					if (!tempJsonImportantNumbers.equals(jsonRoster)) {
						updateList = true;
					}
					jsonRoster = tempJsonImportantNumbers;
					roster = RosterParser.parse(jsonRoster);
					((EllucianApplication) getApplication()).getDataCache()
							.putCache(urls[0], jsonRoster, roster);
					return roster;
				} else {
					throw new RuntimeException(response.getStatusLine()
							.toString());
				}
			} catch (final Exception e) {
				Log.e(EllucianApplication.TAG, "Roster update failed = " + e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Roster roster) {
			if (updateList) {
				setList();
			}
			refreshInProgress = false;
			UICustomizer.setProgressBarVisible(RosterActivity.this, false);
		}
	}

	private String activityTitle;
	private ExpandableListAdapter adapter;
	private String jsonRoster;
	private boolean refreshInProgress;
	private Roster roster;
	private String rosterProfileUrl;
	private String rosterUrl;
	private AsyncTask<String, Void, Roster> task;
	private String courseSectionTitle;

	private void handleIntent(Intent intent) {
		roster = new Roster();
		final Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
		if (appData != null) {
			rosterUrl = appData.getString("rosterUrl");
			rosterProfileUrl = intent.getStringExtra("rosterProfileUrl");
			activityTitle = appData.getString("title");
			courseSectionTitle = appData.getString("courseSectionTitle");
			setTitle(activityTitle + " " + getResources().getString(R.string.roster) + " - "
					+ intent.getStringExtra(SearchManager.QUERY));
		} else {
			rosterUrl = intent.getStringExtra("rosterUrl");
			rosterProfileUrl = intent.getStringExtra("rosterProfileUrl");
			activityTitle = intent.getStringExtra("title");
			courseSectionTitle = intent.getStringExtra("courseSectionTitle");
			setTitle(activityTitle + " " + getResources().getString(R.string.roster));
		}
		
		UICustomizer.style(this);
		if (courseSectionTitle == null) {
			findViewById(R.id.sectionTitle).setVisibility(View.GONE);
		} else {
			((TextView) findViewById(R.id.sectionTitle)).setText(courseSectionTitle);
		}
		final DataCache cache = ((EllucianApplication) getApplication())
				.getDataCache();
		final boolean current = cache.isCurrent(this, rosterUrl);
		final String cachedContent = cache.getCache(this, rosterUrl);
		if (cachedContent != null) {
			try {
				jsonRoster = cachedContent;
				final Object o = cache.getCacheObject(rosterUrl);
				if (o != null && o instanceof List) {
					roster = (Roster) o;
				} else {
					roster = RosterParser.parse(jsonRoster);
					cache.putCacheObject(rosterUrl, roster);
				}
				setList();
			} catch (final JSONException e) {
				Log.e(EllucianApplication.TAG, "Can't parse json in roster");
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
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		@SuppressWarnings("unchecked") final HashMap<String, String> mapv = (HashMap<String, String>) adapter
				.getChild(groupPosition, childPosition);
		String username = mapv.get("username");
		if (username != null) {
			final Intent intent = new Intent(RosterActivity.this,
					ProfileActivity.class);
			intent.putExtra("username", username);
			intent.putExtra("domain", mapv.get("domain"));
			intent.putExtra("profileUrl", rosterProfileUrl);
			intent.putExtra("preferredName", mapv.get("name"));
			startActivity(intent);
		} else {
			Toast.makeText(this, getResources()
					.getString(R.string.userUnlisted), Toast.LENGTH_SHORT).show();
		}
		return super.onChildClick(parent, v, groupPosition, childPosition, id);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.roster_list);
		handleIntent(getIntent());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.roster, menu);
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
		appData.putString("rosterUrl", rosterUrl);
		appData.putString("rosterProfileUrl", rosterProfileUrl);
		appData.putString("title", activityTitle);
		appData.putString("courseSectionTitle", courseSectionTitle);
		startSearch(null, false, appData, false);
		return true;
	}

	private void setList() {
		setList(null);
	}

	private void setList(String searchQuery) {
		final List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
		final List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
		Map<String, String> curGroupMap = new HashMap<String, String>();
		groupData.add(curGroupMap);
		curGroupMap.put("category", "Faculty");
		List<Map<String, String>> children = new ArrayList<Map<String, String>>();
		for (final RosterContact contact : roster.getFaculty()) {
			if (searchQuery != null) {
				if (!contact.getName().toLowerCase()
						.contains(searchQuery.toLowerCase())) {
					continue;
				}
			}
			final Map<String, String> curChildMap = new HashMap<String, String>();
			children.add(curChildMap);
			curChildMap.put("name", contact.getName());
			curChildMap.put("domain", contact.getDomain());
			curChildMap.put("username", contact.getUsername());
		}
		childData.add(children);
		curGroupMap = new HashMap<String, String>();
		groupData.add(curGroupMap);
		curGroupMap.put("category", "Students");
		children = new ArrayList<Map<String, String>>();
		for (final RosterContact contact : roster.getStudents()) {
			if (searchQuery != null) {
				if (!contact.getName().toLowerCase()
						.contains(searchQuery.toLowerCase())) {
					continue;
				}
			}
			final Map<String, String> curChildMap = new HashMap<String, String>();
			children.add(curChildMap);
			curChildMap.put("name", contact.getName());
			curChildMap.put("domain", contact.getDomain());
			curChildMap.put("username", contact.getUsername());
		}
		childData.add(children);
		// Set up our adapter
		adapter = new SimpleExpandableListAdapter(this, groupData,
				android.R.layout.simple_expandable_list_item_1,
				new String[] { "category" }, new int[] { android.R.id.text1, },
				childData, android.R.layout.simple_expandable_list_item_2,
				new String[] { "name" }, new int[] { android.R.id.text1, });
		setListAdapter(adapter);
		getExpandableListView().expandGroup(0);
		getExpandableListView().expandGroup(1);
	}

	private void update() {
		UICustomizer.setProgressBarVisible(RosterActivity.this, true);
		new UpdateRosterTask(RosterActivity.this).execute(rosterUrl);
	}
}
