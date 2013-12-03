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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.DataCache;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.UICustomizer;
import com.ellucian.mobile.android.auth.LoginActivity;
import com.ellucian.mobile.android.auth.LoginUtil;

public class NewsDetailActivity extends ListActivity {



	private static final int LOGIN_RESULT = RESULT_FIRST_USER;
	private String authenticatedUrl;
	private List<NewsFeed> feeds;
	private String moduleTitle;

	private int position;

	private String publicUrl;
	private String title;
	
	private java.text.DateFormat dateFormat;
	private java.text.DateFormat timeFormat;

	private String chooseUrl() {
		final boolean loggedIn = LoginUtil.isLoggedIn(getApplicationContext());
		if (loggedIn) {
			return authenticatedUrl;
		} else {
			return publicUrl;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_RESULT) {
			if (resultCode == RESULT_OK) {
				if (feeds.size() == 1) {
					final Intent intent = new Intent(NewsDetailActivity.this,
							NewsActivity.class);
					intent.putExtra("position", position);
					intent.putExtra("publicUrl", publicUrl);
					intent.putExtra("authenticatedUrl", authenticatedUrl);
					intent.putExtra("title", moduleTitle);
					startActivity(intent);
					finish();
				} else {
					data.putExtra("position", position);
					setResult(NewsActivity.FEED_ACTIVITY_REFRESH, data);
					finish();
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.news_list);
		
		dateFormat = android.text.format.DateFormat
				.getDateFormat(getApplicationContext());
		timeFormat = android.text.format.DateFormat
				.getTimeFormat(getApplicationContext());


		final Intent intent = getIntent();
		position = intent.getIntExtra("position", 0);
		title = intent.getStringExtra("title");

		moduleTitle = intent.getStringExtra("moduleTitle");
		publicUrl = intent.getStringExtra("publicUrl");
		authenticatedUrl = intent.getStringExtra("authenticatedUrl");

		this.setTitle(title);
		UICustomizer.style(this);

		List<NewsItem> items = new ArrayList<NewsItem>();
		
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
				feeds = (List<NewsFeed>) cache.getCacheObject(currentUrl);
				items = feeds.get(position).items;

				setListAdapter(new NewsFeedAdapter(this, items, ((EllucianApplication)getApplication()).getImageLoader()));
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
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		final NewsItem item = (NewsItem) l.getItemAtPosition(position);
		if (item.getContent() != null) {
			final Intent intent = new Intent(NewsDetailActivity.this,
					NewsContentActivity.class);
			intent.putExtra("title", item.getTitle());
			intent.putExtra("image", item.getImage());
			intent.putExtra("content", item.getContent());
			intent.putExtra("website", item.getWebsite());
			
			intent.putExtra("date", dateFormat.format(item.getDate().getTime()));
			
				intent.putExtra("time", timeFormat.format(item.getDate().getTime()));
				
			startActivity(intent);
		} else if (item.getWebsite() != null) {

			final Intent websiteIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(item.getWebsite()));
			startActivity(websiteIntent);
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final Intent data = new Intent();
		data.putExtra("position", position);

		switch (item.getItemId()) {
		case R.id.menu_refresh:
			update();
			break;
		case R.id.menu_login:
			if (feeds.size() == 1) {
				final Intent loginIntent = new Intent(NewsDetailActivity.this,
						LoginActivity.class);
				startActivityForResult(loginIntent, LOGIN_RESULT);
				// finish();
			} else {
				setResult(NewsActivity.FEED_ACTIVITY_LOGIN_REQUESTED, data);
				finish();
			}
			break;
		case R.id.menu_logout:
			setResult(NewsActivity.FEED_ACTIVITY_LOGOUT_REQUESTED, data);
			finish();
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
		menu.findItem(R.id.menu_login).setEnabled(!refreshInProgress);
		menu.findItem(R.id.menu_logout).setEnabled(!refreshInProgress);
		return super.onPrepareOptionsMenu(menu);
	}

	private void update() {
		UICustomizer.setProgressBarVisible(NewsDetailActivity.this, true);
		final boolean loggedIn = LoginUtil.isLoggedIn(getApplicationContext());
		task = new UpdateNewsTask(this, loggedIn).execute(chooseUrl());

	}
	
	private AsyncTask<String, Void, List<NewsFeed>> task;
	private boolean refreshInProgress;

	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (task != null && refreshInProgress) {
			task.cancel(true);
		}
	}
	
	private String jsonNews;

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
				setResult(NewsActivity.FEED_REDRAW_REQUESTED, new Intent());
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
		protected void onPostExecute(List<NewsFeed> feeds) {
			if (feeds != null && feeds.size() > position) {
				NewsFeed feed = feeds.get(position);
				if (feed.title.equals(title)) {
					List<NewsItem> items = feeds.get(position).items;
					setListAdapter(new NewsFeedAdapter(NewsDetailActivity.this,
							items,  ((EllucianApplication)getApplication()).getImageLoader()));
				} else {
					finish();
				}
			}
			refreshInProgress = false;
			UICustomizer.setProgressBarVisible(NewsDetailActivity.this,
					false);
		}
	}
	
	private List<NewsFeed> news;


}
