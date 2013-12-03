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
import android.widget.ListView;
import android.widget.TextView;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.DataCache;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.UICustomizer;
import com.ellucian.mobile.android.auth.LoginUtil;
import com.ellucian.mobile.android.news.NewsFeed;
import com.ellucian.mobile.android.news.NewsFeedAdapter;
import com.ellucian.mobile.android.news.NewsItem;
import com.ellucian.mobile.android.news.NewsParser;

public class CourseAnnouncementsActivity extends ListActivity {

	private String authenticatedUrl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.course_announcements_list);

		final Intent intent = getIntent();
		authenticatedUrl = intent.getStringExtra("authenticatedUrl");

		final Course course = intent.getParcelableExtra("course");
		setTitle(course.getName() + " " + getResources().getString(R.string.announcements));
		UICustomizer.style(this);
		if (course.getSectionTitle() == null) {
			findViewById(R.id.sectionTitle).setVisibility(View.GONE);
		} else {
			((TextView) findViewById(R.id.sectionTitle)).setText(course
					.getSectionTitle());
		}

		List<NewsItem> items = new ArrayList<NewsItem>();

		final DataCache cache = ((EllucianApplication) getApplication())
				.getDataCache();

		final boolean current = cache.isCurrent(this, authenticatedUrl);

		final String cachedContent = cache.getCache(this, authenticatedUrl);

		if (cachedContent != null) {
			try {
				jsonNews = cachedContent;
				final Object o = cache.getCacheObject(authenticatedUrl);
				if (o != null && o instanceof List) {
					news = (NewsFeed) o;
				} else {
					news = NewsParser.parse(jsonNews).get(0);
					cache.putCacheObject(authenticatedUrl, news);
				}
				news = (NewsFeed) cache.getCacheObject(authenticatedUrl);
				items = news.items;

				setListAdapter(new NewsFeedAdapter(this, items,
						((EllucianApplication) getApplication())
								.getImageLoader()));
				if (!current) {
					update();
				}
			} catch (final JSONException e) {
				Log.e(EllucianApplication.TAG,
						"Can't parse json in course announcements");
			}
		} else if (!current) {
			update();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.course_announcements, menu);
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		final NewsItem item = (NewsItem) l.getItemAtPosition(position);
		// if (item.getContent() != null) {
		final Intent intent = new Intent(CourseAnnouncementsActivity.this,
				CourseAnnouncementContentActivity.class);
		intent.putExtra("title", item.getTitle());
		intent.putExtra("image", item.getImage());
		intent.putExtra("content", item.getContent());
		intent.putExtra("website", item.getWebsite());
		startActivity(intent);
		// } else if (item.getWebsite() != null) {
		//
		// final Intent websiteIntent = new Intent(Intent.ACTION_VIEW,
		// Uri.parse(item.getWebsite()));
		// startActivity(websiteIntent);
		// }

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_refresh:
			update();
			break;
		}
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_refresh).setEnabled(!refreshInProgress);
		return super.onPrepareOptionsMenu(menu);
	}

	private void update() {
		UICustomizer.setProgressBarVisible(CourseAnnouncementsActivity.this,
				true);
		task = new UpdateNewsTask(this).execute(authenticatedUrl);

	}

	private AsyncTask<String, Void, NewsFeed> task;
	private boolean refreshInProgress;

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (task != null && refreshInProgress) {
			task.cancel(true);
		}
	}

	private String jsonNews;

	private class UpdateNewsTask extends AsyncTask<String, Void, NewsFeed> {

		private final Context context;

		public UpdateNewsTask(Context context) {

			this.context = context;
		}

		@Override
		protected NewsFeed doInBackground(String... urls) {
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
					jsonNews = sb.toString();

					news = NewsParser.parse(jsonNews).get(0);

					((EllucianApplication) getApplication()).getDataCache()
							.putAuthCache(urls[0], jsonNews, news);

					return news;

				} else {
					throw new RuntimeException(response.getStatusLine()
							.toString());
				}

			} catch (final Exception e) {
				Log.e(EllucianApplication.TAG,
						"Course announcements update failed = " + e);
			}
			return null;

		}

		@Override
		protected void onPostExecute(NewsFeed feed) {
			if (feed != null) {

				List<NewsItem> items = feed.items;
				setListAdapter(new NewsFeedAdapter(
						CourseAnnouncementsActivity.this, items,
						((EllucianApplication) getApplication())
								.getImageLoader()));

			}
			refreshInProgress = false;
			UICustomizer.setProgressBarVisible(
					CourseAnnouncementsActivity.this, false);
		}
	}

	private NewsFeed news;

}
