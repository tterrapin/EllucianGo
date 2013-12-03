package com.ellucian.mobile.android.courses;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
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
import com.ellucian.mobile.android.events.Event;
import com.ellucian.mobile.android.events.EventCalendar;
import com.ellucian.mobile.android.events.EventContentActivity;
import com.ellucian.mobile.android.events.EventsFeedAdapter;
import com.ellucian.mobile.android.events.EventsParser;

public class CourseEventsActivity extends ListActivity {

	private class UpdateEventsTask extends
			AsyncTask<String, Void, EventCalendar> {

		private final Context context;

		public UpdateEventsTask(Context context) {

			this.context = context;
		}

		@Override
		protected EventCalendar doInBackground(String... urls) {
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
					jsonCalendar = sb.toString();

					calendar = EventsParser.parse(jsonCalendar).get(0);

					((EllucianApplication) getApplication()).getDataCache()
							.putAuthCache(urls[0], jsonCalendar, calendar);

					return calendar;

				} else {
					throw new RuntimeException(response.getStatusLine()
							.toString());
				}

			} catch (final Exception e) {
				Log.e(EllucianApplication.TAG,
						"Course events detail list update failed = " + e);
			}
			return null;

		}

		@Override
		protected void onPostExecute(EventCalendar calendar) {
			if (calendar != null) {
				
				List<Event> items = calendar.items;
				setListAdapter(new EventsFeedAdapter(
						CourseEventsActivity.this, items, dateFormat,
						timeFormat));

			}
			refreshInProgress = false;
			UICustomizer
					.setProgressBarVisible(CourseEventsActivity.this, false);
		}
	}

	private String authenticatedUrl;
	private java.text.DateFormat dateFormat;
	private java.text.DateFormat timeFormat;
	private String jsonCalendar;

	private AsyncTask<String, Void, EventCalendar> task;

	private boolean refreshInProgress;

	private EventCalendar calendar;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.course_events_list);

		dateFormat = android.text.format.DateFormat
				.getDateFormat(getApplicationContext());
		timeFormat = android.text.format.DateFormat
				.getTimeFormat(getApplicationContext());

		final Intent intent = getIntent();

		authenticatedUrl = intent.getStringExtra("authenticatedUrl");
		final Course course = intent.getParcelableExtra("course");
		setTitle(course.getName() + " " + getResources().getString(R.string.events));
		UICustomizer.style(this);
		if (course.getSectionTitle() == null) {
			findViewById(R.id.sectionTitle).setVisibility(View.GONE);
		} else {
			((TextView) findViewById(R.id.sectionTitle)).setText(course
					.getSectionTitle());
		}


		final DataCache cache = ((EllucianApplication) getApplication())
				.getDataCache();

		final boolean current = cache.isCurrent(this, authenticatedUrl);

		final String cachedContent = cache.getCache(this, authenticatedUrl);

		if (cachedContent != null) {
			try {
				jsonCalendar = cachedContent;
				final Object o = cache.getCacheObject(authenticatedUrl);
				if (o != null && o instanceof List) {
					calendar = (EventCalendar) o;
				} else {
					calendar = EventsParser.parse(jsonCalendar).get(0);
					cache.putCacheObject(authenticatedUrl, calendar);
				}
				calendar = (EventCalendar) cache.getCacheObject(authenticatedUrl);
				List<Event> items = calendar.items;

				setListAdapter(new EventsFeedAdapter(this, items, dateFormat,
						timeFormat));
				if (!current) {
					update();
				}
			} catch (final JSONException e) {
				Log.e(EllucianApplication.TAG,
						"Can't parse json in course events");
			}
		} else if (!current) {
			update();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.course_events, menu);
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

		final Event item = (Event) l.getItemAtPosition(position);

		final Intent intent = new Intent(CourseEventsActivity.this,
				EventContentActivity.class);
		intent.putExtra("title", item.getTitle());

		intent.putExtra("startDate",
				dateFormat.format(item.getStartDate().getTime()));
		intent.putExtra("endDate",
				dateFormat.format(item.getEndDate().getTime()));
		intent.putExtra("startDateLong", item.getStartDate().getTime()
				.getTime());
		intent.putExtra("endDateLong", item.getEndDate().getTime().getTime());

		if (!item.isAllDay()) {
			intent.putExtra("startTime",
					timeFormat.format(item.getStartDate().getTime()));
			intent.putExtra("endTime",
					timeFormat.format(item.getEndDate().getTime()));
		}
		intent.putExtra("location", item.getLocation());
		intent.putExtra("description", item.getDescription());
		intent.putExtra("allDay", item.isAllDay());
		startActivity(intent);

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
		UICustomizer.setProgressBarVisible(CourseEventsActivity.this, true);
		task = new UpdateEventsTask(this).execute(authenticatedUrl);
	}

}