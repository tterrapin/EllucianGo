package com.ellucian.mobile.android.events;

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

public class EventsActivity extends ListActivity {

	private class UpdateEventsTask extends
			AsyncTask<String, Void, List<EventCalendar>> {

		private final boolean authentication;
		private final Context context;

		public UpdateEventsTask(Context context, boolean authentication) {
			this.authentication = authentication;
			this.context = context;
		}

		@Override
		protected List<EventCalendar> doInBackground(String... urls) {
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
					jsonCalendar = sb.toString();

					calendar = EventsParser.parse(jsonCalendar);
					if (authentication) {
						((EllucianApplication) getApplication()).getDataCache()
								.putAuthCache(urls[0], jsonCalendar, calendar);
					} else {
						((EllucianApplication) getApplication()).getDataCache()
								.putCache(urls[0], jsonCalendar, calendar);

					}
					return calendar;

				} else {
					throw new RuntimeException(response.getStatusLine()
							.toString());
				}

			} catch (final Exception e) {
				Log.e(EllucianApplication.TAG, "Events update failed = " + e);
			}
			return null;

		}

		@Override
		protected void onPostExecute(List<EventCalendar> events) {
			setList();
			refreshInProgress = false;
			UICustomizer.setProgressBarVisible(EventsActivity.this, false);

		}
	}

	static final int CALENDAR_ACTIVITY_FINISHED = RESULT_FIRST_USER;
	static final int CALENDAR_ACTIVITY_LOGIN_REQUESTED = RESULT_FIRST_USER +  2;

	static final int CALENDAR_ACTIVITY_LOGOUT_REQUESTED = RESULT_FIRST_USER + 3;
	static final int CALENDAR_REDRAW_REQUESTED = RESULT_FIRST_USER + 4;


	static final int CALENDAR_ACTIVITY_REFRESH = RESULT_FIRST_USER + 1;

	private static final int LOGIN_RESULT = RESULT_FIRST_USER ;
	private static final int SHOW_CALENDAR_RESULT = RESULT_FIRST_USER +  1;
	private String activityTitle;
	private String authenticatedUrl;
	private String calendarTitleToDisplay;
	private String jsonCalendar;

	private List<EventCalendar> calendar;

	private String publicUrl;

	private boolean refreshInProgress;
	
	private AsyncTask<String, Void, List<EventCalendar>> task;
	private boolean updating;

	private String chooseUrl() {
		final boolean loggedIn = LoginUtil.isLoggedIn(getApplicationContext());
		if (loggedIn) {
			return authenticatedUrl;
		} else {
			return publicUrl;
		}
	}

	private void doLogin() {
		final Intent loginIntent = new Intent(EventsActivity.this,
				LoginActivity.class);
		startActivityForResult(loginIntent, LOGIN_RESULT);
	}

	private void doLogout() {
		setListAdapter(new ArrayAdapter<EventCalendar>(EventsActivity.this,

		android.R.layout.simple_list_item_1, new ArrayList<EventCalendar>()));
		LoginUtil.logout(getApplication());
		update();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_RESULT) {
			if (resultCode == RESULT_OK) {
				update();
			}
			if (updating) {
				updating = false;
				testStartEventsDetailActivity(calendarTitleToDisplay);
			}
		} else if (requestCode == SHOW_CALENDAR_RESULT) {
			if (resultCode == 0) {
				return;
			}
			final int position = data.getIntExtra("position", 0);
			final String calendarTitle = calendar.get(position).title;

			switch (resultCode) {
			case CALENDAR_ACTIVITY_FINISHED:
				if(calendar.size() == 1) {
					finish();
				}
				break;
			case CALENDAR_ACTIVITY_REFRESH:
				update();

				testStartEventsDetailActivity(calendarTitle);
				break;
			case CALENDAR_ACTIVITY_LOGIN_REQUESTED:
				updating = true;

				calendarTitleToDisplay = calendarTitle;
				doLogin();
				break;
			case CALENDAR_ACTIVITY_LOGOUT_REQUESTED:
				doLogout();
				break;
			case CALENDAR_REDRAW_REQUESTED:
				setupDataFromCache();
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.events_list);
		activityTitle = getIntent().getStringExtra("title");
		setTitle(activityTitle);
		UICustomizer.style(this);

		calendar = new ArrayList<EventCalendar>();

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
				jsonCalendar = cachedContent;
				final Object o = cache.getCacheObject(currentUrl);
				if(o != null && o instanceof List) {
					calendar = (List<EventCalendar>) o;
				} else {
					calendar = EventsParser.parse(jsonCalendar);
					cache.putCacheObject(currentUrl, calendar);
				}
				setList();
				if(!current && calendar.size() != 1) {
					update();
				}
			} catch (final JSONException e) {
				Log.e(EllucianApplication.TAG, "Can't parse json in events");
			}
		} else if (!current) {
			update();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.events, menu);
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
		startEventsDetailActivity(position);

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
		menu.findItem(R.id.menu_login).setEnabled(!refreshInProgress);
		menu.findItem(R.id.menu_logout).setEnabled(!refreshInProgress);
		return super.onPrepareOptionsMenu(menu);
	}

	private void setList() {
		if (calendar != null) {
			if (calendar.size() == 1) {
				startEventsDetailActivity(0);
				finish();
			} else if (calendar.size() > 0) {
				setListAdapter(new ArrayAdapter<EventCalendar>(EventsActivity.this,
						android.R.layout.simple_list_item_1, calendar));
			}
		}
	}

	private void startEventsDetailActivity(int position) {
		final Intent intent = new Intent(EventsActivity.this,
				EventsDetailActivity.class);
		intent.putExtra("position", position);
		intent.putExtra("title", calendar.get(position).title);
		intent.putExtra("publicUrl", publicUrl);
		intent.putExtra("authenticatedUrl", authenticatedUrl);
		intent.putExtra("moduleTitle", activityTitle);
		
		startActivityForResult(intent, SHOW_CALENDAR_RESULT);

	}

	private void testStartEventsDetailActivity(String title) {

		for (int i = 0; i < calendar.size(); i++) {
			if (calendar.get(i).title.equals(title)) {
				startEventsDetailActivity(i);
				break;
			}
		}

	}

	private void update() {
		UICustomizer.setProgressBarVisible(EventsActivity.this, true);
		final boolean loggedIn = LoginUtil.isLoggedIn(getApplicationContext());
		new UpdateEventsTask(this, loggedIn).execute(chooseUrl());

	}

}
