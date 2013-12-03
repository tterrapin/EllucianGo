package com.ellucian.mobile.android.courses;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
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
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.DataCache;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.UICustomizer;
import com.ellucian.mobile.android.auth.LoginActivity;
import com.ellucian.mobile.android.auth.LoginUtil;
import com.ellucian.mobile.android.home.HomeActivity;

public class CoursesActivity extends ListActivity {
	private class UpdateCoursesTask extends AsyncTask<String, Void, List<Term>> {
		private final Context context;

		public UpdateCoursesTask(Context context) {
			this.context = context;
		}

		@Override
		protected List<Term> doInBackground(String... urls) {
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
					jsonCourses = sb.toString();
					terms = CoursesParser.parse(jsonCourses);
					((EllucianApplication) getApplication()).getDataCache()
							.putAuthCache(urls[0], jsonCourses, terms);
					return terms;
				} else {
					throw new RuntimeException(response.getStatusLine()
							.toString());
				}
			} catch (final Exception e) {
				Log.e(EllucianApplication.TAG, "Courses update failed = " + e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<Term> terms) {
			setList();
			refreshInProgress = false;
			UICustomizer.setProgressBarVisible(CoursesActivity.this, false);
		}
	}

	// static final int FEED_ACTIVITY_FINISHED = RESULT_FIRST_USER;
	//
	// static final int FEED_ACTIVITY_LOGIN_REQUESTED = RESULT_FIRST_USER + 2;
	// static final int FEED_ACTIVITY_LOGOUT_REQUESTED = RESULT_FIRST_USER + 3;
	// static final int FEED_REDRAW_REQUESTED = RESULT_FIRST_USER + 4;
	//
	// static final int FEED_ACTIVITY_REFRESH = RESULT_FIRST_USER + 1;
	//
	private static final int LOGIN_RESULT = RESULT_FIRST_USER;
	//
	// private static final int SHOW_FEED_RESULT = RESULT_FIRST_USER + 1;
	private String activityTitle;
	private String announcementsUrl;
	private String assignmentsUrl;
	private String coursesUrl;
	private String eventsUrl;
	// private String authenticatedUrl;
	// private String feedTitleToDisplay;
	private String jsonCourses;
	private boolean refreshInProgress;
	private String rosterProfileUrl;
	private String rosterUrl;
	//
	private AsyncTask<String, Void, List<Term>> task;
	//
	private List<Term> terms;

	private void doLogin() {
		final Intent loginIntent = new Intent(CoursesActivity.this,
				LoginActivity.class);
		startActivityForResult(loginIntent, LOGIN_RESULT);
	}

	private void doLogout() {
		LoginUtil.logout(getApplication());
		final Intent intent = new Intent(this, HomeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_RESULT) {
			if (resultCode == RESULT_OK) {
				update();
			} else if (resultCode == RESULT_CANCELED) {
				finish();
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.courses_list);
		activityTitle = getIntent().getStringExtra("title");
		coursesUrl = getIntent().getStringExtra("coursesUrl");
		assignmentsUrl = getIntent().getStringExtra("assignmentsUrl");
		rosterUrl = getIntent().getStringExtra("rosterUrl");
		rosterProfileUrl = getIntent().getStringExtra("rosterProfileUrl");
		announcementsUrl = getIntent().getStringExtra("announcementsUrl");
		eventsUrl = getIntent().getStringExtra("eventsUrl");
		setTitle(activityTitle);
		UICustomizer.style(this);
		terms = new ArrayList<Term>();
		final boolean loggedIn = LoginUtil.isLoggedIn(getApplicationContext());
		if (!loggedIn) {
			doLogin();
		} else {
			setupDataFromCache();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.courses, menu);
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
		final Intent intent = new Intent(CoursesActivity.this,
				CoursesTermActivity.class);
		// intent.putExtra("term", terms.getTerms().get(position).getName());
		// intent.putExtra("url", url);
		intent.putExtra("term", terms.get(position));
		intent.putExtra("coursesUrl", coursesUrl);
		intent.putExtra("assignmentsUrl", assignmentsUrl);
		intent.putExtra("rosterUrl", rosterUrl);
		intent.putExtra("rosterProfileUrl", rosterProfileUrl);
		intent.putExtra("announcementsUrl", announcementsUrl);
		intent.putExtra("eventsUrl", eventsUrl);
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			update();
			break;
		case R.id.menu_logout:
			doLogout();
			break;
		}
		return false;
	}

	private void setList() {
		if (terms != null) {
			setListAdapter(new ArrayAdapter<Term>(CoursesActivity.this,
					android.R.layout.simple_list_item_1, terms));
		}
	}

	// @SuppressWarnings("unchecked")
	@SuppressWarnings("unchecked")
	private void setupDataFromCache() {
		final DataCache cache = ((EllucianApplication) getApplication())
				.getDataCache();
		final boolean current = cache.isCurrent(this, coursesUrl);
		final String cachedContent = cache.getCache(this, coursesUrl);
		if (cachedContent != null) {
			try {
				jsonCourses = cachedContent;
				final Object o = cache.getCacheObject(coursesUrl);
				if (o != null && o instanceof List) {
					terms = (List<Term>) o;
				} else {
					terms = CoursesParser.parse(jsonCourses);
					cache.putCacheObject(coursesUrl, terms);
				}
				setList();
				if (!current) {
					update();
				}
			} catch (final JSONException e) {
				Log.e(EllucianApplication.TAG, "Can't parse json in courses");
			}
		} else if (!current) {
			update();
		}
	}

	private void update() {
		UICustomizer.setProgressBarVisible(CoursesActivity.this, true);
		task = new UpdateCoursesTask(this);
		task.execute(coursesUrl);
	}
}
