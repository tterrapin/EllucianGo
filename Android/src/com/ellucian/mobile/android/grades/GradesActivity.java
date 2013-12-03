package com.ellucian.mobile.android.grades;

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
import org.json.JSONException;

import android.app.ListActivity;
import android.app.NotificationManager;
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

public class GradesActivity extends ListActivity {
	private class UpdateCoursesTask extends AsyncTask<String, Void, GradesData> {
		private final Context context;

		public UpdateCoursesTask(Context context) {
			this.context = context;
		}

		@Override
		protected GradesData doInBackground(String... urls) {
			try {
				refreshInProgress = true;
				final HttpClient client = new DefaultHttpClient();
				final HttpGet request = new HttpGet();
				request.setURI(new URI(urls[0] )); //+ "?date=" +  gradesData.getGradesAsOf()));
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
					String jsonGrades = sb.toString();

					gradesData = GradesParser.parse(jsonGrades);
					((EllucianApplication) getApplication()).getDataCache()
							.putAuthCache(urls[0], jsonGrades, gradesData);
					return gradesData;
				} else {
					throw new RuntimeException(response.getStatusLine()
							.toString());
				}
			} catch (final Exception e) {
				Log.e(EllucianApplication.TAG, "Grades update failed = " + e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(GradesData terms) {
			setList();
			refreshInProgress = false;
			UICustomizer.setProgressBarVisible(GradesActivity.this, false);
		}
	}

	private static final int LOGIN_RESULT = RESULT_FIRST_USER;
	private String activityTitle;
	private String url;

	//private String jsonCourses;
	private boolean refreshInProgress;

	private AsyncTask<String, Void, GradesData> task;

	private GradesData gradesData;

	private void doLogin() {
		final Intent loginIntent = new Intent(GradesActivity.this,
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
		url = getIntent().getStringExtra("url");
		boolean forceRefresh = getIntent().getBooleanExtra("forceRefresh", false);
		setTitle(activityTitle);
		UICustomizer.style(this);
		gradesData = new GradesData();
		final boolean loggedIn = LoginUtil.isLoggedIn(getApplicationContext());
		if (!loggedIn) {
			doLogin();
		} else {
			final DataCache cache = ((EllucianApplication) getApplication())
					.getDataCache();
			final boolean current = cache.isCurrent(this, url);

			final String cachedContent = cache.getCache(this, url);
			if (cachedContent != null) {
				try {
//					jsonCourses = cachedContent;
					final Object o = cache.getCacheObject(url);
					if (o != null && o instanceof GradesData) {
						gradesData = (GradesData) o;
					} else {
						gradesData = GradesParser.parse(cachedContent);
						cache.putCacheObject(url, gradesData);
					}
					setList();
					if (!current || forceRefresh) {
						update();
					}
				} catch (final JSONException e) {
					Log.e(EllucianApplication.TAG, "Can't parse json in grades");
				}
			} else if (!current || forceRefresh) {
				update();
			}
		}
		
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotificationManager.cancel(R.string.grades
					+ url.hashCode());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.grades, menu);
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
		final Intent intent = new Intent(GradesActivity.this,
				GradesTermActivity.class);
		intent.putExtra("term", gradesData.getTerms().get(position));
		intent.putExtra("url", url);
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
		if (gradesData != null) {
			setListAdapter(new ArrayAdapter<Term>(GradesActivity.this,
					android.R.layout.simple_list_item_1, gradesData.getTerms()));
		}
	}

	private void update() {
		UICustomizer.setProgressBarVisible(GradesActivity.this, true);
		task = new UpdateCoursesTask(this);
		task.execute(url);
	}
	
	 
}
