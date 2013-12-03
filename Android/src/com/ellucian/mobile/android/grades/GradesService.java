package com.ellucian.mobile.android.grades;

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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.DataCache;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.auth.LoginUtil;

public class GradesService extends Service {
	private String appName;

	private Runnable mTask = new Runnable() {

		public void run() {
			try {
				Log.v(EllucianApplication.TAG, "Grades Service running");
				final Context context = getApplicationContext();
				final DataCache cache = ((EllucianApplication) getApplication())
						.getDataCache();

				final boolean loggedIn = LoginUtil
						.isLoggedIn(getApplicationContext());
				if (loggedIn) {

					final HttpClient client = new DefaultHttpClient();
					final HttpGet request = new HttpGet();
					final String cachedContent = cache.getCache(context, url);
					String backgroundCachedContent = cache.getCache(context,
							"background-" + url);

					if (cachedContent != null) {
						final GradesData viewedGrades = GradesParser
								.parse(cachedContent);
						final String lastDate = viewedGrades.getGradesAsOf();

						request.setURI(new URI(url + "?date=" + lastDate));
					} else {
						request.setURI(new URI(url));
					}

					final String username = LoginUtil.getUsername(context);
					final String password = LoginUtil.getPassword(context);
					request.addHeader(BasicScheme
							.authenticate(new UsernamePasswordCredentials(
									username, password), "UTF-8", false));
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
						final String jsonGrades = sb.toString();
						final List<Term> terms = GradesParser
								.parseJson(jsonGrades);

						if (backgroundCachedContent != null
								&& jsonGrades.endsWith(backgroundCachedContent
										.substring(backgroundCachedContent
												.indexOf("Terms")))) {
						} else {
							((EllucianApplication) getApplication())
									.getDataCache().putAuthCache(
											"background-" + url, jsonGrades,
											terms);

							if (terms.size() == 0) {
								Log.d(EllucianApplication.TAG,
										"Canceling grades notification");
								final NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
								mNotificationManager.cancel(R.string.grades
										+ url.hashCode());
							} else {
								showNotification(terms);
							}
						}
					} else {
						Log.e(EllucianApplication.TAG,
								"Grades (notification) update failed = "
										+ response.getStatusLine().toString());
					}
				} else {
					Log.v(EllucianApplication.TAG, "GradesService not logged in");
				}
			} catch (final Exception e) {
				Log.e(EllucianApplication.TAG,
						"Grades (notification) update failed = " + e);

			}
			Log.v(EllucianApplication.TAG, "GradesService stopping");
			GradesService.this.stopSelf();
		}

		private void showNotification(List<Term> terms) {
			int count = 0;

			String contentText = "";
			for (final Term t : terms) {
				for (final Course c : t.getCourses()) {
					if (c.getFinalGrade() != null || c.getGrades().size() > 0) {
						contentText += " " + c.getName();
						count++;
					}
				}
			}
			final int icon = R.drawable.icon; // TODO change
			final CharSequence tickerText = appName;
			final long when = System.currentTimeMillis();
			final Notification notification = new Notification(icon,
					tickerText, when);
			notification.number = count;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;

			final Context context = getApplicationContext();
			final CharSequence contentTitle = getResources().getString(
					R.string.gradesUpdated);

			final Intent gradesIntent = new Intent(GradesService.this,
					GradesActivity.class);
			gradesIntent.putExtra("url", url);
			gradesIntent.putExtra("title", title);
			gradesIntent.putExtra("forceRefresh", true);
			final PendingIntent contentIntent = PendingIntent.getActivity(
					GradesService.this, 0, gradesIntent, 0);
			notification.contentIntent = contentIntent;
			notification.setLatestEventInfo(context, contentTitle,
					contentText.trim(), contentIntent);
			final NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			mNotificationManager.notify(R.string.grades + url.hashCode(),
					notification);
			Log.d(EllucianApplication.TAG, "Grade notification created");

		}
	};
	private String title;
	private String url;

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
		final Thread thr = new Thread(null, mTask, "GradesService");
		thr.start();
		return super.onStartCommand(intent, flags, startId);
	}
}
