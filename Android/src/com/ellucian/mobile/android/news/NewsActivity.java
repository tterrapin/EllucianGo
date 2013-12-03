package com.ellucian.mobile.android.news;

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

public class NewsActivity extends ListActivity {

	private class UpdateNewsTask extends
			AsyncTask<String, Void, List<NewsFeed>> {

		private final boolean authentication;
		private final Context context;

		public UpdateNewsTask(Context context, boolean authentication) {
			this.authentication = authentication;
			this.context = context;
		}

		@Override
		protected List<NewsFeed> doInBackground(String... urls) {
			try {
				refreshInProgress = true;
				final HttpClient client = new DefaultHttpClient();
				final HttpGet request = new HttpGet();
				request.setURI(new URI(urls[0]));

				if (authentication) {
					final String username = LoginUtil.getUsername(context);
					final String password = LoginUtil.getPassword(context);

					request.addHeader(BasicScheme
							.authenticate(new UsernamePasswordCredentials(
									username, password), "UTF-8", false));
				}

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
					jsonNews = sb.toString();

					news = NewsParser.parse(jsonNews);
					if (authentication) {
						((EllucianApplication) getApplication()).getDataCache()
								.putAuthCache(urls[0], jsonNews, news);
					} else {
						((EllucianApplication) getApplication()).getDataCache()
								.putCache(urls[0], jsonNews, news);

					}
					return news;

				} else {
					throw new RuntimeException(response.getStatusLine()
							.toString());
				}

			} catch (final Exception e) {
				Log.e(EllucianApplication.TAG, "News update failed = " + e);
			}
			return null;

		}

