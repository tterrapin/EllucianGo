package com.ellucian.mobile.android.notifications;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.util.Log;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.DataCache;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.auth.LoginUtil;
import com.ellucian.mobile.android.configuration.IAlertModule;

public class NotificationsService extends Service {
	private String appName;
	private boolean network;
	Runnable mTask = new Runnable() {
		@SuppressWarnings("unchecked")
		public void run() {
			Log.v(EllucianApplication.TAG, "NotificationsService running");
			try {
				final boolean loggedIn = LoginUtil
						.isLoggedIn(getApplicationContext());

				if (loggedIn) {
					if (network) {
						sendRequest();
					} else {
						List<ColleagueNotification> notifications = new ArrayList<ColleagueNotification>();

						final DataCache cache = ((EllucianApplication) getApplication())
								.getDataCache();

						final String cachedContent = cache.getCache(
								NotificationsService.this, url);

						if (cachedContent != null) {
							try {
								String jsonNotifications = cachedContent;
								final Object o = cache.getCacheObject(url);
								if (o != null && o instanceof List) {
									notifications = (List<ColleagueNotification>) o;
								} else {
									notifications = NotificationsParser
											.parse(jsonNotifications);
									cache.putCacheObject(url, notifications);
								}
								showNotification(notifications, notifications);
							} catch (final JSONException e) {
								Log.e(EllucianApplication.TAG,
										"Can't parse json in notifications");
							}
						}
					}
				} else {
					Log.v(EllucianApplication.TAG, "NotificationsService not logged in");
				}
			} catch (final Exception e) {
				Log.e(EllucianApplication.TAG, "Notifications update failed = "
						+ e);
			}
			Log.v(EllucianApplication.TAG, "NotificationsService stopping");
			NotificationsService.this.stopSelf();
		}

		private void sendRequest() throws URISyntaxException, IOException,
				ClientProtocolException, UnsupportedEncodingException,
				JSONException {
			final Context context = getApplicationContext();
			final DataCache cache = ((EllucianApplication) getApplication())
					.getDataCache();
			final String viewedCachedContent = cache.getCache(context, "read-"
					+ url);
			final String cachedContent = cache.getCache(context, url);
			final HttpClient client = new DefaultHttpClient();
			final HttpGet request = new HttpGet();
			request.setURI(new URI(url));

			final String username = LoginUtil.getUsername(context);
			final String password = LoginUtil.getPassword(context);
			request.addHeader(BasicScheme.authenticate(
					new UsernamePasswordCredentials(username, password),
					"UTF-8", false));
			final HttpResponse response = client.execute(request);
			final int status = response.getStatusLine().getStatusCode();
			if (status == HttpStatus.SC_OK) {
				final BufferedReader in = new BufferedReader(
						new InputStreamReader(
								response.getEntity().getContent(), "UTF-8"));
				final StringBuffer sb = new StringBuffer();
				String line = "";
				final String NL = System.getProperty("line.separator");
				while ((line = in.readLine()) != null) {
					sb.append(line + NL);
				}
				in.close();
				final String jsonNotifications = sb.toString();
				final List<ColleagueNotification> notifications = NotificationsParser
						.parse(jsonNotifications);
				List<ColleagueNotification> viewedNotifications = new ArrayList<ColleagueNotification>();
				cache.putAuthCache(url, jsonNotifications, notifications);
				if (markRead) {
					cache.putAuthCache("read-" + url, jsonNotifications,
							notifications);
					viewedNotifications = notifications;
				} else if (viewedCachedContent != null) {
					viewedNotifications = NotificationsParser
							.parse(viewedCachedContent);
				}

				if (!ranOnce || cachedContent == null
						|| !cachedContent.equals(jsonNotifications)) {
					showNotification(notifications, viewedNotifications);
				}
			} else {
				Log.e(EllucianApplication.TAG, "Notifications update failed = "
						+ response.getStatusLine().toString());
				broadcastIntent.putExtra("error", response.getStatusLine()
						.toString());
			}

			if (!(markRead && !network)) {
				sendBroadcast(broadcastIntent);
			}

			ranOnce = true;
		}

		private void showNotification(
				List<ColleagueNotification> currentNotifications,
				List<ColleagueNotification> cachedNotifications) {

			final DataCache cache = ((EllucianApplication) getApplication())
					.getDataCache();
			cache.recordNotificationCount(url, currentNotifications.size());
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			if (currentNotifications.size() == 0) { // cancel if none exist now
				Log.d(EllucianApplication.TAG, "Cancelling notification");
				mNotificationManager.cancel(R.string.notifications
						+ url.hashCode());
				cache.recordNotificationCount(url, currentNotifications.size());
			} else {
				int newCount = 0;
				for (final ColleagueNotification notification : currentNotifications) {
					if (!cachedNotifications.contains(notification)) {
						newCount++;
					}
				}

				final int icon = R.drawable.icon; // TODO change
				final CharSequence tickerText = appName;
				final long when = System.currentTimeMillis();
				final Notification notification = new Notification(icon,
						tickerText, when);
				notification.number = currentNotifications.size();
				notification.flags |= Notification.FLAG_NO_CLEAR
						| Notification.FLAG_ONGOING_EVENT;
				final Resources resources = getResources();
				final Context context = getApplicationContext();
				final CharSequence contentTitle = String.format(
						resources.getString(R.string.notificationsMessages),
						currentNotifications.size(),
						currentNotifications.size() == 1 ? resources
								.getString(R.string.notificationTerm)
								: resources
										.getString(R.string.notificationsTerm));
				final CharSequence contentText = String.format(
						getResources().getString(
								R.string.notificationsNewMessages),
						newCount,
						newCount == 1 ? resources
								.getString(R.string.notificationTerm)
								: resources
										.getString(R.string.notificationsTerm));
				final Intent notificationIntent = new Intent(
						NotificationsService.this, NotificationsActivity.class);
				notificationIntent.putExtra("url", url);
				notificationIntent.putExtra("title", title);
				final PendingIntent contentIntent = PendingIntent.getActivity(
						NotificationsService.this, 0, notificationIntent, 0);
				notification.setLatestEventInfo(context, contentTitle,
						contentText, contentIntent);
				mNotificationManager.notify(
						R.string.notifications + url.hashCode(), notification);
				Log.d(EllucianApplication.TAG, "Notification created");
			}
		}
	};
	private String title;
	private String url;
	private Intent broadcastIntent;
	private boolean markRead;
	private static boolean ranOnce = false;

	@Override
	public IBinder onBind(Intent arg0) {
		// no binding
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		url = intent.getStringExtra("url");
		title = intent.getStringExtra("title");
		appName = intent.getStringExtra("appName");
		network = intent.getBooleanExtra("network", true);
		markRead = intent.getBooleanExtra("markRead", false);
		broadcastIntent = new Intent(IAlertModule.ACTION);
		final Thread thr = new Thread(null, mTask, "NotificationsService");
		thr.start();
		return super.onStartCommand(intent, flags, startId);
	}
}
