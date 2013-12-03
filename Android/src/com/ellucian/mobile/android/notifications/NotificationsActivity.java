package com.ellucian.mobile.android.notifications;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.DataCache;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.UICustomizer;
import com.ellucian.mobile.android.auth.LoginActivity;
import com.ellucian.mobile.android.auth.LoginUtil;
import com.ellucian.mobile.android.configuration.IAlertModule;

public class NotificationsActivity extends ListActivity {

	private class NotificationsAdapter extends BaseAdapter {
		private List<ColleagueNotification> items;
		private final LayoutInflater mInflater;

		public NotificationsAdapter(Context context,
				List<ColleagueNotification> items) {

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

			final ColleagueNotification item = items.get(position);

			convertView = mInflater.inflate(R.layout.notifications_list_row,
					null);

			final TextView notificationDate = (TextView) convertView
					.findViewById(R.id.notificationDate);
			final TextView notificationDescription = (TextView) convertView
					.findViewById(R.id.notificationDescription);

			notificationDescription.setText(item.getDescription());

			final Date date = item.getStartDate().getTime();
			final java.text.DateFormat dateFormat = android.text.format.DateFormat
					.getDateFormat(getApplicationContext());
			notificationDate.setText(dateFormat.format(date));

			return convertView;

		}

	}

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(refreshInProgress) {
				UICustomizer.setProgressBarVisible(NotificationsActivity.this,
				false);
				refreshInProgress = false;
			}
			if(intent.hasExtra("error")) {
				Toast.makeText(NotificationsActivity.this, getResources().getString(R.string.notificationsUpdateFailed), Toast.LENGTH_LONG).show();
			}
			getData(false);
		}
		
	};

	private static final int LOGIN_RESULT = RESULT_FIRST_USER;

	private String activityTitle;

	private NotificationsAdapter adapter;

	private String jsonNotifications;

	private List<ColleagueNotification> notifications;

	private boolean refreshInProgress;

	private String url;

	private IntentFilter intentFilter;

	private void doLogin() {
		final Intent loginIntent = new Intent(NotificationsActivity.this,
				LoginActivity.class);
		startActivityForResult(loginIntent, LOGIN_RESULT);
	}

	private void doLogout() {
		LoginUtil.logout(getApplication());
		setResult(RESULT_CANCELED);
		finish();
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final boolean loggedIn = LoginUtil.isLoggedIn(getApplicationContext());
		setContentView(R.layout.notifications_list);

		final Intent intent = getIntent();
		url = intent.getStringExtra("url");
		activityTitle = intent.getStringExtra("title");

		setTitle(activityTitle);
		UICustomizer.style(this);

		
		if (!loggedIn) {
			doLogin();
		} else {
			getData(true);
		}
		intentFilter = new IntentFilter(IAlertModule.ACTION);

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(intentFilter != null) {
			registerReceiver(mIntentReceiver, intentFilter);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mIntentReceiver);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		getData(false);
	}

	@SuppressWarnings("unchecked")
	private void getData(boolean refreshIfStale) {
		notifications = new ArrayList<ColleagueNotification>();

		final DataCache cache = ((EllucianApplication) getApplication())
				.getDataCache();

		final String cachedContent = cache.getCache(this, url);
		
		final boolean current = cache.isCurrent(this, url);

		if (cachedContent != null) {
			try {
				jsonNotifications = cachedContent;
				final Object o = cache.getCacheObject(url);
				if (o != null && o instanceof List) {
					notifications = (List<ColleagueNotification>) o;
				} else {
					notifications = NotificationsParser
							.parse(jsonNotifications);
					cache.putCacheObject(url, notifications);
				}
				setList();
			} catch (final JSONException e) {
				Log.e(EllucianApplication.TAG,
						"Can't parse json in notifications");
			}
		}

		if (!current && refreshIfStale) {
			update();
		} else {
			final Intent intent = new Intent(NotificationsActivity.this,
					NotificationsService.class);
			intent.putExtra("url", this.url);
			intent.putExtra("title", activityTitle);
			intent.putExtra("network", false);
			intent.putExtra("markRead", true);
			intent.setAction(url);
			startService(intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.notifications, menu);
		return true;
	}

//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		if (task != null && refreshInProgress) {
//			task.cancel(true);
//		}
//	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final Intent intent = new Intent(NotificationsActivity.this,
				NotificationsDetailActivity.class);

		final ColleagueNotification notification = (ColleagueNotification) adapter
				.getItem(position);

		intent.putExtra("description", notification.getDescription().toString());
		if (notification.getDescriptionDetails() != null) {
			intent.putExtra("descriptionDetails", notification
					.getDescriptionDetails().toString());
		}
		if (notification.getLinkLabel() != null) {
			intent.putExtra("linkLabel", notification.getLinkLabel().toString());
		}
		if (notification.getHyperlink() != null) {
			intent.putExtra("hyperlink", notification.getHyperlink().toString());
		}
		if (notification.getLinkLabel() != null) {
			intent.putExtra("linkLabel", notification.getLinkLabel().toString());
		}
		
		final Date date = notification.getStartDate().getTime();
		final java.text.DateFormat dateFormat = android.text.format.DateFormat
				.getDateFormat(getApplicationContext());
		String dateString = dateFormat.format(date);

		intent.putExtra("startDate", dateString);

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
		if(adapter == null || (adapter != null && !adapter.items.equals(notifications))) {
			adapter = new NotificationsAdapter(this, notifications);
			setListAdapter(adapter);
		}
	}

	private void update() {
		UICustomizer.setProgressBarVisible(NotificationsActivity.this, true);
		refreshInProgress = true;
		final Intent intent = new Intent(NotificationsActivity.this,
				NotificationsService.class);
		intent.putExtra("url", this.url);
		intent.putExtra("title", activityTitle);
		intent.putExtra("network", true);
		intent.putExtra("markRead", true);
		intent.setAction(url);
		startService(intent);

	}
}