		@Override
		protected void onPostExecute(List<NewsFeed> news) {
			setList();
			refreshInProgress = false;
			UICustomizer.setProgressBarVisible(NewsActivity.this, false);

		}
	}

	static final int FEED_ACTIVITY_FINISHED = RESULT_FIRST_USER;

	static final int FEED_ACTIVITY_LOGIN_REQUESTED = RESULT_FIRST_USER + 2;
	static final int FEED_ACTIVITY_LOGOUT_REQUESTED = RESULT_FIRST_USER + 3;
	static final int FEED_REDRAW_REQUESTED = RESULT_FIRST_USER + 4;

	static final int FEED_ACTIVITY_REFRESH = RESULT_FIRST_USER + 1;

	private static final int LOGIN_RESULT = RESULT_FIRST_USER;

	private static final int SHOW_FEED_RESULT = RESULT_FIRST_USER + 1;
	private String activityTitle;
	private String authenticatedUrl;
	private String feedTitleToDisplay;
	private String jsonNews;

	private List<NewsFeed> news;

	private String publicUrl;

	private boolean refreshInProgress;

	private AsyncTask<String, Void, List<NewsFeed>> task;
	private boolean updatingFromFeed;

	private boolean enableLogin;

	private String chooseUrl() {
		final boolean loggedIn = LoginUtil.isLoggedIn(getApplicationContext());
		if (loggedIn) {
			return authenticatedUrl;
		} else {
			return publicUrl;
		}
	}

	private void doLogin() {
		final Intent loginIntent = new Intent(NewsActivity.this,
				LoginActivity.class);
		startActivityForResult(loginIntent, LOGIN_RESULT);
	}

	private void doLogout() {
		setListAdapter(new ArrayAdapter<NewsFeed>(NewsActivity.this,

		android.R.layout.simple_list_item_1, new ArrayList<NewsFeed>()));
		LoginUtil.logout(getApplication());
		update();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_RESULT) {
			if (resultCode == RESULT_OK) {
				update();
			}
			if (updatingFromFeed) {
				updatingFromFeed = false;
				testStartNewsDetailActivity(feedTitleToDisplay);
			}
		} else if (requestCode == SHOW_FEED_RESULT) {
			if (resultCode == 0) {
				return;
			}
			final int position = data.getIntExtra("position", 0);
			final String newsTitle = news.get(position).title;

			switch (resultCode) {
			case FEED_ACTIVITY_FINISHED:
				if (news.size() == 1) {
					finish();
				}
				break;
			case FEED_ACTIVITY_REFRESH:
				update();

				testStartNewsDetailActivity(newsTitle);
				break;
			case FEED_ACTIVITY_LOGIN_REQUESTED:
				updatingFromFeed = true;

				feedTitleToDisplay = newsTitle;
				doLogin();
				break;
			case FEED_ACTIVITY_LOGOUT_REQUESTED:
				doLogout();

				// testStartNewsDetailActivity(newsTitle);
				break;
			case FEED_REDRAW_REQUESTED:
				setupDataFromCache();
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.news_list);
		activityTitle = getIntent().getStringExtra("title");
		setTitle(activityTitle);
		UICustomizer.style(this);

		news = new ArrayList<NewsFeed>();

		publicUrl = getIntent().getStringExtra("publicUrl");
		authenticatedUrl = getIntent().getStringExtra("authenticatedUrl");

		setupDataFromCache();
	}

	@SuppressWarnings("unchecked")
	private void setupDataFromCache() {
		final String currentUrl = chooseUrl();

		final DataCache cache = ((EllucianApplication) getApplication())
				.getDataCache();

		final boolean current = cache.isCurrent(this, currentUrl);

		final String cachedContent = cache.getCache(this, currentUrl);

		if (cachedContent != null) {
			try {
				jsonNews = cachedContent;
				final Object o = cache.getCacheObject(currentUrl);
				if (o != null && o instanceof List) {
					news = (List<NewsFeed>) o;
				} else {
					news = NewsParser.parse(jsonNews);
					cache.putCacheObject(currentUrl, news);
				}
				setList();
				if (!current && news.size() != 1) {
					update();
				}
			} catch (final JSONException e) {
				Log.e(EllucianApplication.TAG, "Can't parse json in news");
			}
		} else if (!current) {
			update();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.news, menu);
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

		startNewsDetailActivity(position);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			update();
			break;
		case R.id.menu_login:
			doLogin();
			break;
		case R.id.menu_logout:
			doLogout();
			break;
		}
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_refresh).setEnabled(!refreshInProgress);
		final boolean loggedIn = LoginUtil.isLoggedIn(getApplicationContext());
		final boolean allowLogin = LoginUtil.allowLogin(getApplication());

		menu.findItem(R.id.menu_login).setVisible(!loggedIn && allowLogin);
		menu.findItem(R.id.menu_logout).setVisible(loggedIn && allowLogin);
		menu.findItem(R.id.menu_login).setEnabled(!refreshInProgress && enableLogin);
		menu.findItem(R.id.menu_logout).setEnabled(!refreshInProgress && enableLogin);
		return super.onPrepareOptionsMenu(menu);
	}

	private void setList() {
		if (news != null) {
			if (news.size() == 1) {
				startNewsDetailActivity(0);
				finish();
			} else if (news.size() > 0) {
				setListAdapter(new ArrayAdapter<NewsFeed>(NewsActivity.this,
						android.R.layout.simple_list_item_1, news));
				enableLogin = true;
			}
		}
	}

	private void startNewsDetailActivity(int position) {
		final Intent intent = new Intent(NewsActivity.this,
				NewsDetailActivity.class);
		intent.putExtra("position", position);
		intent.putExtra("title", news.get(position).title);
		intent.putExtra("publicUrl", publicUrl);
		intent.putExtra("authenticatedUrl", authenticatedUrl);
		intent.putExtra("moduleTitle", activityTitle);

		startActivityForResult(intent, SHOW_FEED_RESULT);

	}

	private void testStartNewsDetailActivity(String title) {

		for (int i = 0; i < news.size(); i++) {
			if (news.get(i).title.equals(title)) {
				startNewsDetailActivity(i);
				break;
			}
		}

	}

	private void update() {
		UICustomizer.setProgressBarVisible(NewsActivity.this, true);
		final boolean loggedIn = LoginUtil.isLoggedIn(getApplicationContext());
		task = new UpdateNewsTask(this, loggedIn).execute(chooseUrl());

	}
}
